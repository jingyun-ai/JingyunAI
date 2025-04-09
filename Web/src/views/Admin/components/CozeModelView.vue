<template>
  <div class="u_container">
    <!-- 这里是顶部按钮区域，可自行调整 -->
    <div style="margin-bottom: 30px; margin-top: 30px">
      <el-button
          color="var(--themeColor2)"
          style="width: 100px"
          @click="dialogFormVisible = true"
      >
        新增智能体
      </el-button>
      <el-button
          color="var(--themeColor2)"
          style="width: 100px"
          @click="dialogFormVisible = true"
      >
        新增表单
      </el-button>
    </div>

    <!-- 这里是表格区域，和原先保持一致 -->
    <el-table
        :data="dataTables"
        height="70%"
        :header-cell-style="{
        background: ' var(--bgColor1)',
        borderColor: 'var(--borderColor)',
      }"
        style="background-color: var(--bgColor1)"
        :row-style="{
        height: '100%',
        background: ' var(--bgColor1)',
        border: 'none',
      }"
    >
      <el-table-column prop="sdModelId" label="标识"/>
      <el-table-column prop="modelName" label="模型值"/>
      <el-table-column prop="textName" label="展示名称"/>
      <el-table-column prop="createdTime" label="创建时间"/>
      <el-table-column fixed="right" label="操作" width="200">
        <template #header>
          <el-input
              style="width: 180px"
              v-model="prompt"
              size="small"
              placeholder="模糊搜索"
              @change="search"
          />
        </template>
        <template #default="scope">
          <el-button
              link
              type="primary"
              size="small"
              @click="deleteSdModel(scope.row)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div style="display: flex; justify-content: right; padding-top: 10px">
      <el-pagination
          layout="prev, pager, next"
          :total="total"
          :page-size="15"
          background
          @current-change="handleCurrentChange"
      />
    </div>

    <!-- 重点：弹窗表单修改为“两列布局”，如示例图 -->
    <el-dialog
        v-model="dialogFormVisible"
        width="600px"
        style="background-color: var(--bgColor1); padding-top: 20px"
    >
      <el-form :model="form" label-width="100px" style="padding: 10px 0;">
        <!-- 第一行：员工昵称、员工职位 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="员工昵称">
              <el-input
                  v-model="form.nickname"
                  autocomplete="off"
                  placeholder="请输入员工昵称"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="员工职位">
              <el-input
                  v-model="form.position"
                  autocomplete="off"
                  placeholder="请输入员工职位"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 第三行：员工分类、员工头像 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="员工分类">
              <el-input
                  v-model="form.category"
                  autocomplete="off"
                  placeholder="请输入员工分类"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="员工头像">
              <el-upload
                  class="avatar-uploader"
                  action="#"
                  :show-file-list="false"
                  :on-success="handleAvatarSuccess"
                  :before-upload="beforeAvatarUpload"
              >
                <img
                    v-if="form.avatarUrl"
                    :src="form.avatarUrl"
                    class="avatar"
                    alt=""
                />
                <i v-else class="el-icon-plus avatar-uploader-icon">
                  <el-icon style="font-size: 18px;">
                    <Upload/>
                  </el-icon>
                </i>
              </el-upload>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 第三行：员工描述（独占整行） -->
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="员工描述">
              <el-input
                  type="textarea"
                  v-model="form.description"
                  autocomplete="off"
                  placeholder="描述该员工信息"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 第四行：提示词（独占整行） -->
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="提示词">
              <el-input
                  type="textarea"
                  v-model="form.prompt"
                  autocomplete="off"
                  placeholder="输入提示词"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button
              type="default"
              color="var(--themeColor2)"
              @click="handleAddEmployee"
          >
            创建
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import {onMounted, ref} from "vue";
import store from "@/store";
// 这里原本引用的接口 DeleteSdModel, GetSdModelPage, PutSdModel 等
import {
  DeleteSdModel,
  GetSdModelPage,
  PutSdModel,
} from "../../../../api/BSideApi";
import {conversionTime} from "@/utils/date";
import {ElLoading, ElMessageBox, ElNotification} from "element-plus";
import {Upload} from "@element-plus/icons-vue";

