<template>
  <div class="np-top">
    <div class="np-top-inner">
      <!-- Logo -->
      <router-link :to="{ name: 'home' }" class="np-logo">
        <img :src="logo" alt="小说精品屋" />
      </router-link>

      <!-- Search -->
      <div class="np-search">
        <el-input
          v-model="keyword"
          placeholder="书名、作者、关键字"
          class="np-search-input"
          @keyup.enter="searchByK"
        >
          <template #append>
            <el-button @click="searchByK" class="np-search-btn">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
      </div>

      <!-- User Actions -->
      <div class="np-user-actions">
        <template v-if="!token">
          <router-link :to="{ name: 'login' }" class="np-link">登录</router-link>
          <el-divider direction="vertical" />
          <router-link :to="{ name: 'register' }" class="np-link">注册</router-link>
        </template>
        <template v-else>
          <el-dropdown>
            <div class="np-user-card">
              <div class="np-user-avatar">
                <el-icon><UserFilled /></el-icon>
              </div>
              <span class="np-user-name">{{ nickName }}</span>
              <el-icon class="np-arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push({ name: 'userSetup' })">
                  <el-icon><User /></el-icon> 个人中心
                </el-dropdown-item>
                <el-dropdown-item @click="goAuthor">
                  <el-icon><EditPen /></el-icon> 作家专区
                </el-dropdown-item>
                <el-dropdown-item divided @click="logout">
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </div>
    </div>
  </div>
</template>

<script>
import logo from "@/assets/images/logo.png";
import { reactive, toRefs } from "vue";
import { useRouter, useRoute } from "vue-router";
import { getToken, getNickName, removeToken, removeNickName, removeUid } from "@/utils/auth";
import { getAuthorStatus } from "@/api/author";
import { Search, UserFilled, ArrowDown, User, EditPen, SwitchButton } from "@element-plus/icons-vue";

export default {
  name: "Top",
  components: { Search, UserFilled, ArrowDown, User, EditPen, SwitchButton },
  setup(props, context) {
    const state = reactive({
      keyword: "",
      nickName: getNickName(),
      token: getToken(),
    });
    const route = useRoute();
    const router = useRouter();
    state.keyword = route.query.key || "";

    const searchByK = () => {
      router.push({ path: "/bookclass", query: { key: state.keyword } });
      context.emit("eventSerch", state.keyword);
    };

    const logout = () => {
      removeToken();
      removeNickName();
      removeUid();
      state.nickName = "";
      state.token = "";
      router.push({ name: "home" });
    };

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
      ...toRefs(state),
      logo,
      searchByK,
      logout,
      goAuthor,
    };
  },
};
</script>

<style scoped>
.np-top {
  background: #fff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  position: relative;
  z-index: 100;
}
.np-top-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.np-logo img {
  height: 40px;
  display: block;
}

.np-search {
  flex: 1;
  max-width: 480px;
  margin: 0 40px;
}
.np-search-input :deep(.el-input__wrapper) {
  border-radius: 12px 0 0 12px;
  box-shadow: 0 0 0 1px #e4e7ed inset;
  padding-left: 16px;
}
.np-search-input :deep(.el-input-group__append) {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  border-radius: 0 12px 12px 0;
  padding: 0 16px;
}
.np-search-btn {
  color: #fff;
  background: transparent;
  border: none;
}
.np-search-btn:hover {
  color: #fff;
  opacity: 0.9;
}

.np-user-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
.np-link {
  color: #606266;
  font-size: 14px;
  padding: 6px 12px;
  border-radius: 8px;
  transition: all 0.3s ease;
}
.np-link:hover {
  color: #667eea;
  background: rgba(102, 126, 234, 0.08);
}

.np-user-card {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border-radius: 12px;
  background: #f5f7fa;
  cursor: pointer;
  transition: all 0.3s ease;
}
.np-user-card:hover {
  background: #eef1f6;
}
.np-user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 14px;
}
.np-user-name {
  font-size: 14px;
  color: #1a1a2e;
  font-weight: 500;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.np-arrow {
  color: #909399;
  font-size: 12px;
}
</style>
