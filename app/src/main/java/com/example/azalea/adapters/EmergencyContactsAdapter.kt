package com.example.azalea.adapters

import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.TextView
import com.example.azalea.R

class EmergencyContactsAdapter(context: Context?, c: Cursor?, flags: Int) : CursorAdapter(context, c, flags) {

    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
        return LayoutInflater.from(context)
            .inflate(R.layout.layout_emergency_contacts, parent, false)
    }

    override fun bindView(view: View?, context: Context?, cursor: Cursor?) {
        val txtName = view?.findViewById<TextView>(R.id.nameContactEmergency)
        val txtNumber = view?.findViewById<TextView>(R.id.emailProfileTextView)
        // Extract properties from cursor
        val nombre = cursor?.getString(1)
        val correo = cursor?.getString(2)

        txtName?.text = nombre
        txtNumber?.text = correo
    }
}