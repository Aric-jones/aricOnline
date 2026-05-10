<template>
	<div class="ai-chat-box" v-animate="['slideUpBigIn']">
		<!-- 标题栏 -->
		<div class="ai-header">
			<div class="ai-title">
				<svg-icon
					icon-class="ai"
					size="1.3rem"
					style="margin-right: 0.4rem"
				></svg-icon>
				AI 智能助手
			</div>
			<div class="ai-toggle" @click="collapsed = !collapsed">
				{{ collapsed ? "展开" : "收起" }}
				<svg-icon
					:icon-class="collapsed ? 'down' : 'up'"
					size="0.8rem"
					style="margin-left: 0.2rem"
				></svg-icon>
			</div>
		</div>

		<!-- 对话内容区 -->
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
				<!-- 加载中 -->
				<div v-if="loading" class="ai-message assistant">
					<div class="msg-avatar"><span>🤖</span></div>
					<div class="msg-content">
						<div class="msg-text typing-indicator">
							<span></span><span></span><span></span>
						</div>
					</div>
				</div>
			</div>

			<!-- 输入区 -->
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
	/** 搜索关键词，作为初始问题 */
	keyword: string;
}>();

const collapsed = ref(false);
const inputText = ref("");
const loading = ref(false);
const streaming = ref(false);
const messagesRef = ref<HTMLDivElement>();
let abortController: AbortController | null = null;

/** 对话历史 */
const chatHistory = ref<ChatMessage[]>([]);

/** 用于显示的消息列表（包含流式回复中的临时消息） */
const currentStreamText = ref("");

const displayMessages = computed(() => {
	const list = [...chatHistory.value];
	if (currentStreamText.value) {
		list.push({ role: "assistant", content: currentStreamText.value });
	}
	return list;
});

// 配置 marked（v12+ 使用 marked.use 替代已废弃的 setOptions）
marked.use({
	breaks: true,
	gfm: true,
});

function renderMarkdown(text: string): string {
	try {
		const result = marked.parse(text);
		// marked.parse 可能返回 string 或 Promise，确保返回 string
		return typeof result === "string" ? result : String(result);
	} catch {
		return text;
	}
}

/** 初始化：把搜索关键词作为第一个问题显示（不自动发送） */
watch(
	() => props.keyword,
	(val) => {
		if (val) {
			// 重置对话
			chatHistory.value = [];
			currentStreamText.value = "";
			inputText.value = val;
		}
	},
	{ immediate: true }
);

/** 自动滚动到底部 */
function scrollToBottom() {
	nextTick(() => {
		if (messagesRef.value) {
			messagesRef.value.scrollTop = messagesRef.value.scrollHeight;
		}
	});
}

