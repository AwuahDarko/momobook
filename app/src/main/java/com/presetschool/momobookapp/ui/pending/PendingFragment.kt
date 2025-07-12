package com.presetschool.momobookapp.ui.pending

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.presetschool.momobookapp.adapter.MessageItemAdapter
import com.presetschool.momobookapp.adapter.PendingMessageItemAdapter
import com.presetschool.momobookapp.databinding.FragmentPendingBinding
import com.presetschool.momobookapp.model.LocalItem
import com.presetschool.momobookapp.model.Message
import com.presetschool.momobookapp.model.MessageSentEvent
import com.presetschool.momobookapp.model.MessageType
import com.presetschool.momobookapp.service.DatabaseHelper
import com.presetschool.momobookapp.service.SharedPreferencesHelper
import com.presetschool.momobookapp.service.SmsReader
import com.presetschool.momobookapp.service.Utils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class PendingFragment : Fragment() {

    private var _binding: FragmentPendingBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PendingMessageItemAdapter
    private val itemList = ArrayList<Message>()
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val pendingViewModel =
            ViewModelProvider(this).get(PendingViewModel::class.java)

        dbHelper = DatabaseHelper(this.requireContext())

        _binding = FragmentPendingBinding.inflate(inflater, container, false)
        val root: View = binding.root


        recyclerView = binding.recyclerView
        swipeRefreshLayout = binding.swipeRefreshLayout
        recyclerView.layoutManager = LinearLayoutManager(this.requireContext())
        progressBar = binding.progressBar

        setupSwipeRefresh()
        getAllPendingMessages()

        adapter = PendingMessageItemAdapter(itemList,
            onFailure = { item ->
                // Handle refresh
                progressBar.visibility = View.GONE
                Snackbar.make(recyclerView, "Action failed", Snackbar.LENGTH_SHORT).show()
            },
            onSuccess = { item, mref ->
                progressBar.visibility = View.GONE
               dbHelper.addItem( LocalItem(item.id.toString(), mref, item.body))
                refreshData()
            },
            onItemClicked = { item -> // item clicked
                Toast.makeText(this.requireContext(), "${item.body}", Toast.LENGTH_LONG).show()
            },
            onLoading = {
                progressBar.visibility = View.VISIBLE
            })

        recyclerView.adapter = adapter

        EventBus.getDefault().register(this)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        EventBus.getDefault().unregister(this)
    }

    private fun getAllPendingMessages() {
        // Add sample data
        itemList.clear()
        itemList.addAll(readPendingMessages())

    }

    private fun readPendingMessages(): List<Message> {
        val sharedPreference = SharedPreferencesHelper(this.requireContext())

        val originalDateStr = sharedPreference.getString("start_date")

        val list = originalDateStr.split("-")
        val filterDate = "${list[2]}/${list[1]}/${list[0]}"

        val items = dbHelper.getAllItems()
        val ids: ArrayList<String> = ArrayList()
        for (item in items) {
            ids.add(item._id)
        }

        var IDs: String = ids.joinToString(",")


        if (IDs.isEmpty()) IDs = "0"

        return SmsReader.readPendingSms(this.requireContext(), filterDate, IDs)
            .asSequence().filter{ Utils.messageType(it) == MessageType.EXPENSE || Utils.messageType(it) == MessageType.INCOME }.toList()

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
        itemList.clear()
        itemList.addAll(readPendingMessages())

        // Update adapter
        adapter.updateData(itemList)

        // Stop refresh animation
        swipeRefreshLayout.isRefreshing = false

        // Show a confirmation message
        Snackbar.make(recyclerView, "Data refreshed", Snackbar.LENGTH_SHORT).show()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDataChangedEvent(event: MessageSentEvent) {
        // Refresh your RecyclerView
        itemList.clear()
        itemList.addAll(readPendingMessages())
        adapter.updateData(itemList)
    }

}