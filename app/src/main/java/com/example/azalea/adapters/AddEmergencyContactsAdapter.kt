package com.example.azalea.adapters

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.CursorAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.azalea.R
import com.example.azalea.data.User
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class AddEmergencyContactsAdapter(private val userList: ArrayList<User>, private val uidList: ArrayList<String>) : RecyclerView.Adapter<AddEmergencyContactsAdapter.ViewHolder>() {
    private lateinit var mListener: onItemClickListener

    interface onItemClickListener{
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(clickListener: onItemClickListener){
        mListener = clickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_add_emergency_contacts, parent, false)
        return ViewHolder(view, mListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Get information from different users
        val currentUser = userList[position]
        holder.nameContactEmergency.text = currentUser.name
        holder.emailContactEmergency.text = currentUser.email
        holder.availabilityContactEmergency.setImageResource(if (currentUser.available) android.R.drawable.presence_online else android.R.drawable.presence_offline)

        // Load image from Firebase Storage
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val imageRef = storageRef.child("pfp/${uidList[position]}")
        val localFile = File.createTempFile("pfp${uidList[position]}", "jpg")
        imageRef.getFile(localFile).addOnSuccessListener {
            val bitmap = android.graphics.BitmapFactory.decodeFile(localFile.absolutePath)
            holder.imageContactEmergency.setImageBitmap(bitmap)
        }.addOnFailureListener {
            Log.d("AddEmergencyContactsAdapter", "Failed to load image")
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    class ViewHolder(itemView: View, clickListener: onItemClickListener) : RecyclerView.ViewHolder(itemView) {
        val nameContactEmergency: TextView = itemView.findViewById(R.id.nameAddContactEmergency)
        val emailContactEmergency: TextView = itemView.findViewById(R.id.emailAddContactEmergency)
        val imageContactEmergency: ImageView = itemView.findViewById(R.id.imgAddContact)
        val availabilityContactEmergency: ImageView = itemView.findViewById(R.id.availabilityAddContact)
        private val addContactButton: Button = itemView.findViewById(R.id.btnAddContactList)


        init {
            addContactButton.setOnClickListener {
                clickListener.onItemClick(adapterPosition)
            }
        }
    }
}