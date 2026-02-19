<template>
    <div class="app-container">
        <!-- 文章标题 -->
        <div class="operation-container">
      <el-input v-model="articleForm.articleTitle" placeholder="请输入文章标题">
        <template #append>
          <el-button type="primary" :loading="aiTitleLoading" @click="handleAiTitle" title="AI 生成标题">
            <el-icon><MagicStick /></el-icon>
          </el-button>
        </template>
      </el-input>
      <el-button type="warning" style="margin-left: 10px" @click="handleOptimize">
        <el-icon style="margin-right: 4px"><MagicStick /></el-icon>AI 优化
      </el-button>
            <el-button type="danger" style="margin-left: 10px" @click="openModel">发布文章</el-button>
        </div>
        <!-- 文章内容 -->
    <md-editor
      ref="editorRef"
      v-model="articleForm.articleContent"
      :theme="isDark ? 'dark' : 'light'"
      class="md-container"
      :toolbars="toolbars"
      @on-upload-img="uploadImg"
      placeholder="请输入文章内容..."
    >
            <template #defToolbars>
                <emoji-extension :on-insert="insert" />
            </template>
        </md-editor>
    <!-- AI 优化对话框 -->
    <el-dialog
      title="AI 一键优化文章"
      v-model="optimizeVisible"
      :width="optimizeLoading ? '500px' : '92vw'"
      top="2vh"
      append-to-body
      destroy-on-close
      class="optimize-dialog"
    >
      <div class="optimize-container">
        <!-- Loading 状态 -->
        <div class="optimize-status" v-if="optimizeLoading">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>AI 正在优化文章，请稍候...</span>
          <span v-if="optimizedContent" class="optimize-progress">（已接收 {{ optimizedContent.length }} 字符）</span>
        </div>
        <!-- Diff 对比视图 -->
        <div v-else-if="diffBlocks.length > 0" class="diff-viewer">
          <div class="diff-stats">
            <span class="diff-stats-item">
              <span class="diff-stats-dot diff-stats-dot-change"></span>
              {{ changeCount }} 处修改
            </span>
            <span class="diff-stats-item">
              <span class="diff-stats-dot diff-stats-dot-accept"></span>
              已接受 {{ acceptedCount }} 处
            </span>
          </div>
          <div class="diff-panel">
            <div class="diff-header-row">
              <div class="diff-col diff-col-left">原始内容</div>
              <div class="diff-col-gutter"></div>
              <div class="diff-col diff-col-right">优化后内容</div>
            </div>
            <div class="diff-body">
              <template v-for="block in diffBlocks" :key="block.id">
                <!-- 相同内容块 -->
                <div v-if="block.type === 'equal'" class="diff-block diff-block-equal">
                  <div class="diff-col diff-col-left">
                    <div v-for="(line, i) in getDisplayLines(block.original)" :key="i" class="diff-line">{{ line }}</div>
                  </div>
                  <div class="diff-col-gutter"></div>
                  <div class="diff-col diff-col-right">
                    <div v-for="(line, i) in getDisplayLines(block.optimized)" :key="i" class="diff-line">{{ line }}</div>
                  </div>
                </div>
                <!-- 修改块 -->
                <div v-else class="diff-block diff-block-change" :class="{ 'diff-block-accepted': block.accepted }">
                  <div class="diff-col diff-col-left diff-col-removed" :class="{ 'diff-col-accepted-left': block.accepted }">
                    <div v-for="(line, i) in getDisplayLines(block.original)" :key="i" class="diff-line">{{ line }}</div>
                    <div v-if="!block.original" class="diff-line diff-line-empty">（无内容）</div>
                  </div>
                  <div class="diff-col-gutter">
                    <button
                      class="diff-accept-btn"
                      :class="{ 'diff-accept-btn-active': block.accepted }"
                      @click="toggleBlock(block.id)"
                      :title="block.accepted ? '撤销接受' : '接受此修改'"
                    >
                      <span v-if="block.accepted">✓</span>
                      <span v-else>»</span>
                    </button>
                  </div>
                  <div class="diff-col diff-col-right diff-col-added" :class="{ 'diff-col-accepted-right': block.accepted }">
                    <div v-for="(line, i) in getDisplayLines(block.optimized)" :key="i" class="diff-line">{{ line }}</div>
                    <div v-if="!block.optimized" class="diff-line diff-line-empty">（无内容）</div>
                  </div>
                </div>
              </template>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="optimize-footer">
          <el-button @click="optimizeVisible = false">取消</el-button>
          <el-button type="warning" :disabled="optimizeLoading || changeCount === 0" @click="acceptAllBlocks"> 全部接受 </el-button>
          <el-button type="primary" :disabled="optimizeLoading || acceptedCount === 0" @click="applySelectedBlocks">
            应用修改（{{ acceptedCount }}/{{ changeCount }}）
          </el-button>
          <el-button type="success" :disabled="optimizeLoading || !optimizedContent" @click="handleReplaceAll"> 全部替换 </el-button>
        </div>
      </template>
    </el-dialog>
        <!-- 发布或修改对话框 -->
        <el-dialog title="发布文章" v-model="addOrUpdate" width="600px" top="0.5vh" append-to-body>
            <el-form ref="articleFormRef" label-width="80px" :model="articleForm" :rules="rules">
                <!-- 文章分类 -->
                <el-form-item label="文章分类" prop="categoryName">
          <el-tag type="success" v-show="articleForm.categoryName" :disable-transitions="true" :closable="true" @close="removeCategory">
                        {{ articleForm.categoryName }}
                    </el-tag>
                    <!-- 分类选项 -->
                    <el-popover v-if="!articleForm.categoryName" placement="bottom-start" width="460" trigger="click">
                        <template #reference>
                            <el-button type="success" plain>添加分类</el-button>
                        </template>
                        <div class="popover-title">分类</div>
                        <!-- 搜索框 -->
            <el-autocomplete
              style="width: 100%"
              v-model="categoryName"
              :fetch-suggestions="searchCategory"
              placeholder="请输入分类名搜索,enter可添加自定义分类"
              :trigger-on-focus="false"
              @keyup.enter="saveCategory"
              @select="handleSelectCategory"
            >
                            <template #default="{ item }">
                                <div>{{ item.categoryName }}</div>
                            </template>
                        </el-autocomplete>
                        <!-- 分类 -->
                        <div class="popover-container">
              <div v-for="item of categoryList" :key="item.id" class="category-item" @click="addCategory(item.categoryName)">
                                {{ item.categoryName }}
                            </div>
                        </div>
                    </el-popover>
          <el-button
            v-if="!articleForm.categoryName"
            type="primary"
            :loading="aiCategoryLoading"
            :icon="MagicStick"
            style="margin-left: 8px"
            @click="handleAiCategory"
          >
            AI 选择
          </el-button>
                </el-form-item>
                <!-- 文章标签 -->
                <el-form-item label="文章标签" prop="tagNameList">
          <el-tag
            v-for="(item, index) of articleForm.tagNameList"
            :key="index"
            :disable-transitions="true"
            :closable="true"
            @close="removeTag(item)"
            style="margin-right: 1rem"
          >
                        {{ item }}
                    </el-tag>
                    <!-- 标签选项 -->
          <el-popover placement="bottom-start" width="460" trigger="click" v-if="articleForm.tagNameList.length < 3">
                        <template #reference>
                            <el-button type="success" plain>添加标签</el-button>
                        </template>
                        <div class="popover-title">标签</div>
                        <!-- 搜索框 -->
            <el-autocomplete
              style="width: 100%"
              v-model="tagName"
              :fetch-suggestions="searchTag"
              placeholder="请输入标签名搜索,enter可添加自定义标签"
              :trigger-on-focus="false"
              @keyup.enter="saveTag"
              @select="handleSelectTag"
            >
                            <template #default="{ item }">
                                <div>{{ item.tagName }}</div>
                            </template>
                        </el-autocomplete>
                        <!-- 标签 -->
                        <div class="popover-container">
                            <div style="margin-bottom: 1rem">添加标签</div>
              <el-tag
                v-for="(item, index) of tagList"
                :key="index"
                :class="tagClass(item.tagName)"
                @click="addTag(item.tagName)"
                style="margin-right: 1rem"
              >
                                {{ item.tagName }}
                            </el-tag>
                        </div>
                    </el-popover>
          <el-button
            v-if="articleForm.tagNameList.length === 0"
            type="primary"
            :loading="aiTagLoading"
            :icon="MagicStick"
            style="margin-left: 8px"
            @click="handleAiTags"
          >
            AI 选择
          </el-button>
                </el-form-item>
                <!-- 文章类型 -->
                <el-form-item label="文章类型" prop="articleType">
                    <el-select v-model="articleForm.articleType" placeholder="请选择类型">
            <el-option v-for="item in typeList" :key="item.value" :label="item.label" :value="item.value"> </el-option>
                    </el-select>
                </el-form-item>
                <!-- 缩略图 -->
                <el-form-item label="缩略图" prop="articleCover">
          <div style="display: flex; flex-direction: column; gap: 8px">
            <el-upload
              drag
              :show-file-list="false"
              :headers="authorization"
              action="/api/admin/article/upload"
              accept="image/*"
              :before-upload="beforeUpload"
              :on-success="handleSuccess"
            >
              <el-icon class="el-icon--upload" v-if="articleForm.articleCover === ''"><upload-filled /></el-icon>
              <div class="el-upload__text" v-if="articleForm.articleCover === ''">将文件拖到此处，或<em>点击上传</em></div>
                        <img v-else :src="articleForm.articleCover" width="360" />
                    </el-upload>
            <!-- AI 生成封面 -->
            <div style="display: flex; align-items: center; gap: 8px; flex-wrap: wrap">
              <el-button type="primary" :loading="coverLoading" :icon="MagicStick" @click="handleGenerateCover">
                {{ coverLoading ? "生成中..." : "AI 生成封面" }}
              </el-button>
              <el-radio-group v-model="coverStyle" size="small">
                <el-radio-button label="gradient">炫彩渐变</el-radio-button>
                <el-radio-button label="tech">科技网格</el-radio-button>
                <el-radio-button label="minimal">温暖简约</el-radio-button>
                <el-radio-button label="dark">深邃暗夜</el-radio-button>
              </el-radio-group>
            </div>
          </div>
                </el-form-item>
                <!-- 置顶 -->
                <el-form-item label="置顶" prop="isTop">
                    <el-switch v-model="articleForm.isTop" :active-value="1" :inactive-value="0"></el-switch>
                </el-form-item>
                <!-- 推荐 -->
                <el-form-item label="推荐" prop="isRecommend">
                    <el-switch v-model="articleForm.isRecommend" :active-value="1" :inactive-value="0"></el-switch>
                </el-form-item>
                <!-- 发布形式 -->
                <el-form-item label="发布形式" prop="status">
                    <el-radio-group v-model="articleForm.status">
                        <el-radio :label="1">公开</el-radio>
                        <el-radio :label="2">私密</el-radio>
                        <el-radio :label="3">草稿</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="文章摘要" prop="articleDesc">
          <div style="display: flex; flex-direction: column; gap: 8px">
            <el-input
              v-model="articleForm.articleDesc"
              resize="none"
              maxlength="100"
              :autosize="{ minRows: 5, maxRows: 5 }"
              style="width: 300px"
              show-word-limit
              type="textarea"
            />
            <el-button type="primary" :loading="aiLoading" :icon="MagicStick" style="width: 120px" @click="handleAiSummary">
              {{ aiLoading ? "AI生成中..." : "AI 生成" }}
            </el-button>
          </div>
                </el-form-item>
            </el-form>
            <template #footer>
                <div class="dialog-footer">
                    <el-button v-if="articleForm.status != 3" type="danger" @click="submitForm">发布文章</el-button>
                    <el-button v-else type="danger" @click="submitForm">保存草稿</el-button>
                    <el-button @click="addOrUpdate = false">取 消</el-button>
                </div>
            </template>
        </el-dialog>
    <!-- 隐藏的 Canvas 用于生成封面 -->
    <canvas ref="coverCanvas" width="800" height="450" style="display: none"></canvas>
    </div>
