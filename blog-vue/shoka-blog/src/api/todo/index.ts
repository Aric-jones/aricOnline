import request from "@/utils/request";
import { AxiosPromise } from "axios";
import { PageResult, Result } from "@/model";
import type { TodoItem, TodoReq, TodoQuery, DiaryItem, DiaryReq } from "./types";

// ================= 代办 =================

export function getTodoList(params: TodoQuery): AxiosPromise<Result<PageResult<TodoItem[]>>> {
  return request({ url: "/user/todo/list", method: "get", params });
}

export function addTodo(data: TodoReq): AxiosPromise<Result<null>> {
  return request({ url: "/user/todo", method: "post", data });
}

export function updateTodo(data: TodoReq): AxiosPromise<Result<null>> {
  return request({ url: "/user/todo", method: "put", data });
}

export function deleteTodo(id: number): AxiosPromise<Result<null>> {
  return request({ url: `/user/todo/${id}`, method: "delete" });
}

export function toggleTodoStatus(id: number): AxiosPromise<Result<null>> {
  return request({ url: `/user/todo/status/${id}`, method: "put" });
}

export function getCalendarData(startDate: string, endDate: string): AxiosPromise<Result<TodoItem[]>> {
  return request({ url: "/user/todo/calendar", method: "get", params: { startDate, endDate } });
}

export function getTodoCategories(): AxiosPromise<Result<string[]>> {
  return request({ url: "/user/todo/categories", method: "get" });
}

// ================= 日记 =================

export function getDiary(date: string): AxiosPromise<Result<DiaryItem>> {
  return request({ url: `/user/diary/${date}`, method: "get" });
}

export function saveDiary(data: DiaryReq): AxiosPromise<Result<null>> {
  return request({ url: "/user/diary", method: "post", data });
}

export function deleteDiary(id: number): AxiosPromise<Result<null>> {
  return request({ url: `/user/diary/${id}`, method: "delete" });
}

export function getDiaryRange(startDate: string, endDate: string): AxiosPromise<Result<DiaryItem[]>> {
  return request({ url: "/user/diary/range", method: "get", params: { startDate, endDate } });
}

// ================= AI =================

export function aiSummary(type: string): AxiosPromise<Result<string>> {
  return request({ url: "/user/todo/ai/summary", method: "post", data: { type } });
}

export function aiSuggest(): AxiosPromise<Result<string>> {
  return request({ url: "/user/todo/ai/suggest", method: "post" });
}

export function getAiRecord(type: string): AxiosPromise<Result<any>> {
  return request({ url: `/user/todo/ai/record/${type}`, method: "get" });
}

export function saveAiRecord(recordType: string, content: string): AxiosPromise<Result<null>> {
  return request({ url: "/user/todo/ai/record", method: "post", data: { recordType, content } });
}

// ================= AI 提示词 =================

export function getAiPromptList(): AxiosPromise<Result<any[]>> {
  return request({ url: "/user/ai/prompt/list", method: "get" });
}

export function saveAiPrompt(data: { promptKey: string; name: string; content: string }): AxiosPromise<Result<null>> {
  return request({ url: "/user/ai/prompt", method: "post", data });
}

export function resetAiPrompt(promptKey: string): AxiosPromise<Result<null>> {
  return request({ url: `/user/ai/prompt/${promptKey}`, method: "delete" });
}

export function optimizeAiPrompt(content: string): AxiosPromise<Result<string>> {
  return request({ url: "/user/ai/prompt/optimize", method: "post", data: { content } });
}

// ================= 习惯洞察 =================

export function aiHabitInsight(habitIds: number[]): AxiosPromise<Result<string>> {
  return request({ url: "/user/todo/ai/habit-insight", method: "post", data: { habitIds } });
}
