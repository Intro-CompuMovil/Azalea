package com.example.azalea.activities

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.azalea.R
import com.example.azalea.data.PermissionsCodes.Companion.CONTACTS_PERMISSION_CODE
import com.example.azalea.databinding.ActivityRegistrarContactoBinding

class RegistrarContactoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistrarContactoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrarContactoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpButtonsWithoutPermissions()
    }

    private fun checkPermissionForContacts() {
        when{
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
                setUpButtonsWithPermissions()
            }

            shouldShowRequestPermissionRationale(android.Manifest.permission.READ_CONTACTS) -> {
                // If user previously denied the permission
                Toast.makeText(this, "Permission previously denied", Toast.LENGTH_SHORT).show()
                requestPermission(this, android.Manifest.permission.READ_CONTACTS, CONTACTS_PERMISSION_CODE, "Needed for adding contacts")
            }

            else -> {
                // Always call the own function to request permission, not the system one (requestPermissions)
                requestPermission(this, android.Manifest.permission.READ_CONTACTS, CONTACTS_PERMISSION_CODE, "Needed for adding contacts")
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
            CONTACTS_PERMISSION_CODE -> {
                if((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission granted
                    setUpButtonsWithPermissions()
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

    private fun setUpButtonsWithoutPermissions() {
        binding.btnSelectContact.setOnClickListener {
            checkPermissionForContacts()
        }

        binding.btnSendRequest.setOnClickListener {
            Toast.makeText(this, "Solicitud enviada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setUpButtonsWithPermissions() {
        binding.btnSelectContact.setOnClickListener {
            // Implicit intent to select a contact
            val intent = Intent(Intent.ACTION_PICK, android.provider.ContactsContract.Contacts.CONTENT_URI)
            startActivity(intent)
        }
    }

    private fun disableButtons() {
        binding.btnSelectContact.isEnabled = false
    }
}