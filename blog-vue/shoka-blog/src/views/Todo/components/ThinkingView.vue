<template>
	<div class="thinking-container">
		<!-- 顶部操作栏 -->
		<div class="thinking-toolbar">
			<n-input
				v-model:value="keyword"
				placeholder="搜索主题 / 收获 / 备注..."
				clearable
				size="small"
				style="max-width: 280px"
				@update:value="debouncedSearch"
			>
				<template #prefix>
					<svg-icon icon-class="search" size="0.9rem" />
				</template>
			</n-input>
			<n-button type="primary" @click="openAdd">
				<template #icon><svg-icon icon-class="edit" size="0.9rem" /></template>
				记录思考
			</n-button>
		</div>

		<!-- 空状态 -->
		<div v-if="!loading && list.length === 0" class="thinking-empty">
			<div class="empty-icon">💡</div>
			<p>还没有思考记录</p>
			<p class="empty-sub">点击「记录思考」开始沉淀你的想法</p>
		</div>

		<!-- 列表 -->
		<div v-else class="thinking-list">
			<div
				v-for="item in list"
				:key="item.id"
				class="thinking-card"
			>
				<div class="card-header">
					<div class="card-topic">
						<span class="topic-icon">🧠</span>
						<span class="topic-text">{{ item.topic }}</span>
					</div>
					<div class="card-actions">
						<n-button text size="small" @click="openEdit(item)">
							<svg-icon icon-class="edit" size="0.85rem" />
						</n-button>
						<n-button text size="small" @click="handleDelete(item.id)">
							<svg-icon icon-class="delete" size="0.85rem" style="color: #ef4444" />
						</n-button>
					</div>
				</div>
				<div class="card-body">
					<div class="harvest-section">
						<span class="section-label">💎 收获</span>
						<div class="section-content" v-html="renderContent(item.harvest)"></div>
					</div>
					<div v-if="item.remark" class="remark-section">
						<span class="section-label">📝 备注</span>
						<div class="section-content remark-text">{{ item.remark }}</div>
					</div>
				</div>
				<div class="card-footer">
					<span class="time-text">{{ formatTime(item.createTime) }}</span>
					<span v-if="item.updateTime !== item.createTime" class="update-text">
						编辑于 {{ formatTime(item.updateTime) }}
					</span>
				</div>
			</div>
		</div>

		<!-- 新增/编辑弹窗 -->
		<n-modal
			v-model:show="showModal"
			preset="card"
			:title="isEdit ? '编辑思考' : '记录思考'"
			style="max-width: 560px"
			:mask-closable="false"
		>
			<div class="form-group">
				<label class="form-label required">思考主题</label>
				<n-input
					v-model:value="form.topic"
					placeholder="你在思考什么问题？"
					maxlength="100"
					show-count
				/>
			</div>
			<div class="form-group">
				<label class="form-label required">收获</label>
				<n-input
					v-model:value="form.harvest"
					type="textarea"
					placeholder="通过思考，你得到了什么结论或启发？"
					:rows="6"
					maxlength="2000"
					show-count
				/>
			</div>
			<div class="form-group">
				<label class="form-label">备注</label>
				<n-input
					v-model:value="form.remark"
					type="textarea"
					placeholder="补充说明（可选）"
					:rows="3"
					maxlength="500"
					show-count
				/>
			</div>
			<template #action>
				<n-button @click="showModal = false">取消</n-button>
				<n-button type="primary" :loading="submitting" @click="handleSubmit">
					{{ isEdit ? '保存修改' : '记录' }}
				</n-button>
			</template>
		</n-modal>
	</div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { getThinkingList, addThinking, updateThinking, deleteThinking } from "@/api/todo";
import type { ThinkingItem, ThinkingReq } from "@/api/todo/types";

const list = ref<ThinkingItem[]>([]);
const loading = ref(false);
const keyword = ref("");
const showModal = ref(false);
const isEdit = ref(false);
const submitting = ref(false);

const defaultForm = (): ThinkingReq => ({ topic: "", harvest: "", remark: "" });
const form = ref<ThinkingReq>(defaultForm());

let searchTimer: ReturnType<typeof setTimeout> | null = null;
function debouncedSearch() {
	if (searchTimer) clearTimeout(searchTimer);
	searchTimer = setTimeout(() => loadList(), 350);
}

async function loadList() {
	loading.value = true;
	try {
		const res = await getThinkingList(keyword.value || undefined);
		list.value = res.data.data || [];
	} finally {
		loading.value = false;
	}
}

function openAdd() {
	isEdit.value = false;
	form.value = defaultForm();
	showModal.value = true;
}

function openEdit(item: ThinkingItem) {
	isEdit.value = true;
	form.value = { id: item.id, topic: item.topic, harvest: item.harvest, remark: item.remark };
	showModal.value = true;
}

