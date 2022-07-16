package uz.mobiler.lesson65.model

import java.io.Serializable

class Group : Serializable {
    var groupName: String? = null
    var groupAbout: String? = null
    var groupKey: String? = null

    constructor()

    constructor(groupName: String?, groupAbout: String?, groupKey: String?) {
        this.groupName = groupName
        this.groupAbout = groupAbout
        this.groupKey = groupKey
    }
}