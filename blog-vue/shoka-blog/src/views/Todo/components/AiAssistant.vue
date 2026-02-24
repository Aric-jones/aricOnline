<template>
	<div class="ai-container">
		<div class="ai-tabs">
			<span class="ai-tab" :class="{ active: aiMode === 'summary' }" @click="aiMode = 'summary'">
				<svg-icon icon-class="ai" size="0.9rem" /> 总结报告
			</span>
			<span class="ai-tab" :class="{ active: aiMode === 'suggest' }" @click="aiMode = 'suggest'">
				<svg-icon icon-class="ai" size="0.9rem" /> 改进建议
			</span>
			<span class="ai-tab" :class="{ active: aiMode === 'habit' }" @click="aiMode = 'habit'">
				<svg-icon icon-class="ai" size="0.9rem" /> 习惯洞察
			</span>
			<span class="ai-tab" :class="{ active: aiMode === 'chat' }" @click="aiMode = 'chat'">
				<svg-icon icon-class="ai" size="0.9rem" /> 对话咨询
			</span>
			<span class="ai-tab settings-tab" :class="{ active: aiMode === 'settings' }" @click="aiMode = 'settings'">
				⚙️ 设置
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

		<!-- 习惯洞察 -->
		<div v-if="aiMode === 'habit'" class="ai-panel">
			<div class="panel-header">
				<span>选择习惯：</span>
				<n-select
					v-model:value="selectedHabitIds"
					:options="habitOptions"
					multiple
					placeholder="选择想洞察的习惯（可多选）"
					size="small"
					style="min-width: 200px; flex: 1; max-width: 400px"
				/>
				<n-button type="primary" size="small" @click="generateHabitInsight" :loading="habitLoading">
					{{ habitResult ? '重新分析' : '开始洞察' }}
				</n-button>
				<span v-if="habitTime" class="record-time">上次生成: {{ habitTime }}</span>
			</div>
			<div class="ai-result" v-if="habitResult">
				<v-md-preview :text="habitResult" class="md-preview"></v-md-preview>
			</div>
			<div v-else-if="habitLoading" class="ai-loading">
				<span class="dot-anim"></span> AI 正在分析你的习惯数据...
			</div>
			<div v-else class="ai-placeholder">选择想要洞察的习惯，AI 将分析你近30天的习惯记录，发现规律和改进空间</div>
		</div>

		<!-- 对话咨询 -->
		<div v-if="aiMode === 'chat'" class="ai-panel chat-panel">
			<div class="chat-range-bar">
				<span class="range-label">数据范围：</span>
				<n-button-group size="tiny">
					<n-button v-for="r in rangeOptions" :key="r.value"
						:type="chatRange === r.value ? 'primary' : 'default'"
						@click="chatRange = r.value">
						{{ r.label }}
					</n-button>
				</n-button-group>
			</div>
			<div class="chat-messages" ref="chatBox">
				<div class="chat-welcome" v-if="chatMessages.length === 0">
					<svg-icon icon-class="ai" size="2rem" />
					<p>你好！我是你的个人效能助手。</p>
					<p>我已了解你的待办、日记和习惯数据，可以针对性地为你解答。</p>
					<p class="welcome-hint">试试问我：「我这周完成了哪些任务？」「我的习惯坚持得怎么样？」</p>
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
				round
				@keyup.enter="sendChat"
				:disabled="chatStreaming"
			/>
				<n-button type="primary" @click="sendChat" :disabled="chatStreaming || !chatInput.trim()">
					发送
				</n-button>
			</div>
		</div>

		<!-- 设置面板 -->
		<div v-if="aiMode === 'settings'" class="ai-panel settings-panel">
			<div class="settings-desc">
				自定义 AI 的角色和行为。修改提示词后，对应功能将使用你自定义的版本。
			</div>
			<div v-if="promptsLoading" class="ai-loading">
				<span class="dot-anim"></span> 加载中...
			</div>
			<div v-else class="prompt-list">
				<div v-for="p in promptList" :key="p.promptKey" class="prompt-card">
					<div class="prompt-card-header">
						<span class="prompt-name">{{ p.name }}</span>
						<span class="prompt-key">{{ p.promptKey }}</span>
						<span v-if="p.userId === 0" class="prompt-badge default">默认</span>
						<span v-else class="prompt-badge custom">自定义</span>
					</div>
					<n-input
						type="textarea"
						v-model:value="p.content"
						:autosize="{ minRows: 3, maxRows: 8 }"
						placeholder="输入提示词内容..."
					/>
					<div class="prompt-card-actions">
						<n-button size="tiny" @click="handleOptimize(p)" :loading="p._optimizing">
							✨ AI 优化
						</n-button>
						<n-button size="tiny" type="primary" @click="handleSavePrompt(p)" :loading="p._saving">
							保存
						</n-button>
						<n-button v-if="p.userId !== 0" size="tiny" quaternary type="error" @click="handleResetPrompt(p)">
							恢复默认
						</n-button>
					</div>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import {
	aiSummary, aiSuggest, getAiRecord, saveAiRecord,
	getAiPromptList, saveAiPrompt, resetAiPrompt, optimizeAiPrompt,
	aiHabitInsight
} from "@/api/todo";
import { getHabitList } from "@/api/habit";
import { getToken, token_prefix } from "@/utils/token";

