package com.varnittyagi.googlemaps.fragments

import android.annotation.SuppressLint
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

class MapsFragment() : Fragment() {
//private lateinit var mapsFragmentBtn:FragmentContainerView
    private  var mGoogleMap: GoogleMap?=null
    private var currentLatLng: LatLng? = null

    private val callback = OnMapReadyCallback { googleMap ->
        mGoogleMap = googleMap
        currentLatLng = LatLng(0.0,0.0)
        googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE

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
    @SuppressLint("SuspiciousIndentation")
    fun updateMap(newLatLng: LatLng) {

        currentLatLng = newLatLng
        mGoogleMap?.clear() // Clear previous markers if any
        mGoogleMap?.addMarker(MarkerOptions().position(newLatLng).title("Updated Location"))
        mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLng(newLatLng))
        mGoogleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(newLatLng,17f))

}
    }