</template>

<script setup lang="ts">
import {
  addArticle,
  editArticle,
  generateAiSummary,
  generateAiTitle,
  generateAiCategory,
  generateAiTags,
  getCategoryOption,
  getTagOption,
  updateArticle,
  uploadArticleCover,
} from "@/api/article"
import { ArticleForm, CategoryVO, TagVO } from "@/api/article/types"
import EmojiExtension from "@/components/EmojiExtension/index.vue"
import { toolbars } from "@/components/EmojiExtension/staticConfig"
import router from "@/router"
import useStore from "@/store"
import { notifySuccess } from "@/utils/modal"
import { getToken, token_prefix } from "@/utils/token"
import { useDark, useDateFormat } from "@vueuse/core"
import { AxiosError, AxiosResponse } from "axios"
import { ElMessage, FormInstance, FormRules, UploadRawFile } from "element-plus"
import { MagicStick, Loading, CircleCheckFilled, Check } from "@element-plus/icons-vue"
import { diffLines } from "diff"
import * as imageConversion from "image-conversion"
import type { ExposeParam, InsertContentGenerator } from "md-editor-v3"
import MdEditor from "md-editor-v3"
import "md-editor-v3/lib/style.css"
import { computed, onMounted, reactive, ref, toRefs } from "vue"
import { useRoute } from "vue-router"

