<template>
    <div>
        <!--    登录或注册-->
        <el-dialog
                v-model="dialogVisible"
                width="420px"
                class="login-dialog"
                :show-close="false"
                align-center
                @close="close"
                style="border-radius: 10px; overflow-x: hidden"
        >
            <div class="login-box">
                <div class="head">
                    <div class="head_img"></div>
                </div>
                <div class="login-title">
                    <div
                            :class="loginType === 1 ? 'login-selected' : ''"
                            @click="switchLoginType(1)"
                    >
                        {{ isLogin ? "密码登录" : "注册账号" }}
                    </div>
                </div>
                <!--微信扫码登录-->
                <div v-if="loginType === 0" class="wechat-login-container">
                    <div class="form">
                        <div class="animation" v-if="!qrCodeLoaded">
                            <view class="loading-model">
                                <view class="loader"></view>
                            </view>
                        </div>
                        <div class="content" v-if="qrCode">
                            <div class="qr-code-container">
                                <img
                                        :src="qrCode"
                                        class="qc_code"
                                        :class="store.getters.themeInfo.className"
                                        alt="二维码"
                                />
                                <div class="cover-div" v-if="isFailure">二维码已失效</div>
                            </div>
                        </div>
                    </div>
                    <div class="btn-generate" v-if="isFailure">
                        <el-button
                                type="primary"
                                color="var(--themeColor2)"
                                @click="getLoginQRCode()"
                        >重新生成
                        </el-button>
                    </div>
                    <div class="h5 prompt-style" v-if="!loginAnimation">
                        正在加载中...
                    </div>
                    <div class="h5 prompt-style" v-if="!promptAnimation">
                        使用微信扫一扫快速登录后使用
                    </div>
                </div>
                <!--登录-->
                <div v-if="loginType === 1" class="login-form-container">
                    <el-form @keyup.enter="onSubmit" ref="formRef" size="large">
                        <el-form-item prop="username">
                            <el-input
                                    type="text"
                                    clearable
                                    v-model="emailForm.email"
                                    placeholder="邮箱/手机号"
                                    autocomplete="off"
                            >
                                <template #prefix>
                                    <el-icon :size="16" color="var(--textColor2)">
                                        <UserFilled/>
                                    </el-icon>
                                </template>
                            </el-input>
                        </el-form-item>
                        <el-form-item prop="password">
                            <el-input
                                    v-model="emailForm.password"
                                    type="password"
                                    placeholder="请输入密码"
                                    show-password
                                    autocomplete="off"
                            >
                                <template #prefix>
                                    <el-icon :size="16" color="var(--textColor2)">
                                        <Platform/>
                                    </el-icon>
                                </template>
                            </el-input>
                            <div class="form-actions" v-if="isLogin">
                                <span class="action-link" @click="isLogin = !isLogin">前往注册</span>
                                <span class="action-link" @click="passwordDialog">找回密码</span>
                            </div>
                        </el-form-item>
                        <el-form-item prop="code" v-show="!isLogin">
                            <el-input
                                    maxlength="6"
                                    minlength="6"
                                    ref="codeRef"
                                    type="text"
                                    clearable
                                    v-model="emailForm.code"
                                    placeholder="请输入验证码"
                                    autocomplete="off"
                            >
                                <template #prefix>
                                    <el-icon :size="16" color="var(--textColor2)">
                                        <Connection/>
                                    </el-icon>
                                </template>
                                <template #append>
                                    <el-button
                                            :disabled="disabled"
                                            @click="startCountdown"
                                            v-text="buttonText"
                                            class="code-button"
                                    ></el-button>
                                </template>
                            </el-input>
                            <div class="form-actions">
                                <span class="action-link" @click="isLogin = !isLogin">前往登录</span>
                            </div>
                        </el-form-item>
                        <el-form-item>
                            <el-button
                                    :loading="loginLoading"
                                    class="submit-button"
                                    round
                                    type="primary"
                                    size="large"
                                    @click="onSubmit"
                            >
                                {{ isLogin ? "验证身份" : "快速注册" }}
                            </el-button>
                        </el-form-item>
                        <div class="privacy-prompt">
                            阅读并接受<span class="privacy-link" @click="privacyDialogVisible = true">井云AI用户隐私政策</span>
                        </div>
                    </el-form>
                </div>
            </div>
        </el-dialog>
        <!--找回密码-->
        <el-dialog
                v-model="isPassword"
                width="420px"
                class="login-dialog"
                :show-close="false"
                align-center
                @close="close"
                style="border-radius: 10px; overflow-x: hidden"
        >
            <div class="login-box">
                <div
                        style="
            display: flex;
            justify-content: center;
            align-items: center;
            padding-top: 40px;
          "
                >
                    <img
                            src="../assets/jingyun_ico.svg"
                            style="width: 60px; height: 60px"
                            alt=""
                    />
                </div>
                <div style="padding: 20px 40px 30px">
                    <div class="login-title">
                        <div class="login-selected">井云AI</div>
                    </div>
                    <el-form @keyup.enter="retrievePassword" ref="formRef" size="large">
                        <el-form-item prop="username">
                            <el-input
                                    type="text"
                                    clearable
                                    v-model="emailForm.email"
                                    placeholder="邮箱/手机号"
                                    autocomplete="off"
                            >
                                <template #prefix>
                                    <el-icon :size="16" color="var(&#45;&#45;textColor2)">
                                        <UserFilled/>
                                    </el-icon>
                                </template>
                            </el-input>
                        </el-form-item>
                        <el-form-item prop="password">
                            <el-input
                                    v-model="emailForm.password"
                                    type="password"
                                    placeholder="重新设置密码"
                                    show-password
                                    autocomplete="off"
                            >
                                <template #prefix>
                                    <el-icon :size="16" color="var(&#45;&#45;textColor2)">
                                        <Platform/>
                                    </el-icon>
                                </template>
                            </el-input>
                        </el-form-item>
                        <el-form-item prop="code">
                            <el-input
                                    maxlength="6"
                                    minlength="6"
                                    ref="codeRef"
                                    type="text"
                                    clearable
                                    v-model="emailForm.code"
                                    placeholder="请输入验证码"
                                    autocomplete="off"
                            >
                                >
                                <template #prefix>
                                    <el-icon :size="16" color="var(&#45;&#45;textColor2)">
                                        <Connection/>
                                    </el-icon>
                                </template>
                                <template #append>
                                    <div style="padding-left: 10px; background: none">
                                        <el-button
                                                :disabled="disabled"
                                                @click="startCountdown"
                                                v-text="buttonText"
                                                style="
                        background-color: var(--themeColor1);
                        color: var(--themeTextColor);
                      "
                                        ></el-button>
                                    </div>
                                </template>
                            </el-input>
                            <div
                                    style="text-align: right; color: #929292; font-size: 13px"
                                    @click="backLoginPanel"
                            >
                                返回登录
                            </div>
                        </el-form-item>
                        <el-form-item>
                            <el-button
                                    :loading="passwordLoading"
                                    class="submit-button"
                                    round
                                    type="primary"
                                    size="large"
                                    @click="retrievePassword"
                            >
                                找回密码
                            </el-button>
                        </el-form-item>
                    </el-form>
                </div>
            </div>
        </el-dialog>
        <!-- 隐私政策弹窗 -->
        <el-dialog
            v-model="privacyDialogVisible"
            width="600px"
            class="privacy-dialog"
            :show-close="true"
            align-center
            style="border-radius: 10px; overflow-x: hidden"
        >
            <div class="privacy-content">
                <h2>井云AI用户隐私政策</h2>
                <div class="privacy-text">
                    <p>尊敬的用户：</p>
                    <p>欢迎使用井云AI数字员工服务。在您使用本软件进行服务之前，请仔细阅读以下法律申明。</p>

                    <h3>一、数据采集与使用</h3>
                    <p>我们将根据您主动的操作合法、正当、必要的原则采集您提供的用于创建AI数字员工的数据，包括但不限于您的视频、声音和其它必要数据。采集的数据仅用于为您创建井云AI数字员工，我们保证不会用于其他任何未经您授权的目的。</p>

                    <h3>二、用户权利与义务</h3>
                    <p>您有权了解您的数据被处理的情况，并有权通过软件自行更正或删除不准确的数据。</p>
                    <p>您有义务确保所提供数据的真实性、合法性和完整性，不得提供虚假、违法或侵权的数据。</p>
                    <p>上传素材不得侵犯他人合法权益（包括但不限于肖像权、形象权、声音音色等）。未经被采集对象本人或其监护人的明确授权，因创建AI数字员工导致的法律责任由用户自行承担，平台不承担任何责任。</p>
                    <p>生成的数字员工内容应确保真实、合法、合规，不得利用该技术制作虚假信息、进行欺诈或传播谣言等违法行为。因此造成的法律责任由用户自行承担，平台不承担任何责任。</p>

                    <h3>三、隐私与数据安全</h3>
                    <p>我们将采取合理的安全措施保护您的数据，防止数据泄露、篡改或丢失。但请注意，任何技术手段均无法保证数据的绝对安全，建议您在服务终止后及时删除冗余数据。</p>

                    <h3>四、法律责任</h3>
                    <p>若您违反本声明任何条款，需自行承担相应法律后果。</p>

                    <h3>五、法律适用与争议解决</h3>
                    <p>如发生争议，双方应首先通过友好协商解决；协商不成的，可向有管辖权的人民法院提起诉讼。</p>

                    <p>请您仔细阅读并理解本声明内容。若不同意其中任何条款，请勿使用井云AI数字员工服务。</p>
                </div>
            </div>
        </el-dialog>
    </div>
