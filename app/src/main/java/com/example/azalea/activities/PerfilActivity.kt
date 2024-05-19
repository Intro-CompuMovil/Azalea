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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class PerfilActivity : AppCompatActivity() {
    private lateinit var preferences: SharedPreferences
    private lateinit var binding: ActivityPerfilBinding
    private val pickMedia = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        binding.profileImage.setImageURI(uri)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpButtons()
        preferences = getSharedPreferences("myPrefs", MODE_PRIVATE)
    }

    override fun onResume() {
        super.onResume()
        setUpInformation()
        reloadImageFromFirebase()
    }

    private fun setUpButtons() {
        binding.goBackButtonLayoutProfile.setOnClickListener {
            val intent = Intent(this, MenuNavigationActivity::class.java)
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
                FirebaseDatabase.getInstance().getReference("Users")
                    .child(FirebaseAuth.getInstance().currentUser?.uid!!)
                    .child("available")
                    .setValue(status == "Disponible")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        binding.linearLayoutLogOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.linearLayoutChangeCode.setOnClickListener {
            val intent = Intent(this, ChangeCodeActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setUpInformation() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid!!
        val userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid)

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
                1 -> {
                    if (checkStoragePermission()) {
                        pickMedia.launch("image/*")
                    } else {
                        requestStoragePermission()
                    }
                }
            }
        }
        builder.show()
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_CODE)
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            STORAGE_PERMISSION_CODE)
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
            STORAGE_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickMedia.launch("image/*")
                } else {
                    Toast.makeText(this, "Permiso de almacenamiento denegado.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var imageUri: Uri? = null
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imageUri = getImageUriFromBitmap(imageBitmap)
        } else if (requestCode == REQUEST_SELECT_IMAGE && resultCode == RESULT_OK) {
            val selectedImageUri: Uri? = data?.data
            if (selectedImageUri != null) {
                imageUri = selectedImageUri
            }
        }

        if (imageUri != null) {
            uploadImageToFirebase(imageUri)
        }
    }

    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "Title", null)
        return Uri.parse(path)
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid!!
        val imageRef = FirebaseStorage.getInstance().reference.child("pfp/$uid")
        imageRef.putFile(imageUri).addOnSuccessListener {
            Toast.makeText(this, "Imagen subida exitosamente", Toast.LENGTH_SHORT).show()
            reloadImageFromFirebase()
        }.addOnFailureListener {
            Toast.makeText(this, "Error al subir la imagen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun reloadImageFromFirebase() {
        // Get the uid of the current user and search for the corresponding image
        val uid = FirebaseAuth.getInstance().currentUser?.uid!!
        val storageRef = FirebaseStorage.getInstance().reference.child("pfp/$uid")
        val tempFile = File.createTempFile(uid, "jpg")

        storageRef.getFile(tempFile).addOnSuccessListener {
            val imageUri = Uri.fromFile(tempFile)
            binding.profileImage.setImageURI(imageUri)
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
        private const val REQUEST_IMAGE_CAPTURE = 101
        private const val REQUEST_SELECT_IMAGE = 102
        private const val STORAGE_PERMISSION_CODE = 103
    }
}