<template>
  <AuthorHeader />
  <div class="main box_center cf">
    <div class="userBox cf">
      <AuthorSidebar active="dashboard" />
      <div class="my_r">
        <!-- 页面标题 -->
        <div class="np-page-title">
          <h2>工作台</h2>
          <router-link :to="{ name: 'authorBookAdd' }" class="np-btn np-btn-primary">
            <el-icon><Plus /></el-icon>
            创建作品
          </router-link>
        </div>

        <!-- 欢迎卡片 -->
        <div class="welcome-banner np-animate-slide-up" v-if="penName">
          <div class="welcome-content">
            <div class="welcome-avatar">
              <el-icon size="36" color="#fff"><UserFilled /></el-icon>
            </div>
            <div class="welcome-text">
              <h3>欢迎回来，{{ penName }}</h3>
              <p>今日也要保持创作热情，笔下生花~</p>
            </div>
          </div>
          <div class="welcome-decoration"></div>
        </div>

        <!-- 统计卡片 -->
        <div class="stats-grid">
          <div class="stat-item np-animate-slide-up np-animate-delay-1 np-glow-hover">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea, #764ba2);">
              <el-icon size="24" color="#fff"><Reading /></el-icon>
            </div>
            <div class="stat-body">
              <div class="stat-value">{{ bookCount }}</div>
              <div class="stat-label">作品数量</div>
            </div>
            <div class="stat-trend" v-if="bookCount > 0">
              <el-icon color="#67c23a"><ArrowUp /></el-icon>
            </div>
          </div>
          <div class="stat-item np-animate-slide-up np-animate-delay-2 np-glow-hover">
            <div class="stat-icon" style="background: linear-gradient(135deg, #11998e, #38ef7d);">
              <el-icon size="24" color="#fff"><Document /></el-icon>
            </div>
            <div class="stat-body">
              <div class="stat-value">{{ formatWordCount(totalWordCount) }}</div>
              <div class="stat-label">总字数</div>
            </div>
          </div>
          <div class="stat-item np-animate-slide-up np-animate-delay-3 np-glow-hover">
            <div class="stat-icon" style="background: linear-gradient(135deg, #fc4a1a, #f7b733);">
              <el-icon size="24" color="#fff"><View /></el-icon>
            </div>
            <div class="stat-body">
              <div class="stat-value">{{ totalVisitCount }}</div>
              <div class="stat-label">总点击量</div>
            </div>
          </div>
          <div class="stat-item np-animate-slide-up np-animate-delay-4 np-glow-hover">
            <div class="stat-icon" style="background: linear-gradient(135deg, #ee0979, #ff6a00);">
              <el-icon size="24" color="#fff"><ChatDotRound /></el-icon>
            </div>
            <div class="stat-body">
              <div class="stat-value">{{ totalCommentCount }}</div>
              <div class="stat-label">总评论数</div>
            </div>
          </div>
        </div>

        <!-- 最近作品 -->
        <div class="section-card np-animate-slide-up np-animate-delay-2" v-if="recentBooks.length > 0">
          <div class="section-header">
            <div class="section-title">
              <span class="section-dot"></span>
              <h3>最近更新</h3>
            </div>
            <router-link :to="{ name: 'authorBookList' }" class="section-link">
              查看全部 <el-icon><ArrowRight /></el-icon>
            </router-link>
          </div>
          <div class="book-grid">
            <div class="book-card" v-for="book in recentBooks" :key="book.id">
              <div class="book-cover-wrap">
                <img :src="resolveImageUrl(book.picUrl)" :alt="book.bookName" class="book-cover" @error="handleImageError" />
                <div class="book-cover-overlay">
                  <router-link :to="{ name: 'authorChapterList', query: { id: book.id } }" class="overlay-btn">
                    管理章节
                  </router-link>
                </div>
              </div>
              <div class="book-info">
                <div class="book-name" :title="book.bookName">{{ book.bookName }}</div>
                <div class="book-meta">
                  <span class="meta-item">
                    <el-icon size="12"><Document /></el-icon>
                    {{ formatWordCount(book.wordCount) }}
                  </span>
                  <span class="meta-item">
                    <el-icon size="12"><View /></el-icon>
                    {{ book.visitCount }}
                  </span>
                </div>
                <div class="book-chapter" v-if="book.lastChapterName" :title="book.lastChapterName">
                  <el-icon size="12" color="#667eea"><Clock /></el-icon>
                  {{ book.lastChapterName }}
                </div>
                <div class="book-time" v-if="book.lastChapterUpdateTime">
                  {{ book.lastChapterUpdateTime }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div class="empty-state" v-if="bookCount === 0">
          <el-empty description="暂无作品，开始你的创作之旅吧">
            <router-link :to="{ name: 'authorBookAdd' }" class="np-btn np-btn-primary">
              创建作品
            </router-link>
          </el-empty>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { reactive, toRefs, onMounted } from "vue";
import AuthorHeader from "@/components/author/Header.vue";
import AuthorSidebar from "@/components/author/Sidebar.vue";
import { getDashboard } from "@/api/author";
import { Reading, Document, View, ChatDotRound, UserFilled, Plus, ArrowRight, ArrowUp, Clock } from "@element-plus/icons-vue";
import picUpload from "@/assets/images/pic_upload.png";

export default {
  name: "authorDashboard",
  components: { AuthorHeader, AuthorSidebar, Reading, Document, View, ChatDotRound, UserFilled, Plus, ArrowRight, ArrowUp, Clock },
  setup() {
    const state = reactive({
      penName: "",
      bookCount: 0,
      totalWordCount: 0,
      totalVisitCount: 0,
      totalCommentCount: 0,
      recentBooks: [],
      imgBaseUrl: process.env.VUE_APP_BASE_IMG_URL,
    });

    onMounted(() => {
      loadDashboard();
    });

    const loadDashboard = async () => {
      try {
        const { data } = await getDashboard();
        if (data) {
          state.penName = data.penName;
          state.bookCount = data.bookCount;
          state.totalWordCount = data.totalWordCount;
          state.totalVisitCount = data.totalVisitCount;
          state.totalCommentCount = data.totalCommentCount;
          state.recentBooks = data.recentBooks || [];
        }
      } catch (e) {
        console.error(e);
      }
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
      formatWordCount,
      resolveImageUrl,
      handleImageError,
      picUpload,
    };
  },
};
</script>

<style scoped>
/* 欢迎横幅 */
.welcome-banner {
  position: relative;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 28px 32px;
  margin-bottom: 24px;
  overflow: hidden;
}
.welcome-content {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  gap: 18px;
}
.welcome-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: rgba(255,255,255,0.2);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid rgba(255,255,255,0.3);
  flex-shrink: 0;
}
.welcome-text h3 {
  font-size: 20px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 6px;
}
.welcome-text p {
  font-size: 14px;
  color: rgba(255,255,255,0.75);
}
.welcome-decoration {
  position: absolute;
  top: -40%;
  right: -10%;
  width: 300px;
  height: 300px;
  border-radius: 50%;
  background: rgba(255,255,255,0.06);
  z-index: 1;
}

