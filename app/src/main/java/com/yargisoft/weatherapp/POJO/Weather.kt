package com.yargisoft.weatherapp.POJO

import com.google.gson.annotations.SerializedName

data class Weather(var id: Int, var main: String,val description : String, var icon:String ) {


}