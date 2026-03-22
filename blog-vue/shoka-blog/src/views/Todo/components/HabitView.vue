<template>
	<div class="habit-container">
		<!-- 顶部操作栏 -->
		<div class="habit-toolbar">
			<n-button type="primary" size="small" @click="showAddHabit">+ 新建习惯</n-button>
			<n-select
				v-model:value="selectedHabitId"
				placeholder="全部习惯"
				clearable
				:options="habitOptions"
				style="width: 160px"
				@update:value="loadRecords"
			/>
			<n-button-group>
				<n-button :type="viewRange === 'week' ? 'primary' : 'default'" size="small" @click="switchRange('week')">周</n-button>
				<n-button :type="viewRange === 'month' ? 'primary' : 'default'" size="small" @click="switchRange('month')">月</n-button>
				<n-button :type="viewRange === 'year' ? 'primary' : 'default'" size="small" @click="switchRange('year')">年</n-button>
			</n-button-group>
		</div>

		<!-- 统计面板 -->
		<div class="stats-panel">
			<div class="stats-header">
				<h3 class="stats-title">{{ rangeLabel }} · 习惯统计</h3>
				<n-button text size="tiny" @click="showHabitFilter = !showHabitFilter">
					{{ showHabitFilter ? '收起筛选' : '筛选习惯' }}
				</n-button>
			</div>
			<div v-if="showHabitFilter" class="habit-filter">
				<label
					v-for="h in activeHabits"
					:key="h.id"
					class="filter-check"
					:class="{ unchecked: !visibleHabitIds.has(h.id) }"
				>
					<input type="checkbox" :checked="visibleHabitIds.has(h.id)" @change="toggleHabitVisible(h.id)" />
					<span class="filter-icon">{{ h.icon || '⭐' }}</span>
					<span class="filter-name">{{ h.name }}</span>
				</label>
			</div>
			<div class="stats-grid" v-if="visibleActiveHabits.length > 0">
				<div
					v-for="h in visibleActiveHabits"
					:key="h.id"
					class="stats-chip"
					:style="{ borderTop: `3px solid ${h.color || '#18a058'}` }"
				>
					<div class="chip-icon">{{ h.icon || '⭐' }}</div>
					<div class="chip-count">{{ getHabitCount(h.id) }}</div>
					<div class="chip-name">{{ h.name }}</div>
					<div class="chip-rate" v-if="getRangeTotal > 0">
						{{ Math.round(getHabitCount(h.id) / getRangeTotal * 100) }}%
					</div>
				</div>
			</div>
			<div v-else class="stats-empty">暂无选中的习惯</div>
		</div>

		<!-- 习惯卡片列表 -->
		<div class="habit-cards">
			<div
				v-for="h in activeHabits"
				:key="h.id"
				class="habit-card"
				:style="{ borderLeft: `4px solid ${h.color || '#18a058'}` }"
			>
				<div class="card-top">
					<span class="card-icon">{{ h.icon || '⭐' }}</span>
					<span class="card-name">{{ h.name }}</span>
					<span v-if="h.category" class="card-category">{{ h.category }}</span>
					<div class="card-actions">
						<n-button text size="tiny" @click="showEditHabit(h)">
							<svg-icon icon-class="edit" size="0.85rem" />
						</n-button>
						<n-button text size="tiny" @click="handleDeleteHabit(h.id)">
							<svg-icon icon-class="delete" size="0.85rem" />
						</n-button>
					</div>
				</div>
				<div class="card-stats">
					<span class="stat-item">
						<span class="stat-num">{{ getHabitCount(h.id) }}</span>
						<span class="stat-label">次记录</span>
					</span>
					<span class="stat-item">
						<span class="stat-num">{{ getHabitAvgRating(h.id) }}</span>
						<span class="stat-label">平均评分</span>
					</span>
					<span class="stat-item">
						<span class="stat-num">{{ getHabitStreak(h.id) }}</span>
						<span class="stat-label">最近连续</span>
					</span>
				</div>
				<n-button
					class="card-record-btn"
					type="primary"
					size="small"
					:color="h.color || '#18a058'"
					@click="showQuickRecord(h)"
				>
					记一笔
				</n-button>
			</div>
			<div v-if="activeHabits.length === 0" class="empty-tip">
				还没有创建习惯，点击上方「新建习惯」开始吧
			</div>
		</div>

		<!-- 热力图（年视图时展示） -->
		<div v-if="viewRange === 'year'" class="heatmap-section">
			<h3 class="section-title">年度热力图</h3>
			<div class="heatmap-wrapper">
				<div class="heatmap-months">
					<span v-for="m in monthLabels" :key="m">{{ m }}</span>
				</div>
				<div class="heatmap-grid">
					<div
						v-for="(day, idx) in heatmapDays"
						:key="idx"
						class="heatmap-cell"
						:style="{ background: day.color }"
						:title="`${day.date}: ${day.count} 条记录`"
					></div>
				</div>
				<div class="heatmap-legend">
					<span class="legend-label">少</span>
					<span class="legend-cell" :style="{ background: 'var(--grey-3, #ebedf0)' }"></span>
					<span class="legend-cell" style="background: #9be9a8"></span>
					<span class="legend-cell" style="background: #40c463"></span>
					<span class="legend-cell" style="background: #216e39"></span>
					<span class="legend-label">多</span>
				</div>
			</div>
		</div>

		<!-- 时间线记录列表 -->
		<div class="timeline-section">
			<div class="timeline-header">
				<h3 class="section-title">记录时间线</h3>
				<div class="timeline-nav">
					<n-button text @click="prevRange">
						<svg-icon icon-class="angle-left" size="1rem" />
					</n-button>
					<span class="range-label">{{ rangeLabel }}</span>
					<n-button text @click="nextRange">
						<svg-icon icon-class="angle-right" size="1rem" />
					</n-button>
				</div>
			</div>
			<div v-if="records.length === 0" class="empty-tip">该时段暂无记录</div>
			<div v-else class="timeline-list">
				<div v-for="r in records" :key="r.id" class="timeline-item">
					<div class="tl-dot" :style="{ background: getHabitColor(r.habitId) }"></div>
					<div class="tl-content">
						<div class="tl-top">
							<span class="tl-icon">{{ getHabitIcon(r.habitId) }}</span>
							<span class="tl-name">{{ getHabitName(r.habitId) }}</span>
							<span class="tl-date">{{ r.recordDate }}</span>
						</div>
						<div class="tl-meta">
							<span class="tl-value">{{ r.value || 1 }} {{ getHabitUnit(r.habitId) || '次' }}</span>
							<span class="tl-rating">{{ ratingStars(r.rating) }}</span>
							<span v-if="r.note" class="tl-note">{{ r.note }}</span>
						</div>
					</div>
					<div class="tl-actions">
						<n-button text size="tiny" @click="showEditRecord(r)">
							<svg-icon icon-class="edit" size="0.8rem" />
						</n-button>
						<n-button text size="tiny" @click="handleDeleteRecord(r.id)">
							<svg-icon icon-class="delete" size="0.8rem" />
						</n-button>
					</div>
				</div>
			</div>
		</div>

		<!-- 新增/编辑习惯弹窗 -->
		<n-modal v-model:show="habitDialogVisible" preset="card" :title="editingHabitId ? '编辑习惯' : '新建习惯'" style="max-width: 450px; width: calc(100vw - 2rem)" display-directive="if">
			<n-form label-placement="left" label-width="70">
				<n-form-item label="名称">
					<n-input v-model:value="habitForm.name" placeholder="如：跑步、阅读" />
				</n-form-item>
				<n-form-item label="图标">
					<div class="icon-picker">
						<span
							v-for="ic in iconList"
							:key="ic"
							class="icon-option"
							:class="{ active: habitForm.icon === ic }"
							@click="habitForm.icon = ic"
						>{{ ic }}</span>
					</div>
				</n-form-item>
				<n-form-item label="颜色">
					<div class="color-picker">
						<span
							v-for="c in colorList"
							:key="c"
							class="color-option"
							:class="{ active: habitForm.color === c }"
							:style="{ background: c }"
							@click="habitForm.color = c"
						></span>
					</div>
				</n-form-item>
				<n-form-item label="分类">
					<n-input v-model:value="habitForm.category" placeholder="运动 / 学习 / 生活" />
				</n-form-item>
				<n-form-item label="单位">
					<n-input v-model:value="habitForm.unit" placeholder="次 / 分钟 / 公里 / 页" />
				</n-form-item>
			</n-form>
			<template #action>
				<n-button @click="habitDialogVisible = false">取消</n-button>
				<n-button type="primary" @click="handleSaveHabit">保存</n-button>
			</template>
		</n-modal>

		<!-- 快速记录弹窗 -->
		<n-modal v-model:show="recordDialogVisible" preset="card" :title="editingRecordId ? '编辑记录' : `记录 - ${recordHabitName}`" style="max-width: 420px; width: calc(100vw - 2rem)" display-directive="if">
			<n-form label-placement="left" label-width="70">
				<n-form-item label="日期">
					<n-date-picker v-model:formatted-value="recordForm.recordDate" type="date" value-format="yyyy-MM-dd" style="width: 100%" />
				</n-form-item>
				<n-form-item label="数值">
					<n-input-number v-model:value="recordForm.value" :min="0" :step="1" style="width: 100%" />
					<span v-if="recordForm.unit" class="unit-label">{{ recordForm.unit }}</span>
				</n-form-item>
				<n-form-item label="评价">
					<div class="rating-picker">
						<span
							v-for="r in 5"
							:key="r"
							class="rating-star"
							:class="{ active: r <= recordForm.rating }"
							@click="recordForm.rating = r"
						>★</span>
					</div>
					<span class="rating-text">{{ ratingLabels[recordForm.rating - 1] }}</span>
				</n-form-item>
				<n-form-item label="备注">
					<n-input v-model:value="recordForm.note" type="textarea" :rows="2" placeholder="今天感觉怎么样？" />
				</n-form-item>
			</n-form>
			<template #action>
				<n-button @click="recordDialogVisible = false">取消</n-button>
				<n-button type="primary" @click="handleSaveRecord">保存</n-button>
			</template>
		</n-modal>
	</div>
