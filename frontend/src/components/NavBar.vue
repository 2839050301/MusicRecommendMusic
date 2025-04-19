<template>
  <nav class="navbar">
    <div class="container">
      <router-link to="/" class="logo">
        <i class="bi bi-music-note-beamed"></i>
        <span>音乐推荐</span>
      </router-link>

      <!-- 添加搜索框 -->
      <div class="search-box">
        <input
            v-model="searchQuery"
            @keyup.enter="search"
            placeholder="搜索歌曲"
        >
        <button @click="search">
          <i class="bi bi-search"></i>
        </button>
      </div>

      <div class="nav-links">
        <router-link
            v-for="link in links"
            :key="link.path"
            :to="link.path"
            :class="{ active: $route.path === link.path }"
        >
          <i :class="link.icon"></i>
          {{ link.name }}
        </router-link>
      </div>

      <div class="user-info" v-if="username">
        欢迎，{{ username }}
        <button @click="logout" class="logout-btn">
          <i class="bi bi-box-arrow-right"></i> 退出登录
        </button>
      </div>
    </div>
  </nav>
</template>

<script>
import { useRouter } from 'vue-router';

export default {
  setup() {
    const router = useRouter();
    return { router };
  },
  data() {
    return {
      searchQuery: '',
      username: localStorage.getItem('username') || '',
      links: [
        { path: '/', name: '首页', icon: 'bi bi-house' },
        { path: '/singers', name: '歌手', icon: 'bi bi-person-video3' },
        { path: '/charts', name: '排行榜', icon: 'bi bi-bar-chart' },
        { path: '/my-music', name: '我的音乐', icon: 'bi bi-music-note-list' }
      ]
    }
  },
  methods: {
    logout() {
      localStorage.removeItem('token');
      localStorage.removeItem('username');
      localStorage.removeItem('userId');
      this.$router.push('/login');
    },

    search() {
      if (this.searchQuery.trim()) {
        this.$router.push({
          path: '/search',
          query: { q: encodeURIComponent(this.searchQuery.trim()) }
        })
        this.searchQuery = '' // 清空搜索框
      }
    }
  }
}
</script>

<style scoped>
.navbar {
  background: #fff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 0 20px;
  height: 60px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.container {
  display: flex;
  align-items: center;
  height: 100%;
  max-width: 1200px;
  margin: 0 auto;
}

.logo {
  display: flex;
  align-items: center;
  font-size: 20px;
  font-weight: bold;
  color: #2c3e50;
  text-decoration: none;
  margin-right: 40px;
}

.logo i {
  font-size: 24px;
  margin-right: 8px;
  color: #4CAF50;
}

.nav-links {
  display: flex;
  gap: 30px;
  flex-grow: 1;
}

/* 确保所有链接状态都无下划线 */
.nav-links a,
.nav-links a:hover,
.nav-links a:active,
.nav-links a:visited,
.nav-links a.router-link-exact-active {
  text-decoration: none !important;
}

.nav-links a {
  color: #333; /* 将颜色改为黑色 */
  font-size: 16px;
  transition: color 0.3s;
  display: flex;
  align-items: center;
  gap: 6px;
}

.nav-links a.router-link-exact-active {
  color: #4CAF50; /* 保持激活状态为绿色 */
  font-weight: 500;

  text-decoration: none; /* 这里也明确取消了激活状态的下划线 */
}


/* 搜索框容器样式 */
.search-box {
  display: flex;
  align-items: center;
  margin: 0 30px;
  width: 300px;
  position: relative;
}

/* 输入框样式 */
.search-box input {
  flex: 1;
  padding: 8px 15px 8px 35px;
  border: 1px solid #e0e0e0;
  border-radius: 20px;
  outline: none;
  font-size: 14px;
  transition: all 0.3s;
  background-color: #f5f5f5;
}

/* 输入框聚焦效果 */
.search-box input:focus {
  border-color: #31c27c;
  box-shadow: 0 0 0 2px rgba(49, 194, 124, 0.2);
  background-color: #fff;
}

/* 搜索按钮样式 */
.search-box button {
  position: absolute;
  right: 10px;
  background: #31c27c;
  border: none;
  color: white;
  cursor: pointer;
  transition: all 0.3s;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.search-box button:hover {
  background: #2aad6e;
  transform: scale(1.05);
}

.search-box button:active {
  transform: scale(0.95);
}

.search-box .bi-search {
  font-size: 14px;
  transition: transform 0.3s;
}

.search-box button:hover .bi-search {
  transform: scale(1.1);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
  color: #555;
  font-size: 14px;
}

.logout-btn {
  background: none;
  border: none;
  color: #ff6b6b;
  cursor: pointer;
  font-size: 16px;
  transition: color 0.3s;
}

.logout-btn:hover {
  color: #ff4757;
}
</style>