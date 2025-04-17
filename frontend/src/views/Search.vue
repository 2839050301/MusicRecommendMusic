<template>
  <div class="search-container">
    <NavBar />
    <div class="content">
      <h2>搜索结果: "{{ $route.query.q }}"</h2>
      
      <div v-if="loading" class="loading">搜索中...</div>
      <div v-else-if="results.length === 0" class="empty">
        <i class="bi bi-search"></i>
        <p>没有找到相关歌曲</p>
      </div>
      <div v-else class="song-grid">
        <div v-for="song in results" :key="song.songId" class="song-card">
          <div class="song-cover">
            <img :src="song.url || require('@/assets/default-music-cover.jpg')">
          </div>
          <div class="song-info">
            <h3>{{ song.sname_song || song.sname }}</h3>
            <p>{{ song.singerName || '未知歌手' }}</p>
            <button 
              @click.stop="toggleLike(song.songId)"
              class="like-btn"
              :class="{ 'liked': likedSongIds.includes(song.songId) }"
            >
              <i class="bi" :class="likedSongIds.includes(song.songId) ? 'bi-heart-fill' : 'bi-heart'"></i>
              {{ likedSongIds.includes(song.songId) ? '已喜欢' : '喜欢' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import NavBar from '@/components/NavBar.vue'
import axios from 'axios'

export default {
  components: {NavBar},
  data() {
    return {
      results: [],
      loading: false,
      likedSongIds: [] // 确保已定义
    }
  },
  created() {
    this.fetchLikedSongs(); // 新增：获取用户喜欢的歌曲
    this.searchSongs()
  },
  watch: {
    '$route.query.q': {
      handler() {
        this.searchSongs()
      },
      immediate: true
    }
  },
  methods: {
    async fetchLikedSongs() {
      try {
        const userId = localStorage.getItem('userId') || 1;
        const response = await axios.get(
          'http://localhost:8088/rest/songs/user/liked-songs',
          { params: { userId } }
        );
        if (response.data?.success) {
          this.likedSongIds = response.data.User_like?.map(item => item.songId) || [];
        }
      } catch (error) {
        console.error('获取喜欢列表失败:', error);
      }
    },
    async searchSongs() {
      this.loading = true
      try {
        const query = decodeURIComponent(this.$route.query.q || '')
        const response = await axios.get('http://localhost:8088/rest/songs/fuzzy', {
          params: { query }
        })
        
        if (response.data?.success) {
          this.results = response.data.songs || []
        } else {
          throw new Error(response.data?.message || '搜索失败')
        }
      } catch (error) {
        console.error('搜索失败:', error)
        this.$message.error(error.message || '搜索失败')
      } finally {
        this.loading = false
      }
    },
    async toggleLike(songId) {
      try {
        const isLike = !this.likedSongIds.includes(songId);
        const userId = localStorage.getItem('userId') || 1;
        
        const response = await axios({
          method: 'post',
          url: `http://localhost:8088/rest/songs/${isLike ? 'like' : 'unlike'}`,
          params: {userId, songId},
          headers: {'Content-Type': 'application/json'}
        });

        if (response.data?.success) {
          if (isLike) {
            this.likedSongIds.push(songId);
          } else {
            this.likedSongIds = this.likedSongIds.filter(id => id !== songId);
          }
          this.$message.success(isLike ? '已添加到喜欢列表' : '已取消喜欢');
        }
      } catch (error) {
        console.error('操作失败:', error);
        this.$message.error(error.message || '操作失败');
      }
    }
  }
}
</script>

<style scoped>
.search-container {
  background: #f8f9fa;
  min-height: 100vh;
}

/* 新增歌曲卡片样式 */
.song-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 20px;
  padding: 20px;
}

.song-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: transform 0.3s ease;
}

.song-card:hover {
  transform: translateY(-5px);
}

.song-cover {
  width: 100%;
  height: 160px; /* 调整封面高度 */
  overflow: hidden;
}

.song-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.song-info {
  padding: 12px;
}

.song-info h3 {
  margin: 0 0 4px;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.song-info p {
  margin: 0;
  font-size: 12px;
  color: #666;
}
</style>