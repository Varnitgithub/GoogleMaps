package com.varnittyagi.googlemaps.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

data class UserDetails(
    var email: String? = null,
    var name: String? = null,
    var profile: String? = null,
    var uid: String? = null,
):Serializable

