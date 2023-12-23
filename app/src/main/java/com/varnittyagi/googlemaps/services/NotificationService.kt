package com.varnittyagi.googlemaps.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.varnittyagi.googlemaps.R
import com.varnittyagi.googlemaps.activities.MainActivity
import com.varnittyagi.googlemaps.models.Messages


class NotificationService : Service() {
    private val notificationId = 1
    private val CHANNEL_ID = "1"
    private var receiverUid:String?=null
    private var receiverName:String?=null
    private var receiverUserId:String?=null
    private var senderUserId:String?=null
    private var messageList: String? = null
    private var firebaseAuth:FirebaseAuth = FirebaseAuth.getInstance()
    private var databaseReference = FirebaseDatabase.getInstance().reference
    var list:ArrayList<String>?=null
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }


    override fun onCreate() {
        super.onCreate()
    }
    @SuppressLint("SuspiciousIndentation")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
           receiverName = intent.getStringExtra("receiverName")
           receiverUserId = intent.getStringExtra("receiveruserid")

            if (firebaseAuth.currentUser?.uid!=senderUserId){
                readMessageFromFirebase()
            }
        }
        return START_STICKY
    }


    private fun createNotification(userMessage: ArrayList<String>, userName: String) {

        createNotificationChannel()
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.userpng)//.setLargeIcon(recevierPhoto)
            .setContentTitle(userName)
            .setContentText(userMessage.toString())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // Set the intent that fires when the user taps the notification.
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        Log.d("TAGGGGGGG", "createNotification: notification done")

        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define.
            if (ActivityCompat.checkSelfPermission(
                    this@NotificationService,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(notificationId, builder.build())
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channelName)
            val descriptionText = getString(R.string.channelDescription)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system.
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun readMessageFromFirebase() {

val currentUserUid = firebaseAuth.currentUser?.uid
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("TAGGGGG", "onDataChange: message got")

                list?.clear()
                for (messageSnapshot: DataSnapshot in dataSnapshot.children) {
                    val message = messageSnapshot.getValue(Messages::class.java)

                    Log.d("TAGGGGG", "onDataChange: senderid ${message?.senderId}")
                    Log.d("TAGGGGG", "onDataChange: receiverid ${message?.recieverId}")
                    Log.d("TAGGGGG", "onDataChange: currentreceiverid ${receiverUserId}")
                    Log.d("TAGGGGG", "onDataChange: currentuserid ${currentUserUid}")



                    if (message?.senderId == currentUserUid && message?.recieverId == receiverUserId ||
                        message?.senderId == receiverUserId && message?.recieverId == currentUserUid
                    ) {
                        list?.add(message?.message!!)

                        Log.d("TAGGGGGGGG", "onDataChange: current user uid : ${firebaseAuth.currentUser?.uid}")
                        Log.d("TAGGGGGGGG", "onDataChange:senderuserid :  ${senderUserId}")
                        if (firebaseAuth.currentUser?.uid!=message?.senderId){
                            receiverName?.let { list?.let { it1 -> createNotification(it1, it) } }

                        }
                        Log.d("TAGGGGG", "onDataChange: message read")


                    }else{
                        Log.d("TAGGGGG", "onDataChange:failed to message read")

                    }
                }

            }
            override fun onCancelled(databaseError: DatabaseError) {
                println("Error: ${databaseError.message}")
            }
        }
        databaseReference.child("messages").addValueEventListener(valueEventListener)
    }


}