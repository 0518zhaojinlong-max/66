# Spring Boot 用户注册网站

基于 Spring Boot 3 的简单用户注册 / 登录网站。

## 技术栈

- Spring Boot 3.3（Web、Thymeleaf、Validation、Data JPA、Security）
- H2 数据库（文件模式，数据保存在 `./data/`）
- BCrypt 密码加密
- Java 17+

## 功能

- 用户注册：用户名、邮箱、密码、确认密码
  - 用户名 3-32 位，仅限字母、数字、下划线
  - 邮箱格式校验，忽略大小写判重
  - 密码至少 8 位，需包含字母和数字，两次输入需一致
  - 用户名 / 邮箱重复检测（数据库唯一约束兜底）
- 用户登录 / 退出（Spring Security 表单登录，CSRF 防护）
- 登录后的欢迎首页

## 运行

```bash
mvn spring-boot:run
```

浏览器访问：

- 注册页：http://localhost:8080/register
- 登录页：http://localhost:8080/login
- H2 控制台：http://localhost:8080/h2-console（JDBC URL：`jdbc:h2:file:./data/userdb`，用户名 `sa`，密码为空）

打包为可执行 jar：

```bash
mvn package
java -jar target/user-registration-0.0.1-SNAPSHOT.jar
```

## 测试

```bash
mvn test
```

## 目录结构

```
src/main/java/com/example/registration
├── RegistrationApplication.java   # 启动类
├── config/SecurityConfig.java     # 安全配置、密码加密
├── controller/AuthController.java # 注册、登录、首页
├── dto/RegistrationForm.java      # 注册表单及校验规则
├── model/User.java                # 用户实体
├── repository/UserRepository.java # 数据访问
└── service/UserService.java       # 注册逻辑、UserDetailsService
src/main/resources
├── application.properties
├── static/css/style.css
└── templates/{register,login,home}.html
```
