package com.example.azalea.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.azalea.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.example.azalea.databinding.ActivityRegistrarseBinding

class RegistrarseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistrarseBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrarseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        setUpButtons();
    }

    private fun setUpButtons() {
        binding.buttonRegister.setOnClickListener {
            registerUser()
        }

        binding.loginTextView.setOnClickListener {
            navigateToMainActivity()
        }
    }

   private fun registerUser() {
    val email = binding.editTextEmailAddressRegister.text.toString()
    val password = binding.editTextPasswordRegister.text.toString()
    val name = binding.editTextNameRegister.text.toString()

    if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
        Toast.makeText(
            baseContext, "Los campos no pueden estar vacios",
            Toast.LENGTH_SHORT
        ).show()
        return
    }

    if (!isEmailValid(email)) {
        Toast.makeText(
            baseContext, "El correo es inválido",
            Toast.LENGTH_SHORT
        ).show()
        return
    }

    if (!isPasswordValid(password)) {
        Toast.makeText(
            baseContext, "La contraseña debe tener mínimo 6 caracteres",
            Toast.LENGTH_SHORT
        ).show()

        return
    }

    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                saveUserToDatabase(name, email)
                navigateToMainActivity()
            } else {
                Toast.makeText(
                    baseContext, "Error al registrarse",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
}



    private fun validateInput(email: String, password: String, name: String): Boolean {
        return email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty() && isEmailValid(email) && isPasswordValid(password)
    }

    private fun isEmailValid(email: String): Boolean {
        if (!email.contains("@") ||
            !email.contains(".") ||
            email.length < 5)
            return false
        return true
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 6
    }

    private fun saveUserToDatabase(name: String, email: String) {
        val user = User(name, email)
        FirebaseDatabase.getInstance().getReference("Users")
            .child(FirebaseAuth.getInstance().currentUser?.uid!!)
            .setValue(user)
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        Toast.makeText(
            baseContext, "Registrado exitosamente",
            Toast.LENGTH_SHORT
        ).show()
        startActivity(intent)
    }


}