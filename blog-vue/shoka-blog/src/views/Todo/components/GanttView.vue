<template>
	<div class="gantt-container">
		<div class="gantt-toolbar">
			<n-button-group>
				<n-button text @click="shiftRange(-1)">
					<svg-icon icon-class="angle-left" size="1rem" />
				</n-button>
				<span class="gantt-range-label">{{ rangeLabel }}</span>
				<n-button text @click="shiftRange(1)">
					<svg-icon icon-class="angle-right" size="1rem" />
				</n-button>
			</n-button-group>
			<n-select v-model:value="rangeDays" :options="rangeOptions" style="width: 100px" size="small" @update:value="loadGantt" />
		</div>

		<div v-if="loading" class="loading-tip">加载中...</div>
		<div v-else-if="ganttRows.length === 0" class="empty-tip">该时段无代办事项</div>
		<div v-else class="gantt-chart">
			<!-- 日期头 -->
			<div class="gantt-header">
				<div class="gantt-label-col">事项</div>
				<div class="gantt-timeline">
					<div v-for="d in dateCols" :key="d" class="gantt-date-col" :class="{ today: d === todayStr }">
						{{ d.substring(5) }}
					</div>
				</div>
			</div>
			<!-- 行 -->
			<div v-for="row in ganttRows" :key="row.id" class="gantt-row">
				<div class="gantt-label-col" :title="row.title">
					<span class="priority-dot" :class="'p' + row.priority"></span>
					<span class="row-title" :class="{ done: row.status === 1 }">{{ row.title }}</span>
				</div>
				<div class="gantt-timeline">
					<div v-for="d in dateCols" :key="d" class="gantt-cell">
						<div v-if="isInRange(row, d)" class="gantt-bar" :class="barClass(row)"></div>
					</div>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { getCalendarData } from "@/api/todo";
import type { TodoItem } from "@/api/todo/types";

const loading = ref(false);
const todos = ref<TodoItem[]>([]);
const rangeDays = ref(14);
const startDate = ref(new Date());

const rangeOptions = [
	{ label: "7 天", value: 7 },
	{ label: "14 天", value: 14 },
	{ label: "30 天", value: 30 },
];

const todayStr = computed(() => fmtDate(new Date()));
const fmtDate = (d: Date) => {
	const y = d.getFullYear();
	const m = String(d.getMonth() + 1).padStart(2, "0");
	const day = String(d.getDate()).padStart(2, "0");
	return `${y}-${m}-${day}`;
};

const dateCols = computed(() => {
	const cols: string[] = [];
	const d = new Date(startDate.value);
	for (let i = 0; i < rangeDays.value; i++) {
		cols.push(fmtDate(d));
		d.setDate(d.getDate() + 1);
	}
	return cols;
});

const rangeLabel = computed(() => {
	const s = dateCols.value[0];
	const e = dateCols.value[dateCols.value.length - 1];
	return `${s.substring(5)} ~ ${e.substring(5)}`;
});

interface GanttRow {
	id: number; title: string; priority: number; status: number;
	startStr: string; endStr: string;
}

const ganttRows = computed<GanttRow[]>(() => {
	return todos.value
		.filter((t) => t.startTime || t.endTime)
		.map((t) => ({
			id: t.id,
			title: t.title,
			priority: t.priority,
			status: t.status,
			startStr: (t.startTime || t.createTime).substring(0, 10),
			endStr: (t.endTime || t.startTime || t.createTime).substring(0, 10),
		}))
		.sort((a, b) => a.startStr.localeCompare(b.startStr));
});

const isInRange = (row: GanttRow, dateStr: string) => dateStr >= row.startStr && dateStr <= row.endStr;
const barClass = (row: GanttRow) => ({
	done: row.status === 1,
	"p-high": row.priority === 2,
	"p-mid": row.priority === 1,
	"p-low": row.priority === 0,
});

const shiftRange = (dir: number) => {
	const d = new Date(startDate.value);
	d.setDate(d.getDate() + dir * rangeDays.value);
	startDate.value = d;
};

const loadGantt = () => {
	loading.value = true;
	const endD = new Date(startDate.value);
	endD.setDate(endD.getDate() + rangeDays.value - 1);
	getCalendarData(fmtDate(startDate.value) + " 00:00:00", fmtDate(endD) + " 23:59:59")
		.then(({ data }) => {
			if (data.flag) todos.value = data.data || [];
		})
		.finally(() => (loading.value = false));
};

