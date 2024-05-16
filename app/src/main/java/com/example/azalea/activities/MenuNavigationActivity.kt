package com.example.azalea.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.azalea.fragments.PanicoFragment
import com.example.azalea.R
import com.example.azalea.databinding.ActivityMenuNavigationBinding
import com.example.azalea.fragments.EmergencyAlertsFragment
import com.example.azalea.fragments.EmergencyContactsFragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.storage.storage
import java.io.File

class MenuNavigationActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    companion object {
        // Saves the temp file for the profile image
        lateinit var tempFile: File
    }
    private lateinit var binding: ActivityMenuNavigationBinding
    private lateinit var fragmentManager: FragmentManager
    private val storageRef = Firebase.storage.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpButtons()
        setUpFragmentNavigation()
        loadImageFromFirebase()
        openFragment(PanicoFragment(), "Botón de pánico")
    }

    private fun setUpButtons() {
        binding.pfpButton.setOnClickListener {
            val intent = Intent(this, PerfilActivity::class.java)
            startActivity(intent)
        }

        binding.notificationsButton.setOnClickListener {
            Toast.makeText(this, "Notificaciones", Toast.LENGTH_SHORT).show()
            //val intent = Intent(this, NotificationsActivity::class.java)
            //startActivity(intent)
        }
    }

    private fun setUpFragmentNavigation() {
        binding.bottomNavigationView.background = null
        binding.bottomNavigationView.setOnItemSelectedListener {item ->
            onNavigationItemSelected(item)
        }
        fragmentManager = supportFragmentManager
    }

    @SuppressLint("SetTextI18n")
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.buttonPanico -> {
                openFragment(PanicoFragment(), "Botón de pánico")
                return true
            }
            R.id.buttonAlertas -> {
                openFragment(EmergencyAlertsFragment(), "Alertas de emergencia")
                return true
            }
            R.id.buttonContactos -> {
                openFragment(EmergencyContactsFragment(), "Contactos de emergencia")
                return true
            }
            else -> {
                return false
            }
        }
    }

    private fun openFragment(fragment: Fragment, tag: String = "") {
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        binding.fragmentTitleTextView.text = tag
        fragmentTransaction.replace(R.id.fragmentContainerView, fragment)
        fragmentTransaction.commit()
    }

    private fun loadImageFromFirebase() {
        // Get the uid of the current user and search for the corresponding image
        val uid = Firebase.auth.currentUser?.uid
        if (uid != null) {
            val imageRef = storageRef.child("pfp/$uid.jpg")
            tempFile = File.createTempFile(uid, "jpg")
            imageRef.getFile(tempFile).addOnSuccessListener {
                val imageUri = Uri.fromFile(tempFile)
                binding.pfpButton.setImageURI(imageUri)
            }.addOnFailureListener {
                Toast.makeText(this, "Error al cargar la imagen de perfil", Toast.LENGTH_SHORT).show()
            }
        }
    }
}