# 3.2.2 接口设计

## 一、概述

本文档详细描述智能养老助手中的系统接口和模块接口，包括数据交换格式、通信协议等。

---

## 二、系统间接口（External System Interfaces）

### 2.1 Bmob REST API 接口

**接口说明**：与Bmob后端云服务通信的REST API接口，采用HTTP JSON通信协议。

**基础URL**：`http://api.bmobcloud.com/1/`

**认证方式**：通过HTTP Header传递应用密钥
- `X-Bmob-Application-Id`: `b9ab00843ca0d46f85a3d1f8c317164a`
- `X-Bmob-REST-API-Key`: `fb2a1df255a59958df971f18472cb2b4`

#### 2.1.1 文件上传接口

| 接口路径 | 方法 | 说明 |
|---------|------|------|
| `/files/{fileName}` | POST | 上传文件到Bmob CDN |

**请求格式**：
```
Content-Type: multipart/form-data
```

**请求参数**：
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| fileName | String | 是 | 文件名（含扩展名） |
| file | File | 是 | 二进制文件内容 |

**响应格式**：
```json
{
  "url": "http://bmob-cdn-31634.bmobpay.com/xxx.png",
  "filename": "xxx.png",
  "cdn": "...",
  "mime_type": "image/png",
  "objectId": "xxx",
  "createdAt": "2024-01-01 12:00:00"
}
```

#### 2.1.2 数据操作接口

| 接口路径 | 方法 | 说明 |
|---------|------|------|
| `/classes/{tableName}` | GET | 查询表数据 |
| `/classes/{tableName}` | POST | 创建数据 |
| `/classes/{tableName}/{objectId}` | PUT | 更新数据 |
| `/classes/{tableName}/{objectId}` | DELETE | 删除数据 |
| `/classes/{tableName}/{objectId}` | GET | 获取单条数据 |

**GET查询参数**：
| 参数名 | 类型 | 说明 |
|-------|------|------|
| where | String | JSON格式查询条件，如 `{"userId":"xxx"}` |
| order | String | 排序字段，如 `-createdAt` 表示降序 |
| limit | Int | 返回记录数限制，默认25 |
| skip | Int | 跳过记录数，用于分页 |

**通用响应格式**：
```json
{
  "results": [...],
  "objectId": "xxx",
  "createdAt": "2024-01-01 12:00:00",
  "updatedAt": "2024-01-01 12:00:00"
}
```

#### 2.1.3 用户管理接口

| 接口路径 | 方法 | 说明 |
|---------|------|------|
| `/users` | POST | 用户注册 |
| `/login` | GET | 用户登录 |
| `/users/{objectId}` | GET | 获取用户信息 |
| `/users/{objectId}` | PUT | 更新用户信息 |

**登录请求参数**：
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| username | String | 是 | 用户名（手机号） |
| password | String | 是 | 密码 |

---

### 2.2 Kimi AI API 接口

**接口说明**：与Moonshot（Kimi）大模型通信的AI对话接口。

**基础URL**：`https://api.moonshot.cn/v1/chat/completions`

**认证方式**：Bearer Token认证

**请求格式**：
```json
{
  "model": "kimi-k2-0905-preview",
  "messages": [
    {
      "role": "user",
      "content": "用户输入的文本"
    }
  ],
  "temperature": 0.7,
  "max_tokens": 1000
}
```

**响应格式**：
```json
{
  "choices": [
    {
      "message": {
        "role": "assistant",
        "content": "AI回复内容"
      }
    }
  ],
  "usage": {...}
}
```

**数据交换说明**：
- 用户输入文本通过`content`字段传递
- AI回复内容从`choices[0].message.content`提取
- 错误信息从`error.message`字段获取

---

### 2.3 Weather API 接口

**接口说明**：获取天气预报信息的外部API接口。

**基础URL**：`https://api.open-meteo.com/v1/forecast`

**认证方式**：无需认证（公开API）

