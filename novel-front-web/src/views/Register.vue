<template>
  <div class="np-login-page">
    <!-- 背景装饰 -->
    <div class="np-login-bg">
      <div class="bg-circle bg-circle-1"></div>
      <div class="bg-circle bg-circle-2"></div>
      <div class="bg-circle bg-circle-3"></div>
    </div>

    <div class="np-login-wrapper">
      <!-- 左侧品牌区 -->
      <div class="np-brand-section">
        <div class="np-brand-logo">📚</div>
        <h1 class="np-brand-title">小说精品屋</h1>
        <p class="np-brand-subtitle">发现好书 · 创作精彩</p>
        <div class="np-brand-features">
          <div class="feature-item">
            <el-icon><Check /></el-icon>
            <span>海量精品小说</span>
          </div>
          <div class="feature-item">
            <el-icon><Check /></el-icon>
            <span>AI 智能写作辅助</span>
          </div>
          <div class="feature-item">
            <el-icon><Check /></el-icon>
            <span>作家创作平台</span>
          </div>
        </div>
      </div>

      <!-- 右侧注册卡片 -->
      <div class="np-login-card-wrapper">
        <div class="np-login-card">
          <div class="np-login-header">
            <div class="np-login-avatar">
              <el-icon><UserFilled /></el-icon>
            </div>
            <h2 class="np-login-title">注册账号</h2>
            <p class="np-login-desc">加入小说精品屋，开启阅读之旅</p>
          </div>

          <el-form :model="form" :rules="rules" ref="formRef" label-width="0" class="np-login-form">
            <el-form-item prop="username">
              <el-input
                v-model="form.username"
                placeholder="请输入手机号码"
                size="large"
                :prefix-icon="User"
                class="np-login-input"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                placeholder="请输入密码（6-20位字母/数字）"
                size="large"
                type="password"
                show-password
                :prefix-icon="Lock"
                class="np-login-input"
              />
            </el-form-item>
            <el-form-item prop="velCode">
              <div class="np-verify-row">
                <el-input
                  v-model="form.velCode"
                  placeholder="请输入验证码"
                  size="large"
                  maxlength="4"
                  :prefix-icon="Key"
                  class="np-login-input np-verify-input"
                />
                <img
                  class="np-verify-img"
                  :src="imgVerifyCode"
                  @click="loadImgVerifyCode"
                  alt="验证码"
                />
              </div>
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                class="np-login-btn"
                @click="handleRegister"
                :loading="loading"
              >
                <el-icon v-if="!loading"><Right /></el-icon>
                <span>立即注册</span>
              </el-button>
            </el-form-item>
          </el-form>

          <div class="np-login-footer">
            <span class="np-login-tip">已有账号？</span>
            <router-link :to="{ name: 'login' }" class="np-login-link">立即登录</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { reactive, ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { User, Lock, Key, Right, Check, UserFilled } from "@element-plus/icons-vue";
import { getImgVerifyCode } from "@/api/resource";
import { register } from "@/api/user";
import { setToken, setNickName, setUid } from "@/utils/auth";

export default {
  name: "Register",
  components: { User, Lock, Key, Right, Check, UserFilled },
  setup() {
    const router = useRouter();
    const loading = ref(false);
    const formRef = ref();
    const imgVerifyCode = ref("");
    const sessionId = ref("");

    const form = reactive({
      username: "",
      password: "",
      velCode: "",
    });

    const rules = {
      username: [{ required: true, message: "请输入手机号码", trigger: "blur" }],
      password: [{ required: true, message: "请输入密码", trigger: "blur" }],
      velCode: [{ required: true, message: "请输入验证码", trigger: "blur" }],
    };

    const loadImgVerifyCode = async () => {
      try {
        const { data } = await getImgVerifyCode();
        imgVerifyCode.value = "data:image/png;base64," + data.img;
        sessionId.value = data.sessionId;
      } catch (e) {}
    };

    const handleRegister = async () => {
      try {
        await formRef.value.validate();
      } catch (e) {
        return;
      }
      if (!form.username) {
        ElMessage.error("用户名不能为空！");
        return;
      }
      if (!/^1[3|4|5|6|7|8|9][0-9]{9}/.test(form.username)) {
        ElMessage.error("手机号格式不正确！");
        return;
      }
      if (!form.password) {
        ElMessage.error("密码不能为空！");
        return;
      }
      loading.value = true;
      try {
        const { data } = await register(form);
        setToken(data.token);
        setUid(data.uid);
        setNickName(data.nickName);
        ElMessage.success({ message: "注册成功！", duration: 1500 });
        router.push({ path: "/home" });
      } catch (e) {
        ElMessage.error(e.message || "注册失败");
      } finally {
        loading.value = false;
      }
    };

    onMounted(() => {
      loadImgVerifyCode();
    });

    return {
      formRef,
      form,
      rules,
      loading,
      imgVerifyCode,
      loadImgVerifyCode,
      handleRegister,
    };
  },
};
</script>

<style scoped>
.np-login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  position: relative;
  overflow: hidden;
}