export default {
  name: "productView",
  components: {Upload},
  computed: {
    store() {
      return store;
    },
  },
  setup() {
    const dialogFormVisible = ref(false);
    const dataTables = ref([]);
    const current = ref(1);
    const total = ref(0);
    const prompt = ref("");
    const load = ref(true);
    const empty = ref(false);
    const error = ref(false);

    // 这里将 form 的字段改成与“员工信息”相关
    const form = ref({
      nickname: "",
      category: "",
      description: "",
      position: "",
      avatarUrl: "", // 存放上传后头像的地址
      prompt: "",
    });

    onMounted(() => {
      if (store.getters.userinfo && store.getters.userinfo.type === "ADMIN") {
        handleCurrentChange(current.value);
      }
    });

    // 这里可以根据你自己需要，做新增员工时的处理
    async function handleAddEmployee() {
      try {
        // 这里只做简单示例：判断必填项
        if (!form.value.nickname) {
          ElNotification({
            title: "错误",
            message: "员工昵称不能为空",
            type: "error",
          });
          return;
        }

        // 假设把员工信息提交给后端
        ElLoading.service({
          fullscreen: true,
          text: "正在提交数据...",
          spinner: "el-icon-loading",
          background: "rgba(0, 0, 0, 0.7)",
        });
        // 这里演示：复用 PutSdModel 或改成你自己的接口
        await PutSdModel({
          modelName: form.value.nickname, // 只是演示如何传参
          textName: form.value.position,
        });
        ElLoading.service().close();

        // 成功后，刷新列表并关闭弹窗
        ElNotification({
          message: "新增成功",
          type: "success",
        });
        dialogFormVisible.value = false;
        prompt.value = "";
        current.value = 1;
        await handleCurrentChange(1);
      } catch (e) {
        ElLoading.service().close();
        ElNotification({
          title: "错误",
          message: e,
          type: "error",
        });
      }
    }

    function search() {
      current.value = 1;
      dataTables.value = [];
      handleCurrentChange(1);
    }

    async function deleteSdModel(data) {
      try {
        await ElMessageBox({
          title: "提示",
          message: "确定要删除?",
          confirmButtonText: "确定",
          cancelButtonText: "再想想",
          showCancelButton: true,
          type: "warning",
        });
        ElLoading.service({
          fullscreen: true,
          text: "正在删除...",
          spinner: "el-icon-loading",
          background: "rgba(0, 0, 0, 0.7)",
        });
        await DeleteSdModel(data.sdModelId);
        ElLoading.service().close();
        prompt.value = "";
        current.value = 1;
        dataTables.value = [];
        await handleCurrentChange(1);
        ElNotification({
          message: "删除成功",
          type: "success",
        });
      } catch (e) {
        console.log("取消删除");
        ElLoading.service().close();
      }
    }

    async function handleCurrentChange(pageNum) {
      try {
        let res = await GetSdModelPage(pageNum, prompt.value);
        let records = res.records;
        if (records.length > 0) {
          for (const r of records) {
            r.createdTime = conversionTime(r.createdTime);
          }
          dataTables.value = records;
        } else {
          empty.value = true;
        }
        current.value = res.current;
        total.value = res.total;
        load.value = false;
        error.value = false;
      } catch (e) {
        load.value = false;
        error.value = true;
      }
    }

    // 以下是头像上传示例回调
    function handleAvatarSuccess(response, file, fileList) {
      // 通常后端会返回一个头像URL，这里只是演示
      form.value.avatarUrl = URL.createObjectURL(file.raw);
    }

    function beforeAvatarUpload(file) {
      // 这里可对文件类型大小做限制
      const isImage = file.type.indexOf("image/") === 0;
      if (!isImage) {
        ElNotification.error("只能上传图片文件");
        return false;
      }
      return true;
    }

    return {
      dialogFormVisible,
      dataTables,
      current,
      total,
      prompt,
      load,
      empty,
      error,
      form,
      handleAddEmployee,
      search,
      deleteSdModel,
      handleCurrentChange,
      handleAvatarSuccess,
      beforeAvatarUpload,
    };
  },
};
</script>

<style scoped>
el-form-item {
  margin-bottom: 20px;
}

::v-deep(.el-textarea__inner) {
  background-color: var(--bgboxShadowColor2);
  border: none;
}

@keyframes explainAnimation {
  from {
    transform: scale(0);
  }
  to {
    transform: scale(1);
  }
}

::v-deep(.el-table--enable-row-hover .el-table__body tr:hover td.el-table__cell) {
  background: none !important;
}

::v-deep(.el-pagination .is-background .el-pager li:not(.is-disabled).is-active) {
  background-color: rgb(125, 128, 255);
}

::v-deep(.el-table-fixed-column--right) {
  background: var(--bgColor1) !important;
}

.head_model {
  display: flex;
  background-color: var(--themeColor1);
  height: 130px;
  margin-bottom: 20px;
  align-items: center;
  border-radius: 3px;
  box-shadow: 0 2px 6px var(--bgColor1);
}

.head_model_style {
  padding-left: 40px;
  color: var(--themeTextColor);
}

.number_people {
  font-size: 35px;
  font-weight: 600;
}

.text_people {
  font-size: 15px;
  margin-top: 5px;
  padding-left: 5px;
}

::v-deep(.el-input__inner) {
  background: var(--bgColor2);
  font-weight: 400;
  color: var(--textColor2);
}

::v-deep(.el-input__wrapper) {
  background: var(--bgColor2);
  box-shadow: none;
}

/* 头像上传的样式示例 */
.avatar-uploader {
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-uploader-icon {
  color: #8c939d;
  line-height: 8px;
}

.avatar {
  width: 80px;
  height: 80px;
  display: block;
  border-radius: 50%;
}
</style>
