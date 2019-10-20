package com.x3noku.daily_maps_android

import java.util.ArrayList
import java.util.LinkedList

class User {
    var nickname: String
    var tasksId: List<String>
    var templatesId: List<String>

    internal constructor() {
        this.nickname = "New User"
        this.tasksId = LinkedList()
        this.templatesId = ArrayList()
    }

    internal constructor(nickname: String) {
        this.nickname = nickname
        this.tasksId = LinkedList()
        this.templatesId = ArrayList()
    }

}
