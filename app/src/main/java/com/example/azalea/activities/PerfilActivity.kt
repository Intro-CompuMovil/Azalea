package com.example.azalea.activities

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.azalea.data.User
import com.example.azalea.databinding.ActivityAddBasicDataBinding
import com.example.azalea.databinding.ActivityPerfilBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import com.google.firebase.storage.storage
import java.io.File
import java.io.FileOutputStream

class PerfilActivity : AppCompatActivity() {
    private lateinit var preferences: SharedPreferences
    private lateinit var binding: ActivityPerfilBinding
    private val storageRef = Firebase.storage.reference
    private val databaseRef = Firebase.database.reference
    private val pickMedia = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        binding.profileImage.setImageURI(uri)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpButtons()
        setUpInformation()
        preferences = getSharedPreferences("myPrefs", MODE_PRIVATE)
        reloadImageFromFirebase()
    }

    override fun onResume() {
        super.onResume()
        setUpInformation()
    }

    private fun setUpButtons() {
        binding.goBackButtonLayoutProfile.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        binding.editProfileButton.setOnClickListener {
            val intent = Intent(this, AddBasicDataActivity::class.java)
            startActivity(intent)
        }

        binding.profileImage.setOnClickListener {
            showMediaOptions()
        }

        // Listen for when the spinner is changed of state
        binding.profileStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val status = parent?.getItemAtPosition(position).toString()
                if (status == "Disponible") {
                    binding.profileStatusIcon.setImageResource(android.R.drawable.presence_online)
                } else {
                    binding.profileStatusIcon.setImageResource(android.R.drawable.presence_busy)
                }

                // Update on firebase database

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun setUpInformation() {
        val uid = Firebase.auth.currentUser?.uid
        if (uid != null) {
            val userRef = databaseRef.child(uid)
            userRef.get().addOnSuccessListener {
                if (it.exists()) {
                    val user = it.getValue(User::class.java)
                    if (user != null) {
                        binding.profileName.text = user.name
                        binding.profileEmail.text = user.email
                        binding.profileInfoBirthdate.text = user.birthDate
                        binding.profileInfoRH.text = user.bloodType
                        binding.profileInfoWeight.text = user.weight.toString()
                        binding.profileInfoHeight.text = user.height.toString()
                        binding.profileInfoExtra.text = user.description

                        if (user.available) {
                            binding.profileStatus.setSelection(0)
                            binding.profileStatusIcon.setImageResource(android.R.drawable.presence_online)
                        } else {
                            binding.profileStatus.setSelection(1)
                            binding.profileStatusIcon.setImageResource(android.R.drawable.presence_busy)
                        }
                    }
                }
            }
        }
    }

    private fun showMediaOptions() {
        val options = arrayOf("Tomar foto", "Seleccionar de galería")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecciona una opción")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> {
                    if (checkCameraPermission()) {
                        openCamera()
                    } else {
                        requestCameraPermission()
                    }
                }
                1 -> pickMedia.launch("image/*")
            }
        }
        builder.show()
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_CODE)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, "Permiso de cámara denegado.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            saveImageToFirebase(imageBitmap)
            reloadImageFromFirebase()
        } else if (requestCode == REQUEST_SELECT_IMAGE && resultCode == RESULT_OK) {
            val selectedImageUri: Uri? = data?.data
            if (selectedImageUri != null) {
                saveImageToFirebase(selectedImageUri)
                reloadImageFromFirebase()
            }
        }
    }

    private fun saveImageToFirebase(bitmap: Bitmap) {
        // Get the current uid and save image acordingly
        val uid = Firebase.auth.currentUser?.uid ?: return
        val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        val imageFile = File(imagesDir, "$uid.jpg")
        val outputStream = FileOutputStream(imageFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        MediaScannerConnection.scanFile(this, arrayOf(imageFile.absolutePath), null, null)

        // Guardar la ruta de la imagen en SharedPreferences
        val editor = preferences.edit()
        editor.putString("imagePath", imageFile.absolutePath)
        editor.apply()

        // Subir la imagen a Firebase Storage
        val imageRef = storageRef.child("pfp/$uid.jpg")
        val imageUri = Uri.fromFile(imageFile)
        imageRef.putFile(imageUri)
    }

    private fun saveImageToFirebase(imageUri: Uri) {
        // Guardar la ruta de la imagen en SharedPreferences
        val editor = preferences.edit()
        editor.putString("imagePath", imageUri.toString())
        editor.apply()

        // Subir la imagen a Firebase Storage
        val uid = Firebase.auth.currentUser?.uid ?: return
        val imageRef = storageRef.child("pfp/$uid.jpg")
        imageRef.putFile(imageUri)
    }

    private fun reloadImageFromFirebase() {
        // Get the uid of the current user and search for the corresponding image
        val uid = Firebase.auth.currentUser?.uid
        if (uid != null) {
            val imageRef = storageRef.child("pfp/$uid.jpg")
            MenuNavigationActivity.tempFile = File.createTempFile(uid, "jpg")
            imageRef.getFile(MenuNavigationActivity.tempFile).addOnSuccessListener {
                val imageUri = Uri.fromFile(MenuNavigationActivity.tempFile)
                binding.profileImage.setImageURI(imageUri)
            }.addOnFailureListener {
                Toast.makeText(this, "Error al cargar la imagen de perfil", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
        private const val REQUEST_IMAGE_CAPTURE = 101
        private const val REQUEST_SELECT_IMAGE = 102
    }
}