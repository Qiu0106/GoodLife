# 3.1.2 功能模块设计

## 一、系统架构总览

本系统采用 **MVVM（Model-View-ViewModel）** 架构模式，基于 **面向对象（OOP）** 的 Android 原生开发技术栈。整体分为 **四层架构**：

```
┌─────────────────────────────────────────────────────────────┐
│                     UI 层（View）                           │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐      │
│  │ Activity │ │ Fragment │ │ Adapter  │ │ Custom   │      │
│  │ /页面    │ │ /碎片    │ │ /适配器  │ │ View     │      │
│  └─────┬────┘ └─────┬────┘ └─────┬────┘ └─────┬────┘      │
│        │            │           │           │              │
├────────┴────────────┴───────────┴───────────┴──────────────┤
│                  ViewModel 层                               │
│     ┌─────────────┐  ┌──────────────┐  ┌──────────────┐   │
│     │PostViewModel│  │UserViewModel │  │AfterlifePlan  │   │
│     │  (帖子管理)  │  │  (用户管理)   │  │ ViewModel     │   │
│     └──────┬──────┘  └──────┬───────┘  └──────┬───────┘   │
├────────────┼────────────────┼─────────────────┼─────────────┤
│          Repository 层（数据仓库）                             │
│     ┌──────────────────────────────────────────────┐       │
│     │              BmobRepository                   │       │
│     │  (统一数据访问接口，封装所有 CRUD 操作)         │       │
│     └──────────────────┬───────────────────────────┘       │
├────────────────────────┼────────────────────────────────────┤
│                Model 层（数据模型）                            │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐      │
│  │ PostBmob │ │ Memorial  │ │ Comment  │ │ UserBmob │      │
│  │ (帖子)   │ │ StarBmob │ │ Bmob    │ │ (用户)   │      │
│  │          │ │ (纪念星)  │ │ (评论)   │ │          │      │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘      │
├─────────────────────────────────────────────────────────────┤
│               Network 层（网络服务）                          │
│  ┌────────────────┐  ┌────────────────┐                    │
│  │ BmobApiService │  │ KimiApiService │                    │
│  │  (REST API)    │  │  (AI 对话)     │                    │
│  └────────────────┘  └────────────────┘                    │
└─────────────────────────────────────────────────────────────┘
```

## 二、包结构图（Package Diagram）

```
com.example.smartelderlycare_app/
│
├── component/                          # UI 组件层（View）
│   ├── MainActivity.kt                 # 主入口/底部导航
│   ├── LoginActivity.kt                # 登录页
│   ├── EditProfileActivity.kt          # 编辑资料页
│   ├── MineActivity.kt                # 个人中心
│   │
│   ├── CommunityActivity.kt            # 社区首页（TabLayout 分区）
│   ├── CreatePostActivity.kt           # 发帖页（标题+分区+图片）
│   ├── PostDetailActivity.kt           # 帖子详情（评论+互动栏）
│   ├── PostAdapter.kt                  # 帖子列表适配器
│   ├── CommentAdapter.kt               # 评论列表适配器
│   │
│   ├── StarrySkyActivity.kt            # 星空纪念馆主页
│   ├── StarrySkyView.kt                # ★ 自定义星空视图（核心算法）
│   ├── AddMemorialActivity.kt          # 添加逝者（含图片上传+审核）
│   ├── MemorialInfoBottomSheet.kt      # 纪念星信息弹窗
│   │
│   ├── ChatActivity.kt                 # AI 聊天页
│   ├── ChatAdapter.kt                  # 聊天消息适配器
│   │
│   ├── CheckinActivity.kt              # 打卡签到
│   ├── AfterlifeCustomActivity.kt      # 后世定制
│   ├── afterlife/                      # 后世定制子模块
│   │   ├── BasicInfoFragment.kt        # 基本信息
│   │   ├── FuneralStyleFragment.kt     # 葬礼风格
│   │   ├── FuneralSuppliesFragment.kt  # 葬礼用品
│   │   ├── RelicsBurialFragment.kt     # 遗物安葬
│   │   ├── VisitGraveFragment.kt       # 祭扫安排
│   │   └── BGMFragment.kt             # 背景音乐
│   │
│   ├── ImagePreviewActivity.kt         # 图片预览
│   ├── MyPostsActivity.kt             # 我的帖子
│   ├── MyFavoritesActivity.kt         # 我的收藏
│   ├── StarFieldActivity.kt           # 星场动画
│   ├── StarFieldView.kt               # 星场视图
│   └── TempLLM.kt                     # LLM 测试入口
│
├── data/
│   ├── model/                          # 数据模型层
│   │   ├── Post.kt / PostBmob.kt      # 帖子实体（含 category/commentCount）
│   │   ├── MemorialData.kt            # 纪念星前端展示模型
│   │   ├── MemorialStarBmob.kt        # 纪念星 Bmob 模型（含 status 审核字段）
│   │   ├── CommentBmob.kt / Comment.kt # 评论实体
│   │   ├── PostInteraction.kt         # 互动记录（点赞/收藏）
│   │   ├── User.kt / UserBmob.kt      # 用户实体
│   │   ├── AfterlifePlan.kt           # 后世计划
│   │   └── CheckinRecord.kt          # 打卡记录
│   │
│   ├── repository/                     # 数据仓库层
│   │   ├── BmobRepository.kt          # ★ 核心 CRUD 仓库
│   │   ├── ElderlyDataRepository.kt   # 老年人数据仓库
│   │   └── BmobTestHelper.kt          # 测试辅助
│   │
│   ├── network/                        # 网络服务层
│   │   ├── BmobApiService.kt          # Bmob REST API 封装
│   │   ├── KimiApiService.kt          # Moonshot(Kimi) AI 接口
│   │   ├── LeanCloudApiService.kt     # LeanCloud API
│   │   ├── OkHttpSingleton.kt         # OkHttp 单例
│   │   └── PromptBuilder.kt           # ★ AI 提示词构建器
│   │
│   └── sync/                           # 数据同步
│       └── DataSyncManager.kt         # 同步管理器
│
└── ui/
    └── viewmodel/                      # ViewModel 层
        ├── PostViewModel.kt            # 帖子状态管理（含 getPostsByCategory）
        ├── UserViewModel.kt            # 用户状态管理
        └── AfterlifePlanViewModel.kt   # 后世计划状态管理
```

