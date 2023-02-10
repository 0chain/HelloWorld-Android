package org.zus.bolt.helloworld.ui.bolt

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.zus.bolt.helloworld.R
import org.zus.bolt.helloworld.models.bolt.TransactionModel
import org.zus.bolt.helloworld.utils.Utils.Companion.getConvertedDateTime

class TransactionsAdapter(
    var context: Context,
    var transactions: List<TransactionModel>,
) : RecyclerView.Adapter<TransactionsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNumber: TextView
        val tvHash: TextView
        val tvDateTime: TextView

        init {
            tvNumber = view.findViewById(R.id.tv_number)
            tvHash = view.findViewById(R.id.tv_hash)
            tvDateTime = view.findViewById(R.id.tv_date_time)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.row_wallet_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvNumber.text = (position + 1).toString()
        holder.tvHash.text = transactions[position].hash.substring(0, 20)
        holder.tvDateTime.text = transactions[position].creation_date.getConvertedDateTime()
    }

    override fun getItemCount() = transactions.size
}
