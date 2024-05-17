package com.example.azalea.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.MatrixCursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import com.example.azalea.R
import com.example.azalea.adapters.AddEmergencyContactsAdapter
import com.example.azalea.adapters.EmergencyContactsAdapter
import com.example.azalea.data.PermissionsCodes.Companion.CONTACTS_PERMISSION_CODE
import com.example.azalea.databinding.ActivityRegistrarContactoBinding
import com.google.firebase.database.FirebaseDatabase

class RegistrarContactoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistrarContactoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrarContactoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpListView()
        // TODO Add option to search for contacts with their emails (filter the users)
    }

    private fun loadContacts(onDataLoaded: (Cursor) -> Unit) {
        val cursor = MatrixCursor(arrayOf("_id", "uid", "name", "email", "availability"))

        val databaseRef = FirebaseDatabase.getInstance().getReference("Users")

        databaseRef.get().addOnSuccessListener {
            for (data in it.children) {
                // TODO Check if the contact is already in the emergency contacts or if it is the current user
                val uid = data.key
                val name = data.child("name").value.toString()
                val email = data.child("email").value.toString()
                val availability = data.child("available").value.toString()
                cursor.addRow(arrayOf("0", uid, name, email, availability))
            }
            onDataLoaded(cursor)
        }.addOnFailureListener {
            Toast.makeText(this, "Error al cargar los contactos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setUpListView(){
        loadContacts { cursor ->
            val adapter = AddEmergencyContactsAdapter(this, cursor, 0)
            binding.listViewAddEmergencyContacts.adapter = adapter
            adapter.notifyDataSetChanged()
        }
    }
}