package com.example.azalea.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.example.azalea.R

class AddBasicDataActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_basic_data)

        setUpButtons()
    }

    private fun setUpButtons() {
        val guardarButton = findViewById<Button>(R.id.btnSaveChanges)
        guardarButton.setOnClickListener {
            Toast.makeText(this, "Cambios guardados", Toast.LENGTH_SHORT).show()
        }
    }
}