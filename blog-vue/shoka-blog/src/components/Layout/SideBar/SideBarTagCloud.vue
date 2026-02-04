<template>
	<div class="side-card">
		<div class="tag-title">
			<svg-icon icon-class="tag" size="1.1875rem"></svg-icon>
			标签
		</div>
		<div class="cloud_wrap">
			<div class="tagcloud-all" ref="cloudRef">
				<router-link
					v-for="item in tagListWithColor"
					:key="item.id"
					class="tag"
					:to="`/tag/${item.id}`"
					:style="{ color: item.color, top: '0', left: '0', filter: 'none' }"
				>
					{{ item.tagName }}
				</router-link>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { getTagList } from "@/api/tag";
import type { Tag } from "@/api/tag/types";
import { nextTick, onMounted, onUnmounted, reactive, ref, computed } from "vue";

// 定义带颜色的标签类型
type TagWithColor = Tag & { color: string };

// 原始标签列表（数据永久保留，刷新不重置）
const tagList = ref<Tag[]>([]);
// 标签云容器Ref
const cloudRef = ref<HTMLElement | null>(null);

// 生成随机0-255数值（用于RGB颜色）
function getRandomNum() {
	return Math.floor(Math.random() * 256);
}

// 按标签ID缓存颜色（页面刷新/重渲染均不变，永久固定）
const colorById = ref<Record<number, string>>({});
// 计算带固定颜色的标签列表（数据+颜色均不变）
const tagListWithColor = computed<TagWithColor[]>(() =>
	tagList.value.map((item) => ({
		...item,
		// 永久使用首次生成的颜色，无则新建
		color:
			colorById.value[item.id] ||
			`rgb(${getRandomNum()},${getRandomNum()},${getRandomNum()})`,
	}))
);

// 球体配置：不跟鼠标，只绕单轴匀速自转，保持圆形
const config = reactive({
	radius: 85,
	dtr: Math.PI / 180,
	d: 220,
	mcList: [] as Array<{
		cx: number;
		cy: number;
		cz: number;
		initialCx: number;
		initialCy: number;
		initialCz: number;
		x?: number;
		y?: number;
		scale?: number;
		alpha?: number;
		offsetWidth?: number;
		offsetHeight?: number;
	}>,
	/** 当前旋转角度（度），sineCosine 内部会乘 dtr 转弧度 */
	angle: 0,
	/** 角速度（度/帧），改这个即可调速，如 0.8=慢 1.5=中 2.5=快 */
	speed: 1.2,
	distr: true,
	howElliptical: 1,
	oList: null as HTMLElement | null,
	oA: null as HTMLCollectionOf<HTMLAnchorElement> | null,
	sa: 0,
	ca: 0,
	sb: 0,
	cb: 0,
	sc: 0,
	cc: 0,
});

// 计算正弦余弦值（旋转核心公式，后台同款）
function sineCosine(a: number, b: number, c: number) {
	config.sa = Math.sin(a * config.dtr);
	config.ca = Math.cos(a * config.dtr);
	config.sb = Math.sin(b * config.dtr);
	config.cb = Math.cos(b * config.dtr);
	config.sc = Math.sin(c * config.dtr);
	config.cc = Math.cos(c * config.dtr);
}

