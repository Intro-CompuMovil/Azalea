package com.example.azalea.fragments

import android.content.Intent
import android.database.Cursor
import android.database.MatrixCursor
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.azalea.R
import com.example.azalea.activities.MapsOSMActivity
import com.example.azalea.activities.MenuNavigationActivity
import com.example.azalea.adapters.EmergencyContactsAdapter
import com.example.azalea.data.User
import com.example.azalea.databinding.FragmentEmergencyAlertsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EmergencyAlertsFragment : Fragment() {
    private var _binding: FragmentEmergencyAlertsBinding? = null
    private val binding get() = _binding
    private val userList = mutableMapOf<String, User>()
    private var mAdapter: EmergencyContactsAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmergencyAlertsBinding.inflate(inflater, container, false)
        return binding?.root ?: View(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getCurrentContactsFor()
    }

    private fun getCurrentContactsFor() {
        binding?.recyclerViewEmergencyContactFor?.visibility ?: android.view.View.GONE
        // Get users current contacts to avoid adding them again
        val databaseRefCurrentContacts = FirebaseDatabase.getInstance().getReference("Users/${FirebaseAuth.getInstance().currentUser?.uid}")

        // Change it so its a on value change event listener so every time a contact is added it updates the list
        databaseRefCurrentContacts.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentUser = snapshot.getValue(User::class.java)
                currentUser?.let {
                    getContactInformation(currentUser)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RegistrarContactoActivity", "Error loading current contacts", error.toException())
            }
        })

        setUpRecyclerView()
    }

    private fun getContactInformation(currentUser: User) {
        // Get reference for all Users and only add the ones that are not already in the current user's contacts
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
                        if (currentUser.emergencyContactFor.contains(userSnapshot.key.toString()) && user.emergency) {
                            userList[userSnapshot.key.toString()] = it
                        }
                    }
                }
                setUpRecyclerView()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RegistrarContactoActivity", "Error loading contacts", error.toException())
            }
        })
    }

    private fun setUpRecyclerView() {
        binding?.let { b ->
            b.recyclerViewEmergencyContactFor.layoutManager = LinearLayoutManager(requireContext())
            mAdapter = EmergencyContactsAdapter(userList)
            b.recyclerViewEmergencyContactFor.adapter = mAdapter

            mAdapter!!.setOnItemClickListener(object : EmergencyContactsAdapter.onItemClickListener {
                override fun onItemClick(position: Int, userListAdapter: MutableMap<String, User>) {
                    val uid = userListAdapter.keys.elementAt(position)
                    val intent = Intent(context, MapsOSMActivity::class.java)
                    intent.putExtra("uid", uid)
                    startActivity(intent)
                }
            })

            b.recyclerViewEmergencyContactFor.visibility = android.view.View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}