# jwt-starter 模块详解

## 一、模块架构

```
lhl-starter/
  jwt-starter/                          # 父POM（pom打包）
  ├── jwt-api/                          # API层 — 核心接口与领域模型
  │   ├── JwtUser.kt                    # Spring Security User 扩展
  │   ├── domain/dto/PayloadDTO.kt      # JWT 荷载数据
  │   ├── domain/bo/WxExtBO.kt          # 微信扩展数据
  │   ├── domain/vo/AccountVO.kt        # 账号VO
  │   ├── domain/vo/WxLoginVO.kt        # 微信登录VO
  │   ├── exception/   (6个异常类)       # JWT 异常体系
  │   ├── handler/     (5个处理器)       # 认证成功/失败/刷新/登出处理器
  │   ├── token/       (3个Token类)      # 认证令牌
  │   ├── utils/       (3个工具类)       # JwtUtils, JwtConstant, CacheManager
  │   └── service/JwtUserDetailsService.kt  # 用户详情服务接口
  │
  ├── jwt-autoconfigure/                # 自动配置层
  │   ├── autoconfigure/
  │   │   ├── JwtProperties.kt          # 配置属性 jwt.security.*
  │   │   └── SecurityConfig.kt         # Spring Security 安全配置
  │   ├── security/configs/ (4个)        # HttpSecurity Configurer
  │   ├── security/filters/ (5个)        # Servlet 过滤器链
  │   ├── security/providers/ (3个)      # 认证 Provider
  │   ├── resolver/ (2个)                # @CurrentUser 参数解析器
  │   └── resources/META-INF/spring.factories
  │
  ├── jwt-spring-boot-starter/          # Starter 聚合模块
  │   └── pom.xml                       # 依赖 jwt-autoconfigure + spring-boot
  │
  └── jwt-spring-boot-sample/           # 示例应用
      └── JwtSampleApplication.kt
```

### 依赖关系链

```
jwt-spring-boot-starter
  └── jwt-autoconfigure
       └── jwt-api
            └── lhl-common-core
                 └── (spring-context, hutool, jackson, etc.)
```

---

## 二、核心数据结构

### 2.1 PayloadDTO — JWT 荷载数据

| 字段 | 类型 | 说明 |
|------|------|------|
| `jti` | `String?` | JWT ID，唯一标识 |
| `iss` | `String?` | 签发人 |
| `exp` | `Long?` | 过期时间（epoch秒） |
| `sub` | `String?` | 主题（通常存用户名） |
| `aud` | `String?` | 受众 |
| `nbf` | `Long?` | 生效时间 |
| `iat` | `Long?` | 签发时间 |
| `username` | `String?` | 用户名 |
| `gender` | `String?` | 性别 |
| `birthday` | `String?` | 出生日期 |
| `isAdmin` | `Boolean?` | 是否管理员 |
| `authorities` | `List<String>?` | 权限列表 |

### 2.2 JwtUser — 用户实体

继承 `org.springframework.security.core.userdetails.User`，扩展字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| `salt` | `String?` | 加密盐值 |
| `userId` | `String?` | 用户ID |
| `wxExtBO` | `WxExtBO?` | 微信扩展数据 |

### 2.3 Token 类体系

```
AbstractAuthenticationToken
  ├── JwtAuthenticationToken        # 持有 JWT 令牌的认证
  ├── MobileCodeAuthenticationToken # 手机验证码认证
  └── WxAuthenticationToken        # 微信认证（公众号/小程序/企业微信）
```

#### JwtAuthenticationToken

| 构造方式 | 用途 |
|----------|------|
| `JwtAuthenticationToken(token: String)` | 未认证——从请求头提取 token 后创建 |
| `JwtAuthenticationToken(principal, token, authorities)` | 已认证——Provider 认证成功后的结果 |

- `token: String?` — 原始 JWT 字符串
- `principal: UserDetails?` — 认证后的用户对象

