<template>
  <div class="body">
    <div class="container">
      <!-- 左侧编辑区域 -->
      <div class="left-panel">
        <div style="padding: 0 20px">
          <div style="height: 80px"></div>
          <div v-if="item.desc">
            <view class="title">{{ item.desc }}</view>
            <view class="dividingLine"></view>
          </div>
          <el-input
            class="textareaBox"
            type="textarea"
            :rows="10"
            :placeholder=item.placeholder
            v-model="item.body"
          />

          <div v-for="(item, index) in item.enterTheList" :key="index">
            <view class="illustrate">{{ item.name }}</view>
            <el-select
              v-if="item.type === 'select'"
              v-model="item.data"
              class="inputBox"
              placeholder="请选择"
              :teleported="false"
            >
              <el-option
                v-for="option in item.options"
                :key="option"
                :label="option"
                :value="option"
              />
            </el-select>
            <el-input
              v-else
              v-model="item.data"
              placeholder="请输入"
              class="inputBox"
            ></el-input>
          </div>

          <button @click="generate" class="determineTheBuild" type="primary">
            生成
          </button>
        </div>
      </div>

      <!-- 右侧预览区域 -->
      <div class="right-panel">
        <ViewState v-if="load" LoadText="正在生成，请稍后..." />
        <ViewState
          v-else-if="error"
          @ClickTheButton="back"
          Type="error"
          ErrorText="AI服务调用失败，正在紧急处理，请稍后使用。"
          IsShowBottom
          ButtonText="返回"
        />
        <div v-else class="preview-container" ref="scrollRef">
          <div class="preview-content">
            <div class="empty-state" v-if="!content">
              <div class="empty-icon">✨</div>
              <div class="empty-text">AI生成的内容将在这里显示</div>
              <div class="empty-desc">请在左侧输入您的需求并点击生成按钮</div>
            </div>
            <template v-else>
              <div class="title">{{ item.body }}</div>
              <div class="result">
                <div class="completeText">
                  <v-md-editor :model-value="content" mode="preview"></v-md-editor>
                </div>
                <div class="operation" v-if="content && !load">
                  <div @click="copyAnswer(content)" class="operationItem">
                    <el-icon size="14">
                      <CopyDocument/>
                    </el-icon>
                    <div class="operationExplain">复制结果</div>
                  </div>
                  <div
                    @click="onCollection"
                    class="operationItem operationItemSelected"
                    v-show="!isCollection"
                  >
                    <el-icon size="14">
                      <Star/>
                    </el-icon>
                    <div class="operationExplain">收藏</div>
                  </div>
                </div>
              </div>
            </template>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElNotification } from "element-plus";
import router from "@/router";
import { GetUserInfo, FavoritesAdd } from "../../../api/BSideApi";
import store from "@/store";
import ViewState from "@/components/ViewState.vue";
import { Star, CopyDocument } from "@element-plus/icons-vue";
import copy from "copy-to-clipboard";

