package com.example.azalea.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.azalea.databinding.ActivityChangeCodeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ChangeCodeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangeCodeBinding
    private var isFirstTime: Boolean = false
    private var lastCodeSaved: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkIfIsFirstCode()
    }

    private fun checkIfIsFirstCode() {
        // Get database instance for emergencyCode
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid
        val databaseRef = FirebaseDatabase.getInstance().getReference("Users/$currentUid/emergencyCode")

        databaseRef.get().addOnSuccessListener {
            if (it.exists()) {
                if (it.value.toString().toInt() == -1) {
                    isFirstTime = true
                    binding.actualCodeTextInputLayout.visibility = View.GONE
                    binding.actualCodeEditTextProfile.visibility = View.GONE
                } else {
                    lastCodeSaved = it.value.toString().toInt()
                }

                setUpButtons()
            }
        }
    }

    private fun setUpButtons() {
        binding.goBackButtonLayoutChangeCode.setOnClickListener {
            finish()
        }

        binding.buttonSaveNewCode.setOnClickListener {
            val lastCode = binding.actualCodeEditTextProfile.text.toString()
            val newCode = binding.newCodeEditTextProfile.text.toString()

            if (isFirstTime && newCode.isNotEmpty()) {
                saveNewCode(newCode.toInt())
            } else if (lastCode.isNotEmpty() && newCode.isNotEmpty()){
                if(lastCode.toInt() == lastCodeSaved) saveNewCode(newCode.toInt())
                else Toast.makeText(this, "El código actual no coincide", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Por favor, rellene bien los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveNewCode(newCode: Int) {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid
        val databaseRef = FirebaseDatabase.getInstance().getReference("Users/$currentUid/emergencyCode")

        databaseRef.setValue(newCode)
        Toast.makeText(this, "Código de emergencia guardado", Toast.LENGTH_SHORT).show()
        finish()
    }
}