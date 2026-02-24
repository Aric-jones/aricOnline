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

		<!-- 代办列表 -->
		<div class="todo-cards">
			<div v-if="loading" class="loading-tip">加载中...</div>
			<div v-else-if="todoList.length === 0" class="empty-tip">暂无代办事项</div>
			<div
				v-for="item in todoList"
				:key="item.id"
				class="todo-card"
				:class="{ completed: item.status === 1 }"
			>
				<div class="card-left" @click="handleToggle(item.id)">
					<div class="checkbox" :class="{ checked: item.status === 1 }"></div>
				</div>
				<div class="card-body">
					<div class="card-title">{{ item.title }}</div>
					<div class="card-meta">
						<span class="priority-tag" :class="'p' + item.priority">
							{{ priorityLabel(item.priority) }}
						</span>
						<span v-if="item.category" class="category-tag">{{ item.category }}</span>
						<span v-if="item.endTime" class="time-tag">
							🕐 {{ formatShortDate(item.endTime) }}
						</span>
					</div>
					<div v-if="item.description" class="card-desc">{{ item.description }}</div>
				</div>
				<div class="card-actions">
					<button class="icon-btn" @click="showEdit(item)" title="编辑">
						<svg-icon icon-class="edit" size="0.85rem" />
					</button>
					<button class="icon-btn icon-btn-del" @click="handleDelete(item.id)" title="删除">
						<svg-icon icon-class="delete" size="0.85rem" />
					</button>
				</div>
			</div>
		</div>

		<!-- 分页 -->
		<div class="pagination" v-if="total > query.size!">
			<n-pagination
				v-model:page="currentPage"
				:page-size="query.size!"
				:item-count="total"
				@update:page="handlePageChange"
			/>
		</div>

		<!-- 新增/编辑弹窗 -->
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
import { useWindowSize } from "@vueuse/core";

const { width: winWidth } = useWindowSize();
const formCols = computed(() => (winWidth.value < 500 ? 1 : 2));

const loading = ref(false);
const submitting = ref(false);
const todoList = ref<TodoItem[]>([]);
const total = ref(0);
const currentPage = ref(1);
const dialogVisible = ref(false);
const editingId = ref<number | null>(null);

