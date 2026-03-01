<template>
  <div class="oauth-background">
    <div id="preloader_6">
      <span></span>
      <span></span>
      <span></span>
      <span></span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { giteeLogin, githubLogin, qqLogin } from "@/api/login";
import { useUserStore } from "@/store";
import { setToken } from "@/utils/token";
const user = useUserStore();
const router = useRouter();
const route = useRoute();

const handleOauthCallback = async (loginFn: Function) => {
  try {
    const { data } = await loginFn({ code: route.query.code as string });
    if (data.flag) {
      setToken(data.data);
      await user.GetUserInfo();
      if (user.email === "") {
        window.$message?.warning("请绑定邮箱以便及时收到回复");
      } else {
        window.$message?.success("登录成功");
      }
    } else {
      window.$message?.error("登录失败");
    }
  } catch {
    window.$message?.error("登录失败，请重试");
  }
  const loginUrl = user.path;
  router.push(loginUrl && loginUrl !== "" ? loginUrl : "/");
};

onMounted(() => {
  const path = route.path;
  if (path === "/oauth/login/qq") {
    handleOauthCallback(qqLogin);
  } else if (path === "/oauth/login/gitee") {
    handleOauthCallback(giteeLogin);
  } else if (path === "/oauth/login/github") {
    handleOauthCallback(githubLogin);
  } else {
    router.push("/");
  }
});
</script>

<style scoped>
.oauth-background {
  position: fixed;
  top: 0;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  z-index: 1000;
}

#preloader_6 {
  position: relative;
  top: 45vh;
  left: 47vw;
  animation: preloader_6 5s infinite linear;
}

#preloader_6 span {
  width: 20px;
  height: 20px;
  position: absolute;
  background: red;
  display: block;
  animation: preloader_6_span 1s infinite linear;
}

#preloader_6 span:nth-child(1) {
  background: #2ecc71;
}

#preloader_6 span:nth-child(2) {
  left: 22px;
  background: #9b59b6;
  animation-delay: 0.2s;
}

#preloader_6 span:nth-child(3) {
  top: 22px;
  background: #3498db;
  animation-delay: 0.4s;
}

#preloader_6 span:nth-child(4) {
  top: 22px;
  left: 22px;
  background: #f1c40f;
  animation-delay: 0.6s;
}

@keyframes preloader_6 {
  from {
    -ms-transform: rotate(0deg);
  }

  to {
    -ms-transform: rotate(360deg);
  }
}

@keyframes preloader_6_span {
  0% {
    transform: scale(1);
  }

  50% {
    transform: scale(0.5);
  }

  100% {
    transform: scale(1);
  }
}
</style>