**请求参数**：
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| latitude | Double | 是 | 纬度 |
| longitude | Double | 是 | 经度 |
| current | String | 是 | 当前天气参数，如 `temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m,wind_direction_10m` |
| timezone | String | 否 | 时区，默认 `auto` |

**响应数据格式**：
```json
{
  "current": {
    "temperature_2m": "20",
    "relative_humidity_2m": "65",
    "weather_code": 1,
    "wind_speed_10m": "10",
    "wind_direction_10m": 180
  }
}
```

**天气码映射（WMO Code）**：
| 天气码 | 描述 | 图标 |
|-------|------|------|
| 0 | 晴 | ☀️ |
| 1-3 | 多云 | 🌤️/⛅/☁️ |
| 45/48 | 雾 | 🌫️ |
| 51-55 | 小雨 | 🌧️ |
| 61-65 | 中雨 | 🌧️ |
| 71-75 | 雪 | ❄️ |
| 95-99 | 雷暴 | ⛈️ |

---

## 三、模块接口（Module Interfaces）

### 3.1 BmobRepository 数据仓库接口

**接口说明**：封装所有Bmob后端云CRUD操作的核心仓库类，是数据访问层的统一入口。

**通信方式**：基于Retrofit的HTTP请求，结果通过`Result<T>`封装返回。

#### 3.1.1 用户相关接口

| 方法 | 说明 | 返回类型 |
|------|------|---------|
| `login(phone, password)` | 用户登录 | `Result<User>` |
| `register(phone, password, nickname)` | 用户注册 | `Result<User>` |
| `getUserByPhone(phone)` | 通过手机号获取用户 | `Result<User>` |
| `updateUser(objectId, user)` | 更新用户信息 | `Result<User>` |
| `changePassword(phone, oldPwd, newPwd)` | 修改密码 | `Result<Unit>` |
| `isLoggedIn()` | 检查登录状态 | `Boolean` |
| `getCurrentUser()` | 获取当前用户 | `User?` |

**Login请求数据格式**：
```kotlin
data class LoginRequest(
    val phone: String,
    val password: String
)
```

**User响应数据格式**：
```kotlin
data class User(
    val id: String? = null,
    val objectId: String? = null,
    val phone: String,
    val password: String? = null,
    val nickname: String? = null,
    val realName: String? = null,
    val avatarUrl: String? = null,
    val gender: String? = null,
    val birthDate: String? = null,
    val address: String? = null,
    val emergencyContact: String? = null,
    val emergencyPhone: String? = null,
    val bloodType: String? = null,
    val medicalHistory: String? = null,
    val signature: String? = null,
    val token: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
```

#### 3.1.2 帖子相关接口

| 方法 | 说明 | 返回类型 |
|------|------|---------|
| `createPost(post)` | 创建帖子 | `Result<Post>` |
| `getAllPosts()` | 获取所有帖子 | `Result<List<Post>>` |
| `getPostsByCategory(category)` | 按分类获取帖子 | `Result<List<Post>>` |
| `getPostsByUserId(userId)` | 获取用户帖子 | `Result<List<Post>>` |
| `getPostById(objectId)` | 获取单条帖子 | `Result<Post?>` |
| `updatePost(objectId, post)` | 更新帖子 | `Result<Post>` |
| `deletePost(objectId)` | 删除帖子 | `Result<Unit>` |
| `incrementPostLikes(objectId)` | 点赞+1 | `Result<Unit>` |
| `decrementPostLikes(objectId)` | 点赞-1 | `Result<Unit>` |
| `incrementPostFavorites(objectId)` | 收藏+1 | `Result<Unit>` |
| `decrementPostFavorites(objectId)` | 收藏-1 | `Result<Unit>` |

**Post数据格式**：
```kotlin
data class Post(
    val id: Long? = null,
    val objectId: String? = null,
    val userId: String,
    val title: String,
    val content: String,
    val category: String = "",
    val coverImageUrl: String? = null,
    val imageUrls: String? = null,
    val userName: String,
    val userAvatarUrl: String? = null,
    val likeCount: Int = 0,
    val favoriteCount: Int = 0,
    val commentCount: Int = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
```

