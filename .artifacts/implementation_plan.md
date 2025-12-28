# Enterprise MCP Project Initialization Plan

本計畫旨在初始化 `mcp-registry-lab` 並重構 `mcp-gateway-lab`，以實現控制面與數據面分離的企業級 MCP 架構。

## 1. 初始化 `mcp-registry-lab` (Control Plane)
註冊中心將負責：
- 透過 K8s API 偵測專案中的 Sidecar Pod。
- 維護全域工具清單。
- 準備整合 Embedding 引擎，提供「語義篩選工具」的能力 (RAG for Tools)。

### [NEW] [mcp-registry-lab](file:///Users/kencheng/Documents/lab/mcp-registry-lab)
- 使用 Spring Boot 3.5.0 + Spring AI MCP。
- 引入 K8s Client 依賴。

## 2. 初始化 `mcp-gateway-lab` (Data Plane)
網關將負責：
- 聚合來自 Registry 的多個 Sidecar 工具清單。
- 作為單一進入點 (SSE/Stdio) 與 AI Agent 通訊。
- 實現請求透明轉發。

### [REFAC] [mcp-gateway-lab](file:///Users/kencheng/Documents/lab/mcp-gateway-lab)
- 從 `mcp-central-gateway-lab` 複製 `mcp-gateway` 核心代碼。
- 修改配置，使其對接獨立的 Registry。

## 待執行動作
1. 建立目錄結構。
2. 複製代碼。
3. 初始化 Maven 專案。
