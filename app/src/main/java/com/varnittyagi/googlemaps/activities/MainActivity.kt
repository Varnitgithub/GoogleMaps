package com.varnittyagi.googlemaps.activities

import MessageAdapter
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.messaging.FirebaseMessaging
import com.pusher.pushnotifications.PushNotifications
import com.varnittyagi.googlemaps.R
import com.varnittyagi.googlemaps.application.ApplicationClass
import com.varnittyagi.googlemaps.databinding.MainActivityBinding
import com.varnittyagi.googlemaps.fragments.MapsFragment
import com.varnittyagi.googlemaps.models.LastOnlineData
import com.varnittyagi.googlemaps.models.LatLngClass
import com.varnittyagi.googlemaps.models.LocationModel
import com.varnittyagi.googlemaps.models.Messages
import com.varnittyagi.googlemaps.models.TypingStatus
import com.varnittyagi.googlemaps.models.UserDetails
import com.varnittyagi.googlemaps.services.NotificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

class MainActivity : AppCompatActivity() {
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var binding: MainActivityBinding
    private lateinit var currentUserUid: String
    private lateinit var receiverUserUid: String
    private lateinit var messageList: ArrayList<Messages>
    private var receiverName: String? = null
    private var previousMessage: String? = null
    private var newMessage: String? = null
    private var receiverPhoto: String? = null
    private var userName: String? = null
    private val REQUEST_LOCATION_PERMISSION = 1
    var lastCount:Int = 0
    private var latitude: String? = null
    private var longitude: String? = null
    private var senderUserUid: String? = null
    private var latitudeLivedata: MutableLiveData<String>? = null
    private var receiverLatitude: String? = null
    private var receiverLongitude: String? = null
    private var inOnCreate: Boolean = false
    private var currentUserLatitude: String? = null
    private var currentUserLongitude: String? = null
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var liveTimeData: MutableLiveData<LastOnlineData>
    private var livetimeString: String = ""
    private lateinit var realTime: MutableLiveData<String>
    private lateinit var receiverStatus: MutableLiveData<LastOnlineData>

    private lateinit var typingaStatusLiveData: MutableLiveData<TypingStatus>
    private lateinit var liveLocationLiveData: MutableLiveData<LatLngClass>
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle
    private var lastHeight = 0

    private lateinit var locationManager: LocationManager

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.main_activity)
        val applicationClass = application as ApplicationClass
        currentUserLatitude = intent.getStringExtra("currentUserLatitude")
        currentUserLongitude = intent.getStringExtra("currentUserLongitude")
        findViewById<FrameLayout>(R.id.frameLayout)
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)

        binding.attachItems.setOnClickListener {
            selectFromMenu(it)
        }

        val mapsFragment =
            (supportFragmentManager.findFragmentByTag("MAPS_FRAGMENT_TAG") as MapsFragment?)

        if (mapsFragment != null && mapsFragment.isAdded) {
            // If the fragment is added, update the map
            mapsFragment.updateMap(
                LatLng(
                    27.00,
                    70.00
                )
            )
        } else {
            // If the fragment is not added, add it for the first time
            val newMapsFragment = MapsFragment()
            newMapsFragment.arguments = Bundle().apply {
                putParcelable(
                    "LATLNG",
                    LatLng(
                        27.00,
                        70.00
                    )
                )
            }

            supportFragmentManager.beginTransaction()
                .add(
                    R.id.frameLayout,
                    newMapsFragment,
                    "MAPS_FRAGMENT_TAG"
                )
                .commit()
        }

        liveLocationLiveData = MutableLiveData()
        liveTimeData = MutableLiveData()
        realTime = MutableLiveData()
        receiverStatus = MutableLiveData()
        typingaStatusLiveData = MutableLiveData()

        firebaseAuth = FirebaseAuth.getInstance()
        currentUserUid = firebaseAuth.currentUser?.uid!!
        databaseReference = FirebaseDatabase.getInstance().reference
        val user = intent.getSerializableExtra("user") as? UserDetails



        currentUserUid = firebaseAuth.currentUser?.uid.toString()

        if (user != null && firebaseAuth.currentUser?.uid != null) {


            receiverUserUid = user.uid.toString()
            receiverName = user.name
            receiverPhoto = user.profile

        }





        messageAdapter = MessageAdapter(this, receiverPhoto ?: "")

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.chatRecyclerview.layoutManager = layoutManager
        messageList = arrayListOf()
        userName = intent.getStringExtra("userName")
        binding.chatRecyclerview.adapter = messageAdapter

        val rootView = findViewById<View>(android.R.id.content)
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = android.graphics.Rect()
            rootView.getWindowVisibleDisplayFrame(rect)
            val screenHeight = rootView.height
            val keypadHeight = screenHeight - rect.bottom

            if (keypadHeight > screenHeight * 0.45) { // Adjust this threshold as needed
                // Keyboard is open
                val params = binding.chatRecyclerview.layoutParams as ViewGroup.MarginLayoutParams
                params.bottomMargin = keypadHeight + 6
                binding.chatRecyclerview.layoutParams = params
            } else {
                // Keyboard is closed
                val params = binding.chatRecyclerview.layoutParams as ViewGroup.MarginLayoutParams
                params.bottomMargin = 0
                binding.chatRecyclerview.layoutParams = params
            }
        }
        getLatLng()
