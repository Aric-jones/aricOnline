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
			<n-tabs v-model:value="activeTab" type="line" animated>
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

const activeTab = ref("list");
</script>

<style lang="scss" scoped>
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

:deep(.n-tabs-nav) {
	background: var(--card-bg, #fff);
	border-radius: 0.5rem;
	padding: 0.25rem 0.5rem;
	box-shadow: var(--card-shadow);
	margin-bottom: 1rem;
}

:deep(.n-tabs-tab) {
	color: var(--grey-6, #666) !important;
}
:deep(.n-tabs-tab--active) {
	color: var(--primary-color, #18a058) !important;
}

:deep(.n-tab-pane) {
	padding: 0;
}

@media (max-width: 767px) {
	.todo-page-title {
		font-size: 1.5rem;
	}
	.todo-wrapper {
		padding: 0.75rem 0.5rem;
	}
	:deep(.n-tabs-nav) {
		padding: 0;
	}
	:deep(.n-tabs-tab) {
		padding: 0.5rem 0.6rem !important;
		font-size: 0.8rem;
	}
}

@media (max-width: 480px) {
	:deep(.n-tabs-tab) {
		padding: 0.4rem 0.4rem !important;
		font-size: 0.75rem;
	}
}
</style>
