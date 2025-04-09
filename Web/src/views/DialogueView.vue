<template>
  <div class="body" ref="scrollRef">
    <div v-if="!conversationList.length" class="explain">
      <img class="logo" alt="Vue logo" src="../assets/jingyun_ico.svg"/>
      <div class="expositoryCase">欢迎使用井云AI</div>
      <div class="consume">
        <el-icon>
          <Goods/>
        </el-icon>
        <div class="consumeText">每次提问消耗{{ store.getters.userinfo.gptFrequency }}个AI币</div>
      </div>
      <div class="beCareful">请注意不支持违法、违规等不当信息内容</div>
    </div>
    <div v-else class="questions" style="margin: 20px 0">
      <div
          v-for="(item, index) in conversationList"
          :key="index"
          class="item slide-animation"
      >
        <div v-if="!item.noDelete || item.noDelete[0]" class="question">
          <div>
            <div class="text">{{ item.user }}
              <img v-if="model==='VISION'&&item.img" :src="item.img" alt="Uploaded Image"
                   style="max-width: 100%; max-height: 100%; object-fit: cover;"/>
            </div>
            <div v-if="item.fileName&&(model==='PLUS'||model==='MOONSHOT')">
              <el-icon>
                <Document/>
              </el-icon>
              {{ truncateFileName(item.fileName, 13) }}
            </div>
            <div class="operation--model_user">
              <div class="op-btn"  @click="copyAnswer(item.user)">
                <el-icon>
                  <CopyDocument/>
                </el-icon>
                <text v-if="item.user.length >= 5" class="op-font">复制</text>
              </div>
              <div class="op-btn" @click="onDelete(index,0)">
                <el-icon>
                  <Delete/>
                </el-icon>
                <text v-if="item.user.length >= 5" class="op-font">删除</text>
              </div>
            </div>
          </div>
          <el-avatar
              class="flexShrink"
              :size="35"
              :icon="UserFilled"
              :src="
              store.getters.userinfo.avatar
                ? imageUrl + store.getters.userinfo.avatar
                : require('../assets/jingyun_ico.png')
            "
          />
        </div>
        <div v-if="!item.noDelete || item.noDelete[1]">
          <div class="answer">
            <el-avatar
                class="flexShrink"
                :size="35"
                :icon="UserFilled"
                :src="require('../assets/jingyun_ico.svg')"
            />
            <template v-if="item.assistant">
              <div style="width: 100%">
                <div
                    class="answer-data"
                    :style="{ maxWidth: calculateWidth(item.assistant.length-4) }"
                >
                  <v-md-editor
                      :model-value="item.assistant"
                      mode="preview"
                      @copy-code-success="handleCopyCodeSuccess"
                  />
                </div>

                <div class="operation--model" v-if="!item.isError">
                  <button class="op-btn" @click="copyAnswer(item.assistant)">
                    <el-icon>
                      <CopyDocument/>
                    </el-icon>
                    <text v-if="item.assistant?.length >= 5" class="op-font">复制</text>
                  </button>
                  <button
                      class="op-btn"
                      @click="onCollection(item, index)"
                      v-if="!item.isCollection"
                  >
                    <el-icon color="rgb(255,236,160)">
                      <StarFilled/>
                    </el-icon>
                    <text v-if="item.assistant?.length >= 5" class="op-font">收藏</text>
                  </button>
                  <button
                      class="op-btn"
                      @click="onDelete(index,1)"
                  >
                    <el-icon>
                      <Delete/>
                    </el-icon>
                    <text v-if="item.assistant?.length >= 5" class="op-font">删除</text>
                  </button>
                </div>
              </div>
            </template>
            <template v-else>
              <div class="answer-data" style="width: 100px">
                <div style="display: flex; padding: 5px 9px">
                  <div class="dot_0"></div>
                  <div class="dot_1"></div>
                  <div class="dot_2"></div>
                  <div class="dot_3"></div>
                  <div class="dot_4"></div>
                </div>
              </div>
            </template>
          </div>
        </div>

      </div>
    </div>
    <div class="suspend" v-show="aiLoading" @click="closeSocket">
      <el-icon :size="16">
        <VideoPause/>
      </el-icon>
      <div>暂停输出</div>
    </div>
    <div class="footer">
      <div class="footer-bar">
        <div class="model-actions">
          <div class="clear" style="margin-left: 0;top: -32%">
            <el-icon size="17px">
              <svg width="16" height="16" viewBox="0 0 30 30" fill="none" xmlns="http://www.w3.org/2000/svg"
                   xmlns:xlink="http://www.w3.org/1999/xlink">
                <path id="path"
                      d="M27.501 8.46875C27.249 8.3457 27.1406 8.58008 26.9932 8.69922C26.9434 8.73828 26.9004 8.78906 26.8584 8.83398C26.4902 9.22852 26.0605 9.48633 25.5 9.45508C24.6787 9.41016 23.9785 9.66797 23.3594 10.2969C23.2275 9.52148 22.79 9.05859 22.125 8.76172C21.7764 8.60742 21.4238 8.45312 21.1807 8.11719C21.0098 7.87891 20.9639 7.61328 20.8779 7.35156C20.8242 7.19336 20.7695 7.03125 20.5879 7.00391C20.3906 6.97266 20.3135 7.13867 20.2363 7.27734C19.9258 7.84375 19.8066 8.46875 19.8174 9.10156C19.8447 10.5234 20.4453 11.6562 21.6367 12.4629C21.7725 12.5547 21.8076 12.6484 21.7646 12.7832C21.6836 13.0605 21.5869 13.3301 21.501 13.6074C21.4473 13.7852 21.3662 13.8242 21.1768 13.7461C20.5225 13.4727 19.957 13.0684 19.458 12.5781C18.6104 11.7578 17.8438 10.8516 16.8877 10.1426C16.6631 9.97656 16.4395 9.82227 16.207 9.67578C15.2314 8.72656 16.335 7.94727 16.5898 7.85547C16.8574 7.75977 16.6826 7.42773 15.8193 7.43164C14.957 7.43555 14.167 7.72461 13.1611 8.10938C13.0137 8.16797 12.8594 8.21094 12.7002 8.24414C11.7871 8.07227 10.8389 8.0332 9.84766 8.14453C7.98242 8.35352 6.49219 9.23633 5.39648 10.7441C4.08105 12.5547 3.77148 14.6133 4.15039 16.7617C4.54883 19.0234 5.70215 20.8984 7.47559 22.3633C9.31348 23.8809 11.4307 24.625 13.8457 24.4824C15.3125 24.3984 16.9463 24.2012 18.7881 22.6406C19.2529 22.8711 19.7402 22.9629 20.5498 23.0332C21.1729 23.0918 21.7725 23.002 22.2373 22.9062C22.9648 22.752 22.9141 22.0781 22.6514 21.9531C20.5186 20.959 20.9863 21.3633 20.5605 21.0371C21.6445 19.752 23.2783 18.418 23.917 14.0977C23.9668 13.7539 23.9238 13.5391 23.917 13.2598C23.9131 13.0918 23.9512 13.0254 24.1445 13.0059C24.6787 12.9453 25.1973 12.7988 25.6738 12.5352C27.0557 11.7793 27.6123 10.5391 27.7441 9.05078C27.7637 8.82422 27.7402 8.58789 27.501 8.46875ZM15.46 21.8613C13.3926 20.2344 12.3906 19.6992 11.9766 19.7227C11.5898 19.7441 11.6592 20.1875 11.7441 20.4766C11.833 20.7617 11.9492 20.959 12.1123 21.209C12.2246 21.375 12.3018 21.623 12 21.8066C11.334 22.2207 10.1768 21.668 10.1221 21.6406C8.77539 20.8477 7.64941 19.7988 6.85547 18.3652C6.08984 16.9844 5.64453 15.5039 5.57129 13.9238C5.55176 13.541 5.66406 13.4062 6.04297 13.3379C6.54199 13.2461 7.05762 13.2266 7.55664 13.2988C9.66602 13.6074 11.4619 14.5527 12.9668 16.0469C13.8262 16.9004 14.4766 17.918 15.1465 18.9121C15.8584 19.9688 16.625 20.9746 17.6006 21.7988C17.9443 22.0879 18.2197 22.3086 18.4824 22.4707C17.6895 22.5586 16.3652 22.5781 15.46 21.8613ZM16.4502 15.4805C16.4502 15.3105 16.5859 15.1758 16.7568 15.1758C16.7949 15.1758 16.8301 15.1836 16.8613 15.1953C16.9033 15.2109 16.9424 15.2344 16.9727 15.2695C17.0273 15.3223 17.0586 15.4004 17.0586 15.4805C17.0586 15.6504 16.9229 15.7852 16.7529 15.7852C16.582 15.7852 16.4502 15.6504 16.4502 15.4805ZM19.5273 17.0625C19.3301 17.1426 19.1328 17.2129 18.9434 17.2207C18.6494 17.2344 18.3281 17.1152 18.1533 16.9688C17.8828 16.7422 17.6895 16.6152 17.6074 16.2168C17.5732 16.0469 17.5928 15.7852 17.623 15.6348C17.6934 15.3105 17.6152 15.1035 17.3877 14.9141C17.2012 14.7598 16.9658 14.7188 16.7061 14.7188C16.6094 14.7188 16.5205 14.6758 16.4541 14.6406C16.3457 14.5859 16.2568 14.4512 16.3418 14.2852C16.3691 14.2324 16.501 14.1016 16.5322 14.0781C16.8838 13.877 17.29 13.9434 17.666 14.0938C18.0146 14.2363 18.2773 14.498 18.6562 14.8672C19.0439 15.3145 19.1133 15.4395 19.334 15.7734C19.5078 16.0371 19.667 16.3066 19.7754 16.6152C19.8408 16.8066 19.7559 16.9648 19.5273 17.0625Z"
                      fill-rule="nonzero" fill="rgb(129,102,231)"></path>
              </svg>
            </el-icon>
            <div style="color: rgb(129,102,231);font-size: 13px;">DeepSeek-R1满血版</div>
          </div>
          <div class="clear action-button"
               :style="{
                background: model === 'BASIC-ALL' ? 'var(--themeColor2)' : 'var(--bgColor1)',
                color: model === 'BASIC-ALL' ? 'white' : 'var(--textColor4)'
              }"
               @click="toggleSearch" style="margin-left: 8px; padding: 3px 8px; top: -32%; font-size: 9px">
            <el-icon size="15px">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="10" cy="10" r="9" stroke="currentColor" stroke-width="1.8"></circle>
                <path d="M10 1c1.657 0 3 4.03 3 9s-1.343 9-3 9M10 19c-1.657 0-3-4.03-3-9s1.343-9 3-9M1 10h18"
                      stroke="currentColor" stroke-width="1.8"></path>
              </svg>
            </el-icon>
            <div>联网搜索</div>
          </div>
        </div>
        <InputFormField
            ref="inputRef"
            :aiLoading="aiLoading"
            :inputText="input"
            @update:inputText="input = $event"
            @update:model="model = $event"
            @onSubmit="onSubmit"
        />
        <div class="right-actions">
          <el-upload
              v-if="['VISION', 'PLUS' , 'MOONSHOT'].includes(model)"
              class="action-button"
              v-show="store.getters.userinfo && !aiLoading"
              type="primary"
              :before-upload="onChangeFileTemp"
              :accept="model === 'VISION' ? 'image/png, image/jpeg, image/gif' : '*'"
          >
            <div style="padding-top: 4px">
              <el-icon size="13px" style="padding-right: 3px">
                <Upload/>
              </el-icon>
            </div>
            <div :style="{ color: visionImage_url||file_url !== '' ? '#6DB4FD' : 'inherit' }">
              {{ visionImage_url || file_url !== '' ? '已上传' + fileName : '上传文件' }}
            </div>
          </el-upload>
          <div
              class="action-button"
              @click="clear"
              v-show="store.getters.userinfo && !aiLoading"
          >
            <el-icon size="13px">
              <Delete/>
            </el-icon>
            <div>清除聊天</div>
          </div>
          <div
              class="action-button"
              v-show="store.getters.userinfo && !aiLoading"
              @click="dialogueDisplay = true"
          >
            <el-icon size="13px">
              <ChatDotRound/>
            </el-icon>
            <div>新的会话</div>
          </div>
        </div>
        <div
            class="url-button"
            v-show="store.getters.userinfo && !aiLoading && false"
        >
          <div style="padding-top: 4px">
            <el-icon size="13px" style="padding-right: 3px">
              <Cloudy/>
            </el-icon>
          </div>
          <div>网址</div>

        </div>
        <!--
          封装后功能check
          1、下拉菜单可以切换模式
          2、文本域输入框可以输入内容
          3、文本域输入框可以提交内容
          4、状态更新可以监听
          5、文本域输入框可以清除内容
          6、文本域输入框可以聚焦

        -->
      </div>
    </div>
  </div>
  <el-dialog
      v-model="dialogueDisplay"
      title=""
      :width="isMobile ? '90%' : '430px'"
      center
      style="background-color: var(--bgColor1)"
  >
    <div>
      <div class="cache-flex-center">
        <img alt="Vue logo" src="../assets/jingyun_ico.svg" class="cache-img"/>
      </div>
      <div class="cache-text">井云AI</div>
      <div class="cache-flex-center cache-padding-top">
        <div class="cache-btn" @click="createdNewChat">
          <el-icon size="16px">
            <ChatLineSquare/>
          </el-icon>
          <div class="cache-btn-text">创建新的聊天</div>
        </div>
      </div>
      <div class="cache-content">
        <div class="cache-scrollbar">
          <el-scrollbar height="250px">
            <div
                class="cache-padding"
                v-for="(item, index) in dialogueCache.array"
                :key="index"
            >
              <div class="cache-flex-space-between">
                <div class="cache-message" @click="switchChat(index)">
                  <div class="cache-message-text">
                    {{ item.title }}
                  </div>
                  <div class="cache-message-time">
                    {{ conversionTime(item.time) }}
                  </div>
                </div>
                <div class="cache-selected">
                  <img
                      :src="
                      dialogueCache.index === index
                        ? require('../assets/selected.svg')
                        : require('../assets/close.svg')
                    "
                      class="cache-selected-img"
                      @click="clearDialogue(index)"
                      alt=""/>
                </div>
              </div>
            </div>
          </el-scrollbar>
        </div>
      </div>
    </div>
  </el-dialog>
  <LoginDialog :show="loginVisible" @close="loginVisible = false"/>
