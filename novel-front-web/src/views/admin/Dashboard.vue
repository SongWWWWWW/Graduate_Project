<template>
  <div class="dashboard">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="welcome-content">
        <h2 class="welcome-title">👋 欢迎回来，管理员</h2>
        <p class="welcome-desc">今天是 {{ today }}，以下是平台的实时数据概览</p>
      </div>
      <div class="welcome-decoration">
        <div class="deco-circle"></div>
        <div class="deco-circle"></div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :xs="24" :sm="12" :md="12" :lg="6" :xl="6">
        <div class="stat-card stat-card-blue">
          <div class="stat-icon-wrap">
            <el-icon class="stat-icon"><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">总用户数</div>
            <div class="stat-value">{{ formatNumber(stats.userCount) }}</div>
            <div class="stat-trend">
              <el-icon><ArrowUp /></el-icon>
              <span>今日新增 {{ stats.todayNewUsers || 0 }}</span>
            </div>
          </div>
          <div class="stat-bg-icon">
            <el-icon><User /></el-icon>
          </div>
        </div>
      </el-col>

      <el-col :xs="24" :sm="12" :md="12" :lg="6" :xl="6">
        <div class="stat-card stat-card-green">
          <div class="stat-icon-wrap">
            <el-icon class="stat-icon"><EditPen /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">注册作家</div>
            <div class="stat-value">{{ formatNumber(stats.authorCount) }}</div>
            <div class="stat-trend">
              <el-icon><Star /></el-icon>
              <span>平台创作者</span>
            </div>
          </div>
          <div class="stat-bg-icon">
            <el-icon><EditPen /></el-icon>
          </div>
        </div>
      </el-col>

      <el-col :xs="24" :sm="12" :md="12" :lg="6" :xl="6">
        <div class="stat-card stat-card-orange">
          <div class="stat-icon-wrap">
            <el-icon class="stat-icon"><Reading /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">小说总数</div>
            <div class="stat-value">{{ formatNumber(stats.bookCount) }}</div>
            <div class="stat-trend">
              <el-icon><ArrowUp /></el-icon>
              <span>今日新增 {{ stats.todayNewBooks || 0 }}</span>
            </div>
          </div>
          <div class="stat-bg-icon">
            <el-icon><Reading /></el-icon>
          </div>
        </div>
      </el-col>

      <el-col :xs="24" :sm="12" :md="12" :lg="6" :xl="6">
        <div class="stat-card stat-card-purple">
          <div class="stat-icon-wrap">
            <el-icon class="stat-icon"><ChatDotRound /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">评论总数</div>
            <div class="stat-value">{{ formatNumber(stats.commentCount) }}</div>
            <div class="stat-trend">
              <el-icon><ChatLineRound /></el-icon>
              <span>用户互动</span>
            </div>
          </div>
          <div class="stat-bg-icon">
            <el-icon><ChatDotRound /></el-icon>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 功能模块 -->
    <el-row :gutter="20" class="module-row">
      <el-col :xs="24" :lg="16">
        <el-card class="module-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <div class="card-header-left">
                <div class="card-header-icon"><el-icon><Grid /></el-icon></div>
                <span>功能快捷入口</span>
              </div>
            </div>
          </template>
          <div class="quick-actions">
            <div class="action-item" @click="$router.push('/admin/users')">
              <div class="action-icon action-blue"><el-icon><User /></el-icon></div>
              <div class="action-name">用户管理</div>
              <div class="action-desc">管理平台注册用户</div>
            </div>
            <div class="action-item" @click="$router.push('/admin/books')">
              <div class="action-icon action-orange"><el-icon><Reading /></el-icon></div>
              <div class="action-name">小说管理</div>
              <div class="action-desc">管理小说与章节</div>
            </div>
            <div class="action-item" @click="$router.push('/admin/comments')">
              <div class="action-icon action-red"><el-icon><ChatDotRound /></el-icon></div>
              <div class="action-name">内容审核</div>
              <div class="action-desc">审核用户内容记录</div>
            </div>
            <div class="action-item" @click="$router.push('/admin/sensitiveWords')">
              <div class="action-icon action-purple"><el-icon><Warning /></el-icon></div>
              <div class="action-name">敏感词</div>
              <div class="action-desc">管理内容过滤词库</div>
            </div>
            <div class="action-item" @click="$router.push('/admin/auditLogs')">
              <div class="action-icon action-teal"><el-icon><Document /></el-icon></div>
              <div class="action-name">审核日志</div>
              <div class="action-desc">查看内容审核记录</div>
            </div>
            <div class="action-item" @click="$router.push('/admin/authors')">
              <div class="action-icon action-green"><el-icon><EditPen /></el-icon></div>
              <div class="action-name">作家管理</div>
              <div class="action-desc">管理平台签约作家</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="8">
        <el-card class="module-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <div class="card-header-left">
                <div class="card-header-icon info-icon"><el-icon><InfoFilled /></el-icon></div>
                <span>系统信息</span>
              </div>
            </div>
          </template>
          <div class="system-info">
            <div class="info-item">
              <div class="info-dot info-dot-blue"></div>
              <div class="info-content">
                <div class="info-title">AI 写作助手</div>
                <div class="info-desc">支持角色设定、续写、润色等4种模式</div>
              </div>
            </div>
            <div class="info-item">
              <div class="info-dot info-dot-green"></div>
              <div class="info-content">
                <div class="info-title">AI 语义搜索</div>
                <div class="info-desc">基于 Milvus 向量数据库的智能检索</div>
              </div>
            </div>
            <div class="info-item">
              <div class="info-dot info-dot-orange"></div>
              <div class="info-content">
                <div class="info-title">内容安全审核</div>
                <div class="info-desc">AC 自动机敏感词过滤 + 审核日志</div>
              </div>
            </div>
            <div class="info-item">
              <div class="info-dot info-dot-purple"></div>
              <div class="info-content">
                <div class="info-title">消息队列</div>
                <div class="info-desc">RabbitMQ 异步处理书籍变更事件</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { reactive, onMounted, computed } from 'vue'
