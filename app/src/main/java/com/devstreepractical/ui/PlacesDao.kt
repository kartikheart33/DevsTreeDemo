package com.devstreepractical.ui

import androidx.room.*
import com.devstreepractical.ui.adapter.PlacesDataModel
import kotlinx.coroutines.flow.Flow

@Dao
interface PlacesDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addNote(note: PlacesDataModel)

    @Query("SELECT * FROM places ORDER by id")
    fun getNotes(): Flow<List<PlacesDataModel>>

    @Update
    suspend fun updateNote(note: PlacesDataModel)

    @Delete
    suspend fun deleteNote(note: PlacesDataModel)

}