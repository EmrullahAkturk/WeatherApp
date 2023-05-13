/*
package com.yargisoft.weatherapp.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.android.gms.location.FusedLocationProviderClient
import com.yargisoft.weatherapp.POJO.ModelClass
import com.yargisoft.weatherapp.Utilities.ApiUtilities
import com.yargisoft.weatherapp.ui.fragments.MainPageFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.*
import kotlin.math.roundToInt

class MainPageRepository {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private fun getCurrentLocation() {

        if (checkPermission()) {

            if (isLocationEnabled()) {

                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        requireContext(),
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermission()
                    return
                }


                fusedLocationProviderClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        Toast.makeText(requireContext(), "Null received", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Reached location succesfully ",
                            Toast.LENGTH_SHORT
                        ).show()
                        fetchCurrentLocationWeather(
                            location.latitude.toString(),
                            location.longitude.toString()
                        )
                    }
                }

            } else {
                Toast.makeText(requireContext(), "Turn on the location", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }

        } else {
            requestPermission()
        }

    }
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }
    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(), arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ), MainPageFragment.PERMISSION_REQUEST_ACCESS_LOCATİON
        )
    }
    private fun checkPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }
    private fun fetchCurrentLocationWeather(latitude: String, longitude: String) {
        binding.pbLoading.visibility = View.VISIBLE
        ApiUtilities.getApiInterface()?.getCurrentWeahterData(latitude, longitude,
            MainPageFragment.API_KEY
        )
            ?.enqueue(object : Callback<ModelClass> {
                override fun onResponse(call: Call<ModelClass>, response: Response<ModelClass>) {
                    if (response.isSuccessful) {

                        setDataOnViews(response.body())
                        println("Data alındı ${response.body()}")
                    }
                }

                override fun onFailure(call: Call<ModelClass>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
                }

            })


    }
    private fun setDataOnViews(body: ModelClass?) {
        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm")
        val currentDate = sdf.format(Date())
        if (body != null) {
            updateUI(body.weather[0].id)
        }
        binding.tvDateAndTime.text = currentDate
        binding.tvMaxTemp.text = "Day ${kelvinToCelcius(body!!.main.temp_min)} °  "
        binding.tvMinTemp.text = "Night ${kelvinToCelcius(body!!.main.temp_max)} °  "
        binding.tvTemp.text = " ${kelvinToCelcius(body!!.main.temp)} ° "
        binding.tvFeelsLike.text = " Feels Alike ${kelvinToCelcius(body!!.main.feels_like)} ° "
        binding.tvWeatherType.text = body.weather[0].main
        binding.tvSunrise.text = timeStampToLocalDate(body.sys.sunrise)
        binding.tvSunset.text = timeStampToLocalDate(body.sys.sunset)
        binding.tvPressure.text = body.main.pressure.toString()
        binding.tvHumidity.text = "%${body.main.humidity}"
        binding.tvWindSpeed.text = "${body.wind.speed} m/s"
        binding.tvTempFahrenheit.text =
            "${(kelvinToCelcius(body.main.temp)).times(1.8).plus(32).roundToInt()}"
        binding.etGetCityName.setText(body.name)


    }

    private fun timeStampToLocalDate(timeStamp: Long): String {
        val localTime = timeStamp.let {
            Instant.ofEpochSecond(it)
                .atZone(ZoneId.systemDefault())
                .toLocalTime()
        }
        return localTime.toString()
    }

    private fun kelvinToCelcius(temp: Double): Double {
        val intTemp = temp - 272.15
        return intTemp.toBigDecimal().setScale(1, RoundingMode.UP).toDouble()
    }

    private fun getCityWeather(cityName: String) {
        binding.pbLoading.visibility = View.VISIBLE
        ApiUtilities.getApiInterface()?.getCityWeahterData(cityName, API_KEY)
            ?.enqueue(object : Callback<ModelClass> {
                override fun onResponse(call: Call<ModelClass>, response: Response<ModelClass>) {
                    setDataOnViews(response.body())
                    if (response.body() != null) {
                        updateUI(response.body()!!.id)
                    }
                }

                override fun onFailure(call: Call<ModelClass>, t: Throwable) {
                    Toast.makeText(requireContext(), "Not a valid city", Toast.LENGTH_SHORT).show()
                }

            })
    }





    private fun updateUI(id: Int) {
        if (id in 200..232) {
            requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            requireActivity().window.statusBarColor =
                ContextCompat.getColor(requireContext(), R.color.clear)
            binding.rvToolbar.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.thunderstorms
                )
            )
            binding.rlSubLayout.background =
                ContextCompat.getDrawable(requireActivity(), R.drawable.thunderstorm_bg)
            binding.llMainBgBelow.background =
                ContextCompat.getDrawable(requireActivity(), R.drawable.thunderstorm_bg)
            binding.llMainBgAbove.background =
                ContextCompat.getDrawable(requireActivity(), R.drawable.thunderstorm_bg)
            binding.ivWeatherBg.setImageResource(R.drawable.thunderstorm_bg)
            binding.ivWeatherIcon.setImageResource(R.drawable.ic_thunderstorm)
            */
