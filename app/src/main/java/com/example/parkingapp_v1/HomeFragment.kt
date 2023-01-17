package com.example.parkingapp_v1

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.parkingapp_v1.databinding.FragmentHomeBinding
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.cardview_layout.view.*
import kotlinx.android.synthetic.main.cardview_layout.view.card_date
import kotlinx.android.synthetic.main.cardview_layout.view.card_location
import kotlinx.android.synthetic.main.cardview_layout.view.card_time
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


class HomeFragment(application: LocationApplication) : Fragment() {


    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var locationName: String = "No Location"
    private var mapImage: String = "null"
    private var stringLocation = ""
    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent
    private var alarmSet = false
    private var newCardView = true
    private var newLocation = true
    var bitMap: Bitmap? = null
    var stream: InputStream? = null
    private var currentLocation: Location =
        Location(mapImage, "demo", "demo", "demo", stringLocation)
    private val now = Calendar.getInstance()
    private val selectedDate = Calendar.getInstance()
    private val currentDate = Calendar.getInstance()
    private var formate = SimpleDateFormat("dd MMM yyyy", Locale.ITALY)
    private var timeFormat = SimpleDateFormat("HH:mm", Locale.ITALY)
    private val selectedTime = Calendar.getInstance()
    private var binding: FragmentHomeBinding? = null
    private val locationViewModel: LocationViewModel by viewModels {
        LocationItemModelFactory((application).repository)
    }

    private fun pickDate(
        date: String = getCurrentDate()
    ) {
        val timePicker = TimePickerDialog(requireContext(), { view, hourOfDay, minute ->
            selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
            selectedTime.set(Calendar.MINUTE, minute)
            val time = timeFormat.format(selectedTime.time)
            setAddItemUnavailable()
            if(binding!!.setAlarm.isChecked){
                alarmSet = true
                createNotificationChannel()
                setAlarm(selectedTime)
            }
            createLocationItem(date, time)
        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true)
        timePicker.show()
    }

    @SuppressLint("MissingPermission")
    private fun setAlarm(selectedTime: Calendar) {
        alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(),AlarmReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(context,0,intent,0)
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            selectedTime.timeInMillis,
            pendingIntent
        )

        Toast.makeText(requireContext(),"Alarm set successfully",Toast.LENGTH_LONG).show()

    }

    private fun createNotificationChannel() {
        val name: CharSequence = "ReminderChannel"
        val description = "Channel for alarm Manager"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("Alarm",name,importance)
        channel.description = description
        val notificationManager = getSystemService(requireContext(),NotificationManager::class.java)
        notificationManager!!.createNotificationChannel(channel)
    }

    private fun createLocationItem(date: String, time: String) {
        setLocationObject(date, time)
        setCardView()
        saveData()
    }

    private fun getCurrentDate(): String {
        currentDate.set(Calendar.YEAR, currentDate.get(Calendar.YEAR))
        currentDate.set(Calendar.MONTH, currentDate.get(Calendar.MONTH))
        currentDate.set(Calendar.DAY_OF_MONTH, currentDate.get(Calendar.DAY_OF_MONTH))
        return formate.format(currentDate.time)
    }

    private fun setCardView() {
        setImageFromURL(mapImage)
        binding!!.cardView.card_date.text = currentLocation.date
        binding!!.cardView.card_location.text = currentLocation.location
        binding!!.cardView.card_time.text =  currentLocation.time
        binding!!.btnRemove.visibility = View.VISIBLE
        binding!!.cardView.visibility = CardView.VISIBLE
        cardviewSetted = true
    }


    private fun saveData() {
        val image = currentLocation.mapImage
        val locationName = currentLocation.location
        val date = currentLocation.date
        val time = currentLocation.time
        val newLocation = Location(image, stringLocation, locationName, date, time)
        locationViewModel.addLocationItem(newLocation)
        locationViewModel.lastLocation.observe(viewLifecycleOwner) {
            locationID = it.id
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val fragmentBinding = FragmentHomeBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return binding?.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(CARD_VIEW_SETTED, cardviewSetted)
        locationID?.let { outState.putInt(LOCATION_ID, it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            cardviewSetted = savedInstanceState.getBoolean(CARD_VIEW_SETTED)
            locationID = savedInstanceState.getInt(LOCATION_ID)
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    stopLocationUpdates()
                    getLocation(location).execute()
                }
            }
        }
        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            500
        ).build()