</template>

<script>
import {defineComponent, ref, watch} from "vue";

import {useStore} from "vuex";

import {
    EmailEnroll,
    EmailLogin,
    getEmailCode, getPhoneCode,
    GetUserInfo,
    GetWechatCode,
    isQrCodeLoginSucceed, PhoneEnroll, PhoneLogin,
    RetrieveEmailPassword, RetrievePhonePassword,
} from "../../api/BSideApi";
import {ElMessage, ElNotification} from "element-plus";
import store from "@/store";
import {Connection, Platform, UserFilled} from "@element-plus/icons-vue";

export default defineComponent({
    name: "LoginDialog",
    components: {Connection, Platform, UserFilled},
    props: {
        show: {
            type: Boolean,
            default: false,
        },
    },
    setup(props, {emit}) {
        let store = useStore();
        const buttonText = ref("获取验证码");
        let loginType = ref(1);
        let qrCode = ref("");
        let qrCodeLoaded = ref(false);
        let promptAnimation = ref(true);
        let loginAnimation = ref(false);
        let loginLoading = ref(false);
        let dialogVisible = ref(false);
        const isPassword = ref(false);
        let isLogin = ref(true);
        let verifyCode = ref("");
        let isFailure = ref(false);
        let timerId;
        let lock = ref(false);
        const countdown = ref(null);
        const disabled = ref(false);
        const isCode = ref(true);
        const passwordLoading = ref(false);
        const privacyDialogVisible = ref(false);
        const emailForm = ref({
            email: "",
            password: "",
            code: "",
            phone: "",
        });
        watch(
            () => props.show,
            (newValue) => {
                if (newValue) {
                    getLoginQRCode();
                    dialogVisible.value = true;
                }
            },
            {
                immediate: true,
            }
        );

        function passwordDialog() {
            dialogVisible.value = false;
            isPassword.value = true;
        }

        function switchLoginType(type) {
            clearInterval(timerId);
            if (type === 0) {
                getLoginQRCode();
            }
            loginType.value = type;
        }

        function backLoginPanel() {
            isPassword.value = false;
            dialogVisible.value = true;
        }

        /**
         * 找回密码
         * @returns {Promise<void>}
         */
        async function retrievePassword() {
            if (passwordLoading.value) {
                return;
            }
            if (!emailForm.value.email) {
                ElMessage.warning("找回邮箱/手机号不能为空");
                return;
            }
            if (!emailForm.value.password) {
                ElMessage.warning("重设密码不能为空");
                return;
            }
            if (!emailForm.value.code) {
                ElMessage.warning("验证码不能为空");
                return;
            }

            try {
                passwordLoading.value = true;
                // 使用正则表达式判断邮箱或手机号
                let isPhoneNumber = /^\d{11}$/.test(emailForm.value.email);
                if (isPhoneNumber) {
                    emailForm.value.phone = emailForm.value.email;
                    await RetrievePhonePassword(emailForm.value);
                } else {
                    await RetrieveEmailPassword(emailForm.value);
                }
                // await RetrieveEmailPassword(emailForm.value);
                ElNotification({
                    title: "找回成功",
                    message: "快登录体验Ai吧",
                    type: "success",
                });
                isLogin.value = true;
                dialogVisible.value = true; // 保证登录对话框显示
                passwordLoading.value = false;
                isPassword.value = false;
            } catch (e) {
                ElNotification({
                    title: "错误",
                    message: e,
                    type: "error",
                });
                passwordLoading.value = false;
            }
        }

        /**
         * 获取验证码
         * startCountdown 开始倒计时
         * @returns {Promise<void>}
         */
        async function startCountdown() {
            if (isCode.value) {
                if (!emailForm.value.email) {
                    ElMessage.warning("验证邮箱/手机号不能为空");
                    return;
                }
                isCode.value = false;
                let seconds = 60;
                try {
                    buttonText.value = "正在发送中";
                    // 使用正则表达式判断邮箱或手机号
                    let isPhoneNumber = /^\d{11}$/.test(emailForm.value.email);
                    if (isPhoneNumber) {
                        emailForm.value.phone = emailForm.value.email;
                        await getPhoneCode(emailForm.value.phone);
                    } else {
                        await getEmailCode(emailForm.value.email);
                    }
                    ElMessage.info("验证码发送成功");
                    disabled.value = true;
                } catch (e) {
                    ElNotification({
                        title: "错误",
                        message: e,
                        type: "error",
                    });
                    buttonText.value = "重新获取验证码";
                    isCode.value = true;
                    return;
                }
                countdown.value = setInterval(() => {
                    if (seconds === 0) {
                        clearInterval(countdown.value);
                        countdown.value = null;
                        disabled.value = false;
                        buttonText.value = "重新获取验证码";
                        isCode.value = true;
                    } else {
                        seconds--;
                        buttonText.value = `${seconds}` + "后重新获取";
                    }
                }, 1000);
            }
        }

        /**
         * 获取登录二维码
         */
        async function getLoginQRCode() {
            // try {
            //     clearInterval(timerId);
            //     let newVar = await GetWechatCode();
            //     if (newVar) {
            //         isFailure.value = false;
            //         verifyCode.value = newVar.verifyCode;
            //         qrCode.value = newVar.qrCode;
            //         qrCodeLoaded.value = true;
            //         loginAnimation.value = true;
            //         promptAnimation.value = false;
            //         timerId = setInterval(() => {
            //             scanCodeLoginResults();
            //         }, 2000);
            //     }
            // } catch (e) {
            //     console.log(e);
            // }
        }

        /**
         * 扫码登录结果
         * @returns {Promise<void>}
         */
        async function scanCodeLoginResults() {
            try {
                let promise = await isQrCodeLoginSucceed(verifyCode.value);
                if (promise) {
                    if (!lock.value) {
                        lock.value = true;
                        clearInterval(timerId);
                        localStorage.setItem("token", promise);
                        dialogVisible.value = false;
                        loginLoading.value = false;
                        try {
                            let res = await GetUserInfo();
                            store.commit("setUserinfo", res);
                            // eslint-disable-next-line no-empty
                        } catch (e) {
                            console.log(e);
                        }
                        clearInterval(timerId);
                        ElNotification({
                            title: "登录成功",
                            message: "欢迎使用井云AI",
                            type: "success",
                        });
                        emit("loginSucceeded");
                        location.reload();
                        lock.value = false;
                    }
                }
            } catch (e) {
                clearInterval(timerId);
                isFailure.value = true;
            }
        }

        function close() {
            emit("close");
        }

        /**
         * 邮箱登录
         * @returns {Promise<void>}
         */
        async function emailLogin() {
            if (loginLoading.value) {
                return;
            }
            let value = emailForm.value;
            if (!value.email) {
                ElMessage.warning("登录邮箱/手机号不能为空");
                return;
            }
            if (!value.password) {
                ElMessage.warning("登录密码不能为空");
                return;
            }
            loginLoading.value = true;
            // 使用正则表达式判断邮箱或手机号
            let isPhoneNumber = /^\d{11}$/.test(value.email);
            try {
                let promise;
                if (isPhoneNumber) {
                    value.phone = value.email
                    // 如果是手机号，则调用PhoneLogin
                    promise = await PhoneLogin(emailForm.value);
                } else {
                    // 如果是邮箱，则调用EmailLogin
                    promise = await EmailLogin(emailForm.value);
                }
                localStorage.setItem("token", promise);
                try {
                    let res = await GetUserInfo();
                    store.commit("setUserinfo", res);
                    // eslint-disable-next-line no-empty
                } catch (e) {
                    console.log(e);
                }
                dialogVisible.value = false;
                loginLoading.value = false;
                ElNotification({
                    title: "登录成功",
                    message: "快登录体验井云AI吧",
                    type: "success",
                });
                loginLoading.value = false;
                isLogin.value = true;
                location.reload();
            } catch (e) {
                ElMessage.error(e);
                loginLoading.value = false;
            }
        }

        async function register() {
            try {
                if (loginLoading.value) {
                    return;
                }
                if (!emailForm.value.email) {
                    ElMessage.warning("注册邮箱/手机号不能为空");
                    return;
                }
                if (!emailForm.value.password) {
                    ElMessage.warning("注册密码不能为空");
                    return;
                }
                if (!emailForm.value.code) {
                    ElMessage.warning("验证码不能为空");
                    return;
                }
                loginLoading.value = true;
                // 使用正则表达式判断邮箱或手机号
                let isPhoneNumber = /^\d{11}$/.test(emailForm.value.email);
                if (isPhoneNumber) {
                    emailForm.value.phone = emailForm.value.email
                    // 如果是手机号，则调用PhoneLogin
                    await PhoneEnroll(emailForm.value);
                } else {
                    // 如果是邮箱，则调用EmailLogin
                    await EmailEnroll(emailForm.value);
                }
                // await EmailEnroll(emailForm.value);
                ElNotification({
                    title: "注册成功",
                    message: "快登录体验Ai吧",
                    type: "success",
                });
                isLogin.value = true;
                dialogVisible.value = true; // 保证登录对话框显示
                loginLoading.value = false;
            } catch (e) {
                loginLoading.value = false;
                ElNotification({
                    title: "错误",
                    message: e,
                    type: "error",
                });
            }
        }

        function onSubmit() {
            if (isLogin.value) {
                emailLogin();
            } else {
                register();
            }
        }

        return {
            backLoginPanel,
            retrievePassword,
            passwordDialog,
            onSubmit,
            startCountdown,
            buttonText,
            countdown,
            qrCode,
            qrCodeLoaded,
            loginAnimation,
            promptAnimation,
            loginLoading,
            dialogVisible,
            isPassword,
            close,
            isLogin,
            getLoginQRCode,
            isFailure,
            loginType,
            switchLoginType,
            emailForm,
            disabled,
            store,
            privacyDialogVisible,
        };
    },
});
</script>

