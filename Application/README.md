# Application 后端服务

Java 21 + Spring Boot 3.2 管理后台 API。

## 启动前准备

- JDK 21
- Maven 3.9+
- MySQL（库名 `project_management`，初始化脚本见 `sql/project_management.sql`）
- Redis

## 常用命令

```bash
mvn spring-boot:run
mvn compile
mvn package -DskipTests
mvn test -Dmaven.test.skip=false
```

## 权限说明

- 登录认证由 `AuthInterceptor` 处理
- 接口授权使用 Sa-Token `@SaCheckPermission`，权限码与 `sys_menu.permission` 对齐
- `SUPER_ADMIN` 角色拥有全部权限

## 测试注意

若本机 Maven 全局 `settings.xml` 配置了 `maven.test.skip=true`，执行测试时需显式覆盖：

```bash
mvn test -Dmaven.test.skip=false
```
