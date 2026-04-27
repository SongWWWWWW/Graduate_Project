<template>
  <AuthorHeader />
  <div class="main box_center cf">
    <div class="userBox cf">
      <AuthorSidebar active="bookList" />
      <div class="my_r">
        <!-- 空状态 -->
        <div v-if="total == 0" class="empty-state">
          <div class="empty-icon">
            <el-icon :size="80" color="#c0c4cc"><Collection /></el-icon>
          </div>
          <h3>暂无作品</h3>
          <p>创建您的第一部小说，开启创作之旅</p>
          <router-link :to="{ name: 'authorBookAdd' }" class="np-btn np-btn-primary">
            <el-icon><Plus /></el-icon>
            创建作品
          </router-link>
        </div>

        <!-- 有内容 -->
        <div v-if="total > 0" class="np-fade-in">
          <!-- 页面标题 -->
          <div class="np-page-title">
            <h2>小说管理</h2>
            <router-link :to="{ name: 'authorBookAdd' }" class="np-btn np-btn-primary">
              <el-icon><Plus /></el-icon>
              发布小说
            </router-link>
          </div>

          <!-- 统计概览 -->
          <div class="stats-row">
            <div class="np-stat-card np-animate-slide-up">
              <div class="np-stat-icon" style="background: linear-gradient(135deg, #667eea, #764ba2);">
                <el-icon><Collection /></el-icon>
              </div>
              <div>
                <div class="np-stat-label">作品总数</div>
                <div class="np-stat-value">{{ total }}</div>
              </div>
            </div>
            <div class="np-stat-card np-animate-slide-up np-animate-delay-1">
              <div class="np-stat-icon" style="background: linear-gradient(135deg, #36cfc9, #4096ff);">
                <el-icon><Document /></el-icon>
              </div>
              <div>
                <div class="np-stat-label">累计字数</div>
                <div class="np-stat-value">{{ formatNumber(totalWords) }}</div>
              </div>
            </div>
            <div class="np-stat-card np-animate-slide-up np-animate-delay-2">
              <div class="np-stat-icon" style="background: linear-gradient(135deg, #ff9c6e, #ff7875);">
                <el-icon><View /></el-icon>
              </div>
              <div>
                <div class="np-stat-label">总点击量</div>
                <div class="np-stat-value">{{ formatNumber(totalVisits) }}</div>
              </div>
            </div>
            <div class="np-stat-card np-animate-slide-up np-animate-delay-3">
              <div class="np-stat-icon" style="background: linear-gradient(135deg, #ffc53d, #faad14);">
                <el-icon><ChatDotRound /></el-icon>
              </div>
              <div>
                <div class="np-stat-label">总评论</div>
                <div class="np-stat-value">{{ formatNumber(totalComments) }}</div>
              </div>
            </div>
          </div>

          <!-- 书籍卡片网格 -->
          <div class="book-grid">
            <div
              v-for="(item, index) in books"
              :key="item.id"
              class="book-card np-glow-hover"
              :class="`np-animate-slide-up np-animate-delay-${Math.min(index % 4, 3)}`"
            >
              <div class="book-card-header">
                <img
                  class="book-cover"
                  :src="resolveImageUrl(item.picUrl)"
                  :alt="item.bookName"
                  @error="handleImageError"
                />
                <div class="book-status">
                  <span class="np-status-tag" :class="item.bookStatus == 0 ? 'success' : 'info'">
                    {{ item.bookStatus == 0 ? '连载中' : '已完结' }}
                  </span>
                </div>
              </div>

              <div class="book-card-body">
                <h3 class="book-name" :title="item.bookName">{{ item.bookName }}</h3>
                <div class="book-category">
                  <el-icon><Folder /></el-icon>
                  {{ item.categoryName }}
                </div>

                <div class="book-stats">
                  <div class="stat-item">
                    <el-icon><Document /></el-icon>
                    <span>{{ wordCountFormat(item.wordCount) }}</span>
                  </div>
                  <div class="stat-item">
                    <el-icon><View /></el-icon>
                    <span>{{ formatNumber(item.visitCount) }}</span>
                  </div>
                  <div class="stat-item">
                    <el-icon><ChatDotRound /></el-icon>
                    <span>{{ item.commentCount || 0 }}</span>
                  </div>
                </div>

                <div class="book-chapter" v-if="item.lastChapterName">
                  <el-icon><Reading /></el-icon>
                  <span class="chapter-name" :title="item.lastChapterName">最新：{{ item.lastChapterName }}</span>
                </div>

                <div class="book-update" v-if="item.updateTime">
                  <el-icon><Clock /></el-icon>
                  <span>{{ formatTime(item.updateTime) }}</span>
                </div>
              </div>

              <div class="book-card-footer">
                <router-link
                  class="action-btn primary"
                  :to="{ name: 'authorChapterList', query: { id: item.id } }"
                >
                  <el-icon><Reading /></el-icon>
                  章节管理
                </router-link>
                <router-link
                  class="action-btn"
                  :to="{ name: 'authorChapterAdd', query: { bookId: item.id } }"
                >
                  <el-icon><Edit /></el-icon>
                  写新章
                </router-link>
              </div>
            </div>
          </div>

          <!-- 分页 -->
          <div class="pagination-wrap">
            <el-pagination
              background
              layout="total, prev, pager, next, jumper"
              :page-size="pageSize"
              :total="total"
              @current-change="handleCurrentChange"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {
  Collection,
  Plus,
  Document,
  View,
  ChatDotRound,
  Folder,
  Reading,
  Clock,
  Edit,
} from "@element-plus/icons-vue";
import { reactive, toRefs, onMounted, computed } from "vue";
import { ElMessage } from "element-plus";
import { listBooks } from "@/api/author";
import AuthorHeader from "@/components/author/Header.vue";
import AuthorSidebar from "@/components/author/Sidebar.vue";
import picUpload from "@/assets/images/pic_upload.png";

