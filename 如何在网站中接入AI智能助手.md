# 如何在网站中接入 AI 智能助手

最近给自己的博客加了个 AI 智能助手功能，用户搜索文章时可以在搜索结果页和 AI 对话。用的是 DeepSeek（也可以换成硅基流动，有免费额度），整体实现不算复杂，记录一下过程。

## 为什么选择 DeepSeek / 硅基流动

- **DeepSeek**：国内大模型，中文效果好，API 兼容 OpenAI 格式，新用户有免费额度
- **硅基流动**：提供多个免费模型（包括 DeepSeek-V3），完全免费，API 格式和 DeepSeek 一样

我最后用的是硅基流动，因为免费额度够用，而且模型选择多。

## 整体架构

```
用户搜索 → 跳转搜索页 → AI 对话框显示 → 用户点击发送
    ↓
前端 fetch POST /ai/chat (SSE流)
    ↓
后端 AiController → AiService
    ↓
OkHttp SSE → DeepSeek/硅基流动 API
    ↓
流式返回 → SseEmitter 转发 → 前端逐字渲染
```

核心是 SSE（Server-Sent Events）流式传输，让 AI 回复可以像打字机一样逐字显示。

## 后端实现（Spring Boot）

### 1. 添加依赖

在 pom.xml 中添加 OkHttp（用于调用 AI API）：

```xml
<!-- OkHttp (用于 AI 流式请求) -->
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>okhttp</artifactId>
    <version>4.12.0</version>
</dependency>
<!-- OkHttp SSE 支持 -->
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>okhttp-sse</artifactId>
    <version>4.12.0</version>
</dependency>
```

### 2. 配置文件

在 `application-dev.yml` 中添加 AI 配置：

```yaml
# AI 对话配置
ai:
  deepseek:
    # 硅基流动 API Key（从 https://siliconflow.cn 获取）
    api-key: sk-你的API密钥
    # API 地址
    api-url: https://api.siliconflow.cn/v1/chat/completions
    # 模型名称（硅基流动的 DeepSeek-V3 免费模型）
    model: deepseek-ai/DeepSeek-V3-0324
    # 系统提示词
    system-prompt: "你是一个博客智能助手，帮助用户解答技术问题。请用简洁、专业的中文回答，支持 Markdown 格式。"
```

### 3. 配置属性类

创建 `DeepSeekProperties.java`：

```java
package com.ican.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai.deepseek")
public class DeepSeekProperties {
    private String apiKey;
    private String apiUrl = "https://api.deepseek.com/chat/completions";
    private String model = "deepseek-chat";
    private String systemPrompt = "你是一个博客智能助手...";
}
```

### 4. AI 服务类

创建 `AiService.java`，核心逻辑：

```java
@Service
public class AiService {
    @Autowired
    private DeepSeekProperties deepSeekProperties;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build();

    public SseEmitter chatStream(List<Map<String, String>> messages) {
        SseEmitter emitter = new SseEmitter(180_000L); // 3分钟超时

        // 构建请求体
        JSONArray messagesArray = new JSONArray();
        // 系统提示词
        JSONObject systemMsg = new JSONObject();
        systemMsg.put("role", "system");
        systemMsg.put("content", deepSeekProperties.getSystemPrompt());
        messagesArray.add(systemMsg);
        // 用户消息
        for (Map<String, String> msg : messages) {
            JSONObject jsonMsg = new JSONObject();
            jsonMsg.put("role", msg.get("role"));
            jsonMsg.put("content", msg.get("content"));
            messagesArray.add(jsonMsg);
        }

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", deepSeekProperties.getModel());
        requestBody.put("messages", messagesArray);
        requestBody.put("stream", true); // 开启流式输出
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 2048);

        Request request = new Request.Builder()
                .url(deepSeekProperties.getApiUrl())
                .addHeader("Authorization", "Bearer " + deepSeekProperties.getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toJSONString(), MediaType.parse("application/json")))
                .build();

        // 创建 SSE 事件源
        EventSource.Factory factory = EventSources.createFactory(httpClient);
        factory.newEventSource(request, new EventSourceListener() {
            @Override
            public void onEvent(EventSource eventSource, String id, String type, String data) {
                try {
                    if ("[DONE]".equals(data)) {
                        emitter.send(SseEmitter.event().data("[DONE]"));
                        emitter.complete();
                        return;
                    }
                    // 解析 AI 返回的 SSE 数据
                    JSONObject jsonData = JSON.parseObject(data);
                    JSONArray choices = jsonData.getJSONArray("choices");
                    if (choices != null && !choices.isEmpty()) {
                        JSONObject delta = choices.getJSONObject(0).getJSONObject("delta");
                        if (delta != null && delta.containsKey("content")) {
                            String content = delta.getString("content");
                            if (content != null) {
                                // 重要：用 JSON 包装内容，避免换行符被 SSE 拆行
                                JSONObject chunk = new JSONObject();
                                chunk.put("content", content);
                                emitter.send(SseEmitter.event().data(chunk.toJSONString()));
                            }
                        }
                    }
                } catch (IOException e) {
                    log.error("SSE 发送失败", e);
                    emitter.completeWithError(e);
                }
            }

            @Override
            public void onFailure(EventSource eventSource, Throwable t, Response response) {
                // 错误处理
                String errorMsg = "AI 服务调用失败";
                if (response != null) {
                    try {
                        errorMsg = "AI 服务返回错误: " + response.code() + " " + response.body().string();
                    } catch (Exception e) {
                        errorMsg = "AI 服务返回错误: " + response.code();
                    }
                }
                log.error(errorMsg, t);
                try {
                    emitter.send(SseEmitter.event().data("[ERROR] " + errorMsg));
                    emitter.complete();
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            }
        });

        emitter.onTimeout(() -> {
            log.warn("SSE 连接超时");
            emitter.complete();
        });

        return emitter;
    }
}
```

