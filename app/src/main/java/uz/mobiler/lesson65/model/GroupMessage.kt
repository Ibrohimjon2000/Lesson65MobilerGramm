package uz.mobiler.lesson65.model

class GroupMessage {
    var text: String? = null
    var user: User? = null
    var date: String? = null
    var isChecked: Boolean? = null
    var key: String? = null
    var imageUrl: String? = null

    constructor()
    constructor(
        text: String?,
        user: User?,
        date: String?,
        isChecked: Boolean?,
        key: String?,
        imageUrl: String?
    ) {
        this.text = text
        this.user = user
        this.date = date
        this.isChecked = isChecked
        this.key = key
        this.imageUrl = imageUrl
    }
}