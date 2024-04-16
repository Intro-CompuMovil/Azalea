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
import com.example.azalea.activities.MapsOSMActivity
import com.example.azalea.activities.MenuNavigationActivity
import com.example.azalea.adapters.EmergencyContactsAdapter
import com.example.azalea.databinding.FragmentEmergencyAlertsBinding

class EmergencyAlertsFragment : Fragment() {
    private var _binding: FragmentEmergencyAlertsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmergencyAlertsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpListView()
    }

    private fun loadAlerts(): Cursor{
        // TODO Load alerts from the database
        val cursor = MatrixCursor(arrayOf("_id", "name", "location"))
        cursor.addRow(arrayOf("0", "Mandela Doe", "4.722842741458784, -74.03903236148713"))
        return cursor
    }

    private fun setUpListView(){
        // Set up the list view
        val cursor = loadAlerts()
        val adapter = EmergencyContactsAdapter(requireContext(), cursor, 0)
        binding.listViewEmergencyAlerts.adapter = adapter

        binding.listViewEmergencyAlerts.setOnItemClickListener { _, _, position, _ ->
            cursor.moveToPosition(position)
            val latitude = cursor.getString(2).split(",")[0]
            val longitude = cursor.getString(2).split(",")[1]
            val bundle = Bundle()
            bundle.putString("latitude", latitude)
            bundle.putString("longitude", longitude)

            val intent = Intent(requireContext(), MapsOSMActivity::class.java)
            intent.putExtras(bundle)
            startActivity(intent)
        }
    }
}