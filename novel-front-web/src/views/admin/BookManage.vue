<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">小说管理</h2>
        <p class="page-subtitle">管理平台所有小说作品，可查看章节和删除内容</p>
      </div>
    </div>

    <el-card class="data-card" shadow="hover">
      <div class="search-bar">
        <div class="search-input-wrap">
          <el-icon class="search-icon"><Search /></el-icon>
          <el-input
            v-model="keyword"
            placeholder="搜索小说名..."
            clearable
            @keyup.enter="loadData(1)"
            class="search-input"
          />
        </div>
        <el-button type="primary" class="search-btn" @click="loadData(1)">
          <el-icon><Search /></el-icon>
          <span>搜索</span>
        </el-button>
        <el-button class="refresh-btn" @click="loadData(pageNum)">
          <el-icon><Refresh /></el-icon>
        </el-button>
      </div>

      <el-table
        :data="list"
        v-loading="loading"
        stripe
        class="data-table"
        :header-cell-style="{ background: '#f8f9fb', color: '#1a1a2e', fontWeight: 600 }"
      >
        <el-table-column prop="id" label="ID" width="70" align="center">
          <template #default="{row}">
            <span class="id-tag">#{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column label="小说信息" min-width="200">
          <template #default="{row}">
            <div class="book-cell">
              <img class="book-cover-img"
                :src="resolveImageUrl(row.picUrl)"
                :alt="row.bookName"
                @error="handleImageError" />
              <div class="book-detail">
                <div class="book-name">{{ row.bookName }}</div>
                <div class="book-meta">
                  <span class="meta-tag meta-category">{{ row.categoryName || '未分类' }}</span>
                  <span class="meta-tag meta-author">{{ row.authorName }}</span>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="数据" width="180">
          <template #default="{row}">
            <div class="book-stats">
              <div class="book-stat">
                <el-icon><Document /></el-icon>
                <span>{{ row.wordCount || 0 }} 字</span>
              </div>
              <div class="book-stat">
                <el-icon><View /></el-icon>
                <span>{{ formatNumber(row.visitCount) }} 阅读</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="评分" width="90" align="center">
          <template #default="{row}">
            <div class="score-wrap">
              <el-icon class="score-icon"><StarFilled /></el-icon>
              <span>{{ row.score || '0.0' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="bookStatus" label="状态" width="90" align="center">
          <template #default="{row}">
            <div class="book-status" :class="row.bookStatus == 0 ? 'status-serial' : 'status-finished'">
              {{ row.bookStatus == 0 ? '连载中' : '已完结' }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="150">
          <template #default="{row}">
            <div class="time-cell">
              <el-icon><Clock /></el-icon>
              <span>{{ row.createTime }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" align="center" fixed="right">
          <template #default="{row}">
            <div class="action-col">
              <el-button type="primary" size="small" class="action-btn" @click="viewChapters(row)">
                <el-icon><List /></el-icon>
                <span>章节</span>
              </el-button>
              <el-button
                :type="row.bookStatus == 0 ? 'success' : 'warning'"
                size="small"
                class="action-btn"
                @click="toggleBookStatus(row)"
              >
                <el-icon><SwitchButton /></el-icon>
                <span>{{ row.bookStatus == 0 ? '完结' : '连载' }}</span>
              </el-button>
              <el-button type="danger" size="small" class="action-btn" @click="deleteBook(row)">
                <el-icon><Delete /></el-icon>
                <span>删除</span>
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && list.length === 0" description="暂无小说数据" class="empty-state">
        <template #image>
          <div class="empty-icon"><el-icon><Reading /></el-icon></div>
        </template>
      </el-empty>

      <div class="pagination-wrap" v-if="list.length > 0">
        <el-pagination
          background
          layout="total, prev, pager, next, jumper"
          :total="total"
          :page-size="pageSize"
          :current-page="pageNum"
          @current-change="loadData"
          class="custom-pagination"
        />
      </div>
    </el-card>

    <!-- 章节弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="📖 章节列表"
      width="750px"
      class="chapter-dialog"
      destroy-on-close
    >
      <div class="chapter-header">
        <span class="chapter-book">《{{ currentBookName }}》</span>
        <span class="chapter-count">共 {{ chapters.length }} 章</span>
      </div>
      <el-table :data="chapters" stripe size="small" class="chapter-table">
        <el-table-column prop="chapterNum" label="序号" width="60" align="center">
          <template #default="{row}">
            <span class="chapter-num">{{ row.chapterNum }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="chapterName" label="章节名" min-width="200" />
        <el-table-column prop="wordCount" label="字数" width="80" align="center">
          <template #default="{row}">
            <span class="chapter-words">{{ row.wordCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="VIP" width="70" align="center">
          <template #default="{row}">
            <el-tag size="small" :type="row.isVip == 1 ? 'warning' : 'info'" effect="plain">
              {{ row.isVip === 1 ? 'VIP' : '免费' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center">
          <template #default="{row}">
            <el-button type="danger" size="small" text @click="deleteChapter(row)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="chapters.length === 0" description="暂无章节" />
      <div class="dialog-footer">
        <el-pagination
          background
          small
          layout="prev, pager, next"
          :total="chapterTotal"
          :page-size="chapterPageSize"
          @current-change="loadChapters"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, Refresh, Reading, Clock, List, Delete,
  Document, View, StarFilled, SwitchButton
} from '@element-plus/icons-vue'
import { adminListBooks, adminListChapters, adminDeleteChapter, adminUpdateBookStatus, adminDeleteBook } from '@/api/admin'
import picUpload from '@/assets/images/pic_upload.png'

const list = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const keyword = ref('')

const dialogVisible = ref(false)
const currentBookId = ref(null)
const currentBookName = ref('')
const chapters = ref([])
const chapterPageSize = ref(10)
const chapterTotal = ref(0)

const resolveImageUrl = (url) => {
  if (!url) return picUpload
  if (url.startsWith('http')) return url
  return process.env.VUE_APP_BASE_IMG_URL + url
}
const handleImageError = (e) => {
  e.target.src = picUpload
}

const formatNumber = (num) => {
  if (!num) return '0'
  if (num >= 10000) return (num / 10000).toFixed(1) + 'w'
  return num.toString()
}

const loadData = async (page = 1) => {
  pageNum.value = page
  loading.value = true
  try {
    const res = await adminListBooks({ pageNum: pageNum.value, pageSize: pageSize.value, keyword: keyword.value })
    list.value = res.data || []
    total.value = list.value.length < pageSize.value
      ? (pageNum.value - 1) * pageSize.value + list.value.length
      : pageNum.value * pageSize.value + 1
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const viewChapters = (row) => {
  currentBookId.value = row.id
  currentBookName.value = row.bookName
  dialogVisible.value = true
  loadChapters(1)
}

const loadChapters = async (page = 1) => {
  try {
    const res = await adminListChapters(currentBookId.value, { pageNum: page, pageSize: chapterPageSize.value })
    chapters.value = res.data || []
    chapterTotal.value = chapters.value.length < chapterPageSize.value
      ? (page - 1) * chapterPageSize.value + chapters.value.length
      : page * chapterPageSize.value + 1
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  }
}

const deleteBook = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除小说《${row.bookName}》吗？\n删除后将同时移除该小说的所有章节、内容和评论，此操作不可恢复！`,
      '删除确认',
      { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'danger' }
    )
    await adminDeleteBook(row.id)
    ElMessage.success({ message: '小说已删除', duration: 1500 })
    loadData(pageNum.value)
  } catch (e) {}
}

const toggleBookStatus = async (row) => {
  const newStatus = row.bookStatus == 0 ? 1 : 0
  const actionText = newStatus === 1 ? '完结' : '连载中'
  try {
    await ElMessageBox.confirm(`确定将《${row.bookName}》设置为${actionText}吗？`, '状态变更', { type: 'warning' })
    await adminUpdateBookStatus(row.id, newStatus)
    row.bookStatus = newStatus
    ElMessage.success({ message: `已设置为${actionText}`, duration: 1500 })
  } catch (e) {}
}

const deleteChapter = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除章节「${row.chapterName}」吗？`, '删除确认', { type: 'warning' })
    await adminDeleteChapter(row.id)
    ElMessage.success({ message: '章节已删除', duration: 1500 })
    loadChapters(1)
  } catch (e) {}
}

onMounted(() => loadData(1))
</script>

<style scoped>
.page-container { animation: fadeIn 0.4s ease; }
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
.page-header { margin-bottom: 20px; }
.page-title { color: #1a1a2e; font-size: 22px; font-weight: 600; margin: 0 0 6px 0; }
.page-subtitle { color: #909399; font-size: 13px; margin: 0; }

.data-card { border-radius: 16px; border: none; box-shadow: 0 4px 12px rgba(0,0,0,0.04); }
.data-card :deep(.el-card__body) { padding: 20px; }

.search-bar {
  display: flex; align-items: center; gap: 12px;
  margin-bottom: 20px; padding-bottom: 16px;
  border-bottom: 1px solid #f0f2f5;
}
.search-input-wrap { flex: 1; max-width: 320px; position: relative; }
.search-icon { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: #c0c4cc; z-index: 1; }
.search-input :deep(.el-input__wrapper) { padding-left: 36px; border-radius: 10px; }
.search-btn { border-radius: 10px; background: linear-gradient(135deg, #667eea, #764ba2); border: none; }
.refresh-btn { border-radius: 10px; }

.data-table { border-radius: 12px; overflow: hidden; }
.data-table :deep(.el-table__header-wrapper th) { background: #f8f9fb !important; }
.id-tag { display: inline-block; padding: 2px 8px; background: #f0f2f5; border-radius: 6px; color: #909399; font-size: 12px; font-weight: 500; }

.book-cell { display: flex; align-items: center; gap: 12px; }
.book-cover-img { width: 44px; height: 56px; border-radius: 6px; object-fit: cover; box-shadow: 0 2px 8px rgba(0,0,0,0.1); background: #f5f7fa; }
.book-name { color: #1a1a2e; font-size: 14px; font-weight: 500; margin-bottom: 6px; }
.book-meta { display: flex; gap: 6px; flex-wrap: wrap; }
.meta-tag { font-size: 11px; padding: 2px 8px; border-radius: 4px; }
.meta-category { background: #fff0e6; color: #fa8c16; }
.meta-author { background: #f0f5ff; color: #2f54eb; }

.book-stats { display: flex; flex-direction: column; gap: 4px; }
.book-stat { display: flex; align-items: center; gap: 6px; color: #909399; font-size: 13px; }

.score-wrap { display: inline-flex; align-items: center; gap: 4px; color: #faad14; font-weight: 600; }
.score-icon { font-size: 16px; }

.book-status { display: inline-block; padding: 3px 10px; border-radius: 20px; font-size: 12px; font-weight: 500; }
.status-serial { background: #e6f7ff; color: #1890ff; }
.status-finished { background: #f6ffed; color: #52c41a; }

.time-cell { display: flex; align-items: center; gap: 6px; color: #909399; font-size: 13px; }
.action-col { display: flex; flex-direction: column; gap: 6px; align-items: center; }
.action-col :deep(.el-button) { margin: 0; padding: 5px 0; width: 72px; }
.action-col :deep(.el-button > span) { display: flex; align-items: center; justify-content: center; gap: 4px; }

.empty-state { padding: 60px 0; }
.empty-icon { width: 80px; height: 80px; border-radius: 50%; background: #f5f7fa; display: flex; align-items: center; justify-content: center; margin: 0 auto 16px; color: #c0c4cc; font-size: 36px; }

.pagination-wrap { display: flex; justify-content: flex-end; padding-top: 16px; border-top: 1px solid #f0f2f5; margin-top: 8px; }
.custom-pagination :deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) { background: linear-gradient(135deg, #667eea, #764ba2); }

/* 章节弹窗 */
.chapter-dialog :deep(.el-dialog__header) { padding: 20px 24px; border-bottom: 1px solid #f0f2f5; }
.chapter-dialog :deep(.el-dialog__title) { font-weight: 600; color: #1a1a2e; }
.chapter-dialog :deep(.el-dialog__body) { padding: 20px 24px; }
.chapter-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.chapter-book { color: #1a1a2e; font-size: 15px; font-weight: 600; }
.chapter-count { color: #909399; font-size: 13px; }
.chapter-table { border-radius: 10px; overflow: hidden; }
.chapter-num { display: inline-block; width: 24px; height: 24px; line-height: 24px; text-align: center; background: #f0f2f5; border-radius: 6px; color: #606266; font-size: 12px; font-weight: 600; }
.chapter-words { color: #909399; font-size: 13px; }
.dialog-footer { display: flex; justify-content: flex-end; padding-top: 16px; }
</style>