//        liveLocationLiveData.observe(this) {
//        }
         liveTimeData.observe(this) { liveTimeData ->
        receiverStatus.observe(this) { receiverStatus ->




                if (receiverStatus.lastOnlineTime == liveTimeData.lastOnlineTime) {
                    binding.activeNow.text = "Active now"
                } else {
                    binding.activeNow.text = receiverStatus.lastOnlineTime?:"df"
                }


            }
        }


        //binding.chatRecyclerview.smoothScrollToPosition(messageAdapter.itemCount-1)
        fetchLocation()

        typingaStatusLiveData.observe(this) {
            Log.d("TAGGGGGGGG", "onCreate: $it is the status")
            databaseReference.child("TypingStatus").child(receiverUserUid).setValue(it)
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (it.isSuccessful) {
                val result = it.result

                Log.d("TAGGGGGGGG", "onCreate:$result ")
            }
        }
        binding.expandMap.setOnClickListener {

            val intent = Intent(this, MapActivity::class.java)
            intent.putExtra("latitudeToMap", receiverLatitude ?: "29.306522")
            intent.putExtra("longitudeToMap", receiverLongitude ?: "77.649698")
            startActivity(intent)
        }

        toggle =
            ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.receiverUserPhoto.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        setReceiverDashBoard()
        var count = 0

        binding.navBtn.setOnClickListener {
            var headerview = navigationView.getHeaderView(0)
            var headerImage = headerview.findViewById<ImageView>(R.id.headerImage)
            headerImage.setImageResource(R.drawable.userpng)

            drawerLayout.openDrawer(GravityCompat.START)
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    Toast.makeText(this, "profile", Toast.LENGTH_SHORT).show()
                    true
                }

                R.id.map -> {
                    startActivity(Intent(this, MapActivity::class.java))
                    Toast.makeText(this, "map", Toast.LENGTH_SHORT).show()
                    true
                }

                R.id.locationFinder -> {
                    startActivity(Intent(this, MapActivity::class.java))

                    Toast.makeText(this, "locationFinder", Toast.LENGTH_SHORT).show()
                    true
                }

                R.id.settings -> {
                    Toast.makeText(this, "settings", Toast.LENGTH_SHORT).show()
                    true
                }

                R.id.help -> {
                    Toast.makeText(this, "help", Toast.LENGTH_SHORT).show()
                    true
                }

                R.id.termsCondition -> {
                    Toast.makeText(this, "termsCondition", Toast.LENGTH_SHORT).show()
                    true
                }

                R.id.signout -> {
                    signOut()
                    Toast.makeText(this, "user signout", Toast.LENGTH_SHORT).show()
                    true
                }

                else -> false
            }

        }

        liveTimeData.observe(this) {

            livetimeString = it.lastOnlineTime
            databaseReference.child("LastOnline").child(currentUserUid).setValue(it)
        }

        binding.sendBtn.setOnClickListener {
            if (binding.userMessage.text.toString().isNotEmpty()) {
                sendMessageOnFirebase()
                binding.userMessage.text.clear()
            } else {
                Toast.makeText(this, "Please write a message", Toast.LENGTH_SHORT).show()
            }

        }
        readMessageFromFirebase()
        detectLocation()
        inOnCreate = true
        //  startService(Intent(this, NotificationService::class.java))
        getReceiverStatus()
        updateTime()

        handler.post(object : Runnable {
            override fun run() {
                updateTime()
                handler.postDelayed(this, 100) // 1000 milliseconds = 1 second
            }
        })



        binding.userMessage.addTextChangedListener(object : TextWatcher {
            var count = 0
            private var typingTimer: Timer? = null
            private val DELAY: Long = 1000

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // You can add some logging here to see if this method gets called
                Log.d("TAGGGGGGGGG", "onTextChanged: text changed $p0")
                if (p0.toString().trim().length == 0) {
                    if ( count<=3){
                        typingaStatusLiveData.postValue(TypingStatus(false,count))
                        count++
                    }else{
                        typingaStatusLiveData.postValue(TypingStatus(false,count))
                    count--
                    }


                } else {
                    if ( count<=3){
                        typingaStatusLiveData.postValue(TypingStatus(true,count))
                        count++
                    }else{
                        typingaStatusLiveData.postValue(TypingStatus(true,count))
                        count--
                    }

                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })

    }

    private fun getReceiverStatus() {
        databaseReference.child("LastOnline").child(receiverUserUid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                        val time = snapshot.getValue(LastOnlineData::class.java)
                        receiverStatus.postValue(time)


                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle onCancelled if needed
                }
            })
    }


    override fun onResume() {
        super.onResume()
        latitudeLivedata?.observe(this) {
        }
    }


    private fun selectFromMenu(view: View) {
        var count = 0
        var menu = PopupMenu(this, view)
        val inflater = menu.menuInflater
        inflater.inflate(R.menu.attachmenu, menu.menu)

        menu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.requestLocation -> {

                    count++
                    if (binding.userMessage.text.toString().isNotEmpty()) {
                        databaseReference.child("location").child(receiverUserUid).setValue(
                            LocationModel(
                                userName ?: "User",
                                binding.userMessage.text.toString(), count
                            )
                        )
                    } else {
                        databaseReference.child("location").child(receiverUserUid).setValue(
                            LocationModel(
                                userName,
                                "Send me your location", count
                            )

                        )
                    }


                    true
                }

                R.id.myLocation -> {
                    Toast.makeText(this, "location find", Toast.LENGTH_SHORT).show()
                    databaseReference.child("UserCurrentLocation").child(currentUserUid)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {

                                val latitude = snapshot.child("latitude").value.toString()
                                val longitude = snapshot.child("longitude").value.toString()

                                getAddressFromLocation(latitude.toDouble(), longitude.toDouble())
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }
                        })





                    true
                }
                // Add more cases as needed

                R.id.trackLocation -> {
                    databaseReference.child("UserCurrentLocation").child(receiverUserUid)
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val latLng = snapshot.getValue(LatLngClass::class.java)

                                if (latLng != null) {

                                    val mapsFragment =
                                        (supportFragmentManager.findFragmentByTag("MAPS_FRAGMENT_TAG") as MapsFragment?)

                                    if (mapsFragment != null && mapsFragment.isAdded) {
                                        // If the fragment is added, update the map
                                        mapsFragment.updateMap(
                                            LatLng(
                                                latLng.latitude?.toDouble()!!,
                                                latLng.longitude?.toDouble()!!
                                            )
                                        )
                                    } else {
                                        // If the fragment is not added, add it for the first time
                                        val newMapsFragment = MapsFragment()
                                        newMapsFragment.arguments = Bundle().apply {
                                            putParcelable(
                                                "LATLNG",
                                                LatLngClass(latLng.latitude, latLng.longitude)
                                            )
                                        }

                                        supportFragmentManager.beginTransaction()
                                            .add(
                                                R.id.frameLayout,
                                                newMapsFragment,
                                                "MAPS_FRAGMENT_TAG"
                                            )
                                            .commit()
                                    }

                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }
                        })

                    true
                }

                else -> false
            }


        }
        menu.show()

    }

    private fun sendMessageOnFirebase() {
        databaseReference.child("messages").push().setValue(
            Messages(
                binding.userMessage.text.toString().trim(),
                currentUserUid,
                receiverUserUid, livetimeString
            )
        )
    }

    private fun readMessageFromFirebase() {
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                messageList.clear()
                for (messageSnapshot: DataSnapshot in dataSnapshot.children) {
                    val message = messageSnapshot.getValue(Messages::class.java)
                    senderUserUid = message?.senderId
                    if (message?.senderId == currentUserUid && message.recieverId == receiverUserUid ||
                        message?.senderId == receiverUserUid && message.recieverId == currentUserUid
                    ) {
                        messageList.add(message)
                        newMessage = message.message
                        previousMessage = message.message
                        //  receiverName?.let { MyFirebaseMessagingService().generateNotification(it,message.message) }
                    }
                }

                messageAdapter.updateList(messageList)
                binding.chatRecyclerview.smoothScrollToPosition(messageList.size + 3)

            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("Error: ${databaseError.message}")
            }
        }
        databaseReference.child("messages").addValueEventListener(valueEventListener)
    }

    override fun onPause() {
        super.onPause()
        val intent = Intent(this@MainActivity, NotificationService::class.java)
        intent.putExtra("receiverName", receiverName)
        intent.putExtra("receiverPhoto", receiverPhoto)
        intent.putExtra("receiveruserid", receiverUserUid)
        startService(intent)
    }

    override fun onStop() {
        super.onStop()

    }


    private fun updateTime() {

        val currentTime = System.currentTimeMillis()
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val formattedTime = sdf.format(Date(currentTime))
        if (lastCount<=3){
            liveTimeData.postValue(LastOnlineData(formattedTime,lastCount))
            lastCount++
        }else{
            liveTimeData.postValue(LastOnlineData(formattedTime,lastCount))
            lastCount--
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, NotificationService::class.java))
    }

    private fun detectLocation() {
        databaseReference.child("location").child(currentUserUid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val location = snapshot.getValue<LocationModel>()

                    if (location?.message != null) {

                        val dialogView = layoutInflater.inflate(R.layout.location_dialog, null)
                        val nameTextView: TextView = dialogView.findViewById(R.id.nameTextView)
                        val messageTextView: TextView =
                            dialogView.findViewById(R.id.messageTextView)
                        val cancelButton: Button = dialogView.findViewById(R.id.cancelButton)
                        val sendButton: Button = dialogView.findViewById(R.id.sendButton)

                        nameTextView.text = location.name
                        messageTextView.text = location.message

                        val alertDialog = AlertDialog.Builder(this@MainActivity)
                            .setView(dialogView)
                            .create()

                        alertDialog.show()

                        cancelButton.setOnClickListener {
                            alertDialog.dismiss()
                        }
                        sendButton.setOnClickListener {
                            databaseReference.child("LocationDetails").child(receiverUserUid)
                                .setValue(LatLngClass(latitude ?: "27", longitude ?: "30"))
                            alertDialog.dismiss()
                        }
                    }


                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun setReceiverDashBoard() {
        binding.receiverUserName.text = receiverName
        Glide.with(this).load(Uri.parse(receiverPhoto)).placeholder(R.drawable.userpng)
            .into(binding.receiverUserPhoto)
    }

    private fun savedataonFirebase(latLngClass: LatLngClass) {
        val map = HashMap<String, String>()
        map["latitude"] = latLngClass.latitude.toString()
        map["longitude"] = latLngClass.longitude.toString()

        firebaseAuth.currentUser?.uid?.let {
            databaseReference.child("UserCurrentLocation").child(it).setValue(map)
        }
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            latitude = location.latitude.toString()
            longitude = location.longitude.toString()
            savedataonFirebase(
                LatLngClass(
                    location.latitude.toString(),
                    location.longitude.toString()
                )
            )
            liveLocationLiveData.postValue(
                LatLngClass(
                    location.latitude.toString(),
                    location.longitude.toString()
                )
            )
            // Handle location changes here
            // The "location" parameter contains the updated location
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            // Handle status changes if needed
        }

        override fun onProviderEnabled(provider: String) {
            // Handle provider enabled
        }

        override fun onProviderDisabled(provider: String) {
            // Handle provider disabled
        }
    }

    private fun fetchLocation() {
        databaseReference.child("LocationDetails")
            .child(currentUserUid).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val fetchedLocation = snapshot.getValue<LatLngClass>()
                    if (fetchedLocation != null) {
                        receiverLatitude = fetchedLocation.latitude
                        receiverLongitude = fetchedLocation.longitude

                        val mapsFragment =
                            (supportFragmentManager.findFragmentByTag("MAPS_FRAGMENT_TAG") as MapsFragment?)

                        if (mapsFragment != null && mapsFragment.isAdded) {
                            // If the fragment is added, update the map
                            mapsFragment.updateMap(
                                LatLng(
                                    fetchedLocation.latitude?.toDouble()!!,
                                    fetchedLocation.longitude?.toDouble()!!
                                )
                            )
                        } else {
                            // If the fragment is not added, add it for the first time
                            val newMapsFragment = MapsFragment()
                            newMapsFragment.arguments = Bundle().apply {
                                putParcelable(
                                    "LATLNG",
                                    LatLngClass(fetchedLocation.latitude, fetchedLocation.longitude)
                                )
                            }

                            supportFragmentManager.beginTransaction()
                                .add(
                                    R.id.frameLayout,
                                    newMapsFragment,
                                    "MAPS_FRAGMENT_TAG"
                                )
                                .commit()
                        }


                        getAddressFromLocation(
                            fetchedLocation.latitude?.toDouble() ?: 29.306522,
                            fetchedLocation.longitude?.toDouble() ?: 77.649698
                        )
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun getLatLng() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Request location updates
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                0f,
                locationListener
            )

        } else {
            // Request location permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION_PERMISSION
        )
    }

    private fun sendLatLngOnFirebase() {

    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            // Check if the permission was granted
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, request location updates
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0,
                    0f,
                    locationListener
                )
            } else {
                requestLocationPermission()
            }
        }
    }

    @SuppressLint("MissingInflatedId")
    fun getAddressFromLocation(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        val result = ""

        try {
            val addresses: MutableList<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    val address = addresses[0]

                    val showAddressDialog =
                        layoutInflater.inflate(R.layout.show_address_dialog, null)
                    val nametxt: TextView = showAddressDialog.findViewById(R.id.nametxt)
                    val subAdmin: TextView = showAddressDialog.findViewById(R.id.subAdminArea)
                    val admin: TextView = showAddressDialog.findViewById(R.id.adminArea)
                    val subLocality: TextView = showAddressDialog.findViewById(R.id.subLocalityArea)
                    val locality: TextView = showAddressDialog.findViewById(R.id.localityArea)
                    val cancelButton: Button = showAddressDialog.findViewById(R.id.cancelButton)
                    val okButton: Button = showAddressDialog.findViewById(R.id.okButton)

                    nametxt.text = receiverName
                    subAdmin.text = address.subAdminArea
                    admin.text = address.adminArea
                    subLocality.text = address.subLocality
                    locality.text = address.locality

                    val locationDialog = AlertDialog.Builder(this@MainActivity)
                        .setView(showAddressDialog)
                        .create()

                    locationDialog.show()

                    cancelButton.setOnClickListener {
                        locationDialog.dismiss()
                    }
                    okButton.setOnClickListener {

                        val intent = Intent(this, MapActivity::class.java)
                        intent.putExtra("latitudeToMap", receiverLatitude)
                        intent.putExtra("longitudeToMap", receiverLongitude)
                        startActivity(intent)
                        locationDialog.dismiss()
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return result
    }

    private fun signOut() {
        firebaseAuth.signOut()

        // If you are using Google Sign-In, you should also revoke access to the Google account
        val googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN)
        googleSignInClient.revokeAccess().addOnCompleteListener {
            // After revoking Google access, you can redirect the user to the login screen
            redirectToLoginScreen()
        }
    }

    private fun redirectToLoginScreen() {
        val intent = Intent(this, Authentication::class.java)
        startActivity(intent)
        finish()
    }

}
