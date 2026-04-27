<template>
  <div class="book-import-page">
    <el-card class="import-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="header-title">
            <el-icon class="header-icon"><Upload /></el-icon>
            <span>EPUB 书籍导入</span>
          </div>
          <el-tag type="info" size="small">支持 .epub 格式</el-tag>
        </div>
      </template>

      <!-- 步骤条 -->
      <el-steps :active="activeStep" finish-status="success" simple class="import-steps">
        <el-step title="上传文件" />
        <el-step title="解析预览" />
        <el-step title="确认导入" />
      </el-steps>

      <!-- Step 1: 上传 -->
      <div v-if="activeStep === 0" class="step-content">
        <el-upload
          class="epub-uploader"
          drag
          action="#"
          :auto-upload="false"
          :on-change="handleFileChange"
          :show-file-list="false"
          accept=".epub"
        >
          <el-icon class="upload-icon"><UploadFilled /></el-icon>
          <div class="upload-text">
            <div class="upload-main">拖拽 EPUB 文件到此处，或 <em>点击上传</em></div>
            <div class="upload-hint">支持 .epub 格式，建议文件大小不超过 50MB</div>
          </div>
        </el-upload>

        <div v-if="fileName" class="file-selected">
          <el-icon class="file-icon"><Document /></el-icon>
          <span class="file-name">{{ fileName }}</span>
          <el-button type="primary" :loading="parsing" @click="previewEpub">
            <el-icon><View /></el-icon>
            <span>解析预览</span>
          </el-button>
        </div>
      </div>

      <!-- Step 2: 预览与编辑 -->
      <div v-if="activeStep === 1" class="step-content">
        <el-form :model="form" label-width="110px" class="meta-form">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="书名">
                <el-input v-model="form.bookName" placeholder="书名" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="作者名">
                <el-input v-model="form.author" placeholder="作者名（可修改）" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="作品方向">
                <el-radio-group v-model="form.workDirection">
                  <el-radio :label="0">男频</el-radio>
                  <el-radio :label="1">女频</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="分类">
                <el-select v-model="form.categoryId" placeholder="选择分类" style="width: 100%">
                  <el-option
                    v-for="cat in categories"
                    :key="cat.id"
                    :label="cat.name"
                    :value="cat.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="简介">
            <el-input
              v-model="form.bookDesc"
              type="textarea"
              :rows="3"
              placeholder="书籍简介"
            />
          </el-form-item>

          <el-form-item label="绑定平台作者">
            <div class="author-select-row">
              <el-select
                v-model="form.authorId"
                placeholder="选择平台已有作者（必填）"
                style="flex: 1"
                filterable
                clearable
                @change="onAuthorChange"
              >
                <el-option
                  v-for="author in authors"
                  :key="author.id"
                  :label="author.penName"
                  :value="author.id"
                />
              </el-select>
              <el-button type="success" plain @click="showCreateAuthorDialog">
                <el-icon><Plus /></el-icon>
                <span>新建作者</span>
              </el-button>
            </div>
            <div class="author-hint">
              <el-icon><InfoFilled /></el-icon>
              <span>导入的书籍将归属到所选平台作者名下。如作者不存在，请点击「新建作者」快速创建。</span>
            </div>
          </el-form-item>
        </el-form>

        <!-- 统计信息 -->
        <div class="stats-bar">
          <div class="stat-item">
            <div class="stat-value">{{ previewData.totalChapters }}</div>
            <div class="stat-label">总章节数</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ previewData.totalWords }}</div>
            <div class="stat-label">总字数</div>
          </div>
        </div>

        <!-- 章节预览 -->
        <div class="chapter-preview">
          <div class="preview-header">
            <span class="preview-title">章节预览</span>
            <el-tag size="small" type="info">共 {{ previewData.chapters.length }} 章</el-tag>
          </div>
          <el-table
            :data="previewData.chapters"
            height="320"
            size="small"
            border
            stripe
          >
            <el-table-column type="index" width="60" align="center" />
            <el-table-column prop="chapterName" label="章节标题" min-width="200" show-overflow-tooltip />
            <el-table-column prop="wordCount" label="字数" width="100" align="center">
              <template #default="{ row }">
                <el-tag size="small" type="success">{{ row.wordCount }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="内容预览" min-width="300" show-overflow-tooltip>
              <template #default="{ row }">
                <span class="text-preview">{{ row.preview.substring(0, 80) }}...</span>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="action-bar">
          <el-button @click="activeStep = 0">上一步</el-button>
          <el-button type="primary" :loading="importing" @click="confirmImport">
            <el-icon><Check /></el-icon>
            <span>确认导入</span>
          </el-button>
        </div>
      </div>

      <!-- Step 3: 导入成功 -->
      <div v-if="activeStep === 2" class="step-content success-step">
        <el-result icon="success" title="导入成功" sub-title="书籍已成功导入系统">
          <template #extra>
            <el-button type="primary" @click="resetImport">继续导入</el-button>
            <el-button @click="$router.push('/admin/books')">查看书籍列表</el-button>
          </template>
        </el-result>
      </div>
    </el-card>

    <!-- 新建作者弹窗 -->
    <el-dialog
      v-model="createDialogVisible"
      title="快速创建作者"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form :model="createForm" label-width="80px">
        <el-form-item label="笔名">
          <el-input v-model="createForm.penName" placeholder="请输入笔名" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="createForm.telPhone" placeholder="可选，用于联系" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="creatingAuthor" @click="submitCreateAuthor">
          创建
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload, UploadFilled, Document, View, Check, Plus, InfoFilled } from '@element-plus/icons-vue'
import { adminPreviewEpub, adminImportEpub, adminQuickCreateAuthor } from '@/api/admin'
import axios from 'axios'

