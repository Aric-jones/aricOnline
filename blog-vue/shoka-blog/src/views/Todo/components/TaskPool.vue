<template>
	<div class="task-pool-container">
		<div class="pool-toolbar">
			<button class="btn-add" @click="showAdd">
				<span class="btn-add-icon">＋</span> 新增任务
			</button>
			<n-input
				v-model:value="keyword"
				placeholder="搜索..."
				clearable
				round
				style="width: 160px"
			/>
		</div>

		<div v-if="filteredTasks.length === 0" class="pool-empty">暂无任务，点击上方按钮添加想做的事</div>

		<div class="pool-list">
			<div
				v-for="task in filteredTasks"
				:key="task.id"
				class="pool-card"
			>
				<div class="pool-card-body" @click="showEdit(task)">
					<div class="pool-card-title">{{ task.title }}</div>
					<div v-if="task.description" class="pool-card-desc">{{ task.description }}</div>
					<div v-if="task.tags.length" class="pool-card-tags">
						<span v-for="tag in task.tags" :key="tag" class="pool-tag">{{ tag }}</span>
					</div>
				</div>
				<div class="pool-card-actions">
					<button class="pool-action-btn convert-btn" @click.stop="convertToTodo(task)" title="转为待办">📋</button>
					<button class="pool-action-btn delete-btn" @click.stop="handleDelete(task.id)" title="删除">✕</button>
				</div>
			</div>
		</div>

		<n-modal v-model:show="dialogVisible" :mask-closable="true" display-directive="if">
			<div class="modal-glass">
				<div class="modal-title">{{ editingId ? '编辑任务' : '新增任务' }}</div>
				<div class="form-group">
					<label class="form-label">标题</label>
					<n-input v-model:value="form.title" placeholder="想做什么？" round />
				</div>
				<div class="form-group">
					<label class="form-label">描述</label>
					<n-input v-model:value="form.description" type="textarea" placeholder="详细描述（可选）" :rows="3" />
				</div>
				<div class="form-group">
					<label class="form-label">标签</label>
					<div class="tag-input-row">
						<n-input v-model:value="tagInput" placeholder="输入标签后回车" round @keyup.enter="addTag" />
					</div>
					<div v-if="form.tags.length" class="tag-list">
						<span v-for="(tag, i) in form.tags" :key="i" class="pool-tag editable" @click="removeTag(i)">{{ tag }} ✕</span>
					</div>
				</div>
				<div class="modal-footer">
					<button class="btn-ghost" @click="dialogVisible = false">取消</button>
					<button class="btn-add" @click="handleSubmit">确定</button>
				</div>
			</div>
		</n-modal>
	</div>
</template>

<script setup lang="ts">
interface PoolTask {
	id: string;
	title: string;
	description: string;
	tags: string[];
	createTime: string;
}

const STORAGE_KEY = "todo-task-pool";

const keyword = ref("");
const dialogVisible = ref(false);
const editingId = ref<string | null>(null);
const tagInput = ref("");

const form = reactive({
	title: "",
	description: "",
	tags: [] as string[],
});

const tasks = ref<PoolTask[]>([]);

const loadTasks = () => {
	try {
		const raw = localStorage.getItem(STORAGE_KEY);
		tasks.value = raw ? JSON.parse(raw) : [];
	} catch { tasks.value = []; }
};

const saveTasks = () => {
	localStorage.setItem(STORAGE_KEY, JSON.stringify(tasks.value));
};

const filteredTasks = computed(() => {
	const kw = keyword.value.trim().toLowerCase();
	if (!kw) return tasks.value;
	return tasks.value.filter(
		(t) => t.title.toLowerCase().includes(kw) || t.description.toLowerCase().includes(kw) || t.tags.some((tag) => tag.toLowerCase().includes(kw))
	);
});

const showAdd = () => {
	editingId.value = null;
	form.title = "";
	form.description = "";
	form.tags = [];
	tagInput.value = "";
	dialogVisible.value = true;
};

const showEdit = (task: PoolTask) => {
	editingId.value = task.id;
	form.title = task.title;
	form.description = task.description;
	form.tags = [...task.tags];
	tagInput.value = "";
	dialogVisible.value = true;
};

const addTag = () => {
	const t = tagInput.value.trim();
	if (t && !form.tags.includes(t)) form.tags.push(t);
	tagInput.value = "";
};

const removeTag = (i: number) => { form.tags.splice(i, 1); };

const handleSubmit = () => {
	if (!form.title.trim()) {
		window.$message?.warning("标题不能为空");
		return;
	}
	if (editingId.value) {
		const idx = tasks.value.findIndex((t) => t.id === editingId.value);
		if (idx >= 0) {
			tasks.value[idx] = { ...tasks.value[idx], title: form.title, description: form.description, tags: [...form.tags] };
		}
	} else {
		tasks.value.unshift({
			id: Date.now().toString(36) + Math.random().toString(36).slice(2, 6),
			title: form.title,
			description: form.description,
			tags: [...form.tags],
			createTime: new Date().toISOString(),
		});
	}
	saveTasks();
	dialogVisible.value = false;
};

const handleDelete = (id: string) => {
	window.$dialog?.warning({
		title: "确认删除",
		content: "删除后不可恢复，确认删除？",
		positiveText: "删除",
		negativeText: "取消",
		onPositiveClick: () => {
			tasks.value = tasks.value.filter((t) => t.id !== id);
			saveTasks();
		},
	});
};

const convertToTodo = (task: PoolTask) => {
	window.$message?.info("请前往「代办列表」新增代办，标题已复制到剪贴板");
	navigator.clipboard.writeText(task.title).catch(() => {});
};

