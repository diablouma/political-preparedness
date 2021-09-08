package com.example.android.politicalpreparedness.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.android.politicalpreparedness.network.models.Election

@Dao
interface ElectionDao {

    //TODO: Add insert query
    @Insert
    suspend fun insert(election: Election)

    //TODO: Add select all election query
    @Query("SELECT * FROM election_table")
    fun getAll(): LiveData<List<Election>>

    //TODO: Add select single election query
    @Query("SELECT * FROM election_table WHERE id = :id")
    fun getById(id: Int): LiveData<Election>

    @Query("DELETE FROM election_table WHERE id = :id")
    suspend fun delete(id: Int)

    //TODO: Add delete query

    //TODO: Add clear query

}