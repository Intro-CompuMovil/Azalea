package com.example.azalea.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.azalea.databinding.ActivityConfigurarMensajeBinding

class ConfigurarMensajeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityConfigurarMensajeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigurarMensajeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpButtons()
    }

    private fun setUpButtons() {
        binding.goBackButtonMessageAct.setOnClickListener {
            val intent = Intent(this, MenuNavigationActivity::class.java)
            startActivity(intent)
        }

        binding.buttonSaveMsgConfigAct.setOnClickListener {
            Toast.makeText(this, "Mensaje de pánico guardado", Toast.LENGTH_SHORT).show()
        }
    }
}