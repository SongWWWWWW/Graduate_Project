<template>
  <div :class="`read_style_${readTheme}`" class="read-page-wrapper">
    <div class="header">
      <Top />
    </div>
    <div id="showDetail">
      <div class="readBody cf">
        <div class="readMain cf">
          <!-- 左侧固定悬浮菜单 -->
          <div class="read_menu">
            <div class="menu_left" :style="menuStyle">
              <ul>
                <li>
                  <a
                    class="ico_catalog"
                    @click="chapterList(data.chapterInfo.bookId)"
                    href="javascript:void(0)"
                    title="目录"
                  >
                    <b>目录</b>
                  </a>
                </li>
                <li>
                  <a
                    class="ico_page"
                    @click="bookDetail(data.chapterInfo.bookId)"
                    href="javascript:void(0)"
                    title="返回书页"
                  >
                    <b>书页</b>
                  </a>
                </li>
                <li>
                  <a
                    class="ico_setup"
                    @click="showSetup = true"
                    href="javascript:void(0)"
                    title="设置"
                  >
                    <b>设置</b>
                  </a>
                </li>
              </ul>
            </div>
          </div>

          <div class="readWrap">
            <div class="bookNav"></div>
            <div id="readcontent">
              <div
                class="textbox cf"
                :style="textboxStyle"
              >
                <div class="book_title">
                  <h1 v-if="data.chapterInfo">
                    {{ data.chapterInfo.chapterName }}
                  </h1>
                  <div class="textinfo" v-if="data.chapterInfo">
                    类别：{{ data.bookInfo.categoryName }} 作者：<a
                      href="javascript:void(0)"
                      v-if="data.bookInfo"
                    >{{ data.bookInfo.authorName }}</a
                    ><span v-if="data.chapterInfo"
                      >字数：{{ data.chapterInfo.chapterWordCount }}</span
                    ><span v-if="data.chapterInfo"
                      >更新时间：{{ data.chapterInfo.chapterUpdateTime }}</span
                    >
                  </div>
                </div>

                <div class="txtwrap">
                  <div
                    id="showReading"
                    class="readBox"
                    :style="readBoxStyle"
                    v-html="formattedContent"
                  ></div>
                </div>
              </div>
            </div>

            <!-- 底部固定翻页栏 -->
            <div class="nextPageBox" :style="nextPageStyle">
              <a
                class="prev"
                href="javascript:void(0)"
                @click="preChapter(data.chapterInfo.bookId)"
                >上一章</a
              >
              <a
                class="dir"
                @click="chapterList(data.chapterInfo.bookId)"
                href="javascript:void(0)"
                >目录</a
              >
              <a
                class="next"
                @click="nextChapter(data.chapterInfo.bookId)"
                href="javascript:void(0)"
                >下一章</a
              >
            </div>

            <!-- 章节评论 start -->
            <div class="chapter-comment-wrap" v-if="data.chapterInfo">
              <div class="comment-header">
                <h3>章节评论</h3>
                <span class="comment-count">({{ chapterComments.commentTotal || 0 }}条)</span>
              </div>
              <div v-if="chapterComments.comments?.length > 0" class="comment-list">
                <div class="comment-item" v-for="item in chapterComments.comments" :key="item.id">
                  <div class="comment-user">
                    <img :src="item.commentUserPhoto || '/default-avatar.png'" class="comment-avatar" />
                    <span class="comment-username">{{ item.commentUser }}</span>
                  </div>
                  <div class="comment-body">
                    <div class="comment-text">{{ item.commentContent }}</div>
                    <div class="comment-time">{{ item.commentTime }}</div>
                  </div>
                </div>
              </div>
              <div v-else class="no-comment">暂无评论，来发表第一条评论吧~</div>
              <div class="comment-form">
                <el-input
                  v-model="commentContent"
                  type="textarea"
                  :rows="2"
                  placeholder="发表评论（10-512字）"
                  maxlength="512"
                  show-word-limit
                />
                <el-button type="primary" size="small" @click="submitComment" :disabled="!commentContent.trim()">
                  发表评论
                </el-button>
              </div>
            </div>
            <!-- 章节评论 end -->
          </div>
        </div>
      </div>

      <!-- 设置弹窗 -->
      <div class="maskBox" v-show="showSetup" @click="showSetup = false"></div>
      <div class="readPopup setupBox" v-show="showSetup">
        <a class="closePopup" href="javascript:void(0);" @click="showSetup = false"></a>
        <div class="popupTit">
          <h3>阅读设置</h3>
        </div>
        <div class="setupList">
          <ul>
            <li class="readTheme">
              <em class="tit">阅读主题：</em>
              <a
                v-for="t in themes"
                :key="t.id"
                :class="[t.className, { current: readTheme === t.id }]"
                href="javascript:void(0);"
                :title="t.name"
                @click="setTheme(t.id)"
              ></a>
            </li>
            <li class="setFont setBtn">
              <em class="tit">正文字体：</em>
              <a
                :class="['setYahei', { current: readFont === 0 }]"
                href="javascript:void(0);"
                @click="setFont(0)"
                >雅黑</a
              >
              <a
                :class="['setSimsun', { current: readFont === 1 }]"
                href="javascript:void(0);"
                @click="setFont(1)"
                >宋体</a
              >
              <a
                :class="['setKs', { current: readFont === 2 }]"
                href="javascript:void(0);"
                @click="setFont(2)"
                >楷书</a
              >
            </li>
            <li class="fontSize setBtn">
              <em class="tit">字体大小：</em>
              <a class="small" href="javascript:void(0);" @click="changeFontSize(-2)">A-</a>
              <span class="current_font">{{ readFontSize }}</span>
              <a class="big" href="javascript:void(0);" @click="changeFontSize(2)">A+</a>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import "@/assets/styles/book.css";
