import request from "@/utils/request";
import { AxiosPromise } from "axios";
import { PageResult, Result } from "@/model";
import type { TodoItem, TodoReq, TodoQuery, DiaryItem, DiaryReq, ThinkingItem, ThinkingReq, TimeBlockItem, TimeBlockReq, DistinctEvent, CategoryStat, TaskPoolItem, TaskPoolReq, TaskPoolAssignReq } from "./types";

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

// ================= 思考沉淀 =================

export function getThinkingList(keyword?: string, category?: string): AxiosPromise<Result<ThinkingItem[]>> {
  return request({ url: "/user/thinking/list", method: "get", params: { keyword, category } });
}

export function getThinkingCategories(): AxiosPromise<Result<string[]>> {
  return request({ url: "/user/thinking/categories", method: "get" });
}

export function addThinking(data: ThinkingReq): AxiosPromise<Result<null>> {
  return request({ url: "/user/thinking", method: "post", data });
}

export function updateThinking(data: ThinkingReq): AxiosPromise<Result<null>> {
  return request({ url: "/user/thinking", method: "put", data });
}

export function deleteThinking(id: number): AxiosPromise<Result<null>> {
  return request({ url: `/user/thinking/${id}`, method: "delete" });
}

// ================= 时间管理 =================

export function getTimeBlocks(date: string): AxiosPromise<Result<TimeBlockItem[]>> {
  return request({ url: "/user/timeblock/list", method: "get", params: { date } });
}

export function addTimeBlock(data: TimeBlockReq): AxiosPromise<Result<null>> {
  return request({ url: "/user/timeblock", method: "post", data });
}

export function updateTimeBlock(data: TimeBlockReq): AxiosPromise<Result<null>> {
  return request({ url: "/user/timeblock", method: "put", data });
}

export function deleteTimeBlock(id: number): AxiosPromise<Result<null>> {
  return request({ url: `/user/timeblock/${id}`, method: "delete" });
}

export function getDistinctEvents(): AxiosPromise<Result<DistinctEvent[]>> {
  return request({ url: "/user/timeblock/events", method: "get" });
}

export function getTimeBlockStats(type: string): AxiosPromise<Result<CategoryStat[]>> {
  return request({ url: "/user/timeblock/stats", method: "get", params: { type } });
}

// ================= 任务池 =================

export function getUnassignedTasks(keyword?: string): AxiosPromise<Result<TaskPoolItem[]>> {
  return request({ url: "/user/taskpool/unassigned", method: "get", params: { keyword } });
}

export function getWeekTasks(weekStart: string): AxiosPromise<Result<TaskPoolItem[]>> {
  return request({ url: "/user/taskpool/week", method: "get", params: { weekStart } });
}

export function getRangeTasks(start: string, end: string): AxiosPromise<Result<TaskPoolItem[]>> {
  return request({ url: "/user/taskpool/range", method: "get", params: { start, end } });
}

export function addPoolTask(data: TaskPoolReq): AxiosPromise<Result<null>> {
  return request({ url: "/user/taskpool", method: "post", data });
}

export function updatePoolTask(data: TaskPoolReq): AxiosPromise<Result<null>> {
  return request({ url: "/user/taskpool", method: "put", data });
}

export function deletePoolTask(id: number): AxiosPromise<Result<null>> {
  return request({ url: `/user/taskpool/${id}`, method: "delete" });
}

export function assignPoolTask(data: TaskPoolAssignReq): AxiosPromise<Result<null>> {
  return request({ url: "/user/taskpool/assign", method: "put", data });
}

export function unassignPoolTask(id: number): AxiosPromise<Result<null>> {
  return request({ url: `/user/taskpool/unassign/${id}`, method: "put" });
}

export function togglePoolTaskStatus(id: number): AxiosPromise<Result<null>> {
  return request({ url: `/user/taskpool/status/${id}`, method: "put" });
}

// ================= AI =================

export function aiSummary(type: string): AxiosPromise<Result<string>> {
  return request({ url: "/user/todo/ai/summary", method: "post", data: { type }, timeout: 180000 });
}

export function aiSuggest(): AxiosPromise<Result<string>> {
  return request({ url: "/user/todo/ai/suggest", method: "post", timeout: 180000 });
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
  return request({ url: "/user/ai/prompt/optimize", method: "post", data: { content }, timeout: 180000 });
}

// ================= 习惯洞察 =================

export function aiHabitInsight(habitIds: number[]): AxiosPromise<Result<string>> {
  return request({ url: "/user/todo/ai/habit-insight", method: "post", data: { habitIds }, timeout: 180000 });
}
