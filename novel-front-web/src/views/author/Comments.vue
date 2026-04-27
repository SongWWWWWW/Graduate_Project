<template>
  <AuthorHeader />
  <div class="main box_center cf">
    <div class="userBox cf">
      <AuthorSidebar active="comments" />
      <div class="my_r">
        <div class="np-page-title">
          <h2>互动管理</h2>
          <span class="title-desc">共 {{ total }} 条读者评论</span>
        </div>

        <!-- 评论卡片列表 -->
        <div class="comment-list">
          <div
            class="comment-card np-animate-slide-up"
            :class="`np-animate-delay-${(index % 4) + 1}`"
            v-for="(item, index) in comments"
            :key="item.id"
          >
            <div class="comment-header">
              <div class="comment-user">
                <img
                  :src="item.userPhoto || defaultAvatar"
                  class="user-avatar"
                  alt="avatar"
                />
                <div class="user-info">
                  <div class="user-name">{{ item.userNickName || '匿名用户' }}</div>
                  <div class="comment-time">{{ item.createTime }}</div>
                </div>
              </div>
              <div class="comment-book">
                <img :src="resolveImageUrl(item.bookPic)" class="book-thumb" v-if="item.bookPic" />
                <span class="book-name">{{ item.bookName }}</span>
              </div>
            </div>
            <div class="comment-body">
              <p class="comment-text">{{ item.commentContent }}</p>
            </div>
            <div class="comment-footer">
              <div class="comment-actions">
                <span class="action-item">
                  <el-icon size="14"><ChatLineRound /></el-icon>
                  {{ item.replyCount }} 回复
                </span>
              </div>
              <span :class="['np-status-tag',
                item.auditStatus === 1 ? 'success' :
                item.auditStatus === 2 ? 'danger' : 'warning']">
                {{ item.auditStatus === 1 ? '审核通过' : item.auditStatus === 2 ? '审核不通过' : '待审核' }}
              </span>
            </div>
          </div>
        </div>

        <div class="table-footer" v-if="total > 0">
          <el-pagination
            small
            layout="prev, pager, next"
            :background="true"
            :page-size="pageSize"
            :total="total"
            @current-change="handleCurrentChange"
          />
        </div>

        <div v-if="comments.length === 0" class="empty-state">
          <el-empty description="暂无评论">
            <template #description>
              <div class="empty-text">暂无读者评论</div>
              <div class="empty-tip">与读者互动是创作的重要动力</div>
            </template>
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
import { listAuthorComments } from "@/api/author";
import { ChatLineRound } from "@element-plus/icons-vue";
import picUpload from "@/assets/images/pic_upload.png";

export default {
  name: "authorComments",
  components: { AuthorHeader, AuthorSidebar, ChatLineRound },
  setup() {
    const state = reactive({
      comments: [],
      searchCondition: {},
      total: 0,
      pageSize: 10,
      imgBaseUrl: process.env.VUE_APP_BASE_IMG_URL,
      defaultAvatar: "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png",
    });

    onMounted(() => {
      load();
    });

    const load = async () => {
      try {
        const { data } = await listAuthorComments(null, state.searchCondition);
        state.comments = data.list;
        state.searchCondition.pageNum = data.pageNum;
        state.searchCondition.pageSize = data.pageSize;
        state.total = Number(data.total);
      } catch (e) {
        console.error(e);
      }
    };

    const handleCurrentChange = (pageNum) => {
      state.searchCondition.pageNum = pageNum;
      load();
    };

    const resolveImageUrl = (url) => {
      if (!url) return picUpload;
      if (url.startsWith('http')) return url;
      return state.imgBaseUrl + url;
    };

    return {
      ...toRefs(state),
      handleCurrentChange,
      resolveImageUrl,
      picUpload,
    };
  },
};
</script>

<style scoped>
.title-desc {
  font-size: 13px;
  color: #909399;
}
.comment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.comment-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  transition: all 0.3s ease;
  border: 1px solid transparent;
}
.comment-card:hover {
  box-shadow: 0 4px 20px rgba(0,0,0,0.06);
  border-color: rgba(102, 126, 234, 0.1);
  transform: translateY(-1px);
}
.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}
.comment-user {
  display: flex;
  align-items: center;
  gap: 10px;
}
.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(102, 126, 234, 0.15);
}
.user-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a2e;
}
.comment-time {
  font-size: 12px;
  color: #c0c4cc;
}
.comment-book {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #f8f9fc;
  padding: 6px 12px;
  border-radius: 20px;
}
.book-thumb {
  width: 24px;
  height: 32px;
  border-radius: 4px;
  object-fit: cover;
}
.book-name {
  font-size: 12px;
  color: #606266;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.comment-body {
  margin-bottom: 12px;
}
.comment-text {
  font-size: 14px;
  color: #606266;
  line-height: 1.7;
  word-break: break-all;
}
.comment-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f2f5;
}
.comment-actions {
  display: flex;
  gap: 16px;
}
.action-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #909399;
  transition: color 0.2s;
}
.action-item:hover {
  color: #667eea;
}
.table-footer {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
.empty-state {
  padding: 60px 0;
}
.empty-text {
  font-size: 15px;
  color: #606266;
  margin-bottom: 6px;
}
.empty-tip {
  font-size: 13px;
  color: #909399;
}
</style>