**PostBmob Bmob表格式**（对应Bmob表名: `Post`）：
```json
{
  "objectId": "xxx",
  "createdAt": "2024-01-01 12:00:00",
  "userId": "用户ID",
  "title": "帖子标题",
  "content": "帖子内容",
  "category": "情感/生活/健康",
  "coverImageUrl": "封面图URL",
  "imageUrls": "图片URL列表，逗号分隔",
  "userName": "用户名",
  "userAvatarUrl": "用户头像URL",
  "likeCount": 10,
  "favoriteCount": 5,
  "commentCount": 3
}
```

#### 3.1.3 评论相关接口

| 方法 | 说明 | 返回类型 |
|------|------|---------|
| `getCommentsByPostId(postId)` | 获取帖子评论 | `Result<List<Comment>>` |
| `addComment(comment)` | 添加评论 | `Result<Comment>` |
| `deleteComment(objectId)` | 删除评论 | `Result<Unit>` |

**Comment数据格式**：
```kotlin
data class Comment(
    val id: Long? = null,
    val objectId: String? = null,
    val postId: String,
    val authorName: String,
    val authorAvatar: String? = null,
    val content: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
```

**CommentBmob Bmob表格式**（对应Bmob表名: `Comment`）：
```json
{
  "objectId": "xxx",
  "createdAt": "2024-01-01 12:00:00",
  "postId": "关联帖子ID",
  "authorName": "评论者名称",
  "authorAvatar": "评论者头像URL",
  "content": "评论内容"
}
```

#### 3.1.4 纪念星相关接口

| 方法 | 说明 | 返回类型 |
|------|------|---------|
| `createMemorialStar(data)` | 创建纪念星 | `Result<MemorialStarBmob>` |
| `getAllMemorialStars()` | 获取所有纪念星 | `Result<List<MemorialStarBmob>>` |
| `getMemorialStarById(objectId)` | 获取单条纪念星 | `Result<MemorialStarBmob?>` |
| `updateMemorialStar(objectId, data)` | 更新纪念星 | `Result<Unit>` |
| `incrementMemorialFlowers(objectId)` | 献花+1 | `Result<Unit>` |

**MemorialStarBmob Bmob表格式**（对应Bmob表名: `MemorialStar`）：
```json
{
  "objectId": "xxx",
  "createdAt": "2024-01-01 12:00:00",
  "name": "逝者姓名",
  "lifeYears": "1945年 - 2023年",
  "message": "寄语",
  "story": "生平事迹",
  "avatarUrl": "头像URL",
  "flowerCount": 0,
  "createdBy": "创建者用户ID",
  "status": "pending/approved/rejected"
}
```

**MemorialData 前端展示格式**：
```kotlin
data class MemorialData(
    val id: String,
    val name: String,
    val lifeYears: String,
    val message: String,
    val story: String,
    val imageUrl: String?,
    val flowerCount: Int = 0
)
```

#### 3.1.5 身后事计划相关接口

| 方法 | 说明 | 返回类型 |
|------|------|---------|
| `createAfterlifePlan(plan)` | 创建身后事计划 | `Result<AfterlifePlan>` |
| `getAfterlifePlansByUserId(userId)` | 获取用户身后事计划 | `Result<List<AfterlifePlan>>` |
| `getAllAfterlifePlans()` | 获取所有身后事计划 | `Result<List<AfterlifePlan>>` |
| `updateAfterlifePlan(objectId, plan)` | 更新身后事计划 | `Result<AfterlifePlan>` |
| `deleteAfterlifePlan(objectId)` | 删除身后事计划 | `Result<Unit>` |
| `batchUploadAfterlifePlans(plans)` | 批量上传计划 | `Result<List<AfterlifePlan>>` |

