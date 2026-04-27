<template>
  <AuthorHeader />
  <div class="main box_center cf">
    <div class="userBox cf">
      <!-- 左侧菜单 -->
      <AuthorSidebar active="aiWriter" />

      <!-- 右侧内容 -->
      <div class="my_r" style="padding: 20px;">
        <div class="ai-writer-container">
          <!-- 顶部工具栏 -->
          <div class="ai-toolbar">
            <div class="mode-selector">
              <el-radio-group v-model="chatMode" size="small">
                <el-radio-button label="0">通用对话</el-radio-button>
                <el-radio-button label="1">智能续写</el-radio-button>
                <el-radio-button label="3">润色优化</el-radio-button>
                <el-radio-button label="2">大纲生成</el-radio-button>
              </el-radio-group>
            </div>
            <div class="character-selector">
              <el-select v-model="selectedCharacterId" placeholder="选择角色/文风设定" size="small" clearable style="width: 180px">
                <el-option label="默认（无设定）" :value="null" />
                <el-option v-for="char in characters" :key="char.id" :label="char.name" :value="char.id" />
              </el-select>
              <el-button size="small" @click="showCharacterDialog = true">管理设定</el-button>
            </div>
          </div>

          <!-- 续写模式的前文输入区 -->
          <div v-if="chatMode == '1'" class="context-area">
            <el-input
              v-model="contextText"
              type="textarea"
              :rows="3"
              placeholder="请输入前文内容，AI 将根据此内容续写..."
            />
            <div class="context-tools">
              <el-input-number v-model="targetLength" :min="100" :max="5000" :step="100" size="small" />
              <span style="margin-left: 8px; color: #666;">字</span>
            </div>
          </div>

          <!-- 润色模式的文本输入区 -->
          <div v-if="chatMode == '3'" class="context-area">
            <el-input
              v-model="contextText"
              type="textarea"
              :rows="3"
              placeholder="请输入需要润色的文本..."
            />
          </div>

          <!-- 聊天区域 -->
          <div class="chat-area" ref="chatAreaRef">
            <div v-if="messages.length === 0" class="empty-tip">
              <el-empty description="开始与 AI 对话吧" />
              <div class="quick-prompts">
                <el-tag v-for="(tip, idx) in quickTips" :key="idx" class="quick-tag" @click="useQuickTip(tip)">
                  {{ tip }}
                </el-tag>
              </div>
            </div>
            <div v-for="(msg, idx) in messages" :key="idx" :class="['message', msg.role]">
              <div class="message-avatar">
                <el-avatar :size="32" :icon="msg.role === 'user' ? 'UserFilled' : 'Cpu'" :class="msg.role" />
              </div>
              <div class="message-content">
                <div class="message-text" v-html="formatText(msg.content)"></div>
                <div v-if="msg.role === 'assistant'" class="message-actions">
                  <el-link type="primary" :underline="false" @click="copyText(msg.content)">复制</el-link>
                  <el-link type="primary" :underline="false" @click="insertToEditor(msg.content)">插入编辑器</el-link>
                </div>
              </div>
            </div>
            <div v-if="isStreaming" class="message assistant streaming">
              <div class="message-avatar">
                <el-avatar :size="32" icon="Cpu" class="assistant" />
              </div>
              <div class="message-content">
                <div class="message-text">{{ streamingText }}<span class="cursor">|</span></div>
              </div>
            </div>
          </div>

          <!-- 输入区域 -->
          <div class="input-area">
            <el-input
              v-model="inputText"
              type="textarea"
              :rows="3"
              :placeholder="inputPlaceholder"
              @keydown.enter.prevent="handleEnter"
            />
            <div class="input-actions">
              <el-button v-if="isStreaming" type="danger" @click="stopStream">停止生成</el-button>
              <el-button v-else type="primary" :disabled="!inputText.trim()" @click="sendMessage">
                发送
              </el-button>
              <el-button @click="newSession">新会话</el-button>
            </div>
          </div>

          <!-- 会话列表侧边栏 -->
          <div class="session-sidebar">
            <div class="session-header">
              <span>会话历史</span>
              <el-button link size="small" @click="loadSessions">刷新</el-button>
            </div>
            <div class="session-list">
              <div
                v-for="session in sessions"
                :key="session.sessionId"
                :class="['session-item', currentSessionId === session.sessionId ? 'active' : '']"
                @click="switchSession(session.sessionId)"
              >
                <div class="session-title">{{ session.title || '新会话' }}</div>
                <div class="session-time">{{ formatTime(session.updateTime) }}</div>
                <el-icon class="session-delete" @click.stop="deleteSession(session.sessionId)"><Close /></el-icon>
              </div>
              <el-empty v-if="sessions.length === 0" description="暂无会话" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 角色设定管理弹窗 -->
  <el-dialog v-model="showCharacterDialog" title="角色/文风设定管理" width="600px">
    <el-form :model="characterForm" label-width="80px">
      <el-form-item label="设定名">
        <el-input v-model="characterForm.name" placeholder="如：热血玄幻风" />
      </el-form-item>
      <el-form-item label="人设/世界观">
        <el-input v-model="characterForm.persona" type="textarea" :rows="3" placeholder="描述世界观、人物设定..." />
      </el-form-item>
      <el-form-item label="文风描述">
        <el-input v-model="characterForm.style" type="textarea" :rows="2" placeholder="如：语言热血，节奏明快，对话生动" />
      </el-form-item>
      <el-form-item>
        <el-checkbox v-model="characterForm.isDefault">设为默认</el-checkbox>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showCharacterDialog = false">取消</el-button>
      <el-button type="primary" @click="saveCharacter">保存</el-button>
    </template>
    <el-divider />
    <div class="character-list">
      <div v-for="char in characters" :key="char.id" class="character-item">
        <div>
          <strong>{{ char.name }}</strong>
          <el-tag v-if="char.isDefault" size="small" type="success">默认</el-tag>
        </div>
        <div class="character-desc">{{ char.persona || char.style }}</div>
        <el-button link type="danger" size="small" @click="deleteCharacter(char.id)">删除</el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script>