## 三、模块调用关系图

```
                        ┌──────────────────┐
                        │   MainActivity   │
                        │  (底部导航容器)   │
                        └────────┬─────────┘
                                 │
        ┌────────────────────────┼────────────────────────┐
        │                        │                        │
        ▼                        ▼                        ▼
┌───────────────┐      ┌────────────────┐      ┌────────────────┐
│CommunityActivity│      │StarrySkyActivity│      │  MineActivity  │
│   (社区模块)    │      │  (星空纪念馆)    │      │  (个人中心)     │
└───────┬───────┘      └───────┬────────┘      └───────┬────────┘
        │                       │                       │
        ▼                       ▼                       ▼
┌───────────────┐      ┌────────────────┐      ┌────────────────┐
│PostViewModel  │◄────►│ BmobRepository │◄────►│UserViewModel   │
└───────┬───────┘      └───────┬────────┘      └────────────────┘
        │                       │
        ▼                       ▼
┌───────────────┐      ┌────────────────┐
│CreatePostAct  │      │BmobApiService  │
│PostDetailAct  │      │KimiApiService  │
│CommentAdapter │      └────────────────┘
└───────────────┘

═══════════════════════════════════════════════════════════
                    核心数据流向
═══════════════════════════════════════════════════════════

[用户操作] → [Activity] → [ViewModel] → [Repository] → [ApiService] → [Bmob/Kimi Server]
                                                                    │
[UI 更新] ← [LiveData.observe] ← [ViewModel._posts.setValue()] ← [JSON 解析]
```

## 四、各模块职责说明

| 层次 | 包名 | 职责 | 核心类 |
|------|------|------|--------|
| **UI 层** | `component` | 页面展示、用户交互、事件分发 | 各 Activity、Adapter、Custom View |
| **ViewModel 层** | `ui.viewmodel` | 管理 UI 状态、协调 Repository 调用 | PostViewModel、UserViewModel |
| **Repository 层** | `data.repository` | 封装所有数据访问逻辑，统一错误处理 | BmobRepository |
| **Model 层** | `data.model` | 数据实体定义，Bmob ↔ 本地映射 | PostBmob、MemorialStarBmob、CommentBmob |
| **Network 层** | `data.network` | HTTP 请求封装、API Key 管理 | BmobApiService、KimiApiService |

---

# 3.1.3 关键功能/算法设计

## 算法一：星空纪念馆 — 主星星防重叠算法

