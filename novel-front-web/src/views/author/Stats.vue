<template>
  <AuthorHeader />
  <div class="main box_center cf">
    <div class="userBox cf">
      <AuthorSidebar active="stats" />
      <div class="my_r">
        <div class="np-page-title">
          <h2>数据统计</h2>
        </div>

        <!-- 概览卡片 -->
        <div class="stats-overview">
          <div class="overview-card np-animate-slide-up">
            <div class="overview-icon" style="background: linear-gradient(135deg, #667eea, #764ba2);">
              <el-icon size="22" color="#fff"><View /></el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-value">{{ totalVisitCount }}</div>
              <div class="overview-label">总点击量</div>
            </div>
          </div>
          <div class="overview-card np-animate-slide-up np-animate-delay-1">
            <div class="overview-icon" style="background: linear-gradient(135deg, #11998e, #38ef7d);">
              <el-icon size="22" color="#fff"><Document /></el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-value">{{ formatWordCount(totalWordCount) }}</div>
              <div class="overview-label">总字数</div>
            </div>
          </div>
          <div class="overview-card np-animate-slide-up np-animate-delay-2">
            <div class="overview-icon" style="background: linear-gradient(135deg, #fc4a1a, #f7b733);">
              <el-icon size="22" color="#fff"><ChatDotRound /></el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-value">{{ totalCommentCount }}</div>
              <div class="overview-label">总评论数</div>
            </div>
          </div>
          <div class="overview-card np-animate-slide-up np-animate-delay-3">
            <div class="overview-icon" style="background: linear-gradient(135deg, #ee0979, #ff6a00);">
              <el-icon size="22" color="#fff"><Reading /></el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-value">{{ total }}</div>
              <div class="overview-label">作品数</div>
            </div>
          </div>
        </div>

        <!-- 数据表格 -->
        <div class="np-table-wrap np-animate-slide-up np-animate-delay-2">
          <table>
            <thead>
              <tr>
                <th>作品信息</th>
                <th>分类</th>
                <th>状态</th>
                <th>点击量</th>
                <th>字数</th>
                <th>评论数</th>
                <th>最新章节</th>
                <th>更新时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in books" :key="item.id">
                <td>
                  <div class="book-cell">
                    <img :src="resolveImageUrl(item.picUrl)" class="np-cover" width="40" height="56" @error="handleImageError" />
                    <span class="book-name">{{ item.bookName }}</span>
                  </div>
                </td>
                <td>{{ item.categoryName }}</td>
                <td>
                  <span :class="['np-status-tag', item.bookStatus == 0 ? 'success' : 'info']">
                    {{ item.bookStatus == 0 ? '连载中' : '已完结' }}
                  </span>
                </td>
                <td><strong>{{ item.visitCount }}</strong></td>
                <td>{{ formatWordCount(item.wordCount) }}</td>
                <td>{{ item.commentCount }}</td>
                <td class="chapter-cell">{{ item.lastChapterName || '-' }}</td>
                <td class="time-cell">{{ item.lastChapterUpdateTime || '-' }}</td>
              </tr>
            </tbody>
          </table>
          <div class="table-footer">
            <el-pagination
              small
              layout="prev, pager, next"
              :background="true"
              :page-size="pageSize"
              :total="total"
              @current-change="handleCurrentChange"
            />
          </div>
        </div>

        <div v-if="books.length === 0" class="empty-state">
          <el-empty description="暂无作品数据" />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { reactive, toRefs, onMounted } from "vue";
import AuthorHeader from "@/components/author/Header.vue";
import AuthorSidebar from "@/components/author/Sidebar.vue";
import { listBookStats } from "@/api/author";
import { View, Document, ChatDotRound, Reading } from "@element-plus/icons-vue";
import picUpload from "@/assets/images/pic_upload.png";

export default {
  name: "authorStats",
  components: { AuthorHeader, AuthorSidebar, View, Document, ChatDotRound, Reading },
  setup() {
    const state = reactive({
      books: [],
      searchCondition: {},
      total: 0,
      pageSize: 10,
      totalVisitCount: 0,
      totalWordCount: 0,
      totalCommentCount: 0,
      imgBaseUrl: process.env.VUE_APP_BASE_IMG_URL,
    });

    onMounted(() => {
      load();
    });

    const load = async () => {
      try {
        const { data } = await listBookStats(state.searchCondition);
        state.books = data.list;
        state.searchCondition.pageNum = data.pageNum;
        state.searchCondition.pageSize = data.pageSize;
        state.total = Number(data.total);

        state.totalVisitCount = state.books.reduce((sum, b) => sum + (b.visitCount || 0), 0);
        state.totalWordCount = state.books.reduce((sum, b) => sum + (b.wordCount || 0), 0);
        state.totalCommentCount = state.books.reduce((sum, b) => sum + (b.commentCount || 0), 0);
      } catch (e) {
        console.error(e);
      }
    };

    const handleCurrentChange = (pageNum) => {
      state.searchCondition.pageNum = pageNum;
      load();
    };

    const formatWordCount = (count) => {
      if (!count) return "0";
      if (count >= 10000) return (count / 10000).toFixed(1) + "万";
      if (count >= 1000) return (count / 1000).toFixed(1) + "千";
      return count;
    };

    const resolveImageUrl = (picUrl) => {
      if (!picUrl) return picUpload;
      if (picUrl.startsWith('http')) return picUrl;
      return state.imgBaseUrl + picUrl;
    };

    const handleImageError = (e) => {
      e.target.src = picUpload;
    };

    return {
      ...toRefs(state),
      handleCurrentChange,
      formatWordCount,
      resolveImageUrl,
      handleImageError,
      picUpload,
    };
  },
};
</script>

<style scoped>
.stats-overview {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}
.overview-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  transition: all 0.3s ease;
}
.overview-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);
}
.overview-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.overview-value {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1.2;
}
.overview-label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.book-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}
.book-cell .book-name {
  font-weight: 500;
  color: #1a1a2e;
}
.chapter-cell {
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.time-cell {
  white-space: nowrap;
  font-size: 13px;
}
.table-footer {
  padding: 16px;
  display: flex;
  justify-content: center;
}
.empty-state {
  padding: 60px 0;
}

@media (max-width: 900px) {
  .stats-overview {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
