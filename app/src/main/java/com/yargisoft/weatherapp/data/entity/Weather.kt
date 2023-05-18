package com.yargisoft.weatherapp.data.entity

import com.google.gson.annotations.SerializedName

data class Weather(var id: Int, var main: String, val description: String, var icon: String) {


}