onMounted(loadTasks);
</script>

<style lang="scss" scoped>
.task-pool-container { color: var(--grey-7, #1e293b); }

.pool-toolbar {
	display: flex; justify-content: space-between; align-items: center;
	flex-wrap: wrap; gap: 0.75rem; margin-bottom: 1.25rem;

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

.btn-add {
	display: inline-flex; align-items: center; gap: 6px;
	padding: 0.55rem 1.25rem; border-radius: 50px; border: none;
	font-size: 0.85rem; font-weight: 600; cursor: pointer;
	background: linear-gradient(135deg, var(--todo-primary, #6366f1), var(--todo-primary-light, #818cf8));
	color: #fff; box-shadow: 0 4px 14px rgba(var(--todo-primary-rgb, 99,102,241), 0.3);
	transition: all 0.25s ease; font-family: inherit;
	&:hover { transform: translateY(-1px); box-shadow: 0 6px 20px rgba(var(--todo-primary-rgb, 99,102,241), 0.4); }
	&:active { transform: translateY(0); }
}
.btn-add-icon { font-size: 1rem; }

.pool-empty { text-align: center; padding: 3rem 0; color: var(--grey-5, #94a3b8); font-size: 0.9rem; }

.pool-list {
	display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 0.8rem;
}

.pool-card {
	background: var(--todo-card-bg, var(--glass-bg, rgba(255, 255, 255, 0.6)));
	backdrop-filter: blur(20px); -webkit-backdrop-filter: blur(20px);
	border: 1.5px solid var(--glass-border, rgba(255, 255, 255, 0.5));
	border-radius: 16px; padding: 1rem 1.1rem;
	box-shadow:
		var(--todo-card-glow, 0 0 0 transparent),
		0 4px 16px rgba(var(--todo-primary-rgb, 99,102,241), 0.08),
		0 1px 4px rgba(0, 0, 0, 0.04);
	display: flex; justify-content: space-between; gap: 0.5rem;
	transition: all 0.25s ease;
	&:hover {
		transform: translateY(-2px);
		box-shadow:
			var(--todo-card-glow, 0 0 0 transparent),
			0 8px 28px rgba(var(--todo-primary-rgb, 99,102,241), 0.15),
			0 2px 8px rgba(0, 0, 0, 0.06);
	}
}

.pool-card-body { flex: 1; min-width: 0; cursor: pointer; }
.pool-card-title { font-size: 0.95rem; font-weight: 700; color: var(--grey-8, #1e293b); margin-bottom: 0.3rem; }
.pool-card-desc {
	font-size: 0.8rem; color: var(--grey-5, #64748b); margin-bottom: 0.4rem;
	display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}
.pool-card-tags { display: flex; flex-wrap: wrap; gap: 0.3rem; }
.pool-tag {
	font-size: 0.68rem; padding: 2px 8px; border-radius: 50px;
	background: rgba(var(--todo-primary-rgb, 99,102,241), 0.1);
	color: var(--todo-primary, #6366f1); font-weight: 600;
	&.editable { cursor: pointer; &:hover { background: rgba(239, 68, 68, 0.1); color: #ef4444; } }
}

.pool-card-actions { display: flex; flex-direction: column; gap: 4px; flex-shrink: 0; }
.pool-action-btn {
	width: 28px; height: 28px; border-radius: 8px; border: none;
	background: transparent; cursor: pointer; font-size: 0.8rem;
	display: flex; align-items: center; justify-content: center;
	transition: background 0.15s;
}
.convert-btn:hover { background: rgba(var(--todo-primary-rgb, 99,102,241), 0.08); }
.delete-btn:hover { background: rgba(239, 68, 68, 0.08); color: #ef4444; }

// ==================== 弹窗 ====================
.modal-glass {
	background: var(--glass-bg-heavy, rgba(255, 255, 255, 0.75));
	backdrop-filter: blur(24px); -webkit-backdrop-filter: blur(24px);
	border: 1.5px solid var(--glass-border, rgba(255, 255, 255, 0.5));
	border-radius: 22px; padding: 2rem; width: 460px;
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
}

.modal-title { font-size: 1.15rem; font-weight: 700; margin-bottom: 1.5rem; color: var(--grey-8, #1e293b); }
.form-group { margin-bottom: 1rem; }
.form-label { display: block; font-size: 0.78rem; font-weight: 600; color: var(--grey-6, #64748b); margin-bottom: 0.35rem; }
.tag-input-row { margin-bottom: 0.4rem; }
.tag-list { display: flex; flex-wrap: wrap; gap: 0.3rem; }
.modal-footer { display: flex; justify-content: flex-end; gap: 0.6rem; margin-top: 1.5rem; }
.btn-ghost {
	background: var(--glass-bg, rgba(255, 255, 255, 0.45));
	backdrop-filter: blur(8px);
	border: 1.5px solid rgba(var(--todo-primary-rgb, 99,102,241), 0.15);
	color: var(--grey-6, #64748b); padding: 0.5rem 1.2rem; border-radius: 50px;
	font-size: 0.82rem; cursor: pointer; font-weight: 600; font-family: inherit;
	transition: all 0.2s;
	&:hover { border-color: var(--todo-primary, #6366f1); color: var(--todo-primary, #6366f1); }
}

@media (max-width: 767px) {
	.pool-toolbar { flex-direction: column; align-items: stretch; }
	.btn-add { width: 100%; justify-content: center; }
	.pool-list { grid-template-columns: 1fr; }
	.modal-glass { padding: 1.5rem; }
}
</style>
