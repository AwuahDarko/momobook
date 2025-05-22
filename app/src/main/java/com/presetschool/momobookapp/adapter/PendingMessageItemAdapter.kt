package com.presetschool.momobookapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
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
        val mrefTxt: EditText = itemView.findViewById(R.id.mrefTxt)
        val checkbox: CheckBox = itemView.findViewById(R.id.checkbox)

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


        holder.sendBtn.setOnClickListener {
            // TODO ==== call
            val mref = holder.mrefTxt.text.trim().toString()
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
                includeInAccount = if( holder.checkbox.isChecked ) 1 else 0
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