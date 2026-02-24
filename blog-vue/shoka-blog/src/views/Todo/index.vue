<template>
	<div class="page-header">
		<div class="page-title">
			<h1 class="todo-page-title">我的代办</h1>
		</div>
		<img
			class="page-cover"
			src="https://big-event0611.oss-cn-beijing.aliyuncs.com/article/b01e4495e1acf8e6b0b968d99dc5886b.jpg"
			alt=""
		/>
		<Waves></Waves>
	</div>
	<div class="bg">
		<div class="todo-wrapper">
			<n-tabs v-model:value="activeTab" type="line">
				<n-tab-pane name="list" tab="代办列表">
					<TodoList />
				</n-tab-pane>
				<n-tab-pane name="calendar" tab="日历视图">
					<CalendarView />
				</n-tab-pane>
				<n-tab-pane name="gantt" tab="甘特图">
					<GanttView />
				</n-tab-pane>
				<n-tab-pane name="diary" tab="日记">
					<DiaryView />
				</n-tab-pane>
				<n-tab-pane name="habit" tab="习惯追踪">
					<HabitView />
				</n-tab-pane>
				<n-tab-pane name="ai" tab="AI 助手">
					<AiAssistant />
				</n-tab-pane>
			</n-tabs>
		</div>
	</div>
</template>

<script setup lang="ts">
import TodoList from "./components/TodoList.vue";
import CalendarView from "./components/CalendarView.vue";
import GanttView from "./components/GanttView.vue";
import DiaryView from "./components/DiaryView.vue";
import AiAssistant from "./components/AiAssistant.vue";
import HabitView from "./components/HabitView.vue";

const activeTab = ref("list");
</script>

<style lang="scss" scoped>
$primary: #6366f1;
$primary-light: #818cf8;
$primary-bg: rgba(99, 102, 241, 0.08);
$glass-input-border: rgba(99, 102, 241, 0.15);
$radius: 16px;
$radius-sm: 10px;

.todo-page-title {
	font-weight: 500;
	font-size: 2rem;
	text-align: center;
	color: var(--header-text-color);
}
.todo-wrapper {
	max-width: 72.5rem;
	margin: 0 auto;
	padding: 1.5rem 1rem;
}

