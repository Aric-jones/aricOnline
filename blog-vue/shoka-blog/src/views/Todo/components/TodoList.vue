<template>
	<div class="todo-list-container">
		<!-- 顶部工具栏 -->
		<div class="toolbar">
			<button class="btn-add" @click="showAdd">
				<span class="btn-add-icon">＋</span> 新增代办
			</button>
			<div class="filters">
				<n-select
					v-model:value="query.status"
					placeholder="状态"
					clearable
					:options="statusOptions"
					style="width: 110px"
					@update:value="loadData"
				/>
				<n-select
					v-model:value="query.priority"
					placeholder="优先级"
					clearable
					:options="priorityOptions"
					style="width: 110px"
					@update:value="loadData"
				/>
				<n-input
					v-model:value="query.keyword"
					placeholder="搜索..."
					clearable
					round
					style="width: 160px"
					@clear="loadData"
					@keyup.enter="loadData"
				/>
			</div>
		</div>

		<!-- 日期导航 -->
		<div class="date-nav">
			<button class="nav-arrow" @click="shiftCenter(-1)">‹</button>
			<span class="nav-title">{{ centerMonthLabel }}</span>
			<button class="nav-today" v-if="!isCenterToday" @click="goToday">今天</button>
			<button class="nav-arrow" @click="shiftCenter(1)">›</button>
		</div>

		<!-- 卡片堆叠区域 -->
		<div class="carousel-stage" v-if="!loading">
			<div
				v-for="card in visibleCards"
				:key="card.dateKey"
				class="date-card"
				:class="{ active: card.offset === 0 }"
				:style="cardStyle(card.offset)"
				@click="handleCardClick(card.dateKey)"
			>
				<!-- 卡片头 -->
				<div class="card-header">
					<div class="header-left">
						<span v-if="card.isToday" class="today-dot"></span>
						<span class="card-date">{{ card.dateLabel }}</span>
						<span class="card-weekday">{{ card.weekday }}</span>
						<span v-if="card.isToday" class="today-tag">今天</span>
					</div>
					<div class="header-right">
						<span class="card-count" v-if="card.items.length > 0">{{ card.items.length }} 项</span>
						<button class="card-add-btn" @click.stop="showAddForDate(card.dateKey)" title="添加">＋</button>
					</div>
				</div>

				<!-- 代办内容 -->
				<div class="card-body" v-if="card.offset === 0">
					<div v-if="card.items.length === 0" class="card-empty">
						<span>暂无事项</span>
					</div>
					<div
						v-for="item in card.items"
						:key="item.id"
						class="todo-item"
						:class="{ completed: item.status === 1 }"
					>
						<div class="item-check" @click.stop="handleToggle(item.id)">
							<div class="checkbox" :class="{ checked: item.status === 1 }"></div>
						</div>
						<div class="item-body" @click.stop="showEdit(item)">
							<div class="item-title">{{ item.title }}</div>
							<div class="item-meta">
								<span class="p-dot" :class="'p' + item.priority"></span>
								<span class="p-label" :class="'p' + item.priority">{{ priorityLabel(item.priority) }}</span>
								<span v-if="item.category" class="cat-tag">{{ item.category }}</span>
								<span v-if="item.endTime" class="time-label">{{ formatHM(item.endTime) }}</span>
							</div>
							<div v-if="item.description" class="item-desc">{{ item.description }}</div>
						</div>
						<button class="item-del" @click.stop="handleDelete(item.id)" title="删除">
							<svg-icon icon-class="delete" size="0.78rem" />
						</button>
					</div>
				</div>

			<!-- 非居中卡片只显示摘要（第四层不渲染内容） -->
			<div class="card-summary" v-else-if="Math.abs(card.offset) <= 2">
				<span v-if="card.items.length === 0">暂无事项</span>
				<span v-else>{{ card.items.length }} 条待办</span>
			</div>
			</div>
		</div>
		<div v-else class="loading-tip">加载中...</div>

		<!-- 隐藏已完成 toggle -->
		<div class="hide-toggle">
			<label>
				<input type="checkbox" v-model="hideCompleted" @change="loadData" />
				隐藏已完成/已失败
			</label>
		</div>

		<!-- 弹窗 -->
		<n-modal v-model:show="dialogVisible" :mask-closable="true" display-directive="if">
			<div class="modal-glass">
				<div class="modal-title">{{ editingId ? '编辑代办' : '新增代办' }}</div>
				<div class="form-group">
					<label class="form-label">标题</label>
					<n-input v-model:value="form.title" placeholder="请输入代办标题" round />
				</div>
				<div class="form-group">
					<label class="form-label">描述</label>
					<n-input v-model:value="form.description" type="textarea" placeholder="详细描述（可选）" :rows="3" />
				</div>
				<div class="form-row">
					<div class="form-group">
						<label class="form-label">优先级</label>
						<n-select v-model:value="form.priority" :options="priorityOptions" />
					</div>
					<div class="form-group">
						<label class="form-label">分类</label>
						<n-input v-model:value="form.category" placeholder="分类标签" round />
					</div>
				</div>
				<div class="form-row">
					<div class="form-group">
						<label class="form-label">开始时间</label>
						<n-date-picker v-model:formatted-value="form.startTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" clearable style="width:100%" />
					</div>
					<div class="form-group">
						<label class="form-label">截止时间</label>
						<n-date-picker v-model:formatted-value="form.endTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" clearable style="width:100%" />
					</div>
				</div>
				<div class="modal-footer">
					<button class="btn-ghost" @click="dialogVisible = false">取消</button>
					<button class="btn-add" @click="handleSubmit" :disabled="submitting">
						{{ submitting ? '提交中...' : '确定' }}
					</button>
				</div>
			</div>
		</n-modal>
	</div>