.np-login-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
}
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

.np-login-wrapper {
  display: flex;
  width: 900px;
  min-height: 560px;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
  overflow: hidden;
  z-index: 1;
}

.np-brand-section {
  flex: 1;
  padding: 60px 50px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.3), rgba(118, 75, 162, 0.3));
  position: relative;
}
.np-brand-section::before {
  content: '';
  position: absolute;
  inset: 0;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
  opacity: 0.5;
}
.np-brand-logo {
  font-size: 64px;
  margin-bottom: 20px;
  animation: bounce 2s ease-in-out infinite;
  position: relative;
}
@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}
.np-brand-title {
  color: #fff;
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 10px 0;
  position: relative;
  text-shadow: 0 2px 10px rgba(0,0,0,0.3);
}
.np-brand-subtitle {
  color: rgba(255, 255, 255, 0.7);
  font-size: 16px;
  margin: 0 0 40px 0;
  position: relative;
}
.np-brand-features {
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

.np-login-card-wrapper {
  width: 400px;
  padding: 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: rgba(255, 255, 255, 0.95);
}
.np-login-card {
  width: 100%;
}
.np-login-header {
  text-align: center;
  margin-bottom: 30px;
}
.np-login-avatar {
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
.np-login-avatar .el-icon {
  font-size: 32px;
  color: #fff;
}
.np-login-title {
  color: #1a1a2e;
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 8px 0;
}
.np-login-desc {
  color: #909399;
  font-size: 14px;
  margin: 0;
}

.np-login-form {
  margin-top: 10px;
}
.np-login-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  padding: 4px 15px;
}
.np-login-input :deep(.el-input__inner) {
  height: 44px;
}

.np-verify-row {
  display: flex;
  gap: 12px;
  align-items: center;
}
.np-verify-input {
  flex: 1;
}
.np-verify-img {
  height: 44px;
  border-radius: 8px;
  cursor: pointer;
  border: 1px solid #e4e7ed;
  transition: all 0.3s ease;
}
.np-verify-img:hover {
  border-color: #667eea;
}

.np-login-btn {
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
.np-login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 25px rgba(102, 126, 234, 0.45);
}

.np-login-footer {
  margin-top: 20px;
  text-align: center;
  font-size: 14px;
  color: #909399;
}
.np-login-link {
  color: #667eea;
  font-weight: 500;
  margin-left: 6px;
}
.np-login-link:hover {
  text-decoration: underline;
}

@media (max-width: 768px) {
  .np-login-wrapper {
    width: 90%;
    min-height: auto;
    flex-direction: column;
  }
  .np-brand-section {
    padding: 30px;
    display: none;
  }
  .np-login-card-wrapper {
    width: 100%;
    padding: 40px 30px;
  }
}
</style>
