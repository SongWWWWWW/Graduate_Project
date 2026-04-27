import axios from 'axios'
const ADMIN_TOKEN_KEY = 'admin_token'

const adminApi = axios.create({
  baseURL: process.env.VUE_APP_BASE_API_URL,
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
})

adminApi.interceptors.request.use(config => {
  const token = localStorage.getItem(ADMIN_TOKEN_KEY)
  if (token) config.headers['Authorization'] = token
  return config
})

adminApi.interceptors.response.use(res => {
  if (typeof res.data !== 'object') {
    return Promise.reject(res)
  }
  if (res.data.code !== '00000') {
    if (res.data.code === 'A0230' || res.data.code === 'A0301') {
      localStorage.removeItem(ADMIN_TOKEN_KEY)
      window.location.href = '/#/admin/login'
    }
    return Promise.reject(res.data)
  }
  return res.data
}, err => {
  return Promise.reject(err)
})

export const adminLogin = (data) => adminApi.post('/admin/login', data)
export const adminProfile = () => adminApi.get('/admin/profile')
export const adminListUsers = (params) => adminApi.get('/admin/user/list', { params })
export const adminUpdateUserStatus = (id, status) => adminApi.put(`/admin/user/${id}/status?status=${status}`)
export const adminListAuthors = (params) => adminApi.get('/admin/user/author/list', { params })
export const adminUpdateAuthorStatus = (id, status) => adminApi.put(`/admin/user/author/${id}/status?status=${status}`)
export const adminListBooks = (params) => adminApi.get('/admin/book/list', { params })
export const adminUpdateBookStatus = (id, status) => adminApi.put(`/admin/book/${id}/status?status=${status}`)
export const adminDeleteBook = (id) => adminApi.delete(`/admin/book/${id}`)
export const adminListChapters = (bookId, params) => adminApi.get(`/admin/book/${bookId}/chapters`, { params })
export const adminDeleteChapter = (id) => adminApi.delete(`/admin/book/chapter/${id}`)
export const adminListComments = (params) => adminApi.get('/admin/comment/list', { params })
export const adminDeleteComment = (id) => adminApi.delete(`/admin/comment/${id}`)
export const adminAuditComment = (id, status) => adminApi.put(`/admin/comment/${id}/audit?auditStatus=${status}`)
export const adminListAuditLogs = (params) => adminApi.get('/admin/audit/log/list', { params })
export const adminGetAuditLogDetail = (id) => adminApi.get(`/admin/audit/log/${id}`)
export const adminUpdateAuditLogStatus = (id, status, remark) => adminApi.put(`/admin/audit/log/${id}/status?status=${status}${remark ? '&remark=' + encodeURIComponent(remark) : ''}`)
export const adminListSensitiveWords = () => adminApi.get('/admin/audit/sensitive/list')
export const adminAddSensitiveWord = (data) => adminApi.post('/admin/audit/sensitive', data)
export const adminDeleteSensitiveWord = (id) => adminApi.delete(`/admin/audit/sensitive/${id}`)
export const adminGetStats = () => adminApi.get('/admin/stats/dashboard')

// EPUB 导入
export const adminPreviewEpub = (formData) => adminApi.post('/admin/import/epub/preview', formData, {
  headers: { 'Content-Type': 'multipart/form-data' }
})
export const adminImportEpub = (formData) => adminApi.post('/admin/import/epub/confirm', formData, {
  headers: { 'Content-Type': 'multipart/form-data' }
})

export const adminQuickCreateAuthor = (data) => adminApi.post('/admin/user/author/quick-create', null, { params: data })

export { ADMIN_TOKEN_KEY }
export default adminApi
