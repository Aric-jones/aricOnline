import request from "@/utils/request";
import { AxiosPromise } from "axios";
import { Result } from "@/model";
import type { HabitItem, HabitReq, HabitRecordItem, HabitRecordReq, DailyStat } from "./types";

// ================= 习惯定义 =================

export function getHabitList(): AxiosPromise<Result<HabitItem[]>> {
	return request({ url: "/user/habit/list", method: "get" });
}

export function addHabit(data: HabitReq): AxiosPromise<Result<null>> {
	return request({ url: "/user/habit", method: "post", data });
}

export function updateHabit(data: HabitReq): AxiosPromise<Result<null>> {
	return request({ url: "/user/habit", method: "put", data });
}

export function deleteHabit(id: number): AxiosPromise<Result<null>> {
	return request({ url: `/user/habit/${id}`, method: "delete" });
}

// ================= 习惯记录 =================

export function getHabitRecords(params: { habitId?: number; startDate: string; endDate: string }): AxiosPromise<Result<HabitRecordItem[]>> {
	return request({ url: "/user/habit/record/list", method: "get", params });
}

export function addHabitRecord(data: HabitRecordReq): AxiosPromise<Result<null>> {
	return request({ url: "/user/habit/record", method: "post", data });
}

export function updateHabitRecord(data: HabitRecordReq): AxiosPromise<Result<null>> {
	return request({ url: "/user/habit/record", method: "put", data });
}

export function deleteHabitRecord(id: number): AxiosPromise<Result<null>> {
	return request({ url: `/user/habit/record/${id}`, method: "delete" });
}

// ================= 统计 =================

export function getHabitStats(params: { habitId?: number; startDate: string; endDate: string }): AxiosPromise<Result<DailyStat[]>> {
	return request({ url: "/user/habit/stats", method: "get", params });
}
