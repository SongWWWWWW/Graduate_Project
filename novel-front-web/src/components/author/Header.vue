<template>
  <div class="np-author-header">
    <div class="np-author-header-inner">
      <div class="np-author-brand">
        <el-icon class="np-author-icon"><EditPen /></el-icon>
        <span class="np-author-title">作家管理中心</span>
      </div>
      <div class="np-author-actions">
        <el-button class="np-author-btn" @click="goHome">
          <el-icon><HomeFilled /></el-icon>
          <span>返回首页</span>
        </el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { useRouter } from "vue-router";
import { EditPen, HomeFilled } from "@element-plus/icons-vue";

export default {
  name: "AuthorHeader",
  components: { EditPen, HomeFilled },
  setup() {
    const router = useRouter();
    const goHome = () => {
      router.push({ name: "home" });
    };
    return { goHome };
  },
};
</script>

<style scoped>
.np-author-header {
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}
.np-author-header-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.np-author-brand {
  display: flex;
  align-items: center;
  gap: 10px;
}
.np-author-icon {
  font-size: 22px;
  color: #667eea;
}
.np-author-title {
  color: #fff;
  font-size: 17px;
  font-weight: 600;
  letter-spacing: 1px;
  background: linear-gradient(135deg, #fff, #a8b1ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.np-author-btn {
  background: rgba(102, 126, 234, 0.2);
  border: 1px solid rgba(102, 126, 234, 0.3);
  color: rgba(255, 255, 255, 0.8);
  border-radius: 10px;
  transition: all 0.3s ease;
}
.np-author-btn:hover {
  background: rgba(102, 126, 234, 0.3);
  color: #fff;
}
</style>
