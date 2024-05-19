package com.example.azalea.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import androidx.biometric.BiometricPrompt
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.azalea.data.PermissionsCodes.Companion.BIOMETRIC_PERMISSION_CODE
import java.util.concurrent.Executors
import com.example.azalea.databinding.ActivityCancelarBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class CancelarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCancelarBinding
    private val databaseRef = Firebase.database.reference
    private var confirmation: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCancelarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPermissionForBiometric(this)
    }

    @SuppressLint("SetTextI18n")
    private fun setUpButtons() {
        binding.switchCancelAct.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                buttonView.text = "Sí"
            } else {
                buttonView.text = "No"
            }
        }

        binding.goBackButtonCancelAct.setOnClickListener {
            val intent = Intent(this, MenuNavigationActivity::class.java)
            startActivity(intent)
        }

        val executor = Executors.newSingleThreadExecutor()

        val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                runOnUiThread{
                    Toast.makeText(applicationContext, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                runOnUiThread {
                    Toast.makeText(
                        applicationContext,
                        "Authentication succeeded",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                // Get reference to database and set own emergency state to true
                val databaseRef = FirebaseDatabase.getInstance().getReference("Users/${FirebaseAuth.getInstance().currentUser?.uid}/emergency")
                databaseRef.setValue(false)
                // Continue with your action after successful authentication
                val intent = Intent(applicationContext, MenuNavigationActivity::class.java)
                startActivity(intent)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                runOnUiThread{
                    Toast.makeText(applicationContext, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric for my app")
            .setSubtitle("Assert your identity")
            .setNegativeButtonText("Use other method")
            .build()

        binding.buttonSendCancelAct.setOnClickListener {
            checkForConfirmation()
            if(confirmation) {
                biometricPrompt.authenticate(promptInfo)
            } else {
                Toast.makeText(this, "Ingrese codigo correcto y/o confirme cancelacion", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun disableButtons() {
        binding.goBackButtonCancelAct.setOnClickListener {
            val intent = Intent(this, MenuNavigationActivity::class.java)
            startActivity(intent)
        }

        binding.buttonSendCancelAct.isEnabled = false
    }

    private fun checkForConfirmation() {
        val codeTyped = binding.codeTextFieldCancelAct.text.toString().toInt()

        // Internally checks if code was correct
        val databaseRef = FirebaseDatabase.getInstance().getReference("Users/${FirebaseAuth.getInstance().currentUser?.uid}/emergencyCode")
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val code = snapshot.getValue(Int::class.java)
                if (code != null) {
                    confirmation = codeTyped == code
                    if(!confirmation) Toast.makeText(this@CancelarActivity, "Código incorrecto", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CancelarActivity", "Error loading emergency code", error.toException())
                confirmation = false
            }
        })

        confirmation = binding.switchCancelAct.isChecked && confirmation
    }

    private fun checkPermissionForBiometric(activity: AppCompatActivity) {
        when{
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.USE_BIOMETRIC) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
                setUpButtons()
            }

            shouldShowRequestPermissionRationale(android.Manifest.permission.USE_BIOMETRIC) -> {
                // If user previously denied the permission
                Toast.makeText(this, "Permission previously denied", Toast.LENGTH_SHORT).show()
                requestPermission(this, android.Manifest.permission.USE_BIOMETRIC, BIOMETRIC_PERMISSION_CODE, "Needed for biometric authentication")
            }

            else -> {
                // Always call the own function to request permission, not the system one (requestPermissions)
                requestPermission(this, android.Manifest.permission.USE_BIOMETRIC, BIOMETRIC_PERMISSION_CODE, "Needed for biometric authentication")
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
            BIOMETRIC_PERMISSION_CODE -> {
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
}