package com.example.azalea.activities

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.example.azalea.R
import com.example.azalea.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private  lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar Firebase Auth
        auth = Firebase.auth

        setUpButtonLogin();
        setUpButtonRegister();
    }

    // Boton de registro
    private fun setUpButtonRegister() {
        binding.registerTextView.setOnClickListener {
            val intent = Intent(this, RegistrarseActivity::class.java)
            startActivity(intent)
        }
    }
//Valiracion de email y contraseña
    private fun isEmailValid(email: String): Boolean {
        if (!email.contains("@") ||
            !email.contains(".") ||
            email.length < 5)
            return false
        return true
    }
    private fun isPasswordValid(password: String): Boolean {
        if (password.length < 6)
            return false
        return true
    }

    // Boton de inicio de sesion
    private fun setUpButtonLogin() {
        val emailField = findViewById<EditText>(R.id.editTextUserMainAct)
        val passwordField = findViewById<EditText>(R.id.editTextPasswordMainAct)
        val butonIniciar = findViewById<Button>(R.id.buttonLogInMainAct)

        binding.buttonLogInMainAct.setOnClickListener {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()

            if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
                if (isEmailValid(email)) {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                // si la autenticacion es exitosa, pasar a la siguiente actividad
                                Log.d(ContentValues.TAG, "signInWithEmail:success")
                                val intent = Intent(this, MenuNavigationActivity::class.java)
                                startActivity(intent)
                            }else{
                                // si falla la autenticacion, mostrar un mensaje al usuario
                                Log.w(ContentValues.TAG, "signInWithEmail:failure", task.exception)
                                Toast.makeText(
                                    baseContext, "Autenticacion fallida",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }else{
                    Toast.makeText(
                        baseContext, "Por favor ingrese un correo válido.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }else {
                Toast.makeText(
                    baseContext, "Por favor ingrese un correo y una contraseña.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}