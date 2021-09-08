package com.example.android.politicalpreparedness.representative

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RepresentativeViewModel : ViewModel() {

    //TODO: Establish live data for representatives and address
    val _addressLine1 = MutableLiveData<String>()
    val addressLine1: LiveData<String>
        get() = _addressLine1

    val _addressLine2 = MutableLiveData<String>()
    val addressLine2: LiveData<String>
        get() = _addressLine2

    val _city = MutableLiveData<String>()
    val city: LiveData<String>
        get() = _city

    val _state = MutableLiveData<String>()
    val state: LiveData<String>
        get() = _state

    //TODO: Create function to fetch representatives from API from a provided address

    /**
     *  The following code will prove helpful in constructing a representative from the API. This code combines the two nodes of the RepresentativeResponse into a single official :

    val (offices, officials) = getRepresentativesDeferred.await()
    _representatives.value = offices.flatMap { office -> office.getRepresentatives(officials) }

    Note: getRepresentatives in the above code represents the method used to fetch data from the API
    Note: _representatives in the above code represents the established mutable live data housing representatives

     */

    //TODO: Create function get address from geo location

    //TODO: Create function to get address from individual fields

}
