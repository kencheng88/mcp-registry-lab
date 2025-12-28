package com.example.mcpregistry.rag.controller;

import com.example.mcpregistry.rag.service.ToolEmbeddingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final ToolEmbeddingService toolEmbeddingService;

    public RagController(ToolEmbeddingService toolEmbeddingService) {
        this.toolEmbeddingService = toolEmbeddingService;
    }

    /**
     * 獲取目前已索引的所有工具向量狀態
     */
    @GetMapping("/vectors")
    public Map<String, Object> getVectors() {
        Map<String, float[]> store = toolEmbeddingService.getVectorStore();
        return Map.of(
                "totalIndexed", store.size(),
                "tools", store.keySet());
    }

    /**
     * 模擬語義搜尋工具 (未來由 RAG 團隊實作完整邏輯)
     * 根據 query 返回最相關的工具 ID 列表
     */
    @GetMapping("/search")
    public Map<String, Object> searchTools(@RequestParam String query) {
        // 目前僅回傳所有已索引工具作為模擬
        Set<String> allTools = toolEmbeddingService.getVectorStore().keySet();
        return Map.of(
                "query", query,
                "recommendedTools", allTools,
                "message", "Semantic search results (simulated)");
    }
}
