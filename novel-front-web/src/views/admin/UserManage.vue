<template>
  <div class="page-container">
    <!-- 页面标题栏 -->
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">用户管理</h2>
        <p class="page-subtitle">管理平台所有注册用户，可启用或禁用账号</p>
      </div>
    </div>

    <el-card class="data-card" shadow="hover">
      <!-- 搜索栏 -->
      <div class="search-bar">
        <div class="search-input-wrap">
          <el-icon class="search-icon"><Search /></el-icon>
          <el-input
            v-model="keyword"
            placeholder="搜索用户名或昵称..."
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

      <!-- 数据表格 -->
      <el-table
        :data="list"
        v-loading="loading"
        stripe
        class="data-table"
        :header-cell-style="{ background: '#f8f9fb', color: '#1a1a2e', fontWeight: 600 }"
      >
        <el-table-column prop="id" label="ID" width="80" align="center">
          <template #default="{row}">
            <span class="id-tag">#{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column label="用户信息" min-width="180">
          <template #default="{row}">
            <div class="user-cell">
              <div class="user-avatar-sm">
                <el-icon><User /></el-icon>
              </div>
              <div class="user-detail">
                <div class="user-name">{{ row.username }}</div>
                <div class="user-nick">{{ row.nickName || '-' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="160">
          <template #default="{row}">
            <div class="time-cell">
              <el-icon><Clock /></el-icon>
              <span>{{ row.createTime }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110" align="center">
          <template #default="{row}">
            <div class="status-tag" :class="row.status == 1 ? 'status-active' : 'status-inactive'">
              <div class="status-dot"></div>
              <span>{{ row.status == 1 ? '正常' : '禁用' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{row}">
            <el-button
              :type="row.status == 1 ? 'danger' : 'success'"
              size="small"
              class="action-btn"
              @click="toggleStatus(row)"
            >
              <el-icon><SwitchButton v-if="row.status == 1" /><CircleCheck v-else /></el-icon>
              <span>{{ row.status == 1 ? '禁用' : '启用' }}</span>
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <el-empty v-if="!loading && list.length === 0" description="暂无用户数据" class="empty-state">
        <template #image>
          <div class="empty-icon"><el-icon><User /></el-icon></div>
        </template>
      </el-empty>

      <!-- 分页 -->
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
import { ElMessage } from 'element-plus'
import {
  Search, Refresh, User, Clock, SwitchButton, CircleCheck
} from '@element-plus/icons-vue'
import { adminListUsers, adminUpdateUserStatus } from '@/api/admin'

const list = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const keyword = ref('')

const loadData = async (page = 1) => {
  pageNum.value = page
  loading.value = true
  try {
    const res = await adminListUsers({ pageNum: pageNum.value, pageSize: pageSize.value, keyword: keyword.value })
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

const toggleStatus = async (row) => {
  const newStatus = row.status == 1 ? 0 : 1
  try {
    await adminUpdateUserStatus(row.id, newStatus)
    row.status = newStatus
    ElMessage.success({ message: newStatus == 1 ? '账号已启用' : '账号已禁用', duration: 1500 })
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

onMounted(() => loadData(1))
</script>

<style scoped>
.page-container {
  animation: fadeIn 0.4s ease;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.page-header {
  margin-bottom: 20px;
}
.page-title {
  color: #1a1a2e;
  font-size: 22px;
  font-weight: 600;
  margin: 0 0 6px 0;
}
.page-subtitle {
  color: #909399;
  font-size: 13px;
  margin: 0;
}

.data-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
}
.data-card :deep(.el-card__body) {
  padding: 20px;
}

/* 搜索栏 */
.search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f2f5;
}
.search-input-wrap {
  flex: 1;
  max-width: 320px;
  position: relative;
}
.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #c0c4cc;
  z-index: 1;
}
.search-input :deep(.el-input__wrapper) {
  padding-left: 36px;
  border-radius: 10px;
}
.search-btn {
  border-radius: 10px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
}
.refresh-btn {
  border-radius: 10px;
}

/* 表格 */
.data-table {
  border-radius: 12px;
  overflow: hidden;
}
.data-table :deep(.el-table__header-wrapper th) {
  background: #f8f9fb !important;
}
.id-tag {
  display: inline-block;
  padding: 2px 8px;
  background: #f0f2f5;
  border-radius: 6px;
  color: #909399;
  font-size: 12px;
  font-weight: 500;
}

/* 用户单元格 */
.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}
.user-avatar-sm {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
}
.user-name {
  color: #1a1a2e;
  font-size: 14px;
  font-weight: 500;
}
.user-nick {
  color: #c0c4cc;
  font-size: 12px;
}

/* 时间 */
.time-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #909399;
  font-size: 13px;
}

/* 状态标签 */
.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}
.status-active {
  background: #f0f9eb;
  color: #67c23a;
}
.status-inactive {
  background: #fef0f0;
  color: #f56c6c;
}
.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

/* 操作按钮 */
.action-btn {
  border-radius: 8px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

/* 空状态 */
.empty-state {
  padding: 60px 0;
}
.empty-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  color: #c0c4cc;
  font-size: 36px;
}

/* 分页 */
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid #f0f2f5;
  margin-top: 8px;
}
.custom-pagination :deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background: linear-gradient(135deg, #667eea, #764ba2);
}
</style>
