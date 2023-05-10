package com.yargisoft.weatherapp.POJO


data class ModelClass(
    var weather: List<Weather>,
    var main: Main,
    var wind: Wind,
    var sys: Sys,
    var id: Int,
    var name: String
) : java.io.Serializable{

}