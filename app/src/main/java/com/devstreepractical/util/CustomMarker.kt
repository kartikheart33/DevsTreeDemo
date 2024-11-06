package com.devstreepractical.util

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.devstreepractical.databinding.MarkerInfoContentsBinding
import com.google.android.gms.location.places.Place
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class CustomMarker(
    private val context: Context
) : GoogleMap.InfoWindowAdapter {
    override fun getInfoContents(marker: Marker): View? {
        val place = marker.tag as? Place ?: return null
        val view = MarkerInfoContentsBinding.inflate(LayoutInflater.from(context))
        view.textViewAddress.text = place.address
        return view.root
    }

    override fun getInfoWindow(p0: Marker): View? {
        return null
    }
}