#### MobileCodeAuthenticationToken

| 构造方式 | 用途 |
|----------|------|
| `MobileCodeAuthenticationToken(principal, credentials)` | 未认证——手机号+验证码 |
| `MobileCodeAuthenticationToken(principal, authorities)` | 已认证——认证成功 |

- `principal` — 手机号
- `credentials` — 验证码

#### WxAuthenticationToken

| 字段 | 类型 | 说明 |
|------|------|------|
| `wxId` | `String` | 微信ID（企业号:userId, 公众号:openId, 小程序:openId） |
| `mobile` | `String?` | 手机号 |
| `type` | `String?` | 类型: `cp`(企业微信), `mp`(公众号), `miniApp`(小程序) |

### 2.4 异常体系

```
RuntimeException
  ├── BizException (lhl-common-core)
  │   ├── JwtException          # 通用JWT异常
  │   └── JwtParamException     # 参数异常
  │
  └── AuthenticationException (Spring Security)
      ├── JwtExpireException         # Token过期
      ├── JwtParseException          # Token解析失败
      ├── JwtSignatureVerifyException # 签名验证失败
      └── JwtVerificationFailedException # 验证失败
```

### 2.5 配置属性

前缀: `jwt.security`

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `permitUrls` | `String?` | `null` | 白名单URL（逗号分隔） |
| `tokenRefreshInterval` | `Int` | `0` | Token刷新间隔（秒），0=不刷新 |
| `clearInterval` | `Int` | `60000` | 黑名单清理间隔（毫秒） |

---

## 三、请求处理流程

### 3.1 账号密码登录流程

```
POST /login

客户端                               Filter链                               Provider
  │                                   │                                      │
  │  POST /login (username+password)  │                                      │
  │──────────────────────────────────>│                                      │
  │                                   │                                      │
  │              LoginAuthenticationFilter.attemptAuthentication()           │
  │              从 body 或 param 提取 username/password                      │
  │              创建 UsernamePasswordAuthenticationToken                    │
  │                                   │                                      │
  │                                   │── authenticate(token) ──────────────>│
  │                                   │                                      │
  │                                   │       DaoAuthenticationProvider      │
  │                                   │       ├── loadUserByUsername()       │
  │                                   │       ├── BCryptPasswordEncoder      │
  │                                   │       └── 密码比对                   │
  │                                   │<─────── JwtAuthenticationToken ─────│
  │                                   │       (已认证, 含UserDetails)         │
  │                                   │                                      │
  │              LoginSuccessHandler.onAuthenticationSuccess()               │
  │               ├── JwtUtils.createToken() 生成JWT                         │
  │               └── Response Header: Authorization: Bearer <token>         │
  │<──────────────────────────────────│                                      │
  │ 200 OK (Authorization头)          │                                      │
```

### 3.2 带 Token 请求验证流程

```
GET /api/resource (Authorization: Bearer <token>)

客户端                               Filter链                               Provider
  │                                   │                                      │
  │  GET /api/resource                │                                      │
  │  Authorization: Bearer xxx        │                                      │
  │──────────────────────────────────>│                                      │
  │                                   │                                      │
  │        JwtAuthenticationFilter.doFilterInternal()                        │
  │        1. 检查白名单 → 通过则放行                                        │
  │        2. 检查黑名单 → 在黑名单中则返回过期错误                            │
  │        3. 提取token → 创建JwtAuthenticationToken                         │
  │                                   │                                      │
  │                                   │── authenticate(token) ──────────────>│
  │                                   │                                      │
  │                                   │    JwtAuthenticationProvider         │
  │                                   │    ├── SignedJWT.parse(token)        │
  │                                   │    ├── 检查expirationTime            │
  │                                   │    ├── loadUserByUsername(subject)   │
  │                                   │    └── 返回JwtAuthenticationToken    │
  │                                   │<─────── (已认证, 含UserDetails) ────│
  │                                   │                                      │
  │              JwtRefreshSuccessHandler.onAuthenticationSuccess()          │
  │               ├── 判断是否需要刷新token                                   │
  │               ├── 需要则生成新token并设置到Response Header                │
  │               └── 设置SecurityContext                                    │
  │                                   │                                      │
  │  ← 请求继续执行到 Controller       │                                      │
```

