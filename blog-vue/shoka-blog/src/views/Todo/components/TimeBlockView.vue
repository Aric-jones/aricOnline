<template>
	<div class="tb-container">
		<!-- 顶部日期导航 -->
		<div class="tb-date-nav">
			<div class="nav-left">
				<n-button text @click="changeDate(-1)" class="nav-btn">
					<svg-icon icon-class="angle-left" size="1.2rem" />
				</n-button>
				<div class="date-display" @click="showDatePicker = !showDatePicker">
					<span class="date-main">{{ dateMain }}</span>
					<span class="date-sub">{{ dateSub }}</span>
				</div>
				<n-button text @click="changeDate(1)" class="nav-btn">
					<svg-icon icon-class="angle-right" size="1.2rem" />
				</n-button>
				<n-button text @click="goToday" class="today-btn" v-if="selectedDate !== todayStr">今天</n-button>
			</div>
			<div class="nav-right">
				<n-date-picker
					v-model:formatted-value="selectedDate"
					type="date"
					value-format="yyyy-MM-dd"
					size="small"
					style="width: 140px"
				/>
				<n-button type="primary" size="small" @click="openAdd()">
					<template #icon><svg-icon icon-class="edit" size="0.85rem" /></template>
					新增
				</n-button>
				<n-button size="small" @click="exportData">导出</n-button>
			</div>
		</div>

		<div class="tb-main">
			<!-- 左侧：时间轴 -->
			<div class="tb-timeline-wrap">
				<div class="tb-timeline" ref="timelineRef">
					<div
						v-for="hour in 24"
						:key="hour - 1"
						class="hour-row"
						@click="clickHourRow(hour - 1)"
					>
						<span class="hour-label">{{ String(hour - 1).padStart(2, '0') }}:00</span>
						<div class="hour-line"></div>
					</div>
					<!-- 当前时间指示线 -->
					<div
						v-if="selectedDate === todayStr"
						class="now-line"
						:style="{ top: nowLineTop + 'px' }"
					>
						<span class="now-dot"></span>
						<span class="now-time">{{ nowTimeStr }}</span>
					</div>
					<!-- 事件块 -->
					<div
						v-for="block in blocks"
						:key="block.id"
						class="event-block"
						:style="getBlockStyle(block)"
						@click.stop="openEdit(block)"
					>
						<div class="event-inner">
							<span class="event-name">{{ block.name }}</span>
							<span class="event-time">{{ block.startTime }} - {{ block.endTime }}</span>
						</div>
						<n-button
							text size="tiny"
							class="event-delete"
							@click.stop="handleDelete(block.id)"
						>
							<svg-icon icon-class="delete" size="0.75rem" />
						</n-button>
					</div>
				</div>
			</div>

			<!-- 右侧面板 -->
			<div class="tb-sidebar">
				<!-- 快捷事件 -->
				<div class="sidebar-section">
					<h4 class="section-title">📋 常用事件</h4>
					<div v-if="distinctEvents.length === 0" class="empty-hint">暂无历史事件</div>
					<div class="event-chips">
						<div
							v-for="ev in distinctEvents"
							:key="ev.name"
							class="event-chip"
							:style="{ borderColor: ev.color, color: ev.color }"
							@click="quickAdd(ev)"
						>
							<span class="chip-dot" :style="{ background: ev.color }"></span>
							{{ ev.name }}
						</div>
					</div>
				</div>

				<!-- 统计饼图 -->
				<div class="sidebar-section">
					<h4 class="section-title">📊 时间分布</h4>
					<div class="stats-tabs">
						<span
							v-for="t in statsTabs"
							:key="t.value"
							class="stats-tab"
							:class="{ active: statsType === t.value }"
							@click="statsType = t.value"
						>{{ t.label }}</span>
					</div>
					<div v-if="statsData.length > 0" class="chart-wrap">
						<Echarts ref="chartRef" :options="pieOptions" height="260px" />
					</div>
					<div v-else class="empty-hint">暂无数据</div>
				</div>
			</div>
		</div>

		<!-- 新增/编辑弹窗 -->
		<n-modal
			v-model:show="showModal"
			preset="card"
			:title="isEdit ? '编辑时间块' : '新增时间块'"
			style="max-width: 480px"
			:mask-closable="false"
		>
			<div class="form-group">
				<label class="form-label required">名称</label>
				<n-input v-model:value="form.name" placeholder="做了什么事" maxlength="100" />
			</div>
			<div class="form-group">
				<label class="form-label required">分类</label>
				<n-input v-model:value="form.category" placeholder="如：工作、学习、运动、休息" maxlength="50" />
			</div>
			<div class="form-row">
				<div class="form-group flex-1">
					<label class="form-label required">开始时间</label>
					<n-time-picker
						v-model:formatted-value="form.startTime"
						format="HH:mm"
						value-format="HH:mm"
						:hours="hourRange"
						:minutes="minuteRange"
						style="width: 100%"
					/>
				</div>
				<div class="form-group flex-1">
					<label class="form-label required">结束时间</label>
					<n-time-picker
						v-model:formatted-value="form.endTime"
						format="HH:mm"
						value-format="HH:mm"
						:hours="hourRange"
						:minutes="minuteRange"
						style="width: 100%"
					/>
				</div>
			</div>
			<div class="form-group">
				<label class="form-label">颜色</label>
				<div class="color-picker">
					<span
						v-for="c in presetColors"
						:key="c"
						class="color-swatch"
						:class="{ active: form.color === c }"
						:style="{ background: c }"
						@click="form.color = c"
					></span>
					<n-color-picker
						v-model:value="form.color"
						:modes="['hex']"
						size="small"
						style="width: 80px"
					/>
				</div>
			</div>
			<div class="form-group">
				<label class="form-label">备注</label>
				<n-input v-model:value="form.remark" type="textarea" placeholder="补充说明（可选）" :rows="2" maxlength="500" />
			</div>
			<template #action>
				<n-button @click="showModal = false">取消</n-button>
				<n-button type="primary" :loading="submitting" @click="handleSubmit">
					{{ isEdit ? '保存' : '新增' }}
				</n-button>
			</template>
		</n-modal>
	</div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from "vue";
