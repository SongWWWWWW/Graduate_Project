<template>
  <AuthorHeader v-show="!isFullscreen" />
  <div class="main box_center cf" :class="{ 'fs-main': isFullscreen }">
    <div class="userBox cf" :class="{ 'fs-userBox': isFullscreen }">
      <AuthorSidebar v-show="!isFullscreen" active="bookList" />
      <div class="my_r chapter-write-page" :class="{ 'fullscreen-mode': isFullscreen }">
        <!-- 页面标题栏 -->
        <div class="write-header">
          <div class="header-left">
            <router-link
              v-if="!isFullscreen"
              :to="{ name: 'authorChapterList', query: { id: chapter.bookId } }"
              class="back-link"
            >
              <el-icon><ArrowLeft /></el-icon>
              返回章节列表
            </router-link>
            <el-button v-else link size="small" @click="toggleFullscreen">
              <el-icon><FullScreen /></el-icon>
              退出全屏
            </el-button>
            <h2>编辑章节</h2>
          </div>
          <div class="header-actions">
            <el-switch
              v-model="chapter.isVip"
              :active-value="1"
              :inactive-value="0"
              active-text="收费"
              inactive-text="免费"
              inline-prompt
              style="margin-right: 16px"
            />
            <el-button type="primary" size="large" @click="updateBookChapter" :loading="saving">
              <el-icon><Check /></el-icon>
              保存修改
            </el-button>
          </div>
        </div>

        <!-- 章节名输入 -->
        <div class="chapter-title-row">
          <el-input
            v-model="chapter.chapterName"
            placeholder="请输入章节标题"
            size="large"
            class="chapter-title-input"
          >
            <template #prefix>
              <el-icon><Document /></el-icon>
            </template>
          </el-input>
          <span class="word-count">{{ chapter.chapterContent?.length || 0 }} 字</span>
        </div>

        <!-- 写作统计条 -->
        <div class="writing-stats-bar">
          <div class="stat-item">
            <el-icon><Timer /></el-icon>
            <span>写作时长 {{ formatTime(writingTime) }}</span>
          </div>
          <div class="stat-item">
            <el-icon><TrendCharts /></el-icon>
            <span>今日累计 {{ dailyWordCount }} 字</span>
          </div>
          <div v-if="autoSaveStatus" class="stat-item auto-save">
            <el-icon><CircleCheck /></el-icon>
            <span>{{ autoSaveStatus }}</span>
          </div>
          <div class="stat-item actions">
            <el-button link size="small" @click="toggleFullscreen">
              <el-icon><FullScreen /></el-icon>
              {{ isFullscreen ? '退出全屏' : '全屏模式' }}
            </el-button>
          </div>
        </div>

        <!-- 编辑器 + AI 助手区域 -->
        <div class="editor-layout" :class="{ 'ai-expanded': aiPanelOpen }">
          <!-- 编辑器 -->
          <div class="editor-main">
            <textarea
              ref="editorRef"
              v-model="chapter.chapterContent"
              class="chapter-editor"
              placeholder="在此开始创作...&#10;&#10;选中文字可使用 AI 快捷功能。&#10;在右侧 AI 助手中输入提示，获取创作灵感。"
              @mouseup="checkSelection"
              @keyup="checkSelection"
            />

            <!-- 选中文字的浮动 AI 工具栏 -->
            <transition name="fade">
              <div v-if="hasSelection && !aiPanelOpen" class="floating-ai-bar">
                <el-tooltip content="AI 润色" placement="top">
                  <el-button type="primary" size="small" circle @click="quickAI('polish')">
                    <el-icon><MagicStick /></el-icon>
                  </el-button>
                </el-tooltip>
                <el-tooltip content="AI 扩写" placement="top">
                  <el-button type="success" size="small" circle @click="showParamDialog('expand')">
                    <el-icon><ZoomIn /></el-icon>
                  </el-button>
                </el-tooltip>
                <el-tooltip content="AI 缩写" placement="top">
                  <el-button type="warning" size="small" circle @click="showParamDialog('condense')">
                    <el-icon><ZoomOut /></el-icon>
                  </el-button>
                </el-tooltip>
                <el-tooltip content="AI 续写" placement="top">
                  <el-button type="danger" size="small" circle @click="showParamDialog('continue')">
                    <el-icon><Right /></el-icon>
                  </el-button>
                </el-tooltip>
              </div>
            </transition>

            <!-- 编辑器底部工具栏 -->
            <div class="editor-toolbar">
              <el-button type="primary" size="small" :loading="isStreaming" @click="aiContinue">
                <el-icon><Lightning /></el-icon>
                {{ isStreaming ? 'AI 续写中...' : '✨ AI 智能续写' }}
              </el-button>
              <span class="toolbar-hint">遇到卡文？让 AI 帮你续写 500 字</span>
            </div>
          </div>

          <!-- AI 助手侧边栏 -->
          <div class="ai-sidebar" v-show="aiPanelOpen">
            <div class="ai-sidebar-header">
              <div class="ai-title">
                <el-icon class="ai-icon"><Cpu /></el-icon>
                <span>AI 助手</span>
              </div>
              <el-button link size="small" @click="aiPanelOpen = false">
                <el-icon><Close /></el-icon>
              </el-button>
            </div>

            <!-- AI 快捷操作 -->
            <div class="ai-quick-actions">
              <el-button
                v-for="btn in aiButtons"
                :key="btn.action"
                :type="btn.type"
                size="small"
                :disabled="isStreaming"
                @click="btn.action === 'polish' ? quickAI('polish') : showParamDialog(btn.action)"
              >
                <el-icon v-if="btn.icon"><component :is="btn.icon" /></el-icon>
                {{ btn.label }}
              </el-button>
            </div>

            <!-- AI 对话区域 -->
            <div class="ai-chat-area" ref="aiChatRef">
              <div v-if="aiMessages.length === 0" class="ai-empty">
                <el-icon :size="48" color="#667eea"><Cpu /></el-icon>
                <h4>🤖 AI 创作助手</h4>
                <p>选中文字即可使用润色/扩写/缩写<br>或输入提示让 AI 帮你创作</p>
                <div class="ai-tips">
                  <el-tag
                    v-for="tip in quickTips"
                    :key="tip"
                    size="small"
                    class="ai-tip-tag"
                    @click="useTip(tip)"
                  >
                    {{ tip }}
                  </el-tag>
                </div>
              </div>
              <div
                v-for="(msg, idx) in aiMessages"
                :key="idx"
                :class="['ai-message', msg.role]"
              >
                <div class="ai-message-content" v-html="formatMsg(msg.content)"></div>
                <div v-if="msg.role === 'assistant'" class="ai-message-actions">
                  <el-link type="primary" :underline="false" @click="copyText(msg.content)">
                    <el-icon><CopyDocument /></el-icon> 复制
                  </el-link>
                  <el-link type="primary" :underline="false" @click="insertText(msg.content)">
                    <el-icon><Bottom /></el-icon> 插入
                  </el-link>
                  <el-link type="primary" :underline="false" @click="replaceSelection(msg.content)">
                    <el-icon><RefreshRight /></el-icon> 替换
                  </el-link>
                </div>
              </div>
              <div v-if="isStreaming" class="ai-message assistant streaming">
                <div class="ai-message-content">
                  {{ streamingText }}<span class="cursor">▎</span>
                </div>
              </div>
            </div>

            <!-- AI 输入 -->
            <div class="ai-input-area">
              <el-input
                v-model="aiInput"
                type="textarea"
                :rows="2"
                :placeholder="aiPlaceholder"
                @keydown.enter.prevent="sendAI"
              />
              <div class="ai-input-actions">
                <el-button v-if="isStreaming" type="danger" size="small" @click="stopStream">
                  <el-icon><VideoPause /></el-icon> 停止
                </el-button>
                <el-button v-else type="primary" size="small" :disabled="!aiInput.trim()" @click="sendAI">
                  <el-icon><Promotion /></el-icon> 发送
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 展开 AI 面板按钮 -->
        <el-button
          v-if="!aiPanelOpen"
          class="ai-toggle-btn"
          type="primary"
          circle
          size="large"
          @click="aiPanelOpen = true"
        >
          <el-icon><Cpu /></el-icon>
        </el-button>
      </div>
    </div>
  </div>

  <!-- 参数设置弹窗 -->
  <el-dialog v-model="paramDialogVisible" :title="paramDialogTitle" width="360px">
    <div v-if="currentAction === 'expand' || currentAction === 'condense'">
      <p style="margin-bottom: 8px; color: var(--text-secondary);">
        {{ currentAction === 'expand' ? '扩写比例' : '缩写比例' }}
      </p>
      <el-slider v-model="ratio" :min="10" :max="200" :step="10" show-stops />
      <div style="text-align: center; color: var(--primary); font-weight: 600;">{{ ratio }}%</div>
    </div>
    <div v-if="currentAction === 'continue'">
      <p style="margin-bottom: 8px; color: var(--text-secondary);">续写字数</p>
      <el-slider v-model="length" :min="100" :max="2000" :step="100" show-stops />
      <div style="text-align: center; color: var(--primary); font-weight: 600;">{{ length }} 字</div>
    </div>
    <template #footer>
      <el-button @click="paramDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="confirmAIAction">确定</el-button>
    </template>
  </el-dialog>