// 设置初始定位（与后台 TagCloud 完全一致）
function positionAll() {
	if (!config.oList || !config.oA) return;
	const max = config.mcList.length;
	let phi = 0;
	let theta = 0;
	const aTmp: HTMLElement[] = [];
	const oFragment = document.createDocumentFragment();
	const len = config.oA.length;
	for (let i = 0; i < len; i++) {
		aTmp.push(config.oA[i] as unknown as HTMLElement);
	}
	aTmp.sort(() => (Math.random() < 0.5 ? 1 : -1));
	for (let i = 0; i < aTmp.length; i++) {
		oFragment.appendChild(aTmp[i]);
	}
	config.oList.appendChild(oFragment);
	for (let i = 1; i < max + 1; i++) {
		if (config.distr) {
			phi = Math.acos(-1 + (2 * i - 1) / max);
			theta = Math.sqrt(max * Math.PI) * phi;
		} else {
			phi = Math.random() * Math.PI;
			theta = Math.random() * (2 * Math.PI);
		}
		const cx0 = config.radius * Math.cos(theta) * Math.sin(phi);
		const cy0 = config.radius * Math.sin(theta) * Math.sin(phi);
		const cz0 = config.radius * Math.cos(phi);
		config.mcList[i - 1].initialCx = cx0;
		config.mcList[i - 1].initialCy = cy0;
		config.mcList[i - 1].initialCz = cz0;
		config.mcList[i - 1].cx = cx0;
		config.mcList[i - 1].cy = cy0;
		config.mcList[i - 1].cz = cz0;
		(config.oA[i - 1] as HTMLElement).style.left =
			config.mcList[i - 1].cx +
			config.oList.offsetWidth / 2 -
			(config.mcList[i - 1].offsetWidth ?? 0) / 2 +
			"px";
		(config.oA[i - 1] as HTMLElement).style.top =
			config.mcList[i - 1].cy +
			config.oList.offsetHeight / 2 -
			(config.mcList[i - 1].offsetHeight ?? 0) / 2 +
			"px";
	}
}

// 与后台 TagCloud 完全一致
function doPosition() {
	if (!config.oList || !config.oA) return;
	const l = config.oList.offsetWidth / 2;
	const t = config.oList.offsetHeight / 2;
	for (let i = 0; i < config.mcList.length; i++) {
		const m = config.mcList[i];
		(config.oA[i] as HTMLElement).style.left =
			m.cx + l - (m.offsetWidth ?? 0) / 2 + "px";
		(config.oA[i] as HTMLElement).style.top =
			m.cy + t - (m.offsetHeight ?? 0) / 2 + "px";
		(config.oA[i] as HTMLElement).style.fontSize =
			Math.ceil((12 * (m.scale ?? 1)) / 2) + 8 + "px";
		(config.oA[i] as HTMLElement).style.opacity = String(m.alpha ?? 1);
	}
}

// 与后台 TagCloud 完全一致（按 cz 深度排序）
function depthSort() {
	if (!config.oA) return;
	const aTmp: HTMLElement[] = [];
	for (let i = 0; i < config.oA.length; i++) {
		aTmp.push(config.oA[i] as unknown as HTMLElement);
	}
	aTmp.sort((v1, v2) => {
		const i1 = config.mcList.findIndex((_, j) => config.oA![j] === v1);
		const i2 = config.mcList.findIndex((_, j) => config.oA![j] === v2);
		const cz1 = config.mcList[i1]?.cz ?? 0;
		const cz2 = config.mcList[i2]?.cz ?? 0;
		if (cz1 > cz2) return -1;
		if (cz1 < cz2) return 1;
		return 0;
	});
	for (let i = 0; i < aTmp.length; i++) {
		(aTmp[i] as HTMLElement).style.zIndex = String(i);
	}
}

// 每帧：用「初始球面坐标 + 当前角度」算旋转，不累加坐标，保持圆形、匀速
function update() {
	config.angle += config.speed;
	if (Math.abs(config.angle) >= 360) config.angle %= 360;
	sineCosine(0, config.angle, 0);
	for (let j = 0; j < config.mcList.length; j++) {
		const m = config.mcList[j];
		const x0 = m.initialCx;
		const y0 = m.initialCy;
		const z0 = m.initialCz;
		const rx1 = x0;
		const ry1 = y0 * config.ca + z0 * -config.sa;
		const rz1 = y0 * config.sa + z0 * config.ca;
		const rx2 = rx1 * config.cb + rz1 * config.sb;
		const ry2 = ry1;
		const rz2 = rx1 * -config.sb + rz1 * config.cb;
		const rx3 = rx2 * config.cc + ry2 * -config.sc;
		const ry3 = rx2 * config.sc + ry2 * config.cc;
		const rz3 = rz2;
		m.cx = rx3;
		m.cy = ry3;
		m.cz = rz3;
		const per = config.d / (config.d + rz3);
		m.x = config.howElliptical * rx3 * per - config.howElliptical * 2;
		m.y = ry3 * per;
		m.scale = per;
		m.alpha = per;
		m.alpha = (m.alpha - 0.6) * (10 / 6);
	}
	doPosition();
	depthSort();
}

