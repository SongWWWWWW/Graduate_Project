<template>
  <div class="my_l">
    <ul class="log_list">
      <li>
        <router-link
          :class="['link_4', active === 'dashboard' ? 'on' : '']"
          :to="{ name: 'authorDashboard' }"
        >工作台</router-link>
      </li>
      <li>
        <router-link
          :class="['link_4', active === 'bookList' ? 'on' : '']"
          :to="{ name: 'authorBookList' }"
        >小说管理</router-link>
      </li>
      <li>
        <router-link
          :class="['link_4', active === 'stats' ? 'on' : '']"
          :to="{ name: 'authorStats' }"
        >数据统计</router-link>
      </li>
      <li>
        <router-link
          :class="['link_4', active === 'income' ? 'on' : '']"
          :to="{ name: 'authorIncome' }"
        >收益管理</router-link>
      </li>
      <li>
        <router-link
          :class="['link_4', active === 'comments' ? 'on' : '']"
          :to="{ name: 'authorComments' }"
        >互动管理</router-link>
      </li>
      <li>
        <router-link
          :class="['link_4', active === 'profile' ? 'on' : '']"
          :to="{ name: 'authorProfile' }"
        >作家资料</router-link>
      </li>
    </ul>
  </div>
</template>

<script>
export default {
  name: "AuthorSidebar",
  props: {
    active: {
      type: String,
      default: "dashboard",
    },
  },
};
</script>
