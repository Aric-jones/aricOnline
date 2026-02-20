<template>
	<div class="todo-list-container">
		<!-- 顶部工具栏 -->
		<div class="toolbar">
			<n-button type="primary" @click="showAdd">
				<svg-icon icon-class="edit" size="0.9rem" style="margin-right: 4px" />
				新增代办
			</n-button>
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
					<div class="checkbox" :class="{ checked: item.status === 1 }">
						<svg-icon v-if="item.status === 1" icon-class="badge" size="1rem" />
					</div>
				</div>
				<div class="card-body">
					<div class="card-title">{{ item.title }}</div>
					<div class="card-meta">
						<span class="priority-tag" :class="'p' + item.priority">
							{{ priorityLabel(item.priority) }}
						</span>
						<span v-if="item.category" class="category-tag">{{ item.category }}</span>
						<span v-if="item.endTime" class="time-tag">
							<svg-icon icon-class="clock" size="0.75rem" />
							{{ formatShortDate(item.endTime) }}
						</span>
					</div>
					<div v-if="item.description" class="card-desc">{{ item.description }}</div>
				</div>
				<div class="card-actions">
					<n-button text @click="showEdit(item)">
						<svg-icon icon-class="edit" size="0.9rem" />
					</n-button>
					<n-button text @click="handleDelete(item.id)">
						<svg-icon icon-class="delete" size="0.9rem" />
					</n-button>
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
		<n-modal v-model:show="dialogVisible" preset="card" :title="editingId ? '编辑代办' : '新增代办'" style="max-width: 500px; width: calc(100vw - 2rem)" :mask-closable="true" display-directive="if">
			<n-form label-placement="top">
				<n-form-item label="标题" required>
					<n-input v-model:value="form.title" placeholder="请输入代办标题" />
				</n-form-item>
				<n-form-item label="描述">
					<n-input v-model:value="form.description" type="textarea" placeholder="详细描述（可选）" :rows="3" />
				</n-form-item>
			<n-grid :cols="formCols" :x-gap="12">
				<n-grid-item>
					<n-form-item label="优先级">
						<n-select v-model:value="form.priority" :options="priorityOptions" />
					</n-form-item>
				</n-grid-item>
				<n-grid-item>
					<n-form-item label="分类">
						<n-input v-model:value="form.category" placeholder="分类标签" />
					</n-form-item>
				</n-grid-item>
			</n-grid>
			<n-grid :cols="formCols" :x-gap="12">
				<n-grid-item>
					<n-form-item label="开始时间">
						<n-date-picker v-model:formatted-value="form.startTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" clearable style="width:100%" />
					</n-form-item>
				</n-grid-item>
				<n-grid-item>
					<n-form-item label="截止时间">
						<n-date-picker v-model:formatted-value="form.endTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" clearable style="width:100%" />
					</n-form-item>
				</n-grid-item>
			</n-grid>
			</n-form>
			<template #action>
				<n-button @click="dialogVisible = false">取消</n-button>
				<n-button type="primary" @click="handleSubmit" :loading="submitting">确定</n-button>
			</template>
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
.toolbar {
	display: flex;
	justify-content: space-between;
	align-items: center;
	flex-wrap: wrap;
	gap: 0.75rem;
	margin-bottom: 1rem;
}
.filters {
	display: flex;
	gap: 0.5rem;
	flex-wrap: wrap;
}
.todo-cards {
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
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
	gap: 0.75rem;
	padding: 0.75rem 1rem;
	border-radius: 0.5rem;
	background: var(--card-bg, #fff);
	box-shadow: var(--card-shadow);
	color: var(--grey-7, #333);
	transition: all 0.2s;
	&.completed {
		opacity: 0.6;
		.card-title {
			text-decoration: line-through;
			color: var(--grey-5);
		}
	}
}
.card-left {
	padding-top: 2px;
	cursor: pointer;
}
.checkbox {
	width: 1.25rem;
	height: 1.25rem;
	border-radius: 50%;
	border: 2px solid var(--grey-4);
	display: flex;
	align-items: center;
	justify-content: center;
	transition: all 0.2s;
	&.checked {
		border-color: var(--primary-color);
		background: var(--primary-color);
		color: #fff;
	}
}
.card-body {
	flex: 1;
	min-width: 0;
}
.card-title {
	font-size: 0.95rem;
	font-weight: 500;
	margin-bottom: 0.25rem;
}
.card-meta {
	display: flex;
	align-items: center;
	gap: 0.4rem;
	flex-wrap: wrap;
	font-size: 0.75rem;
}
.priority-tag {
	padding: 1px 6px;
	border-radius: 3px;
	font-size: 0.7rem;
	color: #fff;
	&.p0 { background: #8bc34a; }
	&.p1 { background: #ff9800; }
	&.p2 { background: #f44336; }
}
.category-tag {
	padding: 1px 6px;
	border-radius: 3px;
	background: rgba(64, 158, 255, 0.15);
	color: #409eff;
	font-size: 0.7rem;
}
.time-tag {
	display: flex;
	align-items: center;
	gap: 2px;
	color: var(--grey-5);
}
.card-desc {
	margin-top: 0.3rem;
	font-size: 0.8rem;
	color: var(--grey-5);
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}
.card-actions {
	display: flex;
	gap: 0.25rem;
	flex-shrink: 0;
}
.pagination {
	display: flex;
	justify-content: center;
	margin-top: 1rem;
}

@media (max-width: 767px) {
	.toolbar {
		flex-direction: column;
		align-items: stretch;
	}
	.filters {
		flex-wrap: wrap;
		:deep(.n-select), :deep(.n-input) {
			width: 100% !important;
			flex: 1;
			min-width: 0;
		}
	}
	.todo-card {
		padding: 0.6rem 0.75rem;
		gap: 0.5rem;
	}
	.card-title {
		font-size: 0.85rem;
	}
	.card-desc {
		font-size: 0.75rem;
	}
}

@media (max-width: 480px) {
	.card-meta {
		font-size: 0.65rem;
	}
	.priority-tag, .category-tag {
		font-size: 0.6rem;
		padding: 0 4px;
	}
}
</style>
