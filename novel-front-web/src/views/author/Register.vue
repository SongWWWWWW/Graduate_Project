<template>
  <div class="channelWrap" style="max-width:800px; margin:80px auto 40px;">
    <div style="padding: 24px 32px;">
      <div style="margin-bottom: 32px;">
        <h2 style="font-size:22px; font-weight:600; margin:0 0 8px;">📝 作者注册</h2>
        <p style="color:#909399; margin:0; font-size:14px;">我是网络小说写手，我要注册为小说精品屋签约作者</p>
      </div>

      <el-form label-width="100px" size="large">
        <el-form-item label="作者笔名：">
          <el-input
            v-model="penName"
            placeholder="请输入笔名"
            maxlength="8"
            style="max-width:320px;"
          />
          <span class="form-tip">长度为 2 到 8 位的中英文</span>
        </el-form-item>

        <el-form-item label="手机号码：">
          <el-input
            v-model="telPhone"
            placeholder="请输入手机号码"
            style="max-width:320px;"
          />
          <span class="form-tip">编辑会通过这个号码与您联系</span>
        </el-form-item>

        <el-form-item label="QQ或微信：">
          <el-input
            v-model="chatAccount"
            placeholder="请输入QQ或微信"
            style="max-width:320px;"
          />
          <span class="form-tip">编辑会通过此账号与您联系</span>
        </el-form-item>

        <el-form-item label="电子邮箱：">
          <el-input
            v-model="email"
            placeholder="请输入电子邮箱"
            style="max-width:320px;"
          />
          <span class="form-tip">用于接收重要通知</span>
        </el-form-item>

        <el-form-item label="作品方向：">
          <el-radio-group v-model="workDirection">
            <el-radio label="0">男频</el-radio>
            <el-radio label="1">女频</el-radio>
          </el-radio-group>
          <span class="form-tip">请选择作品方向</span>
        </el-form-item>

        <el-form-item>
          <button @click="registerAuthor" class="submitBtn">
            立即开始您的作者生涯
          </button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import { reactive, toRefs } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { register } from "@/api/author";

export default {
  name: "authorRegister",
  setup() {
    const router = useRouter();

    const state = reactive({
      penName: "",
      telPhone: "",
      chatAccount: "",
      email: "",
      workDirection: "0",
    });

    const registerAuthor = async () => {
      if (!state.penName) {
        ElMessage.error("笔名不能为空！");
        return;
      }
      if (!state.telPhone) {
        ElMessage.error("手机号不能为空！");
        return;
      }
      if (!/^1[3-9]\d{9}$/.test(state.telPhone)) {
        ElMessage.error("手机号格式不正确！");
        return;
      }
      if (!state.chatAccount) {
        ElMessage.error("QQ或微信账号不能为空！");
        return;
      }
      if (!state.email) {
        ElMessage.error("电子邮箱不能为空！");
        return;
      }
      if (
        !/^[A-Za-z0-9\-_]+[A-Za-z0-9\.\-_]*[A-Za-z0-9\-_]+@[A-Za-z0-9]+[A-Za-z0-9\.\-_]*(\.[A-Za-z0-9\.\-_]+)*[A-Za-z0-9]+\.[A-Za-z0-9]+[A-Za-z0-9\.\-_]*[A-Za-z0-9]+$/.test(
          state.email
        )
      ) {
        ElMessage.error("电子邮箱格式不正确！");
        return;
      }

      const { data } = await register(state);
      ElMessage.success("注册成功！");
      router.push({ name: "authorDashboard" });
    };

    return {
      ...toRefs(state),
      registerAuthor,
    };
  },
};
</script>

<style scoped>
.form-tip {
  color: #909399;
  font-size: 13px;
  margin-left: 12px;
}
.submitBtn {
  display: inline-block;
  padding: 12px 32px;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.submitBtn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}
</style>