</template>

<script setup lang="ts">
import dayjs from "dayjs";
import {
	getHabitList, addHabit, updateHabit, deleteHabit,
	getHabitRecords, addHabitRecord, updateHabitRecord, deleteHabitRecord,
	getHabitStats,
} from "@/api/habit";
import type { HabitItem, HabitRecordItem, DailyStat } from "@/api/habit/types";

const habits = ref<HabitItem[]>([]);
const records = ref<HabitRecordItem[]>([]);
const stats = ref<DailyStat[]>([]);
const selectedHabitId = ref<number | null>(null);
const viewRange = ref<"week" | "month" | "year">("month");
const rangeOffset = ref(0);

const iconList = [
	// 运动
	"🏃", "🏋️", "🧘", "🚴", "🏊", "⚽", "🏀", "🎾", "🏓", "⛷️", "🤸", "🚶",
	// 学习/工作
	"📖", "✍️", "💻", "📝", "🎓", "📚", "🔬", "🧮", "📐", "🗂️",
	// 生活/健康
	"💧", "🍎", "😴", "🛁", "💊", "🦷", "🧴", "🌿", "🍳", "🥗", "🥛",
	// 吃饭/饮食（新增分类）
	"🍚", "🍜", "🍝", "🍲", "🍣", "🍤", "🍗", "🍖", "🍕", "🍔", "🥪",
	// 休闲/爱好
	"🎸", "🎨", "📷", "🎮", "🎬", "🎤", "🎵", "✈️", "🏕️", "🧩",
	// 社交/情感
	"💬", "👨‍👩‍👧", "🤝", "💌", "📞", "🙏",
	// 理财/目标
	"💰", "📊", "🎯", "⭐", "🧠", "⏰", "🔥", "💪", "🌅",
];
const colorList = [
	"#18a058", "#2080f0", "#f0a020", "#d03050", "#8a2be2",
	"#ff6347", "#20b2aa", "#ff69b4", "#607d8b", "#795548",
	"#009688", "#3f51b5", "#e91e63", "#00bcd4", "#ff5722", "#4caf50",
];
const ratingLabels = ["差", "一般", "还行", "好", "很好"];