export default {
  name: "authorBookList",
  components: {
    AuthorHeader,
    AuthorSidebar,
    Collection,
    Plus,
    Document,
    View,
    ChatDotRound,
    Folder,
    Reading,
    Clock,
    Edit,
  },
  setup() {
    const state = reactive({
      books: [],
      searchCondition: {},
      total: 0,
      pageSize: 8,
      imgBaseUrl: process.env.VUE_APP_BASE_IMG_URL,
    });

    onMounted(() => {
      load();
    });

    const load = async () => {
      try {
        const { data } = await listBooks(state.searchCondition);
        state.books = data?.list || [];
        state.searchCondition.pageNum = data?.pageNum || 1;
        state.searchCondition.pageSize = data?.pageSize || 8;
        state.total = Number(data?.total) || 0;
      } catch (e) {
        console.error(e);
        ElMessage.error(e?.message || "加载小说列表失败");
        state.books = [];
        state.total = 0;
      }
    };

    const handleCurrentChange = (pageNum) => {
      state.searchCondition.pageNum = pageNum;
      load();
    };

    // 计算统计数据
    const totalWords = computed(() => {
      return state.books.reduce((sum, b) => sum + (b.wordCount || 0), 0);
    });
    const totalVisits = computed(() => {
      return state.books.reduce((sum, b) => sum + (b.visitCount || 0), 0);
    });
    const totalComments = computed(() => {
      return state.books.reduce((sum, b) => sum + (b.commentCount || 0), 0);
    });

    // 字数格式化
    const wordCountFormat = (wordCount) => {
      if (!wordCount) return "0";
      if (wordCount >= 10000) {
        return (wordCount / 10000).toFixed(1) + "万";
      }
      if (wordCount >= 1000) {
        return (wordCount / 1000).toFixed(1) + "千";
      }
      return wordCount.toString();
    };

    // 数字格式化
    const formatNumber = (num) => {
      if (!num) return "0";
      if (num >= 10000) {
        return (num / 10000).toFixed(1) + "万";
      }
      return num.toLocaleString();
    };

    // 时间格式化
    const formatTime = (time) => {
      if (!time) return "";
      const date = new Date(time);
      const now = new Date();
      const diff = now - date;
      const minutes = Math.floor(diff / 60000);
      const hours = Math.floor(diff / 3600000);
      const days = Math.floor(diff / 86400000);
      if (minutes < 1) return "刚刚";
      if (minutes < 60) return `${minutes}分钟前`;
      if (hours < 24) return `${hours}小时前`;
      if (days < 30) return `${days}天前`;
      return date.toLocaleDateString("zh-CN");
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
      totalWords,
      totalVisits,
      totalComments,
      handleCurrentChange,
      wordCountFormat,
      formatNumber,
      formatTime,
      resolveImageUrl,
      handleImageError,
      picUpload,
      load,
    };
  },
};
</script>

<style scoped>
/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100px 20px;
  text-align: center;
}
.empty-state .empty-icon {
  margin-bottom: 20px;
}
.empty-state h3 {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}
.empty-state p {
  font-size: 14px;
  color: var(--text-muted);
  margin-bottom: 24px;
}

/* 统计行 */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 28px;
}

/* 书籍卡片网格 */
.book-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
  margin-bottom: 28px;
}

.book-card {
  background: #fff;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  overflow: hidden;
  transition: var(--transition);
  display: flex;
  flex-direction: column;
}
.book-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.book-card-header {
  position: relative;
  height: 160px;
  overflow: hidden;
  background: linear-gradient(135deg, #f5f7fa, #e4e7ed);
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
.book-status {
  position: absolute;
  top: 12px;
  right: 12px;
}

.book-card-body {
  padding: 16px;
  flex: 1;
}
.book-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.book-category {
  font-size: 13px;
  color: var(--text-muted);
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 12px;
}

.book-stats {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--divider-color);
}
.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--text-secondary);
}
.stat-item .el-icon {
  font-size: 14px;
  color: var(--primary);
}

.book-chapter,
.book-update {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 4px;
}
.book-chapter .el-icon {
  font-size: 13px;
  color: var(--primary);
  flex-shrink: 0;
}
.chapter-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-card-footer {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  border-top: 1px solid var(--divider-color);
}
.action-btn {
  flex: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 8px 0;
  border-radius: var(--radius-sm);
  font-size: 13px;
  font-weight: 500;
  text-decoration: none;
  transition: var(--transition);
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
  background: #f5f7fa;
  cursor: pointer;
}
.action-btn:hover {
  background: var(--primary-light);
  color: var(--primary);
  border-color: var(--primary);
}
.action-btn.primary {
  background: var(--primary-gradient);
  color: #fff;
  border: none;
  box-shadow: var(--primary-shadow);
}
.action-btn.primary:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

/* 分页 */
.pagination-wrap {
  display: flex;
  justify-content: center;
  padding: 16px 0;
}

/* 响应式 */
@media (max-width: 1200px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: 1fr;
  }
  .book-grid {
    grid-template-columns: 1fr;
  }
}
</style>
