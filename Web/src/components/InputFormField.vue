<template>
  <div style="width: 100%">
    <!--
    放到一起好改样式


    父组件使用代码
    <InputFormField
      ref="inputRef"
      :aiLoading="aiLoading"
      :inputText="input"
      @update:inputText="input = $event"
      @update:model="model = $event"
      @onSubmit="onSubmit"
    />

    1、重置文字   父组件代码
        inputRef.value.resetInputValue();
    2、输入框聚焦   父组件代码
        inputRef.value.$refs.inputRefInner.focus();

   -->
    <div class="InputFormFieldWapper">
      <el-select
        v-model="modelInner"
        v-if="needSelect"
        class="selectWrapper"
        placeholder="AI模型"
        @change="updateModel"
      >
        <el-option value="BASIC" label="智能" />
      </el-select>
      <el-input
        :style="{ marginLeft: needSelect ? '0px' : '10px' }"
        @keydown="handleKeyDown"
        v-model="inputTextInner"
        autosize
        @input="updateInputText"
        ref="inputRefInner"
        type="textarea"
        :placeholder="
          aiLoading ? '思考中..' : '输入你想问的...'
        "
        :disabled="aiLoading"
      >
      </el-input>
      <div class="animation-dot" v-if="aiLoading">
        <div class="dot0"></div>
        <div class="dot1"></div>
        <div class="dot2"></div>
        <div class="dot3"></div>
        <div class="dot4"></div>
      </div>
      <div @click="onSubmit" class="sendIcon" v-else>
        <el-icon :size="20">
          <Promotion />
        </el-icon>
      </div>
    </div>
  </div>
</template>

<script>
import { Promotion } from "@element-plus/icons-vue";
import { ref, defineComponent, watch } from "vue";

export default defineComponent({
  name: "InputFormField",
  components: {
    Promotion,
  },
  props: {
    needSelect: {
      type: Boolean,
      default: true,
    },
    // 选择的模型
    model: {
      type: String,
      default: null,
    },
    // 选择的模型
    inputText: {
      type: String,
      default: null,
    },
    // 加载状态
    aiLoading: {
      type: Boolean,
      default: false,
    },
  },
  setup(props, { emit }) {
    // 通过ref监听组件值
    let inputTextInner = ref(null);
    let modelInner = ref("BASIC");
    const inputRefInner = ref(null);

    //监听父的model 双向绑定需要
    watch(
      () => props.model,
      (newVal) => {
        modelInner = newVal;
      }
    );

    // 使用watch监听content变量的变化
    // eslint-disable-next-line no-unused-vars
    watch(inputTextInner, (newVal, oldValue) => {
      // console.log("watch输入内容改变了", newVal);
      emit("update:inputText", newVal);
    });

    // 重置值
    function resetInputValue() {
      if (inputRefInner.value) {
        inputRefInner.value.clear();
      }
    }

    //提交内容的快捷键监听
    function handleKeyDown(e) {
      if (e.keyCode === 13) {
        emit("onSubmit");
      }
    }

    //提交内容的快捷键监听
    function onSubmit() {
      // console.log("点击了提交onSubmit()");
      emit("onSubmit");
    }

    // 更新父model  选择智能还是标准
    function updateModel(value) {
      // console.log("更新父组件的model", value);
      modelInner = value;
      emit("update:model", value);
    }

    // 更新输入文本，
    function updateInputText(value) {
      // console.log("更新值", value);
      inputTextInner.value = value;
    }

    return {
      updateInputText,
      onSubmit,
      handleKeyDown,
      updateModel,
      resetInputValue,
      inputTextInner,
      modelInner,
      inputRefInner,
    };
  },
});
</script>

<style lang="scss" scoped>
.InputFormFieldWapper {
  display: flex;
  width: 100%;
  align-items: flex-start;

  .sendIcon {
    flex-shrink: 0;
    width: 36px;
    height: 36px;
    color: var(--themeTextColor);
    cursor: pointer;
    background: var(--themeColor2);
    border-radius: 50%;
    justify-content: center;
    align-items: center;
    display: flex;
    margin: 22px 20px 0 0;
  }
}

:deep(.selectWrapper) {
  width: 150px;
  .el-input {
    .el-input__wrapper {
      padding-top: 25px;
      padding-left: 25px;
      box-shadow: none !important;
      background: none !important;
      .el-input__inner {
        text-indent: 10px;
      }

      &:hover {
        box-shadow: none;
        background: none !important;
      }
    }
  }

  &.el-select--disabled {
    background: white;
    .el-input__wrapper {
      background: #fff;
    }
  }
}

:deep(.InputFormFieldWapper) {
  .el-textarea__inner {
    background: var(--bgColor1);
    box-shadow: none;
    max-height: 400px;
    padding: 20px;
    margin: 10px 0px;
    width: 100%;
    resize: none;

    color: var(--textColor3);
    &:hover {
      box-shadow: none;
      background: var(--bgColor1);
    }

    &.el-select--disabled {
      background: white;
    }
  }
}

.animation-dot {
  display: flex;
  padding-right: 5px;
  margin: 35px 12px 0 0;
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

.dot0,
.dot1,
.dot2,
.dot3 {
  background: rgb(166, 129, 236);
  width: 10px;
  height: 10px;
  border-color: #464646;
  border-radius: 50%;
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

.dot0 {
  animation: jumpT 1.3s -0.64s linear infinite;
}

.dot1 {
  animation: jumpT 1.3s -0.32s linear infinite;
}

.dot2 {
  animation: jumpT 1.3s -0.16s linear infinite;
}

.dot3 {
  animation: jumpT 1.3s linear infinite;
}

@media screen and (max-width: 756px) {
  :deep(.selectWrapper) {
    width: 80px;
    min-width: 88px;
    .el-input {
      .el-input__wrapper {
        .el-input__inner {
          text-indent: 0px;
        }
      }
    }
  }
}
</style>
