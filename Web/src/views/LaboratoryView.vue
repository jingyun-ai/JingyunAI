<template>
  <div class="body">
    <div class="container">
      <div class="content">
        <h1 class="text-4xl font-bold text-zinc-900 lg:text-5xl lg:leading-[3.5rem]" style="color: var(--textColor2); margin-bottom: 0; margin-top: 30px;">{{ pageTitle }}</h1>
        <h4 class="text-4xl font-bold text-zinc-900 lg:text-5xl lg:leading-[3.5rem]" style="color: #6b6b6b; margin: 10px;margin-bottom: 40px;">{{ pageSubtitle }}</h4>

        <!-- 添加标签页切换组件 -->
        <el-tabs v-model="activeTab" class="custom-tabs" @tab-click="handleTabClick">
          <el-tab-pane label="全部" name="all"></el-tab-pane>
          <el-tab-pane label="商业思维" name="business_thinking"></el-tab-pane>
          <el-tab-pane label="账号定位" name="account_positioning"></el-tab-pane>
          <el-tab-pane label="全域推广" name="global_promotion"></el-tab-pane>
          <el-tab-pane label="私域运营" name="private_operation"></el-tab-pane>
        </el-tabs>

        <el-row :gutter="20" class="card-row">
          <el-col
              @click="toGpts(item)"
              v-for="(item, index) in filteredGptsCollection"
              :key="index"
              :xs="24"
              :sm="12"
              :md="8"
              :lg="6"
              :xl="4"
          >
            <div class="item">
              <div class="item-content">
                <profile-card
                    :backgroundImage="item.backgroundImage"
                    :icon="item.icon"
                    :name="item.title"
                    :position="item.name"
                    :introduce="item.introduce"
                />
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>
  </div>
  <LoginDialog :show="loginVisible" @close="loginVisible = false"/>
</template>

<script>
import {ref, onMounted, computed, watch} from "vue";
import {useRouter, useRoute} from "vue-router";
import {useStore} from "vuex";
import LoginDialog from "@/components/LoginDialog.vue";
import ProfileCard from "@/components/ProfileCard.vue";

export default {
  name: "LaboratoryView",
  components: {ProfileCard, LoginDialog},
  setup() {
    let store = useStore();
    let router = useRouter();
    let route = useRoute();
    let gptsCollection = ref(require("../utils/DigitalEmployees.json"));
    let loginVisible = ref(false);
    let currentType = ref('');
    let pageTitle = ref('选择你的数字员工');
    let pageSubtitle = ref('根据您当前主体下，历史触达人群、售卖商品、素材偏好、品牌调性、营销偏好等多维历史信息为您推荐');
    let activeTab = ref('all'); // 默认选中全部标签

    // 更新页面内容的函数
    function updatePageContent(type) {
      currentType.value = type;

      // 根据不同的type设置不同的标题和副标题
      if (!type || type === 'all') {
        pageTitle.value = '选择你的数字员工';
        pageSubtitle.value = '根据您当前主体下，历史触达人群、售卖商品、素材偏好、品牌调性、营销偏好等多维历史信息为您推荐';
        return;
      }

      switch(type) {
        case 'business_thinking':
          pageTitle.value = '商业思维工具';
          pageSubtitle.value = '帮助您建立系统的商业思维，提升决策能力';
          break;
        case 'account_positioning':
          pageTitle.value = '账号定位工具';
          pageSubtitle.value = '精准定位目标用户，挖掘市场机会';
          break;
        case 'global_promotion':
          pageTitle.value = '全域推广工具';
          pageSubtitle.value = '多渠道营销推广，实现业务快速增长';
          break;
        case 'private_operation':
          pageTitle.value = '私域运营工具';
          pageSubtitle.value = '打造高效私域运营体系，提升用户留存转化';
          break;
      }
    }

    // 处理标签点击事件
    function handleTabClick(tab) {
      const tabName = tab.props.name;
      // 首先更新当前类型，触发过滤
      if (tabName === 'all') {
        updatePageContent('all');
      } else {
        updatePageContent(tabName);
      }

      // 然后使用replace替代push来更新路由，避免增加历史记录
      if (tabName === 'all') {
        // 全部标签，清除路由参数
        router.replace({path: '/laboratory'});
      } else {
        // 其他标签，更新路由参数
        router.replace({path: '/laboratory', query: {type: tabName}});
      }
    }

    // 使用computed属性来过滤数据
    const filteredGptsCollection = computed(() => {
      if (!currentType.value || currentType.value === 'all') return gptsCollection.value;

      // 根据不同类型标签过滤
      return gptsCollection.value.filter(item => {
        const label = currentType.value === 'business_thinking' ? '商业思维' :
                     currentType.value === 'account_positioning' ? '账号定位' :
                     currentType.value === 'global_promotion' ? '全域推广' :
                     currentType.value === 'private_operation' ? '私域运营' : '';

        return item.label && item.label.includes(label);
      });
    });

    // 监听路由变化
    watch(
      () => route.query.type,
      (newType) => {
        if (newType) {
          updatePageContent(newType);
          activeTab.value = newType; // 同步更新标签选中状态
        } else {
          updatePageContent('all');
          activeTab.value = 'all'; // 没有type参数时选中全部标签
        }
      },
      { immediate: true }
    );

    onMounted(() => {
      const type = route.query.type;
      if (type) {
        updatePageContent(type);
        activeTab.value = type;
      } else {
        activeTab.value = 'all';
      }
    });

    function onItem(data) {
      if (!store.getters.userinfo) return (loginVisible.value = true);
      router.push({
        path: data,
      });
    }

    function toGpts(data) {
      if (!store.getters.userinfo) return (loginVisible.value = true);
      localStorage.setItem("roleData", JSON.stringify(data));
      router.push({
        path: "/custom",
      });
    }

    function toFastGPTs(data) {
      if (!store.getters.userinfo) return (loginVisible.value = true);
      localStorage.setItem("fastGPTsData", JSON.stringify(data));
      router.push({
        path: "/fastGPTs_view",
      });
    }

    return {
      gptsCollection,
      onItem,
      toGpts,
      toFastGPTs,
      loginVisible,
      currentType,
      pageTitle,
      pageSubtitle,
      filteredGptsCollection,
      activeTab,
      handleTabClick
    };
  },
};
</script>

