<template>
  <div class="my_l">
    <ul class="log_list">
      <li>
        <router-link :class="`${routeName == 'userSetup' ? 'link_8 on' : 'link_8'}`" :to="{ name: 'userSetup' }">
          <el-icon><User /></el-icon> 账号设置
        </router-link>
      </li>
      <li>
        <router-link :class="`${routeName == 'userComment' ? 'link_6 on' : 'link_6'}`" :to="{ name: 'userComment' }">
          <el-icon><ChatDotRound /></el-icon> 我的评论
        </router-link>
      </li>
    </ul>
  </div>
</template>

<script>
import { useRoute } from "vue-router";
import { User, ChatDotRound } from "@element-plus/icons-vue";

export default {
  name: "UserMenu",
  components: { User, ChatDotRound },
  setup() {
    const route = useRoute();
    const routeName = route.name;
    return { routeName };
  },
};
</script>

<style scoped>
.my_l .log_list {
  padding: 0;
}
.my_l li a {
  display: flex;
  align-items: center;
  gap: 10px;
}
.my_l li a .el-icon {
  font-size: 18px;
}
</style>