const activeHabits = computed(() => habits.value.filter(h => h.isActive === 1));
const habitOptions = computed(() => habits.value.map(h => ({ label: `${h.icon || '⭐'} ${h.name}`, value: h.id })));

const showHabitFilter = ref(false);
const visibleHabitIds = ref(new Set<number>());

const visibleActiveHabits = computed(() =>
	activeHabits.value.filter(h => visibleHabitIds.value.has(h.id))
);

const getRangeTotal = computed(() => {
	if (viewRange.value === "week") return 7;
	if (viewRange.value === "month") return 30;
	return 365;
});

const toggleHabitVisible = (id: number) => {
	const s = new Set(visibleHabitIds.value);
	if (s.has(id)) s.delete(id);
	else s.add(id);
	visibleHabitIds.value = s;
};

const initVisibleHabits = () => {
	visibleHabitIds.value = new Set(activeHabits.value.map(h => h.id));
};

const rangeDates = computed(() => {
	const now = dayjs();
	let start: dayjs.Dayjs, end: dayjs.Dayjs;
	if (viewRange.value === "week") {
		start = now.add(rangeOffset.value, "week").startOf("week");
		end = start.endOf("week");
	} else if (viewRange.value === "month") {
		start = now.add(rangeOffset.value, "month").startOf("month");
		end = start.endOf("month");
	} else {
		start = now.add(rangeOffset.value, "year").startOf("year");
		end = start.endOf("year");
	}
	return { start: start.format("YYYY-MM-DD"), end: end.format("YYYY-MM-DD") };
});

