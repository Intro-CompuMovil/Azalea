package com.example.azalea.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.azalea.R
import com.example.azalea.data.User
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class AddEmergencyContactsAdapter(private var userListAdapter: MutableMap<String, User>) : RecyclerView.Adapter<AddEmergencyContactsAdapter.ViewHolder>() {
    private lateinit var mListener: onItemClickListener

    interface onItemClickListener{
        fun onItemClick(position: Int, userListAdapter: MutableMap<String, User>)
    }

    fun setOnItemClickListener(clickListener: onItemClickListener){
        mListener = clickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_add_emergency_contacts, parent, false)
        return ViewHolder(view, mListener, this)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Get information from different users
        val currentUser = userListAdapter.values.elementAt(position)
        holder.nameContactEmergency.text = currentUser.name
        holder.emailContactEmergency.text = currentUser.email
        holder.availabilityContactEmergency.setImageResource(if (currentUser.available) android.R.drawable.presence_online else android.R.drawable.presence_offline)

        // Load image from Firebase Storage
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val imageRef = storageRef.child("pfp/${userListAdapter.keys.elementAt(position)}")
        val localFile = File.createTempFile("pfp${userListAdapter.keys.elementAt(position)}", "jpg")
        imageRef.getFile(localFile).addOnSuccessListener {
            val bitmap = android.graphics.BitmapFactory.decodeFile(localFile.absolutePath)
            holder.imageContactEmergency.setImageBitmap(bitmap)
        }.addOnFailureListener {
            holder.imageContactEmergency.setImageResource(R.drawable.pfp)
        }
    }

    override fun getItemCount(): Int {
        return userListAdapter.size
    }

    fun removeItem(position: Int) {
        userListAdapter -= userListAdapter.keys.elementAt(position)
        notifyItemRemoved(position)
    }

    fun updateList(newList: MutableMap<String, User>) {
        userListAdapter = newList
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View, clickListener: onItemClickListener, private val adapter: AddEmergencyContactsAdapter) : RecyclerView.ViewHolder(itemView) {
        val nameContactEmergency: TextView = itemView.findViewById(R.id.nameAddContactEmergency)
        val emailContactEmergency: TextView = itemView.findViewById(R.id.emailAddContactEmergency)
        val imageContactEmergency: ImageView = itemView.findViewById(R.id.imgAddContact)
        val availabilityContactEmergency: ImageView = itemView.findViewById(R.id.availabilityAddContact)
        private val addContactButton: Button = itemView.findViewById(R.id.btnAddContactList)


        init {
            addContactButton.setOnClickListener {
                clickListener.onItemClick(adapterPosition, adapter.userListAdapter)
                // Remove itself from the list
                adapter.removeItem(adapterPosition)
            }
        }
    }
}