/** 发送消息 */
function handleSend() {
	const text = inputText.value.trim();
	if (!text || streaming.value) return;

	// 加入用户消息
	chatHistory.value.push({ role: "user", content: text });
	inputText.value = "";
	loading.value = true;
	streaming.value = true;
	currentStreamText.value = "";
	scrollToBottom();

	// 构建发送给 API 的消息（最近 10 条，避免太长）
	const messagesToSend = chatHistory.value.slice(-10);

	abortController = chatWithAi(
		messagesToSend,
		// onChunk
		(chunk: string) => {
			loading.value = false;
			currentStreamText.value += chunk;
			scrollToBottom();
		},
		// onDone
		() => {
			loading.value = false;
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
		// onError
		(error: string) => {
			loading.value = false;
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

/** 停止生成 */
function handleStop() {
	if (abortController) {
		abortController.abort();
		abortController = null;
	}
	streaming.value = false;
	loading.value = false;
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
.ai-chat-box {
	margin-bottom: 1.5rem;
	border-radius: 0.75rem;
	box-shadow: var(--card-shadow);
	overflow: hidden;
	animation-duration: 0.5s;
	visibility: hidden;
	background: var(--grey-0);
}

.ai-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 0.75rem 1.25rem;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	color: #fff;
	cursor: pointer;
}

.ai-title {
	display: flex;
	align-items: center;
	font-size: 1.05rem;
	font-weight: 600;
}

.ai-toggle {
	display: flex;
	align-items: center;
	font-size: 0.85rem;
	opacity: 0.9;
	cursor: pointer;
	&:hover {
		opacity: 1;
	}
}

.ai-body {
	padding: 0;
}

.ai-messages {
	max-height: 24rem;
	min-height: 3rem;
	overflow-y: auto;
	padding: 1rem 1.25rem;
	scroll-behavior: smooth;

	&::-webkit-scrollbar {
		width: 4px;
	}
	&::-webkit-scrollbar-thumb {
		background: var(--grey-4);
		border-radius: 2px;
	}
}

.ai-message {
	display: flex;
	margin-bottom: 1rem;
	gap: 0.6rem;

	&.user {
		flex-direction: row-reverse;
		.msg-content {
			align-items: flex-end;
		}
		.msg-text {
			background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
			color: #fff;
			border-radius: 1rem 1rem 0.25rem 1rem;
		}
	}

	&.assistant {
		.msg-text {
			background: var(--grey-1);
			color: var(--text-color);
			border-radius: 1rem 1rem 1rem 0.25rem;
		}
	}
}

.msg-avatar {
	flex-shrink: 0;
	width: 2rem;
	height: 2rem;
	display: flex;
	align-items: center;
	justify-content: center;
	border-radius: 50%;
	font-size: 1.2rem;
	background: var(--grey-1);
}

.msg-content {
	display: flex;
	flex-direction: column;
	max-width: 80%;
}

.msg-text {
	padding: 0.6rem 1rem;
	font-size: 0.9rem;
	line-height: 1.7;
	word-break: break-word;

	:deep(pre) {
		background: #1e1e1e;
		color: #d4d4d4;
		padding: 0.75rem 1rem;
		border-radius: 0.5rem;
		overflow-x: auto;
		margin: 0.5rem 0;
		font-size: 0.85rem;
	}

	:deep(code) {
		font-family: "Fira Code", "Consolas", monospace;
		font-size: 0.85em;
	}

	:deep(p code) {
		background: rgba(100, 100, 100, 0.15);
		padding: 0.15em 0.4em;
		border-radius: 0.25rem;
	}

	:deep(p) {
		margin: 0.3em 0;
	}

	:deep(ul),
	:deep(ol) {
		padding-left: 1.5em;
		margin: 0.3em 0;
	}

	:deep(blockquote) {
		border-left: 3px solid #667eea;
		padding-left: 0.75rem;
		margin: 0.5rem 0;
		color: var(--grey-6);
	}

	:deep(a) {
		color: #667eea;
		text-decoration: underline;
	}

	:deep(h1),
	:deep(h2),
	:deep(h3),
	:deep(h4) {
		margin: 0.5em 0 0.3em;
	}

	:deep(table) {
		border-collapse: collapse;
		margin: 0.5rem 0;
		th,
		td {
			border: 1px solid var(--grey-3);
			padding: 0.3rem 0.6rem;
		}
		th {
			background: var(--grey-1);
		}
	}
}

/* 打字动画 */
.typing-indicator {
	display: flex;
	align-items: center;
	gap: 0.3rem;
	padding: 0.8rem 1rem !important;

	span {
		width: 0.5rem;
		height: 0.5rem;
		border-radius: 50%;
		background: #667eea;
		animation: typing 1.4s infinite ease-in-out;

		&:nth-child(1) {
			animation-delay: 0s;
		}
		&:nth-child(2) {
			animation-delay: 0.2s;
		}
		&:nth-child(3) {
			animation-delay: 0.4s;
		}
	}
}

@keyframes typing {
	0%,
	80%,
	100% {
		transform: scale(0.6);
		opacity: 0.4;
	}
	40% {
		transform: scale(1);
		opacity: 1;
	}
}

.ai-input-area {
	display: flex;
	gap: 0.5rem;
	padding: 0.75rem 1.25rem;
	border-top: 1px solid var(--grey-2);
	background: var(--grey-0);
}

.ai-input {
	flex: 1;
	padding: 0.5rem 0.9rem;
	border: 1px solid var(--grey-3);
	border-radius: 1.5rem;
	font-size: 0.9rem;
	outline: none;
	background: var(--grey-0);
	color: var(--text-color);
	transition: border-color 0.2s;

	&:focus {
		border-color: #667eea;
	}

	&::placeholder {
		color: var(--grey-5);
	}
}

.ai-send-btn {
	padding: 0.5rem 1.2rem;
	border: none;
	border-radius: 1.5rem;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	color: #fff;
	font-size: 0.9rem;
	cursor: pointer;
	white-space: nowrap;
	transition: opacity 0.2s, transform 0.1s;

	&:hover:not(:disabled) {
		opacity: 0.9;
		transform: translateY(-1px);
	}

	&:disabled {
		opacity: 0.6;
		cursor: not-allowed;
	}
}

.ai-stop-btn {
	padding: 0.5rem 1rem;
	border: 1px solid #e74c3c;
	border-radius: 1.5rem;
	background: transparent;
	color: #e74c3c;
	font-size: 0.9rem;
	cursor: pointer;
	white-space: nowrap;
	transition: all 0.2s;

	&:hover {
		background: #e74c3c;
		color: #fff;
	}
}

/* 响应式 */
@media (max-width: 768px) {
	.ai-messages {
		max-height: 18rem;
	}
	.msg-content {
		max-width: 90%;
	}
}
</style>
