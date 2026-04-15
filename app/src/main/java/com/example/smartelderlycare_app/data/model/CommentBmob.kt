package com.example.smartelderlycare_app.data.model

/**
 * 评论数据模型 - REST API版本
 * 用于Bmob REST API交互
 * 对应Bmob表名: Comment
 */
class CommentBmob {

    var objectId: String? = null
    var createdAt: String? = null
    var updatedAt: String? = null

    var postId: String? = null
    var authorName: String? = null
    var authorAvatar: String? = null
    var content: String? = null

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "postId" to postId,
            "authorName" to authorName,
            "authorAvatar" to authorAvatar,
            "content" to content
        )
    }

    fun toComment(): Comment {
        return Comment(
            id = objectId?.hashCode()?.toLong(),
            objectId = objectId,
            postId = postId ?: "",
            authorName = authorName ?: "",
            authorAvatar = authorAvatar,
            content = content ?: "",
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromComment(comment: Comment): CommentBmob {
            return CommentBmob().apply {
                objectId = comment.objectId
                postId = comment.postId
                authorName = comment.authorName
                authorAvatar = comment.authorAvatar
                content = comment.content
            }
        }

        fun fromMap(map: Map<String, Any>): CommentBmob {
            return CommentBmob().apply {
                objectId = map["objectId"] as? String
                createdAt = map["createdAt"] as? String
                updatedAt = map["updatedAt"] as? String
                postId = map["postId"] as? String
                authorName = map["authorName"] as? String
                authorAvatar = map["authorAvatar"] as? String
                content = map["content"] as? String
            }
        }
    }
}

/**
 * 评论数据模型
 * 用于前端展示
 */
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