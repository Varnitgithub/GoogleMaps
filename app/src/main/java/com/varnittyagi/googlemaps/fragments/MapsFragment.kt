package com.varnittyagi.googlemaps.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.varnittyagi.googlemaps.R
import com.varnittyagi.googlemaps.models.LatLngClass

class MapsFragment(private var latlong:LatLngClass) : Fragment() {
private lateinit var mapsFragmentBtn:FragmentContainerView
    private lateinit var mGoogleMap: GoogleMap

    private val callback = OnMapReadyCallback { googleMap ->
        mGoogleMap = googleMap
        googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE

        val sydney = LatLng(latlong.latitude?.toDouble()!!,latlong.longitude?.toDouble()!!)
        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 16f))


        val userLocation = LatLng(29.274673, 77.735602)


    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }
}