import "@/assets/styles/read.css";
import { reactive, toRefs, computed, onMounted, onBeforeUnmount } from "vue";
import { useRouter, useRoute } from "vue-router";
import { getBookContent, getPreChapterId, getNextChapterId, listNewestComments } from "@/api/book";
import { comment } from "@/api/user";
import { ElMessage } from "element-plus";
import Top from "@/components/common/Top";

const STORAGE_KEY = "novel_reader_settings";

const defaultSettings = {
  theme: 1,
  font: 0,
  fontSize: 18,
};

function loadSettings() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (raw) return { ...defaultSettings, ...JSON.parse(raw) };
  } catch (e) {
    console.warn("读取阅读设置失败", e);
  }
  return { ...defaultSettings };
}

function saveSettings(settings) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(settings));
  } catch (e) {
    console.warn("保存阅读设置失败", e);
  }
}

const themes = [
  { id: 1, name: "白色", className: "white" },
  { id: 2, name: "绿色", className: "green" },
  { id: 3, name: "粉色", className: "pink" },
  { id: 4, name: "黄色", className: "yellow" },
  { id: 5, name: "灰色", className: "gray" },
  { id: 6, name: "夜间", className: "night" },
];

const fontFamilies = [
  '"Microsoft YaHei", "PingFang SC", "Noto Sans CJK SC", sans-serif',
  '"SimSun", "Songti SC", "STSong", serif',
  '"KaiTi", "Kaiti SC", "STKaiti", serif',
];

