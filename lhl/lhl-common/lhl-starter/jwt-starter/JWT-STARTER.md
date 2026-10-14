# JWT Starter

基于 Spring Security 7 + nimbus-jose-jwt 的无状态 JWT 认证 starter，提供账号密码 / 手机验证码 / 微信三套登录方式、token 自动续期、refresh token 轮换、登出黑名单，以及 `@CurrentUser` 参数注入。

## 模块结构

| 模块 | 说明 |
| --- | --- |
| `jwt-api` | 核心逻辑：令牌签发/校验、处理器、Store 抽象。不依赖 Spring Boot，可直接单测 |
| `jwt-autoconfigure` | 自动配置、过滤器链、`@CurrentUser` 解析、内置刷新端点 |
| `jwt-spring-boot-starter` | 打包入口，引入即用 |
| `jwt-spring-boot-sample` | 可运行的示例应用，用户数据落 MySQL，覆盖登录 / 校验 / 权限 |

## 快速开始

### 1. 引入依赖

```xml
<dependency>
    <groupId>com.lhl</groupId>
    <artifactId>jwt-spring-boot-starter</artifactId>
</dependency>
```

### 2. 实现用户查询

starter 只定义 SPI，具体存储由业务方实现：

```kotlin
@Service
class MyUserDetailsService(private val mapper: UserMapper) : JwtUserDetailsService {

    // 必选：账号密码登录
    override fun loadUserByUsername(username: String): UserDetails {
        val user = mapper.selectByUsername(username) ?: throw UsernameNotFoundException(username)
        return JwtUser(user.username, user.password, user.authorities.map(::SimpleGrantedAuthority))
    }

    // 可选：手机验证码登录
    override fun loadUserByMobile(mobile: String): UserDetails = loadUserByUsername(mobile)

    // 可选：微信小程序 / 公众号登录
    override fun loadUserByOpenId(openId: String): UserDetails = loadUserByUsername(openId)
}
```

> 未实现的方法默认抛出 `UsernameNotFoundException`，因此不存在的登录方式不会被意外认证通过。

### 3. 配置

```yaml
jwt:
  security:
    # 必填，长度 >= 32，生产环境用环境变量注入
    secret: ${JWT_SECRET}
    issuer: lhl
    audience: lhl-client
    access-token-expiration: 2h
    refresh-token-expiration: 7d
    token-refresh-interval: 30m
    permit-urls:
      - /public/**
    # 完全绕过 security 过滤器链（静态资源，连 JwtAuthenticationFilter 都不经过）
    ignore-urls:
      - /favicon.ico
      - /static/**
    cookie:
      enabled: true
      same-site: Lax
    cors:
      enabled: true
      allowed-origin-patterns:
        - "https://example.com"
```

### 示例应用：jwt-spring-boot-sample

`jwt-spring-boot-sample` 是一个可直接运行的完整示例，用户数据落在 MySQL，登录、校验、权限全链路可测。

### 数据模型

| 表 | 说明 |
| --- | --- |
| `sys_user` | 用户，`password` 为 BCrypt 散列；`mobile` / `wx_open_id` 分别支撑手机验证码登录与微信登录 |
| `sys_role` | 角色与权限合表，`type=1` 是角色（`ROLE_ADMIN`），`type=2` 是权限（`USER_READ`） |
| `sys_user_role` | 用户与角色/权限的多对多关联 |

`schema.sql` 只建表（幂等），演示数据由 `SampleDataInitializer` 在启动时用 `PasswordEncoder` 编码后写入——口令散列没法写在 SQL 里。账号已存在则跳过，重复启动不会重置口令。

### 演示账号

| 账号 | 口令 | 角色 / 权限 | 用途 |
| --- | --- | --- | --- |
| `admin` | `admin123` | `ROLE_ADMIN` + `USER_READ/WRITE/DELETE` | 全部放行 |
| `lhl` | `lhl123` | `ROLE_USER` + `USER_READ` | 只读，其他操作 403 |
| `guest` | `guest123` | 无 | 全部 403 |
| `blocked` | `blocked123` | `ROLE_USER`，但 `status=0` | 登录即被拒 |

### 端点

| 端点 | 方法 | 权限 | 说明 |
| --- | --- | --- | --- |
| `/login` | POST | - | 账号密码登录，token 在响应头 |
| `/mobileCodeLogin` | POST | - | 手机 + 验证码（`/sms/code/{mobile}` 下发，示例固定 `123456`） |
| `/wxLogin` | POST | - | 微信，`type` 支持 `miniApp` / `mp` / `cp` |
| `/refresh` | POST | - | 刷新令牌（轮换） |
| `/logout` | POST | 已认证 | 登出，token 立即失效 |
| `/auth/token` | POST | 已认证 | 返回完整令牌对（含 refreshToken） |
| `/me` | GET | 已认证 | 当前用户：账号、主键、角色权限 |
| `/user/list` | GET | `USER_READ` | 用户列表 |
| `/user/{id}/status` | POST | `USER_WRITE` | 启用 / 禁用 |
| `/user/{id}` | DELETE | `USER_DELETE` | 逻辑删除 |
| `/admin/roles` | GET | `ROLE_ADMIN` | 角色与权限清单 |
| `/public/hello` | GET | - | 白名单 |

