# Campus Exchange 校园二手交易平台

本项目为校园二手交易平台，包含前端（Vue 3 + Vite）和后端（Spring Boot）两部分，支持商品发布、浏览、搜索、留言等功能，包含用户，管理员，商家等模块。

## 项目亮点

- 从0到1上线部署到服务器，通过配置nginx代理和重定向解决刷新路由404等问题，模拟真实的业务流程，曾在实验检查时展示。
- 通过懒加载，首页轮播图采用webp格式，对代码结构进行优化等方式实现前端渲染页面的性能优化，首屏LCP指标平均降低30%。
- 实现真实文件上传功能，可上传商品图片等文件，通过配置vite代理实现文件路径的重定向，支持上传至服务器硬盘，提升用户体验。
- 利用SpringSecurit实现基于JWT的用户身份认证与权限控制，支持用户状态的统一管理并持久化，前端可根据角色身份进行路由的重定向。
---

## 目录结构

```
campus-exchange-frontend/   # 前端项目，Vue 3 + Vite
campus-exchange-backend/    # 后端项目，Spring Boot
```

---

## 前端说明

- 技术栈：Vue 3全家桶、Vite、JS
- 具体描述建议详细查看实验报告文档。

  
## 后端说明

- 技术栈：Spring Boot、Maven
- 具体描述建议详细查看实验报告文档。


### 本地开发

```sh
cd campus-exchange-frontend
npm install
npm run dev
```

### 构建生产环境

```sh
npm run build
```

### 单元测试

```sh
npm run test:unit
```

### 代码规范检查

```sh
npm run lint
```

更多配置见 [Vite 配置文档](https://vite.dev/config/)。

---


### 本地开发

```sh
cd campus-exchange-backend
./mvnw spring-boot:run
```

或使用 IDE 直接运行主类。

### 构建

```sh
./mvnw clean package
```

---

## 其他

- 具体描述建议详细查看实验报告文档。
- 图片等静态资源放在 `upload/` 目录，考虑到资源大小等因素已经删除。


---

## License

MIT
