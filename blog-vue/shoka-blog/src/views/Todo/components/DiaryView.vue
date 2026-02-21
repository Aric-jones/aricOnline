<template>
	<div class="diary-container">
		<!-- 日记卡片头部 -->
		<div class="diary-card">
			<div class="diary-card-header">
				<div class="header-left">
					<n-button text @click="changeDate(-1)" class="nav-btn">
						<svg-icon icon-class="angle-left" size="1.2rem" />
					</n-button>
					<div class="date-display">
						<span class="date-main">{{ dateMain }}</span>
						<span class="date-sub">{{ dateSub }}</span>
					</div>
					<n-button text @click="changeDate(1)" class="nav-btn">
						<svg-icon icon-class="angle-right" size="1.2rem" />
					</n-button>
				</div>
				<div class="header-right">
					<n-date-picker
						v-model:formatted-value="selectedDate"
						type="date"
						value-format="yyyy-MM-dd"
						size="small"
						style="width: 140px"
					/>
				</div>
			</div>

			<!-- 心情 + 天气栏 -->
			<div class="meta-bar">
				<div class="meta-group">
					<span class="meta-label">心情</span>
					<div class="meta-items">
						<span
							v-for="m in moods"
							:key="m.value"
							class="meta-chip"
							:class="{ active: currentMood === m.value }"
							@click="currentMood = m.value"
						>{{ m.emoji }}</span>
					</div>
					<span v-if="currentMood" class="mood-text">{{ moodLabel }}</span>
				</div>
			</div>

			<!-- 当日代办摘要 -->
			<div class="day-todos-summary" v-if="dayTodos.length > 0">
				<div class="summary-header">
					<svg-icon icon-class="calendar" size="0.85rem" />
					<span>当日代办 ({{ completedCount }}/{{ dayTodos.length }})</span>
					<div class="progress-bar">
						<div class="progress-fill" :style="{ width: progressPct + '%' }"></div>
					</div>
				</div>
				<div class="summary-list">
					<div v-for="t in dayTodos" :key="t.id" class="summary-item" @click="handleToggleTodo(t)">
						<span class="todo-check" :class="{ done: t.status === 1 }">
							<svg-icon v-if="t.status === 1" icon-class="badge" size="0.85rem" />
						</span>
						<span class="todo-text" :class="{ done: t.status === 1 }">{{ t.title }}</span>
						<span class="priority-badge" :class="'p' + t.priority">{{ priorityLabel(t.priority) }}</span>
					</div>
				</div>
			</div>

			<!-- Markdown 编辑器 -->
			<div class="editor-wrapper">
				<MdEditor
					v-model="diaryContent"
					:style="{ height: editorHeight }"
					placeholder="记录今天的想法、感悟、收获... 支持 Markdown 语法"
					:toolbars="editorToolbars"
					:preview="true"
					show-code-row-number
				/>
			</div>

			<!-- 语音输入浮动按钮 -->
			<div class="voice-bar">
				<n-button
					:type="isListening ? 'error' : 'default'"
					circle
					size="small"
					@click="toggleVoice"
					:title="isListening ? '停止语音输入' : '开始语音输入'"
					class="voice-btn"
				>
					<span class="voice-icon" :class="{ active: isListening }">🎙️</span>
				</n-button>
				<span v-if="isListening" class="voice-status">
					<span class="voice-dot"></span> 正在聆听...
				</span>
				<span v-if="!speechSupported" class="voice-unsupported">浏览器不支持语音输入</span>
			</div>

			<!-- 底部操作栏 -->
			<div class="diary-footer">
				<div class="footer-info">
					<span v-if="diaryId" class="save-hint">已保存</span>
					<span v-else class="save-hint new">新日记</span>
				</div>
				<div class="footer-actions">
					<n-button v-if="diaryId" quaternary type="error" size="small" @click="handleDelete" :loading="deleting">
						删除
					</n-button>
					<n-button type="primary" size="small" @click="handleSave" :loading="saving">
						<svg-icon icon-class="edit" size="0.85rem" style="margin-right: 4px" />
						保存日记
					</n-button>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { getDiary, saveDiary, deleteDiary, getCalendarData, toggleTodoStatus } from "@/api/todo";
