<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script>
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import NavBar from '@/components/NavBar.vue'; // Add this import

export default {
  components: { NavBar }, // Make sure NavBar is registered
  setup() {
    const router = useRouter();
    const isAuthenticated = computed(() => !!localStorage.getItem('token'));
    
    return {
      isAuthenticated
    };
  }
}
</script>

<style>
@import '@/assets/theme.css';

#app {
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  min-height: 100vh;
  background: var(--bg-light);
  color: var(--text-dark);
}
</style>