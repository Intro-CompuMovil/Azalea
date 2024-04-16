package com.example.azalea.activities

import android.Manifest
import android.app.Activity
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
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.azalea.R
import com.example.azalea.data.PermissionsCodes.Companion.CAMERA_PERMISSION_CODE
import com.example.azalea.databinding.ActivityPerfilBinding
import java.io.File
import java.io.FileOutputStream

class PerfilActivity : AppCompatActivity() {
    private lateinit var preferences: SharedPreferences
    private lateinit var profileImage: ImageButton
    private val pickMedia = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        profileImage.setImageURI(uri)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)
        preferences = getSharedPreferences("myPrefs", MODE_PRIVATE)

        // Cargar la imagen de perfil si existe
        val imagePath = preferences.getString("imagePath", null)

        profileImage = findViewById(R.id.profileImage)

        if (imagePath != null) {
            profileImage.setImageURI(Uri.parse(imagePath))
        }

        profileImage.setOnClickListener {
            showMediaOptions()
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
            saveImageToGallery(imageBitmap)
            profileImage.setImageBitmap(imageBitmap)
        } else if (requestCode == REQUEST_SELECT_IMAGE && resultCode == RESULT_OK) {
            val selectedImageUri: Uri? = data?.data
            if (selectedImageUri != null) {
                profileImage.setImageURI(selectedImageUri)
                saveImageToGallery(selectedImageUri)
            }
        }
    }

    private fun saveImageToGallery(bitmap: Bitmap) {
        val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        val imageFile = File(imagesDir, "profile_image_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(imageFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        MediaScannerConnection.scanFile(this, arrayOf(imageFile.absolutePath), null, null)

        // Guardar la ruta de la imagen en SharedPreferences
        val editor = preferences.edit()
        editor.putString("imagePath", imageFile.absolutePath)
        editor.apply()
    }

    private fun saveImageToGallery(imageUri: Uri) {
        // Guardar la ruta de la imagen en SharedPreferences
        val editor = preferences.edit()
        editor.putString("imagePath", imageUri.toString())
        editor.apply()
    }

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
        private const val REQUEST_IMAGE_CAPTURE = 101
        private const val REQUEST_SELECT_IMAGE = 102
    }
}