权限用方法级注解表达。starter 已开启 `@EnableMethodSecurity`，所以不需要改过滤器链：

```kotlin
@PreAuthorize("hasAuthority('USER_READ')")   // 权限，精确匹配 sys_role.role_code
@PreAuthorize("hasRole('ADMIN')")            // 角色，自动补 ROLE_ 前缀
```

未认证返回 401，已认证但缺权限返回 403。

### 跑起来

```bash
# 1. 建库（MySQL 8）
docker exec -it mysql8 mysql -uroot -padmin \
  -e "CREATE DATABASE IF NOT EXISTS lhl_jwt_sample DEFAULT CHARACTER SET utf8mb4"

# 2. 启动（表与演示数据会自动创建）
mvn -pl lhl-common/lhl-starter/jwt-starter/jwt-spring-boot-sample spring-boot:run
```

连接信息在 `application.yml`，可用环境变量覆盖：`DB_URL` / `DB_USERNAME` / `DB_PASSWORD` / `JWT_SECRET`。

验证登录与权限：

```bash
# 登录，从响应头取 token
curl -i -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" -d '{"username":"admin","password":"admin123"}'

# 带 token 访问受保护端点
curl http://localhost:8080/user/list -H "Authorization: Bearer <token>"
```

也可以直接跑现成的校验脚本：`src/test/http/api.http`（IntelliJ / VS Code HTTP Client 插件），覆盖白名单、三种登录、登录失败、token 轮换与篡改、权限矩阵、登出失效共 35 个请求，每个请求都带 `client.assert` 断言。地址与账号在文件头部用 `@name = value` 定义，无需在 IDE 里切换环境。

两个容易踩的点：

- 响应处理脚本用 `> {% %}`（收到响应后执行），请求前脚本用 `< {% %}`（请求行之前）。写反了 IDE 会报「JSON 标准不允许使用此类令牌」，而且该阶段 `response` 还不存在。
- 需要 `response` 对象的断言只能放 `> {% %}`；脚本块与请求之间要空一行。

> 脚本按文件顺序执行，各段依赖前面捕获的全局变量，请整体运行，不要单独点某一个请求。
> 演示环境里 `/sms/code/{mobile}` 会把验证码直接返回给响应体，仅为方便联调，生产环境务必去掉。

## 静态资源：`ignore-urls` vs `permit-urls`

两者容易混淆，区别在于**是否进入过滤器链**：

| 配置 | 走的链路 | CORS 过滤器 | 适合 |
| --- | --- | --- | --- |
| `permit-urls` | 进入过滤器链，只跳过认证 | 执行 | 需要带跨域头的公开接口 |
| `ignore-urls` | `WebSecurity#ignoring`，整条链都不走 | 不执行 | 图标、JS/CSS、图片等静态资源 |

`ignore-urls` 对应旧版 `WebSecurityConfigurerAdapter#configure(WebSecurity web)` 里的 `web.ignoring()`：不加的话 `/favicon.ico`、`/static/**` 会命中 `anyRequest().authenticated()` 而直接 401。

## 端点

| 端点 | 方法 | 说明 |
| --- | --- | --- |
| `/**/login` | POST | 账号密码登录，请求体 `{ "username": "...", "password": "..." }` |
| `/mobileCodeLogin` | POST | 手机验证码登录，请求体 `{ "mobile": "...", "code": "..." }` |
| `/wxLogin` | POST | 微信登录，请求体 `{ "wxId": "...", "type": "cp/mp/miniApp", "mobile": "..." }` |
| `/refresh` | POST | 刷新令牌，请求体 `{ "refreshToken": "..." }`，返回新的令牌对 |
| `/logout` | POST | 登出：token 加入黑名单立即失效，并吊销全部 refresh token |
| 其余路径 | - | 默认全部需要认证 |

登录成功时 token 写在响应头 `Authorization: Bearer <token>`（并通过 `Access-Control-Expose-Headers` 暴露给浏览器），前端取出后放入后续请求头即可。

> 登录/登出类处理器不写响应体：过滤器链会继续向下执行，写体会导致响应被提前提交。
> 需要在响应体中拿到 token 时，调用 `JwtTokenService#createTokenPair(userDetails)` 自建端点。

## 获取当前登录用户

```kotlin
@GetMapping("/me")
fun me(@CurrentUser username: String): ApiResult<String> = ApiResult.success(data = username)

@GetMapping("/me")
fun me(@CurrentUser user: UserDetails): ApiResult<UserDetails> = ApiResult.success(data = user)
```

