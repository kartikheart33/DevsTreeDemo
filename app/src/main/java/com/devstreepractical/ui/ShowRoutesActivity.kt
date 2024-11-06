package com.devstreepractical.ui

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.devstreepractical.R
import com.devstreepractical.databinding.ActivityShowRoutesBinding
import com.devstreepractical.db.PlacesDataBase
import com.devstreepractical.ui.adapter.PlacesDataModel
import com.devstreepractical.util.DirectionsJSONParser
import com.devstreepractical.util.gone
import com.devstreepractical.util.setSafeOnClickListener
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class ShowRoutesActivity : AppCompatActivity() {

    lateinit var mBinding: ActivityShowRoutesBinding

    private var googleMap: GoogleMap? = null
    private val noteDatabase by lazy { PlacesDataBase.getDatabase(this).placesDao() }
    val savedPlacesList = arrayListOf<PlacesDataModel>()

    private var mPolyline: Polyline? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityShowRoutesBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        val mapFragment = supportFragmentManager.findFragmentById(
            R.id.map_fragment
        ) as? SupportMapFragment
        mapFragment?.getMapAsync { it ->
            googleMap = it
        }

        getDate()

        mBinding.imgBack.setSafeOnClickListener {
            onBackPressed()
        }

    }

    private fun getDate() {
        lifecycleScope.launch {
            noteDatabase.getNotes().collect {
                savedPlacesList.clear()
                savedPlacesList.addAll(it)
                setMarkerAndRoutes()
            }
        }
    }

    private fun setMarkerAndRoutes() {
        googleMap?.clear()
        val latLong: ArrayList<LatLng> = arrayListOf()
        savedPlacesList.map {
            val originLocation = LatLng(it.lat!!, it.lag!!)
            latLong.add(originLocation)
            googleMap?.addMarker(
                MarkerOptions()
                    .title(it.name)
                    .position(originLocation)
                    .visible(true)
            )?.showInfoWindow()
        }

        generateUrls(latLong)?.map {
            CoroutineScope(Dispatchers.Main).launch {
                val te = CoroutineScope(Dispatchers.IO).async {
                    downloadMapDataFromUrl(it)
                }.await()
                val data = CoroutineScope(Dispatchers.IO).async {
                    DirectionsJSONParser.parse(JSONObject(te!!))
                }
                temp(data.await())
            }

        }

    }

    private fun downloadMapDataFromUrl(strUrl: String): String? {
        var data = ""
        var iStream: InputStream? = null
        var urlConnection: HttpURLConnection? = null
        try {
            val url = URL(strUrl)

            urlConnection = url.openConnection() as HttpURLConnection

            urlConnection.connect()

            iStream = urlConnection.inputStream
            val br = BufferedReader(InputStreamReader(iStream))
            val sb = StringBuffer()
            var line: String? = ""
            while (br.readLine().also { line = it } != null) {
                sb.append(line)
            }
            data = sb.toString()
            br.close()
        } catch (e: Exception) {
            Log.d("Exception on download", e.toString())
        } finally {
            iStream?.close()
            urlConnection?.disconnect()
        }
        return data
    }

    private fun temp(result: List<List<HashMap<String, String>>>) {

        var points: ArrayList<LatLng?>?
        var lineOptions: PolylineOptions?

        for (i in result.indices) {
            points = ArrayList()
            lineOptions = PolylineOptions()

            val path: List<HashMap<String, String>> = result[i]

            for (j in path.indices) {
                val point = path[j]
                val lat = point["lat"]!!.toDouble()
                val lng = point["lng"]!!.toDouble()
                val position = LatLng(lat, lng)
                points.add(position)
            }

            lineOptions.addAll(points)
            lineOptions.width(8f)
            lineOptions.color(Color.RED)

            mPolyline = googleMap?.addPolyline(lineOptions)

        }

        val first = LatLng(savedPlacesList.last().lat!!, savedPlacesList.last().lag!!)
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(first, 15f))
        mBinding.llProgress.gone()

    }

    private fun generateUrls(markerPoints: ArrayList<LatLng>): List<String>? {
        val mUrls: MutableList<String> = ArrayList()
        if (markerPoints.size > 1) {
            var str_origin = markerPoints[0].latitude.toString() + "," + markerPoints[0].longitude
            var str_dest = markerPoints[1].latitude.toString() + "," + markerPoints[1].longitude
            val sensor = "sensor=false"
            var parameters = "origin=$str_origin&destination=$str_dest&$sensor"
            val output = "json"
            val key = "key=" + resources.getString(R.string.apikey)
            var url = "https://maps.googleapis.com/maps/api/directions/$output?$parameters&$key"
            mUrls.add(url)
            for (i in 2 until markerPoints.size) {
                str_origin = str_dest
                str_dest = markerPoints[i].latitude.toString() + "," + markerPoints[i].longitude
                parameters = "origin=$str_origin&destination=$str_dest&$sensor"
                url = "https://maps.googleapis.com/maps/api/directions/$output?$parameters&$key"
                mUrls.add(url)
            }
        }
        return mUrls
    }

}

