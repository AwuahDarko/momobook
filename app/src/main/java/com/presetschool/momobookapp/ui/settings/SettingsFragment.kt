package com.presetschool.momobookapp.ui.settings

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.presetschool.momobookapp.databinding.FragmentSettingsBinding
import com.presetschool.momobookapp.service.SharedPreferencesHelper
import java.time.LocalDate


class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var datePickerDialog: DatePickerDialog
    private lateinit var dateButton: Button
    private lateinit var sharePreference: SharedPreferencesHelper
    private lateinit var saveBtn: Button
    private var setDate: LocalDate? = null
    private lateinit var delayTextEdit: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val settingsViewModel =
            ViewModelProvider(this).get(SettingsViewModel::class.java)

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        sharePreference = SharedPreferencesHelper(this.requireContext())

//        val textView: TextView = binding.textNotifications
//        notificationsViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }

//        val datePickerDialog: DatePickerDialog = binding.datePickerButton
        dateButton = binding.datePickerButton
        dateButton.setOnClickListener {
            openDatePicker(root)
        }
        dateButton.text = getSetdate()
        saveBtn = binding.saveBtn

        saveBtn.setOnClickListener {
            val delay: String = delayTextEdit.text.trim().toString()
            sharePreference.saveString("start_date", setDate.toString())
            sharePreference.saveInt("delay_time", delay.toInt())

            Toast.makeText(this.requireContext(), "Saved successfully", Toast.LENGTH_SHORT).show()
        }

        delayTextEdit = binding.delayTextEdit
        delayTextEdit.setText(sharePreference.getInt("delay_time").toString())

        initDatePicker()
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getSetdate(): String {
        val localDate: LocalDate = LocalDate.parse(sharePreference.getString("start_date"))
        val year = localDate.year
        val month = localDate.dayOfMonth
        val day = localDate.dayOfMonth

//        val cal: Calendar = Calendar.getInstance()
//        val year: Int = cal.get(Calendar.YEAR)
//        var month: Int = cal.get(Calendar.MONTH)
//        month += 1
//        val day: Int = cal.get(Calendar.DAY_OF_MONTH)
        return makeDateString(day, month, year)
    }

    private fun makeDateString(day: Int, month: Int, year: Int): String {
        return getMonthFormat(month) + " " + day + " " + year
    }

    private fun getMonthFormat(month: Int): String {
        if (month == 1) return "JAN"
        if (month == 2) return "FEB"
        if (month == 3) return "MAR"
        if (month == 4) return "APR"
        if (month == 5) return "MAY"
        if (month == 6) return "JUN"
        if (month == 7) return "JUL"
        if (month == 8) return "AUG"
        if (month == 9) return "SEP"
        if (month == 10) return "OCT"
        if (month == 11) return "NOV"
        if (month == 12) return "DEC"

        //default should never happen
        return "JAN"
    }

    private fun openDatePicker(view: View?) {
        datePickerDialog.show()
    }

    private fun initDatePicker() {
        val dateSetListener = OnDateSetListener { datePicker, year, month, day ->
            var month = month
            month += 1
            val date = makeDateString(day, month, year)
            dateButton.text = date
            setDate = LocalDate.of(year, month, day)
        }

        val localDate: LocalDate = LocalDate.parse(sharePreference.getString("start_date"))
        val year = localDate.year
        val month = localDate.monthValue
        val day = localDate.dayOfMonth

        setDate = LocalDate.of(year, month, day)


        val style: Int = AlertDialog.THEME_HOLO_LIGHT

        datePickerDialog = DatePickerDialog(this.requireContext(), style, dateSetListener, year, month, day)

        //datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
    }
}