</template>

<script setup lang="ts">
import { getTodoList, addTodo, updateTodo, deleteTodo, toggleTodoStatus } from "@/api/todo";
import type { TodoItem, TodoQuery, TodoReq } from "@/api/todo/types";

const loading = ref(false);
const submitting = ref(false);
const allTodos = ref<TodoItem[]>([]);
const dialogVisible = ref(false);
const editingId = ref<number | null>(null);
const hideCompleted = ref(false);

const centerDate = ref(todayStr());

function todayStr(): string {
	const d = new Date();
	return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
}

function addDays(dateStr: string, days: number): string {
	const d = new Date(dateStr);
	d.setDate(d.getDate() + days);
	return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
}

const query = reactive<TodoQuery>({
	current: 1,
	size: 500,
	type: 0,
	status: null,
	priority: null,
	keyword: "",
});

const form = reactive({
	title: "",
	description: "",
	priority: 1,
	category: "",
	startTime: null as string | null,
	endTime: null as string | null,
});

const statusOptions = [
	{ label: "未完成", value: 0 },
	{ label: "已完成", value: 1 },
];
const priorityOptions = [
	{ label: "低", value: 0 },
	{ label: "中", value: 1 },
	{ label: "高", value: 2 },
];

const priorityLabel = (p: number) => (p === 0 ? "低" : p === 1 ? "中" : "高");

const formatHM = (dt: string): string => {
	if (!dt) return "";
	const t = dt.includes("T") ? dt.split("T")[1] : dt.substring(11);
	return t ? t.substring(0, 5) : "";
};

function toDateKey(dt: string | null | undefined): string | null {
	if (!dt) return null;
	return dt.substring(0, 10).replace("T", "");
}

// 跨天待办：在 startTime 到 endTime 之间的每一天都显示
const todoByDate = computed(() => {
	const map = new Map<string, TodoItem[]>();
	const pushTo = (key: string, item: TodoItem) => {
		if (!map.has(key)) map.set(key, []);
		map.get(key)!.push(item);
	};
	for (const item of allTodos.value) {
		if (hideCompleted.value && item.status === 1) continue;
		const start = toDateKey(item.startTime);
		const end = toDateKey(item.endTime);
		if (start && end && start !== end) {
			let cur = start;
			const limit = 60;
			let count = 0;
			while (cur <= end && count++ < limit) {
				pushTo(cur, item);
				cur = addDays(cur, 1);
			}
		} else {
			const key = end || start || toDateKey(item.createTime) || todayStr();
			pushTo(key, item);
		}
	}
	for (const items of map.values()) {
		items.sort((a, b) => {
			if (b.priority !== a.priority) return b.priority - a.priority;
			return (b.createTime || "").localeCompare(a.createTime || "");
		});
	}
	return map;
});

