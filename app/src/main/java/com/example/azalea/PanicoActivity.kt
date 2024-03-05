package com.example.azalea

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.azalea.PermissionsCodes.Companion.LOCATION_PERMISSION_CODE

class PanicoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_panico)

        checkPermissionForLocation(this)
    }

    private fun checkPermissionForLocation(activity: AppCompatActivity) {
        when{
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
                setUpButtons()
            }

            shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // If user previously denied the permission
                Toast.makeText(this, "Permission previously denied", Toast.LENGTH_SHORT).show()
                requestPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION, LOCATION_PERMISSION_CODE, "Needed for locating person")
            }

            else -> {
                // Always call the own function to request permission, not the system one (requestPermissions)
                requestPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION, LOCATION_PERMISSION_CODE, "Needed for locating person")
            }

        }
    }

    private fun requestPermission(context: Activity, permission: String, requestCode: Int, justify: String) {
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
            LOCATION_PERMISSION_CODE -> {
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
        val menuButton = findViewById<ImageButton>(R.id.imgButtonMenuPanicAct)
        menuButton.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
        }

        val panicoButton = findViewById<ImageButton>(R.id.imgButtonPanicAct)
        panicoButton.setOnClickListener {
            Toast.makeText(this, "Mensaje de pánico enviado", Toast.LENGTH_SHORT).show()
        }

        val cancelarButton = findViewById<Button>(R.id.buttonCancelarPanicAct)
        cancelarButton.setOnClickListener {
            val intent = Intent(this, CancelarActivity::class.java)
            startActivity(intent)
        }
    }

    private fun disableButtons() {
        val panicoButton = findViewById<ImageButton>(R.id.imgButtonPanicAct)
        panicoButton.isEnabled = false

        val cancelarButton = findViewById<Button>(R.id.buttonCancelarPanicAct)
        cancelarButton.isEnabled = false
    }
}