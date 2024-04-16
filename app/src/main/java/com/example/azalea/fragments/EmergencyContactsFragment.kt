package com.example.azalea.fragments

import android.content.Intent
import android.database.Cursor
import android.database.MatrixCursor
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.azalea.R
import com.example.azalea.activities.RegistrarContactoActivity
import com.example.azalea.adapters.EmergencyContactsAdapter
import com.example.azalea.databinding.FragmentEmergencyContactsBinding

class EmergencyContactsFragment : Fragment() {
    private var _binding: FragmentEmergencyContactsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmergencyContactsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpListView()
        setUpButtons()
    }

    private fun loadContacts(): Cursor{
        // TODO Load contacts from the database
        val cursor = MatrixCursor(arrayOf("_id", "name", "phone"))
        cursor.addRow(arrayOf("0", "Alba Milena", "3001234567"))
        cursor.addRow(arrayOf("0", "Nahiara Majanaim", "3007654321"))
        return cursor
    }

    private fun setUpListView(){
        val cursor = loadContacts()
        val adapter = EmergencyContactsAdapter(requireContext(), cursor, 0)
        binding.listViewEmergencyContacts.adapter = adapter

        binding.listViewEmergencyContacts.setOnItemClickListener { _, _, position, _ ->
            cursor.moveToPosition(position)
            Toast.makeText(requireContext(), "Este es un contacto", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setUpButtons(){
        binding.buttonAddContact.setOnClickListener {
            val intent = Intent(requireActivity(), RegistrarContactoActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}