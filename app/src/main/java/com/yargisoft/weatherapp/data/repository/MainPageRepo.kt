package com.yargisoft.weatherapp.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.view.Display.Mode
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.yargisoft.weatherapp.MainActivity
import com.yargisoft.weatherapp.Utilities.ApiUtilities
import com.yargisoft.weatherapp.data.entity.ModelClass
import com.yargisoft.weatherapp.ui.fragments.MainPageFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.RoundingMode
import java.time.Instant
import java.time.ZoneId

class MainPageRepo {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val activity = MainActivity()
    private var modelClass: ModelClass? = null
    private val context: Context
        get() {
            TODO()
        }

    init {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)
    }


    fun getCurrentLocation() : ModelClass?{

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return modelClass
        }
        fusedLocationProviderClient.lastLocation.addOnCompleteListener(activity) { task ->
            val location: Location? = task.result
            if (location == null) {
                Toast.makeText(context, "Null received", Toast.LENGTH_SHORT).show()
                // requestPermission()
            } else {
                Toast.makeText(
                    context,
                    "Reached location succesfully ",
                    Toast.LENGTH_SHORT
                ).show()
               modelClass = fetchCurrentLocationWeather(
                    location.latitude.toString(),
                    location.longitude.toString()
                )
            }

        }
        return modelClass
    }

    fun fetchCurrentLocationWeather(latitude: String, longitude: String):ModelClass? {

        ApiUtilities.getApiInterface()?.getCurrentWeahterData(
            latitude, longitude,
            MainPageFragment.API_KEY
        )
            ?.enqueue(object : Callback<ModelClass> {
                override fun onResponse(call: Call<ModelClass>, response: Response<ModelClass>) {
                    if (response.isSuccessful) {
                       modelClass = response.body()
                    }
                }

                override fun onFailure(call: Call<ModelClass>, t: Throwable) {
                }

            })

        return modelClass
    }



    fun timeStampToLocalDate(timeStamp: Long): String {
        val localTime = timeStamp.let {
            Instant.ofEpochSecond(it)
                .atZone(ZoneId.systemDefault())
                .toLocalTime()
        }
        return localTime.toString()
    }

    fun kelvinToCelcius(temp: Double): Double {
        val intTemp = temp - 272.15
        return intTemp.toBigDecimal().setScale(1, RoundingMode.UP).toDouble()
    }

    fun getCityWeather(cityName: String): ModelClass? {

        ApiUtilities.getApiInterface()?.getCityWeahterData(cityName, MainPageFragment.API_KEY)
            ?.enqueue(object : Callback<ModelClass> {
                override fun onResponse(call: Call<ModelClass>, response: Response<ModelClass>) {
                    try {
                        modelClass = response.body()
                    } catch (ex: java.lang.Exception) {
                        Toast.makeText(context, "Not a valid city", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onFailure(call: Call<ModelClass>, t: Throwable) {
                }

            })
        return modelClass
    }

    fun updateUI(id: Int) {

    }

}