</template>

<script>
import {
  ArrowLeft,
  Check,
  Document,
  MagicStick,
  ZoomIn,
  ZoomOut,
  Right,
  Cpu,
  Close,
  ChatLineRound,
  CopyDocument,
  Bottom,
  RefreshRight,
  VideoPause,
  Promotion,
  Timer,
  TrendCharts,
  CircleCheck,
  FullScreen,
  Lightning,
} from "@element-plus/icons-vue";
import { reactive, toRefs, ref, computed, nextTick, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import { updateChapter, getChapter, aiStreamChat } from "@/api/author";
import AuthorHeader from "@/components/author/Header.vue";
import AuthorSidebar from "@/components/author/Sidebar.vue";

export default {
  name: "authorChapterUpdate",
  components: {
    AuthorHeader,
    AuthorSidebar,
    ArrowLeft,
    Check,
    Document,
    MagicStick,
    ZoomIn,
    ZoomOut,
    Right,
    Cpu,
    Close,
    ChatLineRound,
    CopyDocument,
    Bottom,
    RefreshRight,
    VideoPause,
    Promotion,
    Timer,
    TrendCharts,
    CircleCheck,
    FullScreen,
    Lightning,
  },
  setup() {
    const route = useRoute();
    const router = useRouter();
    const editorRef = ref(null);
    const aiChatRef = ref(null);

    const state = reactive({
      chapterId: route.query.id,
      chapter: { chapterName: "", chapterContent: "", isVip: 0, bookId: null },
      saving: false,
      hasSelection: false,
      selectedText: "",
      selectionStart: 0,
      selectionEnd: 0,
      // generating 已合并到 isStreaming

      // 写作统计
      writingTime: 0, // 秒
      writingTimer: null,
      autoSaveStatus: "", // 自动保存状态提示
      isFullscreen: false,
      dailyWordCount: 0,

      // AI 面板
      aiPanelOpen: true,
      aiInput: "",
      aiMessages: [],
      isStreaming: false,
      streamingText: "",
      abortController: null,

      // 参数弹窗
      paramDialogVisible: false,
      currentAction: "",
      ratio: 150,
      length: 500,

      aiButtons: [
        { label: "润色", action: "polish", type: "primary", icon: "MagicStick" },
        { label: "扩写", action: "expand", type: "success", icon: "ZoomIn" },
        { label: "缩写", action: "condense", type: "warning", icon: "ZoomOut" },
        { label: "续写", action: "continue", type: "danger", icon: "Right" },
      ],

      quickTips: [
        "帮我想个章节开头",
        "让这段对话更生动",
        "增加环境描写",
        "设计一个反转",
        "优化节奏感",
      ],
    });

    onMounted(() => {
      load();
      startWritingTimer();
      loadDailyStats();
    });

    const load = async () => {
      const { data } = await getChapter(state.chapterId);
      state.chapter = data;
    };

    // 自动保存相关
    const saveDraft = () => {
      const key = `chapter_draft_${state.chapter.bookId || state.chapterId}`;
      localStorage.setItem(key, JSON.stringify(state.chapter));
      state.autoSaveStatus = `草稿已保存 ${new Date().toLocaleTimeString()}`;
    };

    const clearDraft = () => {
      const key = `chapter_draft_${state.chapter.bookId || state.chapterId}`;
      localStorage.removeItem(key);
    };

    const startWritingTimer = () => {
      if (state.writingTimer) clearInterval(state.writingTimer);
      state.writingTimer = setInterval(() => {
        state.writingTime++;
        if (state.writingTime % 30 === 0) {
          saveDraft();
        }
      }, 1000);
    };

    const loadDailyStats = () => {
      const today = new Date().toDateString();
      const key = `daily_words_${today}`;
      const saved = localStorage.getItem(key);
      state.dailyWordCount = saved ? parseInt(saved) : 0;
    };

    const updateDailyStats = (wordCount) => {
      const today = new Date().toDateString();
      const key = `daily_words_${today}`;
      const current = parseInt(localStorage.getItem(key) || "0");
      localStorage.setItem(key, String(current + wordCount));
    };

    const formatTime = (seconds) => {
      const h = Math.floor(seconds / 3600);
      const m = Math.floor((seconds % 3600) / 60);
      const s = seconds % 60;
      if (h > 0) return `${h}时${m}分`;
      return `${m}分${s}秒`;
    };

    const toggleFullscreen = () => {
      state.isFullscreen = !state.isFullscreen;
    };

    const aiContinue = async () => {
      if (state.isStreaming) return;
      if (!state.chapter.chapterContent.trim()) {
        ElMessage.warning("请先输入一些内容，AI 才能续写");
        return;
      }
      state.currentAction = "continue";
      state.length = 500;
      await quickAI("continue");
    };

    const paramDialogTitle = computed(() => {
      const map = { expand: "扩写设置", condense: "缩写设置", continue: "续写设置" };
      return map[state.currentAction] || "";
    });

    const aiPlaceholder = computed(() => {
      return state.selectedText
        ? "已选中文字，可直接发送或输入补充要求..."
        : "输入提示，如：帮我想一个紧张的开场...";
    });

    // 检查选中文本
    const checkSelection = () => {
      const textarea = editorRef.value;
      if (!textarea) return;
      state.selectionStart = textarea.selectionStart;
      state.selectionEnd = textarea.selectionEnd;
      state.hasSelection = state.selectionStart !== state.selectionEnd;
      if (state.hasSelection) {
        state.selectedText = textarea.value.substring(state.selectionStart, state.selectionEnd);
      }
    };

    // 保存章节
    const updateBookChapter = async () => {
      if (!state.chapter.chapterName.trim()) {
        ElMessage.error("章节名不能为空！");
        return;
      }
      if (!state.chapter.chapterContent.trim()) {
        ElMessage.error("章节内容不能为空！");
        return;
      }
      if (state.chapter.chapterContent.length < 50) {
        ElMessage.error("章节内容至少 50 字！");
        return;
      }
      state.saving = true;
      try {
        await updateChapter(state.chapterId, state.chapter);
        clearDraft();
        updateDailyStats(state.chapter.chapterContent.length);
        ElMessage.success("保存成功！");
        router.push({ name: "authorChapterList", query: { id: state.chapter.bookId } });
      } catch (e) {
        ElMessage.error("保存失败：" + e.message);
      } finally {
        state.saving = false;
      }
    };

    // ========== AI 功能 ==========

    const showParamDialog = (action) => {
      state.currentAction = action;
      if (action === "polish") {
        quickAI("polish");
      } else {
        state.paramDialogVisible = true;
      }
    };

    const confirmAIAction = () => {
      state.paramDialogVisible = false;
      quickAI(state.currentAction);
    };

    const quickAI = async (action) => {
      if (state.isStreaming) return;

      let text = state.selectedText;
      if (!text && action !== "continue") {
        text = state.chapter.chapterContent;
      }
      if (!text && action !== "continue") {
        ElMessage.warning("请先选中文字或输入内容");
        return;
      }

      // 构建 prompt
      let prompt = "";
      if (action === "continue") {
        prompt = `请根据以下内容续写小说，续写约${state.length}字：\n\n${state.chapter.chapterContent}`;
      } else if (action === "expand") {
        prompt = `请将以下内容扩写至原来的${state.ratio}%，保持原意不变，丰富细节和描写：\n\n${text}`;
      } else if (action === "condense") {
        prompt = `请将以下内容缩写至原来的${state.ratio}%，保留核心情节和关键信息：\n\n${text}`;
      } else if (action === "polish") {
        prompt = `请对以下内容进行润色，提升文笔和可读性：\n\n${text}`;
      }

      const sessionTypeMap = {
        polish: 3,
        continue: 1,
        expand: 1,
        condense: 1,
      };

      state.aiMessages.push({
        role: "user",
        content: `【${getActionLabel(action)}】${text ? text.substring(0, 50) + (text.length > 50 ? "..." : "") : ""}`,
      });
      state.aiPanelOpen = true;
      state.isStreaming = true;
      state.streamingText = "";
      scrollAIChat();

      try {
        state.abortController = new AbortController();
        const response = await aiStreamChat({
          prompt,
          sessionType: sessionTypeMap[action] || 0,
          context: action === "continue" ? state.chapter.chapterContent : text,
          targetLength: action === "continue" ? state.length : null,
          bookId: state.chapter.bookId,
        }, state.abortController.signal);

        await handleStream(response);
        state.aiMessages.push({ role: "assistant", content: state.streamingText });
      } catch (err) {
        if (err.name !== "AbortError") {
          ElMessage.error("AI 生成失败：" + err.message);
        }
      } finally {
        state.isStreaming = false;
        state.streamingText = "";
        state.abortController = null;
        scrollAIChat();
      }
    };

    const getActionLabel = (action) => {
      const map = {
        expand: `扩写(${state.ratio}%)`,
        condense: `缩写(${state.ratio}%)`,
        continue: `续写(${state.length}字)`,
        polish: "润色",
      };
      return map[action] || action;
    };

    // 公共 SSE 流处理函数
    const handleStream = async (response) => {
      if (!response.ok) throw new Error("网络请求失败");
      const contentType = response.headers.get("content-type") || "";
      // 如果返回的是 JSON 错误（非 SSE），解析并抛出
      if (!contentType.includes("text/event-stream")) {
        const text = await response.text();
        try {
          const json = JSON.parse(text);
          if (json.code && json.code !== "00000") {
            throw new Error(json.message || "AI 服务异常");
          }
        } catch (e) {
          if (e.message.includes("AI 服务") || e.message.includes("登录")) throw e;
        }
        throw new Error("AI 响应格式异常，请稍后重试");
      }
      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let done = false;
      while (!done) {
        const { value, done: streamDone } = await reader.read();
        done = streamDone;
        if (value) {
          const chunk = decoder.decode(value, { stream: true });
          const lines = chunk.split("\n");
          for (const line of lines) {
            if (line.startsWith("data:")) {
              const data = line.substring(5).trim();
              if (data === "[DONE]") {
                done = true;
              } else {
                state.streamingText += data;
                scrollAIChat();
              }
            }
          }
        }
      }
    };

    // AI 对话（流式）
    const sendAI = async () => {
      const text = state.aiInput.trim();
      if (!text || state.isStreaming) return;

      let fullPrompt = text;
      if (state.selectedText) {
        fullPrompt = `以下是我的小说内容片段，请帮我处理：\n\n${state.selectedText}\n\n我的要求：${text}`;
      } else if (state.chapter.chapterContent) {
        fullPrompt = `以下是我当前章节的内容，请帮我：\n\n${state.chapter.chapterContent.substring(0, 500)}...\n\n我的要求：${text}`;
      }

      state.aiMessages.push({ role: "user", content: text });
      state.aiInput = "";
      state.isStreaming = true;
      state.streamingText = "";
      state.aiPanelOpen = true;
      scrollAIChat();

      try {
        state.abortController = new AbortController();
        const response = await aiStreamChat({
          prompt: fullPrompt,
          sessionType: 0,
          bookId: state.chapter.bookId,
        }, state.abortController.signal);

        await handleStream(response);

        state.aiMessages.push({ role: "assistant", content: state.streamingText });
      } catch (err) {
        if (err.name !== "AbortError") {
          ElMessage.error("AI 响应失败：" + err.message);
        }
      } finally {
        state.isStreaming = false;
        state.streamingText = "";
        state.abortController = null;
        scrollAIChat();
      }
    };

    const stopStream = () => {
      if (state.abortController) state.abortController.abort();
    };

    const scrollAIChat = () => {
      nextTick(() => {
        if (aiChatRef.value) {
          aiChatRef.value.scrollTop = aiChatRef.value.scrollHeight;
        }
      });
    };

    // 文本操作
    const insertText = (text) => {
      const textarea = editorRef.value;
      if (!textarea) return;
      const pos = textarea.selectionStart;
      const before = state.chapter.chapterContent.substring(0, pos);
      const after = state.chapter.chapterContent.substring(pos);
      state.chapter.chapterContent = before + text + after;
      ElMessage.success("已插入到光标位置");
    };

    const replaceSelection = (text) => {
      if (!state.hasSelection) {
        insertText(text);
        return;
      }
      const before = state.chapter.chapterContent.substring(0, state.selectionStart);
      const after = state.chapter.chapterContent.substring(state.selectionEnd);
      state.chapter.chapterContent = before + text + after;
      state.hasSelection = false;
      state.selectedText = "";
      ElMessage.success("已替换选中文本");
    };

    const copyText = (text) => {
      navigator.clipboard.writeText(text).then(() => ElMessage.success("已复制"));
    };

    const useTip = (tip) => {
      state.aiInput = tip;
    };

    const formatMsg = (text) => {
      if (!text) return "";
      return text.replace(/\n/g, "<br>").replace(/ /g, "&nbsp;");
    };

    return {
      ...toRefs(state),
      editorRef,
      aiChatRef,
      paramDialogTitle,
      aiPlaceholder,
      updateBookChapter,
      checkSelection,
      showParamDialog,
      confirmAIAction,
      quickAI,
      sendAI,
      stopStream,
      insertText,
      replaceSelection,
      copyText,
      useTip,
      formatMsg,
      saveDraft,
      clearDraft,
      startWritingTimer,
      loadDailyStats,
      updateDailyStats,
      formatTime,
      toggleFullscreen,
      aiContinue,
    };
  },
};
</script>

<style scoped>
.chapter-write-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding-bottom: 0 !important;
}

