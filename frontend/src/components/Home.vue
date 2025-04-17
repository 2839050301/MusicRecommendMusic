<template>
  <div class="container mt-5">
    <div class="row justify-content-center">
      <div class="col-md-8">
        <div class="card">
          <div class="card-header bg-primary text-white">
            <h3 class="text-center">音乐推荐系统</h3>
          </div>
          <div class="card-body text-center">
            <h4>欢迎使用音乐推荐系统</h4>
            <div class="mt-4">
              <router-link to="/login" class="btn btn-primary mx-2">登录</router-link>
              <router-link to="/register" class="btn btn-secondary mx-2">注册</router-link>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "Home"
}
</script>

<style scoped>
.card {
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}
</style>