const rangeLabel = computed(() => {
	const now = dayjs();
	if (viewRange.value === "week") {
		const s = now.add(rangeOffset.value, "week").startOf("week");
		return `${s.format("MM/DD")} - ${s.endOf("week").format("MM/DD")}`;
	} else if (viewRange.value === "month") {
		const s = now.add(rangeOffset.value, "month");
		return s.format("YYYY年MM月");
	} else {
		const s = now.add(rangeOffset.value, "year");
		return s.format("YYYY年");
	}
});

const monthLabels = computed(() => {
	const yr = dayjs().add(rangeOffset.value, "year").year();
	return Array.from({ length: 12 }, (_, i) => `${i + 1}月`);
});

const heatmapDays = computed(() => {
	const yr = dayjs().add(rangeOffset.value, "year").year();
	const start = dayjs(`${yr}-01-01`);
	const end = dayjs(`${yr}-12-31`);
	const days: { date: string; count: number; color: string }[] = [];
	const countMap = new Map<string, number>();
	records.value.forEach(r => {
		if (visibleHabitIds.value.has(r.habitId)) {
			countMap.set(r.recordDate, (countMap.get(r.recordDate) || 0) + 1);
		}
	});
	let d = start;
	while (d.isBefore(end) || d.isSame(end, "day")) {
		const dateStr = d.format("YYYY-MM-DD");
		const count = countMap.get(dateStr) || 0;
		let color = "var(--grey-3, #ebedf0)";
		if (count === 1) color = "#9be9a8";
		else if (count === 2) color = "#40c463";
		else if (count >= 3) color = "#216e39";
		days.push({ date: dateStr, count, color });
		d = d.add(1, "day");
	}
	return days;
});

// ========== 习惯弹窗 ==========
const habitDialogVisible = ref(false);
const editingHabitId = ref<number | null>(null);
const habitForm = reactive({
	name: "",
	icon: "⭐",
	color: "#18a058",
	category: "",
	unit: "",
});

const showAddHabit = () => {
	editingHabitId.value = null;
	Object.assign(habitForm, { name: "", icon: "⭐", color: "#18a058", category: "", unit: "" });
	habitDialogVisible.value = true;
};

const showEditHabit = (h: HabitItem) => {
	editingHabitId.value = h.id;
	Object.assign(habitForm, { name: h.name, icon: h.icon, color: h.color, category: h.category || "", unit: h.unit || "" });
	habitDialogVisible.value = true;
};

const handleSaveHabit = async () => {
	if (!habitForm.name.trim()) { window.$message?.warning("请输入习惯名称"); return; }
	if (editingHabitId.value) {
		await updateHabit({ id: editingHabitId.value, ...habitForm });
	} else {
		await addHabit(habitForm);
	}
	habitDialogVisible.value = false;
	loadHabits();
};

const handleDeleteHabit = (id: number) => {
	window.$dialog?.warning({
		title: "删除习惯",
		content: "删除后所有相关记录也会被删除，确认？",
		positiveText: "删除",
		negativeText: "取消",
		onPositiveClick: async () => {
			await deleteHabit(id);
			loadHabits();
			loadRecords();
		},
	});
};

// ========== 记录弹窗 ==========
const recordDialogVisible = ref(false);
const editingRecordId = ref<number | null>(null);
const recordHabitName = ref("");
const recordForm = reactive({
	habitId: 0,
	recordDate: dayjs().format("YYYY-MM-DD"),
	value: 1,
	rating: 3,
	note: "",
	unit: "",
});

