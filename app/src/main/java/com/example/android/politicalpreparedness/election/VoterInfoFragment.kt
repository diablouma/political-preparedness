package com.example.android.politicalpreparedness.election

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.databinding.FragmentVoterInfoBinding


class VoterInfoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //TODO: Add ViewModel values and create ViewModel

        //TODO: Add binding values
        val binding: FragmentVoterInfoBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_voter_info, container, false)
        binding.lifecycleOwner = this

        //TODO: Populate voter info -- hide views without provided data.
        /**
        Hint: You will need to ensure proper data is provided from previous fragment.
         */
        val electionId = VoterInfoFragmentArgs.fromBundle(requireArguments()).argElectionId
        val division = VoterInfoFragmentArgs.fromBundle(requireArguments()).argDivision

        val application = requireNotNull(this.activity).application
        val dataSource = ElectionDatabase.getInstance(application).electionDao
        val viewModelFactory = VoterInfoViewModelFactory(dataSource)

        val voterInfoViewModel =
            ViewModelProvider(
                this, viewModelFactory
            ).get(VoterInfoViewModel::class.java)

        binding.voterInfoViewModel = voterInfoViewModel

        voterInfoViewModel.retrieveVoterInformation(electionId, division)
        voterInfoViewModel.retrieveElectionFromDB(electionId)

        voterInfoViewModel.openVotingLocations.observe(
            viewLifecycleOwner,
            Observer { openVotingLocations ->
                if (openVotingLocations) {
                    val uri: Uri = Uri.parse(voterInfoViewModel.votingLocationsUrl.value)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                    voterInfoViewModel.onOpenVotingLocationsCompleted()
                }
            })

        voterInfoViewModel.openBallotInformation.observe(
            viewLifecycleOwner,
            Observer { openBallotInformation ->
                if (openBallotInformation) {
                    val uri: Uri = Uri.parse(voterInfoViewModel.ballotInformationUrl.value)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                    voterInfoViewModel.onOpenBallotInformationCompleted()
                }
            })

        voterInfoViewModel.electionSavedInDB.observe(viewLifecycleOwner, Observer { savedElection ->
                if(savedElection != null) {
                    Log.i(this.javaClass.simpleName, savedElection.id.toString())
                }

        })

        //TODO: Handle loading of URLs

        //TODO: Handle save button UI state
        //TODO: cont'd Handle save button clicks
        return binding.root
    }

    //TODO: Create method to load URL intents

}