package com.yargisoft.weatherapp.ui.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.yargisoft.weatherapp.R
import com.yargisoft.weatherapp.databinding.FragmentLoginPageBinding

class LoginPageFragment : Fragment() {
    companion object{
        val PERMISSION_REQUEST_ACCESS_LOCATION: Int = 100
    }
    private lateinit var binding: FragmentLoginPageBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login_page, container, false)


        binding.gecisButon.setOnClickListener {

            if (!isLocationEnabled(requireContext())) {
                showLocationSettingsDialog(requireContext())
            }
            else{
                if(!checkPermission()){
                    requestPermission()
                }else{
                    Navigation.findNavController(it).navigate(R.id.mainGecis)
                    Toast.makeText(requireContext(),"İzinler Sağlandı", Toast.LENGTH_SHORT).show()
                }
            }
        }
        return binding.root
    }


    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    private fun showLocationSettingsDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Enable Location")
        builder.setMessage("Location services need to be enabled for this app. Do you want to enable it?")
        builder.setPositiveButton("Yes") { dialog, which ->
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            context.startActivity(intent)
        }
        builder.setNegativeButton("No") { dialog, which -> dialog.cancel() }
        builder.show()
    }
    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(), arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ), PERMISSION_REQUEST_ACCESS_LOCATION
        )
    }
    private fun checkPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }


}