export default {
  name: "CreateEditView",
  components: { ViewState, Star, CopyDocument },
  methods: {
    router() {
      return router;
    },
  },
  setup() {
    let router = useRouter();
    let item = ref(
      JSON.parse(decodeURIComponent(router.currentRoute.value.query.item))
    );
    let load = ref(false);
    let error = ref(false);
    let content = ref("");
    let socket = ref(null);
    let scrollRef = ref(null);
    let messages = ref([]);
    let isCollection = ref(false);

    function scrollToTheBottom() {
      setTimeout(
        () => scrollRef.value && (scrollRef.value.scrollTop = scrollRef.value.scrollHeight),
        20
      );
    }

    function webSocket(problemData) {
      if (typeof WebSocket == "undefined") {
        console.log("您的浏览器不支持WebSocket");
        return;
      }

      load.value = true;
      if (socket.value != null) {
        socket.value.close();
        socket.value = null;
      }

      // 重置消息列表
      messages.value = [];
      if (item.value.role) {
        messages.value.push({
          role: "system",
          content: item.value.role,
        });
      }
      messages.value.push({
        role: "user",
        content: problemData,
      });

      // 获取数量值
      let count = null;
      if (item.value.enterTheList) {
        const countField = item.value.enterTheList.find(field => field.name === "数量");
        count = countField ? countField.data : null;
      }

      socket.value = new WebSocket(
        process.env.VUE_APP_WSS +
          "/coze/api/" +
          localStorage.getItem("token") +
          "/" +
          item.value.model
      );

      socket.value.onopen = function () {
        socket.value.send(
          JSON.stringify({
            messages: messages.value,
            count: count
          })
        );
      };

      socket.value.onmessage = function (msg) {
        // 检查消息是否包含结束标志，如果包含则移除
        let messageData = msg.data;

        // // 如果消息为空或只包含空白字符，则不处理
        // if (!messageData || messageData.trim() === "") {
        //   return;
        // }

        // 收到非空消息时取消加载状态
        load.value = false;

        if (messageData.includes("[DONE]")) {
          messageData = messageData.replace("[DONE]", "");
        }
        if (messageData.includes("[END]")) {
          messageData = messageData.replace("[END]", "");
        }

        if (content.value) {
          content.value += messageData;
        } else {
          content.value = messageData;
        }

        scrollToTheBottom();
      };

      socket.value.onclose = function () {
        console.log("websocket已关闭");
        // 确保关闭时不再显示加载状态
        load.value = false;
        getUser();
      };

      socket.value.onerror = function () {
        ElNotification({
          title: "信息过期",
          message: "登录信息已过期,请重新登录",
          type: "error",
        });
        localStorage.removeItem("token");
        localStorage.removeItem("user");
        location.reload();
      };
    }

    function generate() {
      if (!item.value.body)
        return ElMessage({
          message: "请输入内容",
          type: "warning",
        });

      // 验证数量字段 - 只在数量字段存在且为空时显示警告
      if (item.value.enterTheList) {
        const countField = item.value.enterTheList.find(field => field.name === "数量");
        // 只有当找到数量字段且其值为空时才显示警告
        if (countField && countField.data === "") {
          return ElMessage({
            message: "请输入数量",
            type: "warning",
          });
        }
      }

      // 清空上一次生成的内容
      content.value = "";
      isCollection.value = false;

      getUser();
      let frequency = store.getters.userinfo.frequency;
      if (parseInt(frequency) <= 0) {
        ElNotification({
          title: "错误",
          message: "余额不足，请充值获得AI币(用户--充值)",
          type: "error",
        });
        return;
      }

      let problemData = item.value.body;

      if (item.value.enterTheList) {
        item.value.enterTheList.forEach(({ name, data }) => {
          problemData += "，";
          problemData += name;
          problemData += ":";
          problemData += data;
        });
      }

      webSocket(problemData);
    }

    async function getUser() {
      try {
        let res = await GetUserInfo();
        store.commit("setUserinfo", res);
      } catch (e) {
        console.log(e);
      }
    }

    function closeSocket() {
      if (socket.value) {
        socket.value.close();
        socket.value = null;
      }
    }

    // 复制答案
    function copyAnswer(data) {
      copy(data);
      ElNotification({
        message: "复制成功",
        type: "success",
      });
    }

    // 收藏功能
    async function onCollection() {
      if (!store.getters.userinfo) {
        ElNotification({
          message: "请先登录",
          type: "warning",
        });
        return;
      }

      try {
        await FavoritesAdd({
          issue: item.value.body,
          answer: content.value,
        });
        isCollection.value = true;
        ElNotification({
          message: "收藏成功",
          type: "success",
        });
      } catch (e) {
        ElNotification({
          message: e,
          type: "error",
        });
      }
    }

    onMounted(() => {
      // 组件卸载时关闭 WebSocket
      return () => {
        closeSocket();
      };
    });

    return {
      item,
      generate,
      load,
      error,
      content,
      scrollRef,
      copyAnswer,
      onCollection,
      isCollection,
    };
  },
};
</script>

<style scoped>
.body {
  scroll-behavior: smooth;
  width: 100%;
  height: 100vh;
  box-sizing: border-box;
  display: flex;
  overflow: hidden;
  background-color: var(--bgColor2);
}

.container {
  width: 100%;
  height: 100%;
  display: flex;
  box-sizing: border-box;
  max-width: 1600px;
  margin: 0 auto;
}

.left-panel {
  width: 50%;
  height: 100%;
  overflow-y: auto;
  border-right: 1px solid var(--borderColor);
  padding: 0 20px;
  box-sizing: border-box;
}

.right-panel {
  width: 50%;
  height: 100%;
  overflow-y: auto;
  padding: 20px;
  background-color: var(--bgColor2);
  box-sizing: border-box;
}

.preview-container {
  height: 100%;
  overflow-y: auto;
  max-width: 100%;
}

