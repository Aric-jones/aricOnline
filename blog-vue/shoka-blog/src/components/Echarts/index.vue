<template>
	<div ref="chartDom" :style="{ width: width, height: height }"></div>
</template>

<script setup lang="ts">
import { useResizeObserver } from "@vueuse/core";
import * as echarts from "echarts";
// 模板引用
const chartDom = ref<HTMLElement>();
const myChart = ref<echarts.EChartsType>();
/**
 * defineProps用来定义组件入参的类型
 * const props = defineProps({
 *   参数字段名: {
 *     type: 类型,        // 规定参数的类型（必填）
 *     default: 默认值,   // 参数默认值（可选）
 *     required: 布尔值   // 是否必传（可选，默认false）
 *   }
 * });
 */
const props = defineProps({
	options: {
		type: Object,
		default: {},
		required: true,
	},
	width: {
		type: String,
		default: "100%",
	},
	height: {
		type: String,
		default: "400px",
	},
});
// 监听 options 变化，用 notMerge 全量替换以便主题色等立即生效
watch(
	() => props.options,
	(newOptions) => {
		if (newOptions && Object.keys(newOptions).length) {
			myChart.value?.setOption(newOptions, true);
		}
	},
	{ deep: true }
);

onMounted(() => {
	myChart.value = echarts.init(chartDom.value!, "");
	myChart.value.setOption(props.options, true);
	useResizeObserver(chartDom.value, () => {
		myChart.value?.resize();
	});
});
</script>

<style scoped></style>
