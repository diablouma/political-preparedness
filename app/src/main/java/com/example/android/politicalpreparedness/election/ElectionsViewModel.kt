package com.example.android.politicalpreparedness.election

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Election
import kotlinx.coroutines.*
import java.net.InetAddress

//TODO: Construct ViewModel and provide election datasource
class ElectionsViewModel(
    val database: ElectionDao,
    application: Application
) : AndroidViewModel(application) {
    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    //TODO: Create live data val for upcoming elections
    private val _upcomingElections = MutableLiveData<List<Election>>()
    val upcomingElections: LiveData<List<Election>>
        get() = _upcomingElections

    private val _savedElections = MutableLiveData<List<Election>>()
    val savedElections: LiveData<List<Election>>
        get() = _savedElections

    private val _navigateToSelectedElectionScreen = MutableLiveData<Election>()
    val navigateToSelectedElectionScreen: LiveData<Election>
        get() = _navigateToSelectedElectionScreen

    init {
        viewModelScope.launch {
            if (isInternetAvailable()) {
                val electionsFromInternet =
                    CivicsApi.retrofitService.getElections()
                        .await()
                        .elections.filter {
                            it.division.state != null && it.division.country != null && it.division.state.isNotEmpty() && it.division.country.isNotEmpty()
                        }

                _upcomingElections.value = electionsFromInternet
            } else {
                Log.i(
                    "MainViewModel",
                    "No Internet connection available, not loading picture of day"
                )
            }

            _savedElections.value = database.getAll().value
        }
    }

    private suspend fun isInternetAvailable(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val ipAddr: InetAddress = InetAddress.getByName("www.google.com")
                return@withContext !ipAddr.equals("")
            } catch (e: Exception) {
                return@withContext false
            }
        }
    }

    fun onUpcomingElectionClicked(selectedElection: Election) {
        _navigateToSelectedElectionScreen.value = selectedElection
    }

    fun onNavigateToSelectedElectionScreenCompleted() {
        _navigateToSelectedElectionScreen.value = null
    }

    //TODO: Create live data val for saved elections

    //TODO: Create val and functions to populate live data for upcoming elections from the API and saved elections from local database

    //TODO: Create functions to navigate to saved or upcoming election voter info

}