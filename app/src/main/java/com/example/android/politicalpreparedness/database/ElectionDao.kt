package com.example.android.politicalpreparedness.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.android.politicalpreparedness.network.models.Election

@Dao
interface ElectionDao {

    //TODO: Add insert query
    @Insert
    fun insert(election: Election)

    //TODO: Add select all election query
    @Query("SELECT * FROM election_table")
    fun getAll(): LiveData<List<Election>>

    //TODO: Add select single election query
    @Query("SELECT * FROM election_table WHERE id = (:id)")
    fun getById(id: Int): LiveData<Election>

    //TODO: Add delete query

    //TODO: Add clear query

}