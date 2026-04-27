<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">审核日志</h2>
        <p class="page-subtitle">查看系统自动审核的内容记录与命中结果</p>
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
        <el-table-column prop="bizType" label="业务类型" width="110" align="center">
          <template #default="{row}">
            <el-tag size="small" effect="plain" class="biz-tag">
              {{ row.bizType || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="bizId" label="业务ID" width="90" align="center">
          <template #default="{row}">
            <span class="biz-id">{{ row.bizId }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容摘要" min-width="200">
          <template #default="{row}">
            <div class="content-text">{{ row.content || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="详情" width="80" align="center">
          <template #default="{row}">
            <el-button type="primary" size="small" text @click="showDetail(row)">查看</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="localHits" label="命中词" width="160">
          <template #default="{row}">
            <div class="hits-wrap">
              <template v-if="row.localHits">
                <el-tag
                  v-for="(word, idx) in parseHits(row.localHits)"
                  :key="idx"
                  size="small"
                  type="danger"
                  effect="plain"
                  class="hit-tag"
                >{{ word }}</el-tag>
              </template>
              <span v-else class="no-hits">无</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="localResult" label="本地检测" width="100" align="center">
          <template #default="{row}">
            <el-tag size="small" :type="row.localResult === 'PASS' ? 'success' : 'danger'" effect="plain">
              {{ row.localResult === 'PASS' ? '正常' : '命中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="finalResult" label="系统判定" width="100" align="center">
          <template #default="{row}">
            <div class="result-badge" :class="resultClass(row.finalResult)">
              <el-icon>
                <CircleCheck v-if="row.finalResult === 'PASS'" />
                <CircleClose v-else />
              </el-icon>
              <span>{{ resultText(row.finalResult) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="cloudResult" label="云审核" width="100" align="center">
          <template #default="{row}">
            <el-tag size="small" :type="row.cloudResult === 'PASS' ? 'success' : row.cloudResult === 'BLOCK' ? 'danger' : 'warning'" effect="plain">
              {{ row.cloudResult === 'PASS' ? '通过' : row.cloudResult === 'BLOCK' ? '阻断' : '复审' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{row}">
            <div class="action-group" v-if="row.status == null || row.status == 0">
              <el-button type="success" size="small" class="action-btn" @click="audit(row, 1)">
                <el-icon><Check /></el-icon>
              </el-button>
              <el-button type="danger" size="small" class="action-btn" @click="audit(row, 2)">
                <el-icon><Close /></el-icon>
              </el-button>
            </div>
            <el-tag v-else size="small" :type="row.status == 1 ? 'success' : 'danger'" effect="plain">
              {{ row.status == 1 ? '已通过' : '已拒绝' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="审核时间" width="150">
          <template #default="{row}">
            <div class="time-cell">
              <el-icon><Clock /></el-icon>
              <span>{{ row.createTime }}</span>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && list.length === 0" description="暂无审核日志" class="empty-state">
        <template #image>
          <div class="empty-icon"><el-icon><Document /></el-icon></div>
        </template>
      </el-empty>

      <el-dialog v-model="detailVisible" title="审核内容详情" width="700px" class="detail-dialog">
        <div class="detail-section">
          <div class="detail-label">审核内容</div>
          <div class="detail-content">{{ detailContent }}</div>
        </div>
        <div v-if="detailCloud" class="detail-section cloud-section">
          <div class="detail-label">🤖 LLM 云审核</div>
          <div class="cloud-result">
            <el-tag :type="detailCloud.result === 'PASS' ? 'success' : detailCloud.result === 'BLOCK' ? 'danger' : 'warning'" size="small" effect="dark">
              {{ detailCloud.result === 'PASS' ? '通过' : detailCloud.result === 'BLOCK' ? '阻断' : '需复审' }}
            </el-tag>
            <span class="cloud-reason">{{ detailCloud.reason }}</span>
          </div>
          <div v-if="detailCloud.categories?.length" class="cloud-categories">
            <el-tag v-for="(cat, idx) in detailCloud.categories" :key="idx" size="small" type="danger" effect="plain" class="cat-tag">{{ cat }}</el-tag>
          </div>
        </div>
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
import { ElMessage } from 'element-plus'
import { Clock, Document, CircleCheck, CircleClose, Warning, Check, Close } from '@element-plus/icons-vue'
import { adminListAuditLogs, adminGetAuditLogDetail, adminUpdateAuditLogStatus } from '@/api/admin'

const list = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const detailVisible = ref(false)
const detailContent = ref('')
const detailCloud = ref(null)

const showDetail = async (row) => {
  detailVisible.value = true
  detailContent.value = '加载中...'
  detailCloud.value = parseCloudDetail(row.cloudDetail)
  try {
    const res = await adminGetAuditLogDetail(row.id)
    detailContent.value = res.data?.content || row.content || '无内容'
    detailCloud.value = parseCloudDetail(res.data?.cloudDetail) || detailCloud.value
  } catch (e) {
    detailContent.value = row.content || '无内容'
  }
}

const parseCloudDetail = (cloudDetail) => {
  if (!cloudDetail) return null
  try {
    return JSON.parse(cloudDetail)
  } catch (e) {
    return { reason: cloudDetail, categories: [] }
  }
}

const parseHits = (localHits) => {
  if (!localHits) return []
  return localHits.split(', ').map(w => w.trim()).filter(w => w)
}

const audit = async (row, status) => {
  try {
    await adminUpdateAuditLogStatus(row.id, status)
    row.status = status
    ElMessage.success({ message: status == 1 ? '已标记通过' : '已标记拒绝', duration: 1500 })
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

const resultClass = (finalResult) => {
  if (finalResult === 'PASS') return 'result-pass'
  return 'result-reject'
}
const resultText = (finalResult) => {
  if (finalResult === 'PASS') return '通过'
  return '阻断'
}

const loadData = async (page = 1) => {
  pageNum.value = page
  loading.value = true
  try {
    const res = await adminListAuditLogs({ pageNum: pageNum.value, pageSize: pageSize.value })
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

.data-table { border-radius: 12px; overflow: hidden; }
.data-table :deep(.el-table__header-wrapper th) { background: #f8f9fb !important; }
.id-tag { display: inline-block; padding: 2px 8px; background: #f0f2f5; border-radius: 6px; color: #909399; font-size: 12px; font-weight: 500; }

.biz-tag { border-radius: 6px; font-size: 12px; }
.biz-id { color: #909399; font-size: 12px; }

.content-text {
  color: #1a1a2e; font-size: 13px; line-height: 1.5;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}
.detail-dialog :deep(.el-dialog__body) { padding: 20px 24px; max-height: 80vh; overflow-y: auto; }
.detail-content { color: #1a1a2e; font-size: 14px; line-height: 1.8; white-space: pre-wrap; word-break: break-all; }

.hits-wrap { display: flex; flex-wrap: wrap; gap: 4px; }
.hit-tag { border-radius: 4px; font-size: 11px; }
.no-hits { color: #c0c4cc; font-size: 12px; }

.result-badge { display: inline-flex; align-items: center; gap: 4px; padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 500; }
.result-pass { background: #f6ffed; color: #52c41a; }
.result-review { background: #fffbe6; color: #faad14; }
.result-reject { background: #fff1f0; color: #f5222d; }
.action-group { display: flex; justify-content: center; gap: 6px; }
.action-btn { border-radius: 6px; padding: 5px 10px; }

.time-cell { display: flex; align-items: center; gap: 6px; color: #909399; font-size: 13px; }

.empty-state { padding: 60px 0; }
.empty-icon { width: 80px; height: 80px; border-radius: 50%; background: #f5f7fa; display: flex; align-items: center; justify-content: center; margin: 0 auto 16px; color: #c0c4cc; font-size: 36px; }

.pagination-wrap { display: flex; justify-content: flex-end; padding-top: 16px; border-top: 1px solid #f0f2f5; margin-top: 8px; }
.custom-pagination :deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) { background: linear-gradient(135deg, #667eea, #764ba2); }

.detail-section { margin-bottom: 16px; }
.detail-section:last-child { margin-bottom: 0; }
.detail-label { font-size: 13px; font-weight: 600; color: #606266; margin-bottom: 8px; }
.cloud-section { background: #f8f9fb; border-radius: 10px; padding: 14px 16px; }
.cloud-result { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.cloud-reason { color: #1a1a2e; font-size: 13px; }
.cloud-categories { display: flex; flex-wrap: wrap; gap: 6px; }
.cat-tag { font-size: 11px; }
</style>
