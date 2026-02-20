<template>
	<div class="calendar-container">
		<!-- 切换视图 -->
		<div class="cal-header">
			<div class="cal-nav">
				<n-button text @click="prev">
					<svg-icon icon-class="angle-left" size="1.2rem" />
				</n-button>
				<span class="cal-title">{{ headerTitle }}</span>
				<n-button text @click="next">
					<svg-icon icon-class="angle-right" size="1.2rem" />
				</n-button>
			</div>
			<n-button-group>
				<n-button :type="viewMode === 'month' ? 'primary' : 'default'" size="small" @click="viewMode = 'month'">月</n-button>
				<n-button :type="viewMode === 'week' ? 'primary' : 'default'" size="small" @click="viewMode = 'week'">周</n-button>
				<n-button :type="viewMode === 'day' ? 'primary' : 'default'" size="small" @click="viewMode = 'day'">日</n-button>
			</n-button-group>
		</div>

		<!-- 月视图 -->
		<div v-if="viewMode === 'month'" class="month-scroll-wrapper">
		<div class="month-grid">
			<div class="weekday-header" v-for="d in weekDays" :key="d">{{ d }}</div>
			<div
				v-for="(cell, idx) in monthCells"
				:key="idx"
				class="month-cell"
				:class="{ today: cell.isToday, other: cell.isOtherMonth }"
			>
				<div class="cell-date">{{ cell.day }}</div>
				<div class="cell-todos">
					<div
						v-for="t in cell.todos.slice(0, 3)"
						:key="t.id"
						class="cell-todo-item"
						:class="{ done: t.status === 1, 'high-p': t.priority === 2 }"
					>
						{{ t.title }}
					</div>
					<div v-if="cell.todos.length > 3" class="cell-more">+{{ cell.todos.length - 3 }} 更多</div>
				</div>
			</div>
		</div>
		</div>

		<!-- 周视图 -->
		<div v-if="viewMode === 'week'" class="week-scroll-wrapper">
		<div class="week-grid">
			<div v-for="(cell, idx) in weekCells" :key="idx" class="week-cell">
				<div class="week-cell-header" :class="{ today: cell.isToday }">
					<span class="week-day-name">{{ weekDays[idx] }}</span>
					<span class="week-day-num">{{ cell.day }}</span>
				</div>
				<div class="week-cell-body">
					<div
						v-for="t in cell.todos"
						:key="t.id"
						class="week-todo-item"
						:class="{ done: t.status === 1 }"
					>
						<span class="priority-dot" :class="'p' + t.priority"></span>
						{{ t.title }}
					</div>
					<div v-if="cell.todos.length === 0" class="week-empty">无</div>
				</div>
			</div>
		</div>
		</div>

		<!-- 日视图 -->
		<div v-if="viewMode === 'day'" class="day-view">
			<div class="day-header">{{ dayTitle }}</div>
			<div v-if="dayTodos.length === 0" class="day-empty">今日无代办</div>
			<div v-for="t in dayTodos" :key="t.id" class="day-todo-item">
				<span class="priority-dot" :class="'p' + t.priority"></span>
				<div class="day-todo-info">
					<div class="day-todo-title" :class="{ done: t.status === 1 }">{{ t.title }}</div>
					<div v-if="t.description" class="day-todo-desc">{{ t.description }}</div>
					<div class="day-todo-time" v-if="t.startTime || t.endTime">
						{{ formatRange(t.startTime, t.endTime) }}
					</div>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { getCalendarData } from "@/api/todo";
import type { TodoItem } from "@/api/todo/types";

const viewMode = ref<"month" | "week" | "day">("month");
const currentDate = ref(new Date());
const todos = ref<TodoItem[]>([]);
const weekDays = ["日", "一", "二", "三", "四", "五", "六"];

const headerTitle = computed(() => {
	const d = currentDate.value;
	if (viewMode.value === "month") return `${d.getFullYear()} 年 ${d.getMonth() + 1} 月`;
	if (viewMode.value === "week") {
		const start = getWeekStart(d);
		const end = new Date(start);
		end.setDate(end.getDate() + 6);
		return `${fmt(start)} ~ ${fmt(end)}`;
	}
	return fmt(d);
});

