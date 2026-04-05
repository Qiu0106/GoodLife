package com.example.smartelderlycare_app.data.model

/**
 * 帖子数据模型 - REST API版本
 * 用于Bmob REST API交互
 * 对应Bmob表名: Post
 */
class PostBmob {

    var objectId: String? = null
    var createdAt: String? = null
    var updatedAt: String? = null

    // 用户ID
    var userId: String? = null

    // 帖子标题
    var title: String? = null

    // 帖子内容
    var content: String? = null

    // 封面图片URL
    var coverImageUrl: String? = null

    // 用户名
    var userName: String? = null

    // 用户头像URL
    var userAvatarUrl: String? = null

    // 点赞数
    var likeCount: Int = 0

    // 评论数
    var commentCount: Int = 0

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "title" to title,
            "content" to content,
            "coverImageUrl" to coverImageUrl,
            "userName" to userName,
            "userAvatarUrl" to userAvatarUrl,
            "likeCount" to likeCount,
            "commentCount" to commentCount
        )
    }

    fun toPost(): Post {
        return Post(
            id = objectId?.hashCode()?.toLong(),
            userId = userId ?: "",
            title = title ?: "",
            content = content ?: "",
            coverImageUrl = coverImageUrl,
            userName = userName ?: "",
            userAvatarUrl = userAvatarUrl,
            likeCount = likeCount,
            commentCount = commentCount,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromPost(post: Post): PostBmob {
            return PostBmob().apply {
                objectId = post.id?.toString()
                userId = post.userId
                title = post.title
                content = post.content
                coverImageUrl = post.coverImageUrl
                userName = post.userName
                userAvatarUrl = post.userAvatarUrl
                likeCount = post.likeCount
                commentCount = post.commentCount
            }
        }

        fun fromMap(map: Map<String, Any>): PostBmob {
            return PostBmob().apply {
                objectId = map["objectId"] as? String
                createdAt = map["createdAt"] as? String
                updatedAt = map["updatedAt"] as? String
                userId = map["userId"] as? String
                title = map["title"] as? String
                content = map["content"] as? String
                coverImageUrl = map["coverImageUrl"] as? String
                userName = map["userName"] as? String
                userAvatarUrl = map["userAvatarUrl"] as? String
                likeCount = (map["likeCount"] as? Number)?.toInt() ?: 0
                commentCount = (map["commentCount"] as? Number)?.toInt() ?: 0
            }
        }
    }
}
