package com.example.azalea.fragments

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
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

class PanicoFragment : Fragment() {
    private var _binding: FragmentPanicoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPanicoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpButtonsWithoutPermissions()
    }


    private fun setUpButtonsWithoutPermissions() {
        _binding?.imgButtonPanic?.setOnClickListener {
            checkPermissionForLocation(requireActivity() as AppCompatActivity)
        }

        _binding?.buttonCancelarPanic?.isEnabled = false
        _binding?.buttonCancelarPanic?.setOnClickListener {
            val intent = Intent(requireActivity(), CancelarActivity::class.java)
            startActivity(intent)
        }

        _binding?.buttonPersonalizePanic?.setOnClickListener {
            val intent = Intent(requireActivity(), ConfigurarMensajeActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setUpButtonsWithPermissions() {
        _binding?.imgButtonPanic?.setOnClickListener {
            Toast.makeText(requireContext(), "Panic button pressed", Toast.LENGTH_SHORT).show()
            _binding?.buttonCancelarPanic?.isEnabled = true
        }
    }

    private fun disableButtonsWithPermissions() {
        _binding?.imgButtonPanic?.isEnabled = false
    }

    private fun checkPermissionForLocation(activity: AppCompatActivity) {
        when{
            ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
                setUpButtonsWithPermissions()
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