package com.example.android.politicalpreparedness.representative

import android.util.Log
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.ResourceProvider
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Official
import com.example.android.politicalpreparedness.representative.model.Representative
import kotlinx.coroutines.*
import java.net.InetAddress

class RepresentativeViewModel(val resourceProvider: ResourceProvider) : BaseObservable() {
    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)


    private var selectedStatePosition: Int = -1
    private var addressLine1: String = ""
    private var addressLine2: String = ""
    private var city: String = ""
    private var stateValue: String = ""
    private var zip: String = ""

    @Bindable
    fun getSelectedStatePosition(): Int {
        return selectedStatePosition
    }

    fun setSelectedStatePosition(selectedStatePosition: Int) {
        if (!this.selectedStatePosition.equals(selectedStatePosition)) {
            this.selectedStatePosition = selectedStatePosition
            notifyPropertyChanged(BR.selectedStatePosition)
        }
    }

    @Bindable
    fun getAddressLine1(): String {
        return addressLine1
    }

    fun setAddressLine1(addressLine1: String) {
        if (!this.addressLine1.equals(addressLine1)) {
            this.addressLine1 = addressLine1
            notifyPropertyChanged(BR.addressLine1)
        }
    }

    @Bindable
    fun getAddressLine2(): String {
        return addressLine2
    }

    fun setAddressLine2(addressLine2: String) {
        if (!this.addressLine2.equals(addressLine2)) {
            this.addressLine2 = addressLine2
            notifyPropertyChanged(BR.addressLine2)
        }
    }


    @Bindable
    fun getCity(): String {
        return city
    }

    fun setCity(city: String) {
        if (!this.city.equals(city)) {
            this.city = city
            notifyPropertyChanged(BR.city)
        }
    }

    @Bindable
    fun getStateValue(): String {
        return stateValue
    }

    fun setStateValue(stateValue: String) {
        if (!this.stateValue.equals(stateValue)) {
            this.stateValue = stateValue
            notifyPropertyChanged(BR.stateValue)
        }
    }

    @Bindable
    fun getZip(): String {
        return zip
    }

    fun setZip(zip: String) {
        if (!this.zip.equals(zip)) {
            this.zip = zip
            notifyPropertyChanged(BR.zip)
        }
    }

    val _representatives = MutableLiveData<List<Official>>()
    val representatives: LiveData<List<Official>>
        get() = _representatives


    fun findRepresentativesByAddress() {
        viewModelScope.launch {
            if (isInternetAvailable()) {
                _representatives.value =
                    CivicsApi.retrofitService.getRepresentatives(joinAddress())
                        .await().officials
            } else {
                Log.i(
                    this.javaClass.simpleName,
                    "No Internet connection available, not loading representatives"
                )
            }
        }
    }

    private fun getSelectedState(): String {
        val states = resourceProvider.getStringArray(R.array.states)
        return if (selectedStatePosition != -1) states[selectedStatePosition] else ""
    }

    private fun joinAddress() = "$addressLine1 $addressLine2 $city ${getSelectedState()} $zip"

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

    /**
     *  The following code will prove helpful in constructing a representative from the API. This code combines the two nodes of the RepresentativeResponse into a single official :

    val (offices, officials) = getRepresentativesDeferred.await()
    _representatives.value = offices.flatMap { office -> office.getRepresentatives(officials) }

    Note: getRepresentatives in the above code represents the method used to fetch data from the API
    Note: _representatives in the above code represents the established mutable live data housing representatives

     */

    //TODO: Create function get address from geo location

}
