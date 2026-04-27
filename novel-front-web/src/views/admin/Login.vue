<template>
  <div class="admin-login">
    <!-- 背景装饰圆 -->
    <div class="bg-circle bg-circle-1"></div>
    <div class="bg-circle bg-circle-2"></div>
    <div class="bg-circle bg-circle-3"></div>

    <div class="login-wrapper">
      <!-- 左侧品牌区 -->
      <div class="brand-section">
        <div class="brand-logo">📚</div>
        <h1 class="brand-title">小说创作平台</h1>
        <p class="brand-subtitle">智能化内容管理后台</p>
        <div class="brand-features">
          <div class="feature-item">
            <el-icon><Check /></el-icon>
            <span>AI 辅助创作</span>
          </div>
          <div class="feature-item">
            <el-icon><Check /></el-icon>
            <span>智能内容审核</span>
          </div>
          <div class="feature-item">
            <el-icon><Check /></el-icon>
            <span>向量语义搜索</span>
          </div>
        </div>
      </div>

      <!-- 右侧登录卡片 -->
      <div class="login-card-wrapper">
        <div class="login-card">
          <div class="login-header">
            <div class="login-avatar">
              <el-icon><UserFilled /></el-icon>
            </div>
            <h2 class="login-title">管理员登录</h2>
            <p class="login-desc">欢迎回到管理后台</p>
          </div>

          <el-form :model="form" :rules="rules" ref="formRef" label-width="0" class="login-form">
            <el-form-item prop="username">
              <el-input
                v-model="form.username"
                placeholder="请输入用户名"
                size="large"
                :prefix-icon="User"
                class="login-input"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                placeholder="请输入密码"
                size="large"
                type="password"
                show-password
                :prefix-icon="Lock"
                class="login-input"
                @keyup.enter="handleLogin"
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                class="login-btn"
                @click="handleLogin"
                :loading="loading"
              >
                <el-icon v-if="!loading"><Right /></el-icon>
                <span>立即登录</span>
              </el-button>
            </el-form-item>
          </el-form>

          <div class="login-footer">
            <p>默认账号：admin / admin123</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Right, Check, UserFilled } from '@element-plus/icons-vue'
import { adminLogin, ADMIN_TOKEN_KEY } from '@/api/admin'

const router = useRouter()
const loading = ref(false)
const formRef = ref()
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await adminLogin(form)
    localStorage.setItem(ADMIN_TOKEN_KEY, res.data.token)
    ElMessage.success({ message: '登录成功，欢迎回来！', duration: 1500 })
    router.push('/admin/dashboard')
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.admin-login {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  position: relative;
  overflow: hidden;
}

/* 背景装饰圆 */
.bg-circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.4;
  animation: float 8s ease-in-out infinite;
}
.bg-circle-1 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  top: -100px;
  left: -100px;
  animation-delay: 0s;
}
.bg-circle-2 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #f093fb, #f5576c);
  bottom: -50px;
  right: -50px;
  animation-delay: -3s;
}
.bg-circle-3 {
  width: 250px;
  height: 250px;
  background: linear-gradient(135deg, #4facfe, #00f2fe);
  top: 50%;
  left: 50%;
  animation-delay: -5s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(30px, -30px) scale(1.05); }
  66% { transform: translate(-20px, 20px) scale(0.95); }
}

/* 登录包装器 */
.login-wrapper {
  display: flex;
  width: 900px;
  height: 560px;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
  overflow: hidden;
  z-index: 1;
}

/* 左侧品牌区 */
.brand-section {
  flex: 1;
  padding: 60px 50px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.3), rgba(118, 75, 162, 0.3));
  position: relative;
}
.brand-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
  opacity: 0.5;
}
.brand-logo {
  font-size: 64px;
  margin-bottom: 20px;
  animation: bounce 2s ease-in-out infinite;
  position: relative;
}
@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}
.brand-title {
  color: #fff;
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 10px 0;
  position: relative;
  text-shadow: 0 2px 10px rgba(0,0,0,0.3);
}
.brand-subtitle {
  color: rgba(255, 255, 255, 0.7);
  font-size: 16px;
  margin: 0 0 40px 0;
  position: relative;
}
.brand-features {
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: relative;
}
.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  color: rgba(255, 255, 255, 0.85);
  font-size: 14px;
}
.feature-item .el-icon {
  color: #67c23a;
  font-size: 18px;
  background: rgba(103, 194, 58, 0.2);
  border-radius: 50%;
  padding: 4px;
}

/* 右侧登录卡片 */
.login-card-wrapper {
  width: 400px;
  padding: 50px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: rgba(255, 255, 255, 0.95);
}
.login-card {
  width: 100%;
}
.login-header {
  text-align: center;
  margin-bottom: 35px;
}
.login-avatar {
  width: 70px;
  height: 70px;
  margin: 0 auto 16px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}
.login-avatar .el-icon {
  font-size: 32px;
  color: #fff;
}
.login-title {
  color: #1a1a2e;
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 8px 0;
}
.login-desc {
  color: #909399;
  font-size: 14px;
  margin: 0;
}

.login-form {
  margin-top: 10px;
}
.login-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  padding: 4px 15px;
}
.login-input :deep(.el-input__inner) {
  height: 44px;
}

.login-btn {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 500;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.35);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 25px rgba(102, 126, 234, 0.45);
}
.login-btn:active {
  transform: translateY(0);
}

.login-footer {
  margin-top: 24px;
  text-align: center;
}
.login-footer p {
  color: #c0c4cc;
  font-size: 12px;
  margin: 0;
}

/* 响应式 */
@media (max-width: 768px) {
  .login-wrapper {
    width: 90%;
    height: auto;
    flex-direction: column;
  }
  .brand-section {
    padding: 30px;
    display: none;
  }
  .login-card-wrapper {
    width: 100%;
    padding: 40px 30px;
  }
}
</style>
