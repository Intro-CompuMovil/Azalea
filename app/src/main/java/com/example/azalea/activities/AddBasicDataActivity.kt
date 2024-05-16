package com.example.azalea.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import com.example.azalea.R
import com.example.azalea.databinding.ActivityAddBasicDataBinding

class AddBasicDataActivity : AppCompatActivity() {
    lateinit var binding: ActivityAddBasicDataBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBasicDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpEditTexts()
        setUpButtons()
    }

    override fun onResume() {
        super.onResume()
        setEditTextsValues()
    }

    private fun setUpEditTexts() {
        binding.dateEditTextProfile.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = "${selectedDay}/${selectedMonth + 1}/${selectedYear}"
                    binding.dateEditTextProfile.setText(selectedDate)
                },
                year,
                month,
                day
            )

            datePickerDialog.show()
        }

        val bloodTypes = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
        // Set up autocomplete text view
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, bloodTypes)
        binding.spinnerAutoCompleteTextView.setAdapter(adapter)
    }

    private fun setUpButtons() {
        binding.goBackButtonLayoutProfile.setOnClickListener {
            val intent = Intent(this, PerfilActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setEditTextsValues() {
        // TODO: Set values from firebase database
    }
}