**关键点**：用 JSON 包装内容 `{"content":"..."}` 发送，这样换行符 `\n` 会被转义成 `\\n`，不会被 SSE 协议拆成多行，前端才能正确解析。

### 5. 控制器

创建 `AiController.java`：

```java
@RestController
public class AiController {
    @Autowired
    private AiService aiService;

    @PostMapping(value = "/ai/chat", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter chat(@RequestBody List<Map<String, String>> messages) {
        return aiService.chatStream(messages);
    }
}
```

## 前端实现（Vue 3）

### 1. 安装依赖

```bash
npm install marked --save
```

`marked` 用于渲染 AI 回复中的 Markdown。

### 2. AI API

创建 `api/ai/index.ts`：

```typescript
import { getToken, token_prefix } from "@/utils/token";
import { getServiceBaseURL } from "@/utils/service";

export interface ChatMessage {
  role: "user" | "assistant";
  content: string;
}

const isHttpProxy = import.meta.env.DEV && import.meta.env.VITE_HTTP_PROXY === "Y";
const { baseURL } = getServiceBaseURL(import.meta.env, isHttpProxy);

export function chatWithAi(
  messages: ChatMessage[],
  onChunk: (text: string) => void,
  onDone: () => void,
  onError: (error: string) => void
): AbortController {
  const controller = new AbortController();

  const headers: Record<string, string> = {
    "Content-Type": "application/json;charset=UTF-8",
  };
  const token = getToken();
  if (token) {
    headers["Authorization"] = token_prefix + token;
  }

  fetch(`${baseURL}/ai/chat`, {
    method: "POST",
    headers,
    body: JSON.stringify(messages),
    signal: controller.signal,
  })
    .then(async (response) => {
      if (!response.ok) {
        onError(`请求失败: ${response.status}`);
        return;
      }

      const reader = response.body?.getReader();
      if (!reader) {
        onError("无法读取响应流");
        return;
      }

      const decoder = new TextDecoder("utf-8");
      let buffer = "";

      while (true) {
        const { done, value } = await reader.read();
        if (done) {
          onDone();
          break;
        }

        buffer += decoder.decode(value, { stream: true });

        // 解析 SSE 格式：data:xxx\n\n
        const lines = buffer.split("\n");
        buffer = lines.pop() || "";

        for (const line of lines) {
          const trimmed = line.trim();
          if (!trimmed) continue;

          if (trimmed.startsWith("data:")) {
            const data = trimmed.slice(5);
            if (!data) continue;
            if (data === "[DONE]") {
              onDone();
              return;
            }
            if (data.startsWith("[ERROR]")) {
              onError(data);
              return;
            }
            // 解析后端发送的 JSON {"content":"..."}
            try {
              const parsed = JSON.parse(data);
              if (parsed && typeof parsed.content === "string") {
                onChunk(parsed.content);
              } else {
                onChunk(data);
              }
            } catch {
              onChunk(data);
            }
          }
        }
      }
    })
    .catch((err) => {
      if (err.name !== "AbortError") {
        onError(err.message || "网络错误");
      }
    });

  return controller;
}
```

### 3. AI 对话组件

创建 `views/Search/AiChat.vue`：

