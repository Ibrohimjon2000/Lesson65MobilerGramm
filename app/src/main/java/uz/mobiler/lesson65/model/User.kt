package uz.mobiler.lesson65.model

import java.io.Serializable

class User : Serializable {
    var displayName: String? = null
    var uid: String? = null
    var email: String? = null
    var photoUrl: String? = null
    var isOnline: Boolean? = null
    var token:String?=null

    constructor()

    constructor(
        displayName: String?,
        uid: String?,
        email: String?,
        photoUrl: String?,
        isOnline: Boolean?,
        token: String?
    ) {
        this.displayName = displayName
        this.uid = uid
        this.email = email
        this.photoUrl = photoUrl
        this.isOnline = isOnline
        this.token = token
    }

    override fun toString(): String {
        return "User(displayName=$displayName, uid=$uid, email=$email, photoUrl=$photoUrl, isOnline=$isOnline, token=$token)"
    }
}
