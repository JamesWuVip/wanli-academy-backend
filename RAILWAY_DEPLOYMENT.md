# Railway 部署指南

本文档详细说明如何将万里学院项目部署到Railway平台。

## 🚀 部署概览

### 项目结构
- **后端**: Spring Boot应用 (Java)
- **前端**: Vue.js应用 (TypeScript)
- **数据库**: PostgreSQL

### Railway服务配置
1. **Backend Service** - Spring Boot API
2. **Frontend Service** - Vue.js静态网站
3. **Database Service** - PostgreSQL数据库

## 📋 部署步骤

### 1. 准备工作

确保你已经:
- ✅ 创建Railway账户
- ✅ 项目代码已推送到GitHub
- ✅ 本地测试通过

### 2. 创建Railway项目

1. 登录 [Railway.app](https://railway.app)
2. 点击 "New Project"
3. 选择 "Deploy from GitHub repo"
4. 选择你的 `wanliRepo` 仓库

### 3. 配置数据库服务

1. 在Railway项目中点击 "+ New"
2. 选择 "Database" → "PostgreSQL"
3. 等待数据库创建完成
4. 复制 `DATABASE_URL` 环境变量

### 4. 配置后端服务

1. 在Railway项目中点击 "+ New"
2. 选择 "GitHub Repo" → 选择你的仓库
3. 设置 **Root Directory**: `backend`
4. 添加环境变量:
   ```
   DATABASE_URL=<从数据库服务复制>
   SPRING_PROFILES_ACTIVE=production
   PORT=8080
   ```
5. Railway会自动检测Spring Boot项目并部署

### 5. 配置前端服务

1. 在Railway项目中点击 "+ New"
2. 选择 "GitHub Repo" → 选择你的仓库
3. 设置 **Root Directory**: `frontend`
4. 添加环境变量:
   ```
   NODE_ENV=production
   PORT=4173
   VITE_API_BASE_URL=<后端服务URL>
   ```
5. Railway会自动检测Vue.js项目并部署

## 🔧 环境变量配置

### 后端环境变量
```bash
DATABASE_URL=postgresql://username:password@hostname:port/database
SPRING_PROFILES_ACTIVE=production
PORT=8080
SPRING_JPA_HIBERNATE_DDL_AUTO=update
```

### 前端环境变量
```bash
NODE_ENV=production
PORT=4173
VITE_API_BASE_URL=https://your-backend-service.railway.app
```

## 📊 资源配置

### 推荐配置
- **后端**: 512MB RAM, 1 vCPU
- **前端**: 256MB RAM, 0.5 vCPU
- **数据库**: 256MB RAM, 0.5 vCPU

### 免费额度使用
- Railway提供每月5美元免费额度
- 上述配置在免费额度内
- 按使用量计费，无需担心超额

## 🔗 域名配置

### 自动域名
Railway会为每个服务自动生成域名:
- 后端: `https://your-backend-service.railway.app`
- 前端: `https://your-frontend-service.railway.app`

### 自定义域名
1. 在服务设置中点击 "Domains"
2. 添加自定义域名
3. 配置DNS记录指向Railway

## 🚨 故障排除

### 常见问题

1. **后端启动失败**
   - 检查 `DATABASE_URL` 是否正确
   - 确认 `SPRING_PROFILES_ACTIVE=production`
   - 查看构建日志中的错误信息

2. **前端构建失败**
   - 检查Node.js版本兼容性
   - 确认所有依赖已正确安装
   - 检查TypeScript编译错误

3. **数据库连接失败**
   - 确认数据库服务正在运行
   - 检查网络连接和防火墙设置
   - 验证数据库凭据

### 日志查看
```bash
# 查看服务日志
railway logs --service <service-name>

# 实时日志
railway logs --service <service-name> --follow
```

## 📈 监控和维护

### 健康检查
- 后端: `GET /actuator/health`
- 前端: 自动HTTP检查

### 性能监控
- Railway Dashboard提供CPU、内存使用情况
- 可设置告警通知

### 自动部署
- 推送到GitHub main分支自动触发部署
- 支持回滚到之前版本

## 🔄 CI/CD集成

Railway与GitHub无缝集成:
1. 代码推送到main分支
2. 自动触发构建和部署
3. 零停机时间更新

## 💰 成本估算

基于免费额度(5美元/月):
- 轻量级使用: 完全免费
- 中等流量: 1-3美元/月
- 高流量: 需要升级套餐

## 📞 支持

- [Railway文档](https://docs.railway.app)
- [Railway Discord社区](https://discord.gg/railway)
- [GitHub Issues](https://github.com/railwayapp/railway/issues)

---

**注意**: 确保在部署前测试所有功能，并备份重要数据。