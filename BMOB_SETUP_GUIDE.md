# Bmob 后台配置指南

## 1. 注册和创建应用

1. 访问 [Bmob 官网](https://www.bmob.cn/)
2. 注册账号并登录
3. 创建新应用：
   - 点击"创建应用"
   - 输入应用名称（如：GoodLife）
   - 选择应用类型（Android）
   - 点击"创建"

## 2. 获取应用密钥

创建应用后，进入应用管理页面，获取以下信息：

- **Application ID**: 应用标识符
- **REST API Key**: REST API密钥
- **Secret Key**: 应用密钥（用于敏感操作）

## 3. 配置应用

打开 `MyApplication.kt` 文件，替换以下常量：

```kotlin
companion object {
    // 替换为你的Bmob应用信息
    const val BMOB_APPLICATION_ID = "你的Application ID"
    const val BMOB_REST_API_KEY = "你的REST API Key"
}
```

## 4. 创建数据表

在Bmob控制台中创建以下数据表：

### AfterlifePlanBmob 表（身后事计划）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| userId | String | 用户ID |
| name | String | 姓名 |
| age | String | 年龄 |
| contact | String | 联系方式 |
| biography | String | 生平简介 |
| funeralStyle | String | 葬礼风格 |
| funeralLocation | String | 葬礼地点 |
| funeralDate | String | 葬礼日期 |
| funeralTime | String | 葬礼时间 |
| relicsHandling | String | 遗物处理方式 |
| burialMethod | String | 安葬方式 |
| burialLocation | String | 安葬地点 |
| coffin | Boolean | 是否需要棺材 |
| urn | Boolean | 是否需要骨灰盒 |
| flowers | Boolean | 是否需要鲜花 |
| candles | Boolean | 是否需要蜡烛 |
| photos | Boolean | 是否需要遗像 |
| otherSupplies | String | 其他用品 |
| bgm | String | 背景音乐 |
| bgmNotes | String | 音乐备注 |
| visitDate | String | 祭拜日期 |
| visitTime | String | 祭拜时间 |
| visitNotes | String | 祭拜备注 |

### PostBmob 表（社区帖子）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| userId | String | 用户ID |
| title | String | 帖子标题 |
| content | String | 帖子内容 |
| coverImageUrl | String | 封面图片URL |
| userName | String | 用户名 |
| userAvatarUrl | String | 用户头像URL |
| likeCount | Number | 点赞数 |
| commentCount | Number | 评论数 |

## 5. 设置权限

在Bmob控制台的"设置"->"应用选项"中：

1. **开启数据存储**：确保数据存储功能已开启
2. **设置访问权限**：
   - 根据需求设置表的读写权限
   - 建议：用户只能读写自己的数据

## 6. 测试数据上传

完成配置后，运行应用并测试以下功能：

1. **身后事计划保存**：
   - 进入"身后事定制"页面
   - 填写信息并保存
   - 检查Bmob控制台是否有新数据

2. **发帖功能**：
   - 进入"社区"页面
   - 点击发布按钮
   - 输入内容并发布
   - 检查Bmob控制台是否有新帖子

## 7. 常见问题

### Q: 数据上传失败怎么办？

A: 检查以下几点：
- 确认Application ID和REST API Key是否正确
- 检查网络连接
- 查看Logcat中的错误信息
- 确认Bmob控制台中已创建对应的数据表

### Q: 如何查看上传的数据？

A: 登录Bmob控制台，进入对应应用，点击"数据"菜单即可查看所有数据。

### Q: 支持图片上传吗？

A: 支持。Bmob提供文件存储服务，可以上传图片并获取URL。当前代码中图片上传功能需要额外实现。

## 8. 数据安全建议

1. **定期备份**：定期从Bmob控制台导出数据
2. **设置ACL**：为数据设置访问控制列表，确保数据安全
3. **使用HTTPS**：确保所有网络请求都使用HTTPS
4. **敏感信息加密**：用户密码等敏感信息需要加密存储

## 9. 免费额度说明

Bmob免费版包含：
- 数据存储：一定容量的免费空间
- API请求：每日有限的请求次数
- 文件存储：有限的存储空间

如需更多资源，请考虑升级付费套餐。

## 10. 技术支持

- Bmob官方文档：https://doc.bmob.cn/
- 技术支持QQ群：在官网查看
- 问题反馈：通过Bmob控制台提交工单