import type { TodoItem } from "@/api/todo/types";
import { useWindowSize } from "@vueuse/core";
import { MdEditor } from "md-editor-v3";
import type { ToolbarNames } from "md-editor-v3";
import "md-editor-v3/lib/style.css";

const { width: winWidth } = useWindowSize();

const today = new Date();
const fmtDate = (d: Date) => {
	const y = d.getFullYear();
	const m = String(d.getMonth() + 1).padStart(2, "0");
	const day = String(d.getDate()).padStart(2, "0");
	return `${y}-${m}-${day}`;
};

const weekDays = ["周日", "周一", "周二", "周三", "周四", "周五", "周六"];

const selectedDate = ref(fmtDate(today));
const diaryContent = ref("");
const currentMood = ref("");
const diaryId = ref<number | null>(null);
const saving = ref(false);
const deleting = ref(false);
const dayTodos = ref<TodoItem[]>([]);

const editorHeight = computed(() => (winWidth.value < 768 ? "300px" : "420px"));

const editorToolbars: ToolbarNames[] = [
	"bold", "underline", "italic", "strikeThrough", "-",
	"title", "quote", "unorderedList", "orderedList", "task", "-",
	"codeRow", "code", "link", "image", "table", "-",
	"revoke", "next", "=",
	"pageFullscreen", "fullscreen", "preview", "catalog",
];

const dateMain = computed(() => {
	const d = new Date(selectedDate.value);
	return `${d.getMonth() + 1}月${d.getDate()}日`;
});

const dateSub = computed(() => {
	const d = new Date(selectedDate.value);
	return `${d.getFullYear()} ${weekDays[d.getDay()]}`;
});

const moods = [
	{ value: "happy", label: "开心", emoji: "😊" },
	{ value: "calm", label: "平静", emoji: "😌" },
	{ value: "tired", label: "疲惫", emoji: "😩" },
	{ value: "excited", label: "兴奋", emoji: "🤩" },
	{ value: "sad", label: "低落", emoji: "😢" },
	{ value: "focused", label: "专注", emoji: "🎯" },
	{ value: "angry", label: "生气", emoji: "😠" },
	{ value: "love", label: "喜欢", emoji: "😍" },
];

const moodLabel = computed(() => moods.find((m) => m.value === currentMood.value)?.label || "");

const completedCount = computed(() => dayTodos.value.filter((t) => t.status === 1).length);
const progressPct = computed(() => (dayTodos.value.length > 0 ? Math.round((completedCount.value / dayTodos.value.length) * 100) : 0));
const priorityLabel = (p: number) => (p === 0 ? "低" : p === 1 ? "中" : "高");

const changeDate = (dir: number) => {
	const d = new Date(selectedDate.value);
	d.setDate(d.getDate() + dir);
	selectedDate.value = fmtDate(d);
};

const loadDiary = () => {
	getDiary(selectedDate.value).then(({ data }) => {
		if (data.flag && data.data) {
			diaryContent.value = data.data.content || "";
			currentMood.value = data.data.mood || "";
			diaryId.value = data.data.id;
		} else {
			diaryContent.value = "";
			currentMood.value = "";
			diaryId.value = null;
		}
	});
	getCalendarData(selectedDate.value + " 00:00:00", selectedDate.value + " 23:59:59").then(({ data }) => {
		if (data.flag) dayTodos.value = data.data || [];
	});
};

const handleToggleTodo = (item: TodoItem) => {
	toggleTodoStatus(item.id).then(({ data }) => {
		if (data.flag) {
			item.status = item.status === 1 ? 0 : 1;
		}
	});
};

const handleSave = () => {
	if (!diaryContent.value.trim()) {
		window.$message?.warning("日记内容不能为空");
		return;
	}
	saving.value = true;
	saveDiary({ id: diaryId.value || undefined, diaryDate: selectedDate.value, content: diaryContent.value, mood: currentMood.value })
		.then(({ data }) => {
			if (data.flag) {
				window.$message?.success("保存成功");
				loadDiary();
			}
		})
		.finally(() => (saving.value = false));
};

