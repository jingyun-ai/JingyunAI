<template>
  <el-dialog
    v-model="dialogVisible"
    width="400px"
    class="qrcode-dialog"
    :show-close="true"
    :show-header="false"
    align-center
    @close="close"
  >
    <div class="qrcode-box">
      <div class="qrcode-title">{{ title }}</div>
      <div class="qrcode-container">
        <img
          :src="require('@/assets/qrcode.png')"
          class="qrcode-img"
          alt="二维码"
        />
      </div>
      <div class="qrcode-text">{{ text }}</div>
    </div>
  </el-dialog>
</template>

<script>
export default {
  name: 'QRCodeDialog',
  props: {
    show: {
      type: Boolean,
      default: false
    },
    title: {
      type: String,
      default: ''
    },
    text: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      dialogVisible: false
    }
  },
  watch: {
    show(newVal) {
      this.dialogVisible = newVal
    }
  },
  methods: {
    close() {
      this.$emit('close')
    }
  }
}
</script>

<style scoped>
.qrcode-box {
  padding: 20px;
  text-align: center;
  background-color: var(--bgColor1);
}

.qrcode-title {
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 20px;
  color: var(--textColor1);
}

.qrcode-container {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 20px;
}

.qrcode-img {
  width: 200px;
  height: 200px;
  border-radius: 8px;
  box-shadow: 0px 5px 30px var(--bgColor1);
}

.qrcode-text {
  font-size: 14px;
  color: var(--textColor1);
}

:deep(.el-dialog) {
  background-color: var(--bgColor1);
  border-radius: 10px;
  border: none;
}

:deep(.el-dialog__body) {
  padding: 0;
}

:deep(.el-dialog__headerbtn) {
  display: none;
}
</style> 