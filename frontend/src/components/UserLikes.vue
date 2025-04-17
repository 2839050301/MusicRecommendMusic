import { getUserId } from '@/utils/auth';

export default {
  data() {
    return {
      likedSongs: [],
      loading: false
    }
  },
  async created() {
    try {
      const userId = getUserId(); // 从localStorage获取userId
      this.loading = true;
      
      // 调用获取用户喜欢歌曲的API
      const response = await this.$http.get('/rest/songs/user/liked-songs', {
        params: { userId }
      });
      
      if (response.data?.success && response.data.User_like) {
        // 获取歌曲详情
        const songPromises = response.data.User_like.map(like => 
          this.$http.get('/rest/songs/song', { params: { songId: like.songId } })
        );
        
        const songResponses = await Promise.all(songPromises);
        this.likedSongs = songResponses
          .filter(res => res.data?.success && res.data.song)
          .map(res => res.data.song);
      }
    } catch (error) {
      console.error('获取喜欢的歌曲失败:', error);
      this.$message.error('获取喜欢的歌曲失败');
    } finally {
      this.loading = false;
    }
  }
}