const handleDelete = () => {
	if (!diaryId.value) return;
	window.$dialog?.warning({
		title: "确认删除",
		content: "删除后不可恢复",
		positiveText: "删除",
		negativeText: "取消",
		onPositiveClick: () => {
			deleting.value = true;
			deleteDiary(diaryId.value!).then(({ data }) => {
				if (data.flag) {
					window.$message?.success("已删除");
					diaryContent.value = "";
					currentMood.value = "";
					diaryId.value = null;
				}
			}).finally(() => (deleting.value = false));
		},
	});
};

// 语音输入
const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
const speechSupported = ref(!!SpeechRecognition);
const isListening = ref(false);
let recognition: any = null;

if (SpeechRecognition) {
	recognition = new SpeechRecognition();
	recognition.lang = "zh-CN";
	recognition.continuous = true;
	recognition.interimResults = true;

	let finalTranscript = "";

	recognition.onresult = (event: any) => {
		let interim = "";
		for (let i = event.resultIndex; i < event.results.length; i++) {
			const transcript = event.results[i][0].transcript;
			if (event.results[i].isFinal) {
				finalTranscript += transcript;
			} else {
				interim += transcript;
			}
		}
		if (finalTranscript) {
			diaryContent.value += finalTranscript;
			finalTranscript = "";
		}
	};

	recognition.onerror = (event: any) => {
		if (event.error !== "no-speech") {
			window.$message?.warning("语音识别出错: " + event.error);
		}
		isListening.value = false;
	};

	recognition.onend = () => {
		if (isListening.value) {
			recognition.start();
		}
	};
}

const toggleVoice = () => {
	if (!recognition) {
		window.$message?.warning("当前浏览器不支持语音输入，请使用 Chrome");
		return;
	}
	if (isListening.value) {
		isListening.value = false;
		recognition.stop();
	} else {
		isListening.value = true;
		recognition.start();
	}
};

onUnmounted(() => {
	if (recognition && isListening.value) {
		isListening.value = false;
		recognition.stop();
	}
});

watch(selectedDate, loadDiary, { immediate: true });
</script>