**AfterlifePlan数据格式**：
```kotlin
data class AfterlifePlan(
    val id: Long? = null,
    val userId: String,
    val name: String,
    val age: String,
    val contact: String,
    val biography: String? = null,
    val funeralStyle: String,
    val funeralLocation: String? = null,
    val funeralDate: String? = null,
    val funeralTime: String? = null,
    val relicsHandling: String,
    val burialMethod: String,
    val burialLocation: String? = null,
    val coffin: Boolean = false,
    val urn: Boolean = false,
    val flowers: Boolean = false,
    val candles: Boolean = false,
    val photos: Boolean = false,
    val otherSupplies: String? = null,
    val bgm: String,
    val bgmNotes: String? = null,
    val visitDate: String? = null,
    val visitTime: String? = null,
    val visitNotes: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
```

**AfterlifePlanBmob Bmob表格式**（对应Bmob表名: `AfterlifePlan`）：
```json
{
  "objectId": "xxx",
  "createdAt": "2024-01-01 12:00:00",
  "userId": "用户ID",
  "name": "姓名",
  "age": "年龄",
  "contact": "联系方式",
  "biography": "生平简介",
  "funeralInfo": "{\"style\":\"风格\",\"location\":\"地点\",\"date\":\"日期\",\"time\":\"时间\"}",
  "relicsInfo": "{\"handling\":\"处理方式\",\"method\":\"埋葬方式\",\"location\":\"位置\"}",
  "supplies": "{\"coffin\":true,\"urn\":false,\"flowers\":true,...}",
  "bgmInfo": "{\"name\":\"音乐名\",\"notes\":\"备注\"}"
}
```

**AfterlifePlanVBmob Bmob表格式**（对应Bmob表名: `AfterlifePlan_V`，祭拜信息）：
```json
{
  "objectId": "xxx",
  "planId": "关联计划ID",
  "userId": "用户ID",
  "visitDate": "祭拜日期",
  "visitTime": "祭拜时间",
  "visitNotes": "祭拜备注"
}
```

---

### 3.1.5.1 "我的"页面身后事计划 Bmob 表结构

**接口说明**：用户"我的"页面展示身后事计划的数据来源。

**数据来源**：
- **主数据**：Bmob云端 `AfterlifePlan` 表
- **备用数据**：本地 SharedPreferences (`afterlife`)

**数据流向**：
```
MineActivity
    ├── afterlifeViewModel.getAfterlifePlansByUserId(userId)
    │       └── BmobRepository → BmobApiService → Bmob表(AfterlifePlan)
    │
    └── loadFromLocal() [备用]
            └── SharedPreferences("afterlife")
```

**AfterlifePlan Bmob表结构**（对应"我的"页面身后事计划）：

| 字段名 | 类型 | 说明 | 示例值 |
|-------|------|------|--------|
| objectId | String | 记录唯一标识 | "abc123" |
| createdAt | String | 创建时间 | "2024-01-01 12:00:00" |
| updatedAt | String | 更新时间 | "2024-01-01 12:00:00" |
| userId | String | 用户ID | "user_001" |
| name | String | 逝者姓名 | "张三" |
| age | String | 年龄 | "75" |
| contact | String | 联系方式 | "13800138000" |
| biography | String | 生平简介 | "退休教师，热爱生活" |
| funeralInfo | String | 葬礼信息(JSON) | `{"style":"","location":"","date":"","time":""}` |
| relicsInfo | String | 遗物处理信息(JSON) | `{"handling":"","method":"","location":""}` |
| supplies | String | 殡葬用品(JSON) | `{"coffin":false,"urn":false,"flowers":true,...}` |
| bgmInfo | String | 背景音乐信息(JSON) | `{"name":"","notes":""}` |

**JSON子字段详细说明**：

1. **funeralInfo**（葬礼信息）：
```json
{
  "style": "传统葬礼",      // 葬礼风格
  "location": "八宝山",     // 葬礼地点
  "date": "2024-05-01",    // 葬礼日期
  "time": "09:00"          // 葬礼时间
}
```

2. **relicsInfo**（遗物处理）：
```json
{
  "handling": "纪念保存",   // 遗物处理方式
  "method": "树葬",         // 下葬方式
  "location": "北京"        // 下葬地点
}
```