</template>

<script>
import {nextTick, onMounted, ref} from "vue";
import {
  Delete,
  Cloudy,
  ChatDotRound,
  ChatLineSquare,
  CopyDocument, Document,
  Goods,
  StarFilled, Upload,
  UserFilled,
  VideoPause,
} from "@element-plus/icons-vue";
import {ElNotification} from "element-plus";
import {
  FavoritesAdd, fileDelete_moonshot,
  fileUpload,
  fileUpload_moonshot,
  getContent_moonshot,
  GetUserInfo,
  imageUpload
} from "../../api/BSideApi";
import {useStore} from "vuex";
import LoginDialog from "@/components/LoginDialog.vue";
import InputFormField from "@/components/InputFormField.vue";
import store from "@/store";
import {conversionTime} from "@/utils/date";
import copy from 'copy-to-clipboard';

export default {
  name: "dialogueView",
  methods: {conversionTime},
  components: {
    Delete,
    Cloudy,
    Document,
    Upload,
    StarFilled,
    CopyDocument,
    InputFormField,
    ChatDotRound,
    ChatLineSquare,
    VideoPause,
    Goods,
    LoginDialog,
  },
  computed: {
    store() {
      return store;
    },
    UserFilled() {
      return UserFilled;
    },
  },
  setup() {
    let debug = true;
    let initialWidth = ref(100);
    let maxWidth = ref(740);
    let inputRef = ref(null);
    let store = useStore();
    let scrollRef = ref(null);
    let input = ref("");
    let conversationList = ref([]);
    let loginVisible = ref(false);
    let socket = ref(null);
    let aiLoading = ref(false);
    let model = ref("BASIC");
    const imageUrl = ref("");
    const file_url = ref("");
    let fileContent = "";
    let visionImage_url = ref("");
    const fileName = ref("");
    let dialogueDisplay = ref(false);
    const dialogueCache = ref({});
    const dialogueWidth = ref("30%");
    const rate = ref(50);
    const memory = ref(10);
    const size = ref(1000);
    const isMobile = ref(false);
    onMounted(() => {
      window.addEventListener("resize", handleResize);
      handleResize();
      if (store.getters.userinfo) getUser();
      //获取图片域名
      imageUrl.value = process.env.VUE_APP_IMAGE;
      rate.value = parseInt(process.env.VUE_APP_RATE);
      memory.value = parseInt(process.env.VUE_APP_MEMORY);
      size.value = parseInt(process.env.VUE_APP_MEMORY_SIZE);
      //获取对话缓存数据
      let item = localStorage.getItem("dialogueCache");
      if (store.getters.userinfo) {
        if (!store.getters.userinfo) return (loginVisible.value = true);
        if (item) {
          dialogueCache.value = JSON.parse(item);
          let value = dialogueCache.value;
          conversationList.value = value.array[value.index].context;
          // TODO 滚动到底部
          scrollToTheBottom();
        } else {
          dialogueCache.value = {
            index: 0,
            array: [
              {
                title: "新对话",
                time: Date.now(),
                context: conversationList.value,
              },
            ],
          };
          localStorage.setItem(
              "dialogueCache",
              JSON.stringify(dialogueCache.value)
          );
        }
      }
    });

    function toggleSearch() {
      model.value = model.value === 'BASIC-ALL' ? 'BASIC' : 'BASIC-ALL';
    }

    async function onChangeFileTemp(file) {
      const isImage = file.type === 'image/jpg' || file.type === 'image/png' || file.type === 'image/jpeg';
      const maxSize = this.model === 'VISION' ? 10 : 20;
      const isFileSizeValid = file.size / 1024 / 1024 <= maxSize;

      if (model.value === 'VISION' && (!isImage || !isFileSizeValid)) {
        ElNotification({
          message: `识图模式，请上传不超过${maxSize}MB的图片`,
          type: 'error',
        });
        return false;
      }
      if (model.value === 'PLUS' && !isFileSizeValid) {
        ElNotification({
          message: `请上传不超过${maxSize}MB的文件`,
          type: 'error',
        });
        return false;
      }
      // 文件验证通过后，进行文件上传
      const formData = new FormData();
      formData.append('file', file);

      // 处理上传成功后的逻辑，将返回的值赋给file_url或visionImage_url
      if (model.value === 'PLUS') {
        file_url.value = imageUrl.value + await fileUpload(formData);
      } else if (model.value === 'VISION') {
        visionImage_url.value = imageUrl.value + await imageUpload(formData);
      } else if (model.value === 'MOONSHOT') {
        let promise = await fileUpload_moonshot(formData);
        if (debug)
          console.log("上传状态:", promise);

        if (!promise || !promise.id) {
          ElNotification({
            title: "错误",
            message: "文件中未识别到文字",
            type: "error",
          });
        }
        let file_id = promise.id;
        promise = await getContent_moonshot(file_id);
        fileContent = promise.content;
        if (debug)
          console.log("文件内容:", promise);

        setTimeout(async () => {
          await fileDelete_moonshot(file_id);
          if (debug) {
            let stat = await fileDelete_moonshot(file_id);
            console.log("删除状态:", stat);
          }
        }, 5000); // 5000毫秒 = 5秒
      }

      fileName.value = file.name
      return false; // 返回false以阻止el-upload的默认上传行为
    }

    //限制文件名长度
    const truncateFileName = (fileName, maxLength) => {
      if (fileName.length <= maxLength) {
        return fileName;
      } else {
        const prefixLength = Math.ceil((maxLength - 3) / 2);
        const suffixLength = Math.floor((maxLength - 3) / 2);
        return `${fileName.slice(0, prefixLength)}...${fileName.slice(-suffixLength)}`;
      }
    };

    //提交内容的快捷键监听
    function handleKeyDown(e) {
      if (e.keyCode === 13) {
        onSubmit();
      }
    }

    //自适应窗口大小
    function handleResize() {
      if (window.innerWidth <= 768) {
        dialogueWidth.value = "70%";
        isMobile.value = true;
      } else {
        dialogueWidth.value = "30%";
        isMobile.value = false;
      }
    }

    // TODO 切换对话
    function switchChat(index) {
      dialogueCache.value.index = index;
      conversationList.value = dialogueCache.value.array[index].context;
      localStorage.setItem(
          "dialogueCache",
          JSON.stringify(dialogueCache.value)
      );
      dialogueDisplay.value = false;
    }

    function calculateWidth(textLength) {
      let width = initialWidth.value + textLength * 16;
      if (width >= maxWidth.value) {
        width = maxWidth.value;
      }
      return `${width}px`;
    }

    // TODO 清除指定对话
    function clearDialogue(index) {
      if (index !== dialogueCache.value.index) {
        let i = parseInt(dialogueCache.value.index);
        if (index < i) {
          dialogueCache.value.index = i - 1;
        }
        dialogueCache.value.array.splice(index, 1);
      }
      localStorage.setItem(
          "dialogueCache",
          JSON.stringify(dialogueCache.value)
      );
    }

    // TODO 写入对话数据
    function writeDialogue() {
      let item = conversationList.value;
      let value = dialogueCache.value;
      dialogueCache.value.array[value.index].time = Date.now();
      if (item.length > 0) {
        dialogueCache.value.array[value.index].title = item[
        item.length - 1
            ].user
            .trim()
            .slice(0, 25);
      }
      dialogueCache.value.array[value.index].context = item;
      localStorage.setItem(
          "dialogueCache",
          JSON.stringify(dialogueCache.value)
      );
    }

    // TODO 创建新对话
    function createdNewChat() {
      dialogueCache.value.array.unshift({
        title: "新对话",
        time: Date.now(),
        context: [],
      });
      dialogueCache.value.index = 0;
      conversationList.value = [];
      localStorage.setItem(
          "dialogueCache",
          JSON.stringify(dialogueCache.value)
      );
    }

    // TODO 提交问题
    async function onSubmit() {
      // 判断是否登录
      if (!store.getters.userinfo) {
        loginVisible.value = true;
        return;
      }
      if (model.value === "VISION") {
        // conversationList.value = [];
        if (visionImage_url.value === '') {
          ElNotification({
            title: "上传图片",
            message: "请先上传图片",
            type: "error",
          });
          return;
        }
      }
      console.log("父的提交，onSubmit()->input", input);
      console.log("父的提交，onSubmit()->input.value", input.value);
      // 去除空字符串 如果等于空直接 结束
      if (input.value.trim() === "") return;

      // 获取对话记录长度
      let index = conversationList.value.length;

      // 获取输入内容
      let content = input.value;
      // 清空内容
      // input.value = "";
      inputRef.value.resetInputValue();
      // 将对话内容push进整个会话

      let Added = {
        show: "",
        hidden: "",
        once: ""
      };// 附加内容
      if (model.value === "MOONSHOT") {
        Added.hidden = fileContent + "\n";
      } else if (model.value === "PLUS") {
        Added.hidden = file_url.value + "\n";
      }
      conversationList.value.push({
        once: Added.once,
        hidden: Added.hidden,
        user: Added.show + content,
        img: visionImage_url.value,
        fileName: fileName.value,
        noDelete: [true, true],
        assistantRaw: '',  // 新增字段，存储原始消息
        assistant: '',     // 已有的字段，存储处理后的消息
        isThinking: false  // 新增字段，跟踪思考状态
      });
      aiLoading.value = true;
      // TODO 滚动到底部
      scrollToTheBottom();
      // TODO 上下文
      let messages = [];
      conversationList.value
          .slice(-memory.value)
          .forEach(({once, hidden, noDelete, isError, user, assistant}, index, arr) => {
            if (!isError) {
              let truncatedUser = user
              let truncatedAssistant = assistant
              if (arr.length > 2) {
                if (index !== arr.length - 1 && index !== arr.length - 2) {
                  truncatedUser =
                      user.length > size.value ? user.slice(0, size.value) + "..." : user;
                  truncatedAssistant =
                      assistant && assistant.length > size.value
                          ? assistant.slice(0, size.value) + "..."
                          : assistant;
                }
              }
              if (!noDelete || noDelete[0]) {
                if (model.value === "VISION") {
                  messages.push({
                    role: "user",
                    content: [
                      {
                        type: "text",
                        text: once + hidden + truncatedUser
                      },
                      {
                        type: "image_url",
                        image_url: {
                          "url": visionImage_url.value
                        }
                      }
                    ]
                  });
                  // messages.push({
                  //     role: "user",
                  //     content: truncatedUser,
                  // });
                } else {
                  messages.push({
                    role: "user",
                    content: once + hidden + truncatedUser,
                  });
                }
              }

              if ((!noDelete || noDelete[1]) && truncatedAssistant && model.value !== "VISION") {
                messages.push({
                  role: "assistant",
                  content: truncatedAssistant,
                });
              }
            }
          });

      // 清除 visionImage_url 的值
      visionImage_url.value = '';
      file_url.value = '';
      fileContent = '';
      fileName.value = ''
      conversationList.value[index].once = "";

      webSocket({
        messages: {
          messages: messages,
        },
        index: index,
      });
    }

    function webSocket({messages, index}) {
      if (typeof WebSocket == "undefined") {
        console.log("您的浏览器不支持WebSocket");
      } else {
        if (socket.value != null) {
          socket.value.close();
          socket.value = null;
        }

        // 发起websocket

        console.log("发起websocket", model.value);

        socket.value = new WebSocket(
            process.env.VUE_APP_WSS +
            "/gpt-web/api/" +
            localStorage.getItem("token") +
            "/" +
            model.value
        );
        // TODO 建立连接
        socket.value.onopen = function () {
          socket.value.send(JSON.stringify(messages));
          console.log(JSON.stringify(messages))
          console.log(messages)
          conversationList.value[index].isError = true;
        };
        // TODO 接收消息
        socket.value.onmessage = function (news) {
          messageQueue.push({
            msg: news.data,
            index: index,
          }); // 将接收到的消息存储到队列中
          displayMessages(); // 显示消息
        };
        // TODO 关闭连接
        socket.value.onclose = function () {
          waitUntilMessageQueueClear(index);
        };
        // TODO 处理错误
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
    }

    const messageQueue = []; // 消息队列
    let isDisplaying = false; // 是否正在显示消息
    function displayMessages() {
      if (isDisplaying) {
        return; // 如果正在显示消息，则直接返回，等待下一次调用
      }
      isDisplaying = true;
      const message = messageQueue.shift(); // 取出队列中的第一个消息
      if (message) {
        let i = 0;

        // eslint-disable-next-line no-inner-declarations
        function displayNextCharacter() {
          const index = message.index;
          const msg = message.msg;
          const character = msg.charAt(i++);
          if (character) {
            // 累积原始消息
            if (conversationList.value[index].assistantRaw) {
              conversationList.value[index].assistantRaw += character;
            } else {
              conversationList.value[index].assistantRaw = character;
            }
            // 处理消息并更新 assistant
            conversationList.value[index].assistant = processMessage(conversationList.value[index].assistantRaw);
            scrollToTheBottom();
            setTimeout(displayNextCharacter, rate.value);
          } else {
            isDisplaying = false;
            displayMessages(); // 显示下一条消息
          }
        }

        displayNextCharacter();
      } else {
        isDisplaying = false; // 重置标志以便下次能够正确显示消息
      }
    }

    function waitUntilMessageQueueClear(index) {
      return new Promise((resolve) => {
        let interval = setInterval(() => {
          if (messageQueue.length === 0) {
            let assistant = conversationList.value[index].assistant;
            conversationList.value[index].isError = false;
            if (!assistant) {
              conversationList.value.splice(index, 1);
            }
            writeDialogue();
            getUser();
            aiLoading.value = false;
            // 滚动到底部
            scrollToTheBottom();
            nextTick(() => {
              // 这里修改为调用子组件的聚焦
              inputRef.value.$refs.inputRefInner.focus();
            });
            clearInterval(interval);
            resolve();
          }
        }, 50);
      });
    }

    async function getUser() {
      let res = await GetUserInfo();
      store.commit("setUserinfo", res);
    }

    // TODO 滚动到底部
    function scrollToTheBottom() {
      nextTick(() => {
        scrollRef.value.scrollTop = scrollRef.value.scrollHeight;
      });
    }

    // TODO 复制代码块
    function handleCopyCodeSuccess(code) {
      navigator.clipboard.writeText(code);
      ElNotification({
        message: "复制成功",
        type: "success",
      });
    }

    // TODO 复制答案

    function copyAnswer(data) {
      copy(data);
      ElNotification({
        message: "复制成功",
        type: "success",
      });
    }

    function closeSocket() {
      if (socket.value) {
        messageQueue.length = 0;
        socket.value.close();
        socket.value = null;
      }
    }

    function clear() {
      closeSocket();
      conversationList.value = [];
      writeDialogue();
    }

    async function onCollection(item, index) {
      try {
        let bol = !conversationList.value[index].isCollection;
        if (bol) {
          try {
            await FavoritesAdd({
              issue: item.user,
              answer: item.assistant,
            });
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
        conversationList.value[index].isCollection = true;
      } catch (e) {
        ElNotification({
          message: e,
          type: "error",
        });
      }
    }

    function onDelete(index, isAnswer) {
      if (conversationList.value[index].noDelete) {
        conversationList.value[index].noDelete[isAnswer] = false;
        if (!(conversationList.value[index].noDelete[0] || conversationList.value[index].noDelete[1])) {
          conversationList.value.splice(index, 1);
        }
      } else {
        conversationList.value[index].noDelete = [true, true];
        conversationList.value[index].noDelete[isAnswer] = false;
      }

    }

    function processMessage(message) {
      // 处理开始标签（优先处理，确保流式输出中的内容也能应用样式）
      let hasOpenTag = message.includes('<think>') && !message.includes('</think>');
      if (hasOpenTag) {
        // 如果有开始标签但没有结束标签，将整个后续内容都放在引用中
        let parts = message.split('<think>');
        if (parts.length > 1) {
          return parts[0] + '> 🤔思考中...\n> ' + parts[1].split('\n').join('\n> ');
        }
      }
      
      // 处理完整的标签
      message = message.replace(/<think>([\s\S]*?)<\/think>/g, function(match, content) {
        // 分割内容为行
        let lines = content.split('\n');
        // 给每行添加引用符号
        let quotedContent = lines.map(line => '> ' + line).join('\n');
        // 返回完整的引用块
        return '> 🤔思考中...\n' + quotedContent + '\n> 😊已深度思考';
      });
      
      // 处理结束标签
      message = message.replace(/<\/think>/g, '\n> 😊已深度思考');
      
      // 最后处理可能残留的开始标签
      message = message.replace(/<think>/g, '> 🤔思考中...\n> ');
      
      return message;
    }

    return {
      toggleSearch,
      handleKeyDown,
      inputRef,
      model,
      onSubmit,
      input,
      clear,
      conversationList,
      scrollRef,
      handleCopyCodeSuccess,
      loginVisible,
      onCollection,
      onDelete,
      copyAnswer,
      aiLoading,
      closeSocket,
      imageUrl,
      dialogueDisplay,
      dialogueCache,
      createdNewChat,
      switchChat,
      clearDialogue,
      dialogueWidth,
      calculateWidth,
      initialWidth,
      maxWidth,
      onChangeFileTemp,
      visionImage_url,
      file_url,
      fileName,
      truncateFileName,
      isMobile
    };
  },
};
</script>

<style lang="scss" scoped>
@keyframes beating {
  0% {
    transform: translateY(0);
  }

  100% {
    transform: translateY(-10px);
  }
}

@keyframes slideEase {
  0% {
    transform: translateX(-100px);
  }

  100% {
    transform: translateX(0);
  }
}

// 这里需要做媒体查询， 兼容小屏幕的对话框

.body {
  scroll-behavior: smooth;
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  flex-direction: column;
  flex: 1;
  align-items: center;
  padding: 0 20px 140px;
  display: flex;
  overflow: auto;
  background-color: var(--bgColor2);
}

.footer {
  width: 100%;
  box-sizing: border-box;
  z-index: 1;
  pointer-events: none;
  background: linear-gradient(rgba(246, 246, 246, 0), var(--bgColor2) 25%);
  flex-shrink: 0;
  padding: 30px 20px;
  display: flex;
  position: absolute;
  bottom: 0;
  overflow: hidden;
  flex-direction: column;
  align-items: center;
}

.footer-bar {
  position: relative;
  min-height: 60px;
  max-width: 800px;
  width: 100%;
  pointer-events: auto;
  background: var(--bgColor1);
  border-radius: 8px;
  box-shadow: 0 5px 7px rgb(0 0 0 / 6%);
  display: flex;
  align-items: center;
  animation: footerBarAnimation 0.3s;

  .model-actions {
    position: absolute;
    top: -26px;
    left: 0;
    display: flex;
    align-items: center;
    gap: 8px;
    z-index: 2;

    .clear {
      position: static; // 移除原有绝对定位
      margin: 0;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

      &:hover {
        transform: translateY(-1px);
      }
    }

    .action-button {
      transition: all 0.2s ease;
      color: var(--textColor4);
      font-size: 9px; // 统一基础字号
      .el-icon {
        color: inherit;
        font-size: 13px; // 图标尺寸与右侧按钮一致
      }

      div {
        font-size: 11px; // 文字尺寸与右侧按钮一致
        font-weight: 500; // 增加字重提升清晰度
        white-space: nowrap;
      }
    }
  }
}

.right-actions {
  position: absolute;
  right: 0px;
  top: -21%;
  transform: translateY(-50%);
  display: flex;
  gap: 8px;

  .action-button {
    display: flex;
    align-items: center;
    padding: 5px 10px;
    background: var(--bgColor1);
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.3s;
    box-shadow: 0 5px 7px rgb(0 0 0 / 6%);
    color: var(--textColor4);
    font-size: 9px;

    &:hover {
      background: var(--themeColor2);
    }

    .el-icon {
      margin-right: 5px;
    }

    div {
      font-size: 11px;
      white-space: nowrap;
    }
  }
}

@keyframes footerBarAnimation {
  from {
    transform: translateY(150%);
  }

  to {
    transform: translateY(0);
  }
}

.slide-animation {
  animation: slideEase 0.5s ease-in-out forwards;
}

:deep(.footer-bar > .el-input > .el-input__wrapper) {
  box-shadow: none;
  font-size: 16px;
  box-sizing: border-box;
  width: 100%;
  min-height: 60px;
  resize: none;
  -webkit-appearance: none;
  background: var(--bgColor1) 0 0;
  border: 0;
  flex: 1;
  margin: 0;
  padding: 16px 20px;
  line-height: 28px;
}

:deep(.footer-bar > .el-input > .el-input-group__prepend) {
  box-shadow: none;
  font-size: 16px;
  box-sizing: border-box;
  min-height: 60px;
  resize: none;
  -webkit-appearance: none;
  background: 0 0;
  border: 0;
  margin: 0;
  padding: 0;
  line-height: 28px;
}

:deep(.footer-bar > .el-input > .el-input-group__prepend > .el-select) {
  margin: 0 !important;
}

:deep(
    .footer-bar
      > .el-input
      > .el-input-group__prepend
      > .el-select
      > .select-trigger
      > .el-input
      > .el-input__wrapper
  ) {
  box-shadow: none !important;
  font-size: 15px;
  height: 62px;
  padding: 0 20px;

  background-color: var(--bgColor1);
}

.questions {
  width: 100%;
  max-width: 900px;
  box-sizing: border-box;
}

@media only screen and (max-width: 767px) {
  .questions {
    padding: 0;
  }
}

.questions > .item {
  border-radius: 8px;
  padding: 0 20px;
}

.flexShrink {
  flex-shrink: 0;
}

.question {
  display: flex;
  justify-content: right;
  align-items: flex-start;
  padding: 20px 8px;
}

.answer {
  padding: 20px 8px;
  display: flex;
  justify-content: left;
  align-items: flex-start;
}

.question > div > .text {
  max-width: 733px;
  min-width: 20px;
  padding: 5px 10px;
  border-radius: 5px;
  text-align: left;
  min-height: 28px;
  white-space: pre-wrap;
  margin-left: 46px;
  font-size: 16px;
  word-break: break-all;
  line-height: 28px;
  position: relative;
  background-color: var(--themeColor2);
  box-shadow: 0 5px 7px rgba(49, 79, 88, 0.15);
  color: var(--themeTextColor);
  margin-right: 10px;
  margin-top: 2px;
}

::v-deep(.vuepress-markdown-body) {
  padding: 0 0 0 16px;
  color: var(--textColor1);
  background-color: var(--bgColor1);
}

::v-deep(.scrollbar__wrap) {
  background-color: var(--bgColor1);
}

::v-deep(.vuepress-markdown-body tr:nth-child(2n)) {
  background-color: var(--bgColor1);
}

.operation--model {
  margin-top: 5px;
  display: flex;
  align-items: center;
  margin-left: 10px;
}

.operation--model_user {
  margin-top: 5px;
  display: flex;
  align-items: center;
  justify-content: right;
  margin-right: 5px;
}

.op-btn {
  box-shadow: 0 5px 7px rgb(0 0 0 / 6%);
  color: var(--textColor4);
  background-color: var(--bgColor1);
  margin-right: 5px;
  padding: 3px 10px;
  display: flex;
  align-items: center;
  border-radius: 3px;
  border: none;
}

.op-font {
  font-size: 10px;
  padding-left: 5px;
  // 添加过渡效果
  transition: all 0.2s ease;
}

.explain {
  margin: auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  animation: explainAnimation 0.3s;
  color: var(--textColor1);
}

@keyframes explainAnimation {
  from {
    transform: scale(0);
  }

  to {
    transform: scale(1);
  }
}

.suspend {
  animation: explainAnimation 0.3s;
  position: fixed;
  bottom: 150px;
  margin-top: 15px;
  display: flex;
  align-items: center;
  box-shadow: 0 5px 7px var(--bgboxShadowColor1);
  background-color: var(--bgColor1);
  padding: 5px 20px;
  font-size: 13px;
  color: var(--textColor1);
  border-radius: 5px;
}

.answer-data {
  box-shadow: 0 5px 7px rgb(0 0 0 / 6%);
  margin-left: 10px;
  border-radius: 5px;
  margin-top: 2px;
  overflow-x: hidden;
  background-color: var(--bgColor1);
  padding: 10px 10px 10px 5px;
  min-width: 20px;
  margin-right: 46px;
}

.suspend div {
  padding-bottom: 1px;
  padding-left: 8px;
}

.logo {
  width: 100px;
  margin-bottom: 20px;
  animation: beating 0.7s infinite alternate;
}

.expositoryCase {
  font-size: 20px;
  margin-top: 15px;
  text-align: center;
}

.consume {
  display: flex;
  align-items: center;
  margin-top: 30px;
}

.consumeText {
  margin-left: 10px;
  font-size: 15px;
}

.beCareful {
  padding: 40px 6px 12px;
  color: var(--textColor2);
  font-size: 15px;
  line-height: 1.6;
}

:deep(.answer > .el-avatar, .question > .el-avatar) {
  background-color: var(--bgColor2);
}

.clear {
  display: flex;
  align-items: center;
  position: absolute;
  top: 0;
  z-index: 1;
  font-size: 9px;
  border-radius: 5px;
  padding: 3px 10px;
  box-shadow: 0 5px 7px rgb(0 0 0 / 6%);
  color: var(--textColor4);
  background-color: var(--bgColor1);
}

.clear2, .upload-button {
  display: flex;
  align-items: center;
  position: absolute;
  top: 0;
  z-index: 1;
  font-size: 9px;
  color: var(--textColor4);
  background-color: var(--bgColor1);
  border-radius: 5px;
  padding: 3px 10px;
  margin-left: 92px;
  box-shadow: 0 5px 7px rgb(0 0 0 / 6%);
}

.url-button {
  display: flex;
  align-items: center;
  position: absolute;
  top: 0;
  z-index: 1;
  font-size: 9px;
  color: var(--textColor4);
  background-color: var(--bgColor1);
  border-radius: 5px;
  padding: 3px 10px;
  margin-left: 92px;
  box-shadow: 0 5px 7px rgb(0 0 0 / 6%);
}

.upload-button {
  margin-left: 184px;
}

.url-button {
  margin-left: 276px;
}

@media (max-width: 767px) {
  .right-actions {
    right: 20px;
    top: -14px;
    gap: 4px;
  }

  .right-actions .action-button {
    padding: 3px 8px;
  }

  .right-actions .action-button div {
    display: none;
  }

  .right-actions .action-button .el-icon {
    margin-right: 0;
    font-size: 15px;
  }

  .model-actions {
    top: -26px;
  }

  .model-actions .action-button {
    padding: 3px 8px;
  }

  .model-actions .action-button .el-icon {
    margin-right: 0;
    font-size: 15px;
  }

  .clear {
    padding: 3px 8px;
  }

  .clear .el-icon {
    margin-right: 0;
  }
}

.cache-flex-center {
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: var(--bgColor1);
}

.cache-img {
  width: 80px;
  height: 80px;
}

.cache-text {
  color: var(--textColor1);
  text-align: center;
  font-size: 15px;
  font-weight: 550;
  padding-top: 10px;
}

.cache-padding-top {
  padding-top: 15px;
}

.cache-btn {
  color: var(--themeTextColor);
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: var(--themeColor1);
  padding: 10px 30px;
  border-radius: 5px;
}

.cache-btn-img {
  width: 30px;
  height: 30px;
}

.cache-btn-text {
  padding-left: 10px;
  font-size: 12px;
}

.cache-content {
  padding: 20px 10px 10px;
}

.cache-scrollbar {
  background-color: var(--bgboxShadowColor1);
  border-radius: 10px;
  color: var(--textColor3);
}

.cache-padding {
  padding: 10px;
}

.cache-flex-space-between {
  display: flex;
  justify-content: space-between;
  margin: 10px 0;
}

.cache-message {
  padding-bottom: 4px;
  border-bottom: 1px #6b6b6b solid;
}

.cache-message-text {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 13px;
  color: var(--textColor1);
  width: 310px;
}

.cache-message-time {
  padding-top: 5px;
  font-size: 5px;
}

.cache-selected {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 40px;
}

.cache-selected-img {
  width: 16px;
  height: 16px;
}

.operation-user {
  justify-content: right;
  padding-right: 5px;
  margin-left: 0;
}

.animation-dot {
  display: flex;
  padding-right: 10px;
}

.select_style {
  width: 100px;
  margin-right: -20px;
}

.dot_0,
.dot_1,
.dot_2,
.dot_3 {
  background: rgb(166, 129, 236);
  width: 15px;
  height: 15px;
  border-color: #464646;
  border-radius: 50%;
}

.dot_0 {
  animation: jumpT 1.3s -0.64s linear infinite;
}

.dot_1 {
  animation: jumpT 1.3s -0.32s linear infinite;
}

.dot_2 {
  animation: jumpT 1.3s -0.16s linear infinite;
}

.dot_3 {
  animation: jumpT 1.3s linear infinite;
}

@keyframes jumpT {
  0%,
  80%,
  100% {
    transform: scale(0);
    background-color: #f9f9f9;
  }

  40% {
    transform: scale(1);
    background-color: rgb(186, 156, 241);
  }
}

@media (max-width: 767px) {
  .cache-message-text {
    width: 200px;
    font-size: 12px;
  }

  .cache-btn {
    padding: 8px 20px;
  }

  .cache-btn-text {
    font-size: 11px;
  }

  .cache-img {
    width: 60px;
    height: 60px;
  }

  .cache-text {
    font-size: 14px;
  }

  .cache-message-time {
    font-size: 10px;
  }

  .cache-selected-img {
    width: 16px;
    height: 16px;
  }
}

:deep(.vuepress-markdown-body blockquote) {
  font-size: 14px;
  color: var(--textColor2);
  background-color: var(--bgColor2);
  border-left: 4px solid var(--themeColor2);
  margin: 8px 0;
  padding: 8px 12px;
  border-radius: 4px;
}
</style>
