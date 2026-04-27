<template>
  <AuthorHeader />
  <div class="main box_center cf">
    <div class="userBox cf">
      <AuthorSidebar active="bookList" />
      <div class="my_r">
        <div id="noContentDiv" v-if="total == 0">
          <div class="tc" style="margin-top: 200px">
            <router-link
              class="redBtn"
              :to="{ name: 'authorChapterAdd', query: { id: bookId } }"
              >章节发布</router-link
            >
          </div>
        </div>
        <div class="my_bookshelf" id="hasContentDiv" v-if="total > 0">
          <div class="title cf">
            <h2 class="fl">章节列表</h2>
            <div class="fr">
              <router-link
                class="redBtn"
                :to="{ name: 'authorChapterAdd', query: { id: bookId } }"
                >新建章节</router-link
              >
            </div>
          </div>

          <div id="divData" class="updateTable">
            <table cellpadding="0" cellspacing="0">
              <thead>
                <tr>
                  <!-- <th class="style">
                                 序号
                             </th>-->
                  <th class="name">章节名</th>
                  <th class="goread">更新时间</th>
                  <th class="goread">是否收费</th>
                  <th class="goread">操作</th>
                </tr>
              </thead>
              <tbody id="bookList">
                <tr
                  class="book_list"
                  vals="291"
                  v-for="(item, index) in chapters"
                  :key="index"
                >
                  <td id="name1358314029098041344" class="name">
                    {{ item.chapterName }}
                  </td>
                  <td class="goread">{{ item.chapterUpdateTime }}<br />更新</td>
                  <td class="goread" valsc="291|2037554|1">
                    {{ item.isVip == 1 ? "收费" : "免费" }}
                  </td>

                  <td class="goread" id="opt1358314029098041344">
                    <router-link
                      
                      :to="{
                        name: 'authorChapterUpdate',
                        query: { id: item.id },
                      }"
                      >修改</router-link
                    >

                    <br />
                    <a
                      href="javascript:void(0);"
                      @click="deleteBookChapter(item.id)"
                      >删除 </a
                    ><br />
                  </td>
                </tr>
              </tbody>
            </table>
            <el-pagination
              small
              layout="prev, pager, next"
              :background="backgroud"
              :page-size="pageSize"
              :total="total"
              class="mt-4"
              @current-change="handleCurrentChange"
            />
          </div>
          <!--<div id="divData" class="updateTable">
                    <table cellpadding="0" cellspacing="0">
                        <thead>
                        <tr>

                            <th class="name">
                                爬虫源（已开启的爬虫源）
                            </th>
                            <th class="chapter">
                                成功爬取数量（websocket实现）
                            </th>
                            <th class="time">
                            目标爬取数量
                            </th>
                            <th class="goread">
                                状态（正在运行，已停止）（一次只能运行一个爬虫源）
                            </th>
                            <th class="goread">
                                操作（启动，停止）
                            </th>
                        </tr>
                        </thead>
                        <tbody id="bookShelfList">



                        </tbody>
                    </table>
                    <div class="pageBox cf" id="shellPage">
                    </div>
                </div>-->
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import "@/assets/styles/book.css";
import { reactive, toRefs, onMounted, ref } from "vue";
import { useRouter, useRoute } from "vue-router";
import { listChapters, deleteChapter } from "@/api/author";
import AuthorHeader from "@/components/author/Header.vue";
import AuthorSidebar from "@/components/author/Sidebar.vue";
export default {
  name: "authorChapterList",
  components: {
    AuthorHeader,
    AuthorSidebar,
  },
  setup() {
    const route = useRoute();
    const router = useRouter();

    const state = reactive({
      bookId: route.query.id,
      chapters: [],
      searchCondition: {},
      backgroud: true,
      total: 0,
      pageSize: 10,
      imgBaseUrl: process.env.VUE_APP_BASE_IMG_URL,
    });
    onMounted(() => {
      load();
    });

    const load = async () => {
      const { data } = await listChapters(state.bookId, state.searchCondition);
      state.chapters = data.list;
      state.searchCondition.pageNum = data.pageNum;
      state.searchCondition.pageSize = data.pageSize;
      state.total = Number(data.total);
    };

    const handleCurrentChange = (pageNum) => {
      state.searchCondition.pageNum = pageNum;
      load();
    };

    const deleteBookChapter = async (id) => {
      await deleteChapter(id);
      load();
    };

    return {
      ...toRefs(state),
      handleCurrentChange,
      load,
      deleteBookChapter,
    };
  },
};
</script>

<style>
.el-pagination {
  justify-content: center;
}
.el-pagination.is-background .el-pager li:not(.is-disabled).is-active {
  background: linear-gradient(135deg, #667eea, #764ba2) !important;
}
.el-pagination {
  --el-pagination-hover-color: #667eea !important;
}
</style>

<style scoped>
.redBtn {
  padding: 6px 16px;
  border-radius: 8px;
  border: none;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-size: 13px;
  transition: all 0.3s ease;
  display: inline-block;
}
a.redBtn:hover {
  color: #fff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}
</style>
