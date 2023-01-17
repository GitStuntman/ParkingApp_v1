package com.example.parkingapp_v1

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.parkingapp_v1.databinding.ActivityMainBinding
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val permissionCode = 101
    private lateinit var locationManager: LocationManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Where I Parked"
        supportActionBar?.subtitle = "Find My Car"
         locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        checkLocationPermission()


        if(savedInstanceState == null)
            replaceFragment(HomeFragment(application as LocationApplication))

        binding.bottomNavBar.setOnItemSelectedListener {
            when(it.itemId){
                R.id.home-> replaceFragment(HomeFragment(application as LocationApplication).apply {
                    arguments = Bundle().apply {
                        HomeFragment.cardviewSetted
                    }
                })
                R.id.parking_list ->{replaceFragment(ListFragment(application as LocationApplication))
                }
            }
            true
        }
    }




    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                permissionCode
            )
            return
        }
        else
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                Toast.makeText(this, "GPS is Enabled in your device", Toast.LENGTH_SHORT).show();
            }else{
                showGPSDisabledAlertToUser();
            }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            permissionCode->{
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                        Toast.makeText(this, "GPS is Enabled in your devide", Toast.LENGTH_SHORT).show();
                    }else{
                        showGPSDisabledAlertToUser();
                    }
                }
                else
                    Toast.makeText(this,"Permission Not Granted", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun showGPSDisabledAlertToUser() {
        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
            .setCancelable(false)
            .setPositiveButton("Goto Settings Page To Enable GPS") { dialog, id ->
                val callGPSSettingIntent = Intent(
                    Settings.ACTION_LOCATION_SOURCE_SETTINGS
                )
                startActivity(callGPSSettingIntent)
            }
        alertDialogBuilder.setNegativeButton("Cancel") { dialog, id -> dialog.cancel() }
        val alert: AlertDialog = alertDialogBuilder.create()
        alert.show()
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.anchor,fragment)
            commit()
        }
    }
}