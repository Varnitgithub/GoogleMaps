package com.varnittyagi.googlemaps.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LatLngClass(var latitude:String?= null,var longitude:String?=null):Parcelable