/* 全屏模式 */
.fs-main {
  width: 100% !important;
  padding: 0 !important;
  margin: 0 !important;
}
.fs-userBox {
  width: 100% !important;
  padding: 0 !important;
  margin: 0 !important;
  border-radius: 0 !important;
}
.chapter-write-page.fullscreen-mode {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw !important;
  height: 100vh !important;
  z-index: 999;
  background: #fff;
  padding: 16px 24px !important;
  margin: 0 !important;
  border: none !important;
  box-sizing: border-box;
}
.fullscreen-mode .editor-layout {
  gap: 20px;
}
.fullscreen-mode .chapter-editor {
  font-size: 18px;
  line-height: 2;
  padding: 24px 28px;
}
.fullscreen-mode .ai-sidebar {
  width: 360px;
}

.write-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--divider-color);
  margin-bottom: 16px;
  flex-shrink: 0;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.back-link {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: var(--text-muted);
  text-decoration: none;
}
.back-link:hover {
  color: var(--primary);
}
.header-left h2 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}
.header-actions {
  display: flex;
  align-items: center;
}

.chapter-title-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
  flex-shrink: 0;
}
.chapter-title-input {
  flex: 1;
}
:deep(.chapter-title-input .el-input__wrapper) {
  border-radius: var(--radius-sm);
  font-size: 16px;
}
.word-count {
  font-size: 14px;
  color: var(--text-muted);
  font-weight: 500;
  white-space: nowrap;
}

