<template>
  <el-card class="profile-card" shadow="always" body-style="padding: 0">
    <!-- 卡片头部：包含背景图与头像 -->
    <div class="profile-card__header">
      <img
          v-if="backgroundImage"
          :src="require(`@/assets/img/${backgroundImage}`)"
          class="profile-card__cover"
          alt="背景图"
      />
      <img
          v-if="icon"
          :src="require(`@/assets/avatars/${icon}`)"
          class="profile-card__avatar"
          alt="头像"
      />
    </div>

    <!-- 卡片主体信息 -->
    <div class="profile-card__body">
      <h3 class="profile-card__name">{{ name }}</h3>
      <p class="profile-card__position">{{ position }}</p>
      <div class="profile-card__stats">
        <div class="profile-card__stat">
          <strong>{{ introduce }}</strong>
        </div>
      </div>
      <div class="profile-card__social">
        <!-- 这里可以放你想要的社交图标或链接 -->
        <i class="iconfont icon-facebook"></i>
        <i class="iconfont icon-instagram"></i>
        <i class="iconfont icon-linkedin"></i>
      </div>
    </div>
  </el-card>
</template>

<script>
export default {
  name: 'ProfileCard',
  props: {
    // 接收卡片数据
    name: {
      type: String,
      default: '',
    },
    position: {
      type: String,
      default: '',
    },
    introduce: {
      type: String,
      default: '',
    },
    icon: {
      type: String,
      default: '', // 存放头像文件名，如 "user1.png"
    },
    backgroundImage: {
      type: String,
      default: '', // 存放背景文件名，如 "cover-1.webp"
    },
  },
};
</script>

<style scoped>
.profile-card {
  /* 根据需要可设置卡片宽度或自适应 */
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
  padding: 0; /* 移除默认内边距 */
  background: var(--bgColor1); /* 设置黑色背景 */
  border: none; /* 去掉边框 */
}

/* 卡片头部：相对定位，内部背景图和头像绝对定位叠加 */
.profile-card__header {
  /* position: relative;*/
  width: 100%;
  height: 100%; /* 可以根据需要调节高度 */
  overflow: hidden;
}

/* 背景图，占满 header */
.profile-card__cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

/* 头像（图标），在背景图中间 */
.profile-card__avatar {
  position: absolute;
  left: 50%;
  /*top: 50%;*/
  transform: translate(-50%, -50%);
  width: 60px;
  height: 60px;
  border-radius: 50%;
  /*border: 2px solid #fff; /* 给头像加白色边框区分背景 */
  object-fit: cover;
}

/* 卡片主体 */
.profile-card__body {
  text-align: center;
  padding: 40px 16px 20px; /* 顶部留出空间，避免头像被遮挡 */
  max-height: 120px;
  height: 120px;
}

/* 姓名/标题 */
.profile-card__name {
  margin: 0;
  font-size: 21px;
  font-weight: 600;
  color: var(--textColor2);
}

.profile-card__position {
  margin: 4px 0 16px;
  font-size: 14px;
  color: var(--textColor2);
}

/* 数据统计（Follower / Following / Total Post） */
.profile-card__stats {
  display: flex;
  flex-direction: column;
  justify-content: space-around;
  margin-bottom: 16px;
  gap: 12px; /* 增加子元素之间的间距 */
}
.profile-card__stat span {
  display: block;
  font-size: 12px;
  color: #999;
}

.profile-card__stat strong {
  display: block;
  font-size: 14px;
  color: var(--textColor2);
}

/* 社交图标区域 */
.profile-card__social i {
  font-size: 18px;
  margin: 0 6px;
  cursor: pointer;
}
</style>