async function handleSubmit() {
	if (!form.value.topic.trim()) {
		window.$message?.warning("思考主题不能为空");
		return;
	}
	if (!form.value.harvest.trim()) {
		window.$message?.warning("收获不能为空");
		return;
	}
	submitting.value = true;
	try {
		if (isEdit.value) {
			await updateThinking(form.value);
			window.$message?.success("修改成功");
		} else {
			await addThinking(form.value);
			window.$message?.success("记录成功");
		}
		showModal.value = false;
		await loadList();
	} finally {
		submitting.value = false;
	}
}

async function handleDelete(id: number) {
	window.$dialog?.warning({
		title: "确认删除",
		content: "确定要删除这条思考记录吗？",
		positiveText: "删除",
		negativeText: "取消",
		onPositiveClick: async () => {
			await deleteThinking(id);
			window.$message?.success("已删除");
			await loadList();
		},
	});
}

function formatTime(time: string) {
	if (!time) return "";
	const d = new Date(time);
	const pad = (n: number) => String(n).padStart(2, "0");
	return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

function renderContent(text: string) {
	if (!text) return "";
	return text.replace(/\n/g, "<br>");
}

onMounted(() => loadList());
</script>

<style lang="scss" scoped>
.thinking-container {
	max-width: 800px;
	margin: 0 auto;
}

.thinking-toolbar {
	display: flex;
	justify-content: space-between;
	align-items: center;
	gap: 1rem;
	margin-bottom: 1.25rem;
	flex-wrap: wrap;
}

.thinking-empty {
	text-align: center;
	padding: 3rem 1rem;
	.empty-icon { font-size: 3rem; margin-bottom: 0.75rem; }
	p { color: var(--grey-6, #64748b); margin: 0.25rem 0; }
	.empty-sub { font-size: 0.85rem; opacity: 0.7; }
}

.thinking-list {
	display: flex;
	flex-direction: column;
	gap: 1rem;
}

.thinking-card {
	background: var(--glass-bg, rgba(255, 255, 255, 0.6));
	backdrop-filter: blur(16px);
	-webkit-backdrop-filter: blur(16px);
	border: 1.5px solid var(--glass-border, rgba(255, 255, 255, 0.4));
	border-radius: 16px;
	padding: 1.25rem;
	box-shadow: var(--todo-card-glow, 0 0 24px rgba(16, 185, 129, 0.06));
	transition: all 0.3s ease;
	&:hover {
		transform: translateY(-2px);
		box-shadow: 0 8px 32px rgba(var(--todo-primary-rgb, 16,185,129), 0.12);
	}
}

.card-header {
	display: flex;
	justify-content: space-between;
	align-items: flex-start;
	margin-bottom: 0.75rem;
}

.card-topic {
	display: flex;
	align-items: center;
	gap: 0.5rem;
	flex: 1;
	min-width: 0;
	.topic-icon { font-size: 1.3rem; flex-shrink: 0; }
	.topic-text {
		font-size: 1.1rem;
		font-weight: 600;
		color: var(--grey-8, #1e293b);
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}
}

.card-actions {
	display: flex;
	gap: 0.25rem;
	flex-shrink: 0;
	opacity: 0.5;
	transition: opacity 0.2s;
	.thinking-card:hover & { opacity: 1; }
}

.card-body {
	display: flex;
	flex-direction: column;
	gap: 0.75rem;
}

.harvest-section, .remark-section {
	.section-label {
		display: inline-block;
		font-size: 0.8rem;
		font-weight: 600;
		color: var(--todo-primary, #10b981);
		margin-bottom: 0.35rem;
	}
	.section-content {
		font-size: 0.92rem;
		line-height: 1.65;
		color: var(--grey-7, #334155);
		padding-left: 0.25rem;
	}
}

.remark-text {
	opacity: 0.75;
	font-size: 0.85rem !important;
}

.card-footer {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-top: 0.75rem;
	padding-top: 0.6rem;
	border-top: 1px solid rgba(var(--todo-primary-rgb, 16,185,129), 0.08);
	font-size: 0.78rem;
	color: var(--grey-5, #94a3b8);
}

.form-group {
	margin-bottom: 1rem;
}
.form-label {
	display: block;
	font-weight: 600;
	font-size: 0.88rem;
	margin-bottom: 0.4rem;
	color: var(--grey-7, #334155);
	&.required::after {
		content: " *";
		color: #ef4444;
	}
}

@media (max-width: 767px) {
	.thinking-toolbar {
		flex-direction: column;
		align-items: stretch;
	}
	.thinking-card { padding: 1rem; }
	.card-topic .topic-text { font-size: 1rem; }
}
</style>