        if (cardviewSetted && newCardView) {
            if (locationID != null && newLocation) {
                locationViewModel.getLocationFromID(locationID!!)
                locationViewModel.locationItem.observe(viewLifecycleOwner) {
                    restoreCardView(it)
                }
            }
            setAddItemUnavailable()
        }
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        binding!!.btnPark.setOnClickListener {
            if (!newLocation){
                locationViewModel.locationItem.removeObservers(viewLifecycleOwner)
                newLocation = true
            }
            createMapsObject()
        }
        binding!!.btnRemove.setOnClickListener {
            if (alarmSet){
                cancelAlarm()
            }
            setAddItemAvailable()
        }
        binding!!.cardView.setOnClickListener {
            openLocation()
        }
    }

    private fun cancelAlarm() {
        alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(),AlarmReceiver::class.java)
        alarmSet = false
        pendingIntent = PendingIntent.getBroadcast(requireContext(),0,intent,0)
        alarmManager.cancel(pendingIntent)
        Toast.makeText(requireContext(),"Alarm Cancelled",Toast.LENGTH_LONG).show()
    }

    private fun restoreCardView(it: Location) {
        newLocation = false
        cardviewSetted = true
        locationID = null
        setImageFromURL(it.mapImage)
        binding!!.cardView.card_date.text = it.date
        binding!!.cardView.card_location.text = it.location
        binding!!.cardView.card_time.text =  it.time
        binding!!.btnRemove.visibility = View.VISIBLE
        binding!!.cardView.visibility = CardView.VISIBLE
    }

    private fun setAddItemUnavailable() {
        binding!!.btnPark.apply {
            isEnabled = false
            setBackgroundColor(resources.getColor(com.google.android.material.R.color.button_material_dark))
        }
        cardviewSetted = true
    }

    private fun setAddItemAvailable() {
        binding!!.btnPark.apply {
            isEnabled = true
            setBackgroundColor(resources.getColor(com.google.android.material.R.color.design_default_color_primary))
        }
        cardviewSetted = false
        newCardView = false
        binding!!.btnRemove.visibility = View.INVISIBLE
        binding!!.cardView.visibility = CardView.INVISIBLE
    }

    private fun start() {
        if (binding!!.selectDate.isChecked && binding!!.selectHour.isEnabled)
            pickDate()
        else if (!binding!!.selectDate.isChecked && binding!!.selectHour.isEnabled)
            DatePickerDialog(
                requireContext(),
                { view, year, month, dayOfMonth ->
                    selectedDate.set(Calendar.YEAR, year)
                    selectedDate.set(Calendar.MONTH, month)
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    val date = formate.format(selectedDate.time)
                    pickDate(date)
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
            ).show()

    }

    private fun createMapsObject() {
        getUserLocation()
    }


    @SuppressLint("MissingPermission")
    private fun getUserLocation() {
        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
            if (it != null) {
                getLocation(it).execute()
            } else {
                startLocationUpdates()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private fun setLocationObject(date: String, time: String) {
        currentLocation.date = date
        currentLocation.time = time
        currentLocation.location = locationName
        currentLocation.mapImage = mapImage
    }

    private fun openLocation() {
        //open Location in GoogleMaps
        val uri = Uri.parse("geo:0, 0?q=$stringLocation")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun setImageFromURL(mapImage: String) {
        GetMap(mapImage, binding!!.cardMap).execute()
    }


    inner class getLocation(val location: android.location.Location) :
        AsyncTask<Void, Void, List<Address>>() {
        override fun doInBackground(vararg params: Void?): List<Address>? {
            try {
                val latitude: Double
                val longitude: Double
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                var address: List<Address>? = null
                try {
                    address = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    latitude = address!![0].latitude
                    longitude = address[0].longitude
                    mapImage =
                        "https://maps.googleapis.com/maps/api/staticmap?center=$latitude,$longitude&zoom=18&size=400x400" +
                                "&markers=color:red|$latitude,$longitude" +
                                "&key=AIzaSyBxTUdRO5rsq29Y2bK21xgHGdCQhkpE2Bg"
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                if (address != null && address.isNotEmpty()) {
                    return address
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: List<Address>?) {
            super.onPostExecute(result)
            stringLocation = result!![0].getAddressLine(0).toString()
            locationName = result[0].locality.toString()
            start()
        }
    }

    inner class GetMap(private val url: String, private val cardMap: ImageView) :
        AsyncTask<Void, Void, Bitmap>() {
        override fun doInBackground(vararg params: Void?): Bitmap? {
            try {
                val mapUrl = URL(url)
                stream = mapUrl.openConnection().content as InputStream
                bitMap = BitmapFactory.decodeStream(stream)

            } catch (e: IOException) {
                e.printStackTrace()
            }
            return bitMap
        }

        override fun onPostExecute(result: Bitmap?) {
            super.onPostExecute(result)
            cardMap.setImageBitmap(result)
        }
    }

    companion object {
        const val LOCATION_ID = "locationId"
        const val CARD_VIEW_SETTED = "cardviewSetted"
        var cardviewSetted = false
        var locationID: Int? = null
    }
}