import {
	getTimeBlocks, addTimeBlock, updateTimeBlock, deleteTimeBlock,
	getDistinctEvents as fetchDistinctEvents, getTimeBlockStats
} from "@/api/todo";
import type { TimeBlockItem, TimeBlockReq, DistinctEvent, CategoryStat } from "@/api/todo/types";
import Echarts from "@/components/Echarts/index.vue";

const HOUR_HEIGHT = 80;
const todayStr = computed(() => {
	const d = new Date();
	return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
});
const selectedDate = ref(todayStr.value);
const blocks = ref<TimeBlockItem[]>([]);
const distinctEvents = ref<DistinctEvent[]>([]);
const statsData = ref<CategoryStat[]>([]);
const statsType = ref("daily");
const showModal = ref(false);
const isEdit = ref(false);
const submitting = ref(false);
const timelineRef = ref<HTMLElement>();

const statsTabs = [
	{ label: "今日", value: "daily" },
	{ label: "本周", value: "weekly" },
	{ label: "本月", value: "monthly" },
	{ label: "今年", value: "yearly" },
];

const presetColors = [
	"#10b981", "#3b82f6", "#8b5cf6", "#f59e0b",
	"#ef4444", "#ec4899", "#06b6d4", "#84cc16",
	"#f97316", "#6366f1", "#14b8a6", "#a855f7",
];

const hourRange = Array.from({ length: 24 }, (_, i) => i);
const minuteRange = [0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55];

function getNowHHmm(): string {
	const d = new Date();
	const h = String(d.getHours()).padStart(2, '0');
	const m = String(Math.floor(d.getMinutes() / 5) * 5).padStart(2, '0');
	return `${h}:${m}`;
}
function addOneHour(hhmm: string): string {
	const [h, m] = hhmm.split(":").map(Number);
	const nh = Math.min(h + 1, 23);
	return `${String(nh).padStart(2, '0')}:${String(m).padStart(2, '0')}`;
}
const defaultForm = (): TimeBlockReq => {
	const start = getNowHHmm();
	return {
		name: "", category: "", blockDate: selectedDate.value,
		startTime: start, endTime: addOneHour(start), remark: "", color: "#10b981",
	};
};
const form = ref<TimeBlockReq>(defaultForm());

// 日期显示
const dateMain = computed(() => {
	const d = new Date(selectedDate.value + "T00:00:00");
	return `${d.getMonth() + 1}月${d.getDate()}日`;
});
const dateSub = computed(() => {
	const d = new Date(selectedDate.value + "T00:00:00");
	const weekDay = ["周日", "周一", "周二", "周三", "周四", "周五", "周六"];
	return weekDay[d.getDay()];
});

