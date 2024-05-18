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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.azalea.R
import com.example.azalea.adapters.AddEmergencyContactsAdapter
import com.example.azalea.adapters.EmergencyContactsAdapter
import com.example.azalea.data.PermissionsCodes.Companion.CONTACTS_PERMISSION_CODE
import com.example.azalea.data.User
import com.example.azalea.databinding.ActivityRegistrarContactoBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RegistrarContactoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistrarContactoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrarContactoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadContacts()
        //setUpSearch()
        // TODO Add option to search for contacts with their emails (filter the users)
    }

    private fun loadContacts() {
        binding.recyclerViewAddContact.visibility = android.view.View.GONE

        val listUsers = arrayListOf<User>()
        val listUids = arrayListOf<String>()
        val databaseRef = FirebaseDatabase.getInstance().getReference("Users")

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listUsers.clear()
                if(!snapshot.exists()){
                    return
                }
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    user?.let {
                        listUsers.add(it)
                    }
                    listUids.add(userSnapshot.key.toString())
                }
                binding.recyclerViewAddContact.layoutManager = LinearLayoutManager(this@RegistrarContactoActivity)
                val mAdapter = AddEmergencyContactsAdapter(listUsers, listUids)
                binding.recyclerViewAddContact.adapter = mAdapter

                mAdapter.setOnItemClickListener(object : AddEmergencyContactsAdapter.onItemClickListener {
                    override fun onItemClick(position: Int) {
                        // TODO Add contact to the emergency contacts list
                        Toast.makeText(this@RegistrarContactoActivity, "Contact ${listUsers[position].name} added", Toast.LENGTH_SHORT).show()
                    }
                })

                binding.recyclerViewAddContact.visibility = android.view.View.VISIBLE
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RegistrarContactoActivity", "Error loading contacts", error.toException())
            }
        })

    }
}