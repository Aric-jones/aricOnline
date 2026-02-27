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
			<!-- 主题色切换（单按钮循环） -->
			<button
				class="accent-cycle-btn"
				:style="{ background: currentAccentColor }"
				:title="'切换主题色：' + currentAccentLabel"
				@click="cycleAccent"
			></button>
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
import { useAccentColor } from "@/composables/useAccentColor";

const activeTab = ref("list");
const { accent, cycleAccent, currentAccentColor, currentAccentLabel } = useAccentColor();
</script>

<style lang="scss" scoped>
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
	padding: 1rem 1rem;
	position: relative;
}

// ==================== 主题色切换按钮 ====================
.accent-cycle-btn {
	position: absolute; top: 4.75rem; right: 2.5rem;
	width: 20px; height: 20px; border-radius: 50%;
	border: 2px solid rgba(255, 255, 255, 0.7);
	cursor: pointer; z-index: 5;
	box-shadow: 0 2px 10px rgba(0, 0, 0, 0.12);
	transition: all 0.3s ease;
	&:hover { transform: scale(1.15); box-shadow: 0 3px 14px rgba(0, 0, 0, 0.18); }
	&:active { transform: scale(0.95); }
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
	&:hover { background: rgba(var(--todo-primary-rgb, 99,102,241), 0.08); }
}
:deep(.n-tabs-tab--active) { color: var(--todo-primary, #6366f1) !important; font-weight: 600; }
:deep(.n-tabs-bar) { border-radius: 2px; background-color: var(--todo-primary, #6366f1) !important; }
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
	--n-ripple-color: rgba(var(--todo-primary-rgb, 99,102,241), 0.4) !important;
	background-image: linear-gradient(135deg, var(--todo-primary, #6366f1), var(--todo-primary-light, #818cf8)) !important;
	box-shadow: 0 4px 14px rgba(var(--todo-primary-rgb, 99,102,241), 0.35);
	transition: all 0.25s ease !important;
	&:hover { transform: translateY(-1px); box-shadow: 0 6px 20px rgba(var(--todo-primary-rgb, 99,102,241), 0.45); }
	&:active { transform: translateY(0); }
	&:disabled { opacity: 0.6; cursor: not-allowed; transform: none !important; }
}

// ==================== Default 按钮 ====================
:deep(.n-button--default-type:not(.n-button--text-type):not(.n-button--ghost-type):not(.n-button--quaternary-type)) {
	border-radius: 50px !important;
	--n-border: 1.5px solid rgba(var(--todo-primary-rgb, 99,102,241), 0.15) !important;
	--n-border-hover: 1.5px solid var(--todo-primary, #6366f1) !important;
	--n-border-pressed: 1.5px solid var(--todo-primary, #6366f1) !important;
	--n-border-focus: 1.5px solid var(--todo-primary, #6366f1) !important;
	--n-color: var(--glass-bg) !important;
	--n-color-hover: var(--glass-bg) !important;
	--n-color-pressed: var(--glass-bg) !important;
	--n-text-color-hover: var(--todo-primary, #6366f1) !important;
	--n-text-color-pressed: var(--todo-primary, #6366f1) !important;
	backdrop-filter: blur(16px);
	font-weight: 600;
	transition: all 0.2s ease !important;
}

// ==================== Error 按钮 ====================
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
	--n-border: 1.5px solid rgba(var(--todo-primary-rgb, 99,102,241), 0.15) !important;
	--n-border-hover: 1.5px solid var(--todo-primary, #6366f1) !important;
	--n-border-active: 1.5px solid var(--todo-primary, #6366f1) !important;
	--n-border-focus: 1.5px solid var(--todo-primary, #6366f1) !important;
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
	--n-border: 1.5px solid rgba(var(--todo-primary-rgb, 99,102,241), 0.15) !important;
	--n-border-hover: 1.5px solid var(--todo-primary, #6366f1) !important;
	--n-border-focus: 1.5px solid var(--todo-primary, #6366f1) !important;
	--n-color: var(--glass-bg) !important;
	--n-color-focus: var(--glass-bg) !important;
	--n-caret-color: var(--todo-primary, #6366f1) !important;
	backdrop-filter: blur(16px);
}

// ==================== DatePicker ====================
:deep(.n-date-picker .n-input) {
	border-radius: 50px !important;
	.n-input__border,
	.n-input__state-border,
	.n-input-wrapper { border-radius: 50px !important; }
	--n-border: 1.5px solid rgba(var(--todo-primary-rgb, 99,102,241), 0.15) !important;
	--n-border-hover: 1.5px solid var(--todo-primary, #6366f1) !important;
	--n-border-focus: 1.5px solid var(--todo-primary, #6366f1) !important;
	--n-color: var(--glass-bg) !important;
	--n-caret-color: var(--todo-primary, #6366f1) !important;
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
	--n-border: 1.5px solid rgba(var(--todo-primary-rgb, 99,102,241), 0.15) !important;
	--n-border-hover: 1.5px solid var(--todo-primary, #6366f1) !important;
	--n-border-focus: 1.5px solid var(--todo-primary, #6366f1) !important;
	--n-color: var(--glass-bg) !important;
	--n-caret-color: var(--todo-primary, #6366f1) !important;
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
	--n-item-border-active: 1px solid var(--todo-primary, #6366f1) !important;
	--n-item-color-active: var(--todo-primary, #6366f1) !important;
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

<!-- 日期/时间选择器弹出面板（teleport 到 body，需要非 scoped 全局样式） -->
<style lang="scss">
// 面板使用 CSS 变量引用主题色，切换主题色时自动生效
.n-date-panel {
	border-radius: 16px !important;
	background: var(--glass-bg-heavy, rgba(255, 255, 255, 0.8)) !important;
	backdrop-filter: blur(24px) !important;
	-webkit-backdrop-filter: blur(24px) !important;
	border: 1.5px solid var(--glass-border, rgba(255, 255, 255, 0.5)) !important;
	box-shadow: 0 12px 48px rgba(var(--todo-primary-rgb, 99,102,241), 0.12), 0 4px 12px rgba(0, 0, 0, 0.06) !important;
	overflow: hidden;
	z-index: 9999 !important;

	// 覆盖 Naive UI 内部 CSS 变量（日期选中颜色）
	--n-item-color-active: var(--todo-primary, #6366f1) !important;
	--n-item-color-hover: rgba(var(--todo-primary-rgb, 99,102,241), 0.08) !important;
	--n-item-text-color-active: #fff !important;
	--n-item-border-radius: 10px !important;
	--n-panel-action-padding: 8px 12px !important;

	.n-date-panel-calendar .n-date-panel-month__month-year {
		border-radius: 8px;
		&:hover { background: rgba(var(--todo-primary-rgb, 99,102,241), 0.06); }
	}

	.n-date-panel-dates .n-date-panel-date {
		border-radius: 10px !important;
		transition: all 0.15s ease;

		&:hover:not(.n-date-panel-date--selected) {
			background: rgba(var(--todo-primary-rgb, 99,102,241), 0.08) !important;
		}
		&.n-date-panel-date--selected,
		&.n-date-panel-date--covered {
			background: linear-gradient(135deg, var(--todo-primary, #6366f1), var(--todo-primary-light, #818cf8)) !important;
			color: #fff !important;
			.n-date-panel-date__trigger { color: #fff !important; }
		}
		&.n-date-panel-date--current {
			.n-date-panel-date__trigger { color: var(--todo-primary, #6366f1); font-weight: 700; }
			&::after {
				border-color: var(--todo-primary, #6366f1) !important;
				border-radius: 10px !important;
			}
		}
		&.n-date-panel-date--current.n-date-panel-date--selected {
			.n-date-panel-date__trigger { color: #fff !important; }
		}
	}

	// Naive UI 内部 sup 元素（当天标记点）
	.n-date-panel-date__sup { background: var(--todo-primary, #6366f1) !important; }

	.n-date-panel-header {
		border-bottom: 1px solid rgba(var(--todo-primary-rgb, 99,102,241), 0.1);
	}

	.n-date-panel-actions {
		border-top: 1px solid rgba(var(--todo-primary-rgb, 99,102,241), 0.1);
		background: transparent !important;
		.n-button--primary-type {
			border-radius: 50px !important;
			background-image: linear-gradient(135deg, var(--todo-primary, #6366f1), var(--todo-primary-light, #818cf8)) !important;
			border: none !important;
			font-weight: 600;
			--n-color: var(--todo-primary) !important;
			--n-color-hover: var(--todo-primary-light) !important;
			--n-color-pressed: var(--todo-primary) !important;
		}
		.n-button--default-type { border-radius: 50px !important; }
	}

	.n-date-panel-month,
	.n-date-panel-year {
		.n-date-panel-month-calendar__picker-col-item {
			border-radius: 8px !important;
			&--selected,
			&.n-date-panel-month-calendar__picker-col-item--selected {
				background: linear-gradient(135deg, var(--todo-primary, #6366f1), var(--todo-primary-light, #818cf8)) !important;
				color: #fff !important;
			}
			&:hover:not(.n-date-panel-month-calendar__picker-col-item--selected) {
				background: rgba(var(--todo-primary-rgb, 99,102,241), 0.08) !important;
			}
		}
	}

	.n-date-panel-date-input .n-input {
		border-radius: 10px !important;
		--n-color: var(--glass-bg, rgba(255,255,255,0.45)) !important;
		--n-border: 1.5px solid rgba(var(--todo-primary-rgb, 99,102,241), 0.15) !important;
		--n-border-hover: 1.5px solid var(--todo-primary, #6366f1) !important;
		--n-border-focus: 1.5px solid var(--todo-primary, #6366f1) !important;
		.n-input__border,
		.n-input__state-border,
		.n-input-wrapper { border-radius: 10px !important; }
	}
}

// 时间选择器面板
.n-time-picker-panel {
	border-radius: 14px !important;
	background: var(--glass-bg-heavy, rgba(255, 255, 255, 0.88)) !important;
	backdrop-filter: blur(24px) !important;
	-webkit-backdrop-filter: blur(24px) !important;
	border: 1.5px solid var(--glass-border, rgba(255, 255, 255, 0.5)) !important;
	box-shadow: 0 12px 48px rgba(var(--todo-primary-rgb, 99,102,241), 0.12), 0 4px 12px rgba(0, 0, 0, 0.06) !important;
	--n-item-width: 44px !important;
	.n-time-picker-col {
		.n-time-picker-col__item--active {
			color: var(--todo-primary, #6366f1) !important;
			font-weight: 700;
		}
	}

	.n-button--primary-type {
		border-radius: 50px !important;
		background-image: linear-gradient(135deg, var(--todo-primary, #6366f1), var(--todo-primary-light, #818cf8)) !important;
		border: none !important;
		font-weight: 600;
		--n-color: var(--todo-primary) !important;
		--n-color-hover: var(--todo-primary-light) !important;
	}
}

// Select 下拉面板毛玻璃
.n-base-select-menu {
	border-radius: 14px !important;
	background: var(--glass-bg-heavy, rgba(255, 255, 255, 0.88)) !important;
	backdrop-filter: blur(24px) !important;
	-webkit-backdrop-filter: blur(24px) !important;
	border: 1.5px solid var(--glass-border, rgba(255, 255, 255, 0.5)) !important;
	box-shadow: 0 12px 48px rgba(var(--todo-primary-rgb, 99,102,241), 0.12), 0 4px 12px rgba(0, 0, 0, 0.06) !important;
	overflow: hidden;
	--n-color: transparent !important;

	.n-base-select-menu__wrapper {
		background: transparent !important;
	}

	.n-virtual-list {
		background: transparent !important;
	}

	// Naive UI 通过 ::before 伪元素画选项背景，用以下三个 CSS 变量：
	// --n-option-color-pending        → 悬停态 ::before 背景
	// --n-option-color-active         → 选中态 ::before 背景
	// --n-option-color-active-pending  → 选中+悬停 ::before 背景（关键！不设就会叠加默认蓝色）
	.n-base-select-option {
		border-radius: 8px !important;
		margin: 2px 6px !important;
		padding: 0.5rem 0.75rem !important;
		transition: all 0.15s ease;
		--n-option-color-pending: rgba(var(--todo-primary-rgb, 99,102,241), 0.08) !important;
		--n-option-color-active: var(--todo-primary, #6366f1) !important;
		--n-option-color-active-pending: var(--todo-primary, #6366f1) !important;
		--n-option-text-color-active: #fff !important;
		--n-option-check-color: #fff !important;
	}
}

// Naive UI preset="card" 弹窗毛玻璃
// preset="card" 时 .n-card 本身就是 .n-modal（同一个元素）
.n-card.n-modal {
	border-radius: 22px !important;
	background: var(--glass-bg-heavy, rgba(255, 255, 255, 0.82)) !important;
	backdrop-filter: blur(24px) !important;
	-webkit-backdrop-filter: blur(24px) !important;
	border: 1.5px solid var(--glass-border, rgba(255, 255, 255, 0.5)) !important;
	box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1), 0 4px 16px rgba(var(--todo-primary-rgb, 99,102,241), 0.08) !important;
	overflow: hidden;

	.n-card-header {
		border-bottom: 1px solid rgba(var(--todo-primary-rgb, 99,102,241), 0.08);
		.n-card-header__main { font-weight: 700; }
	}

	.n-card__action {
		border-top: 1px solid rgba(var(--todo-primary-rgb, 99,102,241), 0.08);
		background: transparent !important;
	}

	.n-input:not(.n-input--textarea) {
		border-radius: 50px !important;
		--n-color: var(--glass-bg, rgba(255, 255, 255, 0.45)) !important;
		--n-color-focus: var(--glass-bg, rgba(255, 255, 255, 0.55)) !important;
		--n-border: 1.5px solid rgba(var(--todo-primary-rgb, 99,102,241), 0.15) !important;
		--n-border-hover: 1.5px solid var(--todo-primary, #6366f1) !important;
		--n-border-focus: 1.5px solid var(--todo-primary, #6366f1) !important;
		--n-caret-color: var(--todo-primary, #6366f1) !important;
		.n-input__border, .n-input__state-border, .n-input-wrapper { border-radius: 50px !important; }
	}

	.n-input--textarea {
		border-radius: 12px !important;
		--n-color: var(--glass-bg, rgba(255, 255, 255, 0.45)) !important;
		--n-color-focus: var(--glass-bg, rgba(255, 255, 255, 0.55)) !important;
		--n-border: 1.5px solid rgba(var(--todo-primary-rgb, 99,102,241), 0.15) !important;
		--n-border-hover: 1.5px solid var(--todo-primary, #6366f1) !important;
		--n-border-focus: 1.5px solid var(--todo-primary, #6366f1) !important;
		--n-caret-color: var(--todo-primary, #6366f1) !important;
		.n-input__border, .n-input__state-border, .n-input-wrapper { border-radius: 12px !important; }
	}

	.n-input-number .n-input {
		border-radius: 50px !important;
		--n-color: var(--glass-bg, rgba(255, 255, 255, 0.45)) !important;
		--n-border: 1.5px solid rgba(var(--todo-primary-rgb, 99,102,241), 0.15) !important;
		--n-border-hover: 1.5px solid var(--todo-primary, #6366f1) !important;
		--n-border-focus: 1.5px solid var(--todo-primary, #6366f1) !important;
		.n-input__border, .n-input__state-border, .n-input-wrapper { border-radius: 50px !important; }
	}

	.n-base-selection {
		border-radius: 50px !important;
		--n-border: 1.5px solid rgba(var(--todo-primary-rgb, 99,102,241), 0.15) !important;
		--n-border-hover: 1.5px solid var(--todo-primary, #6366f1) !important;
		--n-border-focus: 1.5px solid var(--todo-primary, #6366f1) !important;
		--n-color: var(--glass-bg, rgba(255, 255, 255, 0.45)) !important;
		--n-color-active: var(--glass-bg, rgba(255, 255, 255, 0.55)) !important;
		--n-color-focus: var(--glass-bg, rgba(255, 255, 255, 0.55)) !important;
		.n-base-selection-label, .n-base-selection-tags { border-radius: 50px !important; }
		.n-base-selection__border, .n-base-selection__state-border { border-radius: 50px !important; }
	}

	.n-date-picker .n-input {
		border-radius: 50px !important;
		--n-color: var(--glass-bg, rgba(255, 255, 255, 0.45)) !important;
		--n-border: 1.5px solid rgba(var(--todo-primary-rgb, 99,102,241), 0.15) !important;
		--n-border-hover: 1.5px solid var(--todo-primary, #6366f1) !important;
		--n-border-focus: 1.5px solid var(--todo-primary, #6366f1) !important;
		.n-input__border, .n-input__state-border, .n-input-wrapper { border-radius: 50px !important; }
	}

	.n-button--primary-type {
		border-radius: 50px !important;
		background-image: linear-gradient(135deg, var(--todo-primary, #6366f1), var(--todo-primary-light, #818cf8)) !important;
		border: none !important;
		font-weight: 600;
		--n-color: var(--todo-primary) !important;
		--n-color-hover: var(--todo-primary-light) !important;
		--n-color-pressed: var(--todo-primary) !important;
	}
	.n-button--default-type {
		border-radius: 50px !important;
		--n-border: 1.5px solid rgba(var(--todo-primary-rgb, 99,102,241), 0.25) !important;
		--n-border-hover: 1.5px solid var(--todo-primary, #6366f1) !important;
		--n-border-pressed: 1.5px solid var(--todo-primary, #6366f1) !important;
		--n-border-focus: 1.5px solid var(--todo-primary, #6366f1) !important;
		--n-text-color: var(--grey-7, #333) !important;
		--n-text-color-hover: var(--todo-primary, #6366f1) !important;
		--n-text-color-pressed: var(--todo-primary, #6366f1) !important;
		--n-text-color-focus: var(--todo-primary, #6366f1) !important;
	}
}
</style>
