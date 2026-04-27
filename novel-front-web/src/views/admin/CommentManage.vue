<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">评论审核</h2>
        <p class="page-subtitle">审核用户发表的评论内容，维护社区健康环境</p>
      </div>
      <div class="page-header-right">
        <div class="filter-tabs">
          <div
            class="filter-tab"
            :class="auditFilter == null ? 'active' : ''"
            @click="auditFilter = null; loadData(1)"
          >全部</div>
          <div
            class="filter-tab"
            :class="auditFilter == 0 ? 'active' : ''"
            @click="auditFilter = 0; loadData(1)"
          >
            <span class="tab-dot dot-pending"></span>待审核
          </div>
          <div
            class="filter-tab"
            :class="auditFilter == 1 ? 'active' : ''"
            @click="auditFilter = 1; loadData(1)"
          >
            <span class="tab-dot dot-pass"></span>已通过
          </div>
          <div
            class="filter-tab"
            :class="auditFilter == 2 ? 'active' : ''"
            @click="auditFilter = 2; loadData(1)"
          >
            <span class="tab-dot dot-reject"></span>已拒绝
          </div>
        </div>
      </div>
    </div>

    <el-card class="data-card" shadow="hover">
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
        <el-table-column prop="userId" label="用户ID" width="90" align="center">
          <template #default="{row}">
            <span class="user-id">UID:{{ row.userId }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="bookId" label="书籍ID" width="90" align="center">
          <template #default="{row}">
            <span class="book-id">BID:{{ row.bookId }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="chapterName" label="章节" width="140" align="center">
          <template #default="{row}">
            <el-tag v-if="row.chapterId && row.chapterId > 0" size="small" type="info" effect="plain">{{ row.chapterName || '第' + row.chapterId + '章' }}</el-tag>
            <span v-else class="no-chapter">整本书</span>
          </template>
        </el-table-column>
        <el-table-column prop="commentContent" label="评论内容" min-width="220">
          <template #default="{row}">
            <div class="comment-content">{{ row.commentContent }}</div>
          </template>
        </el-table-column>
        <el-table-column label="详情" width="80" align="center">
          <template #default="{row}">
            <el-button type="primary" size="small" text @click="showDetail(row)">查看</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="auditStatus" label="审核状态" width="110" align="center">
          <template #default="{row}">
            <div class="audit-badge" :class="`audit-${row.auditStatus}`">
              <span class="audit-dot"></span>
              <span>{{ row.auditStatus == 0 ? '待审核' : (row.auditStatus == 1 ? '已通过' : '已拒绝') }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="评论时间" width="150">
          <template #default="{row}">
            <div class="time-cell">
              <el-icon><Clock /></el-icon>
              <span>{{ row.createTime }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{row}">
            <div class="action-group">
              <el-button
                type="success"
                size="small"
                class="action-btn action-pass"
                @click="audit(row, 1)"
                :disabled="row.auditStatus == 1"
              >
                <el-icon><Check /></el-icon>
              </el-button>
              <el-button
                type="danger"
                size="small"
                class="action-btn action-reject"
                @click="audit(row, 2)"
                :disabled="row.auditStatus == 2"
              >
                <el-icon><Close /></el-icon>
              </el-button>
              <el-button
                type="info"
                size="small"
                class="action-btn action-delete"
                @click="del(row)"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && list.length === 0" description="暂无评论数据" class="empty-state">
        <template #image>
          <div class="empty-icon"><el-icon><ChatDotRound /></el-icon></div>
        </template>
      </el-empty>

      <el-dialog v-model="detailVisible" title="评论详情" width="600px" class="detail-dialog">
        <div class="detail-content">{{ detailContent }}</div>
      </el-dialog>

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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Clock, Check, Close, Delete, ChatDotRound
} from '@element-plus/icons-vue'
import { adminListComments, adminAuditComment, adminDeleteComment } from '@/api/admin'

const list = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const auditFilter = ref(null)
const detailVisible = ref(false)
const detailContent = ref('')

const showDetail = (row) => {
  detailContent.value = row.commentContent || '无内容'
  detailVisible.value = true
}

const loadData = async (page = 1) => {
  pageNum.value = page
  loading.value = true
  try {
    const res = await adminListComments({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      auditStatus: auditFilter.value
    })
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

const audit = async (row, status) => {
  try {
    await adminAuditComment(row.id, status)
    row.auditStatus = status
    ElMessage.success({ message: status == 1 ? '评论已通过' : '评论已拒绝', duration: 1500 })
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

const del = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该评论吗？', '删除确认', { type: 'warning' })
    await adminDeleteComment(row.id)
    ElMessage.success({ message: '评论已删除', duration: 1500 })
    loadData(1)
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

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 16px;
}
.page-title { color: #1a1a2e; font-size: 22px; font-weight: 600; margin: 0 0 6px 0; }
.page-subtitle { color: #909399; font-size: 13px; margin: 0; }

/* 筛选标签 */
.filter-tabs { display: flex; gap: 8px; background: #fff; padding: 4px; border-radius: 10px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.filter-tab {
  display: flex; align-items: center; gap: 6px;
  padding: 8px 16px; border-radius: 8px;
  cursor: pointer; font-size: 13px; color: #606266;
  transition: all 0.3s ease;
}
.filter-tab:hover { background: #f5f7fa; }
.filter-tab.active { background: linear-gradient(135deg, #667eea, #764ba2); color: #fff; box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3); }
.tab-dot { width: 6px; height: 6px; border-radius: 50%; }
.dot-pending { background: #faad14; }
.dot-pass { background: #52c41a; }
.dot-reject { background: #f5222d; }

.data-card { border-radius: 16px; border: none; box-shadow: 0 4px 12px rgba(0,0,0,0.04); }
.data-card :deep(.el-card__body) { padding: 20px; }

.data-table { border-radius: 12px; overflow: hidden; }
.data-table :deep(.el-table__header-wrapper th) { background: #f8f9fb !important; }
.id-tag { display: inline-block; padding: 2px 8px; background: #f0f2f5; border-radius: 6px; color: #909399; font-size: 12px; font-weight: 500; }
.user-id, .book-id { color: #909399; font-size: 12px; }

.comment-content {
  color: #1a1a2e;
  font-size: 14px;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.detail-dialog :deep(.el-dialog__body) { padding: 20px 24px; max-height: 80vh; overflow-y: auto; }
.detail-content { color: #1a1a2e; font-size: 14px; line-height: 1.8; white-space: pre-wrap; word-break: break-all; }

.audit-badge { display: inline-flex; align-items: center; gap: 6px; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 500; }
.audit-0 { background: #fff7e6; color: #fa8c16; }
.audit-1 { background: #f6ffed; color: #52c41a; }
.audit-2 { background: #fff1f0; color: #f5222d; }
.audit-dot { width: 6px; height: 6px; border-radius: 50%; background: currentColor; }

.time-cell { display: flex; align-items: center; gap: 6px; color: #909399; font-size: 13px; }

.action-group { display: flex; justify-content: center; gap: 6px; }
.action-btn { border-radius: 8px; padding: 6px 10px; }
.action-pass { background: linear-gradient(135deg, #67c23a, #85ce61); border: none; }
.action-reject { background: linear-gradient(135deg, #f56c6c, #f78989); border: none; }
.action-delete { border: none; }

.empty-state { padding: 60px 0; }
.empty-icon { width: 80px; height: 80px; border-radius: 50%; background: #f5f7fa; display: flex; align-items: center; justify-content: center; margin: 0 auto 16px; color: #c0c4cc; font-size: 36px; }

.no-chapter { color: #c0c4cc; font-size: 12px; }
.pagination-wrap { display: flex; justify-content: flex-end; padding-top: 16px; border-top: 1px solid #f0f2f5; margin-top: 8px; }
.custom-pagination :deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) { background: linear-gradient(135deg, #667eea, #764ba2); }
</style>