<style lang="scss" scoped>
.diary-container {
	max-width: 800px;
	margin: 0 auto;
	color: var(--grey-7, #333);
}
.diary-card {
	border-radius: 0.75rem;
	background: var(--card-bg, #fff);
	box-shadow: var(--card-shadow);
	overflow: hidden;
}

.diary-card-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 1rem 1.25rem;
	border-bottom: 1px solid var(--grey-2, #f0f0f0);
}
.header-left {
	display: flex;
	align-items: center;
	gap: 0.5rem;
}
.nav-btn {
	opacity: 0.6;
	transition: opacity 0.2s;
	&:hover { opacity: 1; }
}
.date-display {
	display: flex;
	flex-direction: column;
	align-items: center;
	min-width: 5rem;
}
.date-main {
	font-size: 1.2rem;
	font-weight: 700;
	line-height: 1.3;
}
.date-sub {
	font-size: 0.75rem;
	color: var(--grey-5);
}

.meta-bar {
	padding: 0.6rem 1.25rem;
	border-bottom: 1px solid var(--grey-2, #f0f0f0);
}
.meta-group {
	display: flex;
	align-items: center;
	gap: 0.5rem;
	flex-wrap: wrap;
}
.meta-label {
	font-size: 0.8rem;
	font-weight: 500;
	color: var(--grey-5);
}
.meta-items {
	display: flex;
	gap: 0.25rem;
}
.meta-chip {
	width: 2rem;
	height: 2rem;
	display: flex;
	align-items: center;
	justify-content: center;
	border-radius: 50%;
	font-size: 1.1rem;
	cursor: pointer;
	transition: all 0.2s;
	border: 2px solid transparent;
	&:hover {
		background: var(--grey-2, #f5f5f5);
		transform: scale(1.15);
	}
	&.active {
		border-color: var(--primary-color);
		background: rgba(64, 158, 255, 0.1);
		transform: scale(1.2);
	}
}
.mood-text {
	font-size: 0.8rem;
	color: var(--primary-color);
	font-weight: 500;
}

.day-todos-summary {
	padding: 0.75rem 1.25rem;
	border-bottom: 1px solid var(--grey-2, #f0f0f0);
	background: rgba(64, 158, 255, 0.03);
}
.summary-header {
	display: flex;
	align-items: center;
	gap: 0.4rem;
	font-size: 0.8rem;
	font-weight: 600;
	margin-bottom: 0.5rem;
	color: var(--grey-6);
}
.progress-bar {
	flex: 1;
	max-width: 120px;
	height: 4px;
	border-radius: 2px;
	background: var(--grey-2, #eee);
	margin-left: 0.5rem;
}
.progress-fill {
	height: 100%;
	border-radius: 2px;
	background: linear-gradient(90deg, #667eea, #764ba2);
	transition: width 0.3s;
}
.summary-list {
	display: flex;
	flex-direction: column;
	gap: 0.25rem;
}
.summary-item {
	display: flex;
	align-items: center;
	gap: 0.4rem;
	font-size: 0.78rem;
	padding: 0.2rem 0;
	cursor: pointer;
	border-radius: 4px;
	transition: background 0.15s;
	&:hover { background: rgba(0, 0, 0, 0.03); }
}
.todo-check {
	width: 1.1rem;
	height: 1.1rem;
	border-radius: 50%;
	border: 2px solid var(--grey-4);
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
	transition: all 0.2s;
	&.done {
		border-color: var(--primary-color);
		background: var(--primary-color);
		color: #fff;
	}
}
.todo-text {
	flex: 1;
	&.done { text-decoration: line-through; color: var(--grey-5); }
}
.priority-badge {
	font-size: 0.6rem;
	padding: 0 4px;
	border-radius: 3px;
	color: #fff;
	line-height: 1.4;
	&.p0 { background: #8bc34a; }
	&.p1 { background: #ff9800; }
	&.p2 { background: #f44336; }
}

.editor-wrapper {
	:deep(.md-editor) {
		border: none;
		border-radius: 0;
		box-shadow: none;
	}
}

.voice-bar {
	display: flex;
	align-items: center;
	gap: 0.5rem;
	padding: 0.5rem 1.25rem;
	border-top: 1px solid var(--grey-2, #f0f0f0);
}
.voice-btn {
	flex-shrink: 0;
}
.voice-icon {
	font-size: 1rem;
	transition: transform 0.2s;
	&.active {
		animation: pulse 1s ease-in-out infinite;
	}
}
@keyframes pulse {
	0%, 100% { transform: scale(1); }
	50% { transform: scale(1.25); }
}
.voice-status {
	display: flex;
	align-items: center;
	gap: 0.3rem;
	font-size: 0.8rem;
	color: #f44336;
	font-weight: 500;
}
.voice-dot {
	width: 6px;
	height: 6px;
	border-radius: 50%;
	background: #f44336;
	animation: blink 1s infinite;
}
@keyframes blink {
	0%, 100% { opacity: 1; }
	50% { opacity: 0.3; }
}
.voice-unsupported {
	font-size: 0.75rem;
	color: var(--grey-5);
}

.diary-footer {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 0.75rem 1.25rem;
	border-top: 1px solid var(--grey-2, #f0f0f0);
	background: var(--grey-1-a3, rgba(250, 250, 250, 0.5));
}
.footer-info {
	display: flex;
	align-items: center;
	gap: 0.5rem;
}
.save-hint {
	font-size: 0.75rem;
	color: var(--primary-color);
	&.new { color: var(--grey-5); }
}
.footer-actions {
	display: flex;
	gap: 0.5rem;
}

@media (max-width: 767px) {
	.diary-card-header {
		flex-direction: column;
		gap: 0.5rem;
		padding: 0.75rem 1rem;
	}
	.date-main { font-size: 1rem; }
	.meta-bar { padding: 0.5rem 1rem; }
	.meta-chip {
		width: 1.75rem;
		height: 1.75rem;
		font-size: 1rem;
	}
	.day-todos-summary { padding: 0.6rem 1rem; }
	.diary-footer { padding: 0.6rem 1rem; }
	.voice-bar { padding: 0.4rem 1rem; }
}
</style>
