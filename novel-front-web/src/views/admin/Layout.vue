<template>
  <el-container class="admin-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
      <div class="logo">
        <span class="logo-icon">📚</span>
        <span v-show="!isCollapse" class="logo-text">小说管理平台</span>
      </div>
      <el-scrollbar class="menu-scrollbar">
        <el-menu
          :default-active="$route.path"
          :collapse="isCollapse"
          :collapse-transition="false"
          router
          class="admin-menu"
        >
          <el-menu-item index="/admin/dashboard">
            <el-icon class="menu-icon"><DataLine /></el-icon>
            <template #title><span>仪表盘</span></template>
          </el-menu-item>

          <div class="menu-divider" v-show="!isCollapse">用户管理</div>
          <div class="menu-divider collapsed" v-show="isCollapse"></div>

          <el-menu-item index="/admin/users">
            <el-icon class="menu-icon"><User /></el-icon>
            <template #title><span>用户管理</span></template>
          </el-menu-item>
          <el-menu-item index="/admin/authors">
            <el-icon class="menu-icon"><EditPen /></el-icon>
            <template #title><span>作家管理</span></template>
          </el-menu-item>

          <div class="menu-divider" v-show="!isCollapse">内容管理</div>
          <div class="menu-divider collapsed" v-show="isCollapse"></div>

          <el-menu-item index="/admin/books">
            <el-icon class="menu-icon"><Reading /></el-icon>
            <template #title><span>小说管理</span></template>
          </el-menu-item>
          <el-menu-item index="/admin/bookImport">
            <el-icon class="menu-icon"><Upload /></el-icon>
            <template #title><span>书籍导入</span></template>
          </el-menu-item>
          <el-menu-item index="/admin/comments">
            <el-icon class="menu-icon"><ChatDotRound /></el-icon>
            <template #title><span>评论审核</span></template>
          </el-menu-item>
          <el-menu-item index="/admin/chapterAudit">
            <el-icon class="menu-icon"><DocumentChecked /></el-icon>
            <template #title><span>章节审核</span></template>
          </el-menu-item>

          <div class="menu-divider" v-show="!isCollapse">系统管理</div>
          <div class="menu-divider collapsed" v-show="isCollapse"></div>

          <el-menu-item index="/admin/auditLogs">
            <el-icon class="menu-icon"><Document /></el-icon>
            <template #title><span>审核日志</span></template>
          </el-menu-item>
          <el-menu-item index="/admin/sensitiveWords">
            <el-icon class="menu-icon"><Warning /></el-icon>
            <template #title><span>敏感词管理</span></template>
          </el-menu-item>
        </el-menu>
      </el-scrollbar>

      <!-- 折叠按钮 -->
      <div class="collapse-btn" @click="toggleCollapse">
        <el-icon><Fold v-if="!isCollapse" /><Expand v-else /></el-icon>
      </div>
    </el-aside>

    <el-container :style="{ marginLeft: isCollapse ? '64px' : '220px' }">
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="breadcrumb">
          <el-icon class="breadcrumb-icon"><Location /></el-icon>
          <span class="breadcrumb-text">{{ pageTitle }}</span>
        </div>
        <div class="header-right">
          <div class="user-card">
            <div class="user-avatar">
              <el-icon><UserFilled /></el-icon>
            </div>
            <div class="user-info">
              <div class="user-name">{{ userInfo.username }}</div>
              <div class="user-role">{{ userInfo.roleName }}</div>
            </div>
          </div>
          <el-divider direction="vertical" />
          <el-tooltip content="退出登录" placement="bottom">
            <div class="logout-btn" @click="logout">
              <el-icon><SwitchButton /></el-icon>
            </div>
          </el-tooltip>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="main">
        <div class="content-wrapper">
          <router-view v-slot="{ Component }">
            <transition name="fade-transform" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { reactive, onMounted, ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  DataLine, User, EditPen, Reading, Upload, ChatDotRound, DocumentChecked, Document, Warning,
  Fold, Expand, Location, UserFilled, SwitchButton
} from '@element-plus/icons-vue'
import { adminProfile, ADMIN_TOKEN_KEY } from '@/api/admin'

const router = useRouter()
const route = useRoute()
const userInfo = reactive({ username: '', roleName: '' })
const isCollapse = ref(false)