const route = useRoute()
const articleId = route.params.articleId
const editorRef = ref<ExposeParam>()
const articleFormRef = ref<FormInstance>()
const articleTitle = ref(useDateFormat(new Date(), "YYYY-MM-DD"))
const { tag } = useStore()
const rules = reactive<FormRules>({
    categoryName: [{ required: true, message: "文章分类不能为空", trigger: "blur" }],
    tagNameList: [{ required: true, message: "文章标签不能为空", trigger: "blur" }],
    articleDesc: [{ required: true, message: "文章概要不能为空", trigger: "blur" }],
})
const authorization = computed(() => {
    return {
        Authorization: token_prefix + getToken(),
    }
})
const isDark = useDark()
const tagClass = computed(() => {
    return function (item: string) {
    const index = articleForm.value.tagNameList.indexOf(item)
    return index !== -1 ? "tag-item-select" : "tag-item"
  }
})

const initArticle = {
    id: undefined,
    articleCover: "",
    articleTitle: articleTitle.value,
    articleContent: "",
    articleDesc: "",
    categoryName: "",
    tagNameList: [],
    articleType: 1,
    isTop: 0,
    isRecommend: 0,
    status: 1,
} as ArticleForm
const data = reactive({
    addOrUpdate: false,
    typeList: [
    { value: 1, label: "原创" },
    { value: 2, label: "转载" },
    { value: 3, label: "翻译" },
    ],
    articleForm: initArticle,
    categoryList: [] as CategoryVO[],
    tagList: [] as TagVO[],
    categoryName: "",
    tagName: "",
})
const { addOrUpdate, typeList, articleForm, categoryList, tagList, categoryName, tagName } = toRefs(data)

// ======================== AI 生成标题 ========================
const aiTitleLoading = ref(false)
const handleAiTitle = async () => {
  const content = articleForm.value.articleContent
  if (!content || content.trim() === "") {
    ElMessage.warning("请先编写文章内容")
    return
  }
  aiTitleLoading.value = true
  try {
    const { data } = await generateAiTitle(content)
    if (data.flag && data.data) {
      articleForm.value.articleTitle = data.data
      ElMessage.success("AI 标题生成成功")
    } else {
      ElMessage.error(data.msg || "AI 标题生成失败")
    }
  } catch {
    ElMessage.error("AI 标题生成失败，请稍后重试")
  } finally {
    aiTitleLoading.value = false
  }
}

// ======================== AI 优化文章 ========================
const MAX_OPTIMIZE_CHARS = 8000
const MAX_OUTPUT_TOKENS = 8192

const optimizeVisible = ref(false)
const optimizeLoading = ref(false)
const optimizedContent = ref("")

// Diff 对比相关
interface DiffBlock {
  id: number
  type: "equal" | "change"
  original: string // 原始文本（含换行）
  optimized: string // 优化后文本（含换行）
  accepted: boolean
}

const diffBlocks = ref<DiffBlock[]>([])

// 获取显示行
const getDisplayLines = (text: string): string[] => {
  if (!text) return []
  const lines = text.split("\n")
  // 移除尾部空行（diffLines 产生的尾部 \n）
  if (lines.length > 0 && lines[lines.length - 1] === "") {
    lines.pop()
  }
  // 空行显示为不间断空格（保持行高）
  return lines.map((l) => (l === "" ? "\u00A0" : l))
}

// 计算 diff 块
const computeDiffBlocks = (original: string, optimized: string) => {
  const changes = diffLines(original, optimized)
  const blocks: DiffBlock[] = []
  let blockId = 0
  let i = 0

  while (i < changes.length) {
    const change = changes[i]

    if (!change.added && !change.removed) {
      // 相同内容
      blocks.push({
        id: blockId++,
        type: "equal",
        original: change.value,
        optimized: change.value,
        accepted: false,
      })
      i++
    } else if (change.removed && i + 1 < changes.length && changes[i + 1].added) {
      // 删除 + 新增 = 修改
      blocks.push({
        id: blockId++,
        type: "change",
        original: change.value,
        optimized: changes[i + 1].value,
        accepted: false,
      })
      i += 2
    } else if (change.removed) {
      // 仅删除
      blocks.push({
        id: blockId++,
        type: "change",
        original: change.value,
        optimized: "",
        accepted: false,
      })
      i++
    } else if (change.added) {
      // 仅新增
      blocks.push({
        id: blockId++,
        type: "change",
        original: "",
        optimized: change.value,
        accepted: false,
      })
      i++
    } else {
      i++
    }
  }

  diffBlocks.value = blocks
}

// 统计
const changeCount = computed(() => diffBlocks.value.filter((b) => b.type === "change").length)
const acceptedCount = computed(() => diffBlocks.value.filter((b) => b.type === "change" && b.accepted).length)

// 切换单个块的接受状态
const toggleBlock = (blockId: number) => {
  const block = diffBlocks.value.find((b) => b.id === blockId)
  if (block) {
    block.accepted = !block.accepted
  }
}

// 全部接受
const acceptAllBlocks = () => {
  diffBlocks.value.forEach((b) => {
    if (b.type === "change") {
      b.accepted = true
    }
  })
}

// 应用已选修改
const applySelectedBlocks = () => {
  let result = ""
  for (const block of diffBlocks.value) {
    if (block.type === "equal") {
      result += block.original
    } else if (block.accepted) {
      result += block.optimized
    } else {
      result += block.original
    }
  }
  articleForm.value.articleContent = result
  optimizeVisible.value = false
  ElMessage.success(`已应用 ${acceptedCount.value} 处修改`)
}