interface CardData {
	dateKey: string;
	dateLabel: string;
	weekday: string;
	isToday: boolean;
	items: TodoItem[];
	offset: number; // -3, -2, -1, 0, +1, +2, +3
}

const weekdays = ["周日", "周一", "周二", "周三", "周四", "周五", "周六"];

// 渲染 7 张卡片（offset -3 到 +3），第 4 层是缓冲区
// 切换 1 步时有 6 张卡片留在 DOM，全部平滑过渡
const visibleCards = computed<CardData[]>(() => {
	const cards: CardData[] = [];
	for (let offset = -3; offset <= 3; offset++) {
		const dk = addDays(centerDate.value, offset);
		const d = new Date(dk);
		const parts = dk.split("-");
		cards.push({
			dateKey: dk,
			dateLabel: `${parts[1]}/${parts[2]}`,
			weekday: weekdays[d.getDay()],
			isToday: dk === todayStr(),
			items: todoByDate.value.get(dk) || [],
			offset,
		});
	}
	return cards;
});

// ===== 卡片层叠参数（可自行调节） =====
// SHIFT: 每层到中心的水平距离(px)，值越大离中心越远
// SCALE: 每层的缩放比例
// OPACITY: 每层的不透明度
// L1=第二层(±1天), L2=第三层(±2天), L3=第四层(±3天，进出缓冲区)
const SHIFT_L1 = 290;
const SHIFT_L2 = 490;
const SHIFT_L3 = 690;
const SCALE_L1 = 0.92;
const SCALE_L2 = 0.84;
const SCALE_L3 = 0.72;
const OPACITY_L1 = 0.88;
const OPACITY_L2 = 0.6;
const OPACITY_L3 = 0;

function cardStyle(offset: number): Record<string, string> {
	const abs = Math.abs(offset);
	const cfg = [
		{ scale: 1, z: 10, opacity: 1, shift: 0 },
		{ scale: SCALE_L1, z: 6, opacity: OPACITY_L1, shift: SHIFT_L1 },
		{ scale: SCALE_L2, z: 3, opacity: OPACITY_L2, shift: SHIFT_L2 },
		{ scale: SCALE_L3, z: 1, opacity: OPACITY_L3, shift: SHIFT_L3 },
	][Math.min(abs, 3)];
	const shiftPx = offset === 0 ? 0 : (offset > 0 ? cfg.shift : -cfg.shift);

	return {
		transform: `translateX(${shiftPx}px) scale(${cfg.scale})`,
		zIndex: String(cfg.z),
		opacity: String(cfg.opacity),
		pointerEvents: abs >= 3 ? "none" : "auto",
	};
}

const centerMonthLabel = computed(() => {
	const parts = centerDate.value.split("-");
	const d = new Date(centerDate.value);
	return `${parts[0]} / ${parts[1]} / ${parts[2]}  ${weekdays[d.getDay()]}`;
});

const isCenterToday = computed(() => centerDate.value === todayStr());

const shiftCenter = (days: number) => {
	centerDate.value = addDays(centerDate.value, days);
};

const goToday = () => {
	centerDate.value = todayStr();
};

const handleCardClick = (dateKey: string) => {
	if (dateKey === centerDate.value) return;
	centerDate.value = dateKey;
};

const loadData = () => {
	loading.value = true;
	query.current = 1;
	getTodoList(query)
		.then(({ data }) => {
			if (data.flag) {
				allTodos.value = data.data.recordList;
			}
		})
		.finally(() => (loading.value = false));
};

const getCurrentTimeStr = (): string => {
	const now = new Date();
	const year = now.getFullYear();
	const month = String(now.getMonth() + 1).padStart(2, "0");
	const day = String(now.getDate()).padStart(2, "0");
	const hours = String(now.getHours()).padStart(2, "0");
	const minutes = String(now.getMinutes()).padStart(2, "0");
	const seconds = String(now.getSeconds()).padStart(2, "0");
	return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
};

