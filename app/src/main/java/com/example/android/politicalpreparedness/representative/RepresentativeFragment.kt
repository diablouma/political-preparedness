package com.example.android.politicalpreparedness.representative

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.ResourceProvider
import com.example.android.politicalpreparedness.databinding.FragmentRepresentativeBinding
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.adapter.RepresentativeListAdapter
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.snackbar.Snackbar
import java.util.Locale

class DetailFragment : Fragment() {

    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private var lastKnownLocation: Location? = null
    private lateinit var binding: FragmentRepresentativeBinding

    companion object {
        //TODO: Add Constant for Location request
    }

    //TODO: Declare ViewModel
    private lateinit var representativeViewModel: RepresentativeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        representativeViewModel = RepresentativeViewModel(ResourceProvider(requireContext()))

        binding = FragmentRepresentativeBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.representativeViewModel = representativeViewModel

        //TODO: Establish bindings

        //TODO: Define and assign Representative adapter
        val manager = GridLayoutManager(activity, 1)
        binding.representativesList.layoutManager = manager

        val representativeListAdapter = RepresentativeListAdapter()
        binding.representativesList.adapter = representativeListAdapter

        representativeViewModel.representatives.observe(viewLifecycleOwner, Observer {
            representativeListAdapter.submitList(it)
        })

        locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        binding.buttonLocation.setOnClickListener {
            checkLocationPermissions()
            hideKeyboard()
        }

        binding.buttonSearch.setOnClickListener {
            hideKeyboard()
        }

        representativeViewModel.geoCodedLocation.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                representativeViewModel.findRepresentativesByGeocodedAddress()
                representativeViewModel.geoCodedLocation.value = null
            }
        })

        representativeViewModel.representativesFound.observe(
            viewLifecycleOwner,
            Observer { representativesFound ->
                if (!representativesFound) {
                    Toast.makeText(
                        requireContext(),
                        R.string.representatives_not_found,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                hideKeyboard()
            })

        return binding.root
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                getUserCurrentAddress()
            }
        } else {
            Snackbar.make(
                binding.representativeFragment,
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.settings) {
                    startActivityForResult(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", requireContext().packageName, null)
                    }, REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE)
                }.show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun checkLocationPermissions() {
        if (isPermissionGranted()) {
            if (lastKnownLocation == null) {
                lastKnownLocation =
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            }

            representativeViewModel.geoCodedLocation.value = geoCodeLocation(lastKnownLocation!!)
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE -> {
                Log.i(this.javaClass.simpleName, "after requesting foregroung permissions")
                getUserCurrentAddress()
            }
            REQUEST_TURN_DEVICE_LOCATION_ON -> {
                getUserCurrentAddress()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getUserCurrentAddress() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    startIntentSenderForResult(
                        exception.getResolution().getIntentSender(),
                        REQUEST_TURN_DEVICE_LOCATION_ON, null, 0, 0, 0, null
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(
                        this.javaClass.simpleName,
                        "Error getting location settings resolution: " + sendEx.message
                    )
                }
            }
        }

        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0,
                    0f,
                    locationListener
                )

                lastKnownLocation =
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (lastKnownLocation != null) {
                    representativeViewModel.geoCodedLocation.value =
                        geoCodeLocation(lastKnownLocation!!)
                }
            }

        }

        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0,
            0f,
            locationListener
        );
    }

    private fun geoCodeLocation(location: Location): Address {
        val geocoder = Geocoder(context, Locale.getDefault())
        return geocoder.getFromLocation(location.latitude, location.longitude, 1)
            .map { address ->
                Address(
                    address.thoroughfare,
                    address.subThoroughfare,
                    address.locality,
                    address.adminArea,
                    address.postalCode
                )
            }
            .first()
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view!!.windowToken, 0)
    }

}

private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29