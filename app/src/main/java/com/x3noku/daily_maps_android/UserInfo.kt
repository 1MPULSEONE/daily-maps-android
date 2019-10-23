package com.x3noku.daily_maps_android

import java.util.ArrayList
import java.util.LinkedList

class UserInfo {
    var nickname: String
    var tasksId: MutableList<String>
    var templatesId: MutableList<String>

    internal constructor() {
        this.nickname = "New UserInfo"
        this.tasksId = LinkedList()
        this.templatesId = ArrayList()
    }

    internal constructor(nickname: String) {
        this.nickname = nickname
        this.tasksId = LinkedList()
        this.templatesId = ArrayList()
    }

    fun addTask( taskId: String ) {
        this.tasksId.add(taskId)
    }

}
