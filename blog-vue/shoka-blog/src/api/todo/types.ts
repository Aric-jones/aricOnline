export interface TodoItem {
	id: number;
	userId: number;
	title: string;
	description: string;
	status: number;
	priority: number;
	type: number;
	category: string;
	startTime: string;
	endTime: string;
	completedTime: string;
	createTime: string;
	updateTime: string;
}

export interface TodoReq {
	id?: number;
	title: string;
	description?: string;
	priority?: number;
	category?: string;
	startTime?: string;
	endTime?: string;
	type?: number;
}

export interface TodoQuery {
	current?: number;
	size?: number;
	type?: number;
	status?: number | null;
	priority?: number | null;
	category?: string;
	keyword?: string;
	startDate?: string;
	endDate?: string;
}

export interface DiaryItem {
	id: number;
	userId: number;
	diaryDate: string;
	content: string;
	mood: string;
	createTime: string;
	updateTime: string;
}

export interface DiaryReq {
	id?: number;
	diaryDate: string;
	content: string;
	mood?: string;
}

export interface ThinkingItem {
	id: number;
	userId: number;
	topic: string;
	harvest: string;
	remark: string;
	createTime: string;
	updateTime: string;
}

export interface ThinkingReq {
	id?: number;
	topic: string;
	harvest: string;
	remark?: string;
}
