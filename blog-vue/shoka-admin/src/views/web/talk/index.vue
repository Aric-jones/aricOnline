<template>
  <div class="app-container">
    <!-- 操作 -->
    <el-row :gutter="10" class="mb15">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Promotion" @click="openModel">发布说说</el-button>
      </el-col>
    </el-row>
    <!-- 说说状态 -->
    <el-row :gutter="24" class="status-filter">
      <el-col :span="1.5" class="status-label">状态</el-col>
      <el-col :span="1.5" :class="isActive(undefined)" @click="changeStatus(undefined)">全部</el-col>
      <el-col :span="1.5" :class="isActive(1)" @click="changeStatus(1)">公开</el-col>
      <el-col :span="1.5" :class="isActive(2)" @click="changeStatus(2)">私密</el-col>
    </el-row>
    <!-- 空状态 -->
    <el-empty v-if="!talkList || talkList.length === 0" description="暂无说说"></el-empty>
    <!-- 说说列表 -->
    <div class="talk-item" v-for="talk of talkList" :key="talk.id">
      <!-- 用户头像 -->
      <img class="user-avatar" :src="talk.avatar" alt="用户头像">
      <div class="talk-content-container">
        <!-- 用户信息 -->
        <div class="user-info">
          <div class="user-name">{{ talk.nickname }}</div>
          <!-- 操作 -->
          <el-dropdown trigger="click" @command="(cmd) => handleOperation(cmd, talk.id)">
            <el-icon class="oper-icon"><MoreFilled /></el-icon>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="update">编辑</el-dropdown-item>
                <el-dropdown-item command="delete">删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        <!-- 说说信息 -->
        <div class="talk-info">
          <span class="talk-time">{{ formatDateTime(talk.createTime) }}</span>
          <span class="tag top" v-if="talk.isTop === 1"><svg-icon icon-class="top" size="0.85rem"></svg-icon>置顶</span>
          <span class="tag secret" v-if="talk.status === 2"><el-icon><Hide /></el-icon>私密</span>
        </div>
        <!-- 说说内容 -->
        <div class="talk-content" v-html="talk.talkContent"></div>
        <!-- 说说图片 -->
        <div class="talk-image-container" v-if="talk.imgList && talk.imgList.length > 0">
          <el-image
              class="talk-image"
              :src="img"
              :preview-src-list="talk.imgList"
              :initial-index="index"
              v-for="(img, index) of talk.imgList"
              :key="index"
              fit="contain"
          ></el-image>
        </div>
      </div>
    </div>
    <!-- 分页 -->
    <el-pagination
        v-if="count > 0"
        class="pagination-container"
        v-model:current-page="queryParams.current"
        v-model:page-size="queryParams.size"
        layout="prev, pager, next"
        :total="count"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
    ></el-pagination>
    <!-- 添加或修改对话框 -->
    <el-dialog :title="title" v-model="addOrUpdate" @close="close" width="750px" append-to-body>
      <el-form :model="talkForm" class="talk-form">
        <el-form-item prop="talkContent">
          <Editor ref="editorRef" v-model:text="talkForm.talkContent" placeholder="说点什么吧"></Editor>
        </el-form-item>
      </el-form>
      <el-row :gutter="10" align="middle" class="dialog-oper-bar">
        <el-col :span="1.5">
          <!-- 表情 -->
          <el-popover placement="bottom-start" :width="460" trigger="click">
            <template #reference>
              <span class="oper-icon"><svg-icon icon-class="emoji" size="1.4rem" color="#838383"></svg-icon></span>
            </template>
            <span class="emoji-item" v-for="(value, key, index) of emojiList" :key="index"
                  @click="addEmoji(key, value)">
                            <img :src="value" :title="key" class="emoji" width="24" height="24" alt="表情" />
                        </span>
          </el-popover>
        </el-col>
        <el-col :span="1.5">
          <!-- 图片上传 -->
          <el-upload
              accept="image/*"
              multiple
              action="/api/admin/talk/upload"
              :headers="authorization"
              :before-upload="beforeUpload"
              :on-success="handleSuccess"
              :show-file-list="false"
          >
            <span class="oper-icon"><svg-icon icon-class="album" size="1.5rem" color="#838383"></svg-icon></span>
          </el-upload>
        </el-col>
        <el-col :span="1.5" :offset="14">
          <el-switch
              v-model="talkForm.isTop"
              inactive-text="置顶"
              :active-value="1"
              :inactive-value="0"
          />
        </el-col>
        <el-col :span="1.5">
          <el-dropdown trigger="click" @command="handleCommand">
                        <span class="el-dropdown-link">
                            {{ statusTitle }}
                            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                        </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-for="(item, index) of statusList" :key="index" :command="item.value">
                  {{ item.label }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </el-col>
        <el-col :span="1.5">
          <el-button type="primary" :disabled="!talkForm.talkContent" @click="submitForm">发布</el-button>
        </el-col>
      </el-row>
      <!-- 图片上传预览 -->
      <el-upload
          v-show="uploadList.length > 0"
          accept="image/*"
          action="/api/admin/talk/upload"
          :headers="authorization"
          list-type="picture-card"
          :file-list="uploadList"
          multiple
          :before-upload="beforeUpload"
          :on-success="handleSuccess"
          :on-remove="handleRemove"
          :on-preview="handlePictureCardPreview"
          class="upload-preview"
      >
        <el-icon><Plus /></el-icon>
      </el-upload>
    </el-dialog>
    <!-- 图片预览 -->
    <el-dialog v-model="dialogVisible" append-to-body title="图片预览">
      <img :src="dialogImageUrl" style="max-width: 100%; max-height: 80vh;" alt="预览图片" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
// 接口请求
import { addTalk, deleteTalk, editTalk, getTalkList, updateTalk } from "@/api/talk";
// 类型定义
import { Talk, TalkForm, TalkQuery } from "@/api/talk/types";
// 组件
import Editor from "@/components/Editor/index.vue";
// 模型
import { Picture } from "@/model";
// 工具函数
import { formatDateTime } from "@/utils/date";
import emojiList from "@/utils/emoji";
import { messageConfirm, notifySuccess } from "@/utils/modal";
import { getToken, token_prefix } from "@/utils/token";
// 第三方依赖
import { AxiosResponse } from "axios";
import { UploadFile, UploadRawFile } from 'element-plus';
import * as imageConversion from 'image-conversion';
// Vue组合式API
import { computed, onMounted, reactive, ref, toRefs } from 'vue';
// Element Plus图标
import { MoreFilled, Hide, ArrowDown, Plus } from '@element-plus/icons-vue';

// 编辑器Ref
const editorRef = ref<{ clear: () => void; addText: (text: string) => void }>();
// 图片预览相关
const dialogImageUrl = ref('');
const dialogVisible = ref(false);

// 状态筛选激活样式
const isActive = computed(() => {
  return (value: number | undefined) => queryParams.value.status === value ? "active-status" : "status";
});

// 请求头鉴权
const authorization = computed(() => ({
  Authorization: token_prefix + getToken(),
}));

// 发布状态文字显示
const statusTitle = computed(() => {
  return statusList.value.find(item => item.value === talkForm.value.status)?.label || "公开";
});

// 响应式数据
const data = reactive({
  count: 0,
  title: "",
  addOrUpdate: false,
  // 分页查询参数
  queryParams: {
    current: 1,
    size: 5,
    status: undefined,
  } as TalkQuery,
  // 状态列表
  statusList: [
    { value: 1, label: "公开" },
    { value: 2, label: "私密" }
  ],
  // 说说表单
  talkForm: {
    id: undefined,
    talkContent: "",
    images: "",
    isTop: 0,
    status: 1,
  } as TalkForm,
  // 说说列表
  talkList: [] as Talk[],
  // 上传文件列表
  uploadList: [] as Picture[],
});

// 解构响应式数据
const {
  count,
  title,
  addOrUpdate,
  queryParams,
  statusList,
  talkForm,
  talkList,
  uploadList,
} = toRefs(data);

// 上传成功回调
const handleSuccess = (response: AxiosResponse) => {
  uploadList.value.push({ url: response.data });
};

// 移除上传文件
const handleRemove = (file: UploadFile) => {
  uploadList.value = uploadList.value.filter(item => item.url !== file.url);
};

// 上传图片预览
const handlePictureCardPreview = (file: UploadFile) => {
  dialogImageUrl.value = file.url!;
  dialogVisible.value = true;
};

// 上传前压缩图片
const beforeUpload = (rawFile: UploadRawFile) => {
  return new Promise((resolve) => {
    if (rawFile.size / 1024 < 200) return resolve(rawFile);
    // 压缩到200KB
    imageConversion.compressAccurately(rawFile, 200).then(res => resolve(res));
  });
};

// 说说操作（编辑/删除）- 优化传参，无需拼接字符串
const handleOperation = (type: 'update' | 'delete', id: number) => {
  talkForm.value.id = id;
  if (type === "delete") {
    messageConfirm("确认删除该说说？").then(() => {
      deleteTalk(id).then(({ data }) => {
        if (data.flag) {
          notifySuccess(data.msg);
          getList();
        }
      });
    });
  } else {
    editTalk(id).then(({ data }) => {
      if (data.flag) {
        talkForm.value = { ...data.data };
        // 回显上传图片
        if (data.data.imgList) {
          uploadList.value = data.data.imgList.map(url => ({ url }));
        }
        title.value = "修改说说";
        addOrUpdate.value = true;
      }
    });
  }
};

// 切换发布状态
const handleCommand = (command: number) => {
  talkForm.value.status = command;
};

// 添加表情
const addEmoji = (key: string, value: string) => {
  editorRef.value?.addText(`<img src='${value}' width='24' height='24' alt='${key}' style='margin: 0 1px;vertical-align: text-bottom'/>`);
};

// 切换状态筛选
const changeStatus = (value: number | undefined) => {
  queryParams.value.current = 1;
  queryParams.value.status = value;
  getList();
};

// 分页大小改变
const handleSizeChange = (size: number) => {
  queryParams.value.size = size;
  getList();
};

// 页码改变
const handleCurrentChange = (current: number) => {
  queryParams.value.current = current;
  getList();
};

// 提交表单（新增/修改）
const submitForm = () => {
  // 处理图片列表
  if (uploadList.value.length > 0) {
    const imgList = uploadList.value.map(item => item.url);
    talkForm.value.images = JSON.stringify(imgList);
  } else {
    talkForm.value.images = "";
  }

  // 区分新增和修改
  const request = talkForm.value.id ? updateTalk(talkForm.value) : addTalk(talkForm.value);
  request.then(({ data }) => {
    if (data.flag) {
      notifySuccess(data.msg);
      getList();
      addOrUpdate.value = false;
      uploadList.value = [];
    }
  });
};

// 重置表单
const reset = () => {
  talkForm.value = { id: undefined, talkContent: "", images: "", isTop: 0, status: 1 };
  uploadList.value = [];
  editorRef.value?.clear();
};

// 关闭弹窗
const close = () => {
  reset();
  addOrUpdate.value = false;
};

// 打开发布弹窗
const openModel = () => {
  reset();
  title.value = "发布说说";
  addOrUpdate.value = true;
};

// 获取说说列表
const getList = () => {
  getTalkList(queryParams.value).then(({ data }) => {
    talkList.value = data.data.recordList || [];
    count.value = data.data.count || 0;
  });
};

// 初始化加载列表
onMounted(() => {
  getList();
});
</script>

<style lang="scss" scoped>
// 全局容器
.app-container {
  padding: 1rem;
}

// 间距类
.mb15 {
  margin-bottom: 0.9375rem;
}

// 状态筛选
.status-filter {
  color: #999;
  margin-bottom: 0.9375rem;
  .status-label {
    font-weight: 500;
    cursor: default;
  }
}

// 状态项通用
.status {
  cursor: pointer;
  transition: color 0.2s;
  &:hover {
    color: #666;
  }
}

// 激活状态
.active-status {
  cursor: pointer;
  color: #333;
  font-weight: bold;
}

// 说说项
.talk-item {
  display: flex;
  padding: 1rem 1.25rem;
  border-radius: 0.5rem;
  box-shadow: 0 0.625rem 1.875rem -0.9375rem rgba(0, 0, 0, 0.1);
  transition: all 0.2s ease-in-out;
  &:hover {
    box-shadow: 0 0 2rem rgba(0, 0, 0, 0.1);
  }
  &:not(:first-child) {
    margin-top: 1.25rem;
  }
}

// 用户头像
.user-avatar {
  width: 2.25rem;
  height: 2.25rem;
  border-radius: 50%;
  transition: transform 0.5s ease;
  object-fit: cover;
  &:hover {
    transform: rotate(360deg);
  }
}

// 说说内容容器
.talk-content-container {
  flex: 1;
  margin-left: 0.825rem;
}

// 用户信息
.user-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  .user-name {
    font-size: 0.875rem;
    font-weight: 600;
    color: #333;
  }
  .oper-icon {
    color: #333;
    cursor: pointer;
    font-size: 1rem;
  }
}

