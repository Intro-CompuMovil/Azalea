package com.example.azalea.adapters

import android.content.Context
import android.database.Cursor
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CursorAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.azalea.R
import com.example.azalea.data.User
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class EmergencyContactsAdapter(private var userListAdapter: MutableMap<String, User>, private var isForEmergency: Boolean = false) : RecyclerView.Adapter<EmergencyContactsAdapter.ViewHolder>() {
    private lateinit var mListener: onItemClickListener
    private var geocoder: Geocoder? = null

    interface onItemClickListener{
        fun onItemClick(position: Int, userListAdapter: MutableMap<String, User>)
    }

    fun setOnItemClickListener(clickListener: onItemClickListener){
        mListener = clickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_emergency_contact, parent, false)
        return ViewHolder(view, mListener, this)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Get information from different users
        val currentUser = userListAdapter.values.elementAt(position)
        holder.nameContactEmergency.text = currentUser.name
        // If is for emergency extra is populated with current location
        if (!isForEmergency) {
            holder.extraContactEmergency.text = currentUser.email
        } else {
            // Get user location and with geoCoder get the address
            val location = currentUser.location
            val latitud = location.split(",")[0].toDouble()
            val longitud = location.split(",")[1].toDouble()

            if (geocoder == null) geocoder = Geocoder(holder.itemView.context)
            val locationName = geocoder!!.getFromLocation(latitud, longitud, 1)?.get(0)
                ?.getAddressLine(0)
            holder.extraContactEmergency.text = locationName
        }
        if(!isForEmergency) holder.availabilityContactEmergency.setImageResource(if (currentUser.available) android.R.drawable.presence_online else android.R.drawable.presence_busy)

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

    inner class ViewHolder(itemView: View, clickListener: onItemClickListener, private val adapter: EmergencyContactsAdapter) : RecyclerView.ViewHolder(itemView) {
        val nameContactEmergency: TextView = itemView.findViewById(R.id.nameEmergencyContact)
        val extraContactEmergency: TextView = itemView.findViewById(R.id.extraEmergencyContact)
        val imageContactEmergency: ImageView = itemView.findViewById(R.id.imgEmergencyContact)
        val availabilityContactEmergency: ImageView = itemView.findViewById(R.id.availabilityEmergencyContact)

        init {
            itemView.setOnClickListener {
                clickListener.onItemClick(adapterPosition, adapter.userListAdapter)
            }
        }
    }
}