3. **supplies**（殡葬用品）：
```json
{
  "coffin": true,    // 棺材
  "urn": false,      // 骨灰盒
  "flowers": true,   // 鲜花
  "candles": true,   // 蜡烛
  "photos": true,    // 照片
  "other": ""        // 其他用品
}
```

4. **bgmInfo**（背景音乐）：
```json
{
  "name": "天空之城",   // 音乐名称
  "notes": "生前喜爱"    // 备注
}
```

**本地 SharedPreferences 字段**（`afterlife`）：

| 字段名 | 类型 | 说明 |
|-------|------|------|
| hasPlan | Boolean | 是否已配置计划 |
| name | String | 姓名 |
| age | String | 年龄 |
| funeralStyle | String | 葬礼风格 |
| relicsHandling | String | 遗物处理 |
| burialMethod | String | 下葬方式 |
| bgm | String | 背景音乐 |
| supplies | Set<String> | 殡葬用品集合 |

**"我的"页面数据展示逻辑**：
```kotlin
// 优先显示云端数据
afterlifeViewModel.afterlifePlans.observe { plans ->
    if (plans.isNotEmpty()) {
        // 显示云端第一条计划
        val plan = plans[0]
        tvAfterlifePlan.text = """
            姓名: ${plan.name}
            年龄: ${plan.age}
            葬礼风格: ${plan.funeralStyle ?: "未设置"}
            遗物处理: ${plan.relicsHandling ?: "未设置"}
            下葬方式: ${plan.burialMethod ?: "未设置"}
            殡葬用品: ${suppliesText}
            背景音乐: ${plan.bgm ?: "无"}
        """.trimIndent()
    } else {
        // 云端无数据，加载本地备用数据
        loadFromLocal()
    }
}

// 本地备用数据加载
private fun loadFromLocal() {
    val prefs = getSharedPreferences("afterlife", MODE_PRIVATE)
    if (prefs.getBoolean("hasPlan", false)) {
        // 显示本地数据
    } else {
        tvAfterlifePlan.text = "未配置身后事计划"
    }
}
```

#### 3.1.5.2 祭拜信息 Bmob 表（AfterlifePlan_V）

| 字段名 | 类型 | 说明 |
|-------|------|------|
| objectId | String | 记录唯一标识 |
| planId | String | 关联的 AfterlifePlan.objectId |
| userId | String | 用户ID |
| visitDate | String | 祭拜日期 |
| visitTime | String | 祭拜时间 |
| visitNotes | String | 祭拜备注 |

#### 3.1.5.3 API 调用示例

**查询用户身后事计划**：
```http
GET /classes/AfterlifePlan?where={"userId":"user_001"}&order=-createdAt
Headers:
  X-Bmob-Application-Id: b9ab00843ca0d46f85a3d1f8c317164a
  X-Bmob-REST-API-Key: fb2a1df255a59958df971f18472cb2b4
```

**响应**：
```json
{
  "results": [
    {
      "objectId": "abc123",
      "createdAt": "2024-01-01 12:00:00",
      "updatedAt": "2024-01-01 12:00:00",
      "userId": "user_001",
      "name": "张三",
      "age": "75",
      "contact": "13800138000",
      "biography": "退休教师",
      "funeralInfo": "{\"style\":\"传统\",\"location\":\"八宝山\",\"date\":\"2024-05-01\",\"time\":\"09:00\"}",
      "relicsInfo": "{\"handling\":\"纪念保存\",\"method\":\"树葬\",\"location\":\"北京\"}",
      "supplies": "{\"coffin\":true,\"urn\":false,\"flowers\":true,\"candles\":true,\"photos\":true,\"other\":\"\"}",
      "bgmInfo": "{\"name\":\"天空之城\",\"notes\":\"生前喜爱\"}"
    }
  ]
}
```

#### 3.1.6 打卡记录相关接口