const showQuickRecord = (h: HabitItem) => {
	editingRecordId.value = null;
	recordHabitName.value = h.name;
	Object.assign(recordForm, {
		habitId: h.id,
		recordDate: dayjs().format("YYYY-MM-DD"),
		value: 1,
		rating: 3,
		note: "",
		unit: h.unit || "",
	});
	recordDialogVisible.value = true;
};

const showEditRecord = (r: HabitRecordItem) => {
	editingRecordId.value = r.id;
	const h = habits.value.find(x => x.id === r.habitId);
	recordHabitName.value = h?.name || "";
	Object.assign(recordForm, {
		habitId: r.habitId,
		recordDate: r.recordDate,
		value: r.value,
		rating: r.rating,
		note: r.note || "",
		unit: h?.unit || "",
	});
	recordDialogVisible.value = true;
};

const handleSaveRecord = async () => {
	if (editingRecordId.value) {
		await updateHabitRecord({ id: editingRecordId.value, ...recordForm });
	} else {
		await addHabitRecord(recordForm);
	}
	recordDialogVisible.value = false;
	loadRecords();
};

const handleDeleteRecord = (id: number) => {
	window.$dialog?.warning({
		title: "删除记录",
		content: "确认删除这条记录？",
		positiveText: "删除",
		negativeText: "取消",
		onPositiveClick: async () => {
			await deleteHabitRecord(id);
			loadRecords();
		},
	});
};

// ========== 辅助函数 ==========
const getHabitName = (id: number) => habits.value.find(h => h.id === id)?.name || "";
const getHabitIcon = (id: number) => habits.value.find(h => h.id === id)?.icon || "⭐";
const getHabitColor = (id: number) => habits.value.find(h => h.id === id)?.color || "#18a058";
const getHabitUnit = (id: number) => habits.value.find(h => h.id === id)?.unit || "";

const ratingStars = (r: number) => "★".repeat(r) + "☆".repeat(5 - r);

const getHabitCount = (habitId: number) => records.value.filter(r => r.habitId === habitId).length;

const getHabitAvgRating = (habitId: number) => {
	const arr = records.value.filter(r => r.habitId === habitId);
	if (arr.length === 0) return "-";
	return (arr.reduce((s, r) => s + r.rating, 0) / arr.length).toFixed(1);
};

const getHabitStreak = (habitId: number) => {
	const dates = records.value
		.filter(r => r.habitId === habitId)
		.map(r => r.recordDate)
		.filter((v, i, a) => a.indexOf(v) === i)
		.sort()
		.reverse();
	if (dates.length === 0) return 0;
	let streak = 1;
	for (let i = 0; i < dates.length - 1; i++) {
		if (dayjs(dates[i]).diff(dayjs(dates[i + 1]), "day") === 1) streak++;
		else break;
	}
	return streak;
};

// ========== 数据加载 ==========
const loadHabits = async () => {
	const { data } = await getHabitList();
	if (data.flag) {
		habits.value = data.data;
		if (visibleHabitIds.value.size === 0) initVisibleHabits();
	}
};

const loadRecords = async () => {
	const { start, end } = rangeDates.value;
	const { data } = await getHabitRecords({
		habitId: selectedHabitId.value || undefined,
		startDate: start,
		endDate: end,
	});
	if (data.flag) records.value = data.data;
	if (viewRange.value === "year") {
		const res = await getHabitStats({
			habitId: selectedHabitId.value || undefined,
			startDate: start,
			endDate: end,
		});
		if (res.data.flag) stats.value = res.data.data;
	}
};

const switchRange = (r: "week" | "month" | "year") => {
	viewRange.value = r;
	rangeOffset.value = 0;
	loadRecords();
};

const prevRange = () => { rangeOffset.value--; loadRecords(); };
const nextRange = () => { rangeOffset.value++; loadRecords(); };

onMounted(() => {
	loadHabits();
	loadRecords();
});
</script>