/*binding.tvWeatherType.text = body.weather[0].main*//*

        } else if (id in 300..321) {
            requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            requireActivity().window.statusBarColor =
                ContextCompat.getColor(requireContext(), R.color.clear)
            binding.rvToolbar.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.drizzle
                )
            )
            binding.rlSubLayout.background =
                ContextCompat.getDrawable(requireActivity(), R.drawable.drizzle_bg)
            binding.llMainBgBelow.background =
                ContextCompat.getDrawable(requireActivity(), R.drawable.drizzle_bg)
            binding.llMainBgAbove.background =
                ContextCompat.getDrawable(requireActivity(), R.drawable.drizzle_bg)
            binding.ivWeatherBg.setImageResource(R.drawable.drizzle_bg)
            binding.ivWeatherIcon.setImageResource(R.drawable.mist_bg)
        } else if (id in 500..531) {
            requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            requireActivity().window.statusBarColor =
                ContextCompat.getColor(requireContext(), R.color.rain)
            binding.rvToolbar.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.rain
                )
            )

            binding.rlSubLayout.background =
                ContextCompat.getDrawable(requireActivity(), R.drawable.rainy_bg)
            binding.llMainBgBelow.background =
                ContextCompat.getDrawable(requireActivity(), R.drawable.rainy_bg)
            binding.llMainBgAbove.background =
                ContextCompat.getDrawable(requireActivity(), R.drawable.rainy_bg)
            binding.ivWeatherBg.setImageResource(R.drawable.rainy_bg)
            binding.ivWeatherIcon.setImageResource(R.drawable.ic_rainy)
        } else if (id in 600..620) {
            requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            requireActivity().window.statusBarColor =
                ContextCompat.getColor(requireContext(), R.color.snow)
            binding.rvToolbar.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.snow
                )
            )
            binding.rlSubLayout.background =
                ContextCompat.getDrawable(requireActivity(), R.drawable.snow_bg)
            binding.llMainBgBelow.background =
                ContextCompat.getDrawable(requireActivity(), R.drawable.snow_bg)
            binding.llMainBgAbove.background =
                ContextCompat.getDrawable(requireActivity(), R.drawable.snow_bg)
            binding.ivWeatherBg.setImageResource(R.drawable.snow_bg)
            binding.ivWeatherIcon.setImageResource(R.drawable.ic_snow)
        } else if (id in 700..781) {
            requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            requireActivity().window.statusBarColor =
                ContextCompat.getColor(requireContext(), R.color.atmosphere)
            binding.rvToolbar.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.atmosphere
                )
            )

            binding.rlSubLayout.background =
                ContextCompat.getDrawable(requireActivity(), R.drawable.mist_bg)
            binding.llMainBgBelow.background =
                ContextCompat.getDrawable(requireActivity(), R.drawable.mist_bg)
            binding.llMainBgAbove.background =
                ContextCompat.getDrawable(requireActivity(), R.drawable.mist_bg)
            binding.ivWeatherBg.setImageResource(R.drawable.mist_bg)
            binding.ivWeatherIcon.setImageResource(R.drawable.ic_mist)
        } else if (id == 800) {
            requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            requireActivity().window.statusBarColor =
                ContextCompat.getColor(requireContext(), R.color.clear)
            binding.rvToolbar.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.clear
                )
            )

            binding.rlSubLayout.background =
                ContextCompat.getDrawable(requireActivity(), R.drawable.clear_bg)
            binding.llMainBgBelow.background =
                ContextCompat.getDrawable(requireActivity(), R.drawable.clear_bg)
            binding.llMainBgAbove.background =
                ContextCompat.getDrawable(requireActivity(), R.drawable.clear_bg)
            binding.ivWeatherBg.setImageResource(R.drawable.clear_bg)
            binding.ivWeatherIcon.setImageResource(R.drawable.ic_clear)
        }
    }



}*/