/* 写作统计条 */
.writing-stats-bar {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 8px 16px;
  background: linear-gradient(135deg, #f8f9fc, #f0f2f8);
  border-radius: var(--radius-sm);
  margin-bottom: 12px;
  flex-shrink: 0;
  font-size: 13px;
}
.writing-stats-bar .stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--text-secondary);
}
.writing-stats-bar .stat-item.auto-save {
  color: #67c23a;
}
.writing-stats-bar .stat-item.actions {
  margin-left: auto;
}

/* 编辑器底部工具栏 */
.editor-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  background: #f8f9fc;
  border-top: 1px solid var(--border-color);
  border-radius: 0 0 var(--radius-lg) var(--radius-lg);
  flex-shrink: 0;
}
.editor-toolbar .toolbar-hint {
  font-size: 12px;
  color: var(--text-muted);
}

.editor-layout {
  display: flex;
  flex: 1;
  gap: 16px;
  min-height: 0;
  overflow: hidden;
}
.editor-main {
  flex: 1;
  position: relative;
  display: flex;
  flex-direction: column;
  min-width: 0;
  z-index: 1;
}
.chapter-editor {
  flex: 1;
  width: 100%;
  padding: 20px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  font-size: 16px;
  line-height: 1.8;
  resize: none;
  outline: none;
  font-family: "Microsoft YaHei", "PingFang SC", sans-serif;
  transition: border-color 0.3s, box-shadow 0.3s;
  background: #fff;
}
.chapter-editor:focus {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px var(--primary-light);
}