// 说说信息
.talk-info {
  display: flex;
  align-items: center;
  margin-top: 0.2rem;
  line-height: 1.5rem;
  .talk-time {
    font-size: 0.8125rem;
    margin-right: 1.25rem;
    color: #9499a0;
  }
  .tag {
    font-size: 0.8125rem;
    display: flex;
    align-items: center;
    gap: 0.2rem;
    &.top {
      color: #ff7242;
    }
    &.secret {
      color: #999;
      margin-left: 10px;
    }
  }
}

// 说说内容
.talk-content {
  margin-top: 0.5rem;
  font-size: 0.875rem;
  line-height: 1.5rem;
  word-wrap: break-word;
  word-break: break-all;
  white-space: pre-line;
  color: #333;
}

// 图片容器 - 核心解决一行多列、自动换行
.talk-image-container {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  align-items: flex-start;
  margin-top: 0.5rem;
}

// 说说图片 - 核心解决最大高度200px、自适应、不拉伸
.talk-image {
  max-height: 200px !important;
  width: auto !important;
  height: auto !important;
  border-radius: 0.25rem;
  cursor: pointer;
  flex-shrink: 0;
  // 穿透scoped作用到el-image内部img
  :deep(img) {
    max-height: 200px !important;
    width: auto !important;
    height: auto !important;
    object-fit: contain !important;
    object-position: center !important;
  }
}

// 分页容器
.pagination-container {
  margin-top: 1.5rem;
  text-align: right;
}

// 对话框表单
.talk-form {
  margin-bottom: 1rem;
}

// 对话框操作栏
.dialog-oper-bar {
  margin-top: 0.5rem;
  .oper-icon {
    cursor: pointer;
    display: inline-block;
    padding: 0.2rem;
    border-radius: 0.25rem;
    &:hover {
      background-color: #f5f5f5;
    }
  }
  .el-dropdown-link {
    display: flex;
    align-items: center;
    cursor: pointer;
    margin-top: 0.1rem;
  }
}

// 上传预览
.upload-preview {
  margin-top: 1rem;
}

// 表情项
.emoji-item {
  cursor: pointer;
  display: inline-block;
  padding: 0.25rem;
  border-radius: 0.25rem;
  transition: background 0.2s;
  &:hover {
    background: #dddddd;
  }
  .emoji {
    user-select: none;
    vertical-align: middle;
  }
}
</style>