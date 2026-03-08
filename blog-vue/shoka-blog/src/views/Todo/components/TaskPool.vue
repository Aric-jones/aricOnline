<template>
	<div class="tp-container">
		<!-- ========== 未分配任务区 ========== -->
		<div
			class="tp-section tp-pool-section"
			:class="{ 'drag-target': draggingSource === 'week' && poolDragOver }"
			@dragover.prevent="onPoolDragover"
			@dragenter.prevent="onPoolDragenter"
			@dragleave="onPoolDragleave"
			@drop="onDropToPool"
		>
			<div class="tp-section-header">
				<div class="tp-title-row">
					<h3 class="tp-section-title">
						未分配任务
						<span class="badge">{{ unassignedTasks.length }}</span>
					</h3>
				</div>
				<div class="tp-action-row">
					<n-input
						v-model:value="keyword"
						placeholder="搜索..."
						clearable
						round
						size="small"
						style="width: 140px"
						@update:value="debouncedLoadPool"
					/>
					<button class="tp-btn-primary" @click="openAdd">＋ 添加任务</button>
				</div>
			</div>

			<div v-if="unassignedTasks.length === 0" class="tp-empty-hint">
				{{ keyword ? '没有匹配的任务' : '暂无未分配任务，添加一些想做的事吧' }}
			</div>

			<div class="tp-card-grid" v-else>
				<div
					v-for="task in unassignedTasks"
					:key="task.id"
					class="tp-card"
					:class="{ 'is-dragging': draggingTaskId === task.id, completed: task.status === 1 }"
					draggable="true"
					@dragstart="onDragStart($event, task, 'pool')"
					@dragend="onDragEnd"
					@dragover.stop
					@dragenter.stop
				>
					<div class="card-top">
						<span class="priority-badge" :class="'p-' + task.priority">
							{{ priorityLabel(task.priority) }}
						</span>
						<span v-if="task.category" class="category-tag">{{ task.category }}</span>
					</div>
					<div class="card-title" :class="{ done: task.status === 1 }">{{ task.title }}</div>
					<div v-if="task.description" class="card-desc">{{ task.description }}</div>
					<div class="card-bottom">
						<button class="icon-btn check-btn" :class="{ checked: task.status === 1 }" @click.stop="toggleStatus(task)">
							{{ task.status === 1 ? '✓' : '○' }}
						</button>
						<button class="icon-btn assign-btn" @click.stop="quickAssign(task)" title="分配到当前周">→周</button>
						<button class="icon-btn" @click.stop="openEdit(task)" title="编辑">✎</button>
						<button class="icon-btn danger" @click.stop="handleDelete(task)" title="删除">✕</button>
					</div>
				</div>
			</div>
		</div>

		<!-- ========== 视图区 ========== -->
		<div class="tp-section tp-view-section">
			<div class="tp-view-header">
				<div class="view-tabs">
					<button
						v-for="v in viewOptions"
						:key="v.value"
						class="view-tab"
						:class="{ active: currentView === v.value }"
						@click="switchView(v.value)"
					>{{ v.label }}</button>
				</div>
				<div class="view-nav">
					<button class="tp-btn-icon" @click="navigateDate(-1)">
						<svg-icon icon-class="angle-left" />
					</button>
					<span class="view-date-label" @click="goToday">{{ dateLabel }}</span>
					<button class="tp-btn-icon" @click="navigateDate(1)">
						<svg-icon icon-class="angle-right" />
					</button>
				</div>
			</div>

			<!-- 周视图 -->
			<div
				v-if="currentView === 'week'"
				class="tp-week-zone"
				:class="{ 'drag-target': draggingSource === 'pool' && weekDragOver }"
				@dragover.prevent="onWeekDragover"
				@dragenter.prevent="onWeekDragenter"
				@dragleave="onWeekDragleave"
				@drop="onDropToWeek"
			>
				<div v-if="weekTasks.length === 0 && !weekDragOver" class="tp-drop-hint">
					<span class="hint-icon">↓</span>
					<span>将任务从上方拖拽到此处，分配到本周</span>
				</div>
				<div v-if="weekDragOver" class="tp-drop-hint active">
					<span class="hint-icon">✦</span>
					<span>松开鼠标，分配到本周</span>
				</div>
				<div v-if="weekTasks.length > 0" class="tp-task-list">
					<div
						v-for="task in weekTasks"
						:key="task.id"
						class="tp-task-row"
						:class="{ completed: task.status === 1, 'is-dragging': draggingTaskId === task.id }"
						draggable="true"
						@dragstart="onDragStart($event, task, 'week')"
						@dragend="onDragEnd"
						@dragover.stop
						@dragenter.stop
					>
						<button class="status-circle" :class="{ checked: task.status === 1 }" @click="toggleStatus(task)">
							{{ task.status === 1 ? '✓' : '' }}
						</button>
						<div class="task-content">
							<span class="priority-dot" :class="'p-' + task.priority"></span>
							<span class="task-name" :class="{ done: task.status === 1 }">{{ task.title }}</span>
							<span v-if="task.category" class="category-tag sm">{{ task.category }}</span>
						</div>
						<div class="task-row-actions">
							<button class="icon-btn" @click.stop="openEdit(task)" title="编辑">✎</button>
							<button class="icon-btn" @click.stop="handleUnassign(task.id)" title="退回任务池">↩</button>
							<button class="icon-btn danger" @click.stop="handleDelete(task)" title="删除">✕</button>
						</div>
					</div>
				</div>
				<div class="week-summary" v-if="weekTasks.length > 0">
					共 {{ weekTasks.length }} 个任务，已完成 {{ weekTasks.filter(t => t.status === 1).length }} 个
				</div>
			</div>

			<!-- 月视图 -->
			<div v-if="currentView === 'month'" class="tp-month-view">
				<div v-if="monthWeeks.length === 0" class="tp-empty-hint">本月暂无分配的任务</div>
				<div v-for="week in monthWeeks" :key="week.monday" class="month-week-group">
					<div class="week-group-header" :class="{ current: isCurrentWeek(week.monday) }">
						<span class="week-label">{{ week.label }}</span>
						<span class="week-range">{{ week.range }}</span>
						<span class="week-count">{{ week.tasks.length }} 个任务</span>
					</div>
					<div v-if="week.tasks.length === 0" class="week-empty">暂无任务</div>
					<div v-else class="tp-task-list compact">
						<div
							v-for="task in week.tasks"
							:key="task.id"
							class="tp-task-row compact"
							:class="{ completed: task.status === 1 }"
						>
							<span class="status-dot" :class="{ checked: task.status === 1 }">{{ task.status === 1 ? '✓' : '○' }}</span>
							<span class="priority-dot" :class="'p-' + task.priority"></span>
							<span class="task-name" :class="{ done: task.status === 1 }">{{ task.title }}</span>
							<span v-if="task.category" class="category-tag sm">{{ task.category }}</span>
						</div>
					</div>
				</div>
			</div>

			<!-- 年视图 -->
			<div v-if="currentView === 'year'" class="tp-year-view">
				<div v-for="m in yearMonths" :key="m.month" class="year-month-group">
					<div class="month-group-header" :class="{ current: isCurrentMonth(m.month) }" @click="toggleExpand(m.month)">
						<span class="month-label">{{ m.label }}</span>
						<div class="month-bar-wrap">
							<div class="month-bar" :style="{ width: getBarWidth(m.tasks.length) }"></div>
						</div>
						<span class="month-count">{{ m.tasks.length }}</span>
						<span class="expand-icon">{{ expandedMonths.has(m.month) ? '▾' : '▸' }}</span>
					</div>
					<div v-if="expandedMonths.has(m.month)" class="month-expand">
						<div v-if="m.tasks.length === 0" class="week-empty">暂无任务</div>
						<div v-else class="tp-task-list compact">
							<div
								v-for="task in m.tasks"
								:key="task.id"
								class="tp-task-row compact"
								:class="{ completed: task.status === 1 }"
							>
								<span class="status-dot" :class="{ checked: task.status === 1 }">{{ task.status === 1 ? '✓' : '○' }}</span>
								<span class="priority-dot" :class="'p-' + task.priority"></span>
								<span class="task-name" :class="{ done: task.status === 1 }">{{ task.title }}</span>
								<span v-if="task.category" class="category-tag sm">{{ task.category }}</span>
								<span class="week-tag">{{ formatWeekTag(task.weekStartDate) }}</span>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>

		<!-- ========== 添加/编辑弹窗 ========== -->
		<n-modal v-model:show="showModal" :mask-closable="true" display-directive="if">
			<div class="modal-glass">
				<div class="modal-title">{{ isEdit ? '编辑任务' : '添加任务' }}</div>
				<div class="form-group">
					<label class="form-label required">标题</label>
					<n-input v-model:value="form.title" placeholder="任务标题" round />
				</div>
				<div class="form-group">
					<label class="form-label">描述</label>
					<n-input v-model:value="form.description" type="textarea" placeholder="任务描述（可选）" :rows="3" />
				</div>
				<div class="form-group">
					<label class="form-label">分类</label>
					<n-input v-model:value="form.category" placeholder="输入分类（可选）" round />
				</div>
				<div class="form-group">
					<label class="form-label">优先级</label>
					<div class="priority-select">
						<button
							v-for="p in priorityOptions"
							:key="p.value"
							class="priority-option"
							:class="{ active: form.priority === p.value, ['p-' + p.value]: true }"
							@click="form.priority = p.value"
						>{{ p.label }}</button>
					</div>
				</div>
				<div class="modal-footer">
					<button class="btn-ghost" @click="showModal = false">取消</button>
					<button class="tp-btn-primary" @click="handleSubmit" :disabled="submitting">
						{{ submitting ? '提交中...' : '确定' }}
					</button>
				</div>
			</div>
		</n-modal>
	</div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from "vue";