<style scoped>
.body {
  scroll-behavior: smooth;
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  flex-direction: column;
  flex: 1;
  align-items: center;
  padding: 0 20px 120px;
  display: flex;
  overflow: auto;
  background-color: var(--bgColor2);
}


@keyframes explainAnimation {
  from {
    transform: scale(0);
  }

  to {
    transform: scale(1);
  }
}

.container {
  animation: explainAnimation 0.3s;
  max-width: 100%;
  width: 100%;
  box-sizing: border-box;
  padding: 0 20px 100px;
}

.list:first-child {
  margin-top: 60px;
}

.list {
  margin-bottom: 40px;
}

.title {
  font-size: 20px;
  font-weight: 500;
}

.content {
  width: 100%;
  margin-top: 60px;
  display: flex;
  flex-direction: column;
  align-items: center;
  max-width: 1600px;
  margin-left: auto;
  margin-right: auto;
  padding: 0 20px;
}

.custom-tabs {
  width: 100%;
  margin-bottom: 30px;
}

:deep(.el-tabs__item) {
  color: var(--textColor1);
  transition: all 0.3s ease;
}

:deep(.el-tabs__item.is-active) {
  color: var(--themeColor1);
  font-weight: bold;
}

:deep(.el-tabs__active-bar) {
  background-color: var(--themeColor1);
}

:deep(.el-tabs__nav-scroll) {
  backdrop-filter: blur(3px); /* 添加背景模糊效果 */
  position: fixed;
  left: 17%;
  top: 6%;
  height: 4%;
  z-index: 1; /* 确保它显示在其他元素上方 */
  border-radius: 10px;
  padding: 6px;
  transition: all 0.3s ease;
}

.card-row {
  width: 100%;
  margin: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 30px;
  justify-content: center;
}

.card-row :deep(.el-col) {
  padding: 0;
  margin-bottom: 30px;
  display: flex;
  justify-content: center;
}

.item {
  width: 100%;
  height: 100%;
  max-width: 320px;
  min-width: 280px;
}

.item-content {
  position: relative;
  width: 100%;
  height: 100%;
}

.item:hover {
  cursor: pointer;
  transition: all 0.3s ease;
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

@media only screen and (max-width: 767px) {
  .container {
    padding: 0;
  }

  .list:first-child {
    margin-top: 30px;
  }

  .list {
    padding-left: 20px;
    padding-right: 20px;
  }

  .content {
    padding-left: 20px;
    padding-right: 20px;
    box-sizing: border-box;
    margin-top: 80px; /* 为标签栏留出空间 */
  }

  .card-row {
    gap: 20px;
  }

  .item {
    max-width: 100%;
    min-width: 100%;
  }

  :deep(.el-tabs__nav-scroll) {
    left: 5%;
    overflow-x: auto;
    max-width: 90%;
    height: 6%;
    top: 7%;
  }
}
</style>
