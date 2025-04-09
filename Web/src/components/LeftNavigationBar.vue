<template>
  <div class="NavigationBar">
    <div :class="controlDisplay?'leftNavigation':'leftNavigation_active'" v-if="isLeftMenu">
      <div class="header">
        <div class="switch-button">
          <router-link
              v-for="(item, index) in navigationList"
              :key="index"
              :class="['switch-item', { 'switch-active': isActive(item) }]"
              class="switch-item"
              :to="item.to"
          >
            <el-icon>
              <component :is="item.icon"/>
            </el-icon>
            <div class="switch-item-title">{{ item.title }}</div>
          </router-link>
        </div>
      </div>
      <div class="bottom">
        <el-avatar
            class="headPortrait"
            :size="70"
            :icon="UserFilled"
            :src="store.getters.userinfo.avatar
      ? imageUrl + store.getters.userinfo.avatar
      : require('../assets/jingyun_ico.svg')"
        />
        <div class="surplus" v-if="store.getters.userinfo">
          AI币 {{ store.getters.userinfo.frequency }}
        </div>
        <div v-else @click="loginVisible = true" class="loginBut">登录</div>
        <div class="dev-info">基于TIME-SEA框架</div>
      </div>
    </div>
    <div class="rightContent">
      <RouterView v-slot="{ Component }">
        <!-- TODO 要缓存 -->
        <KeepAlive>
          <component
              :is="Component"
              :key="$route.name"
              v-if="$route.meta.keepAlive"
          ></component>
        </KeepAlive>
        <!-- TODO 不缓存 -->
        <component
            :is="Component"
            :key="$route.name"
            v-if="!$route.meta.keepAlive"
        ></component>
      </RouterView>
    </div>
  </div>

  <LoginDialog :show="loginVisible" @close="loginVisible = false"/>
</template>

<script>
import {defineComponent, onMounted, ref, watch, markRaw} from "vue";
import {useRouter, useRoute} from "vue-router";
import {
  ChatDotSquare,
  DArrowLeft,
  MessageBox,
  ChatLineRound,
  ScaleToOriginal,
  Share,
  EditPen,
  Aim,
  UserFilled,
  Opportunity
} from "@element-plus/icons-vue";
import router from "@/router";
import store from "../store";
import LoginDialog from "@/components/LoginDialog.vue";

export default defineComponent({
  name: "LeftNavigationBar",
  components: {
    DArrowLeft,
    LoginDialog,
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
    let controlDisplay = ref(true)
    let router = useRouter();
    let route = useRoute();
    // TODO DATA
    let loginVisible = ref(false);
    let dialogVisible = ref(false);
    let appletDialogVisible = ref(false);
    let isHeadNavigation = ref(false);
    let navigationList = ref([
      {
        title: "智能问答",
        icon: markRaw(ChatDotSquare),
        to: "/chat",
      },
      {
        title: "文案创作",
        icon: markRaw(EditPen),
        to: "/creation"
      },
      {
        title: "商业思维",
        icon: markRaw(Opportunity),
        to: {path: "/laboratory", query: {type: "business_thinking"}}
      },
      {
        title: "账号定位",
        icon: markRaw(Aim),
        to: {path: "/laboratory", query: {type: "account_positioning"}}
      },
      {
        title: "全域推广",
        icon: markRaw(Share),
        to: {path: "/laboratory", query: {type: "global_promotion"}}
      },
      {
        title: "私域运营",
        icon: markRaw(ChatLineRound),
        to: {path: "/laboratory", query: {type: "private_operation"}}
      },
      {
        title: "我的收藏",
        icon: markRaw(MessageBox),
        to: "/collection",
      },
      {
        title: "更多角色",
        icon: markRaw(ScaleToOriginal),
        to: "/preset_character",
      },
    ]);
    const isLeftMenu = ref(true);

    watch(
        () => router.currentRoute.value,
        (newValue) => {
          isHeadNavigation.value = newValue.meta.isHeadNavigation;
          isLeftMenu.value = newValue.meta.isLeftMenu;
        },
        {
          immediate: true,
        }
    );
    const imageUrl = ref("");
    onMounted(() => {
      imageUrl.value = process.env.VUE_APP_IMAGE;
    });
    const dropdown1 = ref();

    function showClick() {
      dropdown1.value.handleOpen();
    }

    // 添加判断激活状态的函数
    function isActive(item) {
      if (typeof item.to === 'string') {
        return route.path === item.to;
      } else if (typeof item.to === 'object') {
        return route.path === item.to.path && route.query.type === item.to.query.type;
      }
      return false;
    }

    return {
      controlDisplay,
      isHeadNavigation,
      navigationList,
      UserFilled,
      showClick,
      dropdown1,
      appletDialogVisible,
      dialogVisible,
      loginVisible,
      imageUrl,
      isLeftMenu,
      isActive
    };
  },
});
</script>

<style lang="scss" scoped>
:deep(.answer) {
  .v-md-editor {
    background-color: transparent;
  }
}

:deep(.rightContent) {
  .footer {
    width: calc(100% - 10px);
  }
}
</style>

<style scoped>
.NavigationBar {
  width: 100%;
  height: 100%;
  display: flex;
  overflow: hidden;
}

.leftNavigation {
  width: 260px;
  height: 100%;
  background-color: var(--bgColor1);
  border-right: 1px solid var(--borderColor);
  position: relative;
}

.leftNavigation_active {
  width: 0;
  height: 100%;
  background-color: var(--bgColor1);
  border-right: 1px solid var(--borderColor);
  position: relative;
}

.bottom {
  position: absolute;
  bottom: 1%;
  left: 0;
  width: 100%;
  padding: 20px 0;
  border-top: 1px solid var(--borderColor);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
  box-sizing: border-box;
}

.headPortrait {
  display: block;
  margin: 50px auto 0;
}

.surplus {
  box-sizing: border-box;
  padding: 5px 10px;
  background-color: var(--themeColor1);
  display: table;
  margin: 20px auto 0;
  font-size: 13px;
  color: var(--themeTextColor);
  border-radius: 5px;
}

.rightContent {
  position: relative;
  flex: 1;
}

@media screen and (max-width: 756px) {
  ::v-deep(.rightContent .body) {
    padding-left: 0;
    padding-right: 0;
  }

}

.loginBut {
  width: 80px;
  height: 40px;
  background-color: var(--themeColor1);
  border-radius: 8px;
  margin: 20px auto 0;
  display: flex;
  justify-content: center;
  align-items: center;
  color: var(--themeTextColor);
  font-size: 14px;
  cursor: pointer;
}

.dev-info {
  margin-top: 10px;
  font-size: 12px;
  color: #7c7c7c;
  text-align: center;
}

.header {
  margin-top: 50px;
}

.switch-button {
  box-sizing: border-box;
  padding: 0 20px;
}

.switch-item {
  height: 50px;
  box-sizing: border-box;
  padding: 0 20px;
  display: flex;
  align-items: center;
  text-decoration: none;
  color: #666666;
  font-size: 14px;
  background-color: var(--bgColor1);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.switch-item-title {
  margin-left: 10px;
}

@media only screen and (max-width: 767px) {
  .NavigationBar {
    border: none;
    border-radius: 0;
    overflow: auto;
  }

  .leftNavigation {
    display: none;
  }
}
</style>
