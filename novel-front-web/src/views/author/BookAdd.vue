<template>
  <AuthorHeader />
  <div class="main box_center cf">
    <div class="userBox cf">
      <AuthorSidebar active="bookList" />
      <div class="my_r">
        <div class="np-fade-in">
          <!-- 页面标题 -->
          <div class="np-page-title">
            <div>
              <h2>发布新作品</h2>
              <p class="page-subtitle">填写作品信息，开启您的创作之旅</p>
            </div>
            <router-link :to="{ name: 'authorBookList' }" class="np-btn np-btn-default">
              <el-icon><ArrowLeft /></el-icon>
              返回列表
            </router-link>
          </div>

          <!-- 表单区域 -->
          <div class="form-container">
            <!-- 左侧：封面预览 -->
            <div class="cover-section">
              <div class="cover-preview-wrapper">
                <img
                  class="cover-preview"
                  :src="previewUrl || picUrl || picUpload"
                  @error="handleImageError"
                />
                <div class="cover-overlay" v-if="picUrl || previewUrl">
                  <el-icon :size="24" color="#fff"><Check /></el-icon>
                  <span>{{ previewUrl ? '预览中' : '封面已上传' }}</span>
                </div>
              </div>
              <input
                type="file"
                ref="fileInputRef"
                accept="image/jpeg,image/jpg,image/png,image/gif,image/webp"
                style="display: none"
                @change="handleFileChange"
              />
              <el-button type="primary" size="large" class="upload-btn" @click="fileInputRef?.click()" :loading="uploading">
                <el-icon><Upload /></el-icon>
                {{ uploading ? '上传中...' : (picUrl ? '更换封面' : '上传封面') }}
              </el-button>
              <p class="cover-tip">建议尺寸 300×400，支持 JPG / PNG / GIF / WEBP 格式，最大 5MB</p>
            </div>

            <!-- 右侧：表单 -->
            <div class="form-section">
              <el-form
                ref="bookFormRef"
                :model="book"
                :rules="rules"
                label-position="top"
                size="large"
              >
                <!-- 基本信息分组 -->
                <div class="form-group">
                  <h4 class="group-title">
                    <el-icon><Document /></el-icon>
                    基本信息
                  </h4>

                  <el-form-item label="作品名称" prop="bookName">
                    <el-input
                      v-model="book.bookName"
                      placeholder="请输入作品名称，建议简洁有吸引力"
                      maxlength="30"
                      show-word-limit
                      clearable
                    />
                  </el-form-item>

                  <div class="form-row">
                    <el-form-item label="作品方向" prop="workDirection" class="form-col">
                      <el-select
                        v-model="book.workDirection"
                        placeholder="请选择作品方向"
                        @change="loadCategoryList"
                        style="width: 100%"
                      >
                        <el-option label="男频" :value="0">
                          <span style="display: flex; align-items: center; gap: 8px;">
                            <el-icon><Male /></el-icon> 男频
                          </span>
                        </el-option>
                        <el-option label="女频" :value="1">
                          <span style="display: flex; align-items: center; gap: 8px;">
                            <el-icon><Female /></el-icon> 女频
                          </span>
                        </el-option>
                      </el-select>
                    </el-form-item>

                    <el-form-item label="作品分类" prop="categoryId" class="form-col">
                      <el-select
                        v-model="book.categoryId"
                        placeholder="请选择分类"
                        @change="categoryChange"
                        style="width: 100%"
                      >
                        <el-option
                          v-for="item in bookCategorys"
                          :key="item.id"
                          :label="item.name"
                          :value="item.id"
                        />
                      </el-select>
                    </el-form-item>
                  </div>
                </div>

                <!-- 作品介绍分组 -->
                <div class="form-group">
                  <h4 class="group-title">
                    <el-icon><EditPen /></el-icon>
                    作品介绍
                  </h4>

                  <el-form-item label="作品简介" prop="bookDesc">
                    <el-input
                      v-model="book.bookDesc"
                      type="textarea"
                      :rows="6"
                      placeholder="请填写作品简介，介绍故事背景、主要情节等，让读者快速了解您的作品..."
                      maxlength="500"
                      show-word-limit
                      resize="none"
                    />
                  </el-form-item>
                </div>

                <!-- 提交按钮 -->
                <div class="form-actions">
                  <el-button
                    type="primary"
                    size="large"
                    @click="saveBook"
                    :loading="submitting"
                    class="submit-btn"
                  >
                    <el-icon><Upload /></el-icon>
                    {{ submitting ? '发布中...' : '立即发布' }}
                  </el-button>
                  <el-button
                    size="large"
                    @click="resetForm"
                    class="reset-btn"
                  >
                    重置
                  </el-button>
                </div>
              </el-form>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {
  ArrowLeft,
  Upload,
  Check,
  Document,
  EditPen,
  Male,
  Female,
} from "@element-plus/icons-vue";
import { reactive, toRefs, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { publishBook } from "@/api/author";
import { listCategorys } from "@/api/book";
import axios from "@/utils/request";
import AuthorHeader from "@/components/author/Header.vue";
import AuthorSidebar from "@/components/author/Sidebar.vue";
import picUpload from "@/assets/images/pic_upload.png";

export default {
  name: "authorBookAdd",
  components: {
    AuthorHeader,
    AuthorSidebar,
    ArrowLeft,
    Upload,
    Check,
    Document,
    EditPen,
    Male,
    Female,
  },
  setup() {
    const router = useRouter();
    const bookFormRef = ref(null);

    const state = reactive({
      book: { workDirection: 0, isVip: 0 },
      bookCategorys: [],
      baseUrl: process.env.VUE_APP_BASE_API_URL,
      imgBaseUrl: process.env.VUE_APP_BASE_IMG_URL,
      submitting: false,
      uploading: false,
      previewUrl: '',
      picUrl: '',
    });

    // 表单验证规则
    const rules = {
      bookName: [
        { required: true, message: "请输入作品名称", trigger: "blur" },
        { min: 2, max: 30, message: "作品名称长度在 2 到 30 个字符", trigger: "blur" },
      ],
      workDirection: [
        { required: true, message: "请选择作品方向", trigger: "change" },
      ],
      categoryId: [
        { required: true, message: "请选择作品分类", trigger: "change" },
      ],
    };

    onMounted(() => {
      loadCategoryList();
    });

    const ALLOWED_TYPES = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
    const ALLOWED_EXTS = ['.jpg', '.jpeg', '.png', '.gif', '.webp'];

    // 拼接图片完整 URL
    const buildImgUrl = (path) => {
      if (!path) return '';
      if (path.startsWith('http')) return path;
      const base = state.imgBaseUrl;
      if (base.endsWith('/') && path.startsWith('/')) {
        return base + path.slice(1);
      }
      if (!base.endsWith('/') && !path.startsWith('/')) {
        return base + '/' + path;
      }
      return base + path;
    };

    // 本地预览
    const createPreview = (file) => {
      const reader = new FileReader();
      reader.onload = (e) => {
        state.previewUrl = e.target.result;
      };
      reader.readAsDataURL(file);
    };

    const fileInputRef = ref(null);

    const handleFileChange = async (e) => {
      const file = e.target.files?.[0];
      if (!file) return;

      // 校验（兼容部分系统无法识别 MIME type 的情况，同时检查扩展名）
      const fileName = file.name || '';
      const fileExt = fileName.substring(fileName.lastIndexOf('.')).toLowerCase();
      const typeOk = ALLOWED_TYPES.includes(file.type);
      const extOk = ALLOWED_EXTS.includes(fileExt);
      if (!typeOk && !extOk) {
        ElMessage.error("请上传 JPG、PNG、GIF 或 WEBP 格式的图片!");
        e.target.value = '';
        return;
      }
      if (file.size / 1024 / 1024 > 5) {
        ElMessage.error("图片大小最多 5MB!");
        e.target.value = '';
        return;
      }

      // 本地预览
      createPreview(file);

      // 手动上传（axios，带 token）
      state.uploading = true;
      try {
        const formData = new FormData();
        formData.append('file', file);
        // 不手动设置 Content-Type，让浏览器自动添加 multipart boundary
        const res = await axios.post('/front/resource/image', formData);
        // request.js 拦截器已返回 res.data，所以 res 就是 { code, data, message }
        if (res.code === '00000') {
          const rawPath = res.data || '';
          const normalized = rawPath.replace(/\\/g, '/');
          state.picUrl = buildImgUrl(normalized);
          state.previewUrl = '';
          ElMessage.success("封面上传成功");
        } else {
          ElMessage.error(res.message || "上传失败");
        }
      } catch (err) {
        const msg = (err && (err.message || err.msg || JSON.stringify(err))) || "未知错误";
        ElMessage.error("上传失败：" + msg);
      } finally {
        state.uploading = false;
        e.target.value = '';
      }
    };

    const loadCategoryList = async () => {
      const { data } = await listCategorys({
        workDirection: state.book.workDirection,
      });
      if (data && data.length > 0) {
        state.book.categoryId = data[0].id;
        state.book.categoryName = data[0].name;
        state.bookCategorys = data;
      }
    };

    const categoryChange = (val) => {
      const category = state.bookCategorys.find((c) => c.id == val);
      if (category) {
        state.book.categoryName = category.name;
      }
    };

    const saveBook = async () => {
      if (!bookFormRef.value) return;

      await bookFormRef.value.validate(async (valid) => {
        if (!valid) return;

        if (!state.picUrl) {
          ElMessage.error("请上传作品封面！");
          return;
        }
        // 存入数据库时只保存相对路径，前端展示层再拼接 baseUrl
        let dbPicUrl = state.picUrl;
        if (dbPicUrl.startsWith(state.imgBaseUrl)) {
          dbPicUrl = dbPicUrl.substring(state.imgBaseUrl.length);
          if (dbPicUrl.startsWith('/')) dbPicUrl = dbPicUrl.substring(1);
        }
        state.book.picUrl = dbPicUrl;
        if (!state.book.bookDesc) {
          ElMessage.error("请填写作品简介！");
          return;
        }
        if (state.book.bookDesc.length < 10) {
          ElMessage.error("作品简介至少需要 10 个字符！");
          return;
        }

        state.submitting = true;
        try {
          await publishBook(state.book);
          ElMessage.success("作品发布成功！");
          router.push({ name: "authorBookList" });
        } catch (err) {
          ElMessage.error("发布失败，请稍后重试");
        } finally {
          state.submitting = false;
        }
      });
    };

    const resetForm = () => {
      if (bookFormRef.value) {
        bookFormRef.value.resetFields();
      }
      state.book = { workDirection: 0, isVip: 0 };
      state.picUrl = '';
      loadCategoryList();
    };

    const handleImageError = (e) => {
      if (e.target.src !== picUpload) {
        e.target.src = picUpload;
      }
    };

    return {
      ...toRefs(state),
      rules,
      bookFormRef,
      fileInputRef,
      picUpload,
      buildImgUrl,
      handleFileChange,
      loadCategoryList,
      categoryChange,
      saveBook,
      resetForm,
      handleImageError,
    };
  },
};
</script>

<style scoped>
.page-subtitle {
  font-size: 13px;
  color: var(--text-muted);
  margin-top: 4px;
  font-weight: normal;
}

/* 表单容器 */
.form-container {
  display: flex;
  gap: 32px;
}

/* 左侧封面区域 */
.cover-section {
  width: 280px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.cover-preview-wrapper {
  width: 100%;
  aspect-ratio: 3/4;
  border-radius: var(--radius-lg);
  overflow: hidden;
  background: linear-gradient(135deg, #f5f7fa, #e4e7ed);
  box-shadow: var(--shadow-md);
  position: relative;
  margin-bottom: 16px;
}
.cover-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.cover-overlay {
  position: absolute;
  inset: 0;
  background: rgba(102, 126, 234, 0.85);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #fff;
  font-size: 14px;
  animation: fadeIn 0.3s ease;
}
.cover-uploader {
  width: 100%;
}
.upload-btn {
  width: 100%;
}
.cover-tip {
  font-size: 12px;
  color: var(--text-muted);
  text-align: center;
  margin-top: 8px;
  line-height: 1.5;
}

/* 右侧表单区域 */
.form-section {
  flex: 1;
  min-width: 0;
}

.form-group {
  background: #fff;
  border-radius: var(--radius-lg);
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: var(--shadow-card);
}
.group-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.group-title .el-icon {
  color: var(--primary);
  font-size: 18px;
}

.form-row {
  display: flex;
  gap: 20px;
}
.form-col {
  flex: 1;
}

/* 表单元素美化 */
:deep(.el-form-item__label) {
  font-weight: 500;
  color: var(--text-primary);
  padding-bottom: 6px;
}
:deep(.el-input__wrapper) {
  border-radius: var(--radius-sm);
  box-shadow: 0 0 0 1px var(--border-color) inset;
  padding: 4px 12px;
}
:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--primary) inset, 0 0 0 3px var(--primary-light);
}
:deep(.el-textarea__inner) {
  border-radius: var(--radius-sm);
  border-color: var(--border-color);
  padding: 12px;
  font-size: 14px;
  line-height: 1.6;
}
:deep(.el-textarea__inner:focus) {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px var(--primary-light);
}
:deep(.el-select .el-input__wrapper) {
  border-radius: var(--radius-sm);
}

/* 提交按钮 */
.form-actions {
  display: flex;
  gap: 16px;
  padding: 8px 0 16px;
}
.submit-btn {
  padding: 12px 40px;
  font-size: 15px;
  font-weight: 600;
}
.reset-btn {
  padding: 12px 32px;
}

/* 动画 */
@keyframes fadeIn {
  from { opacity: 0; transform: scale(0.95); }
  to { opacity: 1; transform: scale(1); }
}

/* 响应式 */
@media (max-width: 900px) {
  .form-container {
    flex-direction: column;
  }
  .cover-section {
    width: 100%;
    max-width: 300px;
    margin: 0 auto;
  }
  .form-row {
    flex-direction: column;
    gap: 0;
  }
}
</style>