### 3.3 手机验证码登录流程

```
POST /mobileCodeLogin

客户端                               Filter链                               Provider
  │                                   │                                      │
  │  POST /mobileCodeLogin            │                                      │
  │  (mobile=xxx&code=xxx)            │                                      │
  │──────────────────────────────────>│                                      │
  │                                   │                                      │
  │      MobileCodeAuthenticationFilter.attemptAuthentication()              │
  │      从 body 或 param 提取 mobile/code                                    │
  │      创建 MobileCodeAuthenticationToken                                  │
  │                                   │                                      │
  │                                   │── authenticate(token) ──────────────>│
  │                                   │                                      │
  │                                   │  MobileCodeAuthenticationProvider    │
  │                                   │  └── loadUserByMobile(mobile)        │
  │                                   │<─────── JwtAuthenticationToken ─────│
  │                                   │                                      │
  │              LoginSuccessHandler.onAuthenticationSuccess()               │
  │              └── 生成JWT并设置到Response Header                           │
  │<──────────────────────────────────│                                      │
```

### 3.4 微信登录流程

```
POST /wxLogin

客户端                               Filter链                               Provider
  │                                   │                                      │
  │  POST /wxLogin                    │                                      │
  │  (wxId=xxx&mobile=xxx&type=cp)    │                                      │
  │──────────────────────────────────>│                                      │
  │                                   │                                      │
  │       WxAuthenticationFilter.attemptAuthentication()                     │
  │       从 param 提取 wxId/mobile/type                                      │
  │       创建 WxAuthenticationToken                                         │
  │                                   │                                      │
  │                                   │── authenticate(token) ──────────────>│
  │                                   │                                      │
  │                                   │     WxAuthenticationProvider         │
  │                                   │     ├── type="cp"    → loadUserByMobile()  │
  │                                   │     ├── type="mp"    → loadUserByOpenId()   │
  │                                   │     └── type="miniApp" → loadUserByOpenId() │
  │                                   │<─────── JwtAuthenticationToken ─────│
  │                                   │                                      │
  │              LoginSuccessHandler.onAuthenticationSuccess()               │
  │              └── 生成JWT并设置到Response Header                           │
  │<──────────────────────────────────│                                      │
```

### 3.5 登出流程

```
POST /logout (Authorization: Bearer <token>)

客户端                               LogoutHandler链
  │                                   │
  │  POST /logout                     │
  │  Authorization: Bearer xxx        │
  │──────────────────────────────────>│
  │                                   │
  │  1. TokenClearLogoutHandler       │
  │     └── 清除 UserCache 中的用户    │
  │                                   │
  │  2. LogoutBlacklistHandler        │
  │     └── 解析token → 计算剩余过期时间 → 加入黑名单 (CacheManager)    │
  │                                   │
  │  3. HttpStatusReturningLogoutSuccessHandler                            │
  │     └── 返回 200                   │
  │<──────────────────────────────────│
```

---

## 四、SecurityConfig 过滤器链

```
HttpSecurity 配置的过滤器链顺序：

CorsFilter
  │
  ├── OptionsRequestFilter (处理 OPTIONS 预检请求)
  │
  ├── LoginAuthenticationFilter       ──  /**/login POST
  │   └── LoginConfigurer 注册
  │
  ├── WxAuthenticationFilter          ──  /wxLogin POST
  │   └── WxConfigurer 注册
  │
  ├── MobileCodeAuthenticationFilter  ──  /mobileCodeLogin POST
  │   └── MobileConfigurer 注册
  │
  ├── JwtAuthenticationFilter         ──  所有带 Authorization 头的请求
  │   └── JwtAuthenticationConfigurer 注册
  │
  └── LogoutFilter                    ──  /logout
      ├── TokenClearLogoutHandler
      ├── LogoutBlacklistHandler
      └── HttpStatusReturningLogoutSuccessHandler
```