import {
	getUnassignedTasks, getWeekTasks, getRangeTasks,
	addPoolTask, updatePoolTask, deletePoolTask,
	assignPoolTask, unassignPoolTask, togglePoolTaskStatus
} from "@/api/todo";
import type { TaskPoolItem, TaskPoolReq } from "@/api/todo/types";

const unassignedTasks = ref<TaskPoolItem[]>([]);
const weekTasks = ref<TaskPoolItem[]>([]);
const rangeTasks = ref<TaskPoolItem[]>([]);
const keyword = ref("");
const currentView = ref<"week" | "month" | "year">("week");
const currentDate = ref(new Date());
const showModal = ref(false);
const isEdit = ref(false);
const submitting = ref(false);
const expandedMonths = ref(new Set<number>());
const form = ref<TaskPoolReq>({ title: "", description: "", category: "", priority: 0 });

const draggingTaskId = ref<number | null>(null);
const draggingSource = ref<"pool" | "week" | null>(null);
const poolDragOver = ref(false);
const weekDragOver = ref(false);

const viewOptions = [
	{ label: "周", value: "week" as const },
	{ label: "月", value: "month" as const },
	{ label: "年", value: "year" as const },
];
const priorityOptions = [
	{ label: "低", value: 0 },
	{ label: "中", value: 1 },
	{ label: "高", value: 2 },
];