const showAdd = () => {
	editingId.value = null;
	const endTime = `${centerDate.value} 23:59:59`;
	const startTime = getCurrentTimeStr();
	Object.assign(form, { title: "", description: "", priority: 1, category: "", startTime, endTime });
	dialogVisible.value = true;
};

const showAddForDate = (dateKey: string) => {
	editingId.value = null;
	const endTime = `${dateKey} 23:59:59`;
	const startTime = getCurrentTimeStr();
	Object.assign(form, { title: "", description: "", priority: 1, category: "", startTime, endTime });
	dialogVisible.value = true;
};

const normalizeDateStr = (s: string | null | undefined): string | null => {
	if (!s) return null;
	return s.replace("T", " ");
};

const showEdit = (item: TodoItem) => {
	editingId.value = item.id;
	Object.assign(form, {
		title: item.title,
		description: item.description || "",
		priority: item.priority,
		category: item.category || "",
		startTime: normalizeDateStr(item.startTime),
		endTime: normalizeDateStr(item.endTime),
	});
	dialogVisible.value = true;
};

const handleSubmit = () => {
	if (!form.title.trim()) { window.$message?.warning("标题不能为空"); return; }
	if (form.priority == null) { window.$message?.warning("请选择优先级"); return; }
	if (!form.category?.trim()) { window.$message?.warning("请填写分类"); return; }
	if (!form.startTime) { window.$message?.warning("请选择开始时间"); return; }
	if (!form.endTime) { window.$message?.warning("请选择截止时间"); return; }
	if (form.startTime && form.endTime && form.endTime < form.startTime) {
		window.$message?.warning("截止时间不能早于开始时间");
		return;
	}
	submitting.value = true;
	const req: TodoReq = { ...form };
	const action = editingId.value ? updateTodo({ ...req, id: editingId.value }) : addTodo(req);
	action
		.then(({ data }) => {
			if (data.flag) {
				window.$message?.success(editingId.value ? "修改成功" : "添加成功");
				dialogVisible.value = false;
				loadData();
			}
		})
		.finally(() => (submitting.value = false));
};

const handleToggle = (id: number) => {
	toggleTodoStatus(id).then(({ data }) => {
		if (data.flag) loadData();
	});
};

const handleDelete = (id: number) => {
	window.$dialog?.warning({
		title: "确认删除",
		content: "删除后不可恢复，确认删除？",
		positiveText: "删除",
		negativeText: "取消",
		onPositiveClick: () => {
			deleteTodo(id).then(({ data }) => {
				if (data.flag) {
					window.$message?.success("删除成功");
					loadData();
				}
			});
		},
	});
};

onMounted(() => loadData());
</script>

<style lang="scss" scoped>
// 通过 CSS 变量引用主题色，可由右上角按钮动态切换
// --todo-primary / --todo-primary-light / --todo-primary-rgb 在 useAccentColor 中设置

