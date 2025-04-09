<template>
    <div class="body">
        <div class="container">
            <div class="content">
                <!-- 添加 el-tabs 进行分类切换 -->
                <el-tabs v-model="activeTab" class="custom-tabs">
                    <!-- 遍历分类标签 -->
                    <el-tab-pane v-for="(tag, index) in uniqueTags" :key="index" :label="tag" :name="tag" >
                        <ViewState
                            v-if="tag==='角色收藏' && !store.getters.userinfo"
                            Type="error"
                            ErrorText="登录查看收藏"
                            IsShowBottom
                            ButtonText="登录"
                            @ClickTheButton="loginVisible = true"
                        />
                        <el-row :gutter="20">
                            <!-- 遍历每个角色，根据标签进行分类显示 -->
                            <el-col v-for="(item, itemIndex) in filteredMenuCollection(tag)"
                                    :key="itemIndex"
                                    :xs="12"
                                    :sm="8"
                                    :md="4">
                                <div class="item" @click="onItem(item)">
                                        <Star
                                            class="collection"
                                            :style="{ color: item.tags.includes('角色收藏') ? 'var(--themeColor1)' : 'initial' }"
                                            @click.stop="onCollection_preset(item.title,item);"/>
                                    <div style="font-size: 50px">{{ item.icon }}</div>
                                    <div>
                                        <div class="title">{{ item.title }}</div>
                                        <div class="introduce">{{ item.introduce }}</div>
                                    </div>
                                </div>
                            </el-col>
                        </el-row>
                    </el-tab-pane>
                </el-tabs>
            </div>
        </div>
    </div>
    <LoginDialog :show="loginVisible" @close="loginVisible = false" />
</template>

<script>
import { onMounted, ref} from "vue";
import { useRouter } from "vue-router";
import { useStore } from "vuex";
import LoginDialog from "@/components/LoginDialog.vue";
import {Favorites_Preset} from "../../api/BSideApi";
import ViewState from "@/components/ViewState.vue";
import store from "@/store";
import {onCollection_preset} from  "./CustomView"
export default {
    name: "PresetCharacterView",
    components: { LoginDialog,ViewState },
    computed: {
        store() {
            return store;
        },
    },
    setup() {

        let store = useStore();
        let router = useRouter();
        let menuCollection = ref(require("../utils/PresetsData.json"));
        let loginVisible = ref(false);
        let activeTab = ref("工作提效"); // 设置初始激活的选项卡
        let menuCollection_Dict = {};// 哈希表
        for (let i in menuCollection.value)
            menuCollection_Dict[menuCollection.value[i].title] = i;

        onMounted(async () => {
            try {
                if (store.getters.userinfo)
                {
                    // menuCollection太多而被缓存,所以需要res_last
                    let res = await Favorites_Preset();
                    // let res_last = JSON.parse(localStorage.getItem("res_last"));
                    //
                    // if (res_last)
                    // {
                    //     let disappear = Array.from(new Set([...res_last.filter(x => !new Set(res).has(x))]));
                    //      console.log("移出:",disappear);
                    //     for (let i in disappear)
                    //         menuCollection.value[menuCollection_Dict[disappear[i]]].tags = menuCollection.value[menuCollection_Dict[disappear[i]]].tags.filter(str => str !== "角色收藏");
                    // }

                    for (let i in res)
                    {
                        let temp = menuCollection.value[menuCollection_Dict[res[i]]].tags
                        if (!temp.includes("角色收藏"))
                            temp.push("角色收藏");
                    }
                    // console.log(res)

                    // localStorage.setItem("res_last", JSON.stringify(res));
                }
            } catch (err) {
                //
            }
        })
        // 获取所有的标签
        const tags = menuCollection.value.reduce((allTags, item) => {
            allTags.push("角色收藏");
            if (item.tags) {
                allTags.push(...item.tags);
            }
            return allTags;
        }, []);

        // 过滤掉重复的标签，保留唯一的标签
        const uniqueTags = [...new Set(tags)];

        function onItem(data) {
            if (!store.getters.userinfo) return (loginVisible.value = true);
            localStorage.setItem("roleData", JSON.stringify(data));
            router.push({
                path: "/custom",
            });
        }

        // 根据标签进行筛选
        function filteredMenuCollection(tag) {
            return menuCollection.value.filter(item => item.tags && item.tags.includes(tag));
        }

        return {
            menuCollection,
            onItem,
            loginVisible,
            activeTab,
            uniqueTags,
            filteredMenuCollection,
            onCollection_preset
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

.custom-tabs {
    width: 100%;
    margin-top: 30px;
}

.item {
    position: relative;
    width: 100%;
    max-width: 400px; /* 增加最大宽度以容纳更多内容 */
    height: 185px;
    background-color: var(--bgColor1);
    margin-bottom: 15px;
    border-radius: 8px;
    font-size: 15px;
    color: var(--bgColor2);
    padding: 20px;
    box-shadow: 0 5px 7px rgba(35, 35, 35, 0.06);
    box-sizing: border-box;
}

.item:hover {
    background-color: var(--bgColor1);
    cursor: pointer;
    transition: background-color 0.2s;
}

.collection {
    position: absolute;
    top: 15%;
    right: 10%;
    width: 15%; /* 设置方框的宽度 */
    height: 15%; /* 设置方框的高度 */
    color: #6a737d;
    border-radius: 10px; /* 设置边框圆角半径 */
    transition: color 0.2s; /* 添加颜色渐变过渡效果 */
}

.title {
    font-weight: 700;
    font-size: 16px;
    color: var(--textColor1);
    padding-top: 10px;
}

.introduce {
    padding-top: 10px; /* 调整填充以改善间距 */
    font-size: 13px;
    color: var(--textColor4);
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    text-overflow: ellipsis;
}
.centered-text {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100%; /* 设置父元素的高度，以便垂直居中生效 */
}
:deep(.el-tabs__item) {
    color: var(--textColor1);
}
:deep(.el-tabs__nav-scroll) {
    /*tab-position: right;*/
    backdrop-filter: blur(3px); /* 添加背景模糊效果，调整模糊半径 */
    position: fixed;
    left: 17%;
    top: 6%;
    height: 4%; /* Adjust the height according to your layout */
    z-index: 1; /* Ensure it's above other elements */
    border-radius: 10px;
    padding: 6px;
}


@media only screen and (max-width: 767px) {
    .item {
        max-width: 100%; /* 在小屏幕上将最大宽度设置为100% */
        height: auto; /* 允许高度根据内容调整 */
    }
    .el-col{
        max-width: 300px;
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