package com.example.android.politicalpreparedness.election

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Division
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.State
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.util.*

class VoterInfoViewModel(private val dataSource: ElectionDao) : ViewModel() {
    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _electionName = MutableLiveData<String>()
    val electionName: LiveData<String>
        get() = _electionName

    private val _electionDate = MutableLiveData<Date>()
    val electionDate: LiveData<Date>
        get() = _electionDate

    private val _votingLocationsUrl = MutableLiveData<String>()
    val votingLocationsUrl: LiveData<String>
        get() = _votingLocationsUrl

    private val _openVotingLocations = MutableLiveData<Boolean>()
    val openVotingLocations: LiveData<Boolean>
        get() = _openVotingLocations

    private val _ballotInformationUrl = MutableLiveData<String>()
    val ballotInformationUrl: LiveData<String>
        get() = _ballotInformationUrl

    private val _openBallotInformation = MutableLiveData<Boolean>()
    val openBallotInformation: LiveData<Boolean>
        get() = _openBallotInformation

    private val _electionSavedInDB = MutableLiveData<Election>()
    val electionSavedInDB: LiveData<Election>
        get() = _electionSavedInDB

    private val _electionId = MutableLiveData<Int>()
    private val _division = MutableLiveData<Division>()

    fun retrieveVoterInformation(electionId: Int, division: Division) {
        val address = "${division.country}  ${division.state}"
        viewModelScope.launch {
            initVoterInfo(electionId, address)
            _electionSavedInDB.value = dataSource.getById(electionId).value
        }
    }

    private suspend fun initVoterInfo(
        electionId: Int,
        address: String
    ) {
        val voterInfo = CivicsApi.retrofitService.getVoterInfo(electionId, address).await()
        _electionName.value = voterInfo.election.name
        _electionDate.value = voterInfo.election.electionDay
        _electionId.value = voterInfo.election.id
        _division.value = voterInfo.election.division

        if (!voterInfo.state.isNullOrEmpty()) {
            _votingLocationsUrl.value =
                voterInfo.state.first().electionAdministrationBody.votingLocationFinderUrl

            _ballotInformationUrl.value =
                voterInfo.state.first().electionAdministrationBody.ballotInfoUrl
        }
    }


    init {
        _votingLocationsUrl.value = null
        _ballotInformationUrl.value = null
    }

    fun onVotingLocationsClicked() {
        _openVotingLocations.value = true
    }

    fun onOpenVotingLocationsCompleted() {
        _openVotingLocations.value = false
    }

    fun onBallotInformationClicked() {
        _openBallotInformation.value = true
    }

    fun onOpenBallotInformationCompleted() {
        _openBallotInformation.value = false
    }

    fun followElection() {
        viewModelScope.launch {
            dataSource.insert(
                Election(
                    _electionId.value!!,
                    _electionName.value!!,
                    _electionDate.value!!,
                    _division.value!!
                )
            )
        }
    }

    //TODO: Add var and methods to populate voter info

    //TODO: Add var and methods to support loading URLs

    //TODO: Add var and methods to save and remove elections to local database
    //TODO: cont'd -- Populate initial state of save button to reflect proper action based on election saved status

    /**
     * Hint: The saved state can be accomplished in multiple ways. It is directly related to how elections are saved/removed from the database.
     */

}