<template>
	<div class="diary-container">
		<div class="diary-header">
			<n-button text @click="changeDate(-1)">
				<svg-icon icon-class="angle-left" size="1.2rem" />
			</n-button>
			<n-date-picker v-model:formatted-value="selectedDate" type="date" value-format="yyyy-MM-dd" style="width: 180px" />
			<n-button text @click="changeDate(1)">
				<svg-icon icon-class="angle-right" size="1.2rem" />
			</n-button>
		</div>

		<!-- 当日代办摘要 -->
		<div class="day-todos-summary" v-if="dayTodos.length > 0">
			<div class="summary-title">
				<svg-icon icon-class="calendar" size="0.9rem" style="margin-right: 4px" />
				当日代办 ({{ dayTodos.length }})
			</div>
			<div class="summary-list">
				<div v-for="t in dayTodos" :key="t.id" class="summary-item">
					<span class="priority-dot" :class="'p' + t.priority"></span>
					<span :class="{ done: t.status === 1 }">{{ t.title }}</span>
				</div>
			</div>
		</div>

		<!-- 心情选择 -->
		<div class="mood-bar">
			<span class="mood-label">心情：</span>
			<span
				v-for="m in moods"
				:key="m.value"
				class="mood-item"
				:class="{ active: currentMood === m.value }"
				@click="currentMood = m.value"
			>{{ m.emoji }} {{ m.label }}</span>
		</div>

		<!-- 日记编辑 -->
		<div class="diary-editor">
			<n-input
				v-model:value="diaryContent"
				type="textarea"
				placeholder="记录今天的想法、感悟、收获..."
				:rows="10"
				:autosize="{ minRows: 8, maxRows: 20 }"
			/>
		</div>

		<div class="diary-actions">
			<n-button type="primary" @click="handleSave" :loading="saving">
				保存日记
			</n-button>
			<n-button v-if="diaryId" @click="handleDelete" :loading="deleting">
				删除
			</n-button>
		</div>
	</div>
</template>

<script setup lang="ts">
import { getDiary, saveDiary, deleteDiary, getCalendarData } from "@/api/todo";
import type { TodoItem } from "@/api/todo/types";

const today = new Date();
const fmtDate = (d: Date) => {
	const y = d.getFullYear();
	const m = String(d.getMonth() + 1).padStart(2, "0");
	const day = String(d.getDate()).padStart(2, "0");
	return `${y}-${m}-${day}`;
};

const selectedDate = ref(fmtDate(today));
const diaryContent = ref("");
const currentMood = ref("");
const diaryId = ref<number | null>(null);
const saving = ref(false);
const deleting = ref(false);
const dayTodos = ref<TodoItem[]>([]);

const moods = [
	{ value: "happy", label: "开心", emoji: "😊" },
	{ value: "calm", label: "平静", emoji: "😌" },
	{ value: "tired", label: "疲惫", emoji: "😩" },
	{ value: "excited", label: "兴奋", emoji: "🤩" },
	{ value: "sad", label: "低落", emoji: "😢" },
	{ value: "focused", label: "专注", emoji: "🎯" },
];

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

watch(selectedDate, loadDiary, { immediate: true });
</script>

<style lang="scss" scoped>
.diary-container {
	max-width: 700px;
	margin: 0 auto;
	color: var(--grey-7, #333);
}
.diary-header {
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 0.75rem;
	margin-bottom: 1rem;
}
.day-todos-summary {
	padding: 0.75rem 1rem;
	border-radius: 0.5rem;
	background: rgba(64, 158, 255, 0.06);
	margin-bottom: 1rem;
}
.summary-title {
	font-size: 0.85rem;
	font-weight: 600;
	margin-bottom: 0.4rem;
	display: flex;
	align-items: center;
}
.summary-item {
	display: flex;
	align-items: center;
	gap: 6px;
	font-size: 0.8rem;
	padding: 2px 0;
	.done { text-decoration: line-through; opacity: 0.5; }
}
.priority-dot {
	width: 6px; height: 6px; border-radius: 50%; flex-shrink: 0;
	&.p0 { background: #8bc34a; }
	&.p1 { background: #ff9800; }
	&.p2 { background: #f44336; }
}
.mood-bar {
	display: flex;
	align-items: center;
	gap: 0.5rem;
	margin-bottom: 0.75rem;
	flex-wrap: wrap;
}
.mood-label {
	font-size: 0.85rem;
	font-weight: 500;
}
.mood-item {
	padding: 3px 10px;
	border-radius: 1rem;
	font-size: 0.8rem;
	cursor: pointer;
	border: 1px solid var(--grey-3);
	color: var(--grey-6, #666);
	transition: all 0.2s;
	&.active {
		background: var(--primary-color);
		color: #fff;
		border-color: var(--primary-color);
	}
	&:hover:not(.active) {
		border-color: var(--primary-color);
	}
}
.diary-editor {
	margin-bottom: 1rem;
}
.diary-actions {
	display: flex;
	gap: 0.75rem;
	justify-content: flex-end;
}
</style>