/* 统计网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}
.stat-item {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  transition: all 0.3s ease;
  position: relative;
}
.stat-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);
}
.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-body {
  flex: 1;
  min-width: 0;
}
.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1.2;
}
.stat-label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
.stat-trend {
  position: absolute;
  top: 16px;
  right: 16px;
}

/* 区块卡片 */
.section-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
}
.section-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
}
.section-title h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
}
.section-link {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #667eea;
  transition: all 0.3s;
}
.section-link:hover {
  color: #764ba2;
  gap: 8px;
}

/* 书籍网格 */
.book-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}
.book-card {
  display: flex;
  gap: 14px;
  padding: 14px;
  border-radius: 12px;
  background: #f8f9fc;
  transition: all 0.3s ease;
  border: 1px solid transparent;
}
.book-card:hover {
  background: #fff;
  border-color: rgba(102, 126, 234, 0.15);
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.08);
  transform: translateY(-2px);
}
.book-cover-wrap {
  position: relative;
  width: 72px;
  height: 96px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
}
.book-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}
.book-card:hover .book-cover {
  transform: scale(1.05);
}
.book-cover-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}
.book-card:hover .book-cover-overlay {
  opacity: 1;
}
.overlay-btn {
  padding: 6px 14px;
  background: #fff;
  color: #667eea;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}
.book-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.book-name {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.book-meta {
  display: flex;
  gap: 12px;
  margin-bottom: 6px;
}
.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}
.book-chapter {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #667eea;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.book-time {
  font-size: 11px;
  color: #c0c4cc;
  margin-top: 4px;
}

/* 空状态 */
.empty-state {
  padding: 80px 0;
}

@media (max-width: 900px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .book-grid {
    grid-template-columns: 1fr;
  }
}
</style>
