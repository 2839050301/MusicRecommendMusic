const userId = localStorage.getItem('userId');
if (!userId) {
  // 处理未登录情况
  this.$router.push('/login');
  return;
}

// 使用userId调用API
this.$http.get('/rest/songs/stream', {
  params: {
    username: localStorage.getItem('username'),
    num: 10
  }
})