const aiMode = ref<"summary" | "suggest" | "habit" | "chat" | "settings">("summary");

// =============== 总结 ===============
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

// =============== 建议 ===============
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

// =============== 习惯洞察 ===============
const selectedHabitIds = ref<number[]>([]);
const habitOptions = ref<{ label: string; value: number }[]>([]);
const habitResult = ref("");
const habitLoading = ref(false);
const habitTime = ref("");

const loadHabits = () => {
	getHabitList().then(({ data }) => {
		if (data.flag && data.data) {
			habitOptions.value = data.data.map((h: any) => ({
				label: `${h.icon || "⭐"} ${h.name}`,
				value: h.id,
			}));
		}
	});
};

const loadHabitInsight = () => {
	getAiRecord("habit_insight").then(({ data }) => {
		if (data.flag && data.data) {
			habitResult.value = data.data.content || "";
			habitTime.value = data.data.updateTime || data.data.createTime || "";
		}
	});
};

const generateHabitInsight = () => {
	if (selectedHabitIds.value.length === 0) {
		window.$message?.warning("请至少选择一个习惯");
		return;
	}
	habitLoading.value = true;
	habitResult.value = "";
	habitTime.value = "";
	aiHabitInsight(selectedHabitIds.value)
		.then(({ data }) => {
			if (data.flag) {
				habitResult.value = data.data;
				habitTime.value = "刚刚";
			} else {
				window.$message?.error(data.msg || "生成失败");
			}
		})
		.catch(() => window.$message?.error("AI 服务暂时不可用"))
		.finally(() => (habitLoading.value = false));
};

