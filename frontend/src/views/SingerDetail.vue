<template>
  <div class="singer-detail">
    <NavBar />
    <div class="content">
      <div class="section">
        <h2 class="section-title">{{ singer?.sname || '歌手详情' }}</h2>

        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="songs.length === 0" class="empty">
          <i class="bi bi-music-note"></i>
          <p>暂无歌曲数据</p>
        </div>
        <div v-else class="song-list">
          <!-- 修改歌曲项部分 -->
          <div
            v-for="song in songs"
            :key="song.songId"
            class="song-item"
            @click="handleSongItemClick($event, song.songId)"
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
              <p>歌手: {{ singer?.sname || '未知歌手' }}</p>
              <p>热度: {{ song.hot }}</p>
              <p>流派: {{ getGenreName(song.genre) }}</p>
              <p>标签: {{ song.tags || '无' }}</p>
            </div>
            <!-- 收藏按钮 -->
            <div class="like-btn-container">
              <button
                class="like-btn"
                :class="{ 'liked': song.isLiked }"
                @click="toggleLike(song)"
                :disabled="!userId"
              >
                <i class="bi" :class="song.isLiked ? 'bi-heart-fill' : 'bi-heart'"></i>
                {{ song.isLiked ? '已收藏' : '收藏' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import NavBar from '@/components/NavBar.vue';
import axios from 'axios';

export default {
  components: { NavBar },
  data() {
    return {
      singer: null,
      songs: [],
      loading: false,
      userId: null // 新增 userId
    };
  },
  created() {
    this.userId = localStorage.getItem('userId'); // 初始化 userId
    this.fetchSingerDetail();
  },
  methods: {
    async fetchSingerDetail() {
      this.loading = true;
      const singerId = this.$route.params.id; // 从路由参数中获取歌手ID
      try {
        // 获取歌手信息
        const singerRes = await axios.get('http://localhost:8088/rest/songs/singer', {
          params: { singerId }
        });
        console.log('歌手信息:', singerRes.data);

        // 获取歌手的歌曲列表
        const songsRes = await axios.get('http://localhost:8088/rest/songs/singer/songs', {
          params: { singerId }
        });
        console.log('歌曲列表:', songsRes.data);

        if (singerRes.data?.success && songsRes.data?.success) {
          this.singer = singerRes.data.singer;

          // 获取用户收藏的歌曲列表
          if (this.userId) {
            const likedSongsRes = await axios.get('http://localhost:8088/rest/songs/user/liked-songs', {
              params: { userId: this.userId }
            });
            console.log('用户收藏的歌曲:', likedSongsRes.data);

            const likedSongIds = likedSongsRes.data?.success
              ? likedSongsRes.data.User_like.map(like => like.songId)
              : [];

            // 初始化歌曲列表，并设置 isLiked 状态
            this.songs = (songsRes.data.songs || []).map(song => ({
              ...song,
              isLiked: likedSongIds.includes(song.songId)
            }));
          } else {
            // 用户未登录，直接初始化歌曲列表
            this.songs = songsRes.data.songs || [];
          }
        } else {
          this.$message.error('获取歌手详情失败');
        }
      } catch (error) {
        console.error('获取数据失败:', error);
        if (error.response) {
          this.$message.error(`请求失败: ${error.response.status} ${error.response.data.message}`);
        } else if (error.request) {
          this.$message.error('请求超时，请检查网络连接');
        } else {
          this.$message.error('请求失败，请稍后重试');
        }
      } finally {
        this.loading = false;
      }
    },
    async toggleLike(song) {
      try {
        if (!this.userId) {
          this.$message.warning('请先登录');
          return;
        }

        const isLike = !song.isLiked;
        const response = await axios({
          method: 'post',
          url: `http://localhost:8088/rest/songs/${isLike ? 'like' : 'unlike'}`,
          params: { userId: this.userId, songId: song.songId },
          headers: { 'Content-Type': 'application/json' }
        });

        if (response.data?.success) {
          this.$message.success({
            message: isLike ? '已收藏' : '已取消收藏',
            duration:500
          });
          song.isLiked = isLike;
        } else {
          throw new Error(response.data?.message || '操作失败');
        }
      } catch (error) {
        console.error('操作失败:', error);
        this.$message.error(error.message || '操作失败');
      }
    },
    getGenreName(genreId) {
      const genreMap = {
        1000: '流行',
        1017: '嘻哈说唱',
        1006: '轻音乐',
        1003: '民谣',
        1008: '摇滚',
        1010:'原声带',
        1026:'儿童',
        1077:'乡村',
        1007:'新世纪',
        152124:'朋克',
        1071:'拉丁音乐',
        1051:'金属',
        1037:'蓝调',
        1014:'另类',
        1043:'雷鬼',
        1012:'世界音乐',
        1015:'电子',
        1028:'节奏布鲁斯',
        1252:'二次元',
        1013:'民谣',
        10119:'国风',
        1001:'中国传统特色',
        79088:'慢摇DJ',
        1022:'古典',
        1011:'爵士'

      };
      return genreMap[genreId] || `流派ID: ${genreId}`;
    },
    handleImageError(e) {
      e.target.src = require('@/assets/default-music-cover.jpg');
    },
    viewSongDetail(songId) {
      console.log('跳转到歌曲详情:', songId); // 调试用
      this.$router.push(`/song/${songId}`);
    },
    handleSongItemClick(event, songId) {
      // 检查点击是否来自like-btn或其子元素
      if (event.target.closest('.like-btn')) {
        return; // 如果是点击了喜欢按钮，则不跳转
      }
      this.$router.push(`/song/${songId}`);
    },

  }
}
</script>

<style scoped>
/* 原有样式 */
.singer-detail {
  background: #f8f9fa;
  min-height: 100vh;
}

.section-title {
  font-size: 24px;
  margin-bottom: 24px;
  color: #333;
  position: relative;
  padding-bottom: 12px;
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
  transition: all 0.3s ease;
}

.song-item:hover {
  background: rgba(49, 194, 124, 0.05);
}

.song-cover {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.song-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.song-info {
  flex-grow: 1;
  padding-left: 16px;
}

.song-info h3 {
  margin: 0 0 8px;
  font-size: 16px;
  color: #333;
  font-weight: 500;
}

.song-info p {
  margin: 4px 0;
  font-size: 13px;
  color: #666;
}

.hot-tag {
  display: inline-block;
  padding: 2px 6px;
  background: #ffeaea;
  color: #ff6b6b;
  border-radius: 4px;
  font-size: 12px;
  margin-left: 8px;
}

/* 新增喜欢按钮样式 */
.like-btn-container {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  min-width: 100px;
}

.like-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 1.2em;
  color: #ddd;
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 20px;
  transition: all 0.3s ease;
}

.like-btn:hover {
  background: rgba(255, 107, 107, 0.1);
}

.like-btn.liked {
  color: #ff6b6b;
}

.bi-heart-fill {
  color: #ff6b6b;
}
</style>
