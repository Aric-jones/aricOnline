import { getToken, token_prefix } from "@/utils/token";
import { getServiceBaseURL } from "@/utils/service";

export interface ChatMessage {
  role: "user" | "assistant";
  content: string;
}

const isHttpProxy = import.meta.env.DEV && import.meta.env.VITE_HTTP_PROXY === "Y";
const { baseURL } = getServiceBaseURL(import.meta.env, isHttpProxy);

/**
 * AI 流式对话
 * 使用 fetch + ReadableStream 实现 SSE 流式接收
 *
 * @param messages 对话消息列表
 * @param onChunk  每收到一段文本的回调
 * @param onDone   完成回调
 * @param onError  错误回调
 * @returns AbortController 用于取消请求
 */
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
            // 后端以 JSON {"content":"..."} 格式发送，解析提取 content
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