// ==================== 日期工具 ====================
function getMonday(date: Date): Date {
	const d = new Date(date);
	const day = d.getDay();
	d.setDate(d.getDate() - day + (day === 0 ? -6 : 1));
	d.setHours(0, 0, 0, 0);
	return d;
}
function fmtDate(d: Date): string {
	return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
}
function fmtShort(d: Date): string {
	return `${d.getMonth() + 1}/${d.getDate()}`;
}
function addDays(d: Date, n: number): Date {
	const r = new Date(d);
	r.setDate(r.getDate() + n);
	return r;
}

// ==================== 计算属性 ====================
const currentMonday = computed(() => getMonday(currentDate.value));
const currentSunday = computed(() => addDays(currentMonday.value, 6));
const currentMondayStr = computed(() => fmtDate(currentMonday.value));

const dateLabel = computed(() => {
	if (currentView.value === "week") {
		const todayMonday = fmtDate(getMonday(new Date()));
		const tag = currentMondayStr.value === todayMonday ? " (本周)" : "";
		return `${fmtShort(currentMonday.value)} - ${fmtShort(currentSunday.value)}${tag}`;
	}
	if (currentView.value === "month") {
		return `${currentDate.value.getFullYear()}年${currentDate.value.getMonth() + 1}月`;
	}
	return `${currentDate.value.getFullYear()}年`;
});

const monthWeeks = computed(() => {
	const year = currentDate.value.getFullYear();
	const month = currentDate.value.getMonth();
	const firstDay = new Date(year, month, 1);
	const lastDay = new Date(year, month + 1, 0);
	let monday = getMonday(firstDay);
	const weeks: { monday: string; label: string; range: string; tasks: TaskPoolItem[] }[] = [];
	let idx = 1;
	while (monday <= lastDay) {
		const sun = addDays(monday, 6);
		const ms = fmtDate(monday);
		weeks.push({
			monday: ms,
			label: `第${idx}周`,
			range: `${fmtShort(monday)} - ${fmtShort(sun)}`,
			tasks: rangeTasks.value.filter((t) => t.weekStartDate === ms),
		});
		monday = addDays(monday, 7);
		idx++;
	}
	return weeks;
});

const yearMonths = computed(() => {
	return Array.from({ length: 12 }, (_, i) => ({
		month: i,
		label: `${i + 1}月`,
		tasks: rangeTasks.value.filter((t) => {
			if (!t.weekStartDate) return false;
			return new Date(t.weekStartDate + "T00:00:00").getMonth() === i;
		}),
	}));
});

const maxMonthTasks = computed(() => Math.max(1, ...yearMonths.value.map((m) => m.tasks.length)));

function getBarWidth(count: number): string {
	return `${Math.round((count / maxMonthTasks.value) * 100)}%`;
}

// ==================== 导航 ====================
function navigateDate(delta: number) {
	const d = new Date(currentDate.value);
	if (currentView.value === "week") d.setDate(d.getDate() + delta * 7);
	else if (currentView.value === "month") d.setMonth(d.getMonth() + delta);
	else d.setFullYear(d.getFullYear() + delta);
	currentDate.value = d;
}
function goToday() {
	currentDate.value = new Date();
}
function switchView(v: "week" | "month" | "year") {
	currentView.value = v;
}
function isCurrentWeek(mondayStr: string): boolean {
	return fmtDate(getMonday(new Date())) === mondayStr;
}
function isCurrentMonth(month: number): boolean {
	const now = new Date();
	return now.getFullYear() === currentDate.value.getFullYear() && now.getMonth() === month;
}
function toggleExpand(month: number) {
	const s = new Set(expandedMonths.value);
	if (s.has(month)) s.delete(month);
	else s.add(month);
	expandedMonths.value = s;
}
function formatWeekTag(ws: string | null): string {
	if (!ws) return "";
	const d = new Date(ws + "T00:00:00");
	return `${fmtShort(d)}-${fmtShort(addDays(d, 6))}`;
}

// ==================== 数据加载 ====================
async function loadPool() {
	const res = await getUnassignedTasks(keyword.value.trim() || undefined);
	unassignedTasks.value = res.data.data || [];
}
async function loadWeek() {
	const res = await getWeekTasks(currentMondayStr.value);
	weekTasks.value = res.data.data || [];
}
async function loadRange() {
	let start: string, end: string;
	if (currentView.value === "month") {
		const y = currentDate.value.getFullYear(), m = currentDate.value.getMonth();
		start = fmtDate(getMonday(new Date(y, m, 1)));
		end = fmtDate(new Date(y, m + 1, 0));
	} else {
		const y = currentDate.value.getFullYear();
		start = `${y}-01-01`;
		end = `${y}-12-31`;
	}
	const res = await getRangeTasks(start, end);
	rangeTasks.value = res.data.data || [];
}
async function loadViewData() {
	if (currentView.value === "week") await loadWeek();
	else await loadRange();
}

let debounceTimer: ReturnType<typeof setTimeout> | null = null;
function debouncedLoadPool() {
	if (debounceTimer) clearTimeout(debounceTimer);
	debounceTimer = setTimeout(loadPool, 300);
}

