package com.example.android.politicalpreparedness.election

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.databinding.FragmentElectionBinding
import com.example.android.politicalpreparedness.election.adapter.ElectionListAdapter
import com.example.android.politicalpreparedness.election.adapter.ElectionListener

class ElectionsFragment : Fragment() {

    //TODO: Declare ViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //TODO: Add ViewModel values and create ViewModel

        //TODO: Add binding values
        val binding: FragmentElectionBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_election, container, false)

        val application = requireNotNull(this.activity).application

        val dataSource = ElectionDatabase.getInstance(application).electionDao

        val viewModelFactory = ElectionsViewModelFactory(dataSource, application)

        val electionsViewModel =
            ViewModelProvider(
                this, viewModelFactory
            ).get(ElectionsViewModel::class.java)

        binding.electionsViewModel = electionsViewModel

        electionsViewModel.retrieveSavedElections()

        //TODO: Link elections to voter info

        //TODO: Initiate recycler adapters
        val manager = GridLayoutManager(activity, 1)
        binding.electionsList.layoutManager = manager

        val upcomingElectionsAdapter = ElectionListAdapter(ElectionListener { selectedElection ->
            electionsViewModel.onUpcomingElectionClicked(selectedElection)
        })

        binding.electionsList.adapter = upcomingElectionsAdapter

        electionsViewModel.upcomingElections.observe(viewLifecycleOwner, Observer {
            upcomingElectionsAdapter.submitList(it)
        })

        val savedElectionsAdapter = ElectionListAdapter(ElectionListener { selectedElection ->
//            electionsViewModel.onUpcomingElectionClicked(selectedElection)
        })

        val managerForSavedElections = GridLayoutManager(activity, 1)
        binding.savedElectionsList.layoutManager = managerForSavedElections
        binding.savedElectionsList.adapter = savedElectionsAdapter

        electionsViewModel.savedElections.observe(viewLifecycleOwner, Observer {
            Log.i("PEPE", "SavedElections" + (it!=null).toString())
            savedElectionsAdapter.submitList(it)
        })

        electionsViewModel.navigateToSelectedElectionScreen.observe(viewLifecycleOwner, Observer { selectedElection ->
            if (selectedElection != null) {
                findNavController().navigate(ElectionsFragmentDirections.actionElectionsFragmentToVoterInfoFragment(selectedElection.id, selectedElection.division));
                electionsViewModel.onNavigateToSelectedElectionScreenCompleted()
            }
        })

        binding.lifecycleOwner = this

        //TODO: Populate recycler adapters

        return binding.root
    }

    //TODO: Refresh adapters when fragment loads

}