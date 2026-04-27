<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">敏感词管理</h2>
        <p class="page-subtitle">管理内容审核的敏感词库，新添加的词汇将实时生效</p>
      </div>
      <el-button type="primary" class="add-btn" @click="showAdd = true">
        <el-icon><Plus /></el-icon>
        <span>添加敏感词</span>
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="8">
        <div class="mini-stat">
          <div class="mini-stat-icon"><el-icon><Warning /></el-icon></div>
          <div class="mini-stat-info">
            <div class="mini-stat-value">{{ list.length }}</div>
            <div class="mini-stat-label">敏感词总数</div>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="mini-stat">
          <div class="mini-stat-icon icon-green"><el-icon><Check /></el-icon></div>
          <div class="mini-stat-info">
            <div class="mini-stat-value">{{ enabledCount }}</div>
            <div class="mini-stat-label">已启用</div>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="mini-stat">
          <div class="mini-stat-icon icon-orange"><el-icon><Grid /></el-icon></div>
          <div class="mini-stat-info">
            <div class="mini-stat-value">{{ categoryCount }}</div>
            <div class="mini-stat-label">分类数</div>
          </div>
        </div>
      </el-col>
    </el-row>

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
        <el-table-column prop="word" label="敏感词" min-width="150">
          <template #default="{row}">
            <div class="word-cell">
              <el-icon class="word-icon"><Warning /></el-icon>
              <span class="word-text">{{ row.word }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="120" align="center">
          <template #default="{row}">
            <el-tag
              size="small"
              effect="plain"
              class="category-tag"
              :class="`cat-${row.category}`"
            >{{ row.category || '其他' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="状态" width="100" align="center">
          <template #default="{row}">
            <div class="status-tag" :class="row.enabled == 1 ? 'status-active' : 'status-inactive'">
              <div class="status-dot"></div>
              <span>{{ row.enabled == 1 ? '启用' : '禁用' }}</span>
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
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{row}">
            <el-button type="danger" size="small" class="action-btn" text @click="del(row)">
              <el-icon><Delete /></el-icon>
              <span>删除</span>
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && list.length === 0" description="暂无敏感词" class="empty-state">
        <template #image>
          <div class="empty-icon"><el-icon><Warning /></el-icon></div>
        </template>
      </el-empty>
    </el-card>

    <!-- 添加弹窗 -->
    <el-dialog
      v-model="showAdd"
      title="添加敏感词"
      width="420px"
      class="add-dialog"
      destroy-on-close
    >
      <div class="dialog-body">
        <div class="form-icon">🛡️</div>
        <p class="form-tip">添加后系统将自动过滤包含该词汇的内容</p>
        <el-form :model="form" label-width="0" class="add-form">
          <el-form-item>
            <el-input
              v-model="form.word"
              placeholder="请输入敏感词"
              size="large"
              :prefix-icon="Warning"
              class="dialog-input"
            />
          </el-form-item>
          <el-form-item>
            <el-input
              v-model="form.category"
              placeholder="分类标签，如：政治、色情、暴力"
              size="large"
              :prefix-icon="CollectionTag"
              class="dialog-input"
            />
          </el-form-item>
          <el-form-item>
            <el-select v-model="form.level" placeholder="选择风险等级" size="large" class="dialog-input" style="width: 100%">
              <el-option label="Level 1 - 低危（记录但不处理）" :value="1" />
              <el-option label="Level 2 - 中危（内容替换为*）" :value="2" />
              <el-option label="Level 3 - 高危（直接阻断提交）" :value="3" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button size="large" @click="showAdd = false">取消</el-button>
          <el-button type="primary" size="large" class="confirm-btn" @click="add">
            <el-icon><Plus /></el-icon>
            <span>确认添加</span>
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Warning, Check, Grid, Delete, Clock, CollectionTag
} from '@element-plus/icons-vue'
import { adminListSensitiveWords, adminAddSensitiveWord, adminDeleteSensitiveWord } from '@/api/admin'

const list = ref([])
const loading = ref(false)
const showAdd = ref(false)
const form = reactive({ word: '', category: '' })

const enabledCount = computed(() => list.value.filter(w => w.enabled == 1).length)
const categoryCount = computed(() => {
  const cats = new Set(list.value.map(w => w.category).filter(Boolean))
  return cats.size
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await adminListSensitiveWords()
    list.value = res.data || []
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const add = async () => {
  if (!form.word.trim()) {
    ElMessage.warning('请输入敏感词')
    return
  }
  try {
    await adminAddSensitiveWord({
      word: form.word.trim(),
      category: form.category.trim() || '其他',
      level: form.level,
      isEnabled: 1
    })
    ElMessage.success({ message: '敏感词添加成功', duration: 1500 })
    showAdd.value = false
    form.word = ''
    form.category = ''
    form.level = 1
    loadData()
  } catch (e) {
    ElMessage.error(e.message || '添加失败')
  }
}

const del = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除敏感词「${row.word}」吗？`, '删除确认', { type: 'warning' })
    await adminDeleteSensitiveWord(row.id)
    ElMessage.success({ message: '敏感词已删除', duration: 1500 })
    loadData()
  } catch (e) {}
}

onMounted(() => loadData())
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
  align-items: center;
  margin-bottom: 20px;
}
.page-title { color: #1a1a2e; font-size: 22px; font-weight: 600; margin: 0 0 6px 0; }
.page-subtitle { color: #909399; font-size: 13px; margin: 0; }
.add-btn {
  border-radius: 10px;
  background: linear-gradient(135deg, #f5af19, #f12711);
  border: none;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

/* 迷你统计 */
.stats-row { margin-bottom: 20px; }
.mini-stat {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.04);
}
.mini-stat-icon {
  width: 44px; height: 44px; border-radius: 10px;
  background: linear-gradient(135deg, #f56c6c, #f78989);
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 20px;
}
.mini-stat-icon.icon-green { background: linear-gradient(135deg, #67c23a, #85ce61); }
.mini-stat-icon.icon-orange { background: linear-gradient(135deg, #fa8c16, #ffc53d); }
.mini-stat-value { color: #1a1a2e; font-size: 20px; font-weight: 700; line-height: 1; }
.mini-stat-label { color: #909399; font-size: 12px; margin-top: 4px; }

.data-card { border-radius: 16px; border: none; box-shadow: 0 4px 12px rgba(0,0,0,0.04); }
.data-card :deep(.el-card__body) { padding: 20px; }

.data-table { border-radius: 12px; overflow: hidden; }
.data-table :deep(.el-table__header-wrapper th) { background: #f8f9fb !important; }
.id-tag { display: inline-block; padding: 2px 8px; background: #f0f2f5; border-radius: 6px; color: #909399; font-size: 12px; font-weight: 500; }

.word-cell { display: flex; align-items: center; gap: 8px; }
.word-icon { color: #f56c6c; font-size: 16px; }
.word-text { color: #1a1a2e; font-size: 14px; font-weight: 500; }

.category-tag { border-radius: 6px; font-size: 12px; }
.cat-porn { border-color: #ffd6e7; color: #eb2f96; background: #fff0f6; }
.cat-politics { border-color: #ffd8bf; color: #fa541c; background: #fff7e6; }
.cat-violence { border-color: #ffccc7; color: #f5222d; background: #fff1f0; }
.cat-abuse { border-color: #d3f261; color: #7cb305; background: #fcffe6; }
.cat-ad { border-color: #b7eb8f; color: #389e0d; background: #f6ffed; }

.status-tag { display: inline-flex; align-items: center; gap: 6px; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 500; }
.status-active { background: #f0f9eb; color: #67c23a; }
.status-inactive { background: #f4f4f5; color: #909399; }
.status-dot { width: 6px; height: 6px; border-radius: 50%; background: currentColor; }

.time-cell { display: flex; align-items: center; gap: 6px; color: #909399; font-size: 13px; }
.action-btn { border-radius: 8px; display: inline-flex; align-items: center; gap: 4px; }

.empty-state { padding: 60px 0; }
.empty-icon { width: 80px; height: 80px; border-radius: 50%; background: #f5f7fa; display: flex; align-items: center; justify-content: center; margin: 0 auto 16px; color: #c0c4cc; font-size: 36px; }

/* 添加弹窗 */
.add-dialog :deep(.el-dialog__header) { padding: 20px 24px; border-bottom: 1px solid #f0f2f5; }
.add-dialog :deep(.el-dialog__title) { font-weight: 600; color: #1a1a2e; }
.add-dialog :deep(.el-dialog__body) { padding: 24px; }
.add-dialog :deep(.el-dialog__footer) { padding: 16px 24px; border-top: 1px solid #f0f2f5; }

.dialog-body { text-align: center; }
.form-icon { font-size: 48px; margin-bottom: 8px; }
.form-tip { color: #909399; font-size: 13px; margin-bottom: 20px; }
.add-form { max-width: 320px; margin: 0 auto; }
.dialog-input :deep(.el-input__wrapper) { border-radius: 10px; }

.dialog-footer { display: flex; justify-content: flex-end; gap: 12px; }
.confirm-btn {
  border-radius: 10px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
</style>
