<template>
  <div class="s_container">
    <el-scrollbar height="100%">
      <el-form :inline="true" class="demo-form-inline" label-position="left">
        <el-form-item label="智能GPT对话请求链" label-width="200px">
          <el-input
            placeholder="请设置GPT请求链"
            clearable
            v-model="form.openAiUrl"
          />
        </el-form-item>
        <el-form-item label="增强GPT对话请求链" label-width="200px">
          <el-input
            placeholder="请设置增强GPT请求链"
            clearable
            v-model="form.openAiPlusUrl"
          />
        </el-form-item>
        <el-form-item label="智能GPT对话密钥" label-width="200px">
          <el-input placeholder="请设置密钥" clearable v-model="form.openKey" />
        </el-form-item>
        <el-form-item label="增强GPT对话密钥" label-width="200px">
          <el-input
            placeholder="请设置密钥"
            clearable
            v-model="form.openPlusKey"
          />
        </el-form-item>
        <el-form-item label="智能对话消耗次数" label-width="200px">
          <el-input
            placeholder="请设置消耗次数"
            clearable
            v-model="form.gptFrequency"
          />
        </el-form-item>
        <el-form-item label="增强对话消耗次数" label-width="200px">
          <el-input
            placeholder="请设置消耗次数"
            clearable
            v-model="form.gptPlusFrequency"
          />
        </el-form-item>
        <el-form-item label="新用户奖励次数" label-width="200px">
          <el-input
            placeholder="请设置奖励次数"
            clearable
            v-model="form.incentiveFrequency"
          />
        </el-form-item>
        <el-form-item label="签到奖励次数" label-width="200px">
          <el-input
            placeholder="请设置奖励次数"
            clearable
            v-model="form.signInFrequency"
          />
        </el-form-item>
      </el-form>

      <div class="btn-container">
        <el-button
          color="var(--themeColor2)"
          style="width: 200px"
          @click="onSubmit"
          >重载数据</el-button
        >
      </div>
    </el-scrollbar>
  </div>
</template>

<script>
import store from "@/store";
import { onMounted, ref } from "vue";
import { ElLoading, ElNotification } from "element-plus";
import { GetServerConfig, PutServerConfig } from "../../../../api/BSideApi";

export default {
  name: "DrawingSimplePanelView",
  computed: {
    store() {
      return store;
    },
  },
  setup() {
    const form = ref({
      openAiUrl: "https://api.openai.com/v1",
      openAiPlusUrl: "https://api.openai.com/v1",
      openKey: "sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
      openPlusKey: "sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
      gptFrequency: 1,
      gptPlusFrequency: 1,
      incentiveFrequency: 5,
      signInFrequency: 1
    });

    onMounted(() => {
      if (store.getters.userinfo && store.getters.userinfo.type === "ADMIN") {
        getServerConfig();
      }
    });

    async function getServerConfig() {
      let promise = await GetServerConfig();
      if (promise) {
        form.value = promise;
      }
    }

    async function onSubmit() {
      let value = form.value;
      // 只验证必填字段
      const requiredFields = [
        'openAiUrl',
        'openAiPlusUrl',
        'openKey',
        'openPlusKey',
        'gptFrequency',
        'gptPlusFrequency',
        'incentiveFrequency',
        'signInFrequency'
      ];

      for (let field of requiredFields) {
        if (!value[field] && value[field] !== 0) {
          ElNotification({
            title: "错误",
            message: field + "不能为空",
            type: "error",
          });
          return;
        }
      }

      try {
        ElLoading.service({
          fullscreen: true,
          text: "正在重载数据...",
          spinner: "el-icon-loading",
          background: "rgba(0, 0, 0, 0.7)",
        });
        await PutServerConfig(value);
        // 关闭加载图标
        ElLoading.service().close();
        // 弹出退出登录成功提示框
        ElNotification({
          message: "重载数据成功",
          type: "success",
        });
      } catch (e) {
        ElNotification({
          title: "错误",
          message: e,
          type: "error",
        });
      }
    }

    return {
      onSubmit,
      form,
    };
  },
};
</script>

<style scoped>
@keyframes explainAnimation {
  from {
    transform: scale(0);
  }

  to {
    transform: scale(1);
  }
}

::v-deep(
    .el-table--enable-row-hover .el-table__body tr:hover td.el-table__cell
  ) {
  background: none !important;
}

.btn-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding-right: 100px;
  padding-top: 70px;
}

::v-deep(.el-table-fixed-column--right) {
  background: var(--bgColor1) !important;
}

::v-deep(
    .el-table--enable-row-hover .el-table__body tr:hover td.el-table__cell
  ) {
  background: none;
}

</style>