// ==================== 导航栏 ====================
:deep(.n-tabs-nav) {
	background: var(--glass-bg);
	backdrop-filter: blur(16px);
	-webkit-backdrop-filter: blur(16px);
	border-radius: $radius;
	padding: 0.3rem 0.5rem;
	border: 1.5px solid var(--glass-border);
	box-shadow: var(--glass-shadow);
	margin-bottom: 1.25rem;
}
:deep(.n-tabs-tab) {
	color: var(--grey-6, #64748b) !important;
	font-weight: 500;
	border-radius: $radius-sm;
	transition: all 0.25s ease !important;
	&:hover { background: $primary-bg; }
}
:deep(.n-tabs-tab--active) { color: $primary !important; font-weight: 600; }
:deep(.n-tabs-bar) { border-radius: 2px; background-color: $primary !important; }
:deep(.n-tabs-pane-wrapper) { overflow: visible !important; }
:deep(.n-tab-pane) { padding: 0.75rem 0.5rem; overflow: visible !important; }

// ==================== Primary 按钮 ====================
:deep(.n-button--primary-type:not(.n-button--text-type):not(.n-button--ghost-type):not(.n-button--quaternary-type):not(.card-record-btn)) {
	border-radius: 50px !important;
	font-weight: 600;
	--n-color: transparent !important;
	--n-color-hover: transparent !important;
	--n-color-pressed: transparent !important;
	--n-color-focus: transparent !important;
	--n-border: 1px solid transparent !important;
	--n-border-hover: 1px solid transparent !important;
	--n-border-pressed: 1px solid transparent !important;
	--n-border-focus: 1px solid transparent !important;
	--n-text-color: #fff !important;
	--n-text-color-hover: #fff !important;
	--n-text-color-pressed: #fff !important;
	--n-text-color-focus: #fff !important;
	--n-ripple-color: rgba(99, 102, 241, 0.4) !important;
	background-image: linear-gradient(135deg, $primary, $primary-light) !important;
	box-shadow: 0 4px 14px rgba(99, 102, 241, 0.35);
	transition: all 0.25s ease !important;
	&:hover { transform: translateY(-1px); box-shadow: 0 6px 20px rgba(99, 102, 241, 0.45); }
	&:active { transform: translateY(0); }
	&:disabled { opacity: 0.6; cursor: not-allowed; transform: none !important; }
}

// ==================== Default 按钮 ====================
:deep(.n-button--default-type:not(.n-button--text-type):not(.n-button--ghost-type):not(.n-button--quaternary-type)) {
	border-radius: 50px !important;
	--n-border: 1.5px solid #{$glass-input-border} !important;
	--n-border-hover: 1.5px solid #{$primary} !important;
	--n-border-pressed: 1.5px solid #{$primary} !important;
	--n-border-focus: 1.5px solid #{$primary} !important;
	--n-color: var(--glass-bg) !important;
	--n-color-hover: var(--glass-bg) !important;
	--n-color-pressed: var(--glass-bg) !important;
	--n-text-color-hover: #{$primary} !important;
	--n-text-color-pressed: #{$primary} !important;
	backdrop-filter: blur(16px);
	font-weight: 600;
	transition: all 0.2s ease !important;
}

// ==================== Error 按钮（quaternary delete） ====================
:deep(.n-button--error-type.n-button--quaternary-type) {
	border-radius: 50px !important;
}

// ==================== 按钮组 ====================
:deep(.n-button-group .n-button) {
	border-radius: 0 !important;
	&:first-child { border-radius: 50px 0 0 50px !important; }
	&:last-child { border-radius: 0 50px 50px 0 !important; }
	&:only-child { border-radius: 50px !important; }
}

// ==================== Select ====================
:deep(.n-base-selection) {
	border-radius: 50px !important;
	.n-base-selection-label,
	.n-base-selection-tags { border-radius: 50px !important; }
	.n-base-selection__border,
	.n-base-selection__state-border { border-radius: 50px !important; }
	--n-border: 1.5px solid #{$glass-input-border} !important;
	--n-border-hover: 1.5px solid #{$primary} !important;
	--n-border-active: 1.5px solid #{$primary} !important;
	--n-border-focus: 1.5px solid #{$primary} !important;
	--n-color: var(--glass-bg) !important;
	backdrop-filter: blur(16px);
	.n-tag { border-radius: 50px !important; }
}

// ==================== Input ====================
:deep(.n-input:not(.n-input--textarea)) {
	border-radius: 50px !important;
	.n-input__border,
	.n-input__state-border,
	.n-input-wrapper { border-radius: 50px !important; }
	--n-border: 1.5px solid #{$glass-input-border} !important;
	--n-border-hover: 1.5px solid #{$primary} !important;
	--n-border-focus: 1.5px solid #{$primary} !important;
	--n-color: var(--glass-bg) !important;
	--n-color-focus: var(--glass-bg) !important;
	--n-caret-color: #{$primary} !important;
	backdrop-filter: blur(16px);
}

// ==================== DatePicker ====================
:deep(.n-date-picker .n-input) {
	border-radius: 50px !important;
	.n-input__border,
	.n-input__state-border,
	.n-input-wrapper { border-radius: 50px !important; }
	--n-border: 1.5px solid #{$glass-input-border} !important;
	--n-border-hover: 1.5px solid #{$primary} !important;
	--n-border-focus: 1.5px solid #{$primary} !important;
	--n-color: var(--glass-bg) !important;
	--n-caret-color: #{$primary} !important;
	backdrop-filter: blur(16px);
}

// ==================== InputNumber ====================
:deep(.n-input-number .n-input) {
	border-radius: $radius-sm !important;
	.n-input__border,
	.n-input__state-border,
	.n-input-wrapper { border-radius: $radius-sm !important; }
}

// ==================== Textarea ====================
:deep(.n-input--textarea) {
	--n-border: 1.5px solid #{$glass-input-border} !important;
	--n-border-hover: 1.5px solid #{$primary} !important;
	--n-border-focus: 1.5px solid #{$primary} !important;
	--n-color: var(--glass-bg) !important;
	--n-caret-color: #{$primary} !important;
	border-radius: $radius-sm !important;
	.n-input__border,
	.n-input__state-border { border-radius: $radius-sm !important; }
}

// ==================== 弹窗 ====================
:deep(.n-modal.n-card) {
	border-radius: 20px !important;
	background: var(--glass-bg-heavy) !important;
	backdrop-filter: blur(20px) !important;
	-webkit-backdrop-filter: blur(20px) !important;
	border: 1.5px solid var(--glass-border) !important;
	box-shadow: 0 20px 60px rgba(0, 0, 0, 0.12) !important;
}

// ==================== 分页 ====================
:deep(.n-pagination .n-pagination-item) {
	border-radius: $radius-sm !important;
	--n-item-border-active: 1px solid #{$primary} !important;
	--n-item-color-active: linear-gradient(135deg, #{$primary}, #{$primary-light}) !important;
}

// ==================== 响应式 ====================
@media (max-width: 767px) {
	.todo-page-title { font-size: 1.5rem; }
	.todo-wrapper { padding: 0.75rem 0.5rem; }
	:deep(.n-tabs-nav) { padding: 0.2rem 0.3rem; border-radius: 12px; }
	:deep(.n-tabs-tab) { padding: 0.5rem 0.6rem !important; font-size: 0.8rem; }
}
@media (max-width: 480px) {
	:deep(.n-tabs-tab) { padding: 0.4rem 0.4rem !important; font-size: 0.75rem; }
}
</style>
