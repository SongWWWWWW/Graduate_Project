<template>
  <AuthorHeader />
  <div class="main box_center cf">
    <div class="userBox cf">
      <AuthorSidebar active="profile" />
      <div class="my_r">
        <div class="np-page-title">
          <h2>作家资料</h2>
          <el-button
            v-if="!isEditing"
            type="primary"
            class="np-btn np-btn-primary"
            @click="startEdit"
          >
            <el-icon><Edit /></el-icon>
            编辑资料
          </el-button>
          <div v-else class="edit-actions">
            <el-button @click="cancelEdit">取消</el-button>
            <el-button type="primary" :loading="saving" @click="saveProfile">保存</el-button>
          </div>
        </div>

        <!-- 查看模式 -->
        <div class="profile-wrap np-animate-slide-up" v-if="profile && !isEditing">
          <div class="profile-header-card">
            <div class="profile-avatar-ring">
              <div class="profile-avatar">
                <el-icon size="40" color="#667eea"><UserFilled /></el-icon>
              </div>
            </div>
            <div class="profile-header-info">
              <h3 class="profile-name">{{ profile.penName }}</h3>
              <div class="profile-badges">
                <span :class="['np-status-tag', profile.status === 0 ? 'success' : 'info']">
                  {{ profile.status === 0 ? '正常' : '其他' }}
                </span>
                <span class="np-status-tag info">
                  {{ Number(profile.workDirection) === 0 ? '男频' : Number(profile.workDirection) === 1 ? '女频' : '未设置' }}
                </span>
              </div>
            </div>
            <div class="profile-header-meta">
              <div class="meta-item">
                <div class="meta-label">注册时间</div>
                <div class="meta-value">{{ profile.createTime }}</div>
              </div>
            </div>
          </div>

          <div class="profile-detail">
            <div class="detail-section">
              <div class="section-label">
                <span class="section-line"></span>
                <span>基本信息</span>
              </div>
              <div class="detail-grid">
                <div class="detail-item">
                  <div class="detail-icon"><el-icon><User /></el-icon></div>
                  <div class="detail-content">
                    <div class="detail-label">笔名</div>
                    <div class="detail-value">{{ profile.penName }}</div>
                  </div>
                </div>
                <div class="detail-item">
                  <div class="detail-icon" style="background: linear-gradient(135deg, #667eea22, #764ba222); color: #667eea;">
                    <el-icon><Phone /></el-icon>
                  </div>
                  <div class="detail-content">
                    <div class="detail-label">手机号码</div>
                    <div class="detail-value">{{ profile.telPhone || '未填写' }}</div>
                  </div>
                </div>
                <div class="detail-item">
                  <div class="detail-icon" style="background: linear-gradient(135deg, #11998e22, #38ef7d22); color: #11998e;">
                    <el-icon><ChatRound /></el-icon>
                  </div>
                  <div class="detail-content">
                    <div class="detail-label">QQ / 微信</div>
                    <div class="detail-value">{{ profile.chatAccount || '未填写' }}</div>
                  </div>
                </div>
                <div class="detail-item">
                  <div class="detail-icon" style="background: linear-gradient(135deg, #fc4a1a22, #f7b73322); color: #fc4a1a;">
                    <el-icon><Message /></el-icon>
                  </div>
                  <div class="detail-content">
                    <div class="detail-label">电子邮箱</div>
                    <div class="detail-value">{{ profile.email || '未填写' }}</div>
                  </div>
                </div>
                <div class="detail-item">
                  <div class="detail-icon" style="background: linear-gradient(135deg, #ee097922, #ff6a0022); color: #ee0979;">
                    <el-icon><Compass /></el-icon>
                  </div>
                  <div class="detail-content">
                    <div class="detail-label">作品方向</div>
                    <div class="detail-value">{{ Number(profile.workDirection) === 0 ? '男频' : Number(profile.workDirection) === 1 ? '女频' : '未设置' }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 编辑模式 -->
        <div class="profile-edit np-animate-slide-up" v-if="isEditing">
          <el-form
            ref="formRef"
            :model="editForm"
            :rules="rules"
            label-position="top"
            class="edit-form"
          >
            <div class="edit-section">
              <div class="section-label">
                <span class="section-line"></span>
                <span>编辑资料</span>
              </div>
              <div class="form-grid">
                <el-form-item label="笔名" prop="penName">
                  <el-input v-model="editForm.penName" placeholder="请输入笔名" maxlength="20" show-word-limit />
                </el-form-item>
                <el-form-item label="手机号码" prop="telPhone">
                  <el-input v-model="editForm.telPhone" placeholder="请输入手机号码" maxlength="20" />
                </el-form-item>
                <el-form-item label="QQ / 微信">
                  <el-input v-model="editForm.chatAccount" placeholder="请输入QQ或微信账号" maxlength="50" />
                </el-form-item>
                <el-form-item label="电子邮箱" prop="email">
                  <el-input v-model="editForm.email" placeholder="请输入电子邮箱" maxlength="50" />
                </el-form-item>
                <el-form-item label="作品方向" prop="workDirection">
                  <el-radio-group v-model="editForm.workDirection">
                    <el-radio-button :label="0">男频</el-radio-button>
                    <el-radio-button :label="1">女频</el-radio-button>
                  </el-radio-group>
                </el-form-item>
              </div>
            </div>
          </el-form>
        </div>

        <div v-if="!profile && !isEditing" class="empty-state">
          <el-empty description="未查询到作家资料" />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { reactive, toRefs, ref, onMounted } from "vue";
import { ElMessage } from "element-plus";
import AuthorHeader from "@/components/author/Header.vue";
import AuthorSidebar from "@/components/author/Sidebar.vue";
import { getAuthorProfile, updateAuthorProfile } from "@/api/author";
import { UserFilled, User, Phone, ChatRound, Message, Compass, Edit } from "@element-plus/icons-vue";

export default {
  name: "authorProfile",
  components: { AuthorHeader, AuthorSidebar, UserFilled, User, Phone, ChatRound, Message, Compass, Edit },
  setup() {
    const formRef = ref(null);
    const state = reactive({
      profile: null,
      isEditing: false,
      saving: false,
      editForm: {
        penName: "",
        telPhone: "",
        chatAccount: "",
        email: "",
        workDirection: 0,
      },
    });

    const rules = {
      penName: [{ required: true, message: "笔名不能为空", trigger: "blur" }],
      workDirection: [{ required: true, message: "请选择作品方向", trigger: "change" }],
      email: [{ type: "email", message: "邮箱格式不正确", trigger: "blur" }],
    };

    onMounted(() => {
      loadProfile();
    });

    const loadProfile = async () => {
      try {
        const { data } = await getAuthorProfile();
        state.profile = data;
      } catch (e) {
        console.error(e);
      }
    };

    const startEdit = () => {
      state.editForm = {
        penName: state.profile.penName || "",
        telPhone: state.profile.telPhone || "",
        chatAccount: state.profile.chatAccount || "",
        email: state.profile.email || "",
        workDirection: state.profile.workDirection ?? 0,
      };
      state.isEditing = true;
    };

    const cancelEdit = () => {
      state.isEditing = false;
    };

    const saveProfile = async () => {
      if (!formRef.value) return;
      await formRef.value.validate(async (valid) => {
        if (!valid) return;
        state.saving = true;
        try {
          await updateAuthorProfile(state.editForm);
          ElMessage.success("资料更新成功");
          state.isEditing = false;
          await loadProfile();
        } catch (e) {
          ElMessage.error(e.response?.data?.message || "更新失败");
        } finally {
          state.saving = false;
        }
      });
    };

    return {
      ...toRefs(state),
      formRef,
      rules,
      startEdit,
      cancelEdit,
      saveProfile,
    };
  },
};
</script>

<style scoped>
.edit-actions {
  display: flex;
  gap: 8px;
}

.profile-wrap {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.profile-header-card {
  position: relative;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 32px;
  display: flex;
  align-items: center;
  gap: 24px;
  color: #fff;
  overflow: hidden;
}
.profile-header-card::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -10%;
  width: 300px;
  height: 300px;
  border-radius: 50%;
  background: rgba(255,255,255,0.06);
}
.profile-avatar-ring {
  position: relative;
  z-index: 1;
}
.profile-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: rgba(255,255,255,0.2);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 3px solid rgba(255,255,255,0.3);
}
.profile-header-info {
  flex: 1;
  position: relative;
  z-index: 1;
}
.profile-name {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 10px;
}
.profile-badges {
  display: flex;
  gap: 8px;
}
.profile-header-meta {
  position: relative;
  z-index: 1;
  text-align: right;
}
.profile-header-meta .meta-label {
  font-size: 12px;
  color: rgba(255,255,255,0.6);
  margin-bottom: 4px;
}
.profile-header-meta .meta-value {
  font-size: 14px;
  color: rgba(255,255,255,0.9);
}

.profile-detail {
  background: #fff;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}
.detail-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.section-label {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
}
.section-line {
  width: 4px;
  height: 18px;
  background: linear-gradient(180deg, #667eea, #764ba2);
  border-radius: 2px;
}
.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}
.detail-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  background: #f8f9fc;
  border-radius: 12px;
  transition: all 0.3s ease;
}
.detail-item:hover {
  background: #f0f2f8;
  transform: translateX(4px);
}
.detail-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea22, #764ba222);
  color: #667eea;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}
.detail-content {
  flex: 1;
  min-width: 0;
}
.detail-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}
.detail-value {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a2e;
}

/* 编辑模式 */
.profile-edit {
  background: #fff;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}
.edit-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #1a1a2e;
}
.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px 24px;
}
.form-grid .el-form-item:last-child {
  grid-column: 1 / -1;
}

.empty-state {
  padding: 80px 0;
}

@media (max-width: 900px) {
  .profile-header-card {
    flex-direction: column;
    text-align: center;
  }
  .profile-header-meta {
    text-align: center;
  }
  .detail-grid {
    grid-template-columns: 1fr;
  }
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
