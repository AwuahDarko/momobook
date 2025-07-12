package com.presetschool.momobookapp.ui.home

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.presetschool.momobookapp.R
import com.presetschool.momobookapp.adapter.MessageItemAdapter
import com.presetschool.momobookapp.databinding.FragmentHomeBinding
import com.presetschool.momobookapp.model.Message
import com.presetschool.momobookapp.model.MessageSentEvent
import com.presetschool.momobookapp.model.MessageType
import com.presetschool.momobookapp.service.SharedPreferencesHelper
import com.presetschool.momobookapp.service.SmsReader
import com.presetschool.momobookapp.service.Utils
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.streams.asSequence

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MessageItemAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var btnSelectDate: Button
    private lateinit var btnReset: Button
    private val itemList = ArrayList<Message>()
    private var selectedDate: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        recyclerView = binding.recyclerView
        swipeRefreshLayout = binding.swipeRefreshLayout
        btnSelectDate = binding.btnSelectDate
        btnReset = binding.btnReset
        recyclerView.layoutManager = LinearLayoutManager(this.requireContext())

        setupSwipeRefresh()
        // Prepare data
        getAllMessages()

        // Initialize adapter
        adapter = MessageItemAdapter(itemList) { item ->
            // Handle item click
            Toast.makeText(this.requireContext(), "${item.body}", Toast.LENGTH_SHORT).show()
        }

        btnSelectDate.setOnClickListener {
            showDatePicker()
        }

        btnReset.setOnClickListener {
            resetFilter()
        }

        // Set adapter to RecyclerView
        recyclerView.adapter = adapter
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getAllMessages() {
        // Add sample data
        itemList.clear()
        itemList.addAll(readMessages())

    }

    private fun readMessages(): List<Message>{
        val sharedPreference = SharedPreferencesHelper(this.requireContext())

        val originalDateStr = sharedPreference.getString("start_date")

        val list = originalDateStr.split("-")
        val filterDate = "${list[2]}/${list[1]}/${list[0]}"

//        val filterDate = "01/02/2025" // 🟢 Change this to the desired date (dd/MM/yyyy)
        return SmsReader.readSms(this.requireContext(), filterDate)
            .asSequence().filter{ Utils.messageType(it) == MessageType.EXPENSE || Utils.messageType(it) == MessageType.INCOME }.toList()
//        val smsList = SmsReader.readSms(this.requireContext(), filterDate)

//        for ( m in smsList){
//        }

//        val messages = if (smsList.isNotEmpty()) {
//            smsList.joinToString("\n\n")
//        } else {
//            "No SMS messages from VANY after $filterDate."
//        }

    }

    private fun setupSwipeRefresh() {
        // Set refresh indicator colors
        swipeRefreshLayout.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )

        // Set refresh listener
        swipeRefreshLayout.setOnRefreshListener {
            // Refresh data
            refreshData()
        }
    }

    private fun refreshData() {
        // Load fresh data from database
        getAllMessages()

        // Update adapter
        adapter.updateData(itemList)

        // Stop refresh animation
        swipeRefreshLayout.isRefreshing = false

        // Show a confirmation message
        Snackbar.make(recyclerView, "Data refreshed", Snackbar.LENGTH_SHORT).show()
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            ContextThemeWrapper(this.requireContext(), R.style.CustomDatePickerTheme),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(selectedYear, selectedMonth, selectedDay)

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                selectedDate = dateFormat.format(selectedCalendar.time)

                filterByDate(selectedDate!!)
            },
            year, month, day
        )

        datePickerDialog.setOnShowListener {
            val positiveButton = datePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE)
            val negativeButton = datePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE)

            positiveButton.setTextColor(ContextCompat.getColor(this.requireContext(), R.color.purple_500))
            negativeButton.setTextColor(ContextCompat.getColor(this.requireContext(), R.color.error_color))
        }

        datePickerDialog.show()
    }

    private fun filterByDate(date: String) {
        adapter.filterByDate(date)

        btnReset.visibility = View.VISIBLE
    }

    private fun resetFilter() {
        adapter.resetFilter()
        selectedDate = null

        btnReset.visibility = View.GONE
    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDataChangedEvent(event: MessageSentEvent) {
        // Refresh your RecyclerView
        refreshData()
    }
}