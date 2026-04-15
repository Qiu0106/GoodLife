package com.example.smartelderlycare_app.data.model

class UserBmob {

    var objectId: String? = null
    var createdAt: String? = null
    var updatedAt: String? = null
    var sessionToken: String? = null

    var username: String? = null
    var mobilePhoneNumber: String? = null
    var mobilePhoneVerified: Boolean = false
    var email: String? = null
    var emailVerified: Boolean = false
    var nickname: String? = null
    var realName: String? = null
    var avatarUrl: String? = null
    var gender: String? = null
    var birthDate: String? = null
    var address: String? = null
    var emergencyContact: String? = null
    var emergencyPhone: String? = null
    var bloodType: String? = null
    var medicalHistory: String? = null
    var signature: String? = null

    fun toUser(): User {
        return User(
            id = objectId,
            objectId = objectId,
            phone = mobilePhoneNumber ?: username ?: "",
            password = null,
            nickname = nickname,
            realName = realName,
            avatarUrl = avatarUrl,
            gender = gender,
            birthDate = birthDate,
            address = address,
            emergencyContact = emergencyContact,
            emergencyPhone = emergencyPhone,
            bloodType = bloodType,
            medicalHistory = medicalHistory,
            signature = signature,
            token = sessionToken,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "username" to username,
            "mobilePhoneNumber" to mobilePhoneNumber,
            "nickname" to nickname,
            "realName" to realName,
            "avatarUrl" to avatarUrl,
            "gender" to gender,
            "birthDate" to birthDate,
            "address" to address,
            "emergencyContact" to emergencyContact,
            "emergencyPhone" to emergencyPhone,
            "bloodType" to bloodType,
            "medicalHistory" to medicalHistory,
            "signature" to signature
        )
    }

    companion object {
        fun create(phone: String, nickname: String? = null): UserBmob {
            return UserBmob().apply {
                this.username = phone
                this.mobilePhoneNumber = phone
                this.mobilePhoneVerified = false
                this.nickname = nickname ?: "用户${phone.takeLast(4)}"
            }
        }

        fun fromUser(user: User): UserBmob {
            return UserBmob().apply {
                objectId = user.objectId
                mobilePhoneNumber = user.phone
                username = user.phone
                nickname = user.nickname
                realName = user.realName
                avatarUrl = user.avatarUrl
                gender = user.gender
                birthDate = user.birthDate
                address = user.address
                emergencyContact = user.emergencyContact
                emergencyPhone = user.emergencyPhone
                bloodType = user.bloodType
                medicalHistory = user.medicalHistory
                signature = user.signature
            }
        }

        fun fromMap(map: Map<String, Any>): UserBmob {
            return UserBmob().apply {
                objectId = map["objectId"] as? String
                createdAt = map["createdAt"] as? String
                updatedAt = map["updatedAt"] as? String
                sessionToken = map["sessionToken"] as? String
                username = map["username"] as? String
                mobilePhoneNumber = map["mobilePhoneNumber"] as? String
                mobilePhoneVerified = map["mobilePhoneVerified"] as? Boolean ?: false
                email = map["email"] as? String
                emailVerified = map["emailVerified"] as? Boolean ?: false
                nickname = map["nickname"] as? String
                realName = map["realName"] as? String
                avatarUrl = map["avatarUrl"] as? String
                gender = map["gender"] as? String
                birthDate = map["birthDate"] as? String
                address = map["address"] as? String
                emergencyContact = map["emergencyContact"] as? String
                emergencyPhone = map["emergencyPhone"] as? String
                bloodType = map["bloodType"] as? String
                medicalHistory = map["medicalHistory"] as? String
                signature = map["signature"] as? String
            }
        }
    }
}
