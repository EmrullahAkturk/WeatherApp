package com.yargisoft.weatherapp.view.fragments

import android.Manifest
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.yargisoft.weatherapp.model.entity.ModelClass
import com.yargisoft.weatherapp.R
import com.yargisoft.weatherapp.retrofit.ApiUtilities
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
   

    companion object {
        val PERMISSION_REQUEST_ACCESS_LOCATION: Int = 100
        private var currentLatitude: String = " "
        private var currentLongitude: String = " "
        val API_KEY = "78f93b7b53480ec14ff0b52923381468"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main_page, container, false)
        binding.rlMainLayout.visibility = View.GONE
        binding.pbLoading.visibility = View.VISIBLE
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        binding.etGetCityName.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                getCityWeather(binding.etGetCityName.text.toString())
                val view = requireActivity().currentFocus
                if (view != null) {
                    val imm: InputMethodManager =
                        requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                    binding.etGetCityName.clearFocus()
                }
                true
            } else {
                false
            }
        }

      getCurrentLocation()

        return binding.root
    }


    private fun setDataOnViews(body: ModelClass?) {
        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm")
        val currentDate = sdf.format(Date())
        binding.tvDateAndTime.text = currentDate
        binding.tvMaxTemp.text = "Day ${kelvinToCelcius(body!!.main.temp_max)} ° "
        binding.tvMinTemp.text = "Night ${kelvinToCelcius(body!!.main.temp_min)} ° "
        binding.tvTemp.text = " ${kelvinToCelcius(body!!.main.temp)} ° "
        binding.tvFeelsLike.text = "Feels ALike ${kelvinToCelcius(body!!.main.feels_like)} ° "
        binding.tvWeatherType.text = body.weather[0].description
        binding.tvSunrise.text = timeStampToLocalDate(body.sys.sunrise)
        binding.tvSunset.text = timeStampToLocalDate(body.sys.sunset)
        binding.tvPressure.text = body.main.pressure.toString()
        binding.tvHumidity.text = "${body.main.humidity} %"
        binding.tvWindSpeed.text = "${body.wind.speed} m/s"
        binding.tvTempFahrenheit.text =
            "${(kelvinToCelcius(body.main.temp)).times(1.8).plus(32).roundToInt()}"
        binding.etGetCityName.setText(body.name)
        updateUI(body.weather[0].id)
    }
    fun updateUI(id: Int) {
        if (id >= 200 && id <= 232) {
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
        } else if (id >= 300 && id <= 321) {

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
        } else if (id >= 500 && id <= 531) {
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
        } else if (id >= 600 && id <= 620) {
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
        } else if (id >= 700 && id <= 781) {
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
        } else if (id >= 801 && id <= 804) {
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
            binding.ivWeatherIcon.setImageResource(R.drawable.ic_cloud)
        }
    }



    fun getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(
              requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
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
            return
        }
        fusedLocationProviderClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
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
                fetchCurrentLocationWeather(
                    location.latitude.toString(),
                    location.longitude.toString()
                )
            }

        }
    }

    fun fetchCurrentLocationWeather(latitude: String, longitude: String){

        ApiUtilities.getApiInterface()?.getCurrentWeahterData(latitude, longitude,MainPageFragment.API_KEY)?.enqueue(object : Callback<ModelClass> {
                override fun onResponse(call: Call<ModelClass>, response: Response<ModelClass>) {
                    if (response.isSuccessful) {
                        setDataOnViews(response.body())
                       if (response.body() != null){
                           updateUI(response.body()!!.weather[0].id)
                       }
                        binding.pbLoading.visibility = View.GONE
                        binding.rlMainLayout.visibility = View.VISIBLE
                    }
                }
                override fun onFailure(call: Call<ModelClass>, t: Throwable) {
                }
            })
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

    fun getCityWeather(cityName: String) {

        ApiUtilities.getApiInterface()?.getCityWeahterData(cityName, MainPageFragment.API_KEY)
            ?.enqueue(object : Callback<ModelClass> {
                override fun onResponse(call: Call<ModelClass>, response: Response<ModelClass>) {
                    try {
                        setDataOnViews(response.body())
                        if (response.body() !=null){
                            updateUI(response.body()!!.weather[0].id)
                        }
                        binding.pbLoading.visibility = View.GONE
                        binding.rlMainLayout.visibility = View.VISIBLE
                    } catch (ex: java.lang.Exception) {
                        Toast.makeText(context, "Not a valid city", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onFailure(call: Call<ModelClass>, t: Throwable) {
                }

            })

    }




}