const dayTitle = computed(() => {
	const d = currentDate.value;
	return `${d.getFullYear()} 年 ${d.getMonth() + 1} 月 ${d.getDate()} 日 ${weekDays[d.getDay()]}`;
});

const fmt = (d: Date) => `${d.getMonth() + 1}/${d.getDate()}`;
const fmtFull = (d: Date) => {
	const y = d.getFullYear();
	const m = String(d.getMonth() + 1).padStart(2, "0");
	const day = String(d.getDate()).padStart(2, "0");
	return `${y}-${m}-${day}`;
};
const isToday = (d: Date) => fmtFull(d) === fmtFull(new Date());

const getWeekStart = (d: Date) => {
	const r = new Date(d);
	r.setDate(r.getDate() - r.getDay());
	return r;
};

const todosOnDate = (dateStr: string) => {
	return todos.value.filter((t) => {
		const s = t.startTime ? t.startTime.substring(0, 10) : t.createTime.substring(0, 10);
		const e = t.endTime ? t.endTime.substring(0, 10) : s;
		return dateStr >= s && dateStr <= e;
	});
};

interface CalCell { day: number; date: Date; dateStr: string; isToday: boolean; isOtherMonth: boolean; todos: TodoItem[] }

const monthCells = computed<CalCell[]>(() => {
	const d = currentDate.value;
	const year = d.getFullYear();
	const month = d.getMonth();
	const firstDay = new Date(year, month, 1);
	const startOffset = firstDay.getDay();
	const cells: CalCell[] = [];
	for (let i = 0; i < 42; i++) {
		const date = new Date(year, month, 1 - startOffset + i);
		const dateStr = fmtFull(date);
		cells.push({
			day: date.getDate(),
			date,
			dateStr,
			isToday: isToday(date),
			isOtherMonth: date.getMonth() !== month,
			todos: todosOnDate(dateStr),
		});
	}
	return cells;
});

const weekCells = computed<CalCell[]>(() => {
	const start = getWeekStart(currentDate.value);
	const cells: CalCell[] = [];
	for (let i = 0; i < 7; i++) {
		const date = new Date(start);
		date.setDate(start.getDate() + i);
		const dateStr = fmtFull(date);
		cells.push({ day: date.getDate(), date, dateStr, isToday: isToday(date), isOtherMonth: false, todos: todosOnDate(dateStr) });
	}
	return cells;
});

const dayTodos = computed(() => todosOnDate(fmtFull(currentDate.value)));

const formatRange = (s: string, e: string) => {
	const parts: string[] = [];
	if (s) parts.push(s.substring(0, 16).replace("T", " "));
	if (e) parts.push(e.substring(0, 16).replace("T", " "));
	return parts.join(" ~ ");
};

const prev = () => {
	const d = new Date(currentDate.value);
	if (viewMode.value === "month") d.setMonth(d.getMonth() - 1);
	else if (viewMode.value === "week") d.setDate(d.getDate() - 7);
	else d.setDate(d.getDate() - 1);
	currentDate.value = d;
};
const next = () => {
	const d = new Date(currentDate.value);
	if (viewMode.value === "month") d.setMonth(d.getMonth() + 1);
	else if (viewMode.value === "week") d.setDate(d.getDate() + 7);
	else d.setDate(d.getDate() + 1);
	currentDate.value = d;
};

const loadCalendar = () => {
	const d = currentDate.value;
	let start: Date, end: Date;
	if (viewMode.value === "month") {
		start = new Date(d.getFullYear(), d.getMonth(), 1);
		start.setDate(start.getDate() - start.getDay());
		end = new Date(start);
		end.setDate(end.getDate() + 41);
	} else if (viewMode.value === "week") {
		start = getWeekStart(d);
		end = new Date(start);
		end.setDate(end.getDate() + 6);
	} else {
		start = new Date(d);
		end = new Date(d);
	}
	getCalendarData(fmtFull(start) + " 00:00:00", fmtFull(end) + " 23:59:59").then(({ data }) => {
		if (data.flag) todos.value = data.data || [];
	});
};

watch([viewMode, currentDate], loadCalendar, { immediate: true });
</script>