`@CurrentUser` 同时会把用户名写入 `UserContext`，业务代码可用 `UserContext.get()` 读取；请求结束后自动清理。

## 认证链路

```
CorsFilter
  └─ OptionsRequestFilter                    OPTIONS 预检直接返回
  └─ LoginAuthenticationFilter               POST 账号密码登录
  └─ MobileCodeAuthenticationFilter          POST 手机验证码登录
  └─ WxAuthenticationFilter                  POST 微信登录
  └─ LogoutFilter                            POST 登出（黑名单 + 吊销 refresh token）
  └─ JwtAuthenticationFilter                 携带 token 的请求
  └─ FilterSecurityInterceptor               授权判定
```

`JwtAuthenticationFilter` 的行为：

1. 请求未携带 token（请求头 / Cookie）时直接放行，由授权规则决定是否需要认证；
2. 命中白名单不做认证；
3. 命中黑名单按过期处理；
4. 认证异常交给失败处理器输出 401 JSON，不再向下执行。

## 安全设计

- **密钥**：`SHA-256(secret + ":" + username)` 派生每用户 HMAC-SHA256 密钥。不同用户的 token 互不可伪造，攻击者也无法自行推导密钥；`secret` 长度必须 >= 32（构造 `JwtTokenService` 时强校验）。
- **强制校验链路**：算法必须是 HS256（拒绝 `alg=none` 混淆）、验签、`exp`、`nbf`、`iss`、`aud`、`tokenType`（refresh token 不能当 access token 用）。
- **refresh token 轮换**：每次 `/refresh` 都会吊销旧的 refresh token；登出时吊销该用户全部 refresh token。
- **登出立即失效**：黑名单 TTL = token 剩余有效期，不会无限增长。
- **JWT 不加密**：不要在 payload 中放密码等敏感信息。

## 可替换组件

所有 Bean 都带 `@ConditionalOnMissingBean`，可逐个替换：

| 接口 | 默认实现 | 说明 |
| --- | --- | --- |
| `TokenBlacklistStore` | `InMemoryTokenBlacklistStore` | 登出黑名单，集群部署请换 Redis |
| `RefreshTokenStore` | `InMemoryRefreshTokenStore` | refresh token 元数据，集群部署请换 Redis |
| `PasswordEncoder` | `BCryptPasswordEncoder` | 口令散列 |
| `MobileCodeValidator` | 基于内存缓存的默认实现 | 短信验证码校验，生产建议接入短信中心 |
| `SecurityFilterChain` | starter 内置 | 完全自定义安全配置时退让 |

## 自定义

- 关闭 starter：`jwt.security.enabled: false`
- 关闭内置刷新端点：`jwt.security.endpoint-enabled: false`
- 修改端点路径：`jwt.security.paths.*`
- 静态资源免认证：`jwt.security.ignore-urls`
- 扩展登录方式：实现对应的 `AbstractAuthenticationProcessingFilter` + `AuthenticationProvider`，再 `http.apply(...)` 插入过滤器链

## 与有状态页面共存（多 SecurityFilterChain）

starter 的链默认是 `STATELESS` 且接管全部路径。如果项目里还有需要 Session 的部分（OAuth2 网页登录、Passkey/WebAuthn、管理后台表单登录），两条链的 Session 策略会冲突。做法是把 starter 限定在 API 前缀下，业务方自己再注册一条链：

```yaml
jwt:
  security:
    security-matchers:
      - /api/**
      - /**/login
      - /**/logout
      - /refresh
```

```kotlin
@Configuration
class WebSecurityConfig {

    /** 有状态的页面链，starter 的链不匹配这些路径时会落到这里 */
    @Bean
    @Order(2)
    fun webFilterChain(http: HttpSecurity): SecurityFilterChain =
        http {
            authorizeHttpRequests { authorize(anyRequest, permitAll) }
        }
}
```

注意：多条链按 `@Order` 顺序匹配，**先匹配到的链生效**，所以有状态的链要放在后面（`@Order` 数值更大）。业务方自己定义 `WebSecurityCustomizer` Bean 也可以覆盖 starter 的 `ignore-urls` 行为。

## 什么时候不该用这个 starter

如果 token 是由**外部统一认证中心签发**的（Keycloak、Auth0、自建 IdP），不要用本 starter 自签自验，改用 Spring Security 原生的资源服务器：

```kotlin
@Bean
fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
    http {
        oauth2ResourceServer { it.jwt(withDefaults()) }
    }
```

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://idp.example.com
```

本 starter 的定位是「单体 / 微服务内部自签 token」，两者可以共存，但不要把自签 token 和外部 JWT 混在同一条链里。

## 测试

```bash
mvn -pl lhl-common/lhl-starter/jwt-starter/jwt-api test
```

覆盖签发/解析、密钥长度、篡改、异密钥、过期、iss/aud、token 类型隔离、刷新轮换、登出吊销、黑名单 TTL 与清理、每用户密钥隔离等。