// 全部替换
const handleReplaceAll = () => {
  articleForm.value.articleContent = optimizedContent.value
  optimizeVisible.value = false
  ElMessage.success("已替换为优化后的全部内容")
}

const handleOptimize = () => {
  const content = articleForm.value.articleContent
  if (!content || content.trim() === "") {
    ElMessage.warning("请先编写文章内容")
    return
  }
  // Token 长度预校验
  if (content.length > MAX_OPTIMIZE_CHARS) {
    ElMessage.error(
      `文章内容过长（当前 ${content.length} 字符），最多支持 ${MAX_OPTIMIZE_CHARS} 字符（约 ${Math.round(
        (MAX_OPTIMIZE_CHARS * 2) / 3
      )} tokens），AI 单次最大输出 ${MAX_OUTPUT_TOKENS} tokens。请精简文章后重试。`
    )
    return
  }

  optimizeVisible.value = true
  optimizeLoading.value = true
  optimizedContent.value = ""
  diffBlocks.value = []

  const headers: Record<string, string> = {
    "Content-Type": "application/json;charset=UTF-8",
  }
  const tokenVal = getToken()
  if (tokenVal) {
    headers["Authorization"] = token_prefix + tokenVal
  }

  fetch(`/api/admin/ai/optimize`, {
    method: "POST",
    headers,
    body: JSON.stringify({ content }),
  })
    .then(async (response) => {
      // 检查是否为 JSON 错误响应（后端校验失败时）
      const contentType = response.headers.get("content-type") || ""
      if (contentType.includes("application/json")) {
        try {
          const result = await response.json()
          if (!result.flag) {
            ElMessage.error(result.msg || "AI 优化请求失败")
          }
        } catch {
          ElMessage.error("AI 优化请求失败")
        }
        optimizeLoading.value = false
        optimizeVisible.value = false
        return
      }

      if (!response.ok) {
        ElMessage.error("AI 优化请求失败")
        optimizeLoading.value = false
        return
      }
      const reader = response.body?.getReader()
      if (!reader) {
        optimizeLoading.value = false
        return
      }
      const decoder = new TextDecoder("utf-8")
      let buffer = ""
      while (true) {
        const { done, value } = await reader.read()
        if (done) {
          optimizeLoading.value = false
          // 流式结束后计算 diff
          if (optimizedContent.value) {
            computeDiffBlocks(content, optimizedContent.value)
          }
          break
        }
        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split("\n")
        buffer = lines.pop() || ""
        for (const line of lines) {
          const trimmed = line.trim()
          if (!trimmed) continue
          if (trimmed.startsWith("data:")) {
            const sseData = trimmed.slice(5)
            if (!sseData) continue
            if (sseData === "[DONE]") {
              optimizeLoading.value = false
              // 流式结束后计算 diff
              if (optimizedContent.value) {
                computeDiffBlocks(content, optimizedContent.value)
              }
              return
            }
            if (sseData.startsWith("[ERROR]")) {
              ElMessage.error("AI 优化失败")
              optimizeLoading.value = false
              return
            }
            try {
              const parsed = JSON.parse(sseData)
              if (parsed && typeof parsed.content === "string") {
                optimizedContent.value += parsed.content
              }
            } catch {
              optimizedContent.value += sseData
            }
          }
        }
      }
    })
    .catch(() => {
      ElMessage.error("AI 优化请求异常")
      optimizeLoading.value = false
    })
}

// ======================== AI 自动选择分类 ========================
const aiCategoryLoading = ref(false)
const handleAiCategory = async () => {
  const content = articleForm.value.articleContent
  if (!content || content.trim() === "") {
    ElMessage.warning("请先编写文章内容")
    return
  }
  aiCategoryLoading.value = true
  try {
    // 确保分类列表已加载
    if (categoryList.value.length === 0) {
      const res = await getCategoryOption()
      categoryList.value = res.data.data
    }
    const categoriesStr = categoryList.value.map((c) => c.categoryName).join(",")
    const { data } = await generateAiCategory(content, categoriesStr)
    if (data.flag && data.data) {
      articleForm.value.categoryName = data.data.trim()
      ElMessage.success(`AI 已选择分类: ${articleForm.value.categoryName}`)
    } else {
      ElMessage.error(data.msg || "AI 分类选择失败")
    }
  } catch {
    ElMessage.error("AI 分类选择失败，请稍后重试")
  } finally {
    aiCategoryLoading.value = false
  }
}

// ======================== AI 自动选择标签 ========================
const aiTagLoading = ref(false)
const handleAiTags = async () => {
  const content = articleForm.value.articleContent
  if (!content || content.trim() === "") {
    ElMessage.warning("请先编写文章内容")
    return
  }
  aiTagLoading.value = true
  try {
    // 确保标签列表已加载
    if (tagList.value.length === 0) {
      const res = await getTagOption()
      tagList.value = res.data.data
    }
    const tagsStr = tagList.value.map((t) => t.tagName).join(",")
    const { data } = await generateAiTags(content, tagsStr)
    if (data.flag && data.data) {
      const newTags = data.data
      newTags.forEach((t: string) => {
        if (articleForm.value.tagNameList.indexOf(t) === -1 && articleForm.value.tagNameList.length < 3) {
          articleForm.value.tagNameList.push(t)
        }
      })
      ElMessage.success(`AI 已选择标签: ${newTags.join(", ")}`)
    } else {
      ElMessage.error(data.msg || "AI 标签选择失败")
    }
  } catch {
    ElMessage.error("AI 标签选择失败，请稍后重试")
  } finally {
    aiTagLoading.value = false
  }
}

// ======================== AI 生成封面 ========================
const coverCanvas = ref<HTMLCanvasElement>()
const coverStyle = ref("gradient")
const coverLoading = ref(false)

interface CoverTheme {
  bg: string[]
  bgAngle?: number
  textColor: string
  subColor: string
  accentColor: string
  decorColor: string
}

