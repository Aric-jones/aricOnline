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

export interface TimeBlockItem {
	id: number;
	userId: number;
	blockDate: string;
	name: string;
	category: string;
	startTime: string;
	endTime: string;
	remark: string;
	color: string;
	createTime: string;
	updateTime: string;
}

export interface TimeBlockReq {
	id?: number;
	name: string;
	category: string;
	blockDate: string;
	startTime: string;
	endTime: string;
	remark?: string;
	color?: string;
}

export interface DistinctEvent {
	name: string;
	category: string;
	color: string;
}

export interface CategoryStat {
	category: string;
	total_minutes: number;
}

export interface TaskPoolItem {
	id: number;
	userId: number;
	title: string;
	description: string;
	category: string;
	priority: number;
	status: number;
	weekStartDate: string | null;
	completedTime: string | null;
	createTime: string;
	updateTime: string;
}

export interface TaskPoolReq {
	id?: number;
	title: string;
	description?: string;
	category?: string;
	priority?: number;
}

export interface TaskPoolAssignReq {
	id: number;
	weekStartDate: string;
}
