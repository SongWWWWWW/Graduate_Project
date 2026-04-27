<template>
  <div class="np-footer">
    <div class="np-footer-inner">
      <div class="np-footer-content">
        <div class="np-footer-brand">
          <img :src="logo" alt="小说精品屋" class="np-footer-logo" />
          <p class="np-footer-desc">精品小说阅读与创作平台</p>
        </div>
        <div class="np-footer-links">
          <div class="np-link-group">
            <h4>平台</h4>
            <router-link :to="{ name: 'home' }">首页</router-link>
            <router-link :to="{ name: 'bookclass' }">书库</router-link>
            <router-link :to="{ name: 'bookRank' }">排行榜</router-link>
          </div>
          <div class="np-link-group">
            <h4>作者</h4>
            <a @click="goAuthor" href="javascript:void(0)">作家专区</a>
            <a @click="goFeedBack" href="javascript:void(0)">意见反馈</a>
          </div>
        </div>
      </div>
      <div class="np-footer-bottom">
        <p>Copyright © xxyopen.com All rights reserved · 小说精品屋版权所有</p>
      </div>
    </div>
  </div>
</template>

<script>
import logo from "@/assets/images/logo.png";
import { useRouter } from "vue-router";
import { getToken } from "@/utils/auth";
import { getAuthorStatus } from "@/api/author";

export default {
  name: "Footer",
  setup() {
    const router = useRouter();
    const goFeedBack = () => {
      if (!getToken()) {
        router.push({ name: "login" });
      } else {
        router.push({ name: "feadback" });
      }
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
      logo,
      goFeedBack,
      goAuthor,
    };
  },
};
</script>

<style scoped>
.np-footer {
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  color: rgba(255, 255, 255, 0.5);
  margin-top: 40px;
  padding: 40px 24px 20px;
}
.np-footer-inner {
  max-width: 1200px;
  margin: 0 auto;
}
.np-footer-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 30px;
}
.np-footer-brand {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.np-footer-logo {
  height: 36px;
  filter: brightness(0) invert(1);
  opacity: 0.8;
}
.np-footer-desc {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.4);
}
.np-footer-links {
  display: flex;
  gap: 60px;
}
.np-link-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.np-link-group h4 {
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 4px;
}
.np-link-group a {
  color: rgba(255, 255, 255, 0.5);
  font-size: 13px;
  transition: all 0.3s ease;
  cursor: pointer;
}
.np-link-group a:hover {
  color: #fff;
}
.np-footer-bottom {
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  padding-top: 16px;
  text-align: center;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.35);
}
</style>
