package com.example.azalea.adapters

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CursorAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.azalea.R
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class AddEmergencyContactsAdapter(context: Context?, c: Cursor?, flags: Int) : CursorAdapter(context, c, flags) {

    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
        return LayoutInflater.from(context)
            .inflate(R.layout.layout_add_emergency_contacts, parent, false)
    }

    override fun bindView(view: View?, context: Context?, cursor: Cursor?) {
        val uid = cursor?.getString(1) ?: return
        val nombre = cursor?.getString(2) ?: ""
        val correo = cursor?.getString(3) ?: ""
        val availability = cursor?.getString(4) ?: "false"

        loadImage(uid) { file ->
            val txtName = view?.findViewById<TextView>(R.id.nameContactEmergency)
            val txtEmail = view?.findViewById<TextView>(R.id.emailAddContactEmergency)
            val imgAvailability = view?.findViewById<ImageView>(R.id.availabilityAddContact)
            val imgProfile = view?.findViewById<ImageView>(R.id.imgContact)
            val btnAddContact = view?.findViewById<Button>(R.id.btnAddContactList)

            txtName?.text = nombre
            txtEmail?.text = correo
            Log.i("AddEmergencyContactsAdapter", "Availability: $availability")
            if (availability.equals("true")) {
                imgAvailability?.setImageResource(android.R.drawable.presence_online)
            } else {
                imgAvailability?.setImageResource(android.R.drawable.presence_busy)
            }
            imgProfile?.setImageURI(Uri.fromFile(file))

            btnAddContact?.setOnClickListener {
                // TODO Add contact to emergency contacts of the current user
            }
        }
    }

    private fun loadImage(uid: String, onDataLoaded: (File) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference.child("pfp/$uid")
        val tempFile = File.createTempFile(uid, "jpg")

        storageRef.getFile(tempFile).addOnSuccessListener {
            onDataLoaded(tempFile)
        }.addOnFailureListener() {
            // Use default image from resources drawable named pfp
            onDataLoaded(File.createTempFile("pfp", "jpg"))
        }
    }
}