// ==================== 优先级 ====================
function priorityLabel(p: number): string {
	return ["低", "中", "高"][p] || "低";
}

// ==================== 拖拽 ====================
function onDragStart(e: DragEvent, task: TaskPoolItem, source: "pool" | "week") {
	draggingTaskId.value = task.id;
	draggingSource.value = source;
	if (e.dataTransfer) {
		e.dataTransfer.effectAllowed = "move";
		e.dataTransfer.setData("text/plain", String(task.id));
	}
}
function onDragEnd() {
	draggingTaskId.value = null;
	draggingSource.value = null;
	poolDragOver.value = false;
	weekDragOver.value = false;
}
function onPoolDragover(e: DragEvent) {
	if (draggingSource.value === "week") {
		if (e.dataTransfer) e.dataTransfer.dropEffect = "move";
	}
}
function onPoolDragenter(e: DragEvent) {
	if (draggingSource.value === "week") {
		poolDragOver.value = true;
		if (e.dataTransfer) e.dataTransfer.dropEffect = "move";
	}
}
function onPoolDragleave(e: DragEvent) {
	// 检查是否真的离开了容器（relatedTarget 不在容器内）
	const container = e.currentTarget as HTMLElement;
	const related = e.relatedTarget as HTMLElement | null;
	// 如果 relatedTarget 为 null 或不在容器内，才清除状态
	if (!related || !container.contains(related)) {
		poolDragOver.value = false;
	}
}
function onWeekDragover(e: DragEvent) {
	if (draggingSource.value === "pool") {
		if (e.dataTransfer) e.dataTransfer.dropEffect = "move";
	}
}
function onWeekDragenter(e: DragEvent) {
	if (draggingSource.value === "pool") {
		weekDragOver.value = true;
		if (e.dataTransfer) e.dataTransfer.dropEffect = "move";
	}
}
function onWeekDragleave(e: DragEvent) {
	// 检查是否真的离开了容器（relatedTarget 不在容器内）
	const container = e.currentTarget as HTMLElement;
	const related = e.relatedTarget as HTMLElement | null;
	// 如果 relatedTarget 为 null 或不在容器内，才清除状态
	if (!related || !container.contains(related)) {
		weekDragOver.value = false;
	}
}
async function onDropToPool(e: DragEvent) {
	e.preventDefault();
	poolDragOver.value = false;
	if (draggingSource.value !== "week" || !draggingTaskId.value) return;
	await handleUnassign(draggingTaskId.value);
	onDragEnd();
}
async function onDropToWeek(e: DragEvent) {
	e.preventDefault();
	weekDragOver.value = false;
	if (draggingSource.value !== "pool" || !draggingTaskId.value) return;
	await doAssign(draggingTaskId.value, currentMondayStr.value);
	onDragEnd();
}

// ==================== CRUD ====================
function openAdd() {
	isEdit.value = false;
	form.value = { title: "", description: "", category: "", priority: 0 };
	showModal.value = true;
}
function openEdit(task: TaskPoolItem) {
	isEdit.value = true;
	form.value = {
		id: task.id,
		title: task.title,
		description: task.description || "",
		category: task.category || "",
		priority: task.priority,
	};
	showModal.value = true;
}
async function handleSubmit() {
	if (!form.value.title?.trim()) {
		window.$message?.warning("标题不能为空");
		return;
	}
	submitting.value = true;
	try {
		if (isEdit.value) {
			await updatePoolTask(form.value);
			window.$message?.success("修改成功");
		} else {
			await addPoolTask(form.value);
			window.$message?.success("添加成功");
		}
		showModal.value = false;
		await Promise.all([loadPool(), loadViewData()]);
	} finally {
		submitting.value = false;
	}
}
function handleDelete(task: TaskPoolItem) {
	window.$dialog?.warning({
		title: "确认删除",
		content: `确定删除「${task.title}」？删除后不可恢复。`,
		positiveText: "删除",
		negativeText: "取消",
		onPositiveClick: async () => {
			await deletePoolTask(task.id);
			window.$message?.success("已删除");
			await Promise.all([loadPool(), loadViewData()]);
		},
	});
}
async function toggleStatus(task: TaskPoolItem) {
	await togglePoolTaskStatus(task.id);
	task.status = task.status === 0 ? 1 : 0;
}
async function doAssign(taskId: number, weekStart: string) {
	await assignPoolTask({ id: taskId, weekStartDate: weekStart });
	window.$message?.success("已分配到本周");
	await Promise.all([loadPool(), loadWeek()]);
}
async function quickAssign(task: TaskPoolItem) {
	await doAssign(task.id, currentMondayStr.value);
}
async function handleUnassign(taskId: number) {
	await unassignPoolTask(taskId);
	window.$message?.success("已退回任务池");
	await Promise.all([loadPool(), loadWeek()]);
}

// ==================== 侦听 ====================
watch(currentDate, () => loadViewData());
watch(currentView, () => {
	expandedMonths.value = new Set([new Date().getMonth()]);
	loadViewData();
});

onMounted(async () => {
	expandedMonths.value = new Set([new Date().getMonth()]);
	await Promise.all([loadPool(), loadWeek()]);
});
</script>

<style lang="scss" scoped>
.tp-container {
	max-width: 960px;
	margin: 0 auto;
	display: flex;
	flex-direction: column;
	gap: 1.25rem;
}

