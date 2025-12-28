package com.example.mcpregistry.rag.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ToolEmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(ToolEmbeddingService.class);

    // 模擬向量資料庫: Tool ID -> Vector (float[])
    private final Map<String, float[]> vectorStore = new ConcurrentHashMap<>();

    // 這裡使用 Optional 或直接等 Spring AI 自動配置
    private final EmbeddingModel embeddingModel;

    public ToolEmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    /**
     * 對工具描述進行索引
     */
    public void indexTool(String podName, String toolName, String description) {
        String toolId = podName + ":" + toolName;
        log.info("正在為工具索引 (Embedding): {}", toolId);

        try {
            float[] vector = embeddingModel.embed(description);
            vectorStore.put(toolId, vector);
            log.info("工具索引完成: {}, 向量維度: {}", toolId, vector.length);
        } catch (Exception e) {
            log.error("Embedding 失敗: {}", e.getMessage());
        }
    }

    public Map<String, float[]> getVectorStore() {
        return vectorStore;
    }
}