const COVER_STYLES: Record<string, CoverTheme> = {
  gradient: {
    bg: ["#667eea", "#764ba2", "#f093fb"],
    textColor: "#ffffff",
    subColor: "rgba(255,255,255,0.6)",
    accentColor: "rgba(255,255,255,0.15)",
    decorColor: "rgba(255,255,255,0.08)",
  },
  tech: {
    bg: ["#0a0a2e", "#16213e", "#0f3460"],
    textColor: "#00d2ff",
    subColor: "rgba(0,210,255,0.4)",
    accentColor: "rgba(0,210,255,0.12)",
    decorColor: "rgba(0,210,255,0.06)",
  },
  minimal: {
    bg: ["#ffecd2", "#fcb69f", "#ff9a9e"],
    textColor: "#2d3436",
    subColor: "rgba(45,52,54,0.4)",
    accentColor: "rgba(255,255,255,0.5)",
    decorColor: "rgba(45,52,54,0.05)",
  },
  dark: {
    bg: ["#1a1a2e", "#16213e", "#0f3460"],
    textColor: "#e2e8f0",
    subColor: "rgba(226,232,240,0.4)",
    accentColor: "rgba(226,232,240,0.08)",
    decorColor: "rgba(226,232,240,0.04)",
  },
}

// 基于标题内容的确定性随机，让同一标题生成相同的封面
const seedRandom = (seed: number) => {
  let s = seed
  return () => {
    s = (s * 16807 + 0) % 2147483647
    return (s - 1) / 2147483646
  }
}

const titleToSeed = (title: string): number => {
  let hash = 0
  for (let i = 0; i < title.length; i++) {
    hash = (hash << 5) - hash + title.charCodeAt(i)
    hash |= 0
  }
  return Math.abs(hash) || 1
}

// 绘制几何装饰
const drawGeometricDecor = (ctx: CanvasRenderingContext2D, w: number, h: number, theme: CoverTheme, rand: () => number) => {
  const shapes = Math.floor(rand() * 4) + 6

  for (let i = 0; i < shapes; i++) {
    const x = rand() * w
    const y = rand() * h
    const size = rand() * 80 + 20
    const shapeType = Math.floor(rand() * 5)

    ctx.save()
    ctx.translate(x, y)
    ctx.rotate(rand() * Math.PI * 2)
    ctx.globalAlpha = rand() * 0.08 + 0.03

    switch (shapeType) {
      case 0: // 圆环
        ctx.beginPath()
        ctx.arc(0, 0, size, 0, Math.PI * 2)
        ctx.strokeStyle = theme.textColor
        ctx.lineWidth = rand() * 3 + 1
        ctx.stroke()
        break
      case 1: // 三角形
        ctx.beginPath()
        ctx.moveTo(0, -size)
        ctx.lineTo(size * 0.866, size * 0.5)
        ctx.lineTo(-size * 0.866, size * 0.5)
        ctx.closePath()
        ctx.strokeStyle = theme.textColor
        ctx.lineWidth = 2
        ctx.stroke()
        break
      case 2: // 小方块
        ctx.fillStyle = theme.textColor
        ctx.fillRect(-size / 2, -size / 2, size, size)
        break
      case 3: // 十字
        ctx.strokeStyle = theme.textColor
        ctx.lineWidth = 2
        ctx.beginPath()
        ctx.moveTo(-size / 2, 0)
        ctx.lineTo(size / 2, 0)
        ctx.moveTo(0, -size / 2)
        ctx.lineTo(0, size / 2)
        ctx.stroke()
        break
      case 4: // 虚线圆
        ctx.beginPath()
        ctx.setLineDash([4, 6])
        ctx.arc(0, 0, size, 0, Math.PI * 2)
        ctx.strokeStyle = theme.textColor
        ctx.lineWidth = 1.5
        ctx.stroke()
        ctx.setLineDash([])
        break
    }

    ctx.restore()
  }
}

// 绘制流动波浪线
const drawWaves = (ctx: CanvasRenderingContext2D, w: number, h: number, theme: CoverTheme, rand: () => number) => {
  const waveCount = 3
  for (let wave = 0; wave < waveCount; wave++) {
    ctx.save()
    ctx.globalAlpha = 0.06 + wave * 0.02
    ctx.strokeStyle = theme.textColor
    ctx.lineWidth = 1.5 + wave * 0.5
    ctx.beginPath()

    const baseY = h * (0.65 + wave * 0.1)
    const amplitude = 15 + rand() * 20
    const frequency = 0.005 + rand() * 0.005
    const phase = rand() * Math.PI * 2

    for (let x = 0; x <= w; x += 2) {
      const y = baseY + Math.sin(x * frequency + phase) * amplitude
      if (x === 0) ctx.moveTo(x, y)
      else ctx.lineTo(x, y)
    }
    ctx.stroke()
    ctx.restore()
  }
}

// 绘制粒子/光点
const drawParticles = (ctx: CanvasRenderingContext2D, w: number, h: number, theme: CoverTheme, rand: () => number) => {
  const count = Math.floor(rand() * 20) + 15
  for (let i = 0; i < count; i++) {
    const x = rand() * w
    const y = rand() * h
    const r = rand() * 3 + 0.5
    const alpha = rand() * 0.3 + 0.05

    ctx.save()
    ctx.globalAlpha = alpha
    ctx.fillStyle = theme.textColor
    ctx.beginPath()
    ctx.arc(x, y, r, 0, Math.PI * 2)
    ctx.fill()

    // 光晕
    if (r > 2) {
      const glow = ctx.createRadialGradient(x, y, 0, x, y, r * 4)
      glow.addColorStop(0, theme.accentColor)
      glow.addColorStop(1, "transparent")
      ctx.globalAlpha = alpha * 0.5
      ctx.fillStyle = glow
      ctx.beginPath()
      ctx.arc(x, y, r * 4, 0, Math.PI * 2)
      ctx.fill()
    }

    ctx.restore()
  }
}

// 绘制连线网格（tech 风格专属）
const drawGrid = (ctx: CanvasRenderingContext2D, w: number, h: number, theme: CoverTheme, rand: () => number) => {
  const nodeCount = 12
  const nodes: { x: number; y: number }[] = []
  for (let i = 0; i < nodeCount; i++) {
    nodes.push({ x: rand() * w, y: rand() * h })
  }

  ctx.save()
  ctx.globalAlpha = 0.06
  ctx.strokeStyle = theme.textColor
  ctx.lineWidth = 0.8

  for (let i = 0; i < nodes.length; i++) {
    for (let j = i + 1; j < nodes.length; j++) {
      const dist = Math.hypot(nodes[i].x - nodes[j].x, nodes[i].y - nodes[j].y)
      if (dist < 250) {
        ctx.beginPath()
        ctx.moveTo(nodes[i].x, nodes[i].y)
        ctx.lineTo(nodes[j].x, nodes[j].y)
        ctx.stroke()
      }
    }
  }

  // 节点
  ctx.globalAlpha = 0.15
  ctx.fillStyle = theme.textColor
  for (const node of nodes) {
    ctx.beginPath()
    ctx.arc(node.x, node.y, 2.5, 0, Math.PI * 2)
    ctx.fill()
  }

  ctx.restore()
}

