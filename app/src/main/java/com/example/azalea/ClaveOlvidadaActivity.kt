package com.example.azalea

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

class ClaveOlvidadaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clave_olvidada)

        setUpButtons();
    }

    private fun setUpButtons() {
        val enviarCorreoButton = findViewById<Button>(R.id.buttonSendMailPassForget);
        enviarCorreoButton.setOnClickListener {
            Toast.makeText(this, "Mail sent", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}