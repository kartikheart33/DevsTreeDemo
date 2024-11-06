package com.devstreepractical.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.devstreepractical.R
import com.devstreepractical.databinding.ActivityAddPoiBinding
import com.devstreepractical.db.PlacesDataBase
import com.devstreepractical.ui.adapter.PlacesDataModel
import com.devstreepractical.ui.adapter.SearchLocationAdapter
import com.devstreepractical.util.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.gson.Gson
import kotlinx.coroutines.launch

class AddPoiActivity : AppCompatActivity() {

    lateinit var mBinding: ActivityAddPoiBinding

    private var mAutoCompleteAdapter: SearchLocationAdapter? = null

    private var googleMap: GoogleMap? = null
    private var selectedPlace: Place? = null

    private val noteDatabase by lazy { PlacesDataBase.getDatabase(this).placesDao() }

    private var isEdit = false
    private var oldModel: PlacesDataModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityAddPoiBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        isEdit = intent.getBooleanExtra(Constant.passEdit, false)

        setAllClickListener()

        setPlacesAdapter()

    }

    private fun setAllClickListener() {

        mBinding.imgBack.setOnClickListener {
            onBackPressed()
        }

        mBinding.edtSearch.addTextChangedListener(filterTextWatcher)

        val mapFragment = supportFragmentManager.findFragmentById(
            R.id.map_fragment
        ) as? SupportMapFragment
        mapFragment?.getMapAsync { it ->
            googleMap = it
        }

        mBinding.btnSave.setSafeOnClickListener {
            selectedPlace?.let {

                val model = if (isEdit) {
                    oldModel?.apply {
                        this.name = it.name
                        this.address = it.address
                        this.lat = it.latLng?.latitude
                        this.lag = it.latLng?.longitude
                    }
                } else {
                    PlacesDataModel(
                        name = it.name,
                        address = it.address,
                        lat = it.latLng?.latitude,
                        lag = it.latLng?.longitude
                    )
                }

                lifecycleScope.launch {
                    if (isEdit) {
                        noteDatabase.updateNote(model!!)
                    } else {
                        noteDatabase.addNote(model!!)
                    }
                }.invokeOnCompletion {
                    setResult(RESULT_OK)
                    onBackPressed()
                }
            }
        }

        if (isEdit) {
            mBinding.btnSave.text = getString(R.string.update)
            oldModel = Gson().fromJson(
                intent.getStringExtra(Constant.passPlace),
                PlacesDataModel::class.java
            )
        }

    }

    private fun setPlacesAdapter() {

        Places.initialize(this, resources.getString(R.string.apikey))

        mAutoCompleteAdapter = SearchLocationAdapter(this, onLocationClick)
        mBinding.rvLocationList.apply {
            adapter = mAutoCompleteAdapter
            mAutoCompleteAdapter?.notifyDataSetChanged()
        }

    }


    private val filterTextWatcher: TextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
            if (s.toString() != "") {
                mAutoCompleteAdapter!!.filter.filter(s.toString())
                if (mBinding.llResult.isgGone()) {
                    mBinding.llResult.visible()
                }
            } else {
                if (mBinding.llResult.isVisible()) {
                    mBinding.llResult.gone()
                }
            }
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    }

    private val onLocationClick = object : OnItemClick<Place> {
        override fun onItemClickListener(place: Place) {
            selectedPlace = place
            mBinding.edtSearch.setText("")
            mBinding.edtSearch.hideKeyboard()
            googleMap?.clear()
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(place.latLng!!, 15f))
            googleMap?.setInfoWindowAdapter(CustomMarker(this@AddPoiActivity))
            val marker = googleMap?.addMarker(
                MarkerOptions()
                    .title(place.name)
                    .position(place.latLng!!)
                    .visible(true)
            )
            marker?.tag = place
            marker?.showInfoWindow()
            mBinding.llWantAdd.visible()
        }
    }

}