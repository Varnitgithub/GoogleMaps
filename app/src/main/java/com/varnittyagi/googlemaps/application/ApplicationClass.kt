package com.varnittyagi.googlemaps.application

import android.app.Application
import com.pusher.pushnotifications.PushNotifications

class ApplicationClass :Application() {

    override fun onCreate() {
        super.onCreate()


//        try {
//            PushNotifications.start(applicationContext,"9aba7710-be9f-468c-beaa-5fec31eebfc5")
//            PushNotifications.addDeviceInterest("hello")
//        } catch (e: Exception) {
//            // Log or print the exception
//            e.printStackTrace()
//        }
    }
}