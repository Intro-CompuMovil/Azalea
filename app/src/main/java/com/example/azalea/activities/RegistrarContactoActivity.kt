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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RegistrarContactoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistrarContactoBinding
    private val userList = mutableMapOf<String, User>()
    private var mAdapter: AddEmergencyContactsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrarContactoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadContacts()
        setUpSearchUI()
    }

    private fun loadContacts() {
        binding.recyclerViewAddContact.visibility = android.view.View.GONE
        val databaseRef = FirebaseDatabase.getInstance().getReference("Users")

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                if(!snapshot.exists()){
                    return
                }
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    user?.let {
                        if (userSnapshot.key.toString() != FirebaseAuth.getInstance().currentUser?.uid!!) {
                            userList[userSnapshot.key.toString()] = it
                        }
                    }
                }
                removeCurrentContacts()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RegistrarContactoActivity", "Error loading contacts", error.toException())
            }
        })
    }

    private fun removeCurrentContacts() {
        // Get users current contacts to avoid adding them again
        val databaseRefCurrentContacts = FirebaseDatabase.getInstance().getReference("Users/${FirebaseAuth.getInstance().currentUser?.uid}")

        databaseRefCurrentContacts.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentUser = snapshot.getValue(User::class.java)
                if (currentUser != null) {
                    for (contact in currentUser.emergencyContacts) {
                        userList.remove(contact)
                    }
                    setUpRecyclerView()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RegistrarContactoActivity", "Error loading current contacts", error.toException())
            }
        })
    }

    private fun setUpRecyclerView() {
        binding.recyclerViewAddContact.layoutManager = LinearLayoutManager(this@RegistrarContactoActivity)
        mAdapter = AddEmergencyContactsAdapter(userList)
        binding.recyclerViewAddContact.adapter = mAdapter

        mAdapter!!.setOnItemClickListener(object : AddEmergencyContactsAdapter.onItemClickListener {
            override fun onItemClick(position: Int, userListAdapter: MutableMap<String, User>) {
                // Add contact to the user's emergency contacts
                val currentUID = FirebaseAuth.getInstance().currentUser?.uid
                val contactUID = userListAdapter.keys.elementAt(position)
                val databaseRef = FirebaseDatabase.getInstance().getReference("Users")

                databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(!snapshot.exists()){
                            return
                        }
                        for (userSnapshot in snapshot.children) {
                            // If its current user then add emergency contact to the list of emergency contacts
                            // If its the contact user then add current user to the list of emergency contact for
                            if (userSnapshot.key.toString() == currentUID) {
                                val currentUser = userSnapshot.getValue(User::class.java)
                                if (currentUser != null) {
                                    currentUser.emergencyContacts += contactUID
                                    userSnapshot.ref.setValue(currentUser)
                                }
                            }else if (userSnapshot.key.toString() == contactUID) {
                                val contactUser = userSnapshot.getValue(User::class.java)
                                if (contactUser != null) {
                                    contactUser.emergencyContactFor += currentUID!!
                                    userSnapshot.ref.setValue(contactUser)
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("RegistrarContactoActivity", "Error adding contact", error.toException())
                    }
                })

                Toast.makeText(this@RegistrarContactoActivity, "Contact ${userList.values.elementAt(position).name} added", Toast.LENGTH_SHORT).show()
            }
        })

        binding.recyclerViewAddContact.visibility = android.view.View.VISIBLE
    }

    private fun setUpSearchUI() {
        binding.editTextSearchContact.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchSimilarContacts(s.toString())
            }

            override fun afterTextChanged(s: android.text.Editable?) {
                // Do nothing
            }
        })
    }

    private fun searchSimilarContacts(query: String) {
        val filteredList = userList.filterValues { it.email.contains(query, ignoreCase = true) }
        mAdapter?.updateList(filteredList.toMutableMap())
    }
}