const activeStep = ref(0)
const fileName = ref('')
const fileRaw = ref(null)
const parsing = ref(false)
const importing = ref(false)
const creatingAuthor = ref(false)
const createDialogVisible = ref(false)

const categories = ref([])
const authors = ref([])

const form = reactive({
  bookName: '',
  author: '',
  bookDesc: '',
  workDirection: 0,
  categoryId: null,
  authorId: null
})

const createForm = reactive({
  penName: '',
  telPhone: ''
})

const previewData = reactive({
  bookName: '',
  author: '',
  description: '',
  totalChapters: 0,
  totalWords: 0,
  chapters: []
})

// 获取分类列表
const fetchCategories = async () => {
  try {
    const res = await axios.get(
      `${process.env.VUE_APP_BASE_API_URL}/front/book/category/list?workDirection=${form.workDirection}`
    )
    if (res.data.code === '00000') {
      categories.value = res.data.data
      if (res.data.data.length > 0 && !form.categoryId) {
        form.categoryId = res.data.data[0].id
      }
    }
  } catch (e) {
    console.error('获取分类失败', e)
  }
}

// 获取作者列表
const fetchAuthors = async () => {
  try {
    const res = await axios.get(
      `${process.env.VUE_APP_BASE_API_URL}/admin/user/author/list?pageNum=1&pageSize=100`,
      { headers: { Authorization: localStorage.getItem('admin_token') } }
    )
    if (res.data.code === '00000') {
      authors.value = res.data.data
    }
  } catch (e) {
    console.error('获取作者失败', e)
  }
}

watch(() => form.workDirection, fetchCategories)

onMounted(() => {
  fetchCategories()
  fetchAuthors()
})

const handleFileChange = (uploadFile) => {
  const file = uploadFile.raw
  if (!file) return
  if (!file.name.toLowerCase().endsWith('.epub')) {
    ElMessage.error('请选择 .epub 格式的文件')
    return
  }
  fileName.value = file.name
  fileRaw.value = file
}

const previewEpub = async () => {
  if (!fileRaw.value) return
  parsing.value = true
  try {
    const fd = new FormData()
    fd.append('file', fileRaw.value)
    const res = await adminPreviewEpub(fd)
    if (res.ok) {
      Object.assign(previewData, res.data)
      form.bookName = res.data.bookName
      form.author = res.data.author
      form.bookDesc = res.data.description
      activeStep.value = 1
    }
  } catch (e) {
    ElMessage.error('解析失败：' + (e.message || '未知错误'))
  } finally {
    parsing.value = false
  }
}

const onAuthorChange = (authorId) => {
  const author = authors.value.find(a => a.id === authorId)
  if (author) {
    form.author = author.penName
  }
}

const showCreateAuthorDialog = () => {
  createForm.penName = form.author || ''
  createForm.telPhone = ''
  createDialogVisible.value = true
}

