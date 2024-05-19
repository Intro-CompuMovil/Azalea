package com.example.azalea.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.azalea.activities.CancelarActivity
import com.example.azalea.activities.ConfigurarMensajeActivity
import com.example.azalea.data.PermissionsCodes
import com.example.azalea.databinding.FragmentPanicoBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PanicoFragment : Fragment() {
    private var _binding: FragmentPanicoBinding? = null
    private val binding get() = _binding
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mLocationRequest: LocationRequest? = null
    private var mLocationCallback: LocationCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPanicoBinding.inflate(inflater, container, false)
        return binding?.root ?: View(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            setUpButtonsWithPermissions()
            setUpLocationRequestAndCallback()
        } else {
            setUpButtonsWithoutPermissions()
        }
        setUpButtonsWithoutPermissionRequirement()
        setUpListenerForEmergency()
    }


    private fun setUpButtonsWithoutPermissionRequirement() {
        _binding?.buttonCancelarPanic?.setOnClickListener {
            val intent = Intent(requireActivity(), CancelarActivity::class.java)
            startActivity(intent)
        }

        _binding?.buttonPersonalizePanic?.setOnClickListener {
            val intent = Intent(requireActivity(), ConfigurarMensajeActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setUpButtonsWithoutPermissions() {
        _binding?.imgButtonPanic?.setOnClickListener {
            checkPermissionForLocation(requireActivity() as AppCompatActivity)
        }
    }

    private fun setUpButtonsWithPermissions() {
        _binding?.imgButtonPanic?.setOnClickListener {
            Toast.makeText(requireContext(), "Panic button pressed", Toast.LENGTH_SHORT).show()


            // Get reference to database and set own emergency state to true
            val databaseRef = FirebaseDatabase.getInstance().getReference("Users/${FirebaseAuth.getInstance().currentUser?.uid}/emergency")
            databaseRef.setValue(true)
        }
    }

    private fun disableButtonsWithPermissions() {
        _binding?.imgButtonPanic?.isEnabled = false
    }

    private fun setUpListenerForEmergency() {
        // Get reference to database and listen for changes for own emergency state
        val databaseRef = FirebaseDatabase.getInstance().getReference("Users/${FirebaseAuth.getInstance().currentUser?.uid}/emergency")

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val emergency = snapshot.getValue(Boolean::class.java)
                emergency?.let {
                    if (emergency) {
                        // If emergency is true, disable the panic button and enable the cancel button
                        binding?.imgButtonPanic?.isEnabled = false
                        binding?.buttonCancelarPanic?.isEnabled = true
                        // Start location service for getting and uploading location
                        startLocationUpdates()
                    } else {
                        // If emergency is false, enable the panic button and disable the cancel button
                        binding?.imgButtonPanic?.isEnabled = true
                        binding?.buttonCancelarPanic?.isEnabled = false
                        // Stop location service
                        stopLocationUpdates()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PanicoFragment", "Error loading emergency state", error.toException())
            }
        })
    }

    private fun setUpLocationRequestAndCallback() {
        mLocationRequest = createLocationRequest()
        mLocationCallback = object : LocationCallback() {
            @SuppressLint("SetTextI18n")
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    val databaseRef = FirebaseDatabase.getInstance().getReference("Users/${FirebaseAuth.getInstance().currentUser?.uid}/location")
                    databaseRef.setValue("${location.latitude},${location.longitude}")
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (binding != null && ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationRequest?.let { mLocationCallback?.let { it1 ->
                mFusedLocationClient?.requestLocationUpdates(it,
                    it1, null)
            } }
        }
    }

<<<<<<< HEAD
    fun stopLocationUpdates() {
        if (::mLocationCallback.isInitialized) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback)
        }
=======
    private fun stopLocationUpdates() {
        mLocationCallback?.let { mFusedLocationClient?.removeLocationUpdates(it) }
>>>>>>> 66b7439 (User messages)
    }

    private fun createLocationRequest() : LocationRequest =
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).apply {
            setMinUpdateIntervalMillis(5000)
        }.build()


    private fun checkPermissionForLocation(activity: AppCompatActivity) {
        when{
            ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
                setUpButtonsWithPermissions()
                setUpLocationRequestAndCallback()
            }

            shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // If user previously denied the permission
                Toast.makeText(requireContext(), "Permission previously denied", Toast.LENGTH_SHORT).show()
                requestPermission(requireActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION,
                    PermissionsCodes.LOCATION_PERMISSION_CODE, "Needed for locating person")
            }

            else -> {
                // Always call the own function to request permission, not the system one (requestPermissions)
                requestPermission(requireActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION,
                    PermissionsCodes.LOCATION_PERMISSION_CODE, "Needed for locating person")
            }

        }
    }

    private fun requestPermission(context: Activity, permission: String, requestCode: Int, justify: String) {
        if(ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            if(shouldShowRequestPermissionRationale(permission)) {
                Toast.makeText(requireContext(), justify, Toast.LENGTH_SHORT).show()
            }
            requestPermissions(arrayOf(permission), requestCode)
        } else {
            Toast.makeText(requireContext(), "Permission granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PermissionsCodes.LOCATION_PERMISSION_CODE -> {
                if((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission granted
                    setUpButtonsWithPermissions()
                    setUpLocationRequestAndCallback()
                } else {
                    // Permission denied
                    Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
                    disableButtonsWithPermissions()
                }
                return
            }

            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}