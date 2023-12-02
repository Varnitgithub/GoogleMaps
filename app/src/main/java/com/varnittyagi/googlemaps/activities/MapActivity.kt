package com.varnittyagi.googlemaps.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.varnittyagi.googlemaps.R
import com.varnittyagi.googlemaps.fragments.MapsFragment
import com.varnittyagi.googlemaps.models.LatLngClass

class MapActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        val mapsFragment = MapsFragment(LatLngClass("29.306522","77.649698"))
        supportFragmentManager.beginTransaction().replace(R.id.mFrameLayout, mapsFragment).commit()
    }
}