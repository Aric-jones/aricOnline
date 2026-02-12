<template>
	<div v-if="blog.blogInfoSafe.siteConfig.isChat">
		<div class="chat-container" v-show="show" @click.stop>
			<div class="chat-header">
				<img
					width="32"
					height="32"
					src="https://big-event0611.oss-cn-beijing.aliyuncs.com/article/a66b83711614a5fe7786079e3fb7f45a.png"
				/>
				<div style="margin-left: 12px">
					<div>聊天室</div>
					<div style="font-size: 12px">当前{{ onlineCount }}人在线</div>
				</div>
				<svg-icon
					class="close"
					icon-class="close"
					size="1.2rem"
					@click="show = false"
				></svg-icon>
			</div>
			<div class="chat-content" id="chat-content">
				<div
					class="chat-item"
					v-for="(chat, index) of recordList"
					:class="isMy(chat) ? 'my-chat' : ''"
				>
					<img class="user-avatar" :src="chat.avatar" alt="" />
					<div :class="isMy(chat) ? 'right-info' : 'left-info'">
						<div class="user-info" :class="isMy(chat) ? 'my-chat' : ''">
							<span style="color: var(--color-red)">{{ chat.nickname }}</span>
							<span
								style="color: var(--color-blue)"
								:class="isMy(chat) ? 'right-info' : 'left-info'"
							>
								{{ formatDateTime(chat.createTime) }}
							</span>
							<span
								v-if="chat.ipSource"
								style="
									color: var(--color-grey);
									font-size: 11px;
									margin-left: 8px;
								"
							>
								{{ chat.ipSource }}
							</span>
						</div>
						<div
							class="user-content"
							:class="isMy(chat) ? 'my-content' : ''"
							@contextmenu.prevent.stop="showBack(chat, index, $event)"
						>
							<div v-html="chat.content"></div>
							<div class="back-menu" ref="backBtn" @click="back(chat, index)">
								撤回
							</div>
						</div>
					</div>
				</div>
			</div>
			<div class="chat-footer">
				<textarea
					class="chat-input"
					v-model="chatContent"
					placeholder="开始聊天吧"
					@keydown="handleKeyCode($event)"
				></textarea>
				<Emoji @add-emoji="handleEmoji" @choose-type="handleType"></Emoji>
				<svg-icon
					class="send-btn"
					icon-class="plane"
					size="1.5rem"
					@click="handleSend"
				></svg-icon>
			</div>
		</div>
		<!-- 遮罩层，点击外部关闭聊天室 -->
		<div v-if="show" class="chat-overlay" @click="show = false"></div>
		<div class="chat-btn" @click="handleOpen">
			<span class="unread" v-if="unreadCount > 0">{{ unreadCount }}</span>
			<img
				src="https://big-event0611.oss-cn-beijing.aliyuncs.com/article/86e795e9c25db23ea246af68f7477b11.png"
				alt=""
			/>
		</div>
	</div>
</template>

<script setup lang="ts">
import { Record } from "@/model";
import { useBlogStore, useUserStore } from "@/store";
import { formatDateTime } from "@/utils/date";
import { emojiList } from "@/utils/emoji";
import { tvList } from "@/utils/tv";
const user = useUserStore();
const blog = useBlogStore();
const data = reactive({
	show: false,
	ipAddress: "",
	ipSource: "",
	recordList: [] as Record[],
	chatContent: "",
	emojiType: 0,
	unreadCount: 0,
	webSocketState: false,
	onlineCount: 0,
});
enum Type {
	ONLINE_COUNT = 1,
	HISTORY_RECORD = 2,
	SEND_MESSAGE = 3,
	RECALL_MESSAGE = 4,
	HEART_BEAT = 5,
}
const {
	show,
	recordList,
	ipAddress,
	ipSource,
	chatContent,
	emojiType,
	unreadCount,
	webSocketState,
	onlineCount,
} = toRefs(data);
const websocketMessage = reactive<{
	type: number;
	data: any;
}>({
	type: 0,
	data: {},
});
const backBtn = ref<any>([]);
const websocket = ref<WebSocket>();
const timeout = ref<NodeJS.Timeout>();
const serverTimeout = ref<NodeJS.Timeout>();
const isMy = computed(
	() => (chat: Record) =>
		chat.ipAddress == ipAddress.value ||
		(chat.userId !== undefined && chat.userId === user.id)
);
const userNickname = computed(() =>
	user.nickname ? user.nickname : ipAddress.value
);
const userAvatar = computed(() =>
	user.avatar ? user.avatar : blog.blogInfoSafe.siteConfig.touristAvatar
);

/**
 * 解析表情包文本为 HTML img 标签
 * @param content 原始消息内容（可能包含 [嘻嘻] 这样的表情文本）
 * @param emojiTypeValue 表情类型（0=emoji, 1=tv）
 * @returns 解析后的 HTML 字符串
 */
