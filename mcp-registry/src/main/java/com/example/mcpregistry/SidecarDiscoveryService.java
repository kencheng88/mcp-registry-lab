package com.example.mcpregistry;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.SharedInformerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SidecarDiscoveryService {

    private static final Logger log = LoggerFactory.getLogger(SidecarDiscoveryService.class);

    // 儲存已發現的 Sidecar 資訊: Pod Name -> Base URL
    private final Map<String, String> discoveredSidecars = new ConcurrentHashMap<>();

    private final KubernetesClient kubernetesClient;
    private SharedIndexInformer<Pod> podInformer;

    public SidecarDiscoveryService(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    @PostConstruct
    public void init() {
        log.info("初始化 K8s Sidecar 偵測服務...");

        SharedInformerFactory informerFactory = kubernetesClient.informers();
        podInformer = informerFactory.sharedIndexInformerFor(Pod.class, 0);

        podInformer.addEventHandler(new ResourceEventHandler<Pod>() {
            @Override
            public void onAdd(Pod pod) {
                handlePodChange(pod, "新增");
            }

            @Override
            public void onUpdate(Pod oldPod, Pod newPod) {
                handlePodChange(newPod, "更新");
            }

            @Override
            public void onDelete(Pod pod, boolean deletedFinalStateUnknown) {
                String podName = pod.getMetadata().getName();
                if (discoveredSidecars.remove(podName) != null) {
                    log.info("Sidecar 移除: {}, 目前剩餘數: {}", podName, discoveredSidecars.size());
                }
            }
        });

        informerFactory.startAllRegisteredInformers();
    }

    private void handlePodChange(Pod pod, String action) {
        String podName = pod.getMetadata().getName();

        // 檢查標籤，判斷是否為 mcp-server-sidecar
        Map<String, String> labels = pod.getMetadata().getLabels();
        if (labels != null && "mcp-server-sidecar".equals(labels.get("component"))) {
            String podIp = pod.getStatus().getPodIP();
            if (podIp != null && "Running".equals(pod.getStatus().getPhase())) {
                // 假設 Sidecar 統一開在 8081 Port
                String baseUrl = "http://" + podIp + ":8081";
                discoveredSidecars.put(podName, baseUrl);
                log.info("Sidecar {}: {} (IP: {}), 目前總數: {}", action, podName, podIp, discoveredSidecars.size());
            }
        }
    }

    public Map<String, String> getDiscoveredSidecars() {
        return discoveredSidecars;
    }

    @PreDestroy
    public void cleanup() {
        if (podInformer != null) {
            podInformer.close();
        }
    }
}
