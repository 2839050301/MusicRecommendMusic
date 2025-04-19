import '@/assets/theme.css'
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'  // 确保已安装
import 'element-plus/dist/index.css'

const app = createApp(App)
app.use(router)
app.use(ElementPlus, {
  message: {
    duration: 500,
    showClose: false, // 关闭关闭按钮避免干扰
    customClass: 'custom-message' // 方便自定义样式
  }
})
app.mount('#app')