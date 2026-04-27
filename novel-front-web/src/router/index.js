import { createRouter, createWebHashHistory } from 'vue-router'

const router = createRouter({
  // createWebHistory 路由模式路径不带#号(生产环境下不能直接访问项目，需要 nginx 转发)
  // createWebHashHistory 路由模式路径带#号
  history: createWebHashHistory(), 
  routes: [
    {
      path: '/',
      redirect: '/home'
    },
    {
      path: '/home',
      name: 'home',
      component: () => import('@/views/Home')
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/Register')
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/Login')
    },
    {
      path: '/feadback',
      name: 'feadback',
      component: () => import('@/views/FeadBack')
    },
    {
      path: '/news/:id',
      name: 'news',
      component: () => import('@/views/News')
    },
    {
      path: '/bookclass',
      name: 'bookclass',
      component: () => import('@/views/BookClass')
    },
    {
      path: '/book_rank',
      name: 'bookRank',
      component: () => import('@/views/BookRank')
    },
    {
      path: '/book/:id',
      name: 'book',
      component: () => import('@/views/Book')
	   
    },
    {
      path: '/chapter_list/:bookId',
      name: 'chapterList',
      component: () => import('@/views/ChapterList')
	   
    },
    {
      path: '/book/:id/:chapterId',
      name: 'bookContent',
      component: () => import('@/views/BookContent')
	   
    },
    {
      path: '/user/setup',
      name: 'userSetup',
      component: () => import('@/views/UserSetup')
    },
    {
      path: '/user/comment',
      name: 'userComment',
      component: () => import('@/views/UserComment')
	   
    },
    {
      path: '/author/register',
      name: 'authorRegister',
      component: () => import('@/views/author/Register')
    },
    {
      path: '/author/book_list',
      name: 'authorBookList',
      component: () => import('@/views/author/BookList')
    },
    {
      path: '/author/book_add',
      name: 'authorBookAdd',
      component: () => import('@/views/author/BookAdd')
    },
    {
      path: '/author/chapter_list',
      name: 'authorChapterList',
      component: () => import('@/views/author/ChapterList')
    },
    {
      path: '/author/chapter_add',
      name: 'authorChapterAdd',
      component: () => import('@/views/author/ChapterAdd')
    },
    {
      path: '/author/chapter_update',
      name: 'authorChapterUpdate',
      component: () => import('@/views/author/ChapterUpdate')
    },
    {
      path: '/author/ai_writer',
      name: 'authorAiWriter',
      component: () => import('@/views/author/AiWriter')
    },
    {
      path: '/author/dashboard',
      name: 'authorDashboard',
      component: () => import('@/views/author/Dashboard')
    },
    {
      path: '/author/stats',
      name: 'authorStats',
      component: () => import('@/views/author/Stats')
    },
    {
      path: '/author/income',
      name: 'authorIncome',
      component: () => import('@/views/author/Income')
    },
    {
      path: '/author/comments',
      name: 'authorComments',
      component: () => import('@/views/author/Comments')
    },
    {
      path: '/author/profile',
      name: 'authorProfile',
      component: () => import('@/views/author/Profile')
    },
    {
      path: '/admin/login',
      name: 'adminLogin',
      component: () => import('@/views/admin/Login')
    },
    {
      path: '/admin',
      component: () => import('@/views/admin/Layout'),
      redirect: '/admin/dashboard',
      children: [
        { path: 'dashboard', component: () => import('@/views/admin/Dashboard') },
        { path: 'users', component: () => import('@/views/admin/UserManage') },
        { path: 'authors', component: () => import('@/views/admin/AuthorManage') },
        { path: 'books', component: () => import('@/views/admin/BookManage') },
        { path: 'bookImport', component: () => import('@/views/admin/BookImport') },
        { path: 'comments', component: () => import('@/views/admin/CommentManage') },
        { path: 'chapterAudit', component: () => import('@/views/admin/ChapterAudit') },
        { path: 'auditLogs', component: () => import('@/views/admin/AuditLog') },
        { path: 'sensitiveWords', component: () => import('@/views/admin/SensitiveWord') }
      ]
    }
  ]
})

// 解决 vue 中路由跳转时，总是从新页面中间开始显示
router.afterEach((to,from,next) => {
  window.scrollTo(0,0)
})

export default router