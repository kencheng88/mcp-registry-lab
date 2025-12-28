package com.example.mcpregistry;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class K8sConfig {

    @Bean
    public KubernetesClient kubernetesClient() {
        // 自動讀取 ~/.kube/config 或 In-cluster ServiceAccount
        return new KubernetesClientBuilder().build();
    }
}