.preview-content {
  padding: 20px;
  max-width: 100%;
  box-sizing: border-box;
}

@keyframes explainAnimation {
  from {
    transform: scale(0);
  }
  to {
    transform: scale(1);
  }
}

.title {
  font-size: 18px;
  font-weight: bold;
  line-height: 1.4;
  color: var(--textColor1);
  margin-bottom: 15px;
  display: block;
  word-wrap: break-word;
  word-break: break-all;
  white-space: normal;
  overflow-wrap: break-word;
}

.dividingLine {
  width: 100%;
  height: 1px;
  background-color: var(--borderColor);
  margin-top: 10px;
  margin-bottom: 20px;
  display: block;
}

.illustrate {
  display: block;
  margin: 10px 0;
  color: var(--textColor1);
  font-size: 13px;
}
.el-select-dropdown__item {  background-color: var(--bgColor1); }

:deep(.inputBox > .el-input__wrapper) {
  box-shadow: none;
  width: 100%;
  height: 40px;
  border-radius: 8px;
  background-color: var(--bgColor1);
  color: var(--textColor1);
  font-size: 15px;
  box-sizing: border-box;
  padding: 0 15px;
  outline: none;
  margin-bottom: 10px;
}

:deep(.el-select) {
  width: 100%;
}

:deep(.el-select .el-input__wrapper) {
  background-color: var(--bgColor1);
  box-shadow: none;
  border: 1px solid var(--borderColor);
}

:deep(.el-select .el-input__wrapper:hover) {
  border-color: var(--themeColor1);
}

:deep(.el-select .el-input__wrapper.is-focus) {
  border-color: var(--themeColor1);
  box-shadow: 0 0 0 1px var(--themeColor1);
}

:deep(.el-select-dropdown) {
  background-color: var(--bgColor1);
  border: 1px solid var(--borderColor);
}

:deep(.el-select-dropdown__item) {
  color: var(--textColor1);
}

:deep(.el-select-dropdown__item.hover),
:deep(.el-select-dropdown__item:hover) {
  background-color: var(--themeColor1);
  color: var(--themeTextColor);
}

:deep(.el-select .el-input__inner) {
  color: var(--textColor1);
}

:deep(.el-select .el-input__inner::placeholder) {
  color: var(--textColor2);
}

:deep(.textareaBox > .el-textarea__inner) {
  width: 100%;
  border-radius: 7.5px;
  background-color: var(--bgColor1);
  color: var(--textColor1);
  font-size: 15px;
  outline: none;
  margin-bottom: 20px;
  box-shadow: none;
  box-sizing: border-box;
  padding: 15px;
}

.determineTheBuild {
  font-size: 15px;
  background-color: var(--themeColor1);
  color: var(--themeTextColor);
  border-radius: 100px;
  height: 40px;
  width: 300px;
  max-width: 100%;
  line-height: 40px;
  border: 0;
  display: table;
  margin: 50px auto 0 auto;
  cursor: pointer;
  opacity: 1;
}

.determineTheBuild:hover {
  background-color: var(--themeColor1);
  opacity: 0.8;
}

.result {
  min-height: 60px;
  width: 100%;
  background: var(--bgColor1);
  border-radius: 8px;
  flex-direction: column;
  display: flex;
  position: relative;
  box-shadow: 0 5px 7px rgb(0 0 0 / 6%);
}

.completeText {
  box-sizing: border-box;
  width: 100%;
  min-height: 28px;
  /* white-space: pre-wrap; */
  flex: 1;
  padding: 16px 20px;
  font-size: 16px;
  line-height: 28px;
  position: relative;
}

.operation {
  display: flex;
  margin-top: 20px;
  padding: 0 20px 20px;
}

.operationItem {
  height: 30px;
  cursor: pointer;
  box-sizing: border-box;
  padding: 0 15px;
  margin-right: 5px;
  display: flex;
  align-items: center;
  background-color: var(--bgColor2);
  border-radius: 100px;
  font-size: 13px;
  color: var(--textColor1);
}

.operationItemSelected {
  background-color: var(--themeColor1);
  color: var(--themeTextColor);
}

.operationExplain {
  margin-left: 5px;
}

:deep(.mdPreview > .vuepress-markdown-body) {
  padding: 0;
  background-color: var(--bgColor1);
}

:deep(.v-md-editor) {
  background-color: var(--bgColor1) !important;
}