import {
  User, EditPen, Reading, ChatDotRound, ChatLineRound,
  ArrowUp, Star, Grid, Document, Warning, InfoFilled
} from '@element-plus/icons-vue'
import { adminGetStats } from '@/api/admin'

const stats = reactive({
  userCount: 0, authorCount: 0, bookCount: 0, commentCount: 0,
  todayNewUsers: 0, todayNewBooks: 0
})

const today = computed(() => {
  const d = new Date()
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
})

const formatNumber = (num) => {
  if (!num) return '0'
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  return num.toString()
}

onMounted(async () => {
  try {
    const res = await adminGetStats()
    Object.assign(stats, res.data)
  } catch (e) {}
})
</script>

<style scoped>
.dashboard {
  animation: fadeIn 0.5s ease;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 欢迎横幅 */
.welcome-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 28px 32px;
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.3);
}
.welcome-title {
  color: #fff;
  font-size: 22px;
  font-weight: 600;
  margin: 0 0 8px 0;
}
.welcome-desc {
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
  margin: 0;
}
.welcome-decoration {
  display: flex;
  gap: 12px;
}
.deco-circle {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}
.deco-circle:nth-child(2) {
  width: 40px;
  height: 40px;
  margin-top: 20px;
}

/* 统计卡片 */
.stat-row {
  margin-bottom: 24px;
}
.stat-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
  margin-bottom: 20px;
}
.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08);
}
.stat-icon-wrap {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-icon {
  font-size: 28px;
  color: #fff;
}
.stat-card-blue .stat-icon-wrap { background: linear-gradient(135deg, #667eea, #764ba2); }
.stat-card-green .stat-icon-wrap { background: linear-gradient(135deg, #42b883, #347474); }
.stat-card-orange .stat-icon-wrap { background: linear-gradient(135deg, #f5af19, #f12711); }
.stat-card-purple .stat-icon-wrap { background: linear-gradient(135deg, #a855f7, #6366f1); }

.stat-info {
  flex: 1;
  position: relative;
  z-index: 1;
}
.stat-label {
  color: #909399;
  font-size: 13px;
  margin-bottom: 6px;
}
.stat-value {
  color: #1a1a2e;
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 6px;
  line-height: 1;
}
.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #67c23a;
  font-size: 12px;
}
.stat-card-green .stat-trend { color: #42b883; }
.stat-card-orange .stat-trend { color: #f5af19; }
.stat-card-purple .stat-trend { color: #a855f7; }

.stat-bg-icon {
  position: absolute;
  right: -10px;
  bottom: -10px;
  font-size: 100px;
  opacity: 0.03;
  color: #1a1a2e;
}
.stat-bg-icon .el-icon { font-size: inherit; }

/* 功能模块 */
.module-row { margin-bottom: 20px; }
.module-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
}
.module-card :deep(.el-card__header) {
  border-bottom: 1px solid #f0f2f5;
  padding: 16px 20px;
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.card-header-left {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
  color: #1a1a2e;
}
.card-header-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}
.card-header-icon.info-icon {
  background: linear-gradient(135deg, #4facfe, #00f2fe);
}

/* 快捷入口 */
.quick-actions {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}
.action-item {
  padding: 20px;
  border-radius: 12px;
  background: #f8f9fb;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: center;
}
.action-item:hover {
  background: #fff;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  transform: translateY(-4px);
}
.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 12px;
  color: #fff;
  font-size: 22px;
}
.action-blue { background: linear-gradient(135deg, #667eea, #764ba2); }
.action-orange { background: linear-gradient(135deg, #f5af19, #f12711); }
.action-red { background: linear-gradient(135deg, #f56c6c, #e6a23c); }
.action-purple { background: linear-gradient(135deg, #a855f7, #6366f1); }
.action-teal { background: linear-gradient(135deg, #14b8a6, #06b6d4); }
.action-green { background: linear-gradient(135deg, #42b883, #347474); }

.action-name {
  color: #1a1a2e;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 4px;
}
.action-desc {
  color: #909399;
  font-size: 12px;
}

/* 系统信息 */
.system-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.info-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  border-radius: 10px;
  background: #f8f9fb;
  transition: all 0.3s ease;
}
.info-item:hover {
  background: #f0f2f5;
}
.info-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-top: 5px;
  flex-shrink: 0;
}
.info-dot-blue { background: linear-gradient(135deg, #667eea, #764ba2); }
.info-dot-green { background: linear-gradient(135deg, #42b883, #347474); }
.info-dot-orange { background: linear-gradient(135deg, #f5af19, #f12711); }
.info-dot-purple { background: linear-gradient(135deg, #a855f7, #6366f1); }

.info-title {
  color: #1a1a2e;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
}
.info-desc {
  color: #909399;
  font-size: 12px;
}

@media (max-width: 768px) {
  .quick-actions {
    grid-template-columns: repeat(2, 1fr);
  }
  .welcome-banner {
    padding: 20px;
  }
  .welcome-decoration {
    display: none;
  }
}
</style>
