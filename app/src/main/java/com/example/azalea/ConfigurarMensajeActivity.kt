package com.example.azalea

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast

class ConfigurarMensajeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configurar_mensaje)

        setUpButtons()
    }

    private fun setUpButtons() {
        val menuButton = findViewById<ImageButton>(R.id.imgButtonMenuMsgConfigAct)
        menuButton.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
        }

        val guardarButton = findViewById<Button>(R.id.buttonSaveMsgConfigAct)
        guardarButton.setOnClickListener {
            Toast.makeText(this, "Mensaje de pánico guardado", Toast.LENGTH_SHORT).show()
        }
    }
}