:deep(.v-md-editor__preview) {
  background-color: var(--bgColor1) !important;
}

:deep(.vuepress-markdown-body) {
  color: var(--textColor1);
  background-color: var(--bgColor1) !important;
  padding: 0;
  line-height: 1.6;
}

:deep(.vuepress-markdown-body h1),
:deep(.vuepress-markdown-body h2),
:deep(.vuepress-markdown-body h3),
:deep(.vuepress-markdown-body h4),
:deep(.vuepress-markdown-body h5),
:deep(.vuepress-markdown-body h6) {
  margin-top: 24px;
  margin-bottom: 16px;
  font-weight: 600;
  line-height: 1.25;
  color: var(--textColor1);
}

:deep(.vuepress-markdown-body p) {
  margin-top: 0;
  margin-bottom: 16px;
  line-height: 1.6;
}

:deep(.vuepress-markdown-body code) {
  padding: 0.2em 0.4em;
  margin: 0;
  font-size: 85%;
  background-color: rgba(27, 31, 35, 0.05);
  border-radius: 3px;
  font-family: SFMono-Regular, Consolas, Liberation Mono, Menlo, monospace;
}

:deep(.vuepress-markdown-body pre) {
  padding: 16px;
  overflow: auto;
  font-size: 85%;
  line-height: 1.45;
  background-color: var(--bgColor2);
  border-radius: 3px;
}

:deep(.vuepress-markdown-body pre code) {
  padding: 0;
  margin: 0;
  font-size: 100%;
  word-break: normal;
  white-space: pre;
  background: transparent;
  border: 0;
}

:deep(.vuepress-markdown-body blockquote) {
  padding: 0 1em;
  color: var(--textColor2);
  border-left: 0.25em solid var(--borderColor);
  margin: 0 0 16px 0;
}

:deep(.vuepress-markdown-body ul),
:deep(.vuepress-markdown-body ol) {
  padding-left: 1.5em;
  margin-top: 0;
  margin-bottom: 16px;
}

:deep(.vuepress-markdown-body table) {
  display: block;
  width: 100%;
  overflow: auto;
  margin-top: 0;
  margin-bottom: 16px;
  border-spacing: 0;
  border-collapse: collapse;
}

:deep(.vuepress-markdown-body table th),
:deep(.vuepress-markdown-body table td) {
  padding: 6px 13px;
  border: 1px solid var(--borderColor);
}

:deep(.vuepress-markdown-body table tr) {
  background-color: var(--bgColor1);
  border-top: 1px solid var(--borderColor);
}

:deep(.vuepress-markdown-body table tr:nth-child(2n)) {
  background-color: var(--bgColor2);
}

:deep(.vuepress-markdown-body img) {
  max-width: 100%;
  box-sizing: border-box;
}

:deep(.vuepress-markdown-body a) {
  color: var(--themeColor1);
  text-decoration: none;
}

:deep(.vuepress-markdown-body a:hover) {
  text-decoration: underline;
}

:deep(.vuepress-markdown-body li) {
  margin-top: 4px;
  margin-bottom: 4px;
}

:deep(.vuepress-markdown-body li + li) {
  margin-top: 4px;
}

@media only screen and (max-width: 767px) {
  .container {
    flex-direction: column;
    padding: 0;
    position: relative;
  }

  .left-panel,
  .right-panel {
    width: 100%;
    height: 50vh;
    padding: 10px;
    position: relative;
  }

  .left-panel {
    border-right: none;
    border-bottom: 1px solid var(--borderColor);
    padding-bottom: 60px; /* 为固定按钮留出空间 */
  }

  .preview-content {
    padding: 10px;
  }

  .determineTheBuild {
    position: fixed;
    bottom: 10px;
    left: 50%;
    transform: translateX(-50%);
    width: calc(100% - 20px);
    max-width: 300px;
    z-index: 100;
    margin: 0;
    background-color: var(--themeColor1);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  }

  .determineTheBuild:hover {
    opacity: 0.9;
  }
}

@media only screen and (min-width: 768px) and (max-width: 1024px) {
  .container {
    padding: 0 10px;
  }

  .left-panel,
  .right-panel {
    padding: 10px;
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 300px;
  color: var(--textColor2);
}

.empty-icon {
  font-size: 32px;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 16px;
  margin-bottom: 8px;
}

.empty-desc {
  font-size: 14px;
  opacity: 0.8;
}
</style>