const parseEmoji = (content: string, emojiTypeValue: number): string => {
	if (!content || typeof content !== "string") return content;
	// 如果已经是 HTML（包含 <img>），直接返回
	if (content.includes("<img")) return content;

	return content.replace(/\[.+?\]/g, (str) => {
		if (emojiTypeValue === 0) {
			if (emojiList[str] === undefined) {
				return str;
			}
			return (
				"<img src='" +
				emojiList[str] +
				"' width='21' height='21' style='margin: 0 1px;vertical-align: text-bottom'/>"
			);
		}
		if (emojiTypeValue === 1) {
			if (tvList[str] === undefined) {
				return str;
			}
			return (
				"<img src='" +
				tvList[str] +
				"' width='21' height='21' style='margin: 0 1px;vertical-align: text-bottom'/>"
			);
		}
		return str;
	});
};

/**
 * 获取 WebSocket URL
 * 优先使用后台配置的 websocketUrl，否则根据环境自动推导
 */
const getWebSocketUrl = (): string => {
	const configuredUrl = blog.blogInfoSafe.siteConfig.websocketUrl;
	// 如果后台配置了完整地址，直接使用
	if (configuredUrl && configuredUrl.trim()) {
		return configuredUrl;
	}
	// 自动推导：本地开发直连后端，线上通过 Nginx 代理
	if (import.meta.env.DEV) {
		return "ws://localhost:8080/websocket";
	}
	const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
	return `${protocol}//${window.location.host}/websocket`;
};

/**
 * 创建 WebSocket 连接
 */
const createWebSocket = () => {
	const ws = new WebSocket(getWebSocketUrl());
	ws.onopen = () => {
		webSocketState.value = true;
		startHeart();
	};
	ws.onmessage = (event: MessageEvent) => {
		const data = JSON.parse(event.data);
		switch (data.type) {
			case Type.ONLINE_COUNT:
				onlineCount.value = data.data;
				break;
			case Type.HISTORY_RECORD:
				recordList.value = data.data.chatRecordList;
				ipAddress.value = data.data.ipAddress;
				ipSource.value = data.data.ipSource;
				break;
			case Type.SEND_MESSAGE:
				if (data.data && data.data.content) {
					data.data.content = parseEmoji(data.data.content, emojiType.value);
				}
				recordList.value.push(data.data);
				if (!show.value) {
					unreadCount.value++;
				}
				break;
			case Type.RECALL_MESSAGE:
				for (let i = 0; i < recordList.value.length; i++) {
					if (recordList.value[i].id === data.data) {
						recordList.value.splice(i, 1);
						i--;
					}
				}
				break;
			case Type.HEART_BEAT:
				webSocketState.value = true;
				break;
		}
	};
	ws.onclose = () => {
		webSocketState.value = false;
		websocket.value = undefined; // 重置，允许重连
		clear();
	};
	ws.onerror = (error) => {
		webSocketState.value = false;
		websocket.value = undefined;
		clear();
		// 提示连接失败
		if (show.value) {
			window.$message?.error(
				"WebSocket 连接失败，请检查后台配置的 websocketUrl"
			);
		}
	};
	websocket.value = ws;
};

const handleOpen = () => {
	// 如果 WebSocket 不存在或已关闭，重新创建
	if (
		!websocket.value ||
		websocket.value.readyState === WebSocket.CLOSED ||
		websocket.value.readyState === WebSocket.CLOSING
	) {
		websocket.value = undefined;
		createWebSocket();
	}
	unreadCount.value = 0;
	show.value = !show.value;
};
// 展示菜单
const showBack = (chat: Record, index: number, e: any) => {
	backBtn.value.forEach((item: any) => {
		item.style.display = "none";
	});
	if (
		chat.ipAddress === ipAddress.value ||
		(chat.userId != null && chat.userId == user.id)
	) {
		backBtn.value[index].style.left = e.offsetX + "px";
		backBtn.value[index].style.bottom = e.offsetY + "px";
		backBtn.value[index].style.display = "block";
	}
};
// 撤回消息
const back = (item: Record, index: number) => {
	websocketMessage.type = Type.RECALL_MESSAGE;
	websocketMessage.data = item.id;
	websocket.value?.send(JSON.stringify(websocketMessage));
	backBtn.value[index].style.display = "none";
};
const handleKeyCode = (e: any) => {
	if (e.ctrlKey && e.keyCode === 13) {
		chatContent.value = chatContent.value + "\n";
	} else if (e.keyCode === 13) {
		e.preventDefault();
		handleSend();
	}
};
const handleSend = () => {
	if (chatContent.value.trim() == "") {
		window.$message?.error("内容不能为空");
		return;
	}
	// 检查连接状态，断开则自动重连
	if (!websocket.value || websocket.value.readyState !== WebSocket.OPEN) {
		websocket.value = undefined;
		createWebSocket();
		window.$message?.warning("连接已断开，正在重连，请稍后再试");
		return;
	}
	// 解析表情（发送前转换）
	chatContent.value = parseEmoji(chatContent.value, emojiType.value);
	let chat = {
		nickname: userNickname.value,
		avatar: userAvatar.value,
		content: chatContent.value,
		userId: user.id,
		ipAddress: ipAddress.value,
		ipSource: ipSource.value,
	};
	websocketMessage.type = Type.SEND_MESSAGE;
	websocketMessage.data = chat;
	websocket.value.send(JSON.stringify(websocketMessage));
	chatContent.value = "";
};
const startHeart = () => {
	clear();
	timeout.value = setTimeout(() => {
		if (websocket.value && websocket.value.readyState === WebSocket.OPEN) {
			const beatMessage = {
				type: Type.HEART_BEAT,
				data: "ping",
			};
			websocket.value.send(JSON.stringify(beatMessage));
			waitServer();
		}
	}, 30 * 1000);
};
const waitServer = () => {
	webSocketState.value = false;
	serverTimeout.value = setTimeout(() => {
		if (webSocketState.value) {
			startHeart();
			return;
		}
		// 服务器未响应心跳，关闭连接（onclose 会重置 websocket.value）
		if (websocket.value && websocket.value.readyState === WebSocket.OPEN) {
			websocket.value.close();
		}
	}, 20 * 1000);
};
const clear = () => {
	timeout.value && clearTimeout(timeout.value);
	serverTimeout.value && clearTimeout(serverTimeout.value);
};
const handleEmoji = (key: string) => {
	chatContent.value += key;
};
const handleType = (key: number) => {
	emojiType.value = key;
};
onUpdated(() => {
	const element = document.getElementById("chat-content");
	if (element) {
		element.scrollTop = element.scrollHeight;
	}
});
</script>

