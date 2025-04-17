<template>
  <div class="singers-container">
    <NavBar />
    <div class="content">
      <div class="section">
        
        <!-- 字母导航 -->
        <div class="alphabet-nav">
          <span 
            v-for="letter in alphabet" 
            :key="letter"
            @click="fetchSingersByInitial(letter)"
            :class="{ active: activeInitial === letter }"
          >
            {{ letter }}
          </span>
        </div>

        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="singers.length === 0" class="empty">
          <i class="bi bi-music-note"></i>
          <p>暂无歌手数据</p>
        </div>
        <div v-else class="singer-list">
          <div 
            v-for="singer in singers"
            :key="singer.singerId"
            class="singer-item"
            @click="viewSinger(singer.singerId)"
          >
            <h3>{{ singer.sname }}</h3>
            <i class="bi bi-chevron-right"></i>
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
      singers: [],
      alphabet: 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'.split(''),
      activeInitial: 'A',
      loading: false
    }
  },
  created() {
    this.fetchSingersByInitial('A')
    // 在created或mounted中添加
    axios.defaults.headers.common['Content-Type'] = 'application/json'
  },
  methods: {
    async fetchSingersByInitial(initial) {
      this.loading = true
      this.activeInitial = initial
      try {
        const response = await axios.get('http://localhost:8088/rest/songs/singers/initial', {
          params: { initial }
        })
        if (response.data && response.data.success) {
          this.singers = response.data.singers.map(singer => ({
            singerId: singer.singerId,
            sname: singer.sname || `歌手ID: ${singer.singerId}`
          }))
        } else {
          this.$message.error(response.data?.message || '获取歌手数据失败')
        }
      } catch (error) {
        console.error('获取歌手列表失败:', error)
        this.$message.error(error.response?.data?.message || '获取歌手数据失败')
      } finally {
        this.loading = false
      }
    },
    // Remove the duplicate methods block and move this to the main methods object
    viewSinger(singerId) {
      this.$router.push({
        name: 'SingerDetail',
        params: { id: singerId }
      })
    }
  }
}
</script>

<style scoped>
.singer-list {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
}

.singer-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.3s ease;
}

.singer-item:hover {
  background: rgba(49, 194, 124, 0.05);
  transform: translateY(-2px);
}

.singer-item h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
  font-weight: 500;
}

.bi-chevron-right {
  color: #ccc;
  font-size: 18px;
}.alphabet-nav {
   display: flex;
   flex-wrap: wrap;
   gap: 8px;
   margin-bottom: 20px;
 }

.alphabet-nav span {
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 4px;
}

.alphabet-nav span.active {
  background: var(--primary-color);
  color: white;
}

.loading {
  padding: 40px;
  text-align: center;
  color: var(--text-light);
}

.empty {
  padding: 40px;
  text-align: center;
  color: var(--text-light);
}
</style>
