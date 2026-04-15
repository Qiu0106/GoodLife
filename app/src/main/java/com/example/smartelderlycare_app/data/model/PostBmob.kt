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

    var userId: String? = null
    var title: String? = null
    var content: String? = null
    var category: String? = null
    var coverImageUrl: String? = null
    var imageUrls: String? = null
    var userName: String? = null
    var userAvatarUrl: String? = null
    var likeCount: Int = 0
    var favoriteCount: Int = 0
    var commentCount: Int = 0

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "title" to title,
            "content" to content,
            "category" to category,
            "coverImageUrl" to coverImageUrl,
            "imageUrls" to imageUrls,
            "userName" to userName,
            "userAvatarUrl" to userAvatarUrl,
            "likeCount" to likeCount,
            "favoriteCount" to favoriteCount,
            "commentCount" to commentCount
        )
    }

    fun toPost(): Post {
        return Post(
            id = objectId?.hashCode()?.toLong(),
            objectId = objectId,
            userId = userId ?: "",
            title = title ?: "",
            content = content ?: "",
            category = category ?: "",
            coverImageUrl = coverImageUrl,
            imageUrls = imageUrls,
            userName = userName ?: "",
            userAvatarUrl = userAvatarUrl,
            likeCount = likeCount,
            favoriteCount = favoriteCount,
            commentCount = commentCount,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromPost(post: Post): PostBmob {
            return PostBmob().apply {
                objectId = post.objectId
                userId = post.userId
                title = post.title
                content = post.content
                category = post.category
                coverImageUrl = post.coverImageUrl
                imageUrls = post.imageUrls
                userName = post.userName
                userAvatarUrl = post.userAvatarUrl
                likeCount = post.likeCount
                favoriteCount = post.favoriteCount
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
                category = map["category"] as? String
                coverImageUrl = map["coverImageUrl"] as? String
                imageUrls = map["imageUrls"] as? String
                userName = map["userName"] as? String
                userAvatarUrl = map["userAvatarUrl"] as? String
                likeCount = (map["likeCount"] as? Number)?.toInt() ?: 0
                favoriteCount = (map["favoriteCount"] as? Number)?.toInt() ?: 0
                commentCount = (map["commentCount"] as? Number)?.toInt() ?: 0
            }
        }
    }
}