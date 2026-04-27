import request from '../utils/request'

export function getAuthorStatus() {
    return request.get('/author/status');
}

export function register(params) {
    return request.post('/author/register', params);
}

export function listBooks(params) {
    return request.get('/author/books', { params });
}

export function publishBook(params) {
    return request.post('/author/book', params);
}

export function listChapters(bookId, params) {
    return request.get(`/author/book/chapters/${bookId}`, { params });
}

export function publishChapter(bookId,params) {
    return request.post(`/author/book/chapter/${bookId}`, params);
}

// ==================== AI API（SSE 流式 + 记忆）====================

/**
 * AI 流式对话（SSE）
 * 返回 ReadableStream，需要在前端用 reader 读取
 */
export function aiStreamChat(params, signal) {
    const token = localStorage.getItem('Authorization') || '';
    return fetch(`${process.env.VUE_APP_BASE_API_URL}/author/ai/chat/stream`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': token
        },
        body: JSON.stringify(params),
        signal
    });
}

/**
 * AI 生成小说大纲
 */
export function aiGenerateOutline(theme) {
    return request.post(`/author/ai/outline?theme=${encodeURIComponent(theme)}`);
}

/**
 * 保存/更新角色设定
 */
export function aiSaveCharacter(params) {
    return request.post('/author/ai/character', params);
}

/**
 * 查询角色设定列表
 */
export function aiListCharacters(bookId) {
    return request.get('/author/ai/characters', { params: { bookId } });
}

/**
 * 删除角色设定
 */
export function aiDeleteCharacter(characterId) {
    return request.delete(`/author/ai/character/${characterId}`);
}

/**
 * 查询 AI 会话列表
 */
export function aiListSessions(sessionType) {
    return request.get('/author/ai/sessions', { params: { sessionType } });
}

/**
 * 删除 AI 会话
 */
export function aiDeleteSession(sessionId) {
    return request.delete(`/author/ai/session/${sessionId}`);
}

export function deleteChapter(id) {
    return request.delete(`/author/book/chapter/${id}`);
}

export function getChapter(id) {
    return request.get(`/author/book/chapter/${id}`);
}

export function updateChapter(id,params) {
    return request.put(`/author/book/chapter/${id}`,params);
}

// ==================== 作家工作台 ====================
export function getDashboard() {
    return request.get('/author/dashboard');
}

// ==================== 作品统计 ====================
export function listBookStats(params) {
    return request.get('/author/book/stats', { params });
}

// ==================== 收益管理 ====================
export function listIncome(params) {
    return request.get('/author/income', { params });
}

export function listIncomeDetail(bookId, params) {
    return request.get('/author/income/detail', { params: { bookId, ...params } });
}

// ==================== 互动管理 ====================
export function listAuthorComments(bookId, params) {
    return request.get('/author/book/comments', { params: { bookId, ...params } });
}

// ==================== 作家资料 ====================
export function getAuthorProfile() {
    return request.get('/author/profile');
}

export function updateAuthorProfile(params) {
    return request.put('/author/profile', params);
}