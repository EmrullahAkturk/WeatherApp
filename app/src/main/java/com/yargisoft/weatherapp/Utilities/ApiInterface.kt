package com.yargisoft.weatherapp.Utilities

import com.yargisoft.weatherapp.POJO.ModelClass
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {

    @GET("weather")
    fun getCurrentWeahterData(
        @Query("lat") latitude: String,
        @Query("lon") longitude: String,
        @Query("APPID") api_key: String
    ): Call<ModelClass>


    @GET("weather")
    fun getCityWeahterData(
        @Query("q") cityName: String,
        @Query("APPID") api_key: String
    ): Call<ModelClass>

}