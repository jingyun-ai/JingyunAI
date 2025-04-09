<template>
  <nav class="header">
    <div class="header-side">
      <div>
        <div style="display: flex; align-items: center;">
          <img class="applet-icon" :src="require('../assets/jingyun.png')"/>
          <div class="applet-text themeColorText">井云AI数字员工</div>
        </div>
      </div>
    </div>
    <div v-if="isHeadNavigation" class="header-center">
      <div class="switch-button">
        <template v-for="(item, index) in navigationList" :key="index">
          <router-link
              v-if="!isDesktop"
              active-class="switch-active"
              class="switch-item"
              :to="item.to"
          >{{ item.title }}
          </router-link>
          <a
              v-else
              class="external-link"
              :href="item.href"
              target="_blank"
              rel="noopener noreferrer"
          >{{ item.title }}</a
          >
        </template>
      </div>
    </div>

    <div
        class="header-side header-right"
        style="display: flex; align-items: center"
    >
      <div @click="switchTheme" class="hidden-xs-only">
        <el-button text class="switchThemeIconWrapper">
          <template v-slot:icon>
            <el-icon
                class="IconInner Sunny"
                :class="{
                show: store.getters.themeInfo.className === 'lightMode',
              }"
                size="16"
            >
              <Sunny
              />
            </el-icon>
            <el-icon
                class="IconInner Moon"
                :class="{
                show: store.getters.themeInfo.className !== 'lightMode',
              }"
                size="16"
            >
              <Moon
              />
            </el-icon>
          </template>
        </el-button>
      </div>

      <div
          v-if="store.getters.userinfo"
          style="display: flex; align-items: center"
      >
        <div
            @click="
            router().push({
              path: '/purchase',
            })
          "
            class="hidden-xs-only"
        >
          <el-button text
          >充值

            <template v-slot:icon>
              <el-icon size="16">
                <Goods/>
              </el-icon>
            </template>
          </el-button>
        </div>
        <div
            @click="showExchangeCodeQRCode"
            class="hidden-xs-only"
        >
          <el-button text
          >兑换码

            <template v-slot:icon>
              <el-icon size="16">
                <Ticket/>
              </el-icon>
            </template>
          </el-button>
        </div>
        <div class="header-right">
          <div class="header-user-wrapper">
            <el-dropdown ref="dropdown1" trigger="contextmenu">
              <div @click="showClick" class="header-user-btn">
                <el-avatar
                    :size="28"
                    :icon="UserFilled"
                    :src="
                    store.getters.userinfo.avatar
                      ? imageUrl + store.getters.userinfo.avatar
                      : require('../assets/jingyun_ico.svg')
                  "
                />
                <div class="header-user-name">
                  {{
                    store.getters.userinfo.userName
                        ? store.getters.userinfo.userName
                        : "我的"
                  }}
                </div>
              </div>
              <template #dropdown>
                <el-dropdown-menu class="dropdown-menu">
                  <el-dropdown-item
                      @click="router().push({ path: '/Admin' })"
                      v-if="store.getters.userinfo.type === 'ADMIN'"
                  >控制台
                  </el-dropdown-item
                  >
                  <el-dropdown-item @click="router().push({ path: '/Purchase' })"
                  >余额充值
                  </el-dropdown-item
                  >
                  <el-dropdown-item @click="router().push({ path: '/Orders' })"
                  >充值记录
                  </el-dropdown-item
                  >
                  <el-dropdown-item @click="router().push({ path: '/Collection' })"
                  >我的收藏
                  </el-dropdown-item
                  >
                  <el-dropdown-item
                      @click="router().push({ path: '/Exchange' })"
                  >兑换中心
                  </el-dropdown-item
                  >
                  <el-dropdown-item @click="switchTheme"
                  >切换到{{
                      store.getters.themeInfo.switchText
                    }}
                  </el-dropdown-item
                  >
                  <el-dropdown-item divided @click="logout"
                  >退出登录
                  </el-dropdown-item
                  >
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
      <div v-else @click="dialogVisible = true" class="login">登录</div>
    </div>
  </nav>

  <el-dialog
      v-model="appletDialogVisible"
      title="井云AI"
      width="300"
      center
      align-center
  >
    <div class="wxAppletCodeRow">
      <img class="wxAppletCode" :src="require('../assets/wxAppletCode.jpg')"/>
      <div>微信扫一扫</div>
    </div>
  </el-dialog>

  <LoginDialog :show="dialogVisible" @close="dialogVisible = false"/>
  <QRCodeDialog
      :show="showQRCode"
      :title="qrCodeTitle"
      :text="qrCodeText"
      @close="closeQRCode"
  />