<style lang="scss" scoped>
.habit-container {
	color: var(--grey-7, #333);
}

.habit-toolbar {
	display: flex;
	align-items: center;
	gap: 0.75rem;
	flex-wrap: wrap;
	margin-bottom: 1rem;

	:deep(.n-base-selection) {
		border-radius: 50px !important;
		--n-border: 1.5px solid rgba(var(--todo-primary-rgb, 99,102,241), 0.15) !important;
		--n-border-hover: 1.5px solid rgba(var(--todo-primary-rgb, 99,102,241), 0.4) !important;
		--n-border-focus: 1.5px solid var(--todo-primary, #6366f1) !important;
		--n-border-active: 1.5px solid var(--todo-primary, #6366f1) !important;
		--n-color: var(--glass-bg, rgba(255, 255, 255, 0.55)) !important;
		--n-color-active: var(--glass-bg, rgba(255, 255, 255, 0.55)) !important;
		--n-color-focus: var(--glass-bg, rgba(255, 255, 255, 0.55)) !important;
		backdrop-filter: blur(12px);
		-webkit-backdrop-filter: blur(12px);
		background: transparent !important;
		.n-base-selection-placeholder__inner{color: var(--text-color) !important;}
		.n-base-selection__border, .n-base-selection__state-border { border-radius: 50px !important; }
	}
}

// ===== 统计面板 =====
.stats-panel {
	background: var(--card-bg, rgba(255, 255, 255, 0.65));
	backdrop-filter: blur(16px);
	-webkit-backdrop-filter: blur(16px);
	border: 1.5px solid var(--card-border, rgba(255, 255, 255, 0.5));
	border-radius: 16px;
	padding: 1rem 1.25rem;
	margin-bottom: 1.25rem;
	box-shadow: 0 4px 24px rgba(var(--todo-primary-rgb, 99,102,241), 0.08), 0 1px 3px rgba(0, 0, 0, 0.06);
}
.stats-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 0.75rem;
	:deep(.n-button) {
		width: 80px !important;
		height: 30px !important;
		font-size: 12px !important;
	}
}
.stats-title {
	font-size: 0.95rem;
	font-weight: 700;
	color: var(--grey-8, #222);
	margin: 0;
}
.habit-filter {
	display: flex;
	flex-wrap: wrap;
	gap: 0.5rem;
	margin-bottom: 0.75rem;
	padding-bottom: 0.75rem;
	border-bottom: 1px solid rgba(var(--todo-primary-rgb, 99,102,241), 0.06);
}
.filter-check {
	display: inline-flex;
	align-items: center;
	gap: 4px;
	padding: 0.25rem 0.7rem;
	border-radius: 50px;
	font-size: 0.78rem;
	cursor: pointer;
	background: rgba(var(--todo-primary-rgb, 99,102,241), 0.06);
	border: 1.5px solid rgba(var(--todo-primary-rgb, 99,102,241), 0.15);
	transition: all 0.2s;
	user-select: none;
	&.unchecked {
		background: var(--grey-1, #f9fafb);
		border-color: var(--grey-3, #e5e7eb);
		opacity: 0.55;
	}
	input[type="checkbox"] { display: none; }
}
.filter-icon { font-size: 0.9rem; }
.filter-name { font-weight: 500; color: var(--grey-7, #333); }

.stats-grid {
	display: grid;
	grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
	gap: 0.65rem;
}
.stats-chip {
	background: rgba(255, 255, 255, 0.5);
	border-radius: 16px;
	padding: 0.75rem 0.65rem;
	text-align: center;
	transition: all 0.25s ease;
	border: 1.5px solid rgba(var(--todo-primary-rgb, 99,102,241), 0.08);
	backdrop-filter: blur(8px);
	&:hover {
		transform: translateY(-2px);
		box-shadow: 0 4px 16px rgba(var(--todo-primary-rgb, 99,102,241), 0.1);
	}
}
.chip-icon { font-size: 1.5rem; margin-bottom: 0.2rem; }
.chip-count {
	font-size: 1.6rem;
	font-weight: 800;
	line-height: 1.2;
	color: var(--grey-8, #222);
}
.chip-name {
	font-size: 0.72rem;
	color: var(--grey-5, #999);
	margin-top: 0.15rem;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}
.chip-rate {
	font-size: 0.65rem;
	color: var(--grey-4, #bbb);
	margin-top: 0.1rem;
}
.stats-empty {
	text-align: center;
	padding: 1rem 0;
	color: var(--grey-4);
	font-size: 0.85rem;
}

// ===== 习惯卡片 =====
.habit-cards {
	display: grid;
	grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
	gap: 0.75rem;
	margin-bottom: 1.5rem;
}

.habit-card {
	background: var(--card-bg, rgba(255, 255, 255, 0.65));
	border-radius: 16px;
	padding: 1rem;
	box-shadow: 0 4px 24px rgba(var(--todo-primary-rgb, 99,102,241), 0.08), 0 1px 3px rgba(0, 0, 0, 0.06);
	border: 1.5px solid var(--card-border, rgba(255, 255, 255, 0.5));
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
	transition: all 0.3s ease;
	backdrop-filter: blur(16px);
	-webkit-backdrop-filter: blur(16px);
	&:hover {
		transform: translateY(-2px);
		box-shadow: 0 8px 32px rgba(var(--todo-primary-rgb, 99,102,241), 0.12);
	}
}

.card-top {
	display: flex;
	align-items: center;
	gap: 0.4rem;
}
.card-icon { font-size: 1.3rem; }
.card-name { font-weight: 600; font-size: 0.95rem; flex: 1; }
.card-category {
	font-size: 0.7rem;
	padding: 2px 10px;
	border-radius: 50px;
	background: rgba(var(--todo-primary-rgb, 99,102,241), 0.08);
	color: var(--grey-6, #666);
	font-weight: 500;
}
.card-actions { display: flex; gap: 2px; }

.card-stats {
	display: flex;
	justify-content: space-between;
}
.stat-item {
	display: flex;
	flex-direction: column;
	align-items: center;
}
.stat-num { font-weight: 700; font-size: 1.1rem; color: var(--grey-8, #222); }
.stat-label { font-size: 0.7rem; color: var(--grey-5, #999); }

.card-record-btn {
	width: 100%;
	border-radius: 50px;
}

.empty-tip {
	text-align: center;
	color: var(--grey-5, #999);
	padding: 2rem 0;
	font-size: 0.9rem;
}

// ===== 热力图 =====
.heatmap-section {
	margin-bottom: 1.5rem;
	background: var(--card-bg, rgba(255, 255, 255, 0.65));
	border-radius: 16px;
	padding: 1.25rem;
	box-shadow: 0 4px 24px rgba(var(--todo-primary-rgb, 99,102,241), 0.08), 0 1px 3px rgba(0, 0, 0, 0.06);
	border: 1.5px solid var(--card-border, rgba(255, 255, 255, 0.5));
	backdrop-filter: blur(16px);
	-webkit-backdrop-filter: blur(16px);
}

.section-title {
	font-size: 1rem;
	font-weight: 700;
	margin-bottom: 0.75rem;
	color: var(--grey-8, #222);
	margin-top: 0;
}

.heatmap-wrapper {
	overflow-x: auto;
	-webkit-overflow-scrolling: touch;
	padding-bottom: 0.5rem;
}

.heatmap-months {
	display: flex;
	gap: 0;
	margin-bottom: 4px;
	span {
		flex: 1;
		font-size: 0.7rem;
		color: var(--grey-5, #999);
		text-align: center;
	}
}

.heatmap-grid {
	display: grid;
	grid-template-rows: repeat(7, 12px);
	grid-auto-flow: column;
	grid-auto-columns: 12px;
	gap: 2px;
	min-width: 680px;
}

.heatmap-cell {
	width: 12px;
	height: 12px;
	border-radius: 3px;
	cursor: pointer;
	transition: transform 0.15s;
	&:hover { transform: scale(1.3); outline: 1px solid var(--grey-6, #666); }
}

.heatmap-legend {
	display: flex;
	align-items: center;
	justify-content: flex-end;
	gap: 3px;
	margin-top: 0.5rem;
}
.legend-label {
	font-size: 0.7rem;
	color: var(--grey-5, #999);
	margin: 0 2px;
}
.legend-cell {
	width: 12px;
	height: 12px;
	border-radius: 2px;
}

// ===== 时间线 =====
.timeline-section {
	background: var(--card-bg, rgba(255, 255, 255, 0.65));
	border-radius: 16px;
	padding: 1.25rem;
	box-shadow: 0 4px 24px rgba(var(--todo-primary-rgb, 99,102,241), 0.08), 0 1px 3px rgba(0, 0, 0, 0.06);
	border: 1.5px solid var(--card-border, rgba(255, 255, 255, 0.5));
	backdrop-filter: blur(16px);
	-webkit-backdrop-filter: blur(16px);
}

.timeline-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 0.75rem;
}

.timeline-nav {
	display: flex;
	align-items: center;
	gap: 0.5rem;
}

.range-label {
	font-size: 0.9rem;
	font-weight: 500;
	min-width: 100px;
	text-align: center;
}

.timeline-list {
	display: flex;
	flex-direction: column;
	gap: 0;
}

.timeline-item {
	display: flex;
	align-items: flex-start;
	gap: 0.75rem;
	padding: 0.6rem 0;
	border-bottom: 1px solid rgba(var(--todo-primary-rgb, 99,102,241), 0.06);
	&:last-child { border-bottom: none; }
}

.tl-dot {
	width: 10px;
	height: 10px;
	border-radius: 50%;
	flex-shrink: 0;
	margin-top: 5px;
}

.tl-content { flex: 1; }

.tl-top {
	display: flex;
	align-items: center;
	gap: 0.4rem;
}
.tl-icon { font-size: 1rem; }
.tl-name { font-weight: 600; font-size: 0.9rem; }
.tl-date { font-size: 0.75rem; color: var(--grey-5, #999); margin-left: auto; }

.tl-meta {
	display: flex;
	align-items: center;
	gap: 0.5rem;
	margin-top: 0.25rem;
	font-size: 0.8rem;
}
.tl-value { color: var(--grey-7, #333); font-weight: 500; }
.tl-rating { color: #f0a020; }
.tl-note { color: var(--grey-5, #999); font-style: italic; }

.tl-actions { display: flex; gap: 2px; flex-shrink: 0; }

// ===== 弹窗表单 =====
.icon-picker {
	display: flex;
	flex-wrap: wrap;
	gap: 0.4rem;
	max-height: 180px;
	overflow-y: auto;
	padding: 2px;
}
.icon-option {
	width: 2.2rem;
	height: 2.2rem;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 1.2rem;
	border-radius: 10px;
	cursor: pointer;
	border: 2px solid transparent;
	transition: all 0.2s ease;
	&:hover { background: var(--grey-1, #f5f5f5); transform: scale(1.1); }
	&.active { border-color: var(--todo-primary, #6366f1); background: rgba(var(--todo-primary-rgb, 99,102,241), 0.08); }
}

.color-picker {
	display: flex;
	flex-wrap: wrap;
	gap: 0.5rem;
}
.color-option {
	width: 1.6rem;
	height: 1.6rem;
	border-radius: 50%;
	cursor: pointer;
	border: 2px solid transparent;
	transition: all 0.15s;
	&:hover { transform: scale(1.15); }
	&.active { border-color: var(--grey-8, #222); box-shadow: 0 0 0 2px rgba(0, 0, 0, 0.1); }
}

.rating-picker {
	display: flex;
	gap: 0.3rem;
}
.rating-star {
	font-size: 1.4rem;
	cursor: pointer;
	color: var(--grey-3, #ddd);
	transition: color 0.15s;
	&.active { color: #f0a020; }
	&:hover { color: #f0a020; }
}
.rating-text {
	margin-left: 0.5rem;
	font-size: 0.85rem;
	color: var(--grey-6, #666);
	line-height: 1.4rem;
}

.unit-label {
	margin-left: 0.5rem;
	color: var(--grey-5, #999);
	font-size: 0.85rem;
	white-space: nowrap;
}

// ===== 移动端适配 =====
@media (max-width: 767px) {
	.stats-panel { padding: 0.75rem; border-radius: 12px; }
	.stats-grid { grid-template-columns: repeat(auto-fill, minmax(90px, 1fr)); gap: 0.5rem; }
	.stats-chip { padding: 0.6rem 0.4rem; }
	.chip-icon { font-size: 1.2rem; }
	.chip-count { font-size: 1.3rem; }
	.habit-toolbar {
		gap: 0.5rem;
		flex-direction: column;
		align-items: stretch;
	}
	.habit-cards {
		grid-template-columns: 1fr;
	}
	.heatmap-section {
		padding: 0.6rem;
	}
	.heatmap-grid {
		grid-template-rows: repeat(7, 10px);
		grid-auto-columns: 10px;
		gap: 1px;
		min-width: 540px;
	}
	.heatmap-cell { width: 10px; height: 10px; }
	.heatmap-months span { font-size: 0.6rem; }
	.timeline-section { padding: 0.6rem; }
	.timeline-header {
		flex-direction: column;
		align-items: flex-start;
		gap: 0.4rem;
	}
	.timeline-item { gap: 0.5rem; }
	.tl-top { flex-wrap: wrap; }
	.tl-date { margin-left: 0; }
	.tl-meta { flex-wrap: wrap; }
}

@media (max-width: 480px) {
	.habit-card { padding: 0.75rem; border-radius: 12px; }
	.card-icon { font-size: 1.1rem; }
	.card-name { font-size: 0.85rem; }
	.stat-num { font-size: 0.95rem; }
	.section-title { font-size: 0.9rem; }
}

</style>
