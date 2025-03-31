# StarInn

## 项目简介

StarInn 是一个基于 Spring Boot 开发的用户账户管理系统，提供完整的用户注册、身份验证和授权功能。该系统使用 JWT (JSON Web Token) 进行安全验证，支持邀请码注册机制及 OAuth2 第三方登录。

## 作者赞助通道
如果您觉得本项目对您有帮助，欢迎通过以下方式支持作者：
- [爱发电链接](https://afdian.com/a/FadedTUMI)

## 主要功能

- 用户注册（需要邀请码）
- 用户登录与身份验证
- JWT 令牌管理
- 刷新令牌机制
- 管理员用户管理
- 手机号码验证
- Google OAuth2 第三方登录
- 用户信息管理

## 技术栈

- Java
- Spring Boot
- Spring Security
- Spring Data JPA
- MySQL 数据库
- Maven
- JWT 认证

## 环境要求

- JDK 11+
- Maven 3.6+
- MySQL 8.0+
- IDE (建议使用 IntelliJ IDEA)

## 快速开始

### 1. 克隆仓库

```bash
git clone https://github.com/FADEDTUMI/StarInn.git
cd StarInn
```

### 2. 配置数据库

在 MySQL 中创建数据库：

```sql
CREATE DATABASE android_app CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 修改配置

编辑 `src/main/resources/application.properties` 文件，根据您的环境修改以下配置：

```properties
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/android_app?useSSL=false&serverTimezone=UTC&characterEncoding=utf8&allowPublicKeyRetrieval=true
spring.datasource.username=您的数据库用户名
spring.datasource.password=您的数据库密码

# 邀请码配置
app.invitation.code=您的邀请码

# JWT密钥配置（建议修改为更安全的密钥）
app.jwt.secret=您的JWT密钥

# 管理员注册密钥
admin.registration.key=您的管理员注册密钥
```

### 4. 编译和运行

```bash
mvn clean package
java -jar target/StarInn-0.0.1-SNAPSHOT.jar
```

或者直接使用 Maven 运行：

```bash
mvn spring-boot:run
```

应用将在 `http://localhost:8080` 启动。

## API 接口说明

### 用户注册

```
POST /api/auth/register
```

请求体：
```json
{
  "username": "用户名",
  "email": "邮箱地址",
  "password": "密码",
  "fullName": "全名",
  "phoneNumber": "手机号码",
  "invitationCode": "邀请码"
}
```

### 用户登录

```
POST /api/auth/login
```

请求体：
```json
{
  "username": "用户名",
  "password": "密码"
}
```

### 刷新令牌

```
POST /api/auth/refresh-token
```

请求体：
```json
{
  "refreshToken": "刷新令牌"
}
```

### 注销登录

```
POST /api/auth/logout
```

### 管理员注册

```
POST /api/auth/register/admin
```

请求体：
```json
{
  "username": "管理员用户名",
  "email": "管理员邮箱",
  "password": "密码",
  "fullName": "全名",
  "phoneNumber": "手机号码"
}
```

参数：
```
adminKey=您的管理员注册密钥
```

### 管理员权限检查

```
GET /api/admin/check-permission
```

## 项目结构

```
src/main/java/com/fadedtumi/starinn/
├── config/             # 配置类
├── controller/         # API 控制器
├── dto/                # 数据传输对象
├── entity/             # 数据实体类
├── repository/         # 数据访问层
├── security/           # 安全配置
└── service/            # 业务逻辑服务
```

## 安全配置说明

本项目使用 JWT 进行身份验证，所有敏感操作需要有效的身份令牌。默认令牌有效期为 24 小时，刷新令牌有效期为 7 天。

## 常见问题

### 邀请码无效

确保在 `application.properties` 中正确配置了邀请码：

```properties
app.invitation.code=FIRST
```

### 数据库连接问题

检查数据库配置是否正确，确保 MySQL 服务已启动，并且用户有正确的数据库访问权限。

### JWT 相关问题

如遇到令牌验证问题，请检查 JWT 密钥配置是否正确，以及系统时间是否同步。

## 后续开发计划

- 完善邮箱验证功能
- 增强用户个人资料管理
- 添加更多第三方登录选项
- 实现用户权限精细化管理

## 贡献指南

1. Fork 本仓库
2. 创建您的功能分支 (`git checkout -b feature/amazing-feature`)
3. 提交您的修改 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 开启一个 Pull Request

## 许可证

[Apache License 2.0](license)

## 联系方式

如有任何问题或建议，请通过 GitHub Issue 与我们联系。

## 项目商业化许可

本项目的商业化许可需要联系作者进行授权，非商业化引用请必须标明代码来源，禁止删除代码中原作者任何记号。

## 免责声明

本项目仅供学习和研究使用，作者不对任何因使用本项目而导致的直接或间接损失承担责任。请遵守相关法律法规。