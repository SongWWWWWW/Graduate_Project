<template>
  <Header />

  <div class="main box_center cf mb50">
    <div class="channelRankingContent cf">
      <div class="wrap_left fl">
        <div class="wrap_bg">
          <!--榜单详情 start-->
          <div class="pad20">
            <div class="book_tit">
              <div class="fl">
                <h3 class="font26 mt5 mb5" id="rankName">{{ rankName }}</h3>
              </div>
              <a class="fr"></a>
            </div>
            <div class="updateTable rankTable">
              <table cellpadding="0" cellspacing="0" style="table-layout: fixed !important; width: 100% !important;">
                <thead>
                  <tr>
                    <th class="rank" style="width: 55px !important; padding-left: 4px !important; padding-right: 4px !important; text-align: center !important; white-space: nowrap !important;">排名</th>
                    <th class="cover" style="width: 45px !important; padding-left: 4px !important; padding-right: 4px !important; text-align: center !important; white-space: nowrap !important;">封面</th>
                    <th class="style" style="width: 65px !important; padding-left: 4px !important; padding-right: 4px !important; text-align: center !important; white-space: nowrap !important;">类别</th>
                    <th class="name" style="width: 140px !important; padding-left: 4px !important; padding-right: 4px !important; white-space: nowrap !important;">书名</th>
                    <th class="chapter" style="width: 160px !important; padding-left: 4px !important; padding-right: 4px !important; white-space: nowrap !important;">最新章节</th>
                    <th class="author" style="width: 111px !important; padding-left: 4px !important; padding-right: 4px !important; white-space: nowrap !important;">作者</th>
                    <th class="word" style="width: 50px !important; padding-left: 4px !important; padding-right: 4px !important; text-align: right !important; white-space: nowrap !important;">字数</th>
                  </tr>
                </thead>
                <tbody id="bookRankList">
                  <tr v-for="(item, index) in books" :key="index">
                    <td class="rank">
                      <i :class="'num' + (Number(`${index}`) + 1)">{{
                        index + 1
                      }}</i>
                    </td>
                    <td class="cover">
                      <img
                        class="book-cover-thumb"
                        :src="resolveImageUrl(item.picUrl)"
                        :alt="item.bookName"
                        @error="handleImageError"
                        @click="bookDetail(item.id)"
                      />
                    </td>
                    <td class="style">
                      <a href="javascript:void(0)" @click="bookDetail(item.id)"
                        >[{{ item.categoryName }}]</a
                      >
                    </td>
                    <td class="name">
                      <a
                        @click="bookDetail(item.id)"
                        href="javascript:void(0)"
                        >{{ item.bookName }}</a
                      >
                    </td>
                    <td class="chapter">
                      <a
                        @click="bookDetail(item.id)"
                        href="javascript:void(0)"
                        >{{ item.lastChapterName }}</a
                      >
                    </td>
                    <td class="author">
                      <a href="javascript:void(0)">{{ item.authorName }}</a>
                    </td>
                    <td class="word">{{ wordCountFormat(item.wordCount) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <!--榜单详情 end-->
        </div>
      </div>

      <div class="wrap_right fr">
        <div class="wrap_inner wrap_right_cont mb20">
          <div class="title cf noborder">
            <h3 class="on">排行榜</h3>
          </div>
          <div class="rightList2">
            <ul id="rankType">
              <li>
                <a :class="`${rankType == 1 ? 'on' : ''}`" href="javascript:void(0)" @click="visitRank"
                  >点击榜</a
                >
              </li>
              <li>
                <a :class="`${rankType == 2 ? 'on' : ''}`" href="javascript:void(0)" @click="newestRank">新书榜</a>
              </li>
              <li>
                <a :class="`${rankType == 3 ? 'on' : ''}`" href="javascript:void(0)" @click="updateRank">更新榜</a>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
  <Footer />
</template>

<script>
import "@/assets/styles/book.css";
import { reactive, toRefs, onMounted, ref } from "vue";
import { useRouter, useRoute } from "vue-router";
import {
  listVisitRankBooks,
  listUpdateRankBooks,
  listNewestRankBooks,
} from "@/api/book";
import Header from "@/components/common/Header";
import Footer from "@/components/common/Footer";
import picUpload from "@/assets/images/pic_upload.png";
export default {
  name: "bookRank",
  components: {
    Header,
    Footer,
  },
  setup() {
    const route = useRoute();
    const router = useRouter();

    const state = reactive({
      books: [],
      rankName: "点击榜",
      rankType: 1,
      imgBaseUrl: process.env.VUE_APP_BASE_IMG_URL,
    });
    onMounted(() => {
      visitRank();
    });

    const visitRank = async () => {
      const { data } = await listVisitRankBooks();
      state.books = data;
      state.rankName = "点击榜";
      state.rankType = 1;
    };

    const newestRank = async () => {
      const { data } = await listNewestRankBooks();
      state.books = data;
      state.rankName = "新书榜";
      state.rankType = 2;
    };

    const updateRank = async () => {
      const { data } = await listUpdateRankBooks();
      state.books = data;
      state.rankName = "更新榜";
      state.rankType = 3;
    };

    const bookDetail = (bookId) => {
      router.push({ path: `/book/${bookId}` });
    };

    const resolveImageUrl = (url) => {
      if (!url) return picUpload;
      if (url.startsWith('http')) return url;
      return state.imgBaseUrl + url;
    };

    const handleImageError = (e) => {
      e.target.src = picUpload;
    };

    return {
      ...toRefs(state),
      bookDetail,
      newestRank,
      visitRank,
      updateRank,
      resolveImageUrl,
      handleImageError,
    };
  },
  computed: {
    wordCountFormat(wordCount) {
      return (wordCount) => {
        if (wordCount.length > 5) {
          return parseInt(wordCount / 10000) + "万";
        }
        if (wordCount.length > 4) {
          return parseInt(wordCount / 1000) + "千";
        }
        return wordCount;
      };
    },
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

/* ===== 封面缩略图 ===== */
.book-cover-thumb {
  width: 36px;
  height: 48px;
  object-fit: cover;
  border-radius: 3px;
  cursor: pointer;
  display: block;
  margin: 0 auto;
}

/* ===== 表格行高自适应（覆盖 theme.css 的固定 48px） ===== */
.updateTable td,
.updateTable th {
  height: auto !important;
  line-height: 1.4 !important;
  padding-top: 8px !important;
  padding-bottom: 8px !important;
}

/* 链接溢出省略 */
.updateTable .name a,
.updateTable .chapter a,
.updateTable .author a {
  display: inline-block;
  max-width: 100% !important;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  height: auto !important;
  line-height: inherit !important;
}
</style>
