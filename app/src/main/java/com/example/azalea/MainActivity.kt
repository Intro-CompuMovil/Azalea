package com.example.azalea

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setUpButtons();
    }

    private fun setUpButtons() {
        val logInButton = findViewById<Button>(R.id.buttonLogInMainAct)
        logInButton.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
        }

        val signUpText = findViewById<TextView>(R.id.registerTextView)
        signUpText.setOnClickListener {
            val intent = Intent(this, RegistrarseActivity::class.java)
            startActivity(intent)
        }

        val forgotPassText = findViewById<TextView>(R.id.forgotPasswordTextView)
        forgotPassText.setOnClickListener {
            val intent = Intent(this, ClaveOlvidadaActivity::class.java)
            startActivity(intent)
        }
    }
}