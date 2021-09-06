package com.example.android.politicalpreparedness.election

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Division
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class VoterInfoViewModel(private val dataSource: ElectionDao) : ViewModel() {
    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    fun retrieveVoterInformation(electionId: Int, division: Division) {
            val address = "${division.country}  ${division.state}"
            viewModelScope.launch {
                val voterInfo = CivicsApi.retrofitService.getVoterInfo(electionId, address).await()
                _electionName.value = voterInfo.election.name
                _electionDate.value = voterInfo.election.electionDay.toString()
            }
    }

    //TODO: Add live data to hold voter info
    private val _electionName = MutableLiveData<String>()
    val electionName: LiveData<String>
        get() = _electionName

    private val _electionDate = MutableLiveData<String>()
    val electionDate: LiveData<String>
        get() = _electionDate

    init {

    }

    //TODO: Add var and methods to populate voter info

    //TODO: Add var and methods to support loading URLs

    //TODO: Add var and methods to save and remove elections to local database
    //TODO: cont'd -- Populate initial state of save button to reflect proper action based on election saved status

    /**
     * Hint: The saved state can be accomplished in multiple ways. It is directly related to how elections are saved/removed from the database.
     */

}