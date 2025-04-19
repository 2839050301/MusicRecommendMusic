<template>
  <div class="auth-container">
    <div class="auth-box">
      <div class="logo">
        <i class="bi bi-music-note-beamed"></i>
        <h2>用户注册</h2>
      </div>

      <form @submit.prevent="register" class="form-container">
        <div class="form-group">
          <input v-model="form.username" type="text" placeholder="用户名" required>
          <i class="bi bi-person"></i>
        </div>
        <div class="form-group">
          <input v-model="form.password" type="password" placeholder="密码" required>
          <i class="bi bi-lock"></i>
        </div>
        <div class="form-group">
          <select v-model="form.gender" class="form-select">
            <option value="male">男</option>
            <option value="female">女</option>
          </select>
          <i class="bi bi-gender-male"></i>
        </div>
        <button type="submit" class="auth-btn">注 册</button>
      </form>

      <div class="footer-links">
        <router-link to="/login">已有账号? 去登录</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: "Register",
  data() {
    return {
      form: {
        username: '',
        password: '',
        gender: 'male'
      }
    }
  },
  methods: {
    async register() {
      try {
        const response = await axios.get('http://localhost:8088/rest/users/register', {
          params: this.form
        });
        if (response.data.success) {
          alert('注册成功');
          this.$router.push('/login');
        } else {
          alert('注册失败');
        }
      } catch (error) {
        alert('注册出错');
      }
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
  width: 100%;  /* 新增 */
  background: linear-gradient(135deg, #31c27c, #1a7d5d);
}

.auth-box {
  width: 380px;
  max-width: 95%;  /* 新增 - 防止在小屏幕上溢出 */
  padding: 40px;
  margin: 20px;  /* 新增 - 提供呼吸空间 */
  background: white;
  border-radius: 10px;
  box-shadow: 0 5px 20px rgba(0,0,0,0.1);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.form-container {
  width: 100%;
}

.logo {
  text-align: center;
  margin-bottom: 30px;
}

.logo i {
  font-size: 50px;
  color: #31c27c;
}

.logo h2 {
  margin-top: 15px;
  color: #333;
}

.form-group {
  position: relative;
  margin-bottom: 25px;
  width: 100%;
}

.form-group input,
.form-select {
  width: 100%;  /* 修改为100%宽度 */
  padding: 12px 15px 12px 40px;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 14px;
  transition: all 0.3s;
  box-sizing: border-box;
}

.form-group i {
  position: absolute;
  left: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: #999;
  pointer-events: none;
  z-index: 2;  /* 新增：确保图标在输入框上方 */
}

.form-select {
  appearance: none;
  background-image: url("data:image/svg+xml;charset=UTF-8,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3e%3cpolyline points='6 9 12 15 18 9'%3e%3c/polyline%3e%3c/svg%3e");
  background-repeat: no-repeat;
  background-position: right 10px center;
  background-size: 1em;
}

.form-group input:focus,
.form-select:focus {
  border-color: #31c27c;
  box-shadow: 0 0 0 3px rgba(49,194,124,0.2);
  outline: none;
}

.auth-btn {
  width: 100%;
  padding: 12px;
  background: #31c27c;
  color: white;
  border: none;
  border-radius: 5px;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.3s;
}

.auth-btn:hover {
  background: #2aac6e;
}

.footer-links {
  margin-top: 20px;
  text-align: center;
  font-size: 14px;
  color: #999;
}

.footer-links a {
  color: #666;
  text-decoration: none;
}

.footer-links a:hover {
  color: #31c27c;
}
</style>