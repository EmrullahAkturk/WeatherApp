package com.yargisoft.weatherapp.model.entity

data class Sys(
    var type: Int,
    var id: Int,
    var country: String,
    var sunrise: Long,
    var sunset: Long
) : java.io.Serializable {
}