// ==================== 工具栏 ====================
.toolbar {
	display: flex; justify-content: space-between; align-items: center;
	flex-wrap: wrap; gap: 0.75rem; margin-bottom: 1rem;
}
.btn-add {
	display: inline-flex; align-items: center; gap: 6px;
	padding: 0.55rem 1.25rem; border-radius: 50px; border: none;
	font-size: 0.85rem; font-weight: 600; cursor: pointer;
	background: linear-gradient(135deg, var(--todo-primary, #6366f1), var(--todo-primary-light, #818cf8));
	color: #fff; box-shadow: 0 4px 14px rgba(var(--todo-primary-rgb, 99,102,241), 0.3);
	transition: all 0.25s ease; font-family: inherit;
	&:hover { transform: translateY(-1px); box-shadow: 0 6px 20px rgba(var(--todo-primary-rgb, 99,102,241), 0.4); }
	&:active { transform: translateY(0); }
	&:disabled { opacity: 0.6; cursor: not-allowed; transform: none; }
}
.btn-add-icon { font-size: 1rem; }
.filters {
	display: flex; gap: 0.5rem; flex-wrap: wrap;

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
		.n-base-selection__border, .n-base-selection__state-border { border-radius: 50px !important; }
	}

	:deep(.n-input) {
		--n-color: var(--glass-bg, rgba(255, 255, 255, 0.45)) !important;
		--n-color-focus: var(--glass-bg, rgba(255, 255, 255, 0.55)) !important;
		--n-border: 1.5px solid rgba(var(--todo-primary-rgb, 99,102,241), 0.15) !important;
		--n-border-hover: 1.5px solid rgba(var(--todo-primary-rgb, 99,102,241), 0.4) !important;
		--n-border-focus: 1.5px solid var(--todo-primary, #6366f1) !important;
		--n-caret-color: var(--todo-primary, #6366f1) !important;
		backdrop-filter: blur(12px);
		-webkit-backdrop-filter: blur(12px);
	}
}
.loading-tip { text-align: center; padding: 3rem 0; color: var(--grey-5, #94a3b8); }

// ==================== 日期导航 ====================
.date-nav {
	display: flex; align-items: center; justify-content: center;
	gap: 0.75rem; margin-bottom: 1.5rem;
}
.nav-arrow {
	width: 34px; height: 34px; border-radius: 50%;
	border: 1.5px solid rgba(var(--todo-primary-rgb, 99,102,241), 0.12);
	background: var(--glass-bg, rgba(255, 255, 255, 0.6));
	backdrop-filter: blur(8px);
	cursor: pointer; font-size: 1.2rem; color: var(--grey-6, #64748b);
	display: flex; align-items: center; justify-content: center; transition: all 0.2s;
	&:hover { border-color: var(--todo-primary, #6366f1); color: var(--todo-primary, #6366f1); background: rgba(var(--todo-primary-rgb, 99,102,241), 0.05); }
}
.nav-title {
	font-size: 1.1rem; font-weight: 700;
	color: var(--grey-8, #1e293b); min-width: 7rem; text-align: center;
}
.nav-today {
	padding: 0.25rem 0.75rem; border-radius: 50px;
	border: 1.5px solid rgba(var(--todo-primary-rgb, 99,102,241), 0.2);
	background: transparent; cursor: pointer;
	font-size: 0.72rem; font-weight: 600;
	color: var(--todo-primary, #6366f1); transition: all 0.2s; font-family: inherit;
	&:hover { background: rgba(var(--todo-primary-rgb, 99,102,241), 0.06); }
}

// ==================== 卡片堆叠区域 ====================
.carousel-stage {
	position: relative;
	width: 100%;
	height: 540px;
	display: flex;
	justify-content: center;
	align-items: center;
	perspective: 1200px;
	margin-bottom: 1rem;
}

.date-card {
	position: absolute;
	width: 300px;
	height: 460px;
	border-radius: 20px;
	background: var(--todo-card-bg, var(--glass-bg, rgba(255, 255, 255, 0.7)));
	backdrop-filter: blur(20px); -webkit-backdrop-filter: blur(20px);
	border: 1.5px solid var(--glass-border, rgba(255, 255, 255, 0.5));
	box-shadow:
		var(--todo-card-glow, 0 0 0 transparent),
		0 8px 32px rgba(var(--todo-primary-rgb, 99,102,241), 0.1),
		0 2px 8px rgba(0, 0, 0, 0.04);
	cursor: pointer;
	transition: all 0.45s cubic-bezier(0.4, 0, 0.2, 1);
	overflow: hidden;
	display: flex;
	flex-direction: column;

	&.active {
		width: 340px;
		height: 500px;
		cursor: default;
		border-color: rgba(var(--todo-primary-rgb, 99,102,241), 0.25);
		box-shadow:
			var(--todo-card-glow, 0 0 0 transparent),
			0 0 48px rgba(var(--todo-primary-rgb, 99,102,241), 0.15),
			0 16px 56px rgba(var(--todo-primary-rgb, 99,102,241), 0.18),
			0 4px 12px rgba(0, 0, 0, 0.06),
			inset 0 1px 0 rgba(255, 255, 255, 0.5);
		.card-header {
			border-bottom-color: rgba(var(--todo-primary-rgb, 99,102,241), 0.1);
		}
	}

	&:not(.active):hover {
		filter: brightness(1.04);
	}
}

// ==================== 卡片头部 ====================
.card-header {
	display: flex; align-items: center; justify-content: space-between;
	padding: 1rem 1.3rem;
	border-bottom: 1px solid rgba(var(--todo-primary-rgb, 99,102,241), 0.05);
}
.header-left { display: flex; align-items: center; gap: 0.5rem; }
.today-dot {
	width: 8px; height: 8px; border-radius: 50%;
	background: linear-gradient(135deg, var(--todo-primary, #6366f1), var(--todo-primary-light, #818cf8));
	flex-shrink: 0;
	box-shadow: 0 0 8px rgba(var(--todo-primary-rgb, 99,102,241), 0.5);
}
.card-date { font-size: 1.2rem; font-weight: 800; color: var(--grey-8, #1e293b); }
.card-weekday { font-size: 0.85rem; font-weight: 500; color: var(--grey-5, #94a3b8); }
.today-tag {
	padding: 2px 10px; border-radius: 50px; font-size: 0.64rem; font-weight: 700;
	background: linear-gradient(135deg, var(--todo-primary, #6366f1), var(--todo-primary-light, #818cf8)); color: #fff;
}
.header-right { display: flex; align-items: center; gap: 0.5rem; }
.card-count {
	font-size: 0.72rem; color: var(--grey-5, #94a3b8);
	background: rgba(var(--todo-primary-rgb, 99,102,241), 0.06); padding: 2px 10px; border-radius: 50px; font-weight: 500;
}
.card-add-btn {
	width: 30px; height: 30px; border-radius: 50%;
	border: 1.5px solid rgba(var(--todo-primary-rgb, 99,102,241), 0.12);
	background: transparent; cursor: pointer; font-size: 0.95rem;
	color: var(--grey-5, #94a3b8);
	display: flex; align-items: center; justify-content: center; transition: all 0.2s;
	&:hover { border-color: var(--todo-primary, #6366f1); color: var(--todo-primary, #6366f1); background: rgba(var(--todo-primary-rgb, 99,102,241), 0.05); }
}

// ==================== 卡片内容 (居中卡片) ====================
.card-body {
	flex: 1;
	overflow-y: auto;
	padding: 0.5rem 0.8rem 0.8rem;
	&::-webkit-scrollbar { width: 3px; }
	&::-webkit-scrollbar-thumb { background: rgba(var(--todo-primary-rgb, 99,102,241), 0.12); border-radius: 10px; }
}
.card-empty {
	display: flex; align-items: center; justify-content: center;
	padding: 2rem 0; color: var(--grey-4, #bbb); font-size: 0.85rem;
}

// 非居中卡片的摘要行
.card-summary {
	padding: 0.8rem 1.3rem 1rem;
	font-size: 0.8rem; color: var(--grey-5, #94a3b8); text-align: center;
}

// ==================== 代办条目 ====================
.todo-item {
	display: flex; align-items: flex-start; gap: 0.6rem;
	padding: 0.65rem 0.6rem; border-radius: 10px;
	transition: background 0.15s; cursor: default;
	&:hover { background: rgba(var(--todo-primary-rgb, 99,102,241), 0.04); }
	&:not(:last-child) { border-bottom: 1px solid rgba(var(--todo-primary-rgb, 99,102,241), 0.04); }
	&.completed {
		opacity: 0.4;
		.item-title { text-decoration: line-through; color: var(--grey-5, #94a3b8); }
	}
}
.item-check { padding-top: 2px; cursor: pointer; }
.checkbox {
	width: 20px; height: 20px; border-radius: 6px;
	border: 2px solid var(--grey-4, #ccc);
	display: flex; align-items: center; justify-content: center;
	transition: all 0.2s; flex-shrink: 0;
	&::after { content: ""; }
	&.checked {
		border-color: var(--todo-primary, #6366f1);
		background: linear-gradient(135deg, var(--todo-primary, #6366f1), var(--todo-primary-light, #818cf8));
		&::after { content: "✓"; color: #fff; font-size: 11px; font-weight: 700; }
	}
	&:hover { border-color: var(--todo-primary, #6366f1); }
}
.item-body { flex: 1; min-width: 0; cursor: pointer; }
.item-title {
	font-size: 0.9rem; font-weight: 600; line-height: 1.4;
	color: var(--grey-7, #1e293b); word-break: break-all;
}
.item-meta { display: flex; align-items: center; gap: 0.4rem; margin-top: 0.2rem; flex-wrap: wrap; }
.p-dot {
	width: 6px; height: 6px; border-radius: 50%; flex-shrink: 0;
	&.p0 { background: #22c55e; }
	&.p1 { background: #f59e0b; }
	&.p2 { background: #ef4444; }
}
.p-label {
	font-size: 0.66rem; font-weight: 600;
	&.p0 { color: #16a34a; }
	&.p1 { color: #d97706; }
	&.p2 { color: #ef4444; }
}
.cat-tag {
	padding: 1px 7px; border-radius: 50px;
	background: rgba(var(--todo-primary-rgb, 99,102,241), 0.06); color: var(--todo-primary, #6366f1);
	font-size: 0.64rem; font-weight: 600;
}
.time-label { font-size: 0.66rem; color: var(--grey-5, #94a3b8); }
.item-desc {
	margin-top: 0.2rem; font-size: 0.74rem; color: var(--grey-5, #94a3b8);
	overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.item-del {
	flex-shrink: 0; opacity: 0;
	width: 26px; height: 26px; border-radius: 6px; border: none;
	background: transparent; cursor: pointer;
	display: flex; align-items: center; justify-content: center;
	color: var(--grey-5, #94a3b8); transition: all 0.2s;
	.todo-item:hover & { opacity: 1; }
	&:hover { background: rgba(239, 68, 68, 0.08); color: #ef4444; }
}

// ==================== 隐藏已完成 ====================
.hide-toggle {
	display: flex; justify-content: flex-end; padding: 0.2rem 0;
	label {
		display: flex; align-items: center; gap: 0.4rem;
		font-size: 0.78rem; color: var(--grey-5, #94a3b8);
		cursor: pointer;
		input[type="checkbox"] { accent-color: var(--todo-primary, #6366f1); }
	}
}

// ==================== 弹窗 ====================
.modal-glass {
	background: var(--glass-bg-heavy, rgba(255, 255, 255, 0.75));
	backdrop-filter: blur(24px); -webkit-backdrop-filter: blur(24px);
	border: 1.5px solid var(--glass-border, rgba(255, 255, 255, 0.5));
	border-radius: 22px; padding: 2rem; width: 480px;
	max-width: calc(100vw - 2rem); margin: 0 auto;
	box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1), 0 4px 16px rgba(var(--todo-primary-rgb, 99,102,241), 0.08);

	:deep(.n-input:not(.n-input--textarea)) {
		border-radius: 50px !important;
		--n-color: var(--glass-bg, rgba(255, 255, 255, 0.45)) !important;
		--n-color-focus: var(--glass-bg, rgba(255, 255, 255, 0.55)) !important;
		--n-border: 1.5px solid rgba(var(--todo-primary-rgb, 99,102,241), 0.15) !important;
		--n-border-hover: 1.5px solid var(--todo-primary, #6366f1) !important;
		--n-border-focus: 1.5px solid var(--todo-primary, #6366f1) !important;
		--n-caret-color: var(--todo-primary, #6366f1) !important;
		.n-input__border, .n-input__state-border, .n-input-wrapper { border-radius: 50px !important; }
	}

	:deep(.n-input--textarea) {
		border-radius: 12px !important;
		--n-color: var(--glass-bg, rgba(255, 255, 255, 0.45)) !important;
		--n-color-focus: var(--glass-bg, rgba(255, 255, 255, 0.55)) !important;
		--n-border: 1.5px solid rgba(var(--todo-primary-rgb, 99,102,241), 0.15) !important;
		--n-border-hover: 1.5px solid var(--todo-primary, #6366f1) !important;
		--n-border-focus: 1.5px solid var(--todo-primary, #6366f1) !important;
		--n-caret-color: var(--todo-primary, #6366f1) !important;
		.n-input__border, .n-input__state-border, .n-input-wrapper { border-radius: 12px !important; }
	}

	:deep(.n-base-selection) {
		border-radius: 50px !important;
		--n-border: 1.5px solid rgba(var(--todo-primary-rgb, 99,102,241), 0.15) !important;
		--n-border-hover: 1.5px solid rgba(var(--todo-primary-rgb, 99,102,241), 0.4) !important;
		--n-border-focus: 1.5px solid var(--todo-primary, #6366f1) !important;
		--n-border-active: 1.5px solid var(--todo-primary, #6366f1) !important;
		--n-color: var(--glass-bg, rgba(255, 255, 255, 0.45)) !important;
		--n-color-active: var(--glass-bg, rgba(255, 255, 255, 0.55)) !important;
		--n-color-focus: var(--glass-bg, rgba(255, 255, 255, 0.55)) !important;
		background: transparent !important;
		.n-base-selection-label, .n-base-selection-tags { border-radius: 50px !important; }
		.n-base-selection__border, .n-base-selection__state-border { border-radius: 50px !important; }
	}

	:deep(.n-date-picker .n-input) {
		border-radius: 50px !important;
		--n-color: var(--glass-bg, rgba(255, 255, 255, 0.45)) !important;
		--n-color-focus: var(--glass-bg, rgba(255, 255, 255, 0.55)) !important;
		--n-border: 1.5px solid rgba(var(--todo-primary-rgb, 99,102,241), 0.15) !important;
		--n-border-hover: 1.5px solid var(--todo-primary, #6366f1) !important;
		--n-border-focus: 1.5px solid var(--todo-primary, #6366f1) !important;
		.n-input__border, .n-input__state-border, .n-input-wrapper { border-radius: 50px !important; }
	}
}

.modal-title {
	font-size: 1.15rem; font-weight: 700;
	margin-bottom: 1.5rem; color: var(--grey-8, #1e293b);
}
.form-group { margin-bottom: 1rem; }
.form-label {
	display: block; font-size: 0.78rem; font-weight: 600;
	color: var(--grey-6, #64748b); margin-bottom: 0.35rem;
}
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem; }
.modal-footer { display: flex; justify-content: flex-end; gap: 0.6rem; margin-top: 1.5rem; }
.btn-ghost {
	background: var(--glass-bg, rgba(255, 255, 255, 0.45));
	backdrop-filter: blur(8px);
	border: 1.5px solid rgba(var(--todo-primary-rgb, 99,102,241), 0.15);
	color: var(--grey-6, #64748b); padding: 0.5rem 1.2rem; border-radius: 50px;
	font-size: 0.82rem; cursor: pointer; font-weight: 600; font-family: inherit;
	transition: all 0.2s;
	&:hover { border-color: var(--todo-primary, #6366f1); color: var(--todo-primary, #6366f1); background: rgba(var(--todo-primary-rgb, 99,102,241), 0.06); }
}

// ==================== 响应式 ====================
@media (max-width: 767px) {
	.toolbar { flex-direction: column; align-items: stretch; }
	.btn-add { width: 100%; justify-content: center; }
	.filters {
		flex-wrap: wrap;
		:deep(.n-select), :deep(.n-input) { width: 100% !important; flex: 1; min-width: 0; }
	}
	.carousel-stage {
		height: auto; min-height: 360px; perspective: none;
	}
	.date-card {
		position: relative !important;
		width: 100% !important; height: auto !important; min-height: 340px;
		transform: none !important; opacity: 1 !important;
		margin: 0 auto;
		&:not(.active) { display: none !important; }
		&.active { width: 100% !important; height: auto !important; min-height: 340px; }
	}
	.card-body { max-height: 50vh; overflow-y: auto; }
	.form-row { grid-template-columns: 1fr; }
	.modal-glass { padding: 1.5rem; }
}
</style>