// 绘制大号装饰性文字/图案（模糊背景字）
const drawBackgroundAccent = (ctx: CanvasRenderingContext2D, w: number, h: number, theme: CoverTheme, rand: () => number, title: string) => {
  ctx.save()
  ctx.globalAlpha = 0.04

  // 使用标题第一个字作为巨型背景字
  const firstChar = title.charAt(0)
  ctx.font = `bold 280px "Microsoft YaHei", "PingFang SC", sans-serif`
  ctx.fillStyle = theme.textColor
  ctx.textAlign = "center"
  ctx.textBaseline = "middle"
  ctx.translate(w * (0.2 + rand() * 0.6), h * (0.3 + rand() * 0.4))
  ctx.rotate((rand() - 0.5) * 0.3)
  ctx.fillText(firstChar, 0, 0)

  ctx.restore()
}

// 绘制文本投影
const drawTextShadow = (ctx: CanvasRenderingContext2D, text: string, x: number, y: number, color: string) => {
  ctx.save()
  ctx.shadowColor = color
  ctx.shadowBlur = 20
  ctx.shadowOffsetX = 0
  ctx.shadowOffsetY = 2
  ctx.fillText(text, x, y)
  ctx.restore()
}

const handleGenerateCover = async () => {
  const title = articleForm.value.articleTitle
  if (!title || title.trim() === "") {
    ElMessage.warning("请先输入文章标题")
    return
  }
  const canvas = coverCanvas.value
  if (!canvas) return

  coverLoading.value = true
  try {
    const ctx = canvas.getContext("2d")!
    const theme = COVER_STYLES[coverStyle.value] || COVER_STYLES.gradient
    const w = canvas.width
    const h = canvas.height
    const seed = titleToSeed(title + coverStyle.value)
    const rand = seedRandom(seed)

    // 1. 绘制多色渐变背景
    const angle = rand() * Math.PI
    const gx = Math.cos(angle) * w
    const gy = Math.sin(angle) * h
    const gradient = ctx.createLinearGradient(w / 2 - gx / 2, h / 2 - gy / 2, w / 2 + gx / 2, h / 2 + gy / 2)
    const stops = theme.bg.length
    theme.bg.forEach((color, i) => {
      gradient.addColorStop(i / (stops - 1), color)
    })
    ctx.fillStyle = gradient
    ctx.fillRect(0, 0, w, h)

    // 2. 叠加柔和的径向渐变光效
    const radGrad = ctx.createRadialGradient(w * (0.3 + rand() * 0.4), h * (0.2 + rand() * 0.3), 0, w * 0.5, h * 0.5, w * 0.8)
    radGrad.addColorStop(0, theme.accentColor)
    radGrad.addColorStop(1, "transparent")
    ctx.fillStyle = radGrad
    ctx.fillRect(0, 0, w, h)

    // 3. 绘制背景装饰大字
    drawBackgroundAccent(ctx, w, h, theme, rand, title)

    // 4. 绘制装饰元素（按风格选择不同组合）
    const styleName = coverStyle.value
    if (styleName === "tech") {
      drawGrid(ctx, w, h, theme, rand)
      drawParticles(ctx, w, h, theme, rand)
    } else if (styleName === "minimal") {
      drawGeometricDecor(ctx, w, h, theme, rand)
    } else if (styleName === "dark") {
      drawParticles(ctx, w, h, theme, rand)
      drawWaves(ctx, w, h, theme, rand)
    } else {
      // gradient
      drawGeometricDecor(ctx, w, h, theme, rand)
      drawWaves(ctx, w, h, theme, rand)
      drawParticles(ctx, w, h, theme, rand)
    }

    // 5. 绘制半透明装饰条
    ctx.save()
    ctx.globalAlpha = 0.08
    ctx.fillStyle = theme.textColor
    const barY = h * 0.42
    ctx.fillRect(w * 0.08, barY, w * 0.84, 2)
    ctx.fillRect(w * 0.08, barY + h * 0.22, w * 0.84, 2)
    ctx.restore()

    // 6. 绘制标题文字（带投影）
    const maxWidth = w - 140
    ctx.textAlign = "center"
    ctx.textBaseline = "middle"

    let fontSize = 44
    ctx.font = `bold ${fontSize}px "Microsoft YaHei", "PingFang SC", sans-serif`
    const lines = wrapText(ctx, title, maxWidth)

    // 如果标题超过2行，适当减小字号
    if (lines.length > 2) {
      fontSize = 36
      ctx.font = `bold ${fontSize}px "Microsoft YaHei", "PingFang SC", sans-serif`
      lines.length = 0
      lines.push(...wrapText(ctx, title, maxWidth))
    }

    const lineHeight = fontSize * 1.5
    const startY = h * 0.5 - ((lines.length - 1) * lineHeight) / 2

    // 标题文字背景毛玻璃区域
    ctx.save()
    ctx.globalAlpha = 0.12
    ctx.fillStyle = theme.bg[0]
    const textBlockH = lines.length * lineHeight + 40
    const textBlockY = startY - lineHeight / 2 - 20
    const radius = 16
    ctx.beginPath()
    ctx.moveTo(w * 0.06 + radius, textBlockY)
    ctx.lineTo(w * 0.94 - radius, textBlockY)
    ctx.quadraticCurveTo(w * 0.94, textBlockY, w * 0.94, textBlockY + radius)
    ctx.lineTo(w * 0.94, textBlockY + textBlockH - radius)
    ctx.quadraticCurveTo(w * 0.94, textBlockY + textBlockH, w * 0.94 - radius, textBlockY + textBlockH)
    ctx.lineTo(w * 0.06 + radius, textBlockY + textBlockH)
    ctx.quadraticCurveTo(w * 0.06, textBlockY + textBlockH, w * 0.06, textBlockY + textBlockH - radius)
    ctx.lineTo(w * 0.06, textBlockY + radius)
    ctx.quadraticCurveTo(w * 0.06, textBlockY, w * 0.06 + radius, textBlockY)
    ctx.closePath()
    ctx.fill()
    ctx.restore()

    // 绘制文字
    ctx.fillStyle = theme.textColor
    ctx.font = `bold ${fontSize}px "Microsoft YaHei", "PingFang SC", sans-serif`
    lines.forEach((line, i) => {
      const y = startY + i * lineHeight
      drawTextShadow(ctx, line, w / 2, y, "rgba(0,0,0,0.3)")
      ctx.fillStyle = theme.textColor
      ctx.fillText(line, w / 2, y)
    })

    // 7. 绘制底部日期和分隔线
    ctx.font = `14px "Microsoft YaHei", "PingFang SC", sans-serif`
    ctx.fillStyle = theme.subColor
    const date = new Date().toISOString().slice(0, 10)
    ctx.fillText(date, w / 2, h - 35)

    // 底部小装饰点
    ctx.save()
    ctx.globalAlpha = 0.25
    ctx.fillStyle = theme.textColor
    const dotY = h - 55
    for (let i = -2; i <= 2; i++) {
      ctx.beginPath()
      ctx.arc(w / 2 + i * 12, dotY, 2, 0, Math.PI * 2)
      ctx.fill()
    }
    ctx.restore()

    // 转为 Blob 并上传
    canvas.toBlob(async (blob) => {
      if (!blob) {
        ElMessage.error("封面生成失败")
        coverLoading.value = false
        return
      }
      const file = new File([blob], `cover-${Date.now()}.png`, { type: "image/png" })
      const formData = new FormData()
      formData.append("file", file)
      try {
        const { data } = await uploadArticleCover(formData)
        if (data.flag) {
          articleForm.value.articleCover = data.data
          ElMessage.success("封面生成并上传成功")
        } else {
          ElMessage.error("封面上传失败")
        }
      } catch {
        ElMessage.error("封面上传失败")
      } finally {
        coverLoading.value = false
      }
    }, "image/png")
  } catch {
    ElMessage.error("封面生成异常")
    coverLoading.value = false
  }
}

