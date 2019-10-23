package com.x3noku.daily_maps_android

import com.google.android.gms.maps.model.LatLng

class Task internal constructor() {
    var nameOfTask: String? = null
    var startTimeOfTask: Int = 0
    var durationOfTask: Long = 0
    var priorityOfTask: Int = 0
    var coordinatesOfTask: LatLng? = null

    init {
        //ToDo: add dependence to settings values
        nameOfTask = ""
        startTimeOfTask = 12 * 60
        durationOfTask = 15
        priorityOfTask = 2 // 0 1 2 3 4
    }

}