export default {
  name: "bookContent",
  components: { Top },
  setup() {
    const route = useRoute();
    const router = useRouter();
    const settings = loadSettings();

    const state = reactive({
      data: {},
      imgBaseUrl: process.env.VUE_APP_BASE_IMG_URL,
      showSetup: false,
      readTheme: settings.theme,
      readFont: settings.font,
      readFontSize: settings.fontSize,
      themes,
      chapterComments: {},
      commentContent: '',
    });

    const currentFontFamily = computed(() => fontFamilies[state.readFont] || fontFamilies[0]);

    const menuBgColors = {
      1: "rgba(255, 255, 255, 0.55)",
      2: "rgba(224, 235, 223, 0.7)",
      3: "rgba(244, 229, 229, 0.7)",
      4: "rgba(236, 226, 200, 0.7)",
      5: "rgba(229, 229, 229, 0.7)",
      6: "rgba(50, 50, 50, 0.6)",
    };

    const textboxBgColors = {
      1: "rgba(255, 255, 255, 0.55)",
      2: "rgba(255, 255, 255, 0.4)",
      3: "rgba(255, 255, 255, 0.4)",
      4: "rgba(255, 255, 255, 0.4)",
      5: "rgba(255, 255, 255, 0.4)",
      6: "rgba(255, 255, 255, 0.1)",
    };

    const menuStyle = computed(() => ({
      backgroundColor: menuBgColors[state.readTheme] || menuBgColors[1],
    }));

    const textboxStyle = computed(() => ({
      backgroundColor: textboxBgColors[state.readTheme] || textboxBgColors[1],
    }));

    const readBoxStyle = computed(() => ({
      fontSize: state.readFontSize + "px",
      fontFamily: currentFontFamily.value,
    }));

    const nextPageBgColors = {
      1: "rgba(255, 255, 255, 0.55)",
      2: "rgba(224, 235, 223, 0.85)",
      3: "rgba(244, 229, 229, 0.85)",
      4: "rgba(236, 226, 200, 0.85)",
      5: "rgba(229, 229, 229, 0.85)",
      6: "rgba(40, 40, 40, 0.9)",
    };

    const nextPageTextColors = {
      6: "#bbb",
    };

    const nextPageBorderColors = {
      1: "rgba(0,0,0,0.1)",
      2: "rgba(0,0,0,0.1)",
      3: "rgba(0,0,0,0.1)",
      4: "rgba(0,0,0,0.1)",
      5: "rgba(0,0,0,0.1)",
      6: "rgba(255,255,255,0.1)",
    };

    const nextPageStyle = computed(() => ({
      backgroundColor: nextPageBgColors[state.readTheme] || nextPageBgColors[1],
      color: nextPageTextColors[state.readTheme] || "#333",
      borderTop: `1px solid ${nextPageBorderColors[state.readTheme] || "rgba(0,0,0,0.1)"}`,
    }));

    const formattedContent = computed(() => {
      const content = state.data.bookContent;
      if (!content) return "";
      const paragraphs = content.split(/\n\s*\n/).filter((p) => p.trim().length > 0);
      return paragraphs.map((p) => `<p>${escapeHtml(p.trim())}</p>`).join("");
    });

    const escapeHtml = (text) => {
      if (!text) return "";
      return text
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
    };

    const persistSettings = () => {
      saveSettings({
        theme: state.readTheme,
        font: state.readFont,
        fontSize: state.readFontSize,
      });
    };

    const setTheme = (id) => {
      state.readTheme = id;
      persistSettings();
    };

    const setFont = (index) => {
      state.readFont = index;
      persistSettings();
    };

    const changeFontSize = (delta) => {
      const next = state.readFontSize + delta;
      if (next >= 12 && next <= 32) {
        state.readFontSize = next;
        persistSettings();
      }
    };

    onMounted(() => {
      init(route.params.chapterId);
      keyDown();
    });

    onBeforeUnmount(() => {
      document.onkeydown = null;
    });

    const bookDetail = (bookId) => {
      router.push({ path: `/book/${bookId}` });
    };

    const chapterList = (bookId) => {
      router.push({ path: `/chapter_list/${bookId}` });
    };

    const preChapter = async (bookId) => {
      const { data } = await getPreChapterId(route.params.chapterId);
      if (data) {
        router.push({ path: `/book/${bookId}/${data}` });
        init(data);
      } else {
        ElMessage.warning("已经是第一章了！");
      }
    };

    const nextChapter = async (bookId) => {
      const { data } = await getNextChapterId(route.params.chapterId);
      if (data) {
        router.push({ path: `/book/${bookId}/${data}` });
        init(data);
      } else {
        ElMessage.warning("已经是最后一章了！");
      }
    };

    const init = async (chapterId) => {
      const { data } = await getBookContent(chapterId);
      state.data = data;
      window.scrollTo({ top: 0, behavior: "smooth" });
      // 加载章节评论
      if (data.chapterInfo?.bookId) {
        loadChapterComments(data.chapterInfo.bookId, chapterId);
      }
    };

    const loadChapterComments = async (bookId, chapterId) => {
      try {
        const res = await listNewestComments({ bookId, chapterId });
        state.chapterComments = res.data || {};
      } catch (e) {
        state.chapterComments = {};
      }
    };

    const submitComment = async () => {
      const text = state.commentContent.trim();
      if (!text) {
        ElMessage.error('评论内容不能为空');
        return;
      }
      if (text.length < 10) {
        ElMessage.error('评论不能少于10个字');
        return;
      }
      try {
        await comment({
          bookId: state.data.chapterInfo.bookId,
          chapterId: state.data.chapterInfo.id,
          commentContent: text,
        });
        state.commentContent = '';
        ElMessage.success('评论发表成功');
        loadChapterComments(state.data.chapterInfo.bookId, state.data.chapterInfo.id);
      } catch (e) {
        ElMessage.error(e.message || '评论发表失败');
      }
    };

    const keyDown = () => {
      document.onkeydown = (e) => {
        let e1 = e || event || window.event;
        const bookId = state.data.chapterInfo?.bookId;
        if (!bookId) return;
        if (e1 && e1.keyCode == 37) {
          preChapter(bookId);
        } else if (e1 && e1.keyCode == 39) {
          nextChapter(bookId);
        }
      };
    };

    return {
      ...toRefs(state),
      formattedContent,
      menuStyle,
      textboxStyle,
      readBoxStyle,
      nextPageStyle,
      currentFontFamily,
      setTheme,
      setFont,
      changeFontSize,
      bookDetail,
      chapterList,
      preChapter,
      nextChapter,
      submitComment,
    };
  },
};
</script>

