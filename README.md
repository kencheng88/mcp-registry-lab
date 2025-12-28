# MCP Registry Lab (企業級控制面)

這個實驗室展示了 **MCP Registry (註冊中心)**。它在企業級架構中扮演「控制面」的角色。

## 核心職責
- **服務發現**：透過 K8s API 自動偵測叢集中的 MCP Sidecar。
- **元數據管理**：蒐集所有 Sidecar 提供的工具定義與 Schema。
- **RAG for Tools**：將工具描述向量化，提供語義搜尋能力。

## 專案結構
- **/mcp-registry**: Java/Spring Boot 註冊中心 (Port 8082)

## 啟動說明
```bash
cd mcp-registry
mvn spring-boot:run
```
