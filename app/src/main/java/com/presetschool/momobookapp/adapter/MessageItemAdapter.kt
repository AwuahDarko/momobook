package com.presetschool.momobookapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.presetschool.momobookapp.R
import com.presetschool.momobookapp.model.Message
import com.presetschool.momobookapp.model.MessageType
import com.presetschool.momobookapp.service.Utils

class MessageItemAdapter (
    private var items: List<Message>,
    private val onItemClick: (Message) -> Unit
) : RecyclerView.Adapter<MessageItemAdapter.ViewHolder>() {

    // ViewHolder pattern
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val senderTxt: TextView = itemView.findViewById(R.id.senderTxt)
        val bodyTxt: TextView = itemView.findViewById(R.id.bodyTxt)
        val amtTxt: TextView = itemView.findViewById(R.id.amtTxt)
        val refTxt: TextView = itemView.findViewById(R.id.refTxt)
        val transTxt: TextView = itemView.findViewById(R.id.transTxt)
        val typeTxt: TextView = itemView.findViewById(R.id.typeTxt)
        val dateTxt: TextView = itemView.findViewById(R.id.dateTxt)
    }

    // Create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.sms_list_item, parent, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        val messageType = if (Utils.messageType(item) == MessageType.INCOME) "INCOME" else if(Utils.messageType(item) == MessageType.EXPENSE) "EXPENSE" else "NONE"

        holder.senderTxt.text = Utils.extractSender(item)
        holder.bodyTxt.text = item.body
        holder.amtTxt.text = if (messageType == "NONE") "" else "GHS ${Utils.extractAmount(item)}"
        holder.refTxt.text = Utils.extractRef(item)
        holder.transTxt.text = Utils.extractTransactionId(item)
        holder.transTxt.text = Utils.extractTransactionId(item)
        holder.dateTxt.text = item.displayDate
        holder.typeTxt.text = messageType


        // Set click listener for the item
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    // Return the size of your dataset
    override fun getItemCount() = items.size

    fun updateData(itemList: List<Message>) {
        items = itemList
        notifyDataSetChanged()
    }


}
