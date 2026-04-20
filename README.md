# Base RBAC 权限管理系统

## 项目简介

基于 Spring Boot 3.2 + MyBatis-Plus + JWT 的通用后台权限管理系统（基础版）

## 技术栈

### 后端

- **框架**: Spring Boot 3.2.4
- **JDK**: 17
- **ORM**: MyBatis-Plus 3.5.5
- **数据库**: MySQL 8.4.3
- **缓存**: Redis 7.x
- **安全**: Spring Security + JWT (jjwt 0.12.5)
- **文档**: SpringDoc OpenAPI 3 (Swagger)
- **工具**: Hutool 5.8.26, EasyExcel 3.3.3

### 前端（待开发）

- Vue 3 + Vite + TypeScript + Element Plus

## 当前进度

### ✅ 已完成（阶段一：后端骨架搭建）

1. **项目结构**
   - Maven 项目配置（pom.xml）
   - Spring Boot 启动类
   - 多环境配置文件（dev/prod）
2. **核心组件**
   - 统一响应体（R, PageResult）
   - 全局异常处理（GlobalExceptionHandler）
   - 常量类和枚举类（7个）
   - 配置类（6个：MybatisPlusConfig, CorsConfig, RedisConfig, AsyncConfig, SwaggerConfig, SecurityConfig）
3. **工具类**
   - JwtUtil - JWT Token生成和验证
   - PasswordUtil - BCrypt密码加密
   - SecurityUtil - 安全上下文工具
   - IpUtil - IP地址获取
   - DesensitizeUtil - 参数脱敏
   - BrowserUtil - 浏览器信息解析
   - CaptchaUtil - 图形验证码生成
4. **数据层**
   - 9个 Entity 实体类
   - 9个 Mapper 接口
   - 数据库初始化脚本（建表 + 初始数据）
5. **测试接口**
   - 健康检查接口：`GET /api/test/health`
   - 系统信息接口：`GET /api/test/info`

### ✅ 已完成（阶段二：Security安全模块）

1. **认证组件**
   - JwtAuthenticationFilter - JWT认证过滤器
   - UserDetailsServiceImpl - 用户详情服务
   - AuthService - 认证服务（登录/登出/刷新Token）
2. **DTO/VO类**
   - LoginDTO - 登录请求
   - LoginVO - 登录响应（双Token）
   - UserInfoVO - 用户信息
3. **认证接口**
   - `GET /api/auth/captcha` - 获取验证码
   - `POST /api/auth/login` - 用户登录
   - `POST /api/auth/refresh` - 刷新Token
   - `POST /api/auth/logout` - 用户登出
4. **安全特性**
   - 双Token机制（AccessToken 30分钟 + RefreshToken 7天）
   - 图形验证码（5分钟有效期，一次性使用）
   - 登录失败锁定（5次失败锁定30分钟）
   - BCrypt密码加密
   - Token Redis缓存（支持主动注销）
   - 登录日志记录

### ✅ 已完成（阶段三：核心业务模块）

1. **用户管理**
   - 分页查询（支持多条件筛选）
   - CRUD操作（创建、查询、更新、删除）
   - 密码重置
   - 角色分配
   - 数据保护（admin用户不可删除）
2. **角色管理**
   - 分页查询
   - 查询所有启用角色
   - CRUD操作
   - 系统内置角色保护
3. **菜单管理**
   - 树形结构查询
   - CRUD操作
   - 权限标识唯一性检查
   - 子菜单检查
   - 系统内置菜单保护
4. **操作日志AOP**
   - @OperationLog注解
   - 自动记录操作日志
   - 记录执行耗时
   - 成功/失败状态

### ✅ 已完成（阶段四：前端开发完整版）

1. **项目初始化**
   - Vue 3 + Vite + TypeScript
   - Element Plus UI组件库
   - Axios HTTP客户端
   - Vue Router路由管理
   - Pinia状态管理
2. **核心功能**
   - 登录页面（含验证码）
   - 主布局（侧边栏+顶部导航）
   - 首页Dashboard
   - 路由守卫（Token认证）
   - Axios拦截器（自动添加Token）
3. **业务页面**
   - 用户管理（列表、搜索、CRUD、重置密码）
   - 角色管理（列表、搜索、CRUD、系统数据保护）
   - 菜单管理（树形展示、CRUD、动态表单）
4. **API封装**
   - api/user.ts - 6个用户接口
   - api/role.ts - 6个角色接口
   - api/menu.ts - 5个菜单接口