| 方法 | 说明 | 返回类型 |
|------|------|---------|
| `getCheckinRecord(userId)` | 获取用户打卡记录 | `Result<CheckinRecord?>` |
| `createOrUpdateCheckin(record)` | 创建/更新打卡记录 | `Result<CheckinRecord>` |

**CheckinRecord数据格式**：
```kotlin
data class CheckinRecord(
    val id: Long? = null,
    val objectId: String? = null,
    val userId: String,
    val nickname: String,
    val avatarUrl: String? = null,
    val totalPoints: Int = 0,
    val consecutiveDays: Int = 0,
    val lastCheckinDate: String = "",
    val createdAt: String? = null,
    val updatedAt: String? = null
)
```

#### 3.1.7 帖子互动相关接口

| 方法 | 说明 | 返回类型 |
|------|------|---------|
| `addInteraction(interaction)` | 添加互动记录 | `Result<PostInteraction>` |
| `removeInteraction(objectId)` | 移除互动记录 | `Result<Unit>` |
| `getInteraction(userId, postId, type)` | 获取用户对帖子的互动 | `Result<PostInteraction?>` |
| `getUserInteractions(userId, type)` | 获取用户所有互动 | `Result<List<PostInteraction>>` |

**PostInteraction数据格式**：
```kotlin
data class PostInteraction(
    val id: Long? = null,
    val objectId: String? = null,
    val userId: String,
    val postId: String,
    val type: String,  // "like" 或 "favorite"
    val createdAt: String? = null
)
```

---

### 3.2 ElderlyDataRepository 长辈数据仓库接口

**接口说明**：统一管理AI语音助手所需的用户/长辈数据，包含SharedPreferences本地存储和Bmob远程数据访问。

**通信方式**：混合模式（SharedPreferences + BmobRepository）

| 方法 | 说明 | 返回类型 |
|------|------|---------|
| `getElderlyStatus()` | 获取AI Prompt所需的长辈状态 | `PromptBuilder.ElderlyStatus` |
| `getCurrentUser()` | 获取当前登录用户 | `User?` |
| `getCurrentUserId()` | 获取当前用户ID | `String?` |
| `getAvatarUrl()` | 获取用户头像URL | `String?` |
| `getNickname()` | 获取用户昵称 | `String?` |
| `recordMedicationTaken()` | 记录今日已服药 | `Unit` |

**ElderlyStatus数据格式**：
```kotlin
data class ElderlyStatus(
    val name: String,                    // 长辈姓名
    val age: Int,                        // 年龄
    val gender: String = "未知",          // 性别
    val todaySteps: Int = 0,             // 今日步数
    val stepGoal: Int = 6000,            // 步数目标
    val medicationStatus: MedicationStatus, // 用药状态
    val healthNotes: String = "",        // 健康备注
    val mood: String = "正常"             // 情绪状态
)

enum class MedicationStatus {
    NONE,    // 无用药
    TAKEN,   // 已服药
    PENDING, // 待服药
    MISSED   // 漏服
}
```

---

### 3.3 PromptBuilder AI提示词构建器接口

**接口说明**：构建适老化人设System Prompt的工具类，用于Kimi AI对话。

**通信方式**：本地JSON构建，输出Kimi API所需的messages格式

| 方法 | 说明 | 返回类型 |
|------|------|---------|
| `buildSystemPrompt(status)` | 构建适老化人设System Prompt | `String` |
| `buildKimiRequestJson(status, userMessage, history)` | 构建完整Kimi请求JSON | `JSONObject` |

**buildSystemPrompt输出格式示例**：
```
你是「好好活」智能养老助手的 AI 语音助手。你正在与用户（通常是老年人）对话，他们关心自己的健康与日常生活。

【老人今日概况】
- 老人姓名：张三
- 年龄：75岁（男性）
- 今日步数：3500步（目标 6000 步，完成率 58%）
- 用药状态：您今日尚有药物未服用，请注意按时服药。
- 情绪状态：正常

【服务要求】
1. 语言风格：温和、耐心、简洁，避免过于专业的医学术语
2. 回复长度：针对中老年人习惯，回复控制在50-150字以内
...
```

