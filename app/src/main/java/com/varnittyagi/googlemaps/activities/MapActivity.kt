package com.varnittyagi.googlemaps.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.model.LatLng
import com.varnittyagi.googlemaps.R
import com.varnittyagi.googlemaps.fragments.MapsFragment
import com.varnittyagi.googlemaps.models.LatLngClass

class MapActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val latitude = intent.getStringExtra("latitudeToMap")
        val longitude = intent.getStringExtra("longitudeToMap")
//        val mapsFragment = MapsFragment(LatLngClass(latitude,longitude))
//        supportFragmentManager.beginTransaction().replace(R.id.mFrameLayout, mapsFragment).commit()

        val mapsFragment =
            (supportFragmentManager.findFragmentByTag("MAPS_FRAGMENT_TAG") as MapsFragment?)

        if (mapsFragment != null && mapsFragment.isAdded) {
            // If the fragment is added, update the map
            mapsFragment.updateMap(
                LatLng(
                    latitude?.toDouble()?:27.00,
                    longitude?.toDouble()?:70.00
                )
            )
        } else {
            // If the fragment is not added, add it for the first time
            val newMapsFragment = MapsFragment()
            newMapsFragment.arguments = Bundle().apply {
                putParcelable(
                    "LATLNG",
                    LatLng(
                        latitude?.toDouble()?:27.00,
                        longitude?.toDouble()?:70.00
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
    }
}