// =============== 对话 ===============
const rangeOptions = [
	{ label: "7天", value: "7d" },
	{ label: "1个月", value: "1m" },
	{ label: "3个月", value: "3m" },
	{ label: "6个月", value: "6m" },
	{ label: "1年", value: "1y" },
];
const chatRange = ref("7d");
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

	const messages = chatMessages.value.map((m) => ({ role: m.role, content: m.content }));

	try {
		const baseURL = import.meta.env.VITE_SERVICE_BASE_URL || "/api";
		const resp = await fetch(`${baseURL}/user/todo/ai/chat`, {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
				Authorization: token_prefix + (getToken() || ""),
			},
			body: JSON.stringify({ messages, range: chatRange.value }),
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

// =============== 设置 ===============
const promptList = ref<any[]>([]);
const promptsLoading = ref(false);

const loadPrompts = () => {
	promptsLoading.value = true;
	getAiPromptList()
		.then(({ data }) => {
			if (data.flag && data.data) {
				promptList.value = data.data.map((p: any) => ({
					...p,
					_saving: false,
					_optimizing: false,
				}));
			}
		})
		.finally(() => (promptsLoading.value = false));
};

const handleSavePrompt = (p: any) => {
	p._saving = true;
	saveAiPrompt({ promptKey: p.promptKey, name: p.name, content: p.content })
		.then(({ data }) => {
			if (data.flag) {
				window.$message?.success("保存成功");
				loadPrompts();
			}
		})
		.finally(() => (p._saving = false));
};

const handleResetPrompt = (p: any) => {
	window.$dialog?.warning({
		title: "恢复默认",
		content: `确认将「${p.name}」恢复为系统默认提示词？`,
		positiveText: "确认",
		negativeText: "取消",
		onPositiveClick: () => {
			resetAiPrompt(p.promptKey).then(({ data }) => {
				if (data.flag) {
					window.$message?.success("已恢复默认");
					loadPrompts();
				}
			});
		},
	});
};

const handleOptimize = (p: any) => {
	if (!p.content || !p.content.trim()) {
		window.$message?.warning("请先输入提示词内容");
		return;
	}
	p._optimizing = true;
	optimizeAiPrompt(p.content)
		.then(({ data }) => {
			if (data.flag && data.data) {
				p.content = data.data;
				window.$message?.success("优化完成，请检查效果后保存");
			} else {
				window.$message?.error("优化失败");
			}
		})
		.catch(() => window.$message?.error("AI 服务暂时不可用"))
		.finally(() => (p._optimizing = false));
};

onMounted(() => {
	loadSummary(summaryType.value);
	loadSuggest();
	loadChat();
	loadHabits();
	loadHabitInsight();
	loadPrompts();
});
</script>

<style lang="scss" scoped>
$primary: #6366f1;
$primary-light: #818cf8;
$primary-bg: rgba(99, 102, 241, 0.08);
$card: rgba(255, 255, 255, 0.65);
$card-border: rgba(255, 255, 255, 0.5);
$glass-border: rgba(99, 102, 241, 0.15);
$shadow: 0 4px 24px rgba(99, 102, 241, 0.08), 0 1px 3px rgba(0, 0, 0, 0.06);

.ai-container { max-width: 800px; margin: 0 auto; color: var(--grey-7, #1e293b); }

.ai-tabs { display: flex; gap: 0.6rem; margin-bottom: 1.25rem; flex-wrap: wrap; }
.ai-tab {
	display: flex; align-items: center; gap: 4px;
	padding: 0.45rem 1rem; border-radius: 50px; font-size: 0.85rem;
	cursor: pointer; font-weight: 500;
	border: 1.5px solid $glass-border;
	background: var(--card-bg, $card); backdrop-filter: blur(16px);
	color: var(--grey-6, #64748b); transition: all 0.25s ease;
	&.active {
		background: linear-gradient(135deg, $primary, $primary-light);
		color: #fff; border-color: transparent;
		box-shadow: 0 4px 14px rgba(99, 102, 241, 0.35);
	}
	&:hover:not(.active) { border-color: $primary; color: $primary; }
}
.settings-tab { margin-left: auto; }

.ai-panel {
	padding: 1.25rem; border-radius: 16px; min-height: 200px;
	background: var(--card-bg, $card); backdrop-filter: blur(16px); -webkit-backdrop-filter: blur(16px);
	border: 1.5px solid var(--card-border, $card-border); box-shadow: $shadow;
}
.panel-header {
	display: flex; align-items: center; gap: 0.75rem; flex-wrap: wrap;
	margin-bottom: 1rem; font-size: 0.85rem;
}
.record-time { font-size: 0.75rem; color: var(--grey-5, #94a3b8); white-space: nowrap; }

.ai-result {
	:deep(.md-preview) { font-size: 0.9rem; line-height: 1.7; }
}
.ai-loading {
	display: flex; align-items: center; gap: 0.5rem;
	padding: 2rem 0; justify-content: center;
	color: $primary; font-size: 0.9rem;
}
.dot-anim {
	display: inline-block;
	&::after { content: "..."; animation: dots 1.5s steps(3, end) infinite; letter-spacing: 2px; }
}
@keyframes dots {
	0% { content: "."; }
	33% { content: ".."; }
	66% { content: "..."; }
}
.ai-placeholder {
	text-align: center; padding: 3rem 1rem;
	color: var(--grey-5, #94a3b8); font-size: 0.9rem;
}

.chat-panel { display: flex; flex-direction: column; height: 500px; }
.chat-range-bar {
	display: flex; align-items: center; gap: 0.5rem;
	padding-bottom: 0.5rem; border-bottom: 1px solid rgba(99, 102, 241, 0.06);
	margin-bottom: 0.5rem; flex-shrink: 0;
}
.range-label { font-size: 0.8rem; color: var(--grey-5, #94a3b8); white-space: nowrap; }
.chat-messages { flex: 1; overflow-y: auto; padding: 0.5rem 0; }
.chat-welcome {
	text-align: center; padding: 3rem 1rem; color: var(--grey-5, #94a3b8);
	p { margin: 0.3rem 0; font-size: 0.9rem; }
	.welcome-hint { margin-top: 0.75rem; font-size: 0.8rem; color: var(--grey-4); font-style: italic; }
}
.chat-msg {
	display: flex; margin-bottom: 0.75rem;
	&.user { justify-content: flex-end; }
	&.assistant { justify-content: flex-start; }
}
.msg-bubble {
	max-width: 80%; padding: 0.6rem 0.85rem; border-radius: 16px; font-size: 0.9rem; line-height: 1.6;
	.user & {
		background: linear-gradient(135deg, $primary, $primary-light);
		color: #fff; border-bottom-right-radius: 4px;
		box-shadow: 0 2px 10px rgba(99, 102, 241, 0.2);
	}
	.assistant & {
		background: rgba(255, 255, 255, 0.7); backdrop-filter: blur(8px);
		border: 1px solid rgba(99, 102, 241, 0.08); border-bottom-left-radius: 4px;
	}
	:deep(.md-preview) { font-size: 0.85rem; background: transparent !important; padding: 0 !important; }
}
.chat-input-bar {
	display: flex; gap: 0.5rem; margin-top: 0.75rem;
	padding-top: 0.75rem; border-top: 1px solid rgba(99, 102, 241, 0.06);
}

.settings-panel { min-height: 300px; }
.settings-desc {
	font-size: 0.85rem; color: var(--grey-5, #94a3b8);
	margin-bottom: 1rem; padding-bottom: 0.75rem;
	border-bottom: 1px solid rgba(99, 102, 241, 0.06);
}
.prompt-list { display: flex; flex-direction: column; gap: 1rem; }
.prompt-card {
	padding: 1rem; border-radius: 16px;
	border: 1.5px solid $glass-border;
	background: rgba(255, 255, 255, 0.45); backdrop-filter: blur(8px);
	transition: box-shadow 0.2s;
	&:hover { box-shadow: 0 4px 16px rgba(99, 102, 241, 0.08); }
}
.prompt-card-header { display: flex; align-items: center; gap: 0.5rem; margin-bottom: 0.5rem; }
.prompt-name { font-weight: 600; font-size: 0.9rem; }
.prompt-key {
	font-size: 0.7rem; color: var(--grey-5, #94a3b8); font-family: monospace;
	background: $primary-bg; padding: 2px 8px; border-radius: 50px;
}
.prompt-badge {
	font-size: 0.65rem; padding: 2px 8px; border-radius: 50px; font-weight: 600;
	&.default { background: rgba(0, 0, 0, 0.04); color: var(--grey-5); }
	&.custom { background: rgba(34, 197, 94, 0.1); color: #16a34a; }
}
.prompt-card-actions { display: flex; gap: 0.5rem; margin-top: 0.5rem; justify-content: flex-end; }

@media (max-width: 767px) {
	.ai-tabs { gap: 0.4rem; }
	.ai-tab { padding: 0.3rem 0.65rem; font-size: 0.75rem; }
	.settings-tab { margin-left: 0; }
	.panel-header { font-size: 0.8rem; gap: 0.5rem; }
	.ai-panel { padding: 0.85rem; border-radius: 12px; }
	.chat-panel { height: 400px; }
	.msg-bubble { max-width: 90%; font-size: 0.85rem; }
	.chat-input-bar { gap: 0.3rem; }
}
</style>