<style scoped>
@charset "utf-8";

.read-page-wrapper {
  min-height: 100vh;
}

a {
  color: #333;
}
a:hover,
.redFont,
.current,
.bookNav a:hover,
.textinfo a:hover {
  color: #f70;
}

/* 阅读页背景 */
body {
  color: #333;
  font-family: "Microsoft YaHei";
}
.topMain {
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  filter: progid:DXImageTransform.Microsoft.gradient(startColorstr=#66ffffff,endColorstr=#66ffffff);
  background: none;
  background: rgba(255, 255, 255, 0.4);
}
.read_style_6 .topMain {
  border-bottom: 1px solid #444;
  filter: progid:DXImageTransform.Microsoft.gradient(startColorstr=#0cffffff,endColorstr=#0cffffff);
  background: rgba(255, 255, 255, 0.05);
}

/*颜色：浅黄白、护眼绿、粉色、浅黄、浅灰、夜间黑*/
.read_style_1 {
  background-color: #f4f1ea;
}
.read_style_2 {
  background-color: #cbdec9;
}
.read_style_3 {
  background-color: #edd4d4;
}
.read_style_4 {
  background-color: #e0cfa3;
}
.read_style_5 {
  background-color: #d3d3d3;
}
.read_style_6 {
  background-color: #0e0f0f;
}

.read_style_1 .textbox {
  background-color: rgba(255, 255, 255, 0.55);
}
.read_style_2 .textbox,
.read_style_3 .textbox,
.read_style_4 .textbox,
.read_style_5 .textbox {
  background-color: rgba(255, 255, 255, 0.4);
}
.read_style_6 .textbox {
  background-color: rgba(255, 255, 255, 0.1);
}

.read_style_6 .book_title h1,
.read_style_6 .bookNav,
.read_style_6 .bookNav a,
.read_style_6 .textinfo,
.read_style_6 .textinfo a,
.read_style_6 .textinfo span,
.read_style_6 .read_menu li a b {
  color: #999;
  box-shadow: none;
}
.read_style_6 .bookNav {
  border-color: #444 !important;
}

.readBody {
  height: 100%;
}
.readMain {
  margin: 0 auto;
  position: relative;
  z-index: 3;
  width: 900px;
}

/* ========== 左侧固定悬浮菜单 ========== */
.menu_left {
  width: 60px;
  z-index: 999;
  position: fixed;
  top: 100px;
  left: 50%;
  margin-left: -540px;
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
.read_menu li {
  box-shadow: 0 0 1px 0 rgba(0, 0, 0, 0.05);
  margin-bottom: 1px;
  width: 60px;
  position: relative;
}
.read_menu li a {
  display: block;
  width: 60px;
  height: 60px;
  position: relative;
  opacity: 0.95;
  transition: opacity 0.2s;
}
.read_menu li a b {
  font-weight: 400;
  display: block;
  height: 60px;
  width: 60px;
  text-align: center;
  line-height: 90px;
  color: rgba(0, 0, 0, 0.5);
  font-size: 12px;
}
.menu_left li a:hover {
  opacity: 1;
}
.menu_left li a.ico_catalog {
  background-position: -60px -10px;
}
.menu_left li a.ico_page {
  background-position: 2px -10px;
}
.menu_left li a.ico_setup {
  background-position: -122px -10px;
}

/* ========== 正文栏 ========== */
.textbox {
  border-radius: 6px;
  width: 98%;
  margin: 0 auto 20px;
  padding-bottom: 40px;
  box-shadow: 0 0 1px 0 rgba(0, 0, 0, 0.25);
  color: #111;
}
.bookNav {
  width: 99%;
  margin: 0 auto;
  padding: 18px 0 12px;
  line-height: 2.5;
}
.bookNav a {
  font: 12px/1 "Microsoft YaHei";
  margin: 0 5px;
}
.readWrap {
  margin: 0 auto;
  width: 100%;
  padding-bottom: 100px; /* 给底部固定按钮留空间 */
}
.book_title {
  width: 90%;
  margin: 0 auto;
  padding-bottom: 15px;
  position: relative;
}
.book_title h1 {
  padding: 60px 0 30px;
  font: 26px/1 "Microsoft YaHei";
  color: #000;
  text-align: center;
}
.textinfo {
  color: rgba(0, 0, 0, 0.5);
  font: 12px/1.8 "Microsoft YaHei";
  text-align: center;
  position: relative;
}
.textinfo a,
.textinfo span {
  color: rgba(0, 0, 0, 0.5);
  margin-right: 15px;
  display: inline-block;
  vertical-align: middle;
  margin-top: -3px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.readBox {
  width: 90%;
  max-width: 720px;
  margin: 0 auto;
  font-size: 18px;
  line-height: 1.8;
  padding: 20px 0 60px;
  color: #333;
  font-family: "Microsoft YaHei", "PingFang SC", "Noto Sans CJK SC", sans-serif;
}
.readBox :deep(p) {
  margin: 0 0 1.2em 0 !important;
  text-indent: 2em !important;
  line-height: 1.8 !important;
  text-align: justify !important;
  word-wrap: break-word !important;
  word-break: break-word !important;
}
.readBox :deep(p:first-child) {
  margin-top: 0 !important;
}

/* ========== 底部固定翻页栏 ========== */
.nextPageBox {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 998;
  margin: 0 auto;
  text-align: center;
  width: 900px;
  padding: 12px 0;
  display: flex;
  justify-content: center;
  gap: 20px;
  border-radius: 6px 6px 0 0;
}
.nextPageBox a {
  width: 26%;
  height: 48px;
  line-height: 48px;
  font-size: 16px;
  display: inline-block;
  border-radius: 4px;
  text-align: center;
  opacity: 0.95;
  border: 1px solid rgba(0, 0, 0, 0.1);
  transition: all 0.2s ease;
  text-decoration: none;
}
.nextPageBox a:hover {
  opacity: 1;
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}
.read_style_6 .nextPageBox a {
  color: #bbb;
  border-color: rgba(255, 255, 255, 0.1);
}
.read_style_6 .nextPageBox a:hover {
  color: #fff;
}

/* ========== 设置弹窗 ========== */
.maskBox {
  position: fixed;
  left: 0;
  top: 0;
  z-index: 995;
  width: 100%;
  height: 100%;
  background: black;
  filter: alpha(opacity=20);
  opacity: 0.2;
}
.readPopup {
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  background: #fff;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  padding-bottom: 20px;
  z-index: 9999;
  position: fixed;
  left: 50%;
  top: 50%;
}
.closePopup {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 20px;
  height: 20px;
  background: url(../assets/images/icon_readpage.png) -43px -126px no-repeat;
  cursor: pointer;
  opacity: 0.6;
  transition: opacity 0.2s;
}
.closePopup:hover {
  opacity: 1;
}
.popupTit h3 {
  font-size: 16px;
  margin: 15px 20px;
  font-weight: 600;
}

.setupBox {
  width: 480px;
  margin-left: -240px;
  margin-top: -130px;
}
.setupList {
  padding: 5px 40px;
}
.setupList li {
  font-size: 14px;
  padding: 15px 0;
}
.setupList li a {
  display: inline-block;
  vertical-align: middle;
  margin: 0 6px;
  text-align: center;
  cursor: pointer;
}

.readTheme a {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  position: relative;
  border: 2px solid transparent;
  transition: all 0.2s;
}
.readTheme .white {
  background-color: #faf6ed;
  margin-left: 15px;
}
.readTheme .green {
  background-color: #e2efe2;
}
.readTheme .pink {
  background-color: #fdd9d9;
}
.readTheme .yellow {
  background-color: #f1debd;
}
.readTheme .gray {
  background-color: #eee;
}
.readTheme .night {
  background-color: #666;
}
.readTheme a.current {
  border-color: #f80;
  box-shadow: 0 0 0 2px rgba(255, 136, 0, 0.2);
}
.readTheme a:hover {
  transform: scale(1.1);
}

.setBtn a {
  border: 1px solid #d9d9d9;
  width: 53px;
  height: 28px;
  line-height: 28px;
  box-shadow: 0 1px 1px #ececec;
  border-radius: 3px;
  cursor: pointer;
  transition: all 0.2s;
}
.setBtn a.current {
  color: #cc2931;
  border-color: #cc2931;
  background: rgba(204, 41, 49, 0.05);
}
.setFont .setSimsun {
  font-family: SimSun;
  font-size: 13px;
}
.setFont .setKs {
  font-family: KaiTi;
  font-size: 15px;
}

.setupList li.fontSize a {
  text-align: center;
  margin: 0;
  font-size: 16px;
  width: 70px;
  box-shadow: 0 1px 0 #ececec;
  cursor: pointer;
  transition: all 0.2s;
}
.setupList li.fontSize a.small {
  margin-left: 8px;
  border-right: none;
  border-radius: 3px 0 0 3px;
}
.setupList li.fontSize a.big {
  border-left: none;
  border-radius: 0 3px 3px 0;
}
.setupList li.fontSize a:hover {
  background: #f5f5f5;
}
.setupList li.fontSize .current_font {
  display: inline-block;
  padding: 0 22px;
  border: 1px solid #d9d9d9;
  height: 28px;
  line-height: 28px;
  box-shadow: 0 1px 1px #ececec;
  vertical-align: middle;
  font-weight: 600;
  min-width: 30px;
  text-align: center;
}

/* 夜间模式弹窗适配 */
.read_style_6 .setupBox {
  background: #2a2a2a;
  border-color: #444;
}
.read_style_6 .popupTit h3 {
  color: #ccc;
}
.read_style_6 .setupList li {
  color: #aaa;
}
.read_style_6 .setupList li.fontSize .current_font {
  background: #333;
  border-color: #555;
  color: #ccc;
}
.read_style_6 .setBtn a {
  background: #333;
  border-color: #555;
  color: #aaa;
}
.read_style_6 .setBtn a.current {
  color: #f80;
  border-color: #f80;
}
.read_style_6 .closePopup {
  filter: invert(0.8);
}

/* 响应式：小屏幕下调整侧边栏位置 */
@media screen and (max-width: 1100px) {
  .menu_left {
    margin-left: -460px;
  }
  .nextPageBox {
    width: 100%;
    left: 0;
    right: 0;
    margin-left: 0;
  }
  .readMain {
    width: 100%;
    padding: 0 20px;
    box-sizing: border-box;
  }
}

@media screen and (max-width: 900px) {
  .menu_left {
    position: fixed;
    top: auto;
    bottom: 80px;
    left: 10px;
    margin-left: 0;
    width: 44px;
  }
  .read_menu li,
  .read_menu li a,
  .read_menu li a b {
    width: 44px;
    height: 44px;
  }
  .read_menu li a b {
    line-height: 66px;
    font-size: 10px;
  }
  .book_title h1 {
    font-size: 20px;
    padding: 30px 0 20px;
  }
  .readBox {
    font-size: 16px;
  }
  .nextPageBox a {
    font-size: 14px;
    height: 40px;
    line-height: 40px;
  }
}

/* 章节评论 */
.chapter-comment-wrap {
  max-width: 800px;
  margin: 30px auto 60px;
  padding: 0 20px;
}
.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e8e8e8;
}
.comment-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}
.comment-count {
  color: #999;
  font-size: 14px;
}
.comment-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.comment-item {
  display: flex;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}
.comment-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}
.comment-user {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  min-width: 50px;
}
.comment-username {
  font-size: 12px;
  color: #666;
}
.comment-body {
  flex: 1;
}
.comment-text {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  margin-bottom: 6px;
}
.comment-time {
  font-size: 12px;
  color: #999;
}
.no-comment {
  text-align: center;
  padding: 30px 0;
  color: #999;
  font-size: 14px;
}
.comment-form {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.comment-form .el-button {
  align-self: flex-end;
}
</style>