const pageTitle = computed(() => {
  const titles = {
    '/admin/dashboard': '仪表盘',
    '/admin/users': '用户管理',
    '/admin/authors': '作家管理',
    '/admin/books': '小说管理',
    '/admin/bookImport': '书籍导入',
    '/admin/comments': '评论审核',
    '/admin/chapterAudit': '章节审核',
    '/admin/auditLogs': '审核日志',
    '/admin/sensitiveWords': '敏感词管理'
  }
  return titles[route.path] || '管理后台'
})

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

onMounted(async () => {
  try {
    const res = await adminProfile()
    userInfo.username = res.data.username
    userInfo.roleName = res.data.roleName
  } catch (e) {
    router.push('/admin/login')
  }
})

const logout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '退出确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
    customClass: 'logout-dialog'
  }).then(() => {
    localStorage.removeItem(ADMIN_TOKEN_KEY)
    ElMessage.success({ message: '已安全退出', duration: 1500 })
    router.push('/admin/login')
  }).catch(() => {})
}
</script>

<style scoped>
.admin-layout { height: 100vh; overflow: hidden; }

/* ========== 侧边栏 ========== */
.sidebar {
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  transition: width 0.3s ease;
  display: flex;
  flex-direction: column;
  position: fixed;
  left: 0; top: 0; bottom: 0;
  z-index: 100;
  box-shadow: 4px 0 20px rgba(0, 0, 0, 0.15);
}
.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  flex-shrink: 0;
}
.logo-icon {
  font-size: 28px;
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.3));
}
.logo-text {
  color: #fff;
  font-size: 17px;
  font-weight: 600;
  letter-spacing: 1px;
  background: linear-gradient(135deg, #fff, #a8b1ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.menu-scrollbar {
  flex: 1;
  overflow: hidden;
}
.menu-scrollbar :deep(.el-scrollbar__wrap) {
  overflow-x: hidden;
}

.admin-menu {
  background: transparent !important;
  border-right: none !important;
}
.admin-menu :deep(.el-menu-item) {
  color: rgba(255, 255, 255, 0.65);
  height: 50px;
  line-height: 50px;
  margin: 4px 10px;
  border-radius: 10px;
  transition: all 0.3s ease;
}
.admin-menu :deep(.el-menu-item:hover) {
  color: #fff;
  background: rgba(102, 126, 234, 0.15);
}
.admin-menu :deep(.el-menu-item.is-active) {
  color: #fff;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.5), rgba(118, 75, 162, 0.5));
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}
.admin-menu :deep(.el-menu-item .el-icon) {
  color: inherit;
}
.menu-icon {
  font-size: 18px;
}

.menu-divider {
  padding: 16px 20px 8px;
  color: rgba(255, 255, 255, 0.35);
  font-size: 11px;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 1px;
}
.menu-divider.collapsed {
  padding: 10px;
  margin: 0 10px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.collapse-btn {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  transition: all 0.3s ease;
  flex-shrink: 0;
}
.collapse-btn:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.05);
}
.collapse-btn .el-icon {
  font-size: 18px;
}

/* ========== 顶部导航 ========== */
.header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 64px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  position: relative;
  z-index: 10;
}
.breadcrumb {
  display: flex;
  align-items: center;
  gap: 10px;
}
.breadcrumb-icon {
  color: #667eea;
  font-size: 18px;
}
.breadcrumb-text {
  color: #1a1a2e;
  font-size: 18px;
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.user-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px;
  border-radius: 12px;
  background: #f5f7fa;
  transition: all 0.3s ease;
}
.user-card:hover {
  background: #eef1f6;
}
.user-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
}
.user-info {
  line-height: 1.3;
}
.user-name {
  color: #1a1a2e;
  font-size: 14px;
  font-weight: 500;
}
.user-role {
  color: #909399;
  font-size: 11px;
}

.logout-btn {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #f56c6c;
  background: #fef0f0;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 18px;
}
.logout-btn:hover {
  background: #f56c6c;
  color: #fff;
  transform: scale(1.05);
}

/* ========== 主内容区 ========== */
.admin-layout > .el-container {
  height: 100vh;
  flex-direction: column;
  transition: margin-left 0.3s ease;
}
.main {
  background: #f0f2f5;
  padding: 0;
  flex: 1;
  overflow-y: auto;
}
.content-wrapper {
  padding: 20px;
}

/* ========== 页面过渡动画 ========== */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s ease;
}
.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}
.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(20px);
}
</style>
