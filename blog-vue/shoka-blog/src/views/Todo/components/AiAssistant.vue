<template>
	<div class="ai-container">
		<div class="ai-tabs">
			<span class="ai-tab" :class="{ active: aiMode === 'summary' }" @click="aiMode = 'summary'">
				<svg-icon icon-class="ai" size="0.9rem" /> 总结报告
			</span>
			<span class="ai-tab" :class="{ active: aiMode === 'suggest' }" @click="aiMode = 'suggest'">
				<svg-icon icon-class="ai" size="0.9rem" /> 改进建议
			</span>
			<span class="ai-tab" :class="{ active: aiMode === 'chat' }" @click="aiMode = 'chat'">
				<svg-icon icon-class="ai" size="0.9rem" /> 对话咨询
			</span>
		</div>

		<!-- 总结报告 -->
		<div v-if="aiMode === 'summary'" class="ai-panel">
			<div class="panel-header">
				<span>选择总结范围：</span>
				<n-button-group>
					<n-button :type="summaryType === 'daily' ? 'primary' : 'default'" size="small" @click="summaryType = 'daily'">日</n-button>
					<n-button :type="summaryType === 'weekly' ? 'primary' : 'default'" size="small" @click="summaryType = 'weekly'">周</n-button>
					<n-button :type="summaryType === 'monthly' ? 'primary' : 'default'" size="small" @click="summaryType = 'monthly'">月</n-button>
				</n-button-group>
				<n-button type="primary" size="small" @click="generateSummary" :loading="summaryLoading">
					{{ summaryResult ? '重新生成' : '生成总结' }}
				</n-button>
				<span v-if="summaryTime" class="record-time">上次生成: {{ summaryTime }}</span>
			</div>
			<div class="ai-result" v-if="summaryResult">
				<v-md-preview :text="summaryResult" class="md-preview"></v-md-preview>
			</div>
			<div v-else-if="summaryLoading" class="ai-loading">
				<span class="dot-anim"></span> AI 正在分析你的数据...
			</div>
			<div v-else class="ai-placeholder">点击"生成总结"，AI 将基于你的代办和日记生成个性化总结报告</div>
		</div>

		<!-- 改进建议 -->
		<div v-if="aiMode === 'suggest'" class="ai-panel">
			<div class="panel-header">
				<span>AI 将分析你近两周的代办数据，给出能力提升建议</span>
				<n-button type="primary" size="small" @click="generateSuggest" :loading="suggestLoading">
					{{ suggestResult ? '重新获取' : '获取建议' }}
				</n-button>
				<span v-if="suggestTime" class="record-time">上次生成: {{ suggestTime }}</span>
			</div>
			<div class="ai-result" v-if="suggestResult">
				<v-md-preview :text="suggestResult" class="md-preview"></v-md-preview>
			</div>
			<div v-else-if="suggestLoading" class="ai-loading">
				<span class="dot-anim"></span> AI 导师正在为你制定建议...
			</div>
			<div v-else class="ai-placeholder">点击"获取建议"，AI 导师将从时间管理、技能提升、习惯养成等维度给出建议</div>
		</div>

		<!-- 对话咨询 -->
		<div v-if="aiMode === 'chat'" class="ai-panel chat-panel">
			<div class="chat-messages" ref="chatBox">
				<div class="chat-welcome" v-if="chatMessages.length === 0">
					<svg-icon icon-class="ai" size="2rem" />
					<p>你好！我是你的个人能力提升导师。</p>
					<p>你可以问我关于代办规划、时间管理、学习计划等问题。</p>
				</div>
				<div
					v-for="(msg, idx) in chatMessages"
					:key="idx"
					class="chat-msg"
					:class="msg.role"
				>
					<div class="msg-bubble">
						<v-md-preview v-if="msg.role === 'assistant'" :text="msg.content" class="md-preview"></v-md-preview>
						<span v-else>{{ msg.content }}</span>
					</div>
				</div>
				<div v-if="chatStreaming" class="chat-msg assistant">
					<div class="msg-bubble">
						<v-md-preview v-if="streamingText" :text="streamingText" class="md-preview"></v-md-preview>
						<span v-else class="dot-anim"></span>
					</div>
				</div>
			</div>
			<div class="chat-input-bar">
				<n-button v-if="chatMessages.length > 0" text @click="clearChat" :disabled="chatStreaming" title="清空对话">
					<svg-icon icon-class="delete" size="1rem" />
				</n-button>
				<n-input
					v-model:value="chatInput"
					placeholder="输入你的问题..."
					@keyup.enter="sendChat"
					:disabled="chatStreaming"
				/>
				<n-button type="primary" @click="sendChat" :disabled="chatStreaming || !chatInput.trim()">
					发送
				</n-button>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { aiSummary, aiSuggest, getAiRecord, saveAiRecord } from "@/api/todo";
