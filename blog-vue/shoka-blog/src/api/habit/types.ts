export interface HabitItem {
	id: number;
	userId: number;
	name: string;
	icon: string;
	color: string;
	category: string;
	unit: string;
	sort: number;
	isActive: number;
	createTime: string;
	updateTime: string;
}

export interface HabitReq {
	id?: number;
	name: string;
	icon?: string;
	color?: string;
	category?: string;
	unit?: string;
	sort?: number;
	isActive?: number;
}

export interface HabitRecordItem {
	id: number;
	habitId: number;
	userId: number;
	recordDate: string;
	value: number;
	rating: number;
	note: string;
	createTime: string;
	updateTime: string;
}

export interface HabitRecordReq {
	id?: number;
	habitId: number;
	recordDate?: string;
	value?: number;
	rating?: number;
	note?: string;
}

export interface DailyStat {
	recordDate: string;
	count: number;
	totalValue: number;
	avgRating: number;
}