### 安全配置要点

| 配置项 | 说明 |
|--------|------|
| `cors().disable()` | 启用CORS |
| `csrf().disable()` | 禁用CSRF（API服务无需CSRF） |
| `formLogin().disable()` | 禁用表单登录 |
| `sessionManagement().disable()` | 禁用Session（无状态） |
| `CorsUtils.isPreFlightRequest` | 放行OPTIONS预检请求 |
| `permitUrls` 配置 | 白名单URL（无需认证） |
| 默认白名单 | `/mobileCodeLogin/**, /wxLogin/**, /login/**, /logout/**` |

---

## 五、JWT 令牌生成与验证

### 5.1 令牌生成 (`JwtUtils.createToken`)

```
1. 构建 PayloadDTO（含 jti, iat, exp, sub, username, authorities）
2. 序列化为 JSON 字符串
3. 创建 JWSHeader (algorithm: HS256)
4. 创建 JWSObject (header + payload)
5. 创建 MACSigner (key = md5("king" + secret))
6. JWSObject.sign(signer) → 序列化
7. 返回 JWT 字符串
```

### 5.2 令牌验证 (`JwtUtils.getPayLoadByToken`)

```
1. JWSObject.parse(token)
2. 创建 MACVerifier (key = md5("king" + secret))
3. jwsObject.verify(verifier) — 签名验证
4. 解析 payload 为 PayloadDTO
5. 检查 exp 是否过期
6. 返回 PayloadDTO
```

### 5.3 密钥算法

- 算法: `HS256` (HMAC-SHA256)
- 基础密钥: `"king"` (常量 `TOKEN_SECRET`)
- 动态密钥: `md5("king" + secret)` — secret 为用户名，实现每个用户不同密钥
- 过期时间: 120秒（默认）

---

## 六、缓存管理 (`CacheManager`)

基于 `ConcurrentHashMap` 的内存缓存，主要用于 Token 黑名单管理。

| 方法 | 说明 |
|------|------|
| `getData(key)` | 获取缓存数据，已过期返回 null |
| `setData(key, data, expire)` | 设置缓存（expire=0 表示永不过期） |
| `clear(key)` | 清除指定缓存 |
| `clearAll()` | 清除所有缓存 |
| `clearExpire()` | 清除所有已过期的缓存 |

---

## 七、@CurrentUser 参数解析

通过 `LoginUserHandlerMethodArgumentResolver` 实现，在 Controller 方法参数上使用 `@CurrentUser` 注解自动注入当前登录用户名。

```kotlin
@RestController
class UserController {
    @GetMapping("/me")
    fun getCurrentUser(@CurrentUser username: String?): String {
        return "当前用户: $username"
    }
}
```

解析流程：
1. 从请求头 `Authorization` 提取 token
2. 解析 token 获取 subject (用户名)
3. 验证签名和过期时间
4. 设置 `UserContext` (ThreadLocal)
5. 返回用户名

---

## 八、使用方式

### 8.1 引入依赖

```xml
<dependency>
    <groupId>com.lhl</groupId>
    <artifactId>jwt-spring-boot-starter</artifactId>
</dependency>
```

### 8.2 实现 JwtUserDetailsService

```kotlin
@Component
class MyUserDetailsService : JwtUserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        // 从数据库查询用户
        return JwtUser(username, "{bcrypt}encoded_password", listOf())
    }
}
```

### 8.3 配置参数

```yaml
jwt:
  security:
    permit-urls: /api/public/**,/health/**
    token-refresh-interval: 1800  # 30分钟刷新一次
    clear-interval: 60000         # 1分钟清理一次黑名单
```