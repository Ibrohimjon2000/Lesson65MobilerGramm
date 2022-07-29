package uz.mobiler.lesson65.model

class Message {
    var text: String? = null
    var toUserUid: String? = null
    var fromUserUid: String? = null
    var date: String? = null
    var isChecked: Boolean? = null
    var key: String? = null
    var imageUrl: String? = null

    constructor()
    constructor(
        text: String?,
        toUserUid: String?,
        fromUserUid: String?,
        date: String?,
        isChecked: Boolean?,
        key: String?,
        imageUrl: String?
    ) {
        this.text = text
        this.toUserUid = toUserUid
        this.fromUserUid = fromUserUid
        this.date = date
        this.isChecked = isChecked
        this.key = key
        this.imageUrl = imageUrl
    }
}