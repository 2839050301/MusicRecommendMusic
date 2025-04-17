<template>
  <div class="home-container">
    <NavBar />
    <div class="content">
      <div class="banner">
        <h1>发现好音乐</h1>
        <p>根据您的喜好推荐个性化音乐</p>
      </div>
      
      <div class="section">
        <h2 class="section-title">热门推荐</h2>
        <div class="song-grid">
          <div 
            v-for="(song, i) in songs" 
            :key="i" 
            class="song-card"
            @click="playSong(i)"
          >
            <div class="song-cover">
              <img 
                :src="song.url || require('@/assets/default-music-cover.jpg')" 
                alt="歌曲封面"
                @error="handleImageError"
              >
              <div class="play-icon">
                <i class="bi bi-play-fill"></i>
              </div>
            </div>
            <div class="song-info">
              <h3>{{ song.sname || `歌曲名称 ${i+1}` }}</h3>
              <p>{{ song.singerName || '歌手名称' }}</p>
              <div class="song-meta">
                <span class="hot">
                  <i class="bi bi-fire"></i> 热度: {{ song.hot || 100 }}
                </span>
                <button 
                  @click.stop="toggleLike(song.songId, true)" 
                  class="like-btn"
                >
                  <i class="bi bi-heart"></i> 喜欢
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import NavBar from '@/components/NavBar.vue'

export default {
  components: { NavBar },
  data() {
    return {
      songs: [] // Initialize empty array for songs
    }
  },
  methods: {
    playSong(id) {
      console.log('播放歌曲:', id)
    },
    handleImageError(e) {
      e.target.src = require('@/assets/default-music-cover.jpg');
    },
    async toggleLike(songId, isLike) {
      try {
        const userId = localStorage.getItem('userId') || 1;
        const url = isLike ? 'http://localhost:8088/rest/songs/like' : 'http://localhost:8088/rest/songs/unlike';
        const response = await axios.post(url, {
          userId: userId,
          songId: songId
        }, {
          headers: {
            'Content-Type': 'application/json'
          }
        });
  
        if (response.data?.success) {
          this.$message.success(isLike ? '已添加到喜欢列表' : '已取消喜欢');
        } else {
          throw new Error(response.data?.message || '操作失败');
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
.home-container {
  background: #f8f9fa;
  min-height: 100vh;
}

.content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.banner {
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  color: white;
  padding: 60px 20px;
  border-radius: 10px;
  margin-bottom: 30px;
  text-align: center;
}

.banner h1 {
  font-size: 36px;
  margin-bottom: 10px;
}

.banner p {
  font-size: 18px;
  opacity: 0.9;
}

.section {
  margin-bottom: 40px;
}

.section-title {
  font-size: 24px;
  margin-bottom: 20px;
  color: var(--text-dark);
  position: relative;
  padding-bottom: 10px;
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 60px;
  height: 3px;
  background: var(--primary-color);
}

.song-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.song-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.song-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0,0,0,0.1);
}

.song-cover {
  position: relative;
  padding-top: 100%;
  background: #eee;
}

.song-cover img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.play-icon {
  position: absolute;
  bottom: 10px;
  right: 10px;
  width: 36px;
  height: 36px;
  background: rgba(255,255,255,0.9);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all 0.3s;
}

.song-card:hover .play-icon {
  opacity: 1;
}

.play-icon i {
  color: var(--primary-color);
  font-size: 16px;
  margin-left: 2px;
}

.song-info {
  padding: 15px;
}

.song-info h3 {
  font-size: 16px;
  margin-bottom: 5px;
  color: var(--text-dark);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.song-info p {
  font-size: 14px;
  color: var(--text-light);
  margin-bottom: 10px;
}

.song-meta {
  display: flex;
  font-size: 12px;
  color: var(--text-light);
}

.song-meta span {
  display: flex;
  align-items: center;
  margin-right: 10px;
}

.song-meta i {
  margin-right: 3px;
  font-size: 14px;
}
.like-btn {
  background: none;
  border: 1px solid #ff4d4f;
  color: #ff4d4f;
  padding: 5px 10px;
  border-radius: 4px;
  cursor: pointer;
  margin-left: 10px;
}

.like-btn:hover {
  background: #fff2f0;
}

.bi-heart {
  color: #ff4d4f;
}

/* 原有样式保持不变 */


</style>