---

### 3.4 DataSyncManager 数据同步管理器接口

**接口说明**：负责本地数据与Bmob服务器的双向数据同步。

**通信方式**：本地缓存 + BmobRepository远程同步

| 方法 | 说明 | 返回类型 |
|------|------|---------|
| `syncAllData()` | 同步所有本地数据到服务器 | `SyncResult` |
| `syncAfterlifePlans()` | 同步身后事计划 | `SyncResult` |
| `syncPosts()` | 同步帖子 | `SyncResult` |
| `fetchLatestData()` | 从服务器获取最新数据 | `SyncResult` |

**SyncResult同步结果格式**：
```kotlin
data class SyncResult(
    val success: Boolean,          // 是否完全成功
    val uploadedCount: Int,        // 上传成功数量
    val failedCount: Int,         // 失败数量
    val message: String            // 结果描述信息
)
```

**同步状态常量**：
| 常量 | 值 | 说明 |
|------|------|------|
| `SYNC_STATUS_PENDING` | `"pending"` | 待同步 |
| `SYNC_STATUS_SYNCED` | `"synced"` | 已同步 |
| `SYNC_STATUS_FAILED` | `"failed"` | 同步失败 |

---

### 3.5 ViewModel层接口

#### 3.5.1 PostViewModel 帖子视图模型

**接口说明**：管理帖子列表UI状态，协调BmobRepository调用。

| LiveData属性 | 类型 | 说明 |
|-------------|------|------|
| `isLoading` | `LiveData<Boolean>` | 加载状态 |
| `errorMessage` | `LiveData<String?>` | 错误信息 |
| `successMessage` | `LiveData<String?>` | 成功提示 |
| `posts` | `LiveData<List<Post>>` | 帖子列表 |
| `currentPost` | `LiveData<Post?>` | 当前操作的帖子 |

| 方法 | 说明 |
|------|------|
| `createPost(post)` | 创建帖子 |
| `getAllPosts()` | 获取所有帖子 |
| `getPostsByUserId(userId)` | 获取用户帖子 |
| `getPostsByCategory(category)` | 按分类获取帖子 |
| `updatePost(objectId, post)` | 更新帖子 |
| `deletePost(objectId)` | 删除帖子 |
| `likePost(objectId)` | 点赞帖子 |
| `unlikePost(objectId)` | 取消点赞 |
| `favoritePost(objectId)` | 收藏帖子 |
| `unfavoritePost(objectId)` | 取消收藏 |

#### 3.5.2 UserViewModel 用户视图模型

**接口说明**：管理用户登录注册UI状态，协调BmobRepository调用。

| LiveData属性 | 类型 | 说明 |
|-------------|------|------|
| `isLoading` | `LiveData<Boolean>` | 加载状态 |
| `errorMessage` | `LiveData<String?>` | 错误信息 |
| `successMessage` | `LiveData<String?>` | 成功提示 |
| `currentUser` | `LiveData<User?>` | 当前登录用户 |

| 方法 | 说明 |
|------|------|
| `login(phone, password)` | 用户登录 |
| `register(phone, password, nickname)` | 用户注册 |
| `getUserByPhone(phone)` | 获取用户信息 |
| `updateUser(objectId, user)` | 更新用户信息 |
| `changePassword(phone, oldPwd, newPwd)` | 修改密码 |
| `fetchCurrentUser()` | 获取当前用户（从缓存） |
| `isLoggedIn()` | 检查登录状态 |
| `logout()` | 用户登出 |

#### 3.5.3 AfterlifePlanViewModel 身后事计划视图模型

**接口说明**：管理身后事计划UI状态，协调BmobRepository调用。