<style lang="scss" scoped>
.chat-container {
	box-shadow: 0 5px 40px rgba(0, 0, 0, 0.16);
	font-size: 14px;
	background: var(--grey-1);
	animation: bounceInUp 1s;
	animation-fill-mode: both;
	z-index: 1200;
}

@media (min-width: 760px) {
	.chat-container {
		position: fixed;
		bottom: 100px;
		right: 20px;
		width: 400px;
		height: calc(100% - 110px);
		max-height: 590px;
		min-height: 250px;
		border-radius: 1rem;
	}

	.close {
		display: none;
	}
}

@media (max-width: 760px) {
	.chat-container {
		position: fixed;
		top: 0;
		bottom: 0;
		right: 0;
		left: 0;
	}

	.close {
		display: block;
		margin-left: auto;
	}
}

.chat-header {
	display: flex;
	align-items: center;
	padding: 20px 24px;
	border-radius: 1rem 1rem 0 0;
	background-color: var(--grey-0);
}

.unread {
	position: absolute;
	width: 18px;
	height: 18px;
	text-align: center;
	border-radius: 50%;
	line-height: 20px;
	font-size: 12px;
	background: var(--color-red);
	color: var(--grey-0);
}

.chat-content {
	position: absolute;
	top: 80px;
	bottom: 46px;
	width: 100%;
	padding: 20px 16px 0 16px;
	background-color: var(--chat-bg);
	overflow-y: auto;
	overflow-x: hidden;
}

.my-chat {
	flex-direction: row-reverse;
}

.chat-item {
	display: flex;
	margin-bottom: 0.5rem;

	.user-avatar {
		width: 40px;
		height: 40px;
		border-radius: 50%;
	}

	.left-info {
		margin-left: 0.5rem;
	}

	.right-info {
		margin-right: 0.5rem;
	}

	.user-info {
		display: flex;
		align-items: center;
		font-size: 12px;
	}

	.user-content {
		position: relative;
		padding: 10px;
		border-radius: 5px 20px 20px 20px;
		background: var(--grey-0);
		width: fit-content;
		white-space: pre-line;
		word-wrap: break-word;
		word-break: break-all;
	}

	.my-content {
		float: right;
		border-radius: 20px 5px 20px 20px;
		background: var(--color-blue);
		color: var(--grey-0);
	}
}

.back-menu {
	position: absolute;
	width: 80px;
	height: 35px;
	line-height: 35px;
	font-size: 13px;
	border-radius: 2px;
	background: rgba(255, 255, 255, 0.9);
	color: #000;
	text-align: center;
	display: none;
}

.chat-overlay {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background: rgba(0, 0, 0, 0.3);
	z-index: 1199;
	backdrop-filter: blur(2px);
}

.chat-btn {
	position: fixed;
	bottom: 15px;
	right: 5px;
	width: 60px;
	height: 60px;
	border-radius: 100px;
	cursor: pointer;
	z-index: 1000;
}
.chat-btn img {
	width: 100%;
	height: 100%;
	object-fit: cover; // 保持图片比例铺满容器，不拉伸（裁剪多余部分）
	object-position: center; // 图片居中裁剪，保留核心区域
}

.chat-footer {
	position: absolute;
	bottom: 0;
	display: flex;
	align-items: center;
	width: 100%;
	padding: 8px 16px;
	background: var(--grey-2);
	border-radius: 0 0 1rem 1rem;

	.chat-input {
		width: 100%;
		height: 30px;
		padding: 0.5rem 0 0 0.6rem;
		margin-right: 0.5rem;
		font-size: 13px;
		color: var(--text-color);
		background-color: var(--grey-1);
		outline: none;
		resize: none;
	}

	.send-btn {
		color: var(--color-blue);
		margin-left: 0.5rem;
	}
}
</style>