// 定时器（旋转帧刷新，40ms/帧，平滑不卡顿）
let timer: ReturnType<typeof setInterval> | null = null;

// 初始化标签云（后台同款初始化逻辑）
function initTagCloud() {
	nextTick(() => {
		config.oList = cloudRef.value;
		if (!config.oList) return;
		config.oA = config.oList.getElementsByTagName("a");
		// 清空旧坐标，重新初始化（仅数据更新时执行，不重复生成颜色）
		config.mcList = [];
		// 初始化每个标签的基础宽高（兜底避免为0，确保定位准确）
		Array.from(config.oA).forEach((el) => {
			const dom = el as HTMLElement;
			config.mcList.push({
				cx: 0,
				cy: 0,
				cz: 0,
				initialCx: 0,
				initialCy: 0,
				initialCz: 0,
				offsetWidth: dom.offsetWidth || 45,
				offsetHeight: dom.offsetHeight || 25,
			});
		});
		sineCosine(0, 0, 0);
		positionAll();
		if (timer) clearInterval(timer);
		timer = setInterval(update, 30);
	});
}

// 挂载时请求数据【仅首次请求，数据永久保留，颜色永久固定】
onMounted(() => {
	// 若标签列表已有数据，直接初始化（避免重复请求）
	if (tagList.value.length) {
		initTagCloud();
		return;
	}
	// 首次请求标签数据
	getTagList()
		.then(({ data }) => {
			const list = data?.data ?? [];
			// 为新标签生成【永久固定】颜色，已有标签不重复生成
			const newColors = { ...colorById.value };
			list.forEach((item: Tag) => {
				if (!newColors[item.id]) {
					newColors[
						item.id
					] = `rgb(${getRandomNum()},${getRandomNum()},${getRandomNum()})`;
				}
			});
			// 缓存颜色（永久不变）
			colorById.value = newColors;
			// 缓存标签数据（永久不变）
			tagList.value = list;
			// 数据渲染完成后初始化标签云
			initTagCloud();
		})
		.catch(() => {
			tagList.value = [];
		});
});

// 卸载时清除定时器，防止内存泄漏（后台同款规范）
onUnmounted(() => {
	if (timer) clearInterval(timer);
	timer = null;
});
</script>

<style lang="scss" scoped>
.side-card {
	width: 100%;
}

.tag-title {
	font-size: 1.2em;
	margin-bottom: 0.8rem;
	display: flex;
	align-items: center;
	gap: 0.4rem;
	font-weight: 500;
}

// 与后台 TagCloud 比例一致（radius 120），侧栏略矮
.cloud_wrap {
	width: 100%;
	height: 220px;
}

.tagcloud-all {
	height: 100%;
	position: relative;
	width: 100%;
}

// 标签样式：穿透scoped确保宽高正常获取，hover轻微放大
:deep(.tag) {
	position: absolute;
	top: 0;
	left: 0;
	white-space: nowrap;
	text-decoration: none;
	font-weight: 500;
	padding: 4px 8px;
	transition: all 0.2s ease-in-out;
	cursor: pointer;

	&:hover {
		opacity: 1 !important;
		letter-spacing: 1px;
		transform: scale(1.15) !important;
		z-index: 999 !important; // hover时置顶，不被遮挡
	}
}
</style>