import { getToken, token_prefix } from "@/utils/token";

const aiMode = ref<"summary" | "suggest" | "chat">("summary");

// 总结
const summaryType = ref("daily");
const summaryResult = ref("");
const summaryLoading = ref(false);
const summaryTime = ref("");

const loadSummary = (type: string) => {
	getAiRecord("summary_" + type).then(({ data }) => {
		if (data.flag && data.data) {
			summaryResult.value = data.data.content || "";
			summaryTime.value = data.data.updateTime || data.data.createTime || "";
		}
	});
};

const generateSummary = () => {
	summaryLoading.value = true;
	summaryResult.value = "";
	summaryTime.value = "";
	aiSummary(summaryType.value)
		.then(({ data }) => {
			if (data.flag) {
				summaryResult.value = data.data;
				summaryTime.value = "刚刚";
			} else {
				window.$message?.error(data.msg || "生成失败");
			}
		})
		.catch(() => window.$message?.error("AI 服务暂时不可用"))
		.finally(() => (summaryLoading.value = false));
};

watch(summaryType, (t) => {
	summaryResult.value = "";
	summaryTime.value = "";
	loadSummary(t);
});

// 建议
const suggestResult = ref("");
const suggestLoading = ref(false);
const suggestTime = ref("");

const loadSuggest = () => {
	getAiRecord("suggest").then(({ data }) => {
		if (data.flag && data.data) {
			suggestResult.value = data.data.content || "";
			suggestTime.value = data.data.updateTime || data.data.createTime || "";
		}
	});
};

const generateSuggest = () => {
	suggestLoading.value = true;
	suggestResult.value = "";
	suggestTime.value = "";
	aiSuggest()
		.then(({ data }) => {
			if (data.flag) {
				suggestResult.value = data.data;
				suggestTime.value = "刚刚";
			} else {
				window.$message?.error(data.msg || "生成失败");
			}
		})
		.catch(() => window.$message?.error("AI 服务暂时不可用"))
		.finally(() => (suggestLoading.value = false));
};

// 对话
const chatMessages = ref<{ role: string; content: string }[]>([]);
const chatInput = ref("");
const chatStreaming = ref(false);
const streamingText = ref("");
const chatBox = ref<HTMLElement>();

const loadChat = () => {
	getAiRecord("chat").then(({ data }) => {
		if (data.flag && data.data && data.data.content) {
			try {
				chatMessages.value = JSON.parse(data.data.content);
			} catch { chatMessages.value = []; }
		}
	});
};

const persistChat = () => {
	if (chatMessages.value.length > 0) {
		saveAiRecord("chat", JSON.stringify(chatMessages.value));
	}
};

const clearChat = () => {
	window.$dialog?.warning({
		title: "清空对话",
		content: "确认清空所有对话记录？",
		positiveText: "清空",
		negativeText: "取消",
		onPositiveClick: () => {
			chatMessages.value = [];
			saveAiRecord("chat", "[]");
		},
	});
};

const scrollToBottom = () => {
	nextTick(() => {
		if (chatBox.value) chatBox.value.scrollTop = chatBox.value.scrollHeight;
	});
};

const sendChat = async () => {
	const msg = chatInput.value.trim();
	if (!msg || chatStreaming.value) return;
	chatMessages.value.push({ role: "user", content: msg });
	chatInput.value = "";
	chatStreaming.value = true;
	streamingText.value = "";
	scrollToBottom();

	const systemMsg = {
		role: "system",
		content: "你是一位个人能力提升导师，专注于帮助用户进行代办规划、时间管理、学习计划制定和个人成长。回答要具体、有条理、语气亲切专业。使用 Markdown 格式。",
	};
	const messages = [systemMsg, ...chatMessages.value.map((m) => ({ role: m.role, content: m.content }))];

	try {
		const baseURL = import.meta.env.VITE_SERVICE_BASE_URL || "/api";
		const resp = await fetch(`${baseURL}/admin/todo/ai/chat`, {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
				Authorization: token_prefix + (getToken() || ""),
			},
			body: JSON.stringify(messages),
		});

		if (!resp.ok || !resp.body) {
			throw new Error("请求失败");
		}

		const reader = resp.body.getReader();
		const decoder = new TextDecoder();
		let buffer = "";
		while (true) {
			const { done, value } = await reader.read();
			if (done) break;
			buffer += decoder.decode(value, { stream: true });
			const lines = buffer.split("\n");
			buffer = lines.pop() || "";
			for (const line of lines) {
				if (!line.startsWith("data:")) continue;
				const payload = line.substring(5).trim();
				if (payload === "[DONE]") break;
				if (payload.startsWith("[ERROR]")) {
					streamingText.value += "\n\n(AI 服务出错)";
					break;
				}
				try {
					const json = JSON.parse(payload);
					if (json.content) {
						streamingText.value += json.content;
						scrollToBottom();
					}
				} catch {}
			}
		}

		if (streamingText.value) {
			chatMessages.value.push({ role: "assistant", content: streamingText.value });
			persistChat();
		}
	} catch (e) {
		window.$message?.error("AI 对话请求失败");
	} finally {
		chatStreaming.value = false;
		streamingText.value = "";
		scrollToBottom();
	}
};