import { ref, reactive, onMounted, watch, nextTick } from 'vue';
import { ElMessage } from 'element-plus';
import AuthorHeader from '@/components/author/Header.vue';
import AuthorSidebar from '@/components/author/Sidebar.vue';
import {
  aiStreamChat,
  aiGenerateOutline,
  aiSaveCharacter,
  aiListCharacters,
  aiDeleteCharacter,
  aiListSessions,
  aiDeleteSession
} from '@/api/author';

export default {
  name: 'AiWriter',
  components: { AuthorHeader, AuthorSidebar },
  setup() {
    // ========== 状态 ==========
    const chatMode = ref('0'); // 0-通用 1-续写 2-大纲 3-润色
    const inputText = ref('');
    const contextText = ref('');
    const targetLength = ref(500);
    const messages = ref([]);
    const isStreaming = ref(false);
    const streamingText = ref('');
    const currentSessionId = ref('');
    const sessions = ref([]);
    const characters = ref([]);
    const selectedCharacterId = ref(null);
    const showCharacterDialog = ref(false);
    const chatAreaRef = ref(null);
    const abortController = ref(null);

    const characterForm = reactive({
      name: '',
      persona: '',
      style: '',
      isDefault: false
    });

    const quickTips = [
      '帮我写一个修仙小说的开篇',
      '设计一个亦正亦邪的主角',
      '写一个反派出场的压迫感场景',
      '帮我构思一个反转剧情'
    ];

    const inputPlaceholder = ref('输入你的问题或创作需求...');

    // ========== 监听模式切换 ==========
    watch(chatMode, (val) => {
      const placeholders = {
        '0': '输入你的问题或创作需求...',
        '1': '输入续写提示（如：主角遭遇强敌后的反应）...',
        '2': '输入小说主题或关键词...',
        '3': '输入润色要求（如：让这段对话更生动）...'
      };
      inputPlaceholder.value = placeholders[val] || placeholders['0'];
    });

    // ========== 滚动到底部 ==========
    const scrollToBottom = () => {
      nextTick(() => {
        if (chatAreaRef.value) {
          chatAreaRef.value.scrollTop = chatAreaRef.value.scrollHeight;
        }
      });
    };

    // ========== 发送消息 ==========
    const sendMessage = async () => {
      const text = inputText.value.trim();
      if (!text) return;

      // 大纲模式使用非流式接口
      if (chatMode.value === '2') {
        messages.value.push({ role: 'user', content: text });
        inputText.value = '';
        try {
          const res = await aiGenerateOutline(text);
          messages.value.push({ role: 'assistant', content: res.data });
          scrollToBottom();
        } catch (e) {
          ElMessage.error('生成大纲失败');
        }
        return;
      }

      // 添加用户消息
      messages.value.push({ role: 'user', content: text });
      inputText.value = '';
      isStreaming.value = true;
      streamingText.value = '';
      scrollToBottom();

      // 构建请求参数
      const params = {
        prompt: text,
        sessionId: currentSessionId.value || undefined,
        sessionType: parseInt(chatMode.value),
        characterId: selectedCharacterId.value || undefined,
        context: contextText.value || undefined,
        targetLength: chatMode.value === '1' ? targetLength.value : undefined
      };

      try {
        abortController.value = new AbortController();
        const response = await aiStreamChat(params);

        if (!response.ok) {
          throw new Error('网络请求失败');
        }

        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        let done = false;

        while (!done) {
          const { value, done: streamDone } = await reader.read();
          done = streamDone;
          if (value) {
            const chunk = decoder.decode(value, { stream: true });
            // 解析 SSE 格式数据
            const lines = chunk.split('\n');
            for (const line of lines) {
              if (line.startsWith('data:')) {
                const data = line.substring(5).trim();
                if (data === '[DONE]') {
                  done = true;
                } else {
                  streamingText.value += data;
                  scrollToBottom();
                }
              }
            }
          }
        }

        // 流式结束，保存完整回复
        messages.value.push({ role: 'assistant', content: streamingText.value });
      } catch (err) {
        if (err.name !== 'AbortError') {
          ElMessage.error('AI 响应失败：' + err.message);
        }
      } finally {
        isStreaming.value = false;
        streamingText.value = '';
        abortController.value = null;
        scrollToBottom();
      }
    };

    const stopStream = () => {
      if (abortController.value) {
        abortController.value.abort();
      }
    };

    const handleEnter = (e) => {
      if (!e.shiftKey) {
        sendMessage();
      }
    };

    // ========== 会话管理 ==========
    const newSession = () => {
      currentSessionId.value = '';
      messages.value = [];
      contextText.value = '';
    };

    const switchSession = (sessionId) => {
      currentSessionId.value = sessionId;
      messages.value = []; // TODO: 加载历史消息
      ElMessage.info('已切换会话');
    };

    const loadSessions = async () => {
      try {
        const res = await aiListSessions(chatMode.value);
        sessions.value = res.data || [];
      } catch (e) {
        console.error('加载会话失败', e);
      }
    };

    const deleteSession = async (sessionId) => {
      try {
        await aiDeleteSession(sessionId);
        ElMessage.success('删除成功');
        if (currentSessionId.value === sessionId) {
          newSession();
        }
        loadSessions();
      } catch (e) {
        ElMessage.error('删除失败');
      }
    };

    // ========== 角色设定管理 ==========
    const loadCharacters = async () => {
      try {
        const res = await aiListCharacters();
        characters.value = res.data || [];
      } catch (e) {
        console.error('加载角色设定失败', e);
      }
    };

    const saveCharacter = async () => {
      if (!characterForm.name.trim()) {
        ElMessage.warning('请输入设定名');
        return;
      }
      try {
        await aiSaveCharacter({
          name: characterForm.name,
          persona: characterForm.persona,
          style: characterForm.style,
          isDefault: characterForm.isDefault ? 1 : 0
        });
        ElMessage.success('保存成功');
        characterForm.name = '';
        characterForm.persona = '';
        characterForm.style = '';
        characterForm.isDefault = false;
        loadCharacters();
      } catch (e) {
        ElMessage.error('保存失败');
      }
    };

    const deleteCharacter = async (id) => {
      try {
        await aiDeleteCharacter(id);
        ElMessage.success('删除成功');
        loadCharacters();
      } catch (e) {
        ElMessage.error('删除失败');
      }
    };

    // ========== 工具函数 ==========
    const formatText = (text) => {
      if (!text) return '';
      return text.replace(/\n/g, '<br>').replace(/ /g, '&nbsp;');
    };

    const copyText = (text) => {
      navigator.clipboard.writeText(text).then(() => {
        ElMessage.success('已复制到剪贴板');
      });
    };

    const insertToEditor = (text) => {
      // 可以通过事件总线或 localStorage 传递给编辑器
      localStorage.setItem('ai_insert_text', text);
      ElMessage.success('内容已保存，请在编辑器中粘贴');
    };

    const useQuickTip = (tip) => {
      inputText.value = tip;
    };

    const formatTime = (timeStr) => {
      if (!timeStr) return '';
      const date = new Date(timeStr);
      return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`;
    };

    // ========== 初始化 ==========
    onMounted(() => {
      loadSessions();
      loadCharacters();
    });

    return {
      chatMode,
      inputText,
      contextText,
      targetLength,
      messages,
      isStreaming,
      streamingText,
      currentSessionId,
      sessions,
      characters,
      selectedCharacterId,
      showCharacterDialog,
      characterForm,
      quickTips,
      inputPlaceholder,
      chatAreaRef,
      sendMessage,
      stopStream,
      handleEnter,
      newSession,
      switchSession,
      loadSessions,
      deleteSession,
      saveCharacter,
      deleteCharacter,
      formatText,
      copyText,
      insertToEditor,
      useQuickTip,
      formatTime
    };
  }
};
</script>

<style scoped>
.ai-writer-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 200px);
  position: relative;
}

.ai-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #e0e0e0;
  margin-bottom: 10px;
}

.context-area {
  margin-bottom: 10px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
}

.context-tools {
  margin-top: 8px;
}

.chat-area {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  background: #fafafa;
  border-radius: 4px;
  margin-bottom: 10px;
}

.empty-tip {
  text-align: center;
  padding: 40px 0;
}

.quick-prompts {
  margin-top: 20px;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px;
}

.quick-tag {
  cursor: pointer;
}

.message {
  display: flex;
  margin-bottom: 16px;
  gap: 10px;
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar .el-avatar {
  background: #409eff;
}

.message-avatar .el-avatar.user {
  background: #67c23a;
}

.message-content {
  max-width: 70%;
  padding: 10px 14px;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.message.user .message-content {
  background: #e3f2fd;
}

.message-text {
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
}

.message-actions {
  margin-top: 6px;
  display: flex;
  gap: 12px;
}

.cursor {
  animation: blink 1s infinite;
  color: #409eff;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

.input-area {
  border-top: 1px solid #e0e0e0;
  padding-top: 10px;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 8px;
}

.session-sidebar {
  position: absolute;
  right: -220px;
  top: 0;
  width: 200px;
  height: 100%;
  background: #fff;
  border-left: 1px solid #e0e0e0;
  padding: 10px;
  overflow-y: auto;
}

.session-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid #eee;
}

.session-item {
  padding: 8px;
  border-radius: 4px;
  cursor: pointer;
  margin-bottom: 6px;
  position: relative;
}

.session-item:hover {
  background: #f0f0f0;
}

.session-item.active {
  background: #e3f2fd;
}

.session-title {
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-time {
  font-size: 11px;
  color: #999;
  margin-top: 2px;
}

.session-delete {
  position: absolute;
  right: 4px;
  top: 50%;
  transform: translateY(-50%);
  display: none;
  color: #f56c6c;
}

.session-item:hover .session-delete {
  display: block;
}

.character-list {
  max-height: 200px;
  overflow-y: auto;
}

.character-item {
  padding: 10px;
  border-bottom: 1px solid #eee;
}

.character-desc {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
