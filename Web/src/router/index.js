import {createRouter, createWebHashHistory} from 'vue-router'
import {cancelArr} from "@/utils/BSideRequest";

const routes = [
    {
        path: '/',
        name: 'Home',
        component: () => import('../views/HomeView.vue'),
        meta: {
            title: '井云AI - 智能创作平台',
            isHeadNavigation: false,
            keepAlive: false,
            isLeftMenu: false
        }
    },
    {
        path: '/chat',
        name: 'Chat',
        component: () => import('../views/DialogueView.vue'),
        meta: {
            title: '智能问答',
            isHeadNavigation: true,
            keepAlive: true,
            isLeftMenu: true
        }
    }, {
        path: '/creation',
        name: 'CozeCreate',
        component: () => import('../views/Create/CozeCreateView.vue'),
        meta: {
            title: '文案创作',
            isHeadNavigation: true,
            keepAlive: false,
            isLeftMenu: true
        }
    },
    {
        path: "/creation_edit",
        name: "CreateEdit",
        component: () => import('../views/Create/CozeCreateEditView.vue'),
        meta: {
            title: '创作编辑',
            isHeadNavigation: true,
            keepAlive: false,
            isLeftMenu: true
        },
    },
    {
        path: "/creation_detail",
        name: "CreateDetail",
        component: () => import('../views/Create/CozeCreateDetailView.vue'),
        meta: {
            title: '创作结果',
            isHeadNavigation: true,
            keepAlive: false,
            isLeftMenu: true
        },
    },
    {
        path: '/purchase',
        name: 'Purchase',
        component: () => import('../views/PurchaseView.vue'),
        meta: {
            title: '购买',
            isHeadNavigation: true,
            keepAlive: false,
            isLeftMenu: true
        }
    },
    {
        path: '/orders',
        name: 'Orders',
        component: () => import('../views/OrdersView.vue'),
        meta: {
            title: '充值记录',
            isHeadNavigation: true,
            keepAlive: false,
            isLeftMenu: true
        }
    },
    {
        path: '/collection',
        name: 'Collection',
        component: () => import('../views/CollectionView.vue'),
        meta: {
            title: '我的收藏',
            isHeadNavigation: true,
            keepAlive: false,
            isLeftMenu: true
        }
    },
    {
        path: "/exchange",
        name: "Exchange",
        component: () => import('../views/ExchangeView.vue'),
        meta: {
            title: '兑换中心',
            isHeadNavigation: true,
            keepAlive: false,
            isLeftMenu: true
        }
    },
    {
        path: "/custom",
        name: "Custom",
        component: () => import('../views/CustomView.vue'),
        meta: {
            title: '自定义对话',
            isHeadNavigation: true,
            keepAlive: false,
            isLeftMenu: true
        }
    },
    {
        path: "/GPTs_view",
        name: "GPTsView",
        component: () => import('../views/GPTsView.vue'),
        meta: {
            title: 'GPTs应用',
            isHeadNavigation: true,
            keepAlive: false,
            isLeftMenu: true
        }
    },
    {
        path: "/preset_character",
        name: "PresetCharacter",
        component: () => import('../views/PresetCharacterView.vue'),
        meta: {
            title: '预设',
            isHeadNavigation: true,
            keepAlive: false,
            isLeftMenu: true
        }
    },
    {
        path: "/laboratory",
        name: "Laboratory",
        component: () => import('../views/LaboratoryView.vue'),
        meta: {
            title: '模型',
            isHeadNavigation: true,
            keepAlive: false,
            isLeftMenu: true
        }
    }
    , {
        path: '/admin',
        name: 'Admin',
        component: () => import('@/views/Admin/AdminView.vue'),
        meta: {
            title: '管理控制台',
            isHeadNavigation: true,
            keepAlive: true,
            isLeftMenu: true
        }
    },
]


const router = createRouter({
    history: createWebHashHistory(),
    routes
})


// TODO 全局前置守卫
router.beforeEach(async (to) => {
    // TODO 页面切换中断所有请求
    cancelArr.forEach((cancel, index) => {
        cancel()
        cancelArr.splice(index, 1)
    })

    // TODO 设置浏览器Title
    document.title = (to.meta.title ? to.meta.title : '') + ' - 井云AI'
})

export default router
