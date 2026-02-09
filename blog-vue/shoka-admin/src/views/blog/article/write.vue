<template>
  <div class="app-container">
    <!-- 文章标题 -->
    <div class="operation-container">
      <el-input v-model="articleForm.articleTitle" placeholder="请输入文章标题">
        <template #append>
          <el-button :loading="aiTitleLoading" @click="handleAiTitle" title="AI 生成标题">
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
    <el-dialog title="AI 一键优化文章" v-model="optimizeVisible" width="800px" top="2vh" append-to-body destroy-on-close>
      <div class="optimize-container">
        <div class="optimize-status" v-if="optimizeLoading">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>AI 正在优化文章，请稍候...</span>
        </div>
        <div class="optimize-status optimize-done" v-else-if="optimizedContent">
          <el-icon><CircleCheckFilled /></el-icon>
          <span>优化完成！请选择替换方式</span>
        </div>
        <div class="optimize-preview" v-if="optimizedContent">
          <el-tabs v-model="optimizeTab">
            <el-tab-pane label="优化后内容" name="optimized">
              <div class="preview-scroll">
                <md-editor v-model="optimizedContent" :theme="isDark ? 'dark' : 'light'" preview-only />
              </div>
            </el-tab-pane>
            <el-tab-pane label="原始内容" name="original">
              <div class="preview-scroll">
                <md-editor v-model="articleForm.articleContent" :theme="isDark ? 'dark' : 'light'" preview-only />
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
      <template #footer>
        <el-button @click="optimizeVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!optimizedContent || optimizeLoading" @click="handleReplaceAll"> 全部替换 </el-button>
        <el-button type="success" :disabled="!optimizedContent || optimizeLoading" @click="handleAppendOptimized"> 追加到末尾（对比） </el-button>
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
                <el-radio-button label="gradient">渐变</el-radio-button>
                <el-radio-button label="tech">科技</el-radio-button>
                <el-radio-button label="minimal">简约</el-radio-button>
                <el-radio-button label="dark">暗黑</el-radio-button>
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
import { MagicStick, Loading, CircleCheckFilled } from "@element-plus/icons-vue"
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
const optimizeVisible = ref(false)
const optimizeLoading = ref(false)
const optimizedContent = ref("")
const optimizeTab = ref("optimized")

const handleOptimize = () => {
  const content = articleForm.value.articleContent
  if (!content || content.trim() === "") {
    ElMessage.warning("请先编写文章内容")
    return
  }
  optimizeVisible.value = true
  optimizeLoading.value = true
  optimizedContent.value = ""
  optimizeTab.value = "optimized"

  const headers: Record<string, string> = {
    "Content-Type": "application/json;charset=UTF-8",
  }
  const token = getToken()
  if (token) {
    headers["Authorization"] = token_prefix + token
  }

  fetch(`/api/admin/ai/optimize`, {
    method: "POST",
    headers,
    body: JSON.stringify({ content }),
  })
    .then(async (response) => {
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

const handleReplaceAll = () => {
  articleForm.value.articleContent = optimizedContent.value
  optimizeVisible.value = false
  ElMessage.success("已替换为优化后的内容")
}

const handleAppendOptimized = () => {
  articleForm.value.articleContent += "\n\n---\n\n## AI 优化版本\n\n" + optimizedContent.value
  optimizeVisible.value = false
  ElMessage.success("优化内容已追加到文章末尾")
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

const COVER_STYLES: Record<string, { bg: string[]; textColor: string; subColor: string }> = {
  gradient: {
    bg: ["#667eea", "#764ba2"],
    textColor: "#ffffff",
    subColor: "rgba(255,255,255,0.7)",
  },
  tech: {
    bg: ["#0f2027", "#2c5364"],
    textColor: "#00d2ff",
    subColor: "rgba(0,210,255,0.5)",
  },
  minimal: {
    bg: ["#ffecd2", "#fcb69f"],
    textColor: "#2d3436",
    subColor: "rgba(45,52,54,0.5)",
  },
  dark: {
    bg: ["#232526", "#414345"],
    textColor: "#f5f6fa",
    subColor: "rgba(245,246,250,0.5)",
  },
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
    const style = COVER_STYLES[coverStyle.value] || COVER_STYLES.gradient
    const w = canvas.width
    const h = canvas.height

    // 绘制渐变背景
    const gradient = ctx.createLinearGradient(0, 0, w, h)
    gradient.addColorStop(0, style.bg[0])
    gradient.addColorStop(1, style.bg[1])
    ctx.fillStyle = gradient
    ctx.fillRect(0, 0, w, h)

    // 绘制装饰元素
    ctx.globalAlpha = 0.1
    for (let i = 0; i < 5; i++) {
      ctx.beginPath()
      ctx.arc(Math.random() * w, Math.random() * h, Math.random() * 120 + 40, 0, Math.PI * 2)
      ctx.fillStyle = "#ffffff"
      ctx.fill()
    }
    ctx.globalAlpha = 1

    // 绘制标题文字
    const maxWidth = w - 100
    ctx.textAlign = "center"
    ctx.textBaseline = "middle"

    // 自动换行
    let fontSize = 42
    ctx.font = `bold ${fontSize}px "Microsoft YaHei", "PingFang SC", sans-serif`
    const lines = wrapText(ctx, title, maxWidth)

    ctx.fillStyle = style.textColor
    const lineHeight = fontSize * 1.4
    const startY = h / 2 - ((lines.length - 1) * lineHeight) / 2
    lines.forEach((line, i) => {
      ctx.fillText(line, w / 2, startY + i * lineHeight)
    })

    // 绘制底部日期
    ctx.font = `16px "Microsoft YaHei", "PingFang SC", sans-serif`
    ctx.fillStyle = style.subColor
    const date = new Date().toISOString().slice(0, 10)
    ctx.fillText(date, w / 2, h - 40)

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

.optimize-done {
  background: #f0f9eb;
  color: #67c23a;
}

.preview-scroll {
  max-height: 50vh;
  overflow-y: auto;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 12px;
}
</style>