<style lang="scss" scoped>
.calendar-container {
	color: var(--grey-7, #333);
}
.cal-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 1rem;
	flex-wrap: wrap;
	gap: 0.5rem;
}
.cal-nav {
	display: flex;
	align-items: center;
	gap: 0.75rem;
}
.cal-title {
	font-size: 1.1rem;
	font-weight: 600;
	min-width: 10rem;
	text-align: center;
}

/* 月视图 */
.month-scroll-wrapper {
	overflow-x: auto;
	-webkit-overflow-scrolling: touch;
	border-radius: 0.5rem;
}
.month-grid {
	display: grid;
	grid-template-columns: repeat(7, 1fr);
	min-width: 500px;
	gap: 1px;
	background: var(--grey-3, #eee);
	border-radius: 0.5rem;
	overflow: hidden;
}
.weekday-header {
	text-align: center;
	padding: 0.4rem;
	font-size: 0.8rem;
	font-weight: 600;
	background: var(--card-bg, #fff);
	color: var(--grey-6);
}
.month-cell {
	min-height: 5rem;
	padding: 0.25rem;
	background: var(--card-bg, #fff);
	&.today { background: rgba(64, 158, 255, 0.08); }
	&.other { opacity: 0.4; }
}
.cell-date {
	font-size: 0.75rem;
	font-weight: 500;
	margin-bottom: 2px;
	color: var(--grey-7);
}
.cell-todo-item {
	font-size: 0.65rem;
	padding: 1px 3px;
	margin-bottom: 1px;
	border-radius: 2px;
	background: rgba(64, 158, 255, 0.15);
	color: #409eff;
	overflow: hidden;
	white-space: nowrap;
	text-overflow: ellipsis;
	&.done { opacity: 0.5; text-decoration: line-through; }
	&.high-p { background: rgba(244, 67, 54, 0.15); color: #f44336; }
}
.cell-more {
	font-size: 0.6rem;
	color: var(--grey-5);
}

/* 周视图 */
.week-scroll-wrapper {
	overflow-x: auto;
	-webkit-overflow-scrolling: touch;
}
.week-grid {
	display: grid;
	grid-template-columns: repeat(7, 1fr);
	min-width: 560px;
	gap: 0.5rem;
}
.week-cell {
	border-radius: 0.5rem;
	background: var(--card-bg, #fff);
	box-shadow: var(--card-shadow);
	overflow: hidden;
}
.week-cell-header {
	text-align: center;
	padding: 0.4rem;
	font-size: 0.8rem;
	background: var(--grey-2);
	color: var(--grey-7);
	&.today { background: var(--primary-color); color: #fff; }
}
.week-day-name { margin-right: 4px; }
.week-cell-body { padding: 0.4rem; min-height: 4rem; }
.week-todo-item {
	display: flex;
	align-items: center;
	gap: 4px;
	font-size: 0.75rem;
	padding: 2px 0;
	&.done { text-decoration: line-through; opacity: 0.5; }
}
.week-empty { font-size: 0.7rem; color: var(--grey-4); text-align: center; padding: 1rem 0; }

/* 日视图 */
.day-view { max-width: 600px; margin: 0 auto; }
.day-header { font-size: 1.1rem; font-weight: 600; margin-bottom: 1rem; text-align: center; }
.day-empty { text-align: center; padding: 3rem 0; color: var(--grey-5); }
.day-todo-item {
	display: flex;
	gap: 0.75rem;
	padding: 0.75rem 1rem;
	margin-bottom: 0.5rem;
	border-radius: 0.5rem;
	background: var(--card-bg, #fff);
	box-shadow: var(--card-shadow);
	color: var(--grey-7, #333);
}
.day-todo-title { font-weight: 500; &.done { text-decoration: line-through; color: var(--grey-5); } }
.day-todo-desc { font-size: 0.8rem; color: var(--grey-5); margin-top: 0.25rem; }
.day-todo-time { font-size: 0.75rem; color: var(--grey-4); margin-top: 0.25rem; }

.priority-dot {
	width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; margin-top: 2px;
	&.p0 { background: #8bc34a; }
	&.p1 { background: #ff9800; }
	&.p2 { background: #f44336; }
}

@media (max-width: 767px) {
	.cal-title { font-size: 0.9rem; min-width: auto; }
	.month-cell { min-height: 3.5rem; }
	.cell-todo-item { font-size: 0.55rem; }
	.cell-date { font-size: 0.65rem; }
}
</style>