watch([startDate, rangeDays], loadGantt, { immediate: true });
</script>

<style lang="scss" scoped>
$card: rgba(255, 255, 255, 0.65);
$card-border: rgba(255, 255, 255, 0.5);

.gantt-container { color: var(--grey-7, #1e293b); }
.gantt-toolbar {
	display: flex; justify-content: space-between; align-items: center;
	margin-bottom: 1.25rem; gap: 0.5rem;

	:deep(.n-base-selection) {
		border-radius: 50px !important;
		--n-border: 1.5px solid rgba(var(--todo-primary-rgb, 99,102,241), 0.15) !important;
		--n-border-hover: 1.5px solid rgba(var(--todo-primary-rgb, 99,102,241), 0.4) !important;
		--n-border-focus: 1.5px solid var(--todo-primary, #6366f1) !important;
		--n-border-active: 1.5px solid var(--todo-primary, #6366f1) !important;
		--n-color: var(--glass-bg, rgba(255, 255, 255, 0.45)) !important;
		--n-color-active: var(--glass-bg, rgba(255, 255, 255, 0.55)) !important;
		--n-color-focus: var(--glass-bg, rgba(255, 255, 255, 0.55)) !important;
		backdrop-filter: blur(12px);
		-webkit-backdrop-filter: blur(12px);
		background: transparent !important;
		.n-base-selection-label, .n-base-selection-tags { border-radius: 50px !important; background: transparent !important; }
		.n-base-selection__border, .n-base-selection__state-border { border-radius: 50px !important; }
	}
}
.gantt-range-label { font-size: 0.9rem; font-weight: 600; min-width: 8rem; text-align: center; color: var(--grey-8, #1e293b); }
.loading-tip, .empty-tip { text-align: center; padding: 3rem 0; color: var(--grey-5, #94a3b8); }
.gantt-chart {
	overflow-x: auto; border-radius: 16px;
	background: var(--glass-bg); backdrop-filter: blur(16px); -webkit-backdrop-filter: blur(16px);
	border: 1.5px solid var(--glass-border); box-shadow: var(--glass-shadow);
}
.gantt-header, .gantt-row { display: flex; min-width: fit-content; }
.gantt-header {
	font-size: 0.7rem; font-weight: 700; color: var(--grey-6, #64748b);
	border-bottom: 1px solid rgba(var(--todo-primary-rgb, 99,102,241), 0.08);
	position: sticky; top: 0;
	background: var(--glass-bg); backdrop-filter: blur(12px);
	z-index: 1; letter-spacing: 0.3px;
}
.gantt-label-col {
	width: 140px; min-width: 140px; padding: 0.4rem 0.5rem;
	display: flex; align-items: center; gap: 4px;
	overflow: hidden; white-space: nowrap; text-overflow: ellipsis;
	border-right: 1px solid rgba(var(--todo-primary-rgb, 99,102,241), 0.06); font-size: 0.8rem;
}
.row-title {
	overflow: hidden; white-space: nowrap; text-overflow: ellipsis;
	&.done { text-decoration: line-through; opacity: 0.5; }
}
.gantt-timeline { display: flex; flex: 1; }
.gantt-date-col, .gantt-cell {
	width: 40px; min-width: 40px; text-align: center; padding: 0.3rem 0;
	border-right: 1px solid rgba(0, 0, 0, 0.03);
}
.gantt-date-col.today { background: rgba(var(--todo-primary-rgb, 99,102,241), 0.08); color: var(--todo-primary, #6366f1); font-weight: 700; }
.gantt-row {
	border-bottom: 1px solid rgba(0, 0, 0, 0.03);
	transition: background 0.2s;
	&:hover { background: rgba(var(--todo-primary-rgb, 99,102,241), 0.08); }
}
.gantt-cell {
	display: flex; align-items: center; justify-content: center; padding: 0.35rem 1px;
}
.gantt-bar {
	width: 100%; height: 10px; border-radius: 5px;
	&.p-high { background: linear-gradient(90deg, #ef4444, #f87171); }
	&.p-mid { background: linear-gradient(90deg, #f59e0b, #fbbf24); }
	&.p-low { background: linear-gradient(90deg, #22c55e, #4ade80); }
	&.done { opacity: 0.35; }
}
.priority-dot {
	width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0;
	&.p0 { background: #22c55e; }
	&.p1 { background: #f59e0b; }
	&.p2 { background: #ef4444; }
}
</style>
