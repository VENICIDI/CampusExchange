import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import ProductDetailView from '../views/ProductDetailView.vue'
import ProductPublishView from '../views/ProductPublishView.vue'
import MerchantDashboardView from '../views/MerchantDashboardView.vue'
import MerchantStorefrontView from '../views/MerchantStorefrontView.vue'
import MerchantProfileEditView from '../views/MerchantProfileEditView.vue'
import OrderConfirmView from '../views/OrderConfirmView.vue'
import OrderDetailView from '../views/OrderDetailView.vue'
import UserOrdersView from '../views/UserOrdersView.vue'
import MerchantOrdersView from '../views/MerchantOrdersView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
      meta: { requiresAuth: false } // 首页允许未登录访问
    },
    {
      path: '/buyer',
      name: 'buyer-home',
      component: HomeView,
      meta: { requiresAuth: true } // 买家首页需要登录
    },
    {
      path: '/about',
      name: 'about',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('../views/AboutView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { guest: true }
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView,
      meta: { guest: true }
    },
    {
      path: '/product/:id',
      name: 'product-detail',
      component: ProductDetailView,
      meta: { requiresAuth: false }
    },
    {
      path: '/product/publish',
      name: 'product-publish',
      component: ProductPublishView,
      meta: { requiresAuth: true, roles: ['MERCHANT'] }
    },
    {
      path: '/product/edit/:id',
      name: 'product-edit',
      component: ProductPublishView,
      meta: { requiresAuth: true, roles: ['MERCHANT'] }
    },
    {
      path: '/merchant',
      name: 'merchant-dashboard',
      component: MerchantDashboardView,
      meta: { requiresAuth: true, roles: ['MERCHANT'] }
    },
    {
      path: '/merchant/profile',
      name: 'merchant-profile-edit',
      component: MerchantProfileEditView,
      meta: { requiresAuth: true, roles: ['MERCHANT'] }
    },
    {
      path: '/store/:id',
      name: 'merchant-storefront',
      component: MerchantStorefrontView,
      meta: { requiresAuth: false }  // 店铺主页允许未登录访问
    },
    // 订单相关路由
    {
      path: '/order/confirm',
      name: 'order-confirm',
      component: OrderConfirmView,
      meta: { requiresAuth: true }
    },
    {
      path: '/order/:orderNo',
      name: 'order-detail',
      component: OrderDetailView,
      meta: { requiresAuth: true }
    },
    {
      path: '/orders/user',
      name: 'user-orders',
      component: UserOrdersView,
      meta: { requiresAuth: true }
    },
    {
      path: '/orders/merchant',
      name: 'merchant-orders',
      component: MerchantOrdersView,
      meta: { requiresAuth: true, roles: ['MERCHANT'] }
    }
  ],
})

// 路由守卫
router.beforeEach((to, from, next) => {
  console.log('路由守卫触发，跳转到:', to.path, '从:', from.path);
  
  // 每次路由变更时重新从localStorage获取用户数据，确保状态最新
  const userJson = localStorage.getItem('user');
  console.log('路由守卫中的用户数据:', userJson);
  
  let isLoggedIn = false;
  let userRole = '';
  let userData = null;
  
  if (userJson) {
    try {
      userData = JSON.parse(userJson);
      // 验证用户数据的有效性
      isLoggedIn = userData && userData.username && userData.userId;
      
      // 确保角色信息存在，默认为USER
      if (!userData.role) {
        console.warn('用户数据缺少角色信息，设置默认角色为USER');
        userData.role = 'USER';
        // 更新存储的用户信息
        localStorage.setItem('user', JSON.stringify(userData));
      }
      
      userRole = userData.role;
      console.log('用户登录状态:', isLoggedIn, '用户名:', userData.username, '角色:', userRole);
    } catch (e) {
      console.error('路由守卫中解析用户数据失败:', e);
      // 数据无效，清除它
      localStorage.removeItem('user');
      isLoggedIn = false;
    }
  } else {
    console.log('用户未登录');
  }

  // 登录成功后的默认重定向逻辑
  if (to.path === '/' && isLoggedIn && userRole === 'MERCHANT' && from.path === '/login') {
    console.log('商家登录成功，重定向到商家中心');
    next({ name: 'merchant-dashboard' });
    return;
  }

  // 检查是否需要特定角色
  if (to.matched.some(record => record.meta.roles)) {
    // 如果用户未登录，先跳转到登录页
    if (!isLoggedIn) {
      console.log('需要登录才能访问此页面，重定向到登录页');
      next({ 
        name: 'login',
        query: { redirect: to.fullPath } // 保存原来要去的页面，便于登录后跳转回来
      });
      return;
    }
    
    // 如果用户已登录但角色不匹配，跳转到首页
    if (!to.meta.roles.includes(userRole)) {
      console.log('用户角色不满足路由要求，重定向到首页');
      next({ name: 'home' });
      return;
    }
  }
  
  // 如果页面需要登录认证且用户未登录
  if (to.matched.some(record => record.meta.requiresAuth) && !isLoggedIn) {
    console.log('需要登录才能访问此页面，重定向到登录页');
    next({ 
      name: 'login',
      query: { redirect: to.fullPath } // 保存原来要去的页面，便于登录后跳转回来
    });
    return;
  } 
  
  // 如果页面是游客页面(登录/注册)且用户已登录
  if (to.matched.some(record => record.meta.guest) && isLoggedIn) {
    console.log('用户已登录，不能访问游客页面，根据角色重定向');
    // 根据用户角色跳转到合适的页面
    if (userRole === 'MERCHANT') {
      next({ name: 'merchant-dashboard' });
    } else if (userRole === 'ADMIN') {
      // 如果有管理员页面可以跳转到管理员页面
      next({ name: 'home' }); // 临时跳转到首页
    } else {
      next({ name: 'home' });
    }
    return;
  } 
  
  // 正常导航
  console.log('正常导航到:', to.path);
  next();
})

export default router 