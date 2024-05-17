package com.example.azalea.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.azalea.R
import com.example.azalea.data.User
import com.example.azalea.databinding.ActivityAddBasicDataBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class AddBasicDataActivity : AppCompatActivity() {
    companion object {
        var userChange: User = User()
    }
    lateinit var binding: ActivityAddBasicDataBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBasicDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpEditTexts()
        setUpButtons()
    }

    override fun onResume() {
        super.onResume()
        setTextInformation()
        reloadImageFromFirebase()
    }

    private fun setUpEditTexts() {
        binding.dateEditTextProfile.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = "${selectedDay}/${selectedMonth + 1}/${selectedYear}"
                    binding.dateEditTextProfile.setText(selectedDate)
                },
                year,
                month,
                day
            )

            datePickerDialog.show()
        }
    }

    private fun setUpButtons() {
        binding.goBackButtonLayoutProfile.setOnClickListener {
            val intent = Intent(this, PerfilActivity::class.java)
            startActivity(intent)
        }

        binding.buttonSaveProfileInformation.setOnClickListener {
            saveProfileData()
            val intent = Intent(this, PerfilActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setTextInformation() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid!!
        val userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid)

        userRef.get().addOnSuccessListener {
            if (it.exists()) {
                val user = it.getValue(User::class.java)
                if (user != null) {
                        userChange = user
                        binding.nameEditTextProfile.setText(user.name)
                        binding.emailProfileTextView.text = user.email
                        binding.dateEditTextProfile.setText(user.birthDate)
                        val bloodTypeIndex = resources.getStringArray(R.array.spinner_rh).indexOf(user.bloodType)
                        binding.spinnerRh.setSelection(bloodTypeIndex)
                        binding.weightEditTextProfile.setText(user.weight.toString())
                        binding.heightEditTextProfile.setText(user.height.toString())
                        binding.descriptionEditTextProfile.setText(user.description)
                }
            }
        }
    }

    private fun reloadImageFromFirebase() {
        // Get the uid of the current user and search for the corresponding image
        val uid = FirebaseAuth.getInstance().currentUser?.uid!!
        val storageRef = FirebaseStorage.getInstance().reference.child("pfp/$uid")
        val tempFile = File.createTempFile(uid, "jpg")

        storageRef.getFile(tempFile).addOnSuccessListener {
            val imageUri = Uri.fromFile(tempFile)
            binding.profileEditImage.setImageURI(imageUri)
        }
    }

    private fun saveProfileData() {
        // Get the data from the EditTexts and save it in the database
        userChange.name = binding.nameEditTextProfile.text.toString()
        userChange.birthDate = binding.dateEditTextProfile.text.toString()
        userChange.bloodType = binding.spinnerRh.selectedItem.toString()
        userChange.weight = binding.weightEditTextProfile.text.toString().toDouble()
        userChange.height = binding.heightEditTextProfile.text.toString().toDouble()
        userChange.description = binding.descriptionEditTextProfile.text.toString()


        val uid = FirebaseAuth.getInstance().currentUser?.uid!!
        val userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid)
        userRef.setValue(userChange).addOnSuccessListener {
            Toast.makeText(this, "Datos guardados", Toast.LENGTH_SHORT).show()
        }
    }
}