package com.devstreepractical.ui.adapter

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "places")
class PlacesDataModel(
    @PrimaryKey
    @ColumnInfo(name = "id")
    var id: Long? = null,
    @ColumnInfo(name = "name")
    var name: String? = null,
    @ColumnInfo(name = "address")
    var address: String? = null,
    @ColumnInfo(name = "lat")
    var lat: Double? = null,
    @ColumnInfo(name = "lag")
    var lag: Double? = null
)