package com.devstreepractical.ui

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.devstreepractical.databinding.ActivityMainBinding
import com.devstreepractical.db.PlacesDataBase
import com.devstreepractical.ui.adapter.PlacesDataModel
import com.devstreepractical.ui.adapter.SavedPlacesAdapter
import com.devstreepractical.util.Constant
import com.devstreepractical.util.gone
import com.devstreepractical.util.OnSavedItemClick
import com.devstreepractical.util.visible
import com.google.gson.Gson
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {

    lateinit var mBinding: ActivityMainBinding

    private val noteDatabase by lazy { PlacesDataBase.getDatabase(this).placesDao() }
    val savedPlacesList = arrayListOf<PlacesDataModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setAllClickListener()

        getSavedDataAndSet()

    }

    private fun setAllClickListener() {

        mBinding.btnAddPOI.setOnClickListener {
            val intent = Intent(this, AddPoiActivity::class.java)
            resultLauncher.launch(intent)
        }

        mBinding.btnAddPOIOne.setOnClickListener {
            val intent = Intent(this, AddPoiActivity::class.java)
            resultLauncher.launch(intent)
        }

        mBinding.btnRoute.setOnClickListener {
            startActivity(Intent(this, ShowRoutesActivity::class.java))
        }

    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                getSavedDataAndSet()
            }
        }

    private fun getSavedDataAndSet() {

        lifecycleScope.launch {
            noteDatabase.getNotes().collect {
                if (it.isNotEmpty()) {
                    savedPlacesList.clear()
                    savedPlacesList.addAll(it)
                    mBinding.llEmpty.gone()
                    mBinding.llSavedData.visible()
                } else {
                    mBinding.llEmpty.visible()
                    mBinding.llSavedData.gone()
                }
                setSaveList()
            }
        }

    }

    private fun setSaveList() {

        mBinding.rvSaved.apply {
            adapter = SavedPlacesAdapter(
                this@DashboardActivity,
                savedPlacesList,
                object : OnSavedItemClick<PlacesDataModel> {
                    override fun onTap(model: PlacesDataModel, position: Int) {

                    }

                    override fun onDelete(model: PlacesDataModel, position: Int) {
                        deleteSavedPlace(position, model)
                    }

                    override fun onEdit(model: PlacesDataModel, position: Int) {
                        val intent = Intent(this@DashboardActivity, AddPoiActivity::class.java)
                        intent.putExtra(Constant.passEdit,true)
                        intent.putExtra(Constant.passPlace,Gson().toJson(model))
                        resultLauncher.launch(intent)
                    }

                })
        }

    }

    private fun deleteSavedPlace(position: Int, model: PlacesDataModel) {
        lifecycleScope.launch {
            noteDatabase.deleteNote(model)
        }
        savedPlacesList.remove(model)
        mBinding.rvSaved.adapter?.notifyItemRemoved(position)
        if (savedPlacesList.isNotEmpty()) {
            mBinding.llEmpty.gone()
            mBinding.llSavedData.visible()
        } else {
            mBinding.llEmpty.visible()
            mBinding.llSavedData.gone()
        }
    }

}