| LiveData属性 | 类型 | 说明 |
|-------------|------|------|
| `isLoading` | `LiveData<Boolean>` | 加载状态 |
| `errorMessage` | `LiveData<String?>` | 错误信息 |
| `successMessage` | `LiveData<String?>` | 成功提示 |
| `afterlifePlans` | `LiveData<List<AfterlifePlan>>` | 计划列表 |
| `currentPlan` | `LiveData<AfterlifePlan?>` | 当前编辑的计划 |
| `visitInfos` | `LiveData<List<AfterlifePlanVisitInfo>>` | 祭拜信息列表 |
| `currentVisitInfo` | `LiveData<AfterlifePlanVisitInfo?>` | 当前祭拜信息 |

| 方法 | 说明 |
|------|------|
| `createAfterlifePlan(plan)` | 创建身后事计划 |
| `getAfterlifePlansByUserId(userId)` | 获取用户计划 |
| `getAllAfterlifePlans()` | 获取所有计划 |
| `updateAfterlifePlan(objectId, plan)` | 更新计划 |
| `deleteAfterlifePlan(objectId)` | 删除计划 |
| `createVisitInfo(info)` | 创建祭拜信息 |
| `getVisitInfosByPlanId(planId)` | 获取计划关联的祭拜信息 |

---

## 四、数据格式汇总

### 4.1 Bmob表结构汇总

| Bmob表名 | 说明 | 关联表 |
|---------|------|-------|
| `_User` | 用户表 | - |
| `Post` | 帖子表 | `_User`（userId） |
| `Comment` | 评论表 | `Post`（postId） |
| `MemorialStar` | 纪念星表 | `_User`（createdBy） |
| `AfterlifePlan` | 身后事计划表 | `_User`（userId） |
| `AfterlifePlan_V` | 祭拜信息表 | `AfterlifePlan`（planId） |
| `CheckinRecord` | 打卡记录表 | `_User`（userId） |
| `PostInteraction` | 帖子互动表 | `Post`（postId）、`_User`（userId） |

### 4.2 数据同步状态字段

所有支持同步的数据模型都包含`syncStatus`字段：

| syncStatus值 | 说明 |
|-------------|------|
| 0 | 未同步 |
| 1 | 已同步 |
| 2 | 同步失败 |

### 4.3 通用API响应格式

```kotlin
data class ApiResponse<T>(
    val code: Int,           // 状态码，0表示成功
    val message: String,     // 响应消息
    val data: T? = null      // 响应数据
)
```

---

## 五、接口调用流程示例

### 5.1 用户登录流程

```
1. UI层（LoginActivity）
   ↓ login(phone, password)
2. ViewModel层（UserViewModel）
   ↓ repository.login(phone, password)
3. Repository层（BmobRepository）
   ↓ BmobApiService.login(username, password)
4. Network层（BmobApiService）
   → HTTP GET /login?username=xxx&password=xxx
   → Bmob Server
5. 响应数据逐层向上传递
6. 最终通过 LiveData 通知 UI 更新
```

### 5.2 AI对话流程

```
1. UI层（ChatActivity）
   ↓ chat(userMessage)
2. Repository层（ElderlyDataRepository）
   ↓ getElderlyStatus()
   → 获取用户状态（SharedPreferences + CheckinRecord）
3. Network层（PromptBuilder）
   ↓ buildSystemPrompt(status) + buildKimiRequestJson()
4. Network层（KimiApiService）
   ↓ chat(userMessage)
   → HTTP POST https://api.moonshot.cn/v1/chat/completions
   → Kimi Server
5. 响应数据通过回调返回UI层
```

---

## 六、错误处理机制

### 6.1 网络请求错误

所有Repository方法返回`Result<T>`类型，通过`onSuccess`和`onFailure`处理：

```kotlin
repository.createPost(post)
    .onSuccess { savedPost ->
        // 处理成功
    }
    .onFailure { error ->
        // 处理错误，error.message包含错误信息
    }
```

### 6.2 错误码说明

| 错误场景 | 处理方式 |
|---------|---------|
| 网络连接失败 | 提示"网络异常，请检查网络连接" |
| API返回错误 | 提示具体错误信息 |
| 数据解析失败 | 提示"数据解析异常" |
| 用户未登录 | 跳转登录页面 |
| 权限不足 | 提示"权限不足" |