const submitCreateAuthor = async () => {
  if (!createForm.penName || !createForm.penName.trim()) {
    ElMessage.warning('请输入笔名')
    return
  }
  creatingAuthor.value = true
  try {
    const res = await adminQuickCreateAuthor({
      penName: createForm.penName.trim(),
      telPhone: createForm.telPhone.trim()
    })
    if (res.ok) {
      ElMessage.success('作者创建成功')
      createDialogVisible.value = false
      await fetchAuthors()
      // 自动选中新创建的作者
      const newAuthor = authors.value.find(a => a.id === res.data.id)
      if (newAuthor) {
        form.authorId = newAuthor.id
        form.author = newAuthor.penName
      }
    }
  } catch (e) {
    ElMessage.error('创建失败：' + (e.message || '未知错误'))
  } finally {
    creatingAuthor.value = false
  }
}

const confirmImport = async () => {
  if (!form.categoryId) {
    ElMessage.warning('请选择分类')
    return
  }
  if (!form.authorId) {
    ElMessage.warning('请选择或创建平台作者')
    return
  }
  if (!form.bookName) {
    ElMessage.warning('请输入书名')
    return
  }

  const cat = categories.value.find(c => c.id === form.categoryId)
  const author = authors.value.find(a => a.id === form.authorId)

  importing.value = true
  try {
    const fd = new FormData()
    fd.append('file', fileRaw.value)
    fd.append('categoryId', form.categoryId)
    fd.append('categoryName', cat ? cat.name : '')
    fd.append('workDirection', form.workDirection)
    fd.append('authorId', form.authorId)
    fd.append('authorName', author ? author.penName : form.author)
    fd.append('bookName', form.bookName)
    fd.append('bookDesc', form.bookDesc || '')
    const res = await adminImportEpub(fd)
    if (res.ok) {
      activeStep.value = 2
      ElMessage.success('导入成功')
    }
  } catch (e) {
    ElMessage.error('导入失败：' + (e.message || '未知错误'))
  } finally {
    importing.value = false
  }
}

const resetImport = () => {
  activeStep.value = 0
  fileName.value = ''
  fileRaw.value = null
  form.bookName = ''
  form.author = ''
  form.bookDesc = ''
  form.workDirection = 0
  form.categoryId = categories.value[0]?.id || null
  form.authorId = null
  previewData.chapters = []
  previewData.totalChapters = 0
  previewData.totalWords = 0
}
</script>

<style scoped>
.book-import-page {
  max-width: 960px;
  margin: 0 auto;
}

.import-card {
  border-radius: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
}

.header-icon {
  font-size: 20px;
  color: #667eea;
}

.import-steps {
  margin: 20px 0 30px;
}

.step-content {
  padding: 10px 0;
}

.epub-uploader {
  border: 2px dashed #dcdfe6;
  border-radius: 12px;
  padding: 40px;
  text-align: center;
  transition: all 0.3s;
  cursor: pointer;
}

.epub-uploader:hover {
  border-color: #667eea;
  background: #f8f9ff;
}

.upload-icon {
  font-size: 48px;
  color: #c0c4cc;
  margin-bottom: 16px;
}

.upload-main {
  font-size: 16px;
  color: #606266;
  margin-bottom: 8px;
}

.upload-main em {
  color: #667eea;
  font-style: normal;
  font-weight: 500;
}

.upload-hint {
  font-size: 12px;
  color: #909399;
}

.file-selected {
  margin-top: 24px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: #f5f7fa;
  border-radius: 10px;
}

.file-icon {
  font-size: 24px;
  color: #667eea;
}

.file-name {
  flex: 1;
  font-size: 14px;
  color: #1a1a2e;
  font-weight: 500;
}

.meta-form {
  margin-bottom: 20px;
}

.author-select-row {
  display: flex;
  gap: 10px;
  align-items: center;
}

.author-hint {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}

.author-hint .el-icon {
  font-size: 14px;
  margin-top: 1px;
  color: #e6a23c;
}

.stats-bar {
  display: flex;
  gap: 40px;
  margin-bottom: 20px;
  padding: 16px 20px;
  background: linear-gradient(135deg, #667eea10, #764ba210);
  border-radius: 10px;
}
.stat-item {
  text-align: center;
}
.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #667eea;
  line-height: 1.2;
}
.stat-label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.chapter-preview {
  margin-bottom: 20px;
}

.preview-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.preview-title {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a2e;
}

.text-preview {
  color: #606266;
  font-size: 13px;
}

.action-bar {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.success-step {
  padding: 40px 0;
}
</style>
