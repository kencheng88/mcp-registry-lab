package com.example.mcpregistry.registry.service;

import com.example.mcpregistry.rag.service.ToolEmbeddingService;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.web.client.RestTemplate;

@Service
public class SidecarDiscoveryService {

    private static final Logger log = LoggerFactory.getLogger(SidecarDiscoveryService.class);

    // 儲存已發現的 Sidecar 資訊: Pod Name -> Base URL
    private final Map<String, String> discoveredSidecars = new ConcurrentHashMap<>();

    private final KubernetesClient kubernetesClient;
    private final ToolEmbeddingService embeddingService;
    private final RestTemplate restTemplate;
    private SharedIndexInformer<Pod> podInformer;

    public SidecarDiscoveryService(KubernetesClient kubernetesClient,
            ToolEmbeddingService embeddingService,
            RestTemplate restTemplate) {
        this.kubernetesClient = kubernetesClient;
        this.embeddingService = embeddingService;
        this.restTemplate = restTemplate;
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

                // 非同步抓取工具清單並進行 Embedding
                fetchAndIndexTools(podName, baseUrl);
            }
        }
    }

    private void fetchAndIndexTools(String podName, String baseUrl) {
        try {
            // 假設 Sidecar 有曝露標準的 MCP listTools 或自定義的清單 API
            // 這裡先模擬呼叫，實務上會透過 MCP Client 讀取
            log.info("正在從 Sidecar {} 獲取工具清單...", podName);
            // 這裡暫時用一個模擬的清單或是假設路徑
            List<Map<String, String>> tools = List.of(
                    Map.of("name", "calculate", "description", "執行數學運算與邏輯計算"),
                    Map.of("name", "getBusinessInfo", "description", "獲取企業核心業務資訊與營收數據"));

            for (Map<String, String> tool : tools) {
                embeddingService.indexTool(podName, tool.get("name"), tool.get("description"));
            }
        } catch (Exception e) {
            log.error("抓取工具清單失敗: {}", e.getMessage());
        }
    }

    public Map<String, String> getSidecars() {
        return discoveredSidecars;
    }

    @PreDestroy
    public void cleanup() {
        if (podInformer != null) {
            podInformer.close();
        }
    }
}
