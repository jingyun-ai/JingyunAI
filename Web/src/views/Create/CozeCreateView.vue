<template>
  <div class="body">
    <div class="container">
      <div class="content">
        <h1 class="text-4xl font-bold text-zinc-900 lg:text-5xl lg:leading-[3.5rem]" style="color: var(--textColor2); margin-bottom: 0">数字员工创作中心</h1>
        <h4 class="text-4xl font-bold text-zinc-900 lg:text-5xl lg:leading-[3.5rem]" style="color: #6b6b6b; margin: 10px;margin-bottom: 40px;">选择创作类型，开启智能写作之旅</h4>
        <el-row :gutter="20" class="card-row">
          <el-col
            @click="onItem(item)"
            v-for="(item, index) in allItems"
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
                  :introduce="item.desc"
                />
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>
  </div>
  <LoginDialog :show="loginVisible" @close="loginVisible = false" />
</template>

<script>
import { ref, computed } from "vue";
import { useRouter } from "vue-router";
import { useStore } from "vuex";
import LoginDialog from "@/components/LoginDialog.vue";
import ProfileCard from "@/components/ProfileCard.vue";

export default {
  name: "CreateView",
  components: { LoginDialog, ProfileCard },
  setup() {
    let store = useStore();
    let router = useRouter();
    let menuCollection = ref(require("../../utils/CozeCreateData.json"));
    let loginVisible = ref(false);

    // 将所有类别的项目合并为一个扁平数组
    const allItems = computed(() => {
      let items = [];
      menuCollection.value.forEach(menu => {
        menu.list.forEach(item => {
          items.push(item);
        });
      });
      return items;
    });

    function onItem(item) {
      if (!store.getters.userinfo) return (loginVisible.value = true);
      router.push({
        path: "/creation_edit",
        query: {
          item: encodeURIComponent(JSON.stringify(item)),
        },
      });
    }

    return {
      menuCollection,
      allItems,
      onItem,
      loginVisible,
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
  color: var(--textColor1);
  font-size: 20px;
  font-weight: 500;
}

.content {
  width: 100%;
  margin-top: 30px;
  display: flex;
  flex-direction: column;
  align-items: center;
  max-width: 1600px;
  margin-left: auto;
  margin-right: auto;
  padding: 0 20px;
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
  }

  .card-row {
    gap: 20px;
  }

  .item {
    max-width: 100%;
    min-width: 100%;
  }
}
</style>
