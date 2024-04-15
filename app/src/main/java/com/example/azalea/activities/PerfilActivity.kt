package com.example.azalea.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.azalea.data.PermissionsCodes.Companion.CAMERA_PERMISSION_CODE
import com.example.azalea.databinding.ActivityPerfilBinding

class PerfilActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPerfilBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpButtonsWithoutPermissions()
    }

    private fun setUpButtonsWithoutPermissions() {
        binding.profileImage.setOnClickListener {
            checkPermissionForCamera(this)
        }

        binding.buttonEditProfile.setOnClickListener {
            val intent = Intent(this, AddBasicDataActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setUpButtonsWithCameraPermissions() {
        binding.profileImage.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivity(takePictureIntent)
        }
    }

    private fun disableButtons() {
        binding.profileImage.isEnabled = false
    }

    private fun checkPermissionForCamera(activity: AppCompatActivity) {
        when{
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
                setUpButtonsWithCameraPermissions()
            }

            shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA) -> {
                // If user previously denied the permission
                Toast.makeText(this, "Permission previously denied", Toast.LENGTH_SHORT).show()
                requestPermission(this, android.Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE, "Needed for taking pictures")
            }

            else -> {
                // Always call the own function to request permission, not the system one (requestPermissions)
                requestPermission(this, android.Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE, "Needed for taking pictures")
            }

        }
    }

    private fun requestPermission(context: AppCompatActivity, permission: String, requestCode: Int, justify: String) {
        if(ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            if(shouldShowRequestPermissionRationale(permission)) {
                Toast.makeText(this, justify, Toast.LENGTH_SHORT).show()
            }
            requestPermissions(arrayOf(permission), requestCode)
        } else {
            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_PERMISSION_CODE -> {
                if((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission granted
                    setUpButtonsWithCameraPermissions()
                } else {
                    // Permission denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                    disableButtons()
                }
                return
            }

            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }
}