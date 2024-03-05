package com.example.azalea

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        setUpButtons()
    }

    private fun setUpButtons() {
        val perfilButton = findViewById<Button>(R.id.buttonMenuPerfil)
        perfilButton.setOnClickListener {
            val intent = Intent(this, PerfilActivity::class.java)
            startActivity(intent)
        }

        val cancelarButton = findViewById<Button>(R.id.buttonMenuCancel)
        cancelarButton.setOnClickListener {
            val intent = Intent(this, CancelarActivity::class.java)
            startActivity(intent)
        }

        val panicoButton = findViewById<Button>(R.id.buttonMenuPanico)
        panicoButton.setOnClickListener {
            val intent = Intent(this, PanicoActivity::class.java)
            startActivity(intent)
        }

        val configurarMsgButton = findViewById<Button>(R.id.buttonMenuCongMeng)
        configurarMsgButton.setOnClickListener {
            val intent = Intent(this, ConfigurarMensajeActivity::class.java)
            startActivity(intent)
        }

        val datosBasicosButton = findViewById<Button>(R.id.buttonMenuRegisDatBas)
        datosBasicosButton.setOnClickListener {
            val intent = Intent(this, AddBasicDataActivity::class.java)
            startActivity(intent)
        }

        val registrarEmergenciaButton = findViewById<Button>(R.id.buttonMenuRegiContEmer)
        registrarEmergenciaButton.setOnClickListener {
            val intent = Intent(this, RegistrarContactoActivity::class.java)
            startActivity(intent)
        }
    }
}