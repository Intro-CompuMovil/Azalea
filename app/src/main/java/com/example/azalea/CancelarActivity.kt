package com.example.azalea

import android.content.Intent
import android.content.pm.PackageManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.azalea.PermissionsCodes.Companion.BIOMETRIC_PERMISSION_CODE
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class CancelarActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cancelar)

        checkPermissionForBiometric(this)
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

    private fun setUpButtons() {
        val menuButton = findViewById<ImageButton>(R.id.imgButtonMenuCancelAct)
        menuButton.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
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
                runOnUiThread{
                    Toast.makeText(applicationContext, "Authentication succeeded", Toast.LENGTH_SHORT).show()
                }
                // Continue with your action after successful authentication
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

        val cancelarButton = findViewById<Button>(R.id.buttonSendCancelAct)
        cancelarButton.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }
    }

    private fun disableButtons() {
        val menuButton = findViewById<ImageButton>(R.id.imgButtonMenuCancelAct)
        menuButton.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
        }

        val cancelarButton = findViewById<Button>(R.id.buttonSendCancelAct)
        cancelarButton.isEnabled = false
    }
}