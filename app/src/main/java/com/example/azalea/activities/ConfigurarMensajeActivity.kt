package com.example.azalea.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.azalea.data.User
import com.example.azalea.databinding.ActivityConfigurarMensajeBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ConfigurarMensajeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityConfigurarMensajeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigurarMensajeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getCurrentMessages()
        setUpButtons()
    }

    private fun getCurrentMessages() {
        // Use databaseRef given the current UID
        val databaseRef = FirebaseDatabase.getInstance().getReference("Users/${FirebaseAuth.getInstance().currentUser!!.uid}")

        // Get user
        databaseRef.get().addOnSuccessListener {
            val user = it.getValue(User::class.java)

            // Update user
            user?.let {
                binding.editTextMsgConfigAct.setText(user.emergencyMessage)
                binding.editTextCancelMsgConfigAct.setText(user.cancelMessage)
            }
        }
    }

    private fun setUpButtons() {
        binding.goBackButtonMessageAct.setOnClickListener {
            finish()
        }

        binding.buttonSaveMsgConfigAct.setOnClickListener {
            val messageEmergency = binding.editTextMsgConfigAct.text.toString()
            val messageCancel = binding.editTextCancelMsgConfigAct.text.toString()

            if (messageEmergency.isNotEmpty() && messageCancel.isNotEmpty()) {
                saveMessages(messageEmergency, messageCancel)
                Toast.makeText(this, "Mensaje guardado", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Por favor, ingrese un mensaje", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveMessages(messageEmergency: String, messageCancel: String) {
        // Use databaseRef given the current UID
        val databaseRef = FirebaseDatabase.getInstance().getReference("Users/${FirebaseAuth.getInstance().currentUser!!.uid}")

        // Get user
        databaseRef.get().addOnSuccessListener {
            val user = it.getValue(User::class.java)

            // Update user
            user?.let {
                user.emergencyMessage = messageEmergency
                user.cancelMessage = messageCancel
                databaseRef.setValue(user)
            }
        }
    }
}