package com.presetschool.momobookapp.adapter

import android.content.Context
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.presetschool.momobookapp.R
import com.presetschool.momobookapp.model.Message
import com.presetschool.momobookapp.model.MessageType
import com.presetschool.momobookapp.model.SmsRequest
import com.presetschool.momobookapp.service.Utils

class PendingMessageItemAdapter(
    private var items: List<Message>,
    private val onSuccess: (Message, String) -> Unit,
    private val onFailure: (Message) -> Unit,
    private val onLoading: () -> Unit,
    private val onItemClicked: (Message) -> Unit
) : RecyclerView.Adapter<PendingMessageItemAdapter.ViewHolder>() {

    // ViewHolder pattern
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val senderTxt: TextView = itemView.findViewById(R.id.senderTxt)
        val bodyTxt: TextView = itemView.findViewById(R.id.bodyTxt)
        val amtTxt: TextView = itemView.findViewById(R.id.amtTxt)
        val refTxt: TextView = itemView.findViewById(R.id.refTxt)
        val transTxt: TextView = itemView.findViewById(R.id.transTxt)
        val typeTxt: TextView = itemView.findViewById(R.id.typeTxt)
        val dateTxt: TextView = itemView.findViewById(R.id.dateTxt)
        val sendBtn: Button = itemView.findViewById(R.id.sendBtn)
        val mrefTxt: TextInputEditText = itemView.findViewById(R.id.mrefTxt)
        val checkbox: CheckBox = itemView.findViewById(R.id.checkbox)
        val usageTxt: TextInputEditText = itemView.findViewById(R.id.usageTxt)

    }

    // Create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.pending_sms_list_item, parent, false)
        return ViewHolder(view)
    }


    // Replace the contents of a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        val type = if (Utils.messageType(item) == MessageType.INCOME) "INCOME" else if(Utils.messageType(item) == MessageType.EXPENSE) "EXPENSE" else "NONE"

        val amt: String = Utils.extractAmount(item)
        val sender: String = Utils.extractSender(item)
        val transactionId: String = Utils.extractTransactionId(item)
        val apiDate: String = item.apiDate
        val ref: String = Utils.extractRef(item)

        holder.senderTxt.text = sender
        holder.bodyTxt.text = item.body
        holder.amtTxt.text = if (type == "NONE") "" else "GHS $amt"
        holder.refTxt.text = ref
        holder.transTxt.text = transactionId
        holder.dateTxt.text = item.displayDate
        holder.typeTxt.text = type
        holder.checkbox.isChecked = true
        holder.usageTxt.setText("1")

        holder.mrefTxt.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {

                holder.mrefTxt.clearFocus()
                hideKeyboard(holder.mrefTxt)
                true
            } else {
                false
            }
        }


        holder.usageTxt.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {

                holder.usageTxt.clearFocus()
                hideKeyboard(holder.usageTxt)
                true
            } else {
                false
            }
        }


        holder.sendBtn.setOnClickListener {
            // TODO ==== call
            val mref = holder.mrefTxt.text?.trim().toString()
            val use = holder.usageTxt.text?.trim().toString()
            val postRequest = SmsRequest(
                datetime = apiDate,
                message = item.body,
                ref = ref,
                mref = mref,
                transactionId = transactionId,
                sender = sender,
                id = item.id.toInt(),
                type = type,
                amount = amt,
                includeInAccount = if( holder.checkbox.isChecked ) 1 else 0,
                useTimes = use,
                from =  "preset"
            )

            onLoading()


            Utils.sendPostRequest(postRequest,
                success = { msg ->
                    onSuccess(item, mref)
                }, failure = { msg ->
                    onFailure(item)
                })


//            Snackbar.make(holder.sendBtn, "", Snackbar.LENGTH_SHORT).show()

        }


        // Set click listener for the item
        holder.itemView.setOnClickListener {
            onItemClicked(item)
        }
    }

    private fun hideKeyboard(editText: EditText) {
        val imm = editText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    // Return the size of your dataset
    override fun getItemCount() = items.size

    fun updateData(itemList: List<Message>) {
        items = itemList
        notifyDataSetChanged()
    }

    interface PendingMessageListener {
        fun onRefresh()
    }
}