.floating-ai-bar {
  position: absolute;
  bottom: 60px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 8px;
  padding: 8px 12px;
  background: #fff;
  border-radius: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  border: 1px solid rgba(102, 126, 234, 0.15);
}

.ai-sidebar {
  width: 320px;
  flex-shrink: 0;
  background: #fff;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  z-index: 100;
  position: relative;
}
.ai-sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid var(--divider-color);
}
.ai-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: var(--text-primary);
}
.ai-icon {
  color: var(--primary);
}

.ai-quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 12px 16px;
  border-bottom: 1px solid var(--divider-color);
}

.ai-chat-area {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px;
  min-height: 0;
}
.ai-empty {
  text-align: center;
  padding: 30px 0;
  color: var(--text-muted);
}
.ai-empty p {
  margin: 12px 0;
  font-size: 13px;
  line-height: 1.6;
}
.ai-tips {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
}
.ai-tip-tag {
  cursor: pointer;
}
.ai-tip-tag:hover {
  color: var(--primary);
  border-color: var(--primary);
}

.ai-message {
  margin-bottom: 12px;
}
.ai-message.user {
  text-align: right;
}
.ai-message-content {
  display: inline-block;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 13px;
  line-height: 1.6;
  max-width: 100%;
  word-break: break-all;
}
.ai-message.user .ai-message-content {
  background: var(--primary-light);
  color: var(--primary);
  text-align: left;
}
.ai-message.assistant .ai-message-content {
  background: #f5f7fa;
  color: var(--text-secondary);
}
.ai-message.assistant.streaming .ai-message-content {
  background: #f0f7ff;
}
.cursor {
  animation: blink 1s infinite;
  color: var(--primary);
}
@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

.ai-message-actions {
  display: flex;
  gap: 12px;
  margin-top: 6px;
  padding-left: 4px;
}
.ai-message-actions .el-link {
  font-size: 12px;
}

.ai-input-area {
  padding: 12px 16px;
  border-top: 1px solid var(--divider-color);
}
.ai-input-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}

.ai-toggle-btn {
  position: fixed;
  right: 32px;
  bottom: 32px;
  z-index: 2000;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.5);
}
.ai-toggle-btn:hover {
  transform: scale(1.1);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(10px);
}

@media (max-width: 900px) {
  .editor-layout.ai-expanded .editor-main {
    display: none;
  }
  .ai-sidebar {
    width: 100%;
  }
}
</style>
