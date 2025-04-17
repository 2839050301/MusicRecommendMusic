<template>
  <div class="auth-container">
    <div class="auth-box">
      <div class="logo">
        <i class="bi bi-music-note-beamed"></i>
        <h2>音乐推荐系统</h2>
      </div>

      <form @submit.prevent="login">
        <div class="form-group">
          <i class="bi bi-person"></i>
          <input 
            v-model="form.username" 
            type="text" 
            placeholder="用户名" 
            required
          >
        </div>
        <div class="form-group">
          <i class="bi bi-lock"></i>
          <input 
            v-model="form.password" 
            type="password" 
            placeholder="密码" 
            required
          >
        </div>
        <button type="submit" class="auth-btn">
          <i class="bi bi-box-arrow-in-right"></i> 登 录
        </button>
      </form>

      <div class="footer-links">
        <span>还没有账号？</span>
        <router-link to="/register">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import { useRouter } from 'vue-router';

export default {
  name: "Login",
  setup() {
    const router = useRouter();
    return { router };
  },
  data() {
    return {
      form: {
        username: '',
        password: ''
      }
    }
  },
  methods: {
    async login() {
      try {
        const response = await axios.get('http://localhost:8088/rest/users/login', {
          params: this.form
        });
        
        if (response.data.success) {
          // 获取用户ID
          const userIdResponse = await axios.get('http://localhost:8088/rest/users/userId', {
            params: { username: this.form.username }
          });
          
          if (userIdResponse.data.success) {
            localStorage.setItem('token', 'authenticated');
            localStorage.setItem('username', this.form.username);
            localStorage.setItem('userId', userIdResponse.data.userId);
            this.$router.push('/');
          } else {
            alert('获取用户信息失败');
          }
        } else {
          alert('用户名或密码错误');
        }
      } catch (error) {
        console.error('登录错误详情:', error);
        alert('登录出错: ' + (error.response?.data?.message || error.message));
      }
    },
    
    logout() {
      localStorage.removeItem('token');
      localStorage.removeItem('username');
      localStorage.removeItem('userId');
      this.$router.push('/login');
    }
  }
}
</script>

<style scoped>
.auth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
}

.auth-box {
  width: 380px;
  max-width: 90%; /* 新增：确保在小屏幕上不会超出视口 */
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-md);
  backdrop-filter: blur(10px);
  animation: fadeIn 0.6s ease-out;
}

.logo {
  text-align: center;
  margin-bottom: 30px;
}

.logo i {
  font-size: 50px;
  color: var(--primary-color);
}

.logo h2 {
  margin-top: 15px;
  color: var(--text-dark);
}

.form-group {
  position: relative;
  margin-bottom: 25px;
  width: 100%; /* 确保宽度100% */
}

.form-group input {
  width: 100%;
  padding: 12px 15px 12px 45px;
  border: 1px solid #ddd;
  border-radius: var(--radius-sm);
  font-size: 14px;
  transition: var(--transition);
  box-sizing: border-box; /* 新增：确保padding不会增加元素总宽度 */
}

.form-group input:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(49, 194, 124, 0.2);
}

.auth-btn {
  width: 100%;
  padding: 12px;
  background: var(--primary-color);
  color: white;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: var(--transition);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.auth-btn:hover {
  background: var(--secondary-color);
}

.auth-btn i {
  font-size: 18px;
}

.footer-links {
  margin-top: 20px;
  text-align: center;
  font-size: 14px;
  color: var(--text-light);
}

.footer-links a {
  color: var(--primary-color);
  text-decoration: none;
  font-weight: 500;
  margin-left: 5px;
}

.footer-links a:hover {
  text-decoration: underline;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 480px) {
  .auth-box {
    width: 95%;
    padding: 20px;
  }
  
  .form-group input {
    padding: 10px 15px 10px 40px;
  }
}
</style>