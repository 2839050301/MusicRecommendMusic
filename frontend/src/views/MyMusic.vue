

<template>
  <div class="my-music-container">
    <NavBar />
    <div class="content">
      <div class="section">
        <h2 class="section-title">我喜欢的歌曲</h2>
        <div v-if="loading" class="loading">加载中...</div>
        <div v-else class="song-list">
          <div
            v-for="song in likedSongs"
            :key="song.songId"
            class="song-item"
          >
            <div class="song-cover">
              <img
                :src="song.url || require('@/assets/default-music-cover.jpg')"
                :alt="song.sname"
                @error="handleImageError"
              >
            </div>
            <div class="song-info">
              <h3>{{ song.sname }}</h3>
              <p>{{ song.singerName }}</p>
            </div>
            <button
              @click="toggleLike(song.songId, false)"
              class="like-btn"
            >
              <i class="bi bi-heart-fill"></i> 取消喜欢
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
  components: { NavBar },
  data() {
    return {
      likedSongs: [],
      loading: false,
      currentUserId: localStorage.getItem('userId') || 1
    }
  },
  created() {
    this.fetchLikedSongs()
  },
  methods: {
    async fetchLikedSongs() {
      this.loading = true
      try {
        const likedResponse = await axios.get(
          'http://localhost:8088/rest/songs/user/liked-songs',
          { params: { userId: this.currentUserId } }
        )

        if (likedResponse.data?.success && likedResponse.data.User_like) {
          const songIds = likedResponse.data.User_like.map(item => item.songId)

          // 获取歌曲详情时同时获取歌手信息
          this.likedSongs = await Promise.all(
            songIds.map(async id => {
              const songRes = await axios.get('http://localhost:8088/rest/songs/song', {
                params: { songId: id }
              })

              if (songRes.data?.success && songRes.data.song) {
                const song = songRes.data.song
                // 修复字段名大小写问题，同时确保歌手ID存在
                const singerId = song.singerId || song.SingerId
                let singerName = '未知歌手'

                if (singerId) {
                  const singerRes = await axios.get('http://localhost:8088/rest/songs/singer', {
                    params: { singerId: Number(singerId) }
                  })
                  singerName = singerRes.data?.singer?.sname || `歌手ID: ${singerId}`
                }

                return {
                  ...song,
                  singerName // 确保使用统一的字段名
                }
              }
              return null
            })
          ).then(songs => songs.filter(Boolean)) // 过滤掉null值
        }
      } catch (error) {
        console.error('获取喜欢的歌曲失败:', error)
        this.$message.error('获取喜欢的歌曲失败')
      } finally {
        this.loading = false
      }
    },

    handleImageError(e) {
      e.target.src = require('@/assets/default-music-cover.jpg')
    },

    async toggleLike(songId, isLike) {
      try {
        const response = await axios({
          method: 'post',
          url: `http://localhost:8088/rest/songs/${isLike ? 'like' : 'unlike'}`,
          params: {
            userId: this.currentUserId,
            songId: songId
          },
          headers: {
            'Content-Type': 'application/json'
          }
        });

        if (response.data?.success) {
          this.$message.success({
            message: isLike ? '已添加到喜欢列表' : '已取消喜欢',
            duration: 500  // 明确设置持续时间
          })
          this.fetchLikedSongs(); // 刷新喜欢列表
        } else {
          throw new Error(response.data?.message || '操作失败');
        }
      } catch (error) {
        console.error('操作失败:', error);
        this.$message.error(error.message || '操作失败');
      }
    },

    async getSingerName(singerId) {
      if (!singerId || isNaN(singerId)) return '未知歌手';

      try {
        const response = await axios.get(
          'http://localhost:8088/rest/songs/singer',
          {
            params: {
              singerId: Number(singerId)
            }
          }
        );

        if (response.data?.success && response.data.singer) {
          return response.data.singer.sname || `歌手ID: ${singerId}`;
        }
        return `歌手ID: ${singerId}`;
      } catch (error) {
        console.error('获取歌手信息失败:', error);
        return `歌手ID: ${singerId}`;
      }
    }
  }
}
</script>

<style scoped>
.my-music-container {
  background: #f8f9fa;
  min-height: 100vh;
}

.content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
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

.song-list {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
}

.song-item {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.song-item:hover {
  background: rgba(49, 194, 124, 0.05);
}

.song-cover {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  overflow: hidden;
  margin-right: 16px;
  flex-shrink: 0;
}

.song-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.song-info {
  flex-grow: 1;
}

.song-info h3 {
  margin: 0 0 4px;
  font-size: 16px;
  color: #333;
}

.song-info p {
  margin: 0;
  font-size: 14px;
  color: #666;
}

.like-btn {
  background: none;
  border: 1px solid #ff4d4f;
  color: #ff4d4f;
  padding: 5px 10px;
  border-radius: 4px;
  cursor: pointer;
  margin-left: 16px;
}

.like-btn:hover {
  background: #fff2f0;
}

.bi-heart-fill {
  color: #ff4d4f;
}
</style>