// ==================== 通用 section ====================
.tp-section {
	background: var(--glass-bg, rgba(255, 255, 255, 0.6));
	backdrop-filter: blur(16px);
	-webkit-backdrop-filter: blur(16px);
	border: 1.5px solid var(--glass-border, rgba(255, 255, 255, 0.4));
	border-radius: 16px;
	padding: 1.25rem;
	box-shadow: var(--todo-card-glow, 0 0 24px rgba(16, 185, 129, 0.06));
	transition: border-color 0.3s, box-shadow 0.3s;

	&.drag-target {
		border-color: var(--todo-primary, #10b981);
		box-shadow: 0 0 0 3px rgba(var(--todo-primary-rgb, 16, 185, 129), 0.18),
			var(--todo-card-glow, 0 0 24px rgba(16, 185, 129, 0.06));
	}
}

// ==================== 头部 ====================
.tp-section-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	flex-wrap: wrap;
	gap: 0.75rem;
	margin-bottom: 1rem;
}
.tp-title-row {
	display: flex;
	align-items: center;
	gap: 0.5rem;
}
.tp-section-title {
	font-size: 1rem;
	font-weight: 700;
	color: var(--grey-8, #1e293b);
	margin: 0;
	display: flex;
	align-items: center;
	gap: 0.4rem;
}
.badge {
	font-size: 0.7rem;
	font-weight: 700;
	min-width: 20px;
	height: 20px;
	line-height: 20px;
	text-align: center;
	border-radius: 10px;
	background: rgba(var(--todo-primary-rgb, 16, 185, 129), 0.12);
	color: var(--todo-primary, #10b981);
}
.tp-action-row {
	display: flex;
	align-items: center;
	gap: 0.5rem;
}

// ==================== 按钮 ====================
.tp-btn-primary {
	display: inline-flex;
	align-items: center;
	gap: 4px;
	padding: 0.45rem 1rem;
	border-radius: 50px;
	border: none;
	font-size: 0.82rem;
	font-weight: 600;
	cursor: pointer;
	background: linear-gradient(135deg, var(--todo-primary, #10b981), var(--todo-primary-light, #34d399));
	color: #fff;
	box-shadow: 0 4px 14px rgba(var(--todo-primary-rgb, 16, 185, 129), 0.3);
	transition: all 0.25s ease;
	font-family: inherit;
	white-space: nowrap;
	&:hover {
		transform: translateY(-1px);
		box-shadow: 0 6px 20px rgba(var(--todo-primary-rgb, 16, 185, 129), 0.4);
	}
	&:active {
		transform: translateY(0);
	}
	&:disabled {
		opacity: 0.6;
		cursor: not-allowed;
	}
}
.tp-btn-icon {
	width: 32px;
	height: 32px;
	border-radius: 8px;
	border: 1.5px solid rgba(var(--todo-primary-rgb, 16, 185, 129), 0.15);
	background: var(--glass-bg, rgba(255, 255, 255, 0.4));
	color: var(--grey-6, #64748b);
	cursor: pointer;
	display: flex;
	align-items: center;
	justify-content: center;
	transition: all 0.2s;
	&:hover {
		border-color: var(--todo-primary, #10b981);
		color: var(--todo-primary, #10b981);
	}
}
.btn-ghost {
	background: var(--glass-bg, rgba(255, 255, 255, 0.45));
	backdrop-filter: blur(8px);
	border: 1.5px solid rgba(var(--todo-primary-rgb, 16, 185, 129), 0.15);
	color: var(--grey-6, #64748b);
	padding: 0.5rem 1.2rem;
	border-radius: 50px;
	font-size: 0.82rem;
	cursor: pointer;
	font-weight: 600;
	font-family: inherit;
	transition: all 0.2s;
	&:hover {
		border-color: var(--todo-primary, #10b981);
		color: var(--todo-primary, #10b981);
	}
}
.icon-btn {
	background: none;
	border: none;
	cursor: pointer;
	font-size: 0.8rem;
	padding: 4px 6px;
	border-radius: 6px;
	color: var(--grey-5, #94a3b8);
	transition: all 0.15s;
	font-family: inherit;
	&:hover {
		background: rgba(var(--todo-primary-rgb, 16, 185, 129), 0.08);
		color: var(--todo-primary, #10b981);
	}
	&.danger:hover {
		background: rgba(239, 68, 68, 0.08);
		color: #ef4444;
	}
	&.check-btn.checked {
		color: var(--todo-primary, #10b981);
	}
	&.assign-btn {
		font-weight: 700;
		font-size: 0.75rem;
	}
}

// ==================== 空状态 ====================
.tp-empty-hint {
	text-align: center;
	padding: 2rem 0;
	color: var(--grey-5, #94a3b8);
	font-size: 0.88rem;
}

// ==================== 未分配任务卡片网格 ====================
.tp-card-grid {
	display: grid;
	grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
	gap: 0.75rem;
	max-height: 360px;
	overflow-y: auto;
	padding: 2px;
}
.tp-card {
	background: var(--todo-card-bg, var(--glass-bg, rgba(255, 255, 255, 0.6)));
	backdrop-filter: blur(12px);
	-webkit-backdrop-filter: blur(12px);
	border: 1.5px solid var(--glass-border, rgba(255, 255, 255, 0.5));
	border-radius: 14px;
	padding: 0.85rem;
	cursor: grab;
	transition: all 0.25s ease;
	display: flex;
	flex-direction: column;
	gap: 0.4rem;

	&:hover {
		transform: translateY(-2px);
		box-shadow: 0 6px 24px rgba(var(--todo-primary-rgb, 16, 185, 129), 0.12);
	}
	&.is-dragging {
		opacity: 0.4;
		transform: scale(0.95);
	}
	&.completed {
		opacity: 0.65;
	}
}
.card-top {
	display: flex;
	align-items: center;
	gap: 0.4rem;
	flex-wrap: wrap;
}
.card-title {
	font-size: 0.9rem;
	font-weight: 600;
	color: var(--grey-8, #1e293b);
	word-break: break-word;
	&.done {
		text-decoration: line-through;
		color: var(--grey-5, #94a3b8);
	}
}
.card-desc {
	font-size: 0.78rem;
	color: var(--grey-5, #64748b);
	display: -webkit-box;
	-webkit-line-clamp: 2;
	-webkit-box-orient: vertical;
	overflow: hidden;
}
.card-bottom {
	display: flex;
	align-items: center;
	gap: 2px;
	margin-top: auto;
	padding-top: 0.3rem;
	border-top: 1px solid rgba(var(--todo-primary-rgb, 16, 185, 129), 0.06);
}

// ==================== 优先级 & 分类标签 ====================
.priority-badge {
	font-size: 0.65rem;
	font-weight: 700;
	padding: 1px 8px;
	border-radius: 50px;
	line-height: 1.6;
	&.p-0 {
		background: rgba(148, 163, 184, 0.12);
		color: #94a3b8;
	}
	&.p-1 {
		background: rgba(245, 158, 11, 0.12);
		color: #d97706;
	}
	&.p-2 {
		background: rgba(239, 68, 68, 0.12);
		color: #ef4444;
	}
}
.priority-dot {
	display: inline-block;
	width: 8px;
	height: 8px;
	border-radius: 50%;
	flex-shrink: 0;
	&.p-0 {
		background: #94a3b8;
	}
	&.p-1 {
		background: #f59e0b;
	}
	&.p-2 {
		background: #ef4444;
	}
}
.category-tag {
	font-size: 0.68rem;
	padding: 1px 8px;
	border-radius: 50px;
	background: rgba(var(--todo-primary-rgb, 16, 185, 129), 0.1);
	color: var(--todo-primary, #10b981);
	font-weight: 600;
	white-space: nowrap;
	&.sm {
		font-size: 0.62rem;
		padding: 0 6px;
	}
}
.week-tag {
	font-size: 0.6rem;
	padding: 0 6px;
	border-radius: 50px;
	background: rgba(var(--todo-primary-rgb, 16, 185, 129), 0.06);
	color: var(--grey-5, #94a3b8);
	white-space: nowrap;
	margin-left: auto;
}

// ==================== 视图导航 ====================
.tp-view-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	flex-wrap: wrap;
	gap: 0.75rem;
	margin-bottom: 1rem;
}
.view-tabs {
	display: flex;
	gap: 4px;
	background: rgba(var(--todo-primary-rgb, 16, 185, 129), 0.06);
	border-radius: 10px;
	padding: 3px;
}
.view-tab {
	padding: 0.35rem 0.9rem;
	border-radius: 8px;
	border: none;
	background: transparent;
	font-size: 0.8rem;
	font-weight: 600;
	cursor: pointer;
	color: var(--grey-6, #64748b);
	transition: all 0.2s;
	font-family: inherit;
	&.active {
		background: var(--todo-primary, #10b981);
		color: #fff;
		box-shadow: 0 2px 8px rgba(var(--todo-primary-rgb, 16, 185, 129), 0.3);
	}
	&:not(.active):hover {
		color: var(--todo-primary, #10b981);
	}
}
.view-nav {
	display: flex;
	align-items: center;
	gap: 0.4rem;
}
.view-date-label {
	font-size: 0.88rem;
	font-weight: 600;
	color: var(--grey-7, #334155);
	cursor: pointer;
	min-width: 120px;
	text-align: center;
	white-space: nowrap;
	&:hover {
		color: var(--todo-primary, #10b981);
	}
}

// ==================== 周视图 drop zone ====================
.tp-week-zone {
	min-height: 120px;
	border: 2px dashed rgba(var(--todo-primary-rgb, 16, 185, 129), 0.2);
	border-radius: 14px;
	padding: 0.75rem;
	transition: all 0.3s;

	&.drag-target {
		border-color: var(--todo-primary, #10b981);
		background: rgba(var(--todo-primary-rgb, 16, 185, 129), 0.04);
	}
}
.tp-drop-hint {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 0.4rem;
	padding: 2rem;
	color: var(--grey-5, #94a3b8);
	font-size: 0.88rem;
	text-align: center;

	.hint-icon {
		font-size: 1.5rem;
		opacity: 0.5;
	}
	&.active {
		color: var(--todo-primary, #10b981);
		.hint-icon {
			opacity: 1;
			animation: pulse 0.8s infinite alternate;
		}
	}
}
@keyframes pulse {
	from { transform: scale(1); }
	to { transform: scale(1.2); }
}

// ==================== 任务列表行 ====================
.tp-task-list {
	display: flex;
	flex-direction: column;
	gap: 0.4rem;

	&.compact {
		gap: 0.25rem;
	}
}
.tp-task-row {
	display: flex;
	align-items: center;
	gap: 0.5rem;
	padding: 0.55rem 0.6rem;
	border-radius: 10px;
	background: rgba(var(--todo-primary-rgb, 16, 185, 129), 0.02);
	transition: all 0.2s;
	cursor: grab;

	&:hover {
		background: rgba(var(--todo-primary-rgb, 16, 185, 129), 0.06);
	}
	&.is-dragging {
		opacity: 0.4;
	}
	&.completed {
		opacity: 0.6;
	}
	&.compact {
		padding: 0.4rem 0.5rem;
		cursor: default;
	}
}
.status-circle {
	width: 24px;
	height: 24px;
	border-radius: 50%;
	border: 2px solid rgba(var(--todo-primary-rgb, 16, 185, 129), 0.3);
	background: transparent;
	cursor: pointer;
	font-size: 0.7rem;
	color: transparent;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
	transition: all 0.2s;

	&.checked {
		border-color: var(--todo-primary, #10b981);
		background: var(--todo-primary, #10b981);
		color: #fff;
	}
	&:hover {
		border-color: var(--todo-primary, #10b981);
	}
}
.status-dot {
	font-size: 0.75rem;
	flex-shrink: 0;
	width: 16px;
	text-align: center;
	color: var(--grey-5, #94a3b8);
	&.checked {
		color: var(--todo-primary, #10b981);
	}
}
.task-content {
	flex: 1;
	min-width: 0;
	display: flex;
	align-items: center;
	gap: 0.4rem;
	flex-wrap: wrap;
}
.task-name {
	font-size: 0.88rem;
	font-weight: 500;
	color: var(--grey-8, #1e293b);
	word-break: break-word;
	&.done {
		text-decoration: line-through;
		color: var(--grey-5, #94a3b8);
	}
}
.task-row-actions {
	display: flex;
	align-items: center;
	gap: 2px;
	flex-shrink: 0;
	opacity: 0;
	transition: opacity 0.2s;
}
.tp-task-row:hover .task-row-actions {
	opacity: 1;
}
.week-summary {
	text-align: center;
	font-size: 0.78rem;
	color: var(--grey-5, #94a3b8);
	margin-top: 0.6rem;
	padding-top: 0.5rem;
	border-top: 1px solid rgba(var(--todo-primary-rgb, 16, 185, 129), 0.08);
}

// ==================== 月视图 ====================
.tp-month-view {
	display: flex;
	flex-direction: column;
	gap: 0.75rem;
}
.month-week-group {
	border: 1.5px solid rgba(var(--todo-primary-rgb, 16, 185, 129), 0.08);
	border-radius: 12px;
	overflow: hidden;
}
.week-group-header {
	display: flex;
	align-items: center;
	gap: 0.6rem;
	padding: 0.6rem 0.85rem;
	background: rgba(var(--todo-primary-rgb, 16, 185, 129), 0.04);
	font-size: 0.82rem;

	&.current {
		background: rgba(var(--todo-primary-rgb, 16, 185, 129), 0.1);
		.week-label {
			color: var(--todo-primary, #10b981);
		}
	}
}
.week-label {
	font-weight: 700;
	color: var(--grey-7, #334155);
}
.week-range {
	font-size: 0.78rem;
	color: var(--grey-5, #94a3b8);
}
.week-count {
	margin-left: auto;
	font-size: 0.72rem;
	color: var(--grey-5, #94a3b8);
	white-space: nowrap;
}
.week-empty {
	padding: 0.5rem 0.85rem;
	font-size: 0.78rem;
	color: var(--grey-4, #cbd5e1);
}

// ==================== 年视图 ====================
.tp-year-view {
	display: flex;
	flex-direction: column;
	gap: 0.35rem;
}
.year-month-group {
	border-radius: 10px;
	overflow: hidden;
}
.month-group-header {
	display: flex;
	align-items: center;
	gap: 0.6rem;
	padding: 0.6rem 0.85rem;
	cursor: pointer;
	border-radius: 10px;
	transition: background 0.15s;

	&:hover {
		background: rgba(var(--todo-primary-rgb, 16, 185, 129), 0.04);
	}
	&.current {
		background: rgba(var(--todo-primary-rgb, 16, 185, 129), 0.08);
		.month-label {
			color: var(--todo-primary, #10b981);
		}
	}
}
.month-label {
	font-weight: 700;
	font-size: 0.88rem;
	color: var(--grey-7, #334155);
	min-width: 2.5rem;
}
.month-bar-wrap {
	flex: 1;
	height: 6px;
	background: rgba(var(--todo-primary-rgb, 16, 185, 129), 0.08);
	border-radius: 3px;
	overflow: hidden;
}
.month-bar {
	height: 100%;
	background: linear-gradient(90deg, var(--todo-primary, #10b981), var(--todo-primary-light, #34d399));
	border-radius: 3px;
	transition: width 0.4s ease;
	min-width: 0;
}
.month-count {
	font-size: 0.78rem;
	color: var(--grey-5, #94a3b8);
	min-width: 1.5rem;
	text-align: right;
}
.expand-icon {
	font-size: 0.7rem;
	color: var(--grey-5, #94a3b8);
	width: 1rem;
	text-align: center;
}
.month-expand {
	padding: 0.4rem 0.6rem 0.6rem;
}

// ==================== 弹窗 ====================
.modal-glass {
	background: var(--glass-bg-heavy, rgba(255, 255, 255, 0.75));
	backdrop-filter: blur(24px);
	-webkit-backdrop-filter: blur(24px);
	border: 1.5px solid var(--glass-border, rgba(255, 255, 255, 0.5));
	border-radius: 22px;
	padding: 2rem;
	width: 460px;
	max-width: calc(100vw - 2rem);
	margin: 0 auto;
	box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1),
		0 4px 16px rgba(var(--todo-primary-rgb, 16, 185, 129), 0.08);

	:deep(.n-input:not(.n-input--textarea)) {
		border-radius: 50px !important;
		--n-color: var(--glass-bg, rgba(255, 255, 255, 0.45)) !important;
		--n-color-focus: var(--glass-bg, rgba(255, 255, 255, 0.55)) !important;
		--n-border: 1.5px solid rgba(var(--todo-primary-rgb, 16, 185, 129), 0.15) !important;
		--n-border-hover: 1.5px solid var(--todo-primary, #10b981) !important;
		--n-border-focus: 1.5px solid var(--todo-primary, #10b981) !important;
		--n-caret-color: var(--todo-primary, #10b981) !important;
		.n-input__border,
		.n-input__state-border,
		.n-input-wrapper {
			border-radius: 50px !important;
		}
	}

	:deep(.n-input--textarea) {
		border-radius: 12px !important;
		--n-color: var(--glass-bg, rgba(255, 255, 255, 0.45)) !important;
		--n-color-focus: var(--glass-bg, rgba(255, 255, 255, 0.55)) !important;
		--n-border: 1.5px solid rgba(var(--todo-primary-rgb, 16, 185, 129), 0.15) !important;
		--n-border-hover: 1.5px solid var(--todo-primary, #10b981) !important;
		--n-border-focus: 1.5px solid var(--todo-primary, #10b981) !important;
		--n-caret-color: var(--todo-primary, #10b981) !important;
		.n-input__border,
		.n-input__state-border,
		.n-input-wrapper {
			border-radius: 12px !important;
		}
	}
}
.modal-title {
	font-size: 1.15rem;
	font-weight: 700;
	margin-bottom: 1.5rem;
	color: var(--grey-8, #1e293b);
}
.form-group {
	margin-bottom: 1rem;
}
.form-label {
	display: block;
	font-size: 0.78rem;
	font-weight: 600;
	color: var(--grey-6, #64748b);
	margin-bottom: 0.35rem;
	&.required::after {
		content: " *";
		color: #ef4444;
	}
}
.priority-select {
	display: flex;
	gap: 0.4rem;
}
.priority-option {
	flex: 1;
	padding: 0.4rem;
	border-radius: 10px;
	border: 1.5px solid rgba(var(--todo-primary-rgb, 16, 185, 129), 0.12);
	background: transparent;
	font-size: 0.82rem;
	font-weight: 600;
	cursor: pointer;
	font-family: inherit;
	transition: all 0.2s;
	color: var(--grey-6, #64748b);

	&.active.p-0 {
		border-color: #94a3b8;
		background: rgba(148, 163, 184, 0.1);
		color: #64748b;
	}
	&.active.p-1 {
		border-color: #f59e0b;
		background: rgba(245, 158, 11, 0.1);
		color: #d97706;
	}
	&.active.p-2 {
		border-color: #ef4444;
		background: rgba(239, 68, 68, 0.1);
		color: #ef4444;
	}
}
.modal-footer {
	display: flex;
	justify-content: flex-end;
	gap: 0.6rem;
	margin-top: 1.5rem;
}

// ==================== Naive UI 搜索框 ====================
.tp-section-header {
	:deep(.n-input) {
		--n-color: var(--glass-bg, rgba(255, 255, 255, 0.45)) !important;
		--n-color-focus: var(--glass-bg, rgba(255, 255, 255, 0.55)) !important;
		--n-border: 1.5px solid rgba(var(--todo-primary-rgb, 16, 185, 129), 0.15) !important;
		--n-border-hover: 1.5px solid rgba(var(--todo-primary-rgb, 16, 185, 129), 0.4) !important;
		--n-border-focus: 1.5px solid var(--todo-primary, #10b981) !important;
		--n-caret-color: var(--todo-primary, #10b981) !important;
		backdrop-filter: blur(12px);
		-webkit-backdrop-filter: blur(12px);
	}
}

// ==================== 响应式 ====================
@media (max-width: 767px) {
	.tp-container {
		gap: 0.75rem;
	}
	.tp-section {
		padding: 0.85rem;
		border-radius: 12px;
	}
	.tp-section-header {
		flex-direction: column;
		align-items: stretch;
		gap: 0.5rem;
	}
	.tp-action-row {
		justify-content: space-between;
	}
	.tp-card-grid {
		grid-template-columns: 1fr;
		max-height: 280px;
	}
	.tp-view-header {
		flex-direction: column;
		align-items: stretch;
		gap: 0.5rem;
	}
	.view-tabs {
		justify-content: center;
	}
	.view-nav {
		justify-content: center;
	}
	.view-date-label {
		font-size: 0.82rem;
		min-width: 100px;
	}
	.tp-task-row .task-row-actions {
		opacity: 1;
	}
	.task-row-actions {
		gap: 0;
	}
	.modal-glass {
		padding: 1.5rem;
		border-radius: 16px;
	}
	.tp-week-zone {
		padding: 0.5rem;
	}
}

@media (max-width: 480px) {
	.tp-card-grid {
		max-height: 220px;
	}
	.tp-card {
		padding: 0.65rem;
	}
	.card-title {
		font-size: 0.82rem;
	}
	.view-date-label {
		font-size: 0.78rem;
		min-width: 80px;
	}
	.tp-btn-primary {
		padding: 0.4rem 0.8rem;
		font-size: 0.78rem;
	}
}

@media (min-width: 768px) and (max-width: 1024px) {
	.tp-card-grid {
		grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
	}
}
</style>
