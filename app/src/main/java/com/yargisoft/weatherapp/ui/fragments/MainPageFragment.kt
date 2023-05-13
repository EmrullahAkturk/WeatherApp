package com.yargisoft.weatherapp.ui.fragments

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.yargisoft.weatherapp.POJO.ModelClass
import com.yargisoft.weatherapp.R
import com.yargisoft.weatherapp.Utilities.ApiUtilities
import com.yargisoft.weatherapp.databinding.FragmentMainPageBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.*
import kotlin.math.roundToInt

class MainPageFragment : Fragment() {

    private lateinit var binding: FragmentMainPageBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main_page, container, false)
        binding.rlMainLayout.visibility = View.GONE

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        getCurrentLocation()



        return binding.root
    }

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
            binding.rlMainLayout.visibility = View.VISIBLE
            binding.pbLoading.visibility = View.GONE
        } else {
            requestPermission()
        }

    }


    private fun fetchCurrentLocationWeather(latitude: String, longitude: String) {

        binding.pbLoading.visibility = View.VISIBLE
        ApiUtilities.getApiInterface()?.getCurrentWeahterData(latitude, longitude, API_KEY)
            ?.enqueue(object : Callback<ModelClass> {
                override fun onResponse(call: Call<ModelClass>, response: Response<ModelClass>) {
                    if (response.isSuccessful) {

                        setDataOnViews(response.body())
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
        binding.tvDateAndTime.text = currentDate
        //binding.tvDayMaxTemp = "Day ${kelvinToCelcius(body!!.main.temp_max)} ° "
        //binding.tvDayMinTemp
        binding.tvTemp.text = " ${kelvinToCelcius(body!!.main.temp)} ° "
        binding.tvFeelsLike.text = "Feels ALike ${kelvinToCelcius(body!!.main.feels_like)} ° "
        binding.tvWeatherType.text = body.weather[0].main
        binding.tvSunrise.text = timeStampToLocalDate(body.sys.sunrise)
        binding.tvSunset.text = timeStampToLocalDate(body.sys.sunset)
        binding.tvPressure.text = body.main.pressure.toString()
        binding.tvHumidity.text = "${body.main.humidity} %"
        binding.tvWindSpeed.text = "${body.wind.speed} m/s"
        binding.tvTempFahrenheit.text = "${(kelvinToCelcius(body.main.temp)).times(1.8).plus(32).roundToInt()}"
        binding.etGetCityName.setText(body.name)

        updateUI(body.weather[0].id)
    }

    fun updateUI(id: Int) {
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
            /*binding.tvWeatherType.text = body.weather[0].main*/
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

    private fun timeStampToLocalDate(timeStamp: Long): String {
        val localTime = timeStamp.let {
            Instant.ofEpochSecond(it)
                .atZone(ZoneId.systemDefault())
                .toLocalTime()
        }
        return localTime.toString()
    }

    private fun kelvinToCelcius(temp: Double): Double {
        var intTemp = temp - 272.15
        return intTemp.toBigDecimal().setScale(1, RoundingMode.UP).toDouble()
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
            ), PERMISSION_REQUEST_ACCESS_LOCATİON
        )
    }

    private fun getCityWeather(cityName: String) {
        //binding.pbLoading.visibility = View.VISIBLE
        ApiUtilities.getApiInterface()?.getCityWeahterData(cityName, MainPageFragment.API_KEY)
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






    companion object {
        private const val PERMISSION_REQUEST_ACCESS_LOCATİON = 100
        val API_KEY = "78f93b7b53480ec14ff0b52923381468"
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

   /* override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_ACCESS_LOCATİON) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }*/

    override fun onResume() {
        super.onResume()
    }

}