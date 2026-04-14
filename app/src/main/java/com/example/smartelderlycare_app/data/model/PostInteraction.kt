package com.example.smartelderlycare_app.data.model

data class PostInteraction(
    val id: Long? = null,
    val objectId: String? = null,
    val userId: String,
    val postId: String,
    val type: String,
    val createdAt: String? = null
)

class PostInteractionBmob {
    var objectId: String? = null
    var createdAt: String? = null
    var updatedAt: String? = null
    var userId: String? = null
    var postId: String? = null
    var type: String? = null

    fun toPostInteraction(): PostInteraction {
        return PostInteraction(
            id = objectId?.hashCode()?.toLong(),
            objectId = objectId,
            userId = userId ?: "",
            postId = postId ?: "",
            type = type ?: "like",
            createdAt = createdAt
        )
    }

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "postId" to postId,
            "type" to type
        )
    }

    companion object {
        fun fromPostInteraction(interaction: PostInteraction): PostInteractionBmob {
            return PostInteractionBmob().apply {
                objectId = interaction.objectId
                userId = interaction.userId
                postId = interaction.postId
                type = interaction.type
            }
        }

        fun fromMap(map: Map<String, Any>): PostInteractionBmob {
            return PostInteractionBmob().apply {
                objectId = map["objectId"] as? String
                createdAt = map["createdAt"] as? String
                updatedAt = map["updatedAt"] as? String
                userId = map["userId"] as? String
                postId = map["postId"] as? String
                type = map["type"] as? String
            }
        }
    }
}