5. **开发环境**
   - 前端端口：3000
   - 后端代理：[http://localhost:10040](http://localhost:8080)
   - 热重载支持

✅ 已创建 9 张表：

- sys\_user（用户表）
- sys\_role（角色表）
- sys\_menu（菜单表）
- sys\_api\_permission（接口权限表）
- sys\_operation\_log（操作日志表）
- sys\_login\_log（登录日志表）
- sys\_user\_role（用户-角色关联表）
- sys\_role\_menu（角色-菜单关联表）
- sys\_role\_api（角色-接口权限关联表）

✅ 已初始化基础数据：

- 默认管理员账号：`admin` / `admin123`
- 默认角色：Admin, User
- 示例菜单和权限

### 🚀 如何运行

#### 前置条件

1. JDK 17+
2. Maven 3.6+
3. MySQL 8.0+（Laragon 提供）
4. Redis 7.x（Laragon 提供）

#### 启动步骤

1. **确保 MySQL 和 Redis 正在运行**
   ```bash
   # 检查 MySQL
   D:\laragon\bin\mysql\mysql-8.4.3-winx64\bin\mysql.exe -u root -e "SELECT VERSION();"

   # 检查 Redis
   D:\laragon\bin\redis\redis-x64-5.0.14.1\redis-cli.exe ping
   ```
2. **初始化数据库**（如果尚未执行）
   ```bash
   # 建表
   Get-Content "管理后台\src\main\resources\db\init_schema.sql" | D:\laragon\bin\mysql\mysql-8.4.3-winx64\bin\mysql.exe -u root --default-character-set=utf8mb4

   # 初始化数据
   Get-Content "管理后台\src\main\resources\db\init_data.sql" | D:\laragon\bin\mysql\mysql-8.4.3-winx64\bin\mysql.exe -u root --default-character-set=utf8mb4
   ```
3. **启动应用**
   ```bash
   cd 管理后台
   mvn spring-boot:run
   ```
4. **访问接口**
   - Swagger 文档：[http://localhost:10040/swagger-ui.html](http://localhost:8080/swagger-ui.html)
   - 健康检查：[http://localhost:10040/api/test/health](http://localhost:8080/api/test/health)
   - 系统信息：[http://localhost:10040/api/test/info](http://localhost:8080/api/test/info)

## 默认账号

- **用户名**: admin
- **密码**: admin123

## 下一步计划

### 阶段二：Security 安全模块（Day 3-4）

- [ ] JwtAuthenticationFilter - JWT认证过滤器
- [ ] PermissionInterceptor - 权限拦截器
- [ ] UserDetailsServiceImpl - 用户详情服务
- [ ] LoginController - 登录/登出接口

### 阶段三：核心业务模块（Day 5-10）

- [ ] 用户管理（CRUD + 分页 + 导出）
- [ ] 角色管理
- [ ] 菜单管理
- [ ] 接口权限管理
- [ ] 操作日志记录（AOP）

### 阶段四：前端开发（Day 11-18）

- [ ] Vue 3 项目初始化
- [ ] 登录页面
- [ ] 主布局（侧边栏 + 顶部导航）
- [ ] 用户管理页面
- [ ] 角色管理页面
- [ ] 菜单管理页面

### 阶段五：测试与优化（Day 19-22）

- [ ] 单元测试
- [ ] 集成测试
- [ ] 性能优化
- [ ] 部署配置

## 项目结构

```
smallTweaksScm/
├── 管理后台/
│   ├── src/main/java/com/baserbac/
│   │   ├── BaseRbacApplication.java      # 启动类
│   │   ├── common/
│   │   │   ├── config/                   # 配置类
│   │   │   ├── constant/                 # 常量类
│   │   │   ├── enums/                    # 枚举类
│   │   │   ├── exception/                # 异常类
│   │   │   ├── result/                   # 统一响应
│   │   │   └── util/                     # 工具类
│   │   ├── controller/                   # 控制器
│   │   ├── entity/                       # 实体类
│   │   ├── mapper/                       # Mapper接口
│   │   ├── service/                      # 服务层（待开发）
│   │   └── dto/                          # 数据传输对象（待开发）
│   └── src/main/resources/
│       ├── application.yml               # 配置文件
│       └── db/                           # 数据库脚本
└── docs/                                 # 设计文档
```

## 开发规范

- 编码：UTF-8 无 BOM
- 数据库访问使用 Laragon 提供的客户端
- 优先确保系统快速运行，禁止重构

## 许可证

MIT License
