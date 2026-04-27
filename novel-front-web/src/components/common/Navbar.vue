<template>
  <div class="np-navbar">
    <div class="np-navbar-inner">
      <el-menu
        :default-active="activeIndex"
        mode="horizontal"
        :ellipsis="false"
        router
        class="np-menu"
      >
        <el-menu-item index="/home">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/bookclass">
          <el-icon><Reading /></el-icon>
          <span>全部作品</span>
        </el-menu-item>
        <el-menu-item index="/book_rank">
          <el-icon><Trophy /></el-icon>
          <span>排行榜</span>
        </el-menu-item>
        <el-menu-item index="" @click="goAuthor">
          <el-icon><EditPen /></el-icon>
          <span>作家专区</span>
        </el-menu-item>
      </el-menu>
    </div>
  </div>
</template>

<script>
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { getToken } from "@/utils/auth";
import { getAuthorStatus } from "@/api/author";
import { HomeFilled, Reading, Trophy, EditPen } from "@element-plus/icons-vue";

export default {
  name: "Navbar",
  components: { HomeFilled, Reading, Trophy, EditPen },
  setup() {
    const route = useRoute();
    const router = useRouter();

    const activeIndex = computed(() => {
      return route.path;
    });

    const goAuthor = async () => {
      if (!getToken()) {
        router.push({ name: "login" });
        return;
      }
      const { data } = await getAuthorStatus();
      if (data === null) {
        router.push({ name: "authorRegister" });
        return;
      }
      let routeUrl = router.resolve({ name: "authorDashboard" });
      window.open(routeUrl.href, "_blank");
    };

    return {
      activeIndex,
      goAuthor,
    };
  },
};
</script>

<style scoped>
.np-navbar {
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}
.np-navbar-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}
.np-menu {
  background: transparent !important;
  border-bottom: none !important;
}
.np-menu :deep(.el-menu-item) {
  color: rgba(255, 255, 255, 0.7);
  font-size: 15px;
  height: 52px;
  line-height: 52px;
  border-bottom: none !important;
  transition: all 0.3s ease;
}
.np-menu :deep(.el-menu-item:hover) {
  color: #fff;
  background: rgba(102, 126, 234, 0.15) !important;
}
.np-menu :deep(.el-menu-item.is-active) {
  color: #fff !important;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.5), rgba(118, 75, 162, 0.5)) !important;
  border-radius: 8px 8px 0 0;
}
.np-menu :deep(.el-menu-item .el-icon) {
  margin-right: 6px;
  font-size: 18px;
}
</style>