// 当前时间线
const nowMinutes = ref(0);
const nowTimeStr = ref("");
let nowTimer: ReturnType<typeof setInterval> | null = null;

function updateNow() {
	const d = new Date();
	nowMinutes.value = d.getHours() * 60 + d.getMinutes();
	nowTimeStr.value = `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
}
const nowLineTop = computed(() => (nowMinutes.value / 60) * HOUR_HEIGHT);

function changeDate(delta: number) {
	const d = new Date(selectedDate.value + "T00:00:00");
	d.setDate(d.getDate() + delta);
	selectedDate.value = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}
function goToday() { selectedDate.value = todayStr.value; }

// 事件块定位
function getBlockStyle(block: TimeBlockItem) {
	const [sh, sm] = block.startTime.split(":").map(Number);
	const [eh, em] = block.endTime.split(":").map(Number);
	const top = (sh * 60 + sm) / 60 * HOUR_HEIGHT;
	const height = Math.max(((eh * 60 + em) - (sh * 60 + sm)) / 60 * HOUR_HEIGHT, 24);
	return {
		top: top + "px",
		height: height + "px",
		background: block.color || "#10b981",
	};
}

// CRUD
async function loadBlocks() {
	const res = await getTimeBlocks(selectedDate.value);
	blocks.value = res.data.data || [];
}
async function loadDistinct() {
	const res = await fetchDistinctEvents();
	distinctEvents.value = (res.data.data || []) as DistinctEvent[];
}
async function loadStats() {
	const res = await getTimeBlockStats(statsType.value);
	statsData.value = (res.data.data || []) as CategoryStat[];
}

function openAdd(startHour?: number) {
	isEdit.value = false;
	form.value = defaultForm();
	form.value.blockDate = selectedDate.value;
	if (startHour !== undefined) {
		form.value.startTime = String(startHour).padStart(2, '0') + ":00";
		form.value.endTime = String(Math.min(startHour + 1, 23)).padStart(2, '0') + ":00";
	}
	showModal.value = true;
}

function openEdit(block: TimeBlockItem) {
	isEdit.value = true;
	form.value = {
		id: block.id, name: block.name, category: block.category,
		blockDate: block.blockDate, startTime: block.startTime,
		endTime: block.endTime, remark: block.remark, color: block.color,
	};
	showModal.value = true;
}

function quickAdd(ev: DistinctEvent) {
	isEdit.value = false;
	form.value = defaultForm();
	form.value.blockDate = selectedDate.value;
	form.value.name = ev.name;
	form.value.category = ev.category;
	form.value.color = ev.color;
	showModal.value = true;
}

function clickHourRow(hour: number) {
	const hStr = String(hour).padStart(2, '0') + ":00";
	const hEnd = String(Math.min(hour + 1, 23)).padStart(2, '0') + ":00";
	const existing = blocks.value.find(b => hStr < b.endTime && hEnd > b.startTime);
	if (existing) {
		openEdit(existing);
	} else {
		openAdd(hour);
	}
}

function hasOverlap(startTime: string, endTime: string, excludeId?: number): boolean {
	return blocks.value.some(b => {
		if (excludeId && b.id === excludeId) return false;
		return startTime < b.endTime && endTime > b.startTime;
	});
}

async function handleSubmit() {
	if (!form.value.name.trim()) { window.$message?.warning("名称不能为空"); return; }
	if (!form.value.category.trim()) { window.$message?.warning("分类不能为空"); return; }
	if (!form.value.startTime || !form.value.endTime) { window.$message?.warning("请选择时间"); return; }
	if (form.value.startTime >= form.value.endTime) { window.$message?.warning("结束时间必须晚于开始时间"); return; }
	if (hasOverlap(form.value.startTime, form.value.endTime, form.value.id)) {
		window.$message?.warning("该时间段已有事件，不能重复添加"); return;
	}
	submitting.value = true;
	try {
		if (isEdit.value) {
			await updateTimeBlock(form.value);
			window.$message?.success("修改成功");
		} else {
			await addTimeBlock(form.value);
			window.$message?.success("新增成功");
		}
		showModal.value = false;
		await Promise.all([loadBlocks(), loadDistinct(), loadStats()]);
	} finally {
		submitting.value = false;
	}
}

async function handleDelete(id: number) {
	window.$dialog?.warning({
		title: "确认删除",
		content: "确定删除这个时间块吗？",
		positiveText: "删除",
		negativeText: "取消",
		onPositiveClick: async () => {
			await deleteTimeBlock(id);
			window.$message?.success("已删除");
			await Promise.all([loadBlocks(), loadDistinct(), loadStats()]);
		},
	});
}

// 饼图配置
const pieOptions = computed(() => {
	const data = statsData.value.map(s => ({
		name: s.category,
		value: s.total_minutes,
	}));
	const total = data.reduce((sum, d) => sum + d.value, 0);
	return {
		tooltip: {
			trigger: "item",
			formatter: (p: any) => {
				const h = Math.floor(p.value / 60);
				const m = p.value % 60;
				return `${p.name}<br/>${h}小时${m}分钟 (${p.percent}%)`;
			},
		},
		legend: { bottom: 0, textStyle: { fontSize: 11 } },
		series: [{
			type: "pie",
			radius: ["40%", "70%"],
			center: ["50%", "45%"],
			avoidLabelOverlap: true,
			itemStyle: { borderRadius: 6, borderColor: "transparent", borderWidth: 2 },
			label: {
				show: true,
				formatter: (p: any) => {
					const h = Math.floor(p.value / 60);
					const m = p.value % 60;
					return h > 0 ? `${p.name}\n${h}h${m}m` : `${p.name}\n${m}m`;
				},
				fontSize: 11,
			},
			data,
		}],
	};
});

const chartRef = ref<InstanceType<typeof Echarts>>();

// 导出：当日事件表格CSV + 饼图PNG
function exportData() {
	// 导出CSV
	const rows = blocks.value.map(b => [
		b.blockDate, b.startTime, b.endTime, b.name, b.category, b.remark || ""
	]);
	const header = "日期,开始时间,结束时间,名称,分类,备注";
	const csv = "\uFEFF" + header + "\n" + rows.map(r => r.join(",")).join("\n");
	const blob = new Blob([csv], { type: "text/csv;charset=utf-8" });
	const url = URL.createObjectURL(blob);
	const a = document.createElement("a");
	a.href = url;
	a.download = `时间管理_${selectedDate.value}.csv`;
	a.click();
	URL.revokeObjectURL(url);

	// 导出饼图
	if (chartRef.value) {
		const chartDom = (chartRef.value as any).$el as HTMLElement;
		const canvas = chartDom.querySelector("canvas");
		if (canvas) {
			const imgUrl = canvas.toDataURL("image/png");
			const imgA = document.createElement("a");
			imgA.href = imgUrl;
			imgA.download = `时间分布_${statsType.value}.png`;
			imgA.click();
		}
	}
	window.$message?.success("导出成功");
}

// 自动滚动到当前时间
function scrollToNow() {
	if (timelineRef.value && selectedDate.value === todayStr.value) {
		const scrollTarget = Math.max(0, nowLineTop.value - 200);
		timelineRef.value.scrollTo({ top: scrollTarget, behavior: "smooth" });
	}
}

watch(selectedDate, () => {
	loadBlocks();
	form.value.blockDate = selectedDate.value;
});
watch(statsType, () => loadStats());

onMounted(async () => {
	updateNow();
	nowTimer = setInterval(updateNow, 60000);
	await Promise.all([loadBlocks(), loadDistinct(), loadStats()]);
	setTimeout(scrollToNow, 300);
});
onUnmounted(() => { if (nowTimer) clearInterval(nowTimer); });
</script>

<style lang="scss" scoped>
.tb-container { max-width: 1100px; margin: 0 auto; }

// ==================== 日期导航 ====================
.tb-date-nav {
	display: flex; justify-content: space-between; align-items: center;
	gap: 1rem; margin-bottom: 1rem; flex-wrap: wrap;
	.nav-left {
		display: flex; align-items: center; gap: 0.5rem;
	}
	.nav-right {
		display: flex; align-items: center; gap: 0.5rem;
	}
	.date-display {
		cursor: pointer; text-align: center; min-width: 80px;
		.date-main { font-size: 1.15rem; font-weight: 700; color: var(--grey-8, #1e293b); }
		.date-sub { margin-left: 0.3rem; font-size: 0.85rem; color: var(--grey-5, #94a3b8); }
	}
	.today-btn {
		font-size: 0.8rem; padding: 0.15rem 0.5rem;
		border-radius: 50px; background: rgba(var(--todo-primary-rgb, 16,185,129), 0.1);
		color: var(--todo-primary, #10b981);
	}
}

// ==================== 主布局 ====================
.tb-main {
	display: flex; gap: 1.25rem; align-items: flex-start;
}
.tb-timeline-wrap {
	flex: 1; min-width: 0;
	background: var(--glass-bg, rgba(255,255,255,0.6));
	backdrop-filter: blur(16px); -webkit-backdrop-filter: blur(16px);
	border: 1.5px solid var(--glass-border, rgba(255,255,255,0.4));
	border-radius: 16px; padding: 0.5rem 0;
	box-shadow: var(--todo-card-glow);
}
.tb-timeline {
	position: relative; overflow-y: auto; max-height: 70vh;
	/* 24小时 × 每行高度，修改 80px 可调整每行高度 */
	height: calc(24 * 80px);
}

// ==================== 小时行 ====================
.hour-row {
	/* 每小时行高度，与 JS 中 HOUR_HEIGHT 保持一致 */
	position: relative; height: 80px; display: flex; align-items: flex-start;
	cursor: pointer; transition: background 0.15s;
	&:hover { background: rgba(var(--todo-primary-rgb, 16,185,129), 0.04); }
}
.hour-label {
	width: 52px; flex-shrink: 0; text-align: right; padding-right: 10px;
	font-size: 0.75rem; color: var(--grey-5, #94a3b8); line-height: 1;
	padding-top: 2px; font-variant-numeric: tabular-nums;
}
.hour-line {
	flex: 1; border-top: 1px solid rgba(var(--todo-primary-rgb, 16,185,129), 0.08);
}

// ==================== 当前时间线 ====================
.now-line {
	position: absolute; left: 46px; right: 8px; height: 2px; z-index: 5;
	background: var(--todo-primary, #10b981); pointer-events: none;
	.now-dot {
		position: absolute; left: -4px; top: -3px; width: 8px; height: 8px;
		border-radius: 50%; background: var(--todo-primary, #10b981);
	}
	.now-time {
		position: absolute; left: -52px; top: -7px;
		font-size: 0.7rem; font-weight: 700;
		color: var(--todo-primary, #10b981);
	}
}

// ==================== 事件块 ====================
.event-block {
	position: absolute; left: 58px; right: 10px; z-index: 3;
	border-radius: 8px; padding: 0.3rem 0.5rem;
	color: #fff; cursor: pointer; overflow: hidden;
	transition: all 0.2s; min-height: 24px;
	display: flex; align-items: center; justify-content: space-between;
	box-shadow: 0 2px 8px rgba(0,0,0,0.12);
	&:hover {
		transform: scale(1.01); box-shadow: 0 4px 16px rgba(0,0,0,0.2);
		.event-delete { opacity: 1; }
	}
}
.event-inner {
	flex: 1; min-width: 0; display: flex; flex-direction: row; gap: 1px;justify-content: space-between;align-items: center;
	.event-name {
		font-size: 0.82rem; font-weight: 600;
		white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
	}
	.event-time { font-size: 0.7rem; opacity: 0.85; }
}
.event-delete {
	opacity: 0; transition: opacity 0.2s; color: rgba(255,255,255,0.8) !important;
	flex-shrink: 0;
}

// ==================== 右侧面板 ====================
.tb-sidebar {
	width: 280px; flex-shrink: 0;
	display: flex; flex-direction: column; gap: 1rem;
}
.sidebar-section {
	background: var(--glass-bg, rgba(255,255,255,0.6));
	backdrop-filter: blur(16px); -webkit-backdrop-filter: blur(16px);
	border: 1.5px solid var(--glass-border, rgba(255,255,255,0.4));
	border-radius: 16px; padding: 1rem;
	box-shadow: var(--todo-card-glow);
}
.section-title {
	font-size: 0.9rem; font-weight: 700; margin: 0 0 0.75rem;
	color: var(--grey-8, #1e293b);
}
.empty-hint {
	text-align: center; padding: 1rem 0; font-size: 0.82rem;
	color: var(--grey-5, #94a3b8);
}

// ==================== 常用事件 ====================
.event-chips {
	display: flex; flex-wrap: wrap; gap: 0.4rem;
	max-height: 200px; overflow-y: auto;
}
.event-chip {
	display: flex; align-items: center; gap: 0.3rem;
	padding: 0.25rem 0.6rem; border-radius: 50px;
	border: 1.5px solid; font-size: 0.78rem; font-weight: 500;
	cursor: pointer; transition: all 0.2s;
	background: transparent;
	&:hover { transform: scale(1.04); background: rgba(255, 255, 255, 0.39);}
	.chip-dot { width: 6px; height: 6px; border-radius: 50%; flex-shrink: 0; }
}

// ==================== 统计 ====================
.stats-tabs {
	display: flex; gap: 0.3rem; margin-bottom: 0.5rem;
}
.stats-tab {
	padding: 0.2rem 0.6rem; border-radius: 50px; font-size: 0.75rem;
	cursor: pointer; transition: all 0.2s; font-weight: 500;
	color: var(--grey-6, #64748b);
	&.active {
		background: var(--todo-primary, #10b981); color: #fff;
	}
	&:hover:not(.active) {
		background: rgba(var(--todo-primary-rgb, 16,185,129), 0.08);
	}
}
.chart-wrap { margin-top: 0.25rem; }

// ==================== 表单 ====================
.form-group { margin-bottom: 0.85rem; }
.form-label {
	display: block; font-weight: 600; font-size: 0.85rem;
	margin-bottom: 0.35rem; color: var(--grey-7, #334155);
	&.required::after { content: " *"; color: #ef4444; }
}
.form-row { display: flex; gap: 0.75rem; }
.flex-1 { flex: 1; }
.color-picker {
	display: flex; align-items: center; gap: 0.4rem; flex-wrap: wrap;
}
.color-swatch {
	width: 24px; height: 24px; border-radius: 50%; cursor: pointer;
	border: 2px solid transparent; transition: all 0.2s;
	&.active { border-color: var(--grey-8, #1e293b); transform: scale(1.15); }
	&:hover:not(.active) { transform: scale(1.1); }
}

// ==================== 响应式 ====================
// 注意：.hour-row 高度和 .tb-timeline 高度不能在媒体查询中修改！
// 必须和 JS 中的 HOUR_HEIGHT (80px) 保持一致，否则事件块位置会错乱。
// 移动端通过缩小字体、标签宽度和 max-height（滚动）来适配。
@media (max-width: 767px) {
	.tb-main {
		flex-direction: column;
		gap: 1rem;
	}
	.tb-timeline-wrap {
		width: 100%;
		padding: 0.3rem 0;
		border-radius: 12px;
	}
	.tb-timeline {
		max-height: 55vh;
	}
	.tb-sidebar { width: 100%; }
	.tb-date-nav {
		flex-direction: column; align-items: stretch;
		.nav-left, .nav-right { justify-content: center; }
		.date-display {
			.date-main { font-size: 1rem; }
			.date-sub { font-size: 0.75rem; }
		}
	}
	.hour-label {
		width: 42px; padding-right: 6px;
		font-size: 0.7rem;
	}
	.now-line {
		left: 38px; right: 4px;
		.now-time { left: -42px; font-size: 0.65rem; }
	}
	.event-block {
		left: 46px; right: 4px;
		padding: 0.25rem 0.4rem;
		border-radius: 6px;
	}
	.event-inner {
		.event-name { font-size: 0.75rem; }
		.event-time { font-size: 0.65rem; }
	}
	.event-delete { opacity: 0.6; }
	.event-chips { max-height: 120px; }
}
@media (max-width: 480px) {
	.tb-date-nav {
		.nav-left, .nav-right { flex-wrap: wrap; gap: 0.3rem; }
		.date-display {
			min-width: 70px;
			.date-main { font-size: 0.9rem; }
			.date-sub { font-size: 0.7rem; }
		}
	}
	.tb-timeline {
		max-height: 45vh;
	}
	.hour-label {
		width: 36px; padding-right: 4px;
		font-size: 0.65rem;
	}
	.now-line {
		left: 32px; right: 3px;
		.now-time { left: -36px; font-size: 0.6rem; }
	}
	.event-block {
		left: 40px; right: 3px;
		padding: 0.2rem 0.3rem;
	}
	.event-inner {
		.event-name { font-size: 0.7rem; }
		.event-time { font-size: 0.6rem; }
	}
}
</style>
