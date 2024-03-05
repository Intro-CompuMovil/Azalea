package com.example.azalea

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.azalea.PermissionsCodes.Companion.CONTACTS_PERMISSION_CODE

class RegistrarContactoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_contacto)

        checkPermissionForContacts()
    }

    private fun checkPermissionForContacts() {
        when{
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
                setUpButtons()
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
                    setUpButtons()
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

    private fun setUpButtons() {
        val seleccionarContactoButton = findViewById<Button>(R.id.btnSelectContact)
        seleccionarContactoButton.setOnClickListener {
            // Implicit intent to select a contact
            val intent = Intent(Intent.ACTION_PICK, android.provider.ContactsContract.Contacts.CONTENT_URI)
            startActivity(intent)
        }

        val registrarContactoButton = findViewById<Button>(R.id.btnSendRequest)
        registrarContactoButton.setOnClickListener {
            Toast.makeText(this, "Solicitud enviada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun disableButtons() {
        val seleccionarContactoButton = findViewById<Button>(R.id.btnSelectContact)
        seleccionarContactoButton.isEnabled = false

        val registrarContactoButton = findViewById<Button>(R.id.btnSendRequest)
        registrarContactoButton.isEnabled = false
    }
}