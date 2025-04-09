<template>
  <div class="home-page">
    <!-- 顶部导航栏 -->
    <header>
      <router-link to="/" class="logo">井云科技</router-link>
      <nav>
        <ul>
          <li><a href="https://jingyun.ai/" target="_blank" rel="noopener noreferrer">官网</a></li>
          <li><a href="https://jingyuncenter.com/" target="_blank" rel="noopener noreferrer">创作</a></li>
          <li><a href="https://www.zizuoai.com/" target="_blank" rel="noopener noreferrer">数字人</a></li>
          <li><a href="https://acnq1rombfk4.feishu.cn/wiki/space/7475524021124497436"
                 target="_blank"
                 rel="noopener noreferrer">文档</a></li>
        </ul>
      </nav>
      <div class="btn-group">
        <a href="#" class="btn-purchase" @click.prevent="handleLogin">登录</a>
      </div>
    </header>

    <!-- Hero 区域 -->
    <section class="hero">
      <h1>你的第一个数字员工，<span>AI赋能企业降本增效</span></h1>
      <p>
        井云AI数字员工作为企业"数字枢纽"，深度融合文案创作、数字人交互与私域运营，构建全域智能增长引擎。支持动态生成短视频脚本、朋友圈SOP及直播剧本，创意生产效率提升15倍。</p>

      <div class="avatar-stack stats">
        <img
            v-for="(avatar, index) in avatars"
            :key="index"
            :src="require(`@/assets/${avatar}`)"
            class="avatar"
            alt="用户头像"
        />
        <span>1000+ 付费客户</span>
      </div>

      <div class="hero-buttons">
        <button class="hero-button btn-preview" @click="handlePreview">立即进入</button>
        <button class="hero-button btn-figma" @click="showCooperationQRCode">渠道合作</button>
      </div>

      <div>
        <a href="#" class="free-version" @click.prevent="showFreeVersionQRCode">获取免费版本</a>
      </div>
    </section>

    <!-- 二维码弹窗 -->
    <QRCodeDialog
        :show="showQRCode"
        :title="qrCodeTitle"
        :text="qrCodeText"
        @close="closeQRCode"
    />
  </div>
</template>

<script>
import QRCodeDialog from '@/components/QRCodeDialog.vue'

export default {
  name: 'HomeView',
  components: {
    QRCodeDialog
  },
  data() {
    return {
      avatars: ['avatar-2.webp', 'avatar-3.webp', 'avatar-4.webp'],
      showQRCode: false,
      qrCodeTitle: '',
      qrCodeText: ''
    }
  },
  methods: {
    handleLogin() {
      // 处理登录逻辑
      this.$router.push('/chat')
    },
    handlePreview() {
      // 预览功能
      this.$router.push('/chat')
    },
    showCooperationQRCode() {
      this.qrCodeTitle = '渠道合作'
      this.qrCodeText = '添加客服沟通合作'
      this.showQRCode = true
    },
    showFreeVersionQRCode() {
      this.qrCodeTitle = '获取免费版本'
      this.qrCodeText = '添加客领取激活码'
      this.showQRCode = true
    },
    closeQRCode() {
      this.showQRCode = false
    }
  }
}
</script>

<style scoped>
:deep(.el-dialog) {
  border: none !important;
  background: transparent !important;
}

:deep(.el-dialog__header) {
  display: none !important;
}

:deep(.el-dialog__body) {
  padding: 0 !important;
  background: rgba(0, 0, 0, 0.7) !important;
  border-radius: 12px !important;
}

:deep(.el-dialog__close) {
  color: white !important;
  font-size: 20px;
}

.home-page {
  min-height: 100vh;
  background: #0e0f10 url("~@/assets/background-img.png");
  color: #fff;
}

/* 顶部导航样式 */
header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 2rem;
  background-color: transparent;
}

.logo {
  font-weight: bold;
  font-size: 1.5rem;
  color: #00d68f;
  text-decoration: none;
}

nav ul {
  list-style: none;
  display: flex;
  gap: 2rem;
}

nav a {
  color: inherit;
  transition: color 0.3s;
  text-decoration: none;
}

nav a:hover {
  color: #00d68f;
}

/* 按钮组样式 */
.btn-group {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.btn-purchase {
  padding: 0.5rem 1rem;
  border-radius: 4px;
  background-color: #00d68f;
  color: #000;
  cursor: pointer;
  transition: filter 0.3s;
  text-decoration: none;
}

.btn-purchase:hover {
  filter: brightness(1.1);
}

/* Hero 区域样式 */
.hero {
  padding: 14rem 2rem;
  text-align: center;
  position: relative;
}

.hero h1 {
  font-size: 2.5rem;
  margin-bottom: 1rem;
}

.hero h1 span {
  color: #00d68f;
}

.hero p {
  max-width: 1000px;
  margin: 0.5rem auto 2rem;
  color: #cccccc;
}

/* 头像堆叠样式 */
.avatar-stack {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 2rem;
  width: auto; /* 改为自适应宽度 */
  margin-left: auto;
  margin-right: auto;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 2px solid white;
  margin-left: -10px;
  transition: transform 0.3s ease, filter 0.3s ease;
}

.avatar:hover {
  transform: translateY(-5px);
  z-index: 1;
}

.avatar-stack:hover .avatar:not(:hover) {
  filter: brightness(0.9) blur(1px);
}

/* 按钮样式 */
.hero-buttons {
  display: flex;
  justify-content: center;
  gap: 1rem;
  margin-bottom: 2rem;
}

.hero-button {
  padding: 0.8rem 1.5rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-preview {
  background-color: #fff;
  color: #000;
}

.btn-preview:hover {
  background-color: #e6e6e6;
}

.btn-figma {
  background-color: transparent;
  border: 2px solid #fff;
  color: #fff;
}

.btn-figma:hover {
  background-color: #fff;
  color: #000;
}

.free-version {
  color: #00d68f;
  text-decoration: none;
  transition: opacity 0.3s;
}

.free-version:hover {
  opacity: 0.8;
}

/* 响应式设计 */
@media (max-width: 768px) {
  header {
    flex-direction: row;
    align-items: center;
    padding: 0.5rem 1rem;
    flex-wrap: nowrap;
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }

  nav ul {
    display: flex;
    gap: 0.8rem;
    margin: 0;
    padding: 0;
    flex-wrap: nowrap;
  }

  nav a {
    font-size: 0.9rem;
    white-space: nowrap;
  }

  .btn-group {
    margin: 0;
    padding-left: 0.5rem;
  }

  .logo {
    font-size: 1.2rem;
    white-space: nowrap;
  }

  .hero {
    padding: 6rem 1rem;
  }

  .hero h1 {
    font-size: 2rem;
  }
}
</style>