```vue
<template>
  <div class="ai-chat-box">
    <div class="ai-header">
      <div class="ai-title">AI 智能助手</div>
      <div class="ai-toggle" @click="collapsed = !collapsed">
        {{ collapsed ? "展开" : "收起" }}
      </div>
    </div>

    <div class="ai-body" v-show="!collapsed">
      <div class="ai-messages" ref="messagesRef">
        <div
          v-for="(msg, index) in displayMessages"
          :key="index"
          class="ai-message"
          :class="msg.role"
        >
          <div class="msg-avatar">
            <span v-if="msg.role === 'user'">🧑</span>
            <span v-else>🤖</span>
          </div>
          <div class="msg-content">
            <div
              v-if="msg.role === 'assistant'"
              class="msg-text markdown-body"
              v-html="renderMarkdown(msg.content)"
            ></div>
            <div v-else class="msg-text">{{ msg.content }}</div>
          </div>
        </div>
      </div>

      <div class="ai-input-area">
        <input
          class="ai-input"
          v-model="inputText"
          placeholder="输入你的问题..."
          @keyup.enter="handleSend"
          :disabled="streaming"
        />
        <button
          class="ai-send-btn"
          @click="handleSend"
          :disabled="streaming || !inputText.trim()"
        >
          {{ streaming ? "回答中..." : "发送" }}
        </button>
        <button v-if="streaming" class="ai-stop-btn" @click="handleStop">
          停止
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { chatWithAi, type ChatMessage } from "@/api/ai";
import { marked } from "marked";

const props = defineProps<{
  keyword: string;
}>();

const collapsed = ref(false);
const inputText = ref("");
const streaming = ref(false);
const messagesRef = ref<HTMLDivElement>();
let abortController: AbortController | null = null;

const chatHistory = ref<ChatMessage[]>([]);
const currentStreamText = ref("");

const displayMessages = computed(() => {
  const list = [...chatHistory.value];
  if (currentStreamText.value) {
    list.push({ role: "assistant", content: currentStreamText.value });
  }
  return list;
});

// 配置 marked（v12+ 使用 marked.use）
marked.use({
  breaks: true,
  gfm: true,
});

function renderMarkdown(text: string): string {
  try {
    const result = marked.parse(text);
    return typeof result === "string" ? result : String(result);
  } catch {
    return text;
  }
}

watch(
  () => props.keyword,
  (val) => {
    if (val) {
      chatHistory.value = [];
      currentStreamText.value = "";
      inputText.value = val;
    }
  },
  { immediate: true }
);

function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight;
    }
  });
}

function handleSend() {
  const text = inputText.value.trim();
  if (!text || streaming.value) return;

  chatHistory.value.push({ role: "user", content: text });
  inputText.value = "";
  streaming.value = true;
  currentStreamText.value = "";
  scrollToBottom();

  const messagesToSend = chatHistory.value.slice(-10); // 最近10条

  abortController = chatWithAi(
    messagesToSend,
    (chunk: string) => {
      currentStreamText.value += chunk;
      scrollToBottom();
    },
    () => {
      streaming.value = false;
      if (currentStreamText.value) {
        chatHistory.value.push({
          role: "assistant",
          content: currentStreamText.value,
        });
        currentStreamText.value = "";
      }
      abortController = null;
    },
    (error: string) => {
      streaming.value = false;
      chatHistory.value.push({
        role: "assistant",
        content: `抱歉，AI 服务暂时不可用：${error}`,
      });
      currentStreamText.value = "";
      abortController = null;
      scrollToBottom();
    }
  );
}

function handleStop() {
  if (abortController) {
    abortController.abort();
    abortController = null;
  }
  streaming.value = false;
  if (currentStreamText.value) {
    chatHistory.value.push({
      role: "assistant",
      content: currentStreamText.value,
    });
    currentStreamText.value = "";
  }
}

onUnmounted(() => {
  if (abortController) {
    abortController.abort();
  }
});
</script>

<style lang="scss" scoped>
/* 样式省略，主要是消息气泡、输入框、Markdown 渲染样式 */
/* 完整样式见项目代码 */
</style>
```

### 4. 集成到搜索页

在搜索页 `views/Search/index.vue` 中引入：

```vue
<template>
  <div class="bg">
    <div class="main-container mt">
      <div class="left-container">
        <!-- AI 对话框（仅关键词搜索时显示） -->
        <AiChat v-if="keyword" :keyword="keyword" />
        <!-- 文章列表 -->
        <div class="article-item" v-for="article of articleList" :key="article.id">
          <!-- ... -->
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import AiChat from "./AiChat.vue";
// ... 其他导入
</script>
```

## 遇到的问题

### 1. Markdown 不渲染

**问题**：AI 回复的 Markdown 格式没有渲染，显示为纯文本。

**原因**：后端发送包含 `\n` 的内容时，Spring 的 `SseEmitter` 会把每行拆成单独的 `data:` 行。前端解析时跳过了空的 `data:` 行，导致所有换行丢失。

**解决**：后端用 JSON 包装内容 `{"content":"..."}` 发送，前端解析 JSON 提取 `content`，换行符完整保留。

### 2. marked API 变更

**问题**：`marked.setOptions()` 报错。

**原因**：`marked` v12+ 废弃了 `setOptions()`，改用 `marked.use()`。

**解决**：改用 `marked.use({ breaks: true, gfm: true })`。

## 总结

整体实现不算复杂，主要是：

1. **后端**：用 OkHttp SSE 调用 AI API，用 Spring 的 `SseEmitter` 转发给前端
2. **前端**：用 `fetch` + `ReadableStream` 读取 SSE 流，实时渲染
3. **关键点**：内容用 JSON 包装，避免换行符丢失

效果就是用户搜索时，顶部出现 AI 对话框，可以多轮对话，回复流式显示，支持 Markdown 渲染（代码块、列表、表格等）。

如果遇到问题，可以检查：
- API Key 是否正确
- 后端日志看是否有错误
- 浏览器控制台看网络请求是否正常