**文件位置**: [StarrySkyView.kt](file:///Users/paokie/Downloads/GoodLife_Clean/app/src/main/java/com/example/smartelderlycare_app/component/StarrySkyView.kt#L211-L280)

### 流程图

```
┌─────────────────────────────┐
│  输入: baseRadius, padding,  │
│        name, attempts=100    │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│  1. 计算新星星 touchRadius   │
│  2. 计算名字文字区域占位      │
│  3. 计算屏幕安全边界         │
└──────────────┬──────────────┘
               │
               ▼
        ┌──────────────┐
        │ attempt < 100?│
        └──────┬───────┘
           Yes │    No
               │     │
               ▼     ▼
    ┌────────────┐  ┌──────────────┐
    │ 随机生成坐标 │  │ 九宫格兜底   │
    │ (minX~maxX) │  │ 均匀分配位置  │
    │ (minY~maxY) │  └──────┬───────┘
    └──────┬─────┘         │
           │               │
           ▼               │
    ┌────────────┐         │
    │ 遍历已有星星 │         │
    │ 检测距离    │         │
    └──────┬─────┘         │
           │               │
     ┌─────┴─────┐         │
     │有重叠?    │         │
     └─────┬─────┘         │
      Yes   │ No           │
       │    ▼              │
       │ 返回坐标 ◄────────┘
       │
       ▼
   attempt++
   继续循环
```

### 核心代码逻辑

```kotlin
// 安全距离系数: 两星星 touchRadius 之和 × 1.8
val MIN_DISTANCE_MULTIPLIER = 1.8f
// 额外文字保护间距: 120dp
val EXTRA_PADDING_FOR_TEXT = 120f * density

while (attemptCount < attempts) {
    val candidateX = Random.nextFloat() * (maxX - minX) + minX
    val candidateY = Random.nextFloat() * (maxY - minY) + minY
    
    var hasOverlap = false
    for (existingStar in stars) {
        val dx = existingStar.x - candidateX
        val dy = existingStar.y - candidateY
        val distance = sqrt(dx*dx + dy*dy)
        val minAllowedDistance = (
            existingStar.touchRadiusPx + newStarTouchRadius
        ) * MIN_DISTANCE_MULTIPLIER + EXTRA_PADDING_FOR_TEXT
        
        if (distance < minAllowedDistance) { hasOverlap = true; break }
    }
    
    if (!hasOverlap) return Pair(candidateX, candidateY)
    attemptCount++
}
// 兜底: 九宫格均匀分配
```

### 优化技巧
1. **距离系数 1.8x**: 保证星星视觉间距充足，适合老年人点击
2. **文字区域保护**: 额外增加 120dp 保护间距，避免名字与相邻星星重叠
3. **九宫格兜底**: 100 次随机尝试失败后，使用网格化均匀分配，保证一定有解
4. **touchRadius × 1.5**: 触摸热区大于视觉半径，提升点击成功率

---

## 算法二：背景星星 Lerp 物理引擎（手指吸引 + 平滑归位）

**文件位置**: [StarrySkyView.kt](file:///Users/paokie/Downloads/GoodLife_Clean/app/src/main/java/com/example/smartelderlycare_app/component/StarrySkyView.kt#L368-L404)

### 流程图

```
┌───────────────────────────────────────┐
│         每帧调用 updateAndDraw        │
│         (由 ValueAnimator 驱动 60fps)  │
└──────────────────┬────────────────────┘
                   │
                   ▼
          ┌────────────────┐
          │ isFingerDown?  │
          └───────┬────────┘
              Yes │     No
                  │      │
                  ▼      ▼
        ┌────────────┐ ┌────────────────┐
        │计算到手指   │ │Lerp 归位到原始  │
        │的距离       │ │位置 originalHome│
        └─────┬──────┘ └───────┬────────┘
              │                │
              ▼                │
    ┌─────────────────┐       │
    │ distance < 400px?│       │
    └────┬────────────┘       │
      Yes │ No                │
          │ │                 │
          ▼ ▼                 │
  ┌──────────────┐           │
  │home 向手指    │           │
  │偏移 15% 位置  │           │
  │(lerp factor   │           │
  │ = 0.08)       │           │
  └──────┬───────┘           │
         │                    │
         └────────┬───────────┘
                  │
                  ▼
    ┌─────────────────────────┐
    │ bgStar.x = lerp(x, homeX, 0.02) │
    │ bgStar.y = lerp(y, homeY, 0.02) │
    └────────────┬────────────┘
                 │
                 ▼
    ┌─────────────────────────┐
    │ 计算闪烁 alpha (sin函数)  │
    │ 绘制圆形到 Canvas         │
    └─────────────────────────┘
```

### 核心公式

```kotlin
// Lerp 线性插值公式
private fun lerp(start: Float, end: Float, fraction: Float): Float {
    return start + (end - start) * fraction
}

// 手指吸引时: home 向手指方向偏移 15%（lerpFactor = 0.08，较慢）
bgStar.homeX = lerp(bgStar.homeX, targetX, 0.08f)
bgStar.homeY = lerp(bgStar.homeY, targetY, 0.08f)

// 归位时: 缓慢回归原位（lerpFactor = 0.02，极慢，绝不回弹）
bgStar.homeX = lerp(bgStar.homeX, bgStar.originalHomeX, 0.02f)

// 位置跟随: 当前位置向目标位置插值
bgStar.x = lerp(bgStar.x, bgStar.homeX, 0.02f)
bgStar.y = lerp(bgStar.y, bgStar.homeY, 0.02f)
```

### 优化技巧
1. **双层 Lerp**: 先更新 `home`（目标点），再更新实际 `x/y`（当前位置），实现"渐进式"运动
2. **不同速率**: 吸引速率(0.08) > 归位速率(0.02)，产生"被吸走后缓慢飘回"的丝滑感
3. **无弹簧振荡**: 纯 Lerp 不会过冲/回弹，避免物理不稳定
4. **闪烁效果**: 使用 `sin()` 函数 + 随机偏移量 + 随机速度，每颗星星闪烁不同步

---

## 算法三：星星发光渲染（四层 RadialGradient 叠加）

**文件位置**: [StarrySkyView.kt](file:///Users/paokie/Downloads/GoodLife_Clean/app/src/main/java/com/example/smartelderlycare_app/component/StarrySkyView.kt#L410-L512)

### 渲染层级

```
┌─────────────────────────────────┐
│  第1层: 外层大光晕 (alpha 15%)    │  ← RadialGradient, 半径 × 1.5
│    颜色渐变: 色→色×0.5→色×0.3→透明 │
├─────────────────────────────────┤
│  第2层: 中层光晕   (alpha 35%)    │  ← RadialGradient, 标准半径
│    颜色渐变: 色→色×0.5→色×0.15→透明│
├─────────────────────────────────┤
│  第3层: 星星实体核心             │  ← RadialGradient, 白→粉→色×0.6→透明
├─────────────────────────────────┤
│  第4层: 名字胶囊底托 + 文字       │  ← RoundRect + drawText
│    底托: #99000000 半透明黑色    │
│    文字: 白色 42sp + 阴影        │
├─────────────────────────────────┤
│  第5层(可选): 选中高亮金圈       │  ← STROKE 圆环 #FFD700
└─────────────────────────────────┘
```

### 呼吸动画公式

```kotlin
val breathProgress = breathAnimator?.animatedValue as? Float ?: 0f
// 正弦波驱动光晕脉动: 0.8 ~ 1.2 倍振幅
val glowPulse = 0.8f + 0.4f * sin(breathProgress * 2π).toFloat()
val currentGlowRadius = star.glowRadiusPx * glowPulse
```

---

## 算法四：AI 智能助手对话流程

**文件位置**: [KimiApiService.kt](file:///Users/paokie/Downloads/GoodLife_Clean/app/src/main/java/com/example/smartelderlycare_app/data/network/KimiApiService.kt), [PromptBuilder.kt](file:///Users/paokie/Downloads/GoodLife_Clean/app/src/main/java/com/example/smartelderlycare_app/data/network/PromptBuilder.kt), [ChatActivity.kt](file:///Users/paokie/Downloads/GoodLife_Clean/app/src/main/java/com/example/smartelderlycare_app/component/ChatActivity.kt)

### 流程图

```
┌──────────┐    ┌──────────────┐    ┌─────────────────┐
│ 用户输入  │───►│ PromptBuilder │───►│ KimiApiService  │
│ 文本消息  │    │ 构建 System   │    │ POST /v1/chat/  │
│          │    │ Prompt 人设   │    │ completions     │
└──────────┘    └──────────────┘    └────────┬────────┘
                                               │
                                               ▼
                                    ┌─────────────────────┐
                                    │ Moonshot API Server  │
                                    │ moonshot-v1-8k 模型  │
                                    └────────┬────────────┘
                                             │
                                             ▼
                                    ┌─────────────────────┐
                                    │ 解析 JSON Response  │
                                    │ 提取 choices[0].msg │
                                    └────────┬────────────┘
                                             │
                                             ▼
                                    ┌─────────────────────┐
                                    │ 打字机效果逐字显示    │
                                    │ (ChatAdapter 通知刷新)│
                                    └─────────────────────┘
```

### PromptBuilder 设计要点

- 角色: "好好活"智能养老助手
- 特征: 温暖耐心、适老化语言、不使用专业术语
- 上下文注入: 用户昵称、今日步数、用药状态、心情指数

---

## 算法五：纪念星审核流程（Bmob status 字段机）

**文件位置**: [AddMemorialActivity.kt](file:///Users/paokie/Downloads/GoodLife_Clean/app/src/main/java/com/example/smartelderlycare_app/component/AddMemorialActivity.kt), [BmobRepository.kt](file:///Users/paokie/Downloads/GoodLife_Clean/app/src/main/java/com/example/smartelderlycare_app/data/repository/BmobRepository.kt#L680-L700)

### 状态流转图

```
                    ┌─────────────┐
     用户提交       │             │
   ───────────────► │   PENDING   │ (待审核)
                    │  (默认状态)  │
                    └──────┬──────┘
                           │
              管理员在 Bmob 后台操作
                           │
              ┌────────────┼────────────┐
              │            │            │
              ▼            ▼            ▼
     ┌─────────────┐ ┌──────────┐ ┌──────────┐
     │  APPROVED   │ │ REJECTED │ │  DELETE  │
     │  (已通过)    │ │ (已拒绝)  │ │ (已删除)  │
     └──────┬──────┘ └──────────┘ └──────────┘
            │
            ▼ 仅 approved 状态的记录
     ┌─────────────────────┐
     │ getApprovedMemorial  │
     │ Stars() 查询条件:    │
     │ where={"status":     │
     │  "approved"}          │
     └──────────┬──────────┘
                │
                ▼
     ┌─────────────────────┐
     │ StarrySkyView 显示   │
     │ 在星空画布上渲染       │
     └─────────────────────┘
```

### 数据存储过程

```
AddMemorialActivity
    │
    ├─ 1. 选择图片 → uriToFile() → File
    │
    ├─ 2. bmobRepository.uploadImage(file) → avatarUrl
    │
    ├─ 3. 构建 MemorialStarBmob:
    │      name, lifeYears, message, story,
    │      avatarUrl, flowerCount=0,
    │      createdBy=userObjectId,
    │      status="pending"  ← 默认待审核
    │
    └─ 4. bmobRepository.createMemorialStar(star)
           │
           ▼
      POST https://api.bmob.cn/1/classes/MemorialStar
           │
           ▼
      Bmob 存储 → 管理员在控制台修改 status → 审核通过
```

---

## 算法六：社区帖子分区筛选机制

**文件位置**: [CommunityActivity.kt](file:///Users/paokie/Downloads/GoodLife_Clean/app/src/main/java/com/example/smartelderlycare_app/component/CommunityActivity.kt), [PostViewModel.kt](file:///Users/paokie/Downloads/GoodLife_Clean/app/src/main/java/com/example/smartelderlycare_app/ui/viewmodel/PostViewModel.kt), [BmobRepository.kt](file:///Users/paokie/Downloads/GoodLife_Clean/app/src/main/java/com/example/smartelderlycare_app/data/repository/BmobRepository.kt#L152-L170)

### Tab 切换数据流

```
┌──────────────────────────────────────────────────┐
│              TabLayout (适老化 64dp 高)            │
│  [推荐]  [健康养生]  [日常闲聊]  [社区活动]        │
└───────────────────────┬──────────────────────────┘
                        │ onTabSelected
                        ▼
              ┌─────────────────────┐
              │ CommunityActivity    │
              │ loadPostsByCategory()│
              └──────────┬──────────┘
                         │
              ┌──────────┴──────────┐
              │ position == 0?      │
              │ (推荐 Tab)           │
              └──────┬────────┬─────┘
               Yes    │        │ No
                      │        ▼
                      │  currentCategory =
                      │  categories[position]
                      │  ("健康养生"/"日常闲聊"/"社区活动")
                      ▼
              ┌─────────────────────┐
              │ PostViewModel        │
              │ getAllPosts() /      │
              │ getPostsByCategory() │
              └──────────┬──────────┘
                         │
                         ▼
              ┌─────────────────────────────┐
              │ BmobRepository              │
              │                            │
              │ 推荐: GET /1/classes/Post   │
              │      order=-createdAt       │
              │                            │
              │ 分类: GET /1/classes/Post   │
              │      where={"category":"xxx"}│
              │      order=-createdAt       │
              └──────────────┬──────────────┘
                             │
                             ▼
              _posts.setValue(list) ──► LiveData.observe ──► RecyclerView 刷新
```

---

## 算法七：帖子互动系统（点赞/收藏/评论计数器）

**文件位置**: [PostDetailActivity.kt](file:///Users/paokie/Downloads/GoodLife_Clean/app/src/main/java/com/example/smartelderlycare_app/component/PostDetailActivity.kt#L256-L330), [BmobRepository.kt](file:///Users/paookie/Downloads/GoodLife_Clean/app/src/main/java/com/example/smartelderlycare_app/data/repository/BmobRepository.kt)

### 点赞/收藏原子操作流程

```
┌────────────────┐
│ 用户点击点赞   │
└───────┬────────┘
        │
        ▼
┌──────────────────────┐
│ 已登录? userId非空?  │──── 否 ──► Toast("请先登录")
└───────┬──────────────┘
        │ 是
        ▼
┌──────────────────────┐
│ 当前 isLiked?        │
└───────┬──────┬───────┘
    True │      │ False
         │      │
         ▼      ▼
  ┌──────────┐ ┌──────────────────────────┐
  │ 取消点赞:  │ │ 新增点赞:                  │
  │ 1. remove │ │ 1. PostInteraction(       │
  │ Interaction│ │    userId, postId, "like")│
  │ 2. decrement│ │ 2. addInteraction()      │
  │ PostLikes  │ │ 3. incrementPostLikes()  │
  │ 3. count-- │ │ 4. count++               │
  └──────┬───┘ └────────────┬─────────────┘
         │                │
         └───────┬────────┘
                 │
                 ▼
    ┌────────────────────────┐
    │ UI 更新:                │
    │ - 切换图标 (实心/空心)    │
    │ - 更新数字               │
    │ - 缩放动画 (1.0→1.3→1.0) │
    └────────────────────────┘
```

### Bmob 原子计数器（Increment 操作）

```kotlin
// 点赞数 +1 (使用 Bmob 原子操作，无并发问题)
POST /1/classes/Post/{objectId}
Body: {"likeCount": {"__op": "Increment", "amount": 1}}

// 评论数 +1
POST /1/classes/Post/{objectId}
Body: {"commentCount": {"__op": "Increment", "amount": 1}}
```

---

## 算法八：评论系统数据流

**文件位置**: [CommentBmob.kt](file:///Users/paokie/Downloads/GoodLife_Clean/app/src/main/java/com/example/smartelderlycare_app/data/model/CommentBmob.kt), [CommentAdapter.kt](file:///Users/paokie/Downloads/GoodLife_Clean/app/src/main/java/com/example/smartelderlycare_app/component/CommentAdapter.kt)

### Comment 表结构与查询

```
Comment 表 (Bmob):
┌─────────────┬──────────┬─────────────┐
│ postId      │authorName│ authorAvatar│
│ (关联帖子ID) │ (评论者昵称)│ (头像URL)  │
├─────────────┼──────────┼─────────────┤
│ content     │createdAt │ updatedAt   │
│ (评论内容)   │          │             │
└─────────────┴──────────┴─────────────┘

查询: GET /1/classes/Comment?where={"postId":"xxx"}&order=createdAt
      ↓
CommentBmob.fromMap() → Comment → CommentAdapter → RecyclerView
```

---

## 总结：关键技术指标

| 技术 | 参数/方案 |
|------|----------|
| 防重叠算法 | 距离系数 1.8x, 最大尝试 100 次, 九宫格兜底 |
| Lerp 物理引擎 | 归位因子 0.02, 吸引因子 0.08, 无弹簧振荡 |
| 星星光晕渲染 | 四层 RadialGradient 叠加, sin 呼吸动画 |
| AI 对话 | Moonshot API (moonshot-v1-8k), OkHttp 异步 |
| 数据库 | Bmob REST API, Increment 原子操作 |
| 审核机制 | status 字段 (pending→approved), 后台手动切换 |
| 适老化设计 | 字号 18-26sp, 按钮 56dp+, 触摸热区 1.5x |
| 架构模式 | MVVM + LiveData + Repository + Kotlin Coroutines |