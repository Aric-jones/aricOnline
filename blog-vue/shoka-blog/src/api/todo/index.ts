import request from "@/utils/request";
import { AxiosPromise } from "axios";
import { PageResult, Result } from "@/model";
import type { TodoItem, TodoReq, TodoQuery, DiaryItem, DiaryReq } from "./types";

// ================= 代办 =================

export function getTodoList(params: TodoQuery): AxiosPromise<Result<PageResult<TodoItem[]>>> {
  return request({ url: "/admin/todo/list", method: "get", params });
}

export function addTodo(data: TodoReq): AxiosPromise<Result<null>> {
  return request({ url: "/admin/todo", method: "post", data });
}

export function updateTodo(data: TodoReq): AxiosPromise<Result<null>> {
  return request({ url: "/admin/todo", method: "put", data });
}

export function deleteTodo(id: number): AxiosPromise<Result<null>> {
  return request({ url: `/admin/todo/${id}`, method: "delete" });
}

export function toggleTodoStatus(id: number): AxiosPromise<Result<null>> {
  return request({ url: `/admin/todo/status/${id}`, method: "put" });
}

export function getCalendarData(startDate: string, endDate: string): AxiosPromise<Result<TodoItem[]>> {
  return request({ url: "/admin/todo/calendar", method: "get", params: { startDate, endDate } });
}

export function getTodoCategories(): AxiosPromise<Result<string[]>> {
  return request({ url: "/admin/todo/categories", method: "get" });
}

// ================= 日记 =================

export function getDiary(date: string): AxiosPromise<Result<DiaryItem>> {
  return request({ url: `/admin/diary/${date}`, method: "get" });
}

export function saveDiary(data: DiaryReq): AxiosPromise<Result<null>> {
  return request({ url: "/admin/diary", method: "post", data });
}

export function deleteDiary(id: number): AxiosPromise<Result<null>> {
  return request({ url: `/admin/diary/${id}`, method: "delete" });
}

export function getDiaryRange(startDate: string, endDate: string): AxiosPromise<Result<DiaryItem[]>> {
  return request({ url: "/admin/diary/range", method: "get", params: { startDate, endDate } });
}

// ================= AI =================

export function aiSummary(type: string): AxiosPromise<Result<string>> {
  return request({ url: "/admin/todo/ai/summary", method: "post", data: { type } });
}

export function aiSuggest(): AxiosPromise<Result<string>> {
  return request({ url: "/admin/todo/ai/suggest", method: "post" });
}

export function getAiRecord(type: string): AxiosPromise<Result<any>> {
  return request({ url: `/admin/todo/ai/record/${type}`, method: "get" });
}

export function saveAiRecord(recordType: string, content: string): AxiosPromise<Result<null>> {
  return request({ url: "/admin/todo/ai/record", method: "post", data: { recordType, content } });
}