<style lang="scss" scoped>
:deep(.el-dialog) {
  background-color: var(--bgColor1);
}

.animation {
  width: 160px;
  height: 160px;
  overflow: hidden;
  margin: 20px auto 0;
  background-color: rgb(53, 57, 60);
}

.animation img {
  width: 100%;
  height: 100%;
  object-fit: none;
}

.h5 {
  font-size: 10px;
}

@media (max-width: 767px) {
  .h5 {
    font-size: 12px;
  }
}

.login-box {
  overflow: hidden;
  width: 100%;
  padding: 0;
  background-color: var(--bgColor1);
}

.cover-div {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(27, 30, 32, 0.8);
  padding: 10px;
  border-radius: 5px;
  width: 150px;
  height: 150px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: var(--textColor1);
}

.login-box > .head > img {
  display: block;
  width: 100%;
  margin: 0 auto;
  user-select: none;
  margin-bottom: -20px;
}

.head_img {
  background-image: linear-gradient(
                  to top,
                  var(--bgColor1) 30%,
                  transparent 100%
  ),
  url("../assets/login-header.png");
  background-size: cover;
  background-position: center;
  height: 100px;
}

.form {
  position: relative;
  padding: 10px;
}

.submit-button {
  width: 100%;
  letter-spacing: 2px;
  font-weight: 300;
  margin-top: 15px;
  --el-button-bg-color: var(--themeColor1);
  border: none;
}