</template>
<script>
import {defineComponent, onMounted, ref, watch} from "vue";
import {useRouter} from "vue-router";
import {UserFilled, Goods, Ticket} from "@element-plus/icons-vue";
import router from "@/router";
import store from "../store";
import LoginDialog from "@/components/LoginDialog.vue";
import {ElLoading, ElNotification} from "element-plus";
import {Logout} from "../../api/BSideApi";

import {useStore} from "vuex";
import QRCodeDialog from "@/components/QRCodeDialog.vue";

export default defineComponent({
  name: "NavigationBar",
  components: {
    Ticket,
    LoginDialog,
    QRCodeDialog,
  },
  computed: {
    store() {
      return store;
    },
  },
  methods: {
    router() {
      return router;
    },
  },
  setup() {
    let router = useRouter();
    // TODO DATA
    let dialogVisible = ref(false);
    let appletDialogVisible = ref(false);
    let isHeadNavigation = ref(false);
    const imageUrl = ref("");
    let store = useStore();
    let isDesktop = ref(window.innerWidth > 768);
    let showQRCode = ref(false);
    let qrCodeTitle = ref("");
    let qrCodeText = ref("");

    onMounted(() => {
      //获取图片域名
      imageUrl.value = process.env.VUE_APP_IMAGE;

      // 添加窗口大小变化监听
      window.addEventListener('resize', () => {
        isDesktop.value = window.innerWidth > 768;
        updateNavigationList();
      });

      // 初始化导航列表
      updateNavigationList();
    });

    let navigationList = ref([]);

    // 根据设备类型更新导航列表
    function updateNavigationList() {
      if (isDesktop.value) {
        navigationList.value = [
          {
            title: "官网",
            href: "https://jingyun.ai/",
          },
          {
            title: "创作",
            href: "https://jingyuncenter.com/",
          },
          {
            title: "数字人",
            href: "https://www.zizuoai.com/",
          },
          {
            title: "文档",
            href: "https://acnq1rombfk4.feishu.cn/wiki/space/7475524021124497436",
          },
        ];
      } else {
        navigationList.value = [
          {
            title: "智能",
            to: "/chat",
          },
          {
            title: "编导",
            to: "/creation",
          },
          {
            title: "员工",
            to: "/laboratory",
          },
          {
            title: '更多',
            to: "/preset_character",
          },
        ];
      }
    }

    watch(
        () => router.currentRoute.value,
        (newValue) => {
          isHeadNavigation.value = newValue.meta.isHeadNavigation;
        },
        {
          immediate: true,
        }
    );

    const dropdown1 = ref();

    function switchTheme() {
      store.commit(
          "setThemeSwitchIndex",
          store.getters.themeInfo.id === 0 ? 1 : 0
      );
    }

    function showClick() {
      dropdown1.value.handleOpen();
    }

    async function logout() {
      try {
        // 显示加载图标
        ElLoading.service({
          fullscreen: true,
          text: "正在退出登录...",
          spinner: "el-icon-loading",
          background: "rgba(0, 0, 0, 0.7)",
        });
        await Logout();
        localStorage.removeItem("token");
        localStorage.removeItem("user");
        location.reload();
        // 关闭加载图标
        ElLoading.service().close();
        // 弹出退出登录成功提示框
        ElNotification({
          message: "退出登录成功",
          type: "success",
        });
      } catch (e) {
        // 关闭加载图标
        ElLoading.service().close();
        ElNotification({
          message: e,
          type: "error",
        });
      }
    }

    function showExchangeCodeQRCode() {
      qrCodeTitle.value = '兑换码激活';
      qrCodeText.value = '添加客服获取兑换码';
      showQRCode.value = true;
    }

    function closeQRCode() {
      showQRCode.value = false;
    }

    return {
      logout,
      isHeadNavigation,
      navigationList,
      UserFilled,
      Goods,
      Ticket,
      showClick,
      dropdown1,
      appletDialogVisible,
      dialogVisible,
      imageUrl,
      switchTheme,
      isDesktop,
      showQRCode,
      qrCodeTitle,
      qrCodeText,
      showExchangeCodeQRCode,
      closeQRCode,
    };
  },
});
</script>

<style lang="scss" scoped>
/* 新增的APPlet样式 */
.applet-container {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  height: 34px;
  border-radius: 6px;
  background: var(--themeColor1);
  cursor: pointer;
  transition: all 0.3s;
  line-height: 1;

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
}

.applet-icon {
  width: 16%;
  height: 16%;
  object-fit: contain;
  flex-shrink: 0;
}

.applet-text {
  font: 600 15px/1 'Microsoft YaHei', 'Segoe UI', cursive;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.1);
  white-space: nowrap;
  letter-spacing: 0.5px;
  margin-left: 7px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .applet-container {
    padding: 4px 8px;
    height: 30px;
  }
  .applet-text {
    font-size: 13px;
  }
  .applet-icon {
    width: 16%;
    height: 16%;
  }
}