const wrapText = (ctx: CanvasRenderingContext2D, text: string, maxWidth: number): string[] => {
  const lines: string[] = []
  let currentLine = ""
  for (const char of text) {
    const testLine = currentLine + char
    if (ctx.measureText(testLine).width > maxWidth) {
      lines.push(currentLine)
      currentLine = char
    } else {
      currentLine = testLine
    }
  }
  if (currentLine) lines.push(currentLine)
  return lines.length > 3 ? lines.slice(0, 3) : lines
}

// ======================== 原有功能 ========================

const uploadImg = async (files: Array<File>, callback: (urls: string[]) => void) => {
    const res = await Promise.all(
        files.map((file) => {
            return new Promise((rev, rej) => {
        const form = new FormData()
        form.append("file", file)
        uploadArticleCover(form)
          .then(({ data }) => {
                    if (data.flag) {
              rev(data)
            }
          })
          .catch((error: AxiosError) => rej(error))
      })
    })
  )
  callback(res.map((item: any) => item.data))
}
const openModel = () => {
    if (articleForm.value.articleTitle.trim() == "") {
    ElMessage.error("文章标题不能为空")
    return false
    }
    if (articleForm.value.articleContent.trim() == "") {
    ElMessage.error("文章内容不能为空")
    return false
    }
  articleFormRef.value?.clearValidate()
    getCategoryOption().then(({ data }) => {
    categoryList.value = data.data
  })
    getTagOption().then(({ data }) => {
    tagList.value = data.data
  })
    if (articleForm.value.articleDesc === "") {
    articleForm.value.articleDesc = removeSpecialChars(articleForm.value.articleContent)
    }
  addOrUpdate.value = true
}
const removeSpecialChars = (str: string) => {
  var cleanedStr = str.replace(/<[^>]*>?/g, "")
  cleanedStr = cleanedStr.replace(/[\s\*#\[\]]/g, "")
  cleanedStr = cleanedStr.replace(/\([^\)]*\)/g, "")
  cleanedStr = cleanedStr.replace(/[\n\t]+/g, "")
  return cleanedStr.substring(0, 100)
}

const aiLoading = ref(false)
const handleAiSummary = async () => {
  const content = articleForm.value.articleContent
  if (!content || content.trim() === "") {
    ElMessage.warning("请先编写文章内容")
    return
  }
  aiLoading.value = true
  try {
    const { data } = await generateAiSummary(content)
    if (data.flag && data.data) {
      articleForm.value.articleDesc = data.data
      ElMessage.success("AI 摘要生成成功")
    } else {
      ElMessage.error(data.msg || "AI 摘要生成失败")
    }
  } catch (e: any) {
    ElMessage.error("AI 摘要生成失败，请稍后重试")
  } finally {
    aiLoading.value = false
  }
}

const removeTag = (item: string) => {
  const index = articleForm.value.tagNameList.indexOf(item)
  articleForm.value.tagNameList.splice(index, 1)
}
const handleSelectTag = (item: TagVO) => {
  addTag(item.tagName)
}
const saveTag = () => {
    if (tagName.value.trim() != "") {
    addTag(tagName.value)
    tagName.value = ""
    }
}
const addTag = (item: string) => {
    if (articleForm.value.tagNameList.indexOf(item) == -1) {
    articleForm.value.tagNameList.push(item)
    }
}
const searchTag = (keyword: string, cb: (arg: TagVO[]) => void) => {
  const results = keyword ? tagList.value.filter(createTagFilter(keyword)) : tagList.value
  cb(results)
}
const createTagFilter = (queryString: string) => {
  return (restaurant: TagVO) => restaurant.tagName.indexOf(queryString) !== -1
}
const removeCategory = () => {
  articleForm.value.categoryName = ""
}
const handleSelectCategory = (item: CategoryVO) => {
  addCategory(item.categoryName)
}
const saveCategory = () => {
    if (categoryName.value.trim() != "") {
    addCategory(categoryName.value)
    categoryName.value = ""
    }
}
const addCategory = (item: string) => {
  articleForm.value.categoryName = item
}
const searchCategory = (keyword: string, cb: (arg: CategoryVO[]) => void) => {
  const results = keyword ? categoryList.value.filter(createCategoryFilter(keyword)) : categoryList.value
  cb(results)
}
const createCategoryFilter = (queryString: string) => {
    return (restaurant: CategoryVO) => {
    return restaurant.categoryName.indexOf(queryString) !== -1
    }
}
const insert = (generator: InsertContentGenerator) => {
  editorRef.value?.insert(generator)
}
const handleSuccess = (response: AxiosResponse) => {
  articleForm.value.articleCover = response.data
}
const beforeUpload = (rawFile: UploadRawFile) => {
  return new Promise((resolve) => {
        if (rawFile.size / 1024 < 200) {
      resolve(rawFile)
    }
    imageConversion.compressAccurately(rawFile, 200).then((res) => {
      resolve(res)
    })
  })
}
const submitForm = () => {
    articleFormRef.value?.validate((valid) => {
        if (!valid) {
      return
        }
        if (articleForm.value.id !== undefined) {
            updateArticle(articleForm.value).then(({ data }) => {
                if (data.flag) {
          notifySuccess(data.msg)
          tag.delView({ path: `/article/write/${articleForm.value.id}` })
          router.push({ path: "/article/list" })
          articleForm.value = initArticle
        }
        addOrUpdate.value = false
      })
        } else {
            addArticle(articleForm.value).then(({ data }) => {
                if (data.flag) {
          notifySuccess(data.msg)
          tag.delView({ path: "/article/write" })
          router.push({ path: "/article/list" })
          articleForm.value = initArticle
        }
        addOrUpdate.value = false
      })
    }
  })
}
onMounted(() => {
    if (articleId) {
        editArticle(Number(articleId)).then(({ data }) => {
            if (data.flag) {
        articleForm.value = data.data
            } else {
        tag.delView({ path: `/article/write/${articleId}` })
        router.push({ path: "/article/list" })
            }
    })
    }
})
</script>

<style scoped>
.operation-container {
    display: flex;
    align-items: center;
    margin-bottom: 1.25rem;
}

.md-container {
    min-height: 300px;
    height: calc(100vh - 200px);
}

.popover-title {
    margin-bottom: 1rem;
    text-align: center;
}

.popover-container {
    margin-top: 1rem;
    height: 260px;
    overflow-y: auto;
}

.category-item {
    cursor: pointer;
    padding: 0.6rem 0.5rem;
}

.category-item:hover {
    background-color: #f0f9eb;
    color: #67c23a;
}

.tag-item {
    margin-right: 1rem;
    margin-bottom: 1rem;
    cursor: pointer;
}

.tag-item-select {
    margin-right: 1rem;
    margin-bottom: 1rem;
    cursor: not-allowed;
    color: #ccccd8 !important;
}

/* AI 优化对话框 */
.optimize-container {
  min-height: 200px;
}

.optimize-status {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #f0f9ff;
  border-radius: 8px;
  margin-bottom: 16px;
  color: #409eff;
  font-size: 14px;
}

.optimize-progress {
  color: #909399;
  font-size: 12px;
}

.optimize-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

/* ========== Diff 对比视图 ========== */
.diff-viewer {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.diff-stats {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: #606266;
}

.diff-stats-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.diff-stats-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.diff-stats-dot-change {
  background: #e6a23c;
}

.diff-stats-dot-accept {
  background: #67c23a;
}

.diff-panel {
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  overflow: hidden;
}

.diff-header-row {
  display: flex;
  background: #f5f7fa;
  border-bottom: 1px solid #dcdfe6;
  font-weight: 600;
  font-size: 13px;
  color: #303133;
}

.diff-header-row .diff-col {
  padding: 8px 12px;
}

.diff-body {
  max-height: 60vh;
  overflow-y: auto;
}

.diff-block {
  display: flex;
  border-bottom: 1px solid #ebeef5;
}

.diff-block:last-child {
  border-bottom: none;
}

.diff-col {
  flex: 1;
  min-width: 0;
}

.diff-col-left {
  border-right: 1px solid #ebeef5;
}

.diff-col-gutter {
  width: 40px;
  min-width: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-right: 1px solid #ebeef5;
  background: #fafafa;
}

.diff-header-row .diff-col-gutter {
  border-right: 1px solid #dcdfe6;
}

.diff-line {
  padding: 1px 12px;
  font-family: "Consolas", "Monaco", "Courier New", "Source Code Pro", monospace;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
  min-height: 22px;
}

.diff-line-empty {
  color: #c0c4cc;
  font-style: italic;
  padding: 2px 12px;
  font-size: 12px;
}

/* 相同块 */
.diff-block-equal {
  background: transparent;
}

.diff-block-equal .diff-line {
  color: #606266;
}

/* 修改块 - 左侧（删除/原始） */
.diff-col-removed {
  background: rgba(255, 77, 79, 0.06);
  border-left: 3px solid #ff4d4f;
}

.diff-col-removed .diff-line {
  color: #a8071a;
}

/* 修改块 - 右侧（新增/优化） */
.diff-col-added {
  background: rgba(82, 196, 26, 0.06);
  border-left: 3px solid #52c41a;
}

.diff-col-added .diff-line {
  color: #135200;
}

/* 已接受状态 */
.diff-block-accepted .diff-col-accepted-left {
  background: rgba(0, 0, 0, 0.02);
  border-left: 3px solid #dcdfe6;
  text-decoration: line-through;
  opacity: 0.5;
}

.diff-block-accepted .diff-col-accepted-left .diff-line {
  color: #909399;
}

.diff-block-accepted .diff-col-accepted-right {
  background: rgba(82, 196, 26, 0.1);
  border-left: 3px solid #67c23a;
}

.diff-block-accepted .diff-col-accepted-right .diff-line {
  color: #135200;
  font-weight: 500;
}

/* Accept 按钮 */
.diff-accept-btn {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  border: 1px solid #dcdfe6;
  background: #fff;
  color: #409eff;
  font-size: 14px;
  font-weight: bold;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  line-height: 1;
}

.diff-accept-btn:hover {
  background: #ecf5ff;
  border-color: #409eff;
  transform: scale(1.1);
}

.diff-accept-btn-active {
  background: #67c23a;
  border-color: #67c23a;
  color: #fff;
}

.diff-accept-btn-active:hover {
  background: #529b2e;
  border-color: #529b2e;
}
</style>