const query = reactive<TodoQuery>({
	current: 1,
	size: 10,
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

const formatShortDate = (dt: string) => {
	if (!dt) return "";
	return dt.substring(0, 16).replace("T", " ");
};

const loadData = () => {
	loading.value = true;
	query.current = currentPage.value;
	getTodoList(query)
		.then(({ data }) => {
			if (data.flag) {
				todoList.value = data.data.recordList;
				total.value = data.data.count;
			}
		})
		.finally(() => (loading.value = false));
};

const handlePageChange = (page: number) => {
	currentPage.value = page;
	loadData();
};

const showAdd = () => {
	editingId.value = null;
	Object.assign(form, { title: "", description: "", priority: 1, category: "", startTime: null, endTime: null });
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
	if (!form.title.trim()) {
		window.$message?.warning("标题不能为空");
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
/* ====== Glass Morphism Theme ====== */
.toolbar {
	display: flex;
	justify-content: space-between;
	align-items: center;
	flex-wrap: wrap;
	gap: 0.75rem;
	margin-bottom: 1.25rem;
}
.btn-add {
	display: inline-flex;
	align-items: center;
	gap: 6px;
	padding: 0.55rem 1.25rem;
	border-radius: 50px;
	border: none;
	font-size: 0.85rem;
	font-weight: 600;
	cursor: pointer;
	background: linear-gradient(135deg, var(--primary-color, #6366f1), var(--primary-color-light, #818cf8));
	color: #fff;
	box-shadow: 0 4px 14px rgba(99, 102, 241, 0.3);
	transition: all 0.25s ease;
	font-family: inherit;
	&:hover {
		transform: translateY(-1px);
		box-shadow: 0 6px 20px rgba(99, 102, 241, 0.4);
	}
	&:active { transform: translateY(0); }
	&:disabled { opacity: 0.6; cursor: not-allowed; transform: none; }
}
.btn-add-icon { font-size: 1rem; }

.filters {
	display: flex;
	gap: 0.5rem;
	flex-wrap: wrap;
}

/* ====== Cards ====== */
.todo-cards {
	display: flex;
	flex-direction: column;
	gap: 0.6rem;
}
.loading-tip,
.empty-tip {
	text-align: center;
	padding: 3rem 0;
	color: var(--grey-5);
	font-size: 0.95rem;
}
.todo-card {
	display: flex;
	align-items: flex-start;
	gap: 0.85rem;
	padding: 1rem 1.25rem;
	border-radius: 16px;
	background: var(--card-bg, rgba(255, 255, 255, 0.65));
	backdrop-filter: blur(16px);
	-webkit-backdrop-filter: blur(16px);
	border: 1.5px solid var(--card-border, rgba(255, 255, 255, 0.5));
	box-shadow: 0 4px 24px rgba(99, 102, 241, 0.08), 0 1px 3px rgba(0, 0, 0, 0.06);
	color: var(--grey-7, #1e293b);
	transition: all 0.3s ease;
	&:hover {
		transform: translateY(-2px);
		box-shadow: 0 8px 32px rgba(99, 102, 241, 0.12);
	}
	&.completed {
		opacity: 0.55;
		.card-title {
			text-decoration: line-through;
			color: var(--grey-5);
		}
	}
}
.card-left {
	padding-top: 1px;
	cursor: pointer;
}
.checkbox {
	width: 22px;
	height: 22px;
	border-radius: 7px;
	border: 2px solid var(--grey-4, #ccc);
	display: flex;
	align-items: center;
	justify-content: center;
	transition: all 0.2s;
	flex-shrink: 0;
	&::after { content: ""; }
	&.checked {
		border-color: var(--primary-color, #6366f1);
		background: linear-gradient(135deg, var(--primary-color, #6366f1), var(--primary-color-light, #818cf8));
		&::after {
			content: "✓";
			color: #fff;
			font-size: 12px;
			font-weight: 700;
		}
	}
	&:hover { border-color: var(--primary-color, #6366f1); }
}
.card-body {
	flex: 1;
	min-width: 0;
}
.card-title {
	font-size: 0.92rem;
	font-weight: 600;
	margin-bottom: 0.3rem;
	line-height: 1.4;
}
.card-meta {
	display: flex;
	align-items: center;
	gap: 0.4rem;
	flex-wrap: wrap;
	font-size: 0.75rem;
}
.priority-tag {
	padding: 2px 10px;
	border-radius: 50px;
	font-size: 0.68rem;
	font-weight: 600;
	&.p0 { background: rgba(34, 197, 94, 0.12); color: #16a34a; }
	&.p1 { background: rgba(245, 158, 11, 0.12); color: #d97706; }
	&.p2 { background: rgba(239, 68, 68, 0.12); color: #ef4444; }
}
.category-tag {
	padding: 2px 10px;
	border-radius: 50px;
	background: rgba(99, 102, 241, 0.08);
	color: var(--primary-color, #6366f1);
	font-size: 0.68rem;
	font-weight: 600;
}
.time-tag {
	display: flex;
	align-items: center;
	gap: 2px;
	color: var(--grey-5);
	font-size: 0.72rem;
}
.card-desc {
	margin-top: 0.35rem;
	font-size: 0.78rem;
	color: var(--grey-5);
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}
.card-actions {
	display: flex;
	gap: 0.15rem;
	flex-shrink: 0;
}
.icon-btn {
	width: 32px;
	height: 32px;
	border-radius: 8px;
	border: none;
	background: transparent;
	cursor: pointer;
	display: flex;
	align-items: center;
	justify-content: center;
	color: var(--grey-5, #94a3b8);
	transition: all 0.2s;
	&:hover {
		background: rgba(99, 102, 241, 0.08);
		color: var(--primary-color, #6366f1);
	}
}
.icon-btn-del:hover {
	background: rgba(239, 68, 68, 0.08) !important;
	color: #ef4444 !important;
}

.pagination {
	display: flex;
	justify-content: center;
	margin-top: 1.25rem;
}

/* ====== Modal Glass ====== */
.modal-glass {
	background: var(--glass-bg-heavy);
	backdrop-filter: blur(20px);
	-webkit-backdrop-filter: blur(20px);
	border: 1.5px solid var(--glass-border);
	border-radius: 20px;
	padding: 2rem;
	width: 480px;
	max-width: calc(100vw - 2rem);
	margin: 0 auto;
	box-shadow: 0 20px 60px rgba(0, 0, 0, 0.12);
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
}
.form-row {
	display: grid;
	grid-template-columns: 1fr 1fr;
	gap: 0.75rem;
}
.modal-footer {
	display: flex;
	justify-content: flex-end;
	gap: 0.6rem;
	margin-top: 1.5rem;
}
.btn-ghost {
	background: transparent;
	border: 1.5px solid rgba(99, 102, 241, 0.2);
	color: var(--grey-6, #64748b);
	padding: 0.5rem 1.2rem;
	border-radius: 50px;
	font-size: 0.82rem;
	cursor: pointer;
	font-weight: 600;
	font-family: inherit;
	transition: all 0.2s;
	&:hover {
		border-color: var(--primary-color, #6366f1);
		color: var(--primary-color, #6366f1);
	}
}

/* ====== Responsive ====== */
@media (max-width: 767px) {
	.toolbar {
		flex-direction: column;
		align-items: stretch;
	}
	.btn-add { width: 100%; justify-content: center; }
	.filters {
		flex-wrap: wrap;
		:deep(.n-select), :deep(.n-input) {
			width: 100% !important;
			flex: 1;
			min-width: 0;
		}
	}
	.todo-card {
		padding: 0.75rem 1rem;
		gap: 0.65rem;
		border-radius: 12px;
	}
	.card-title { font-size: 0.85rem; }
	.card-desc { font-size: 0.75rem; }
	.form-row { grid-template-columns: 1fr; }
	.modal-glass { padding: 1.5rem; }
}

@media (max-width: 480px) {
	.card-meta { font-size: 0.65rem; }
	.priority-tag, .category-tag {
		font-size: 0.6rem;
		padding: 1px 7px;
	}
}
</style>
