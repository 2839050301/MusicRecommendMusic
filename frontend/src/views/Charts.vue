<template>
  <div class="charts-container">
    <NavBar />
    <div class="content">
      <div class="section">
        <h2 class="section-title">热门排行榜</h2>
        <div class="chart-list">
          <div v-if="loading" class="loading">加载中...</div>
          <div v-else-if="songs.length === 0" class="empty">
            <i class="bi bi-music-note"></i>
            <p>暂无排行榜数据</p>
          </div>
          <div v-else>
            <div 
              v-for="(song, index) in songs" 
              :key="song.id || index"
              class="chart-item"
              @click="playSong(song)"
            >
              <div class="rank">{{ index + 1 }}</div>
              <div class="song-cover">
                <img :src="song.coverUrl || require('@/assets/default-cover.jpg')" alt="歌曲封面">
              </div>
              <div class="song-details">
                <h3>{{ song.name }}</h3>
                <p>{{ song.singer }}</p>
              </div>
              <div class="song-hot">
                <i class="bi bi-fire"></i> {{ song.hot }}
              </div>
              <button
                @click.stop="toggleLike(song.songId)"
                class="like-btn"
                :class="{ 'liked': likedSongIds.includes(song.songId) }"
              >
                <i :class="likedSongIds.includes(song.songId) ? 'bi-heart-fill' : 'bi-heart'"></i>
                {{ likedSongIds.includes(song.songId) ? '已喜欢' : '喜欢' }}
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
      songs: [],
      loading: false,
      likedSongIds: []
    };
  },
  created() {
    this.restoreLikedSongs(); // 恢复本地存储的喜欢列表
    this.fetchHotSongs();
    this.fetchLikedSongs();
  },
  methods: {
    async fetchHotSongs() {
      this.loading = true;
      try {
        const response = await axios.get('http://localhost:8088/rest/songs/hotSongs', {
          params: { num: 50 }
        });

        if (response.data?.success) {
          const songsData = response.data.songs || [];
          const singerIds = [...new Set(songsData.map(song => song.singerId))];
          const singersMap = new Map();

          await Promise.all(singerIds.map(async id => {
            const singerRes = await axios.get('http://localhost:8088/rest/songs/singer', {
              params: { singerId: id }
            });
            if (singerRes.data?.success && singerRes.data.singer) {
              singersMap.set(id, singerRes.data.singer.sname);
            }
          }));

          this.songs = songsData.map(song => ({
            id: song.songId,
            songId: song.songId,
            name: song.sname || '未知歌曲',
            singer: singersMap.get(song.singerId) || `歌手ID: ${song.singerId}`,
            hot: song.hot || 0,
            coverUrl: song.url,
            genre: this.getGenreName(song.genre),
            tags: song.tags,
            languages: song.languages
          }));
        } else {
          this.songs = [];
        }
      } catch (error) {
        console.error('获取数据失败:', error);
        this.$message.error('获取排行榜数据失败');
        this.songs = [];
      } finally {
        this.loading = false;
      }
    },
    getGenreName(genreId) {
      const genreMap = {
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
    playSong(song) {
      console.log('播放歌曲:', song);
      // 添加播放逻辑
    },
    async toggleLike(songId) {
      try {
        const isLike = !this.likedSongIds.includes(songId);
        const userId = localStorage.getItem('userId') || 1;
        const response = await axios({
          method: 'post',
          url: `http://localhost:8088/rest/songs/${isLike ? 'like' : 'unlike'}`,
          params: { userId, songId },
          headers: { 'Content-Type': 'application/json' }
        });

        if (response.data?.success) {
          this.$message.success({
            message: isLike ? '已添加到喜欢列表' : '已取消喜欢',
            duration: 500
          });
          this.likedSongIds = isLike
            ? [...this.likedSongIds, songId]
            : this.likedSongIds.filter(id => id !== songId);
          this.saveLikedSongs(); // 保存到本地存储
        } else {
          throw new Error(response.data?.message || '操作失败');
        }
      } catch (error) {
        console.error('操作失败:', error);
        this.$message.error(error.message || '操作失败');
      }
    },
    async fetchLikedSongs() {
      try {
        const userId = localStorage.getItem('userId') || 1;
        const response = await axios.get(
          'http://localhost:8088/rest/songs/user/liked-songs',
          { params: { userId } }
        );
        if (response.data?.success) {
          this.likedSongIds = response.data.User_like?.map(item => item.songId) || [];
          this.saveLikedSongs(); // 成功后保存到本地存储
        }
      } catch (error) {
        console.error('获取喜欢列表失败:', error);
        this.restoreLikedSongs(); // 备用：从本地存储恢复
      }
    },
    // 本地存储相关方法
    saveLikedSongs() {
      const userId = localStorage.getItem('userId') || 1;
      localStorage.setItem(`likedSongs_${userId}`, JSON.stringify(this.likedSongIds));
    },
    restoreLikedSongs() {
      const userId = localStorage.getItem('userId') || 1;
      const stored = localStorage.getItem(`likedSongs_${userId}`);
      this.likedSongIds = stored ? JSON.parse(stored) : [];
    }
  }
};
</script>

<style scoped>
/* 美化样式 */
.charts-container {
  background: linear-gradient(135deg, #f8f9fa, #eef2f8);
  min-height: 100vh;
}

.content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.section-title {
  font-size: 28px;
  color: #2c3e50;
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
  background: linear-gradient(to right, #4CAF50, #45a049);
}

.chart-list {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.chart-item {
  display: flex;
  align-items: center;
  padding: 18px 24px;
  transition: all 0.3s ease;
  cursor: pointer;
  background: #fff;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
  border-radius: 8px;
}

.chart-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.12);
}

.chart-item:nth-child(even) {
  background: #f8f9fa;
}

.rank {
  width: 40px;
  font-size: 18px;
  font-weight: bold;
  color: #666;
  text-align: center;
  margin-right: 20px;
}

.song-cover {
  width: 60px;
  height: 60px;
  margin-right: 20px;
  border-radius: 8px;
  overflow: hidden;
  background: #f0f4f8;
}

.song-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}

.song-details {
  flex: 1;
}

.song-details h3 {
  font-size: 16px;
  color: #333;
  margin-bottom: 5px;
}

.song-details p {
  font-size: 14px;
  color: #666;
  opacity: 0.8;
}

.song-hot {
  margin-left: auto;
  color: #ff6b6b;
  font-weight: 500;
  display: flex;
  align-items: center;
}

.song-hot i {
  font-size: 18px;
  color: #ff6b6b;
}

.loading {
  padding: 20px;
  text-align: center;
  color: #666;
  font-size: 16px;
}

.empty {
  padding: 60px 0;
  text-align: center;
  color: #666;
}

.empty i {
  font-size: 60px;
  color: #4CAF50;
}

.empty p {
  margin-top: 15px;
  font-size: 16px;
  opacity: 0.8;
}

.like-btn {
  background: none;
  border: 1px solid #e0e0e0;
  padding: 8px 16px;
  border-radius: 24px;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-left: 20px;
}

.like-btn:hover {
  background: #f0f4f8;
  transform: scale(1.05);
}

.like-btn.liked {
  color: #ff6b6b;
  border-color: #ff6b6b;
  background: #fff1f0;
}

.like-btn i {
  font-size: 16px;
  transition: transform 0.3s;
}

.like-btn.liked i {
  transform: scale(1.1);
}

/* 响应式布局 */
@media (max-width: 768px) {
  .chart-item {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .song-cover {
    margin: 15px 0;
  }

  .song-hot,
  .like-btn {
    margin: 10px 0;
  }
}
</style>
