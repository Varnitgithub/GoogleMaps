package com.varnittyagi.googlemaps.activities

import MessageAdapter
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.varnittyagi.googlemaps.R
import com.varnittyagi.googlemaps.databinding.MainActivityBinding
import com.varnittyagi.googlemaps.fragments.MapsFragment
import com.varnittyagi.googlemaps.models.LatLngClass
import com.varnittyagi.googlemaps.models.LocationModel
import com.varnittyagi.googlemaps.models.Messages
import com.varnittyagi.googlemaps.models.UserDetails

class MainActivity : AppCompatActivity() {
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var binding: MainActivityBinding
    private lateinit var currentUserUid: String
    private lateinit var receiverUserUid: String
    private lateinit var messageList: ArrayList<Messages>
    private var receiverName: String? = null
    private var receiverPhoto: String? = null
    private var userName: String? = null
    private val REQUEST_LOCATION_PERMISSION = 1
    private var latitude: String? = null
    private var longitude: String? = null
 private var receiverLatitude: String? = null
    private var receiverLongitude: String? = null

    private lateinit var locationManager: LocationManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.main_activity)
        var frameLayout = findViewById<FrameLayout>(R.id.frameLayout)


        firebaseAuth = FirebaseAuth.getInstance()
        currentUserUid = firebaseAuth.currentUser?.uid!!
        databaseReference = FirebaseDatabase.getInstance().reference
        messageAdapter = MessageAdapter(this)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        binding.chatRecyclerview.layoutManager = LinearLayoutManager(this)
        messageList = arrayListOf()
        userName = intent.getStringExtra("userName")
        binding.chatRecyclerview.adapter = messageAdapter
        //binding.chatRecyclerview.smoothScrollToPosition(messageAdapter.itemCount-1)
        fetchLocation()
        getLatLng()

        val mapsFragment = MapsFragment(LatLngClass(latitude?:"29.306522",longitude?:"77.649698"))
        supportFragmentManager.beginTransaction().replace(R.id.frameLayout, mapsFragment).commit()

        binding.expandMap.setOnClickListener {
            Log.d("TAGGGGGG", "onCreate: click")
            startActivity(Intent(this, MapActivity::class.java))
        }

        val user = intent.getParcelableExtra<UserDetails>("user")

        if (user != null && firebaseAuth.currentUser?.uid != null) {
            Log.d("TAGGGGGGGGG", "onCreate: $user is the user")

            currentUserUid = firebaseAuth.currentUser?.uid.toString()
            receiverUserUid = user.uid.toString()
            receiverName = user.name
            receiverPhoto = user.profile

        }
        setReceiverDashBoard()

        binding.btnRequestLocation.setOnClickListener {
            Toast.makeText(this, "write a message to request location", Toast.LENGTH_SHORT).show()
            if (binding.userMessage.text != null) {
                databaseReference.child("location").child(receiverUserUid).setValue(
                    LocationModel(
                        userName ?: "User",
                        binding.userMessage.text.toString()
                    )
                )

                Toast.makeText(
                    this@MainActivity,
                    "$latitude is the latitude and $longitude is the longitude",
                    Toast.LENGTH_SHORT
                )
                    .show()
                Log.d(
                    "TAGGGGGGGG",
                    "onDataChange: $latitude is the latitude and $longitude is the longitude"
                )

            } else {
                Toast.makeText(this, "write a message to request location", Toast.LENGTH_SHORT)
                    .show()
            }


        }

        binding.sendBtn.setOnClickListener {
            sendMessageOnFirebase()
            binding.userMessage.text.clear()
        }
        readMessageFromFirebase()
        detectLocation()

    }

    private fun sendMessageOnFirebase() {
        databaseReference.child("messages").push().setValue(
            Messages(
                binding.userMessage.text.toString().trim(),
                currentUserUid,
                receiverUserUid
            )
        )
    }

    private fun readMessageFromFirebase() {

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                messageList.clear()

                for (messageSnapshot: DataSnapshot in dataSnapshot.children) {
                    val message = messageSnapshot.getValue(Messages::class.java)

                    if (message?.senderId == currentUserUid && message.recieverId == receiverUserUid ||
                        message?.senderId == receiverUserUid && message.recieverId == currentUserUid
                    ) {
                        messageList.add(message)
                    }
                }
                messageAdapter.updateList(messageList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors, if any
                println("Error: ${databaseError.message}")
            }
        }

        // Attach the ValueEventListener to the database reference
        databaseReference.child("messages").addValueEventListener(valueEventListener)
    }

    private fun detectLocation() {
        databaseReference.child("location").child(currentUserUid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val location = snapshot.getValue<LocationModel>()
                    Log.d(
                        "TAGGGGGGG",
                        "onDataChange: ${location?.message} is the name ${location?.name} is the message"
                    )


                    val dialogView = layoutInflater.inflate(R.layout.location_dialog, null)
                    val nameTextView: TextView = dialogView.findViewById(R.id.nameTextView)
                    val messageTextView: TextView = dialogView.findViewById(R.id.messageTextView)
                    val cancelButton: Button = dialogView.findViewById(R.id.cancelButton)
                    val sendButton: Button = dialogView.findViewById(R.id.sendButton)

                    nameTextView.text = location?.name
                    messageTextView.text = location?.message


                    val alertDialog = AlertDialog.Builder(this@MainActivity)
                        .setView(dialogView)
                        .create()

                    alertDialog.show()

                    cancelButton.setOnClickListener {
                        alertDialog.dismiss()
                    }
                    sendButton.setOnClickListener {
                        databaseReference.child("LocationDetails").child(receiverUserUid)
                            .setValue(LatLngClass(latitude ?: "", longitude ?: ""))
                        alertDialog.dismiss()
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun setReceiverDashBoard() {
        binding.receiverUserName.text = receiverName
        Log.d("TAGGGGGGG", "setReceiverDashBoard: $receiverPhoto")
       Glide.with(this).load(Uri.parse(receiverPhoto)).placeholder(R.drawable.userpng).into(binding.receiverUserPhoto)
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            latitude = location.latitude.toString()
            longitude = location.longitude.toString()
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
                        Log.d(
                            "TAGGGGGGGGGGGG",
                            "onDataChange: ${fetchedLocation.latitude} is the latitud" +
                                    "e \n ${fetchedLocation.longitude} is the longitude"
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

}
