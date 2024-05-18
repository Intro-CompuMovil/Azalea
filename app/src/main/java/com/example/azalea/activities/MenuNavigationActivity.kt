package com.example.azalea.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.EditText
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
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
        openFragment(PanicoFragment(), "Botón de pánico")

        // Get reference to database and listen for changes for own emergency state
        val uid = FirebaseAuth.getInstance().currentUser?.uid!!
        val databaseRef = FirebaseDatabase.getInstance().getReference("Users/$uid/emergencyCode")

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val emergencyCode = dataSnapshot.getValue(Int::class.java)

                if (emergencyCode == null) {
                    // Maneja el caso cuando el emergencyCode no se puede leer como Int
                    Log.e("MenuNavigationActivity", "El emergencyCode no se pudo leer como un entero")
                    return
                }
                if (emergencyCode == -1) {
                    setUpEmergencyCodeDialog()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
                Log.e("MenuNavigationActivity", "Error al leer emergencyCode: ${databaseError.message}")

            }
        })
    }

    @SuppressLint("MissingInflatedId")
    private fun setUpEmergencyCodeDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        builder.setTitle("Ingrese el nuevo código de emergencia")

        val dialogLayout = inflater.inflate(R.layout.alert_dialog_custom_view, null)
        val editText  = dialogLayout.findViewById<EditText>(R.id.editText)

        builder.setView(dialogLayout)
        builder.setPositiveButton("Actualizar") { dialogInterface, i ->
            val newCode = editText.text.toString()
            if (newCode.isNotEmpty()) {
                val uid = FirebaseAuth.getInstance().currentUser?.uid!!
                val databaseRef = FirebaseDatabase.getInstance().getReference("Users/$uid/emergencyCode")
                databaseRef.setValue(newCode.toInt()).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Check if the user is still signed in
                        val user = FirebaseAuth.getInstance().currentUser
                        if (user == null) {
                            // Re-authenticate the user or redirect to login screen
                        }
                    } else {
                        Toast.makeText(this@MenuNavigationActivity, "Error al actualizar el código de emergencia", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this@MenuNavigationActivity, "El código de emergencia no puede estar vacío", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancelar") { dialogInterface, i -> }
        builder.show()
    }

    override fun onResume() {
        super.onResume()
        reloadImageFromFirebase()
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

    private fun reloadImageFromFirebase() {
        // Get the uid of the current user and search for the corresponding image
        val uid = FirebaseAuth.getInstance().currentUser?.uid!!
        val storageRef = FirebaseStorage.getInstance().reference.child("pfp/$uid")
        val tempFile = File.createTempFile(uid, "jpg")

        storageRef.getFile(tempFile).addOnSuccessListener {
            val imageUri = Uri.fromFile(tempFile)
            binding.pfpButton.setImageURI(imageUri)
        }
    }
}