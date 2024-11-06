package com.devstreepractical.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.devstreepractical.databinding.ItemSavedPlacesBinding
import com.devstreepractical.util.OnSavedItemClick
import com.devstreepractical.util.setSafeOnClickListener

class SavedPlacesAdapter(
    private val context: Context,
    private var mResultList: ArrayList<PlacesDataModel>?,
    private val listener: OnSavedItemClick<PlacesDataModel>
) : RecyclerView.Adapter<SavedPlacesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val layoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val convertView = ItemSavedPlacesBinding.inflate(layoutInflater)
        return ViewHolder(convertView)
    }

    override fun getItemCount(): Int {
        return mResultList!!.size
    }

    inner class ViewHolder(private val mBinding: ItemSavedPlacesBinding) :
        RecyclerView.ViewHolder(mBinding.root) {

        fun bind(item: PlacesDataModel, position: Int) {

            mBinding.tvArea.text = item.name
            mBinding.tvAddress.text = item.address

            mBinding.imgEdit.setSafeOnClickListener {
                listener.onEdit(item,position)
            }

            mBinding.imgDelete.setSafeOnClickListener {
                listener.onDelete(item, position)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mResultList!![position], position)
    }

}