.header {
  user-select: none;
  height: 60px;
  box-sizing: border-box;
  width: 100%;
  border-bottom: 1px solid var(--borderColor);
  flex-shrink: 0;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
  display: flex;
  position: relative;
  overflow: hidden;
}

.header-center {
  position: absolute;
  left: 50%;
  transform: translate(-50%);
}

@media only screen and (max-width: 767px) {
  .header-center {
    left: 16px;
    transform: translate(0);
  }
}

.switch-button {
  color: var(--textColor1);
  box-sizing: border-box;
  height: 34px;
  background: var(--bgColor3);
  border-radius: 7px;
  align-items: center;
  padding: 0 2px;
  display: flex;
}

.switch-item {
  height: 30px;
  cursor: pointer;
  border-radius: 5px;
  justify-content: center;
  align-items: center;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  color: var(--textColor1);
  text-decoration: none;
  box-sizing: border-box;
  padding: 0 16px;
}

.switch-active {
  background: var(--bgColor1);
}

.header-side {
  height: 100%;
  align-items: center;
  display: flex;
}

.logo {
  cursor: pointer;
  font-size: 20px;
  font-weight: bold;
  color: var(--el-text-color-primary);
  text-decoration: none;
  animation: logoAnimation 0.3s;
}

@keyframes logoAnimation {
  from {
    transform: translateX(-100%);
  }

  to {
    transform: translateX(0);
  }
}

:deep(.header-right) {
  animation: headerRightAnimation 0.3s;

  .el-button {
    background: var(--bgColor1);
    margin-right: 10px;

    &.switchThemeIconWrapper {
      padding: 8px;
      border-radius: 100%;
      width: 18px;
      height: 18px;
      position: relative;
      user-select: none;
      box-sizing: content-box;

      &:hover {
        box-shadow: 0px 0px 16px -2px var(--bgColor3);
        color: var(--themeColor1);
      }

      .IconInner {
        position: absolute;
        top: 50%;
        left: 50%;
        margin-left: -50%;
        margin-top: -50%;
        opacity: 0;

        &.Sunny {
          transform: translateX(-50px);
        }

        &.Moon {
          transform: translateX(50px);
        }

        &.show {
          opacity: 1;
          transform: translateX(0px);
        }
      }
    }

    transition: all 0.2s;
  }

  .el-button.is-text:not(.is-disabled):focus {
    background: var(--bgColor1);
  }

  .el-button.is-text:not(.is-disabled):hover {
    background: var(--bgColor1);
    /* opacity: 0.8; */
  }
}

.rechargeButton {
  width: 84px;
  height: 34px;
  border-radius: 6px;
  margin-right: 12px;
  padding: 0;
  font-size: 14px;
  background-color: var(--bgColor2);
  color: var(--textColor2);
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
}

.rechargeButtonText {
  margin-left: 10px;
}

.themeColorText {
  color: var(--textColor2);
}

@keyframes headerRightAnimation {
  from {
    transform: translateX(100%);
  }

  to {
    transform: translateX(0);
  }
}

.header-user-name {
  max-width: 120px;
  white-space: nowrap;
  text-overflow: ellipsis;
  text-align: center;
  word-break: break-all;
  font-size: 14px;
  overflow: hidden;
}

.dropdown-menu {
  width: 150px;
  box-sizing: border-box;
  padding: 6px;
}

.applet {
  width: 120px;
  margin-left: 10px;
  background-color: var(--themeColor1);
  color: var(--themeTextColor);
}

.applet > .rechargeButtonText {
  margin-left: 5px;
}

/*.appletIcon {
  width: 25%;
  height: 25%;
}*/

.wxAppletCodeRow {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.wxAppletCode {
  width: 150px;
  height: 150px;
  margin: 20px 0;
}

.login {
  width: 72px;
  height: 34px;
  background: var(--themeColor1);
  border-radius: 6px;
  padding: 0;
  font-size: 14px;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
  color: var(--themeTextColor);
}

.header-user-btn {
  height: 36px;
  cursor: pointer;
  border-radius: 6px;
  align-items: center;
  padding: 0 6px;
  display: flex;
}

.header-user-name {
  max-width: 120px;
  white-space: nowrap;
  text-overflow: ellipsis;
  word-break: break-all;
  margin-left: 8px;
  font-size: 15px;
  overflow: hidden;
  color: var(--textColor2);
}

.header-user-btn:hover {
  background: var(--bgColor2);
}

.external-link {
  margin: 0 12px;
  text-decoration: none;
  color: var(--textColor1);
  font-size: 14px;
  font-weight: 500;
}
</style>
<style>
.el-dialog__header {
  display: none !important;
}

.el-dialog__body {
  background: var(--bgColor1) !important;
}
</style>