onMounted(() => {
	loadSummary(summaryType.value);
	loadSuggest();
	loadChat();
});
</script>

<style lang="scss" scoped>
.ai-container {
	max-width: 800px;
	margin: 0 auto;
	color: var(--grey-7, #333);
}
.ai-tabs {
	display: flex;
	gap: 0.75rem;
	margin-bottom: 1rem;
	flex-wrap: wrap;
}
.ai-tab {
	display: flex;
	align-items: center;
	gap: 4px;
	padding: 0.4rem 1rem;
	border-radius: 2rem;
	font-size: 0.85rem;
	cursor: pointer;
	border: 1px solid var(--grey-3);
	color: var(--grey-6, #666);
	transition: all 0.2s;
	&.active {
		background: linear-gradient(135deg, #667eea, #764ba2);
		color: #fff;
		border-color: transparent;
	}
	&:hover:not(.active) {
		border-color: var(--primary-color);
	}
}
.ai-panel {
	padding: 1rem;
	border-radius: 0.5rem;
	background: var(--card-bg, #fff);
	box-shadow: var(--card-shadow);
	min-height: 200px;
}
.panel-header {
	display: flex;
	align-items: center;
	gap: 0.75rem;
	flex-wrap: wrap;
	margin-bottom: 1rem;
	font-size: 0.85rem;
}
.record-time {
	font-size: 0.75rem;
	color: var(--grey-5);
	white-space: nowrap;
}
.ai-result {
	:deep(.md-preview) {
		font-size: 0.9rem;
		line-height: 1.7;
	}
}
.ai-loading {
	display: flex;
	align-items: center;
	gap: 0.5rem;
	padding: 2rem 0;
	justify-content: center;
	color: #667eea;
	font-size: 0.9rem;
}
.dot-anim {
	display: inline-block;
	&::after {
		content: "...";
		animation: dots 1.5s steps(3, end) infinite;
		letter-spacing: 2px;
	}
}
@keyframes dots {
	0% { content: "."; }
	33% { content: ".."; }
	66% { content: "..."; }
}
.ai-placeholder {
	text-align: center;
	padding: 3rem 1rem;
	color: var(--grey-5);
	font-size: 0.9rem;
}

/* 对话 */
.chat-panel {
	display: flex;
	flex-direction: column;
	height: 500px;
}
.chat-messages {
	flex: 1;
	overflow-y: auto;
	padding: 0.5rem 0;
}
.chat-welcome {
	text-align: center;
	padding: 3rem 1rem;
	color: var(--grey-5);
	p { margin: 0.3rem 0; font-size: 0.9rem; }
}
.chat-msg {
	display: flex;
	margin-bottom: 0.75rem;
	&.user { justify-content: flex-end; }
	&.assistant { justify-content: flex-start; }
}
.msg-bubble {
	max-width: 80%;
	padding: 0.5rem 0.75rem;
	border-radius: 0.75rem;
	font-size: 0.9rem;
	line-height: 1.6;
	.user & {
		background: var(--primary-color);
		color: #fff;
		border-bottom-right-radius: 0.2rem;
	}
	.assistant & {
		background: var(--grey-2, #f5f5f5);
		border-bottom-left-radius: 0.2rem;
	}
	:deep(.md-preview) {
		font-size: 0.85rem;
		background: transparent !important;
		padding: 0 !important;
	}
}
.chat-input-bar {
	display: flex;
	gap: 0.5rem;
	margin-top: 0.75rem;
	padding-top: 0.75rem;
	border-top: 1px solid var(--grey-3, #eee);
}

@media (max-width: 767px) {
	.ai-tabs {
		gap: 0.4rem;
	}
	.ai-tab {
		padding: 0.3rem 0.6rem;
		font-size: 0.75rem;
	}
	.panel-header {
		font-size: 0.8rem;
		gap: 0.5rem;
	}
	.ai-panel {
		padding: 0.75rem;
	}
	.chat-panel { height: 400px; }
	.msg-bubble {
		max-width: 90%;
		font-size: 0.85rem;
	}
	.chat-input-bar {
		gap: 0.3rem;
	}
}
</style>
