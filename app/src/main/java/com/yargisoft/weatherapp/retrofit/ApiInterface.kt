package com.yargisoft.weatherapp.retrofit

import com.yargisoft.weatherapp.model.entity.ModelClass
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