.submit-button:hover,
.submit-button:focus,
.submit-button:active {
  background-color: rgb(83, 67, 146);
  outline: 0;
}

.form > .content {
  display: flex;
  justify-content: center;
  align-items: center;
}

.loader {
  width: 20px;
  height: 20px;
  position: relative;
}

.loader:before,
.loader:after {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.loader:before {
  width: 15px;
  height: 15px;
  border-radius: 50%;
  background: #c2a5ff;
  animation: preloader_4_before 1.5s infinite ease-in-out;
}

.loader:after {
  width: 15px;
  height: 15px;
  border-radius: 50%;
  background: #ffa4c8;
  left: 5px;
  animation: preloader_4_after 1.5s infinite ease-in-out;
}

@keyframes preloader_4_before {
  0% {
    transform: translateX(0px) rotate(0deg);
  }
  50% {
    transform: translateX(15px) scale(1.2) rotate(260deg);
    background: #cca7f1;
    border-radius: 0px;
  }
  100% {
    transform: translateX(0px) rotate(0deg);
  }
}

@keyframes preloader_4_after {
  0% {
    transform: translateX(0px);
  }
  50% {
    transform: translateX(-15px) scale(1.2) rotate(-260deg);
    background: #9dbefa;
    border-radius: 0;
  }
  100% {
    transform: translateX(0px);
  }
}

.loading-model {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 160px;
  height: 160px;
}

.login-title {
  text-align: center;
  font-size: 15px;
  font-weight: 500;
  padding-bottom: 10px;
  color: #b3b3b3;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 10px;
}

.login-selected {
  color: var(--textColor1);
  font-weight: 550;
}

.login-title div {
  margin: 0 20px;
}

.qc_code {
  width: 140px;
  height: 140px;
  padding: 10px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0px 5px 30px var(--bgboxShadowColor2);

  &.darkMode {
    background-color: #eee;
  }
}

.btn-generate {
  display: flex;
  justify-content: center;
  align-items: center;
  padding-top: 20px;
}

.prompt-style {
  text-align: center;
  padding-top: 30px;
  padding-bottom: 20px;
}

.privacy-prompt {
  text-align: center;
  font-size: 12px;
  color: var(--textColor2);
  padding-bottom: 20px;

  .privacy-link {
    color: var(--themeColor1);
    cursor: pointer;
    text-decoration: underline;

    &:hover {
      opacity: 0.8;
    }
  }
}

.privacy-content {
  padding: 20px;
  color: var(--textColor2);

  h2 {
    text-align: center;
    margin-bottom: 20px;
    color: var(--textColor1);
  }

  h3 {
    color: var(--textColor1);
    margin: 15px 0 10px;
  }

  p {
    margin: 10px 0;
    line-height: 1.6;
  }
}

:deep(.el-input__inner) {
  background: var(--bgColor2);

  font-weight: 400;
  color: var(--textColor2);
}

:deep(.el-input__wrapper) {
  background: var(--bgColor2);
  box-shadow: none;
}

:deep(.el-input-group__append, .el-input-group__prepend) {
  border: none !important;
}

:deep(.login-dialog) {
  input:-internal-autofill-previewed,
  input:-internal-autofill-selected {
    -webkit-text-fill-color: var(--textColor2);
    box-shadow: inset 0 0 0 1000px var(--bgColor2) !important; // 改变了背景色
  }
}

.wechat-login-container {
  padding: 20px 40px;
}

.login-form-container {
  padding: 20px 40px 30px;
}

.qr-code-container {
  position: relative;
  border-radius: 5px;
  margin: 0 auto;
  width: 160px;
  height: 160px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 8px;
  font-size: 13px;

  .action-link {
    color: var(--textColor2);
    cursor: pointer;
    transition: color 0.3s;

    &:hover {
      color: var(--themeColor1);
    }
  }
}

.code-button {
  background-color: var(--themeColor1);
  color: var(--themeTextColor);
  border: none;
  padding: 0 15px;
  height: 100%;
  min-width: 110px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;

  &:disabled {
    opacity: 0.7;
  }
}

:deep(.el-input-group__append) {
  padding: 0;
  background: none;
  border: none;
  width: auto;
  min-width: 110px;
}

:deep(.el-input__wrapper) {
  background: var(--bgColor2);
  box-shadow: none;
  padding: 0 15px;
  padding-right: 0;
}

:deep(.el-input__inner) {
  height: 45px;
  line-height: 45px;
  background: var(--bgColor2);
  font-weight: 400;
  color: var(--textColor2);
  padding-right: 10px;
}

.privacy-prompt {
  text-align: center;
  font-size: 12px;
  color: var(--textColor2);
  margin-top: 15px;

  .privacy-link {
    color: var(--themeColor1);
    cursor: pointer;
    text-decoration: underline;
    transition: opacity 0.3s;

    &:hover {
      opacity: 0.8;
    }
  }
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

.submit-button {
  width: 100%;
  height: 45px;
  letter-spacing: 2px;
  font-weight: 500;
  margin-top: 10px;
  --el-button-bg-color: var(--themeColor1);
  border: none;
  transition: background-color 0.3s;

  &:hover {
    background-color: var(--themeColor2);
  }
}
</style>
