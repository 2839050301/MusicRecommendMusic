import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Login from '../components/Login.vue'
import Register from '../components/Register.vue'
import Charts from '../views/Charts.vue'
import Singers from '../views/Singers.vue'  // Add this import
import SingerDetail from '../views/SingerDetail.vue'
import MyMusic from '../views/MyMusic.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: { requiresAuth: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  // 新增charts路由
  {
    path: '/charts',
    name: 'Charts',
    component: Charts,
    meta: { requiresAuth: true }
  },
  {
    path: '/singers',
    name: 'Singers',
    component: Singers,
    meta: { requiresAuth: true }
  },
  {
    path: '/singer/:id',
    name: 'SingerDetail',
    component: () => import('../views/SingerDetail.vue')
  },
  {
    path: '/my-music',
    name: 'MyMusic',
    component: MyMusic,
    meta: { requiresAuth: true }
  },
  // 在路由配置中添加
  // 修改路由配置，确保正确加载Search组件
  {
    path: '/search',
    name: 'Search',
    component: () => import('@/views/Search.vue')  // 使用@别名确保路径正确
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const isAuthenticated = !!localStorage.getItem('token')
  
  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login')
  } else {
    next()
  }
})

export default router