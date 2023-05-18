package com.yargisoft.weatherapp.ui.fragments

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
import com.yargisoft.weatherapp.data.entity.ModelClass
import com.yargisoft.weatherapp.R
import com.yargisoft.weatherapp.Utilities.ApiUtilities
import com.yargisoft.weatherapp.databinding.FragmentMainPageBinding
import com.yargisoft.weatherapp.ui.viewmodel.MainViewModel
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

    private var viewModel = MainViewModel()

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
       /* binding.rlMainLayout.visibility = View.GONE*/
        binding.etGetCityName.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val cityWeather = viewModel.getCityWeather(binding.etGetCityName.text.toString())
                setDataOnViews(cityWeather)
                if(cityWeather != null){
                    updateUI(cityWeather.weather[0].id)
                }
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

        val currentWeather = viewModel.getCurrentLocation()
        setDataOnViews(currentWeather)
        if (currentWeather !=null){
            updateUI(currentWeather.weather[0].id)
        }


        return binding.root
    }


    private fun setDataOnViews(body: ModelClass?) {
        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm")
        val currentDate = sdf.format(Date())
        binding.tvDateAndTime.text = currentDate
        binding.tvMaxTemp.text = "Day ${viewModel.kelvinToCelcius(body!!.main.temp_max)} ° "
        binding.tvMinTemp.text = "Night ${viewModel.kelvinToCelcius(body!!.main.temp_min)} ° "
        binding.tvTemp.text = " ${viewModel.kelvinToCelcius(body!!.main.temp)} ° "
        binding.tvFeelsLike.text = "Feels ALike ${viewModel.kelvinToCelcius(body!!.main.feels_like)} ° "
        binding.tvWeatherType.text = body.weather[0].description
        binding.tvSunrise.text = viewModel.timeStampToLocalDate(body.sys.sunrise)
        binding.tvSunset.text = viewModel.timeStampToLocalDate(body.sys.sunset)
        binding.tvPressure.text = body.main.pressure.toString()
        binding.tvHumidity.text = "${body.main.humidity} %"
        binding.tvWindSpeed.text = "${body.wind.speed} m/s"
        binding.tvTempFahrenheit.text =
            "${(viewModel.kelvinToCelcius(body.main.temp)).times(1.8).plus(32).roundToInt()}"
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






}