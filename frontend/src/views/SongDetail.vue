<template>
  <div class="song-detail">
    <NavBar />
    <div class="content">
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="!song" class="empty">歌曲不存在</div>
      <div v-else>
        <div class="song-info">
          <img :src="song.url" :alt="song.sname_song" class="cover">
          <div class="details">
            <h1>{{ song.sname_song }}</h1>
            <p>歌手: {{ song.SingerName }}</p>
            <p>流派: {{ song.GenreName }}</p>
            <p>热度: {{ song.hot }}</p>
            <button @click="toggleLike" :class="{ 'liked': isLiked }">
              <i class="bi" :class="isLiked ? 'bi-heart-fill' : 'bi-heart'"></i>
              {{ isLiked ? '取消喜欢' : '喜欢' }}
            </button>
          </div>
        </div>

        <!-- 修改相关音乐部分 -->
        <div class="recommendations" v-if="recommendations.length > 0">
          <h2>相关音乐</h2>
          <div class="song-list">
            <div class="song-item" 
              v-for="rec in recommendations" 
              :key="rec.songId"
              @click="goToSongDetail(rec.songId)">
              <img :src="rec.url" class="rec-cover" v-if="rec.url">
              <div class="rec-info">
                <h3>{{ rec.sname_song }}</h3>
                <p>{{ rec.SingerName }}</p>
                <button
                  @click.stop="toggleRecLike(rec)"
                  class="like-btn"
                  :class="{ 'liked': rec.isLiked }"
                >
                  <i class="bi" :class="rec.isLiked ? 'bi-heart-fill' : 'bi-heart'"></i>
                  {{ rec.isLiked ? '取消喜欢' : '喜欢' }}
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
import axios from 'axios'

export default {
  components: { NavBar },
  data() {
    return {
      song: null,
      recommendations: [],
      loading: false,
      isLiked: false,
      userId: localStorage.getItem('userId')
    };
  },
  created() {
    this.fetchSongDetail();
  },
  methods: {
    async fetchSongDetail() {
      this.loading = true;
      const songId = this.$route.params.id;
      try {
        // 获取主歌曲信息
        const songRes = await axios.get('http://localhost:8088/rest/songs/es/song', {
          params: { songId }
        });
        
        if (songRes.data.success) {
          this.song = songRes.data.song;
          
          // 获取推荐歌曲并查询ES获取完整信息
          const recRes = await axios.get('http://localhost:8088/rest/songs/content-based', {
            params: { songId, num: 14 }
          });
          
          if (recRes.data.success) {
            // 获取用户喜欢的歌曲列表
            let likedSongs = [];
            if (this.userId) {
              const likeRes = await axios.get('http://localhost:8088/rest/songs/user/liked-songs', {
                params: { userId: this.userId }
              });
              likedSongs = likeRes.data.User_like || [];
              this.isLiked = likedSongs.some(like => like.songId == songId);
            }

            const recPromises = recRes.data.recommendations.map(async rec => {
              const res = await axios.get('http://localhost:8088/rest/songs/es/song', {
                params: { songId: rec.songId }
              });
              const songData = res.data.success ? { ...rec, ...res.data.song } : rec;
              // 初始化喜欢状态
              songData.isLiked = likedSongs.some(like => like.songId == songData.songId);
              return songData;
            });
            
            this.recommendations = await Promise.all(recPromises);
          }
        }
      } catch (error) {
        console.error('获取歌曲详情失败:', error);
      } finally {
        this.loading = false;
      }
    },
    goToSongDetail(songId) {
      this.$router.push(`/song/${songId}`);
    },
    async toggleLike() {
      if (!this.userId) {
        alert('请先登录');
        return;
      }

      try {
        const method = this.isLiked ? 'unlike' : 'like';
        const response = await axios.post(
          `http://localhost:8088/rest/songs/${method}`,
          null,
          {
            params: {
              userId: this.userId,
              songId: this.$route.params.id
            }
          }
        );

        if (response.data.success) {
          this.isLiked = !this.isLiked;
          this.$message.success({
            message: response.data.message,
            duration: 300 // 设置为1.5秒
          });
        }
      } catch (error) {
        console.error('操作失败:', error);
      }
    },


    async toggleRecLike(rec) {
      if (!this.userId) {
        alert('请先登录');
        return;
      }

      try {
        const method = rec.isLiked ? 'unlike' : 'like';
        const response = await axios.post(
          `http://localhost:8088/rest/songs/${method}`,
          null,
          {
            params: {
              userId: this.userId,
              songId: rec.songId
            }
          }
        );

        if (response.data.success) {
          rec.isLiked = !rec.isLiked;
          this.$message.success({
            message: response.data.message,
            duration: 300  // 从1500ms改为500ms
          });
          
          if (this.song && this.song.songId === rec.songId) {
            this.isLiked = rec.isLiked;
          }
        }
      } catch (error) {
        console.error('操作失败:', error);
      }
    },

  },
  watch: {
    '$route.params.id': {
      handler(newId) {
        if (newId) {
          this.fetchSongDetail();
        } else {
          console.error('未获取到歌曲ID');
          this.$message.error('未获取到歌曲ID');
        }
      },
      immediate: true
    }
  }
};
</script>

<style scoped>
/* 修改封面样式 */
.cover {
  width: 240px;  /* 调整封面宽度 */
  height: 240px; /* 调整封面高度 */
  border-radius: 12px;
  object-fit: cover;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

/* 新增样式 */
.recommendations {
  margin-top: 30px;
}

.song-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.song-item {
  cursor: pointer;
  transition: transform 0.3s;
}

.song-item:hover {
  transform: translateY(-5px);
}

.rec-cover {
  width: 100%;
  height: 120px;
  object-fit: cover;
  border-radius: 8px;
}

.rec-info {
  padding: 10px 0;
}

.rec-cover {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  object-fit: cover;
  margin-right: 16px;
}

.rec-info {
  position: relative;
  padding-right: 40px; /* 为按钮留出空间 */
  flex-grow: 1;
}

.rec-info h3 {
  margin: 0 0 4px;
  font-size: 16px;
  color: #333;
}

.rec-info p {
  margin: 0;
  font-size: 14px;
  color: #666;
}

/* 修改喜欢按钮样式 */
button {
  transition: all 0.3s;
}

button:active {
  transform: scale(0.95);
}

.like-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: #999;
  margin-top: 8px;
  transition: all 0.3s;
}

.like-btn:active {
  transform: scale(0.9);
}

.like-btn.liked {
  color: #ff4d4f;
}
</style>
