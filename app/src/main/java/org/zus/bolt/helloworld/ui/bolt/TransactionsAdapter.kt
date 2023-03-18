package org.zus.bolt.helloworld.ui.bolt

import android.content.Context
import android.content.Intent
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
        val tvHash: TextView
        val tvDateTime: TextView

        init {
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
        holder.tvHash.text = buildString {
            append(position + 1)
            append(". ")
            append(
                transactions[position].hash.substring(
                    0,
                    6
                )
            )
            append("...")
            append(
                transactions[position].hash.substring(
                    transactions[position].hash.length - 6,
                    transactions[position].hash.length
                )
            )
        }

        // We are getting the unix timestamp in nano seconds, so we need to divide it by 1000000000 to get the date in seconds
        holder.tvDateTime.text =
            (transactions[position].creation_date / 1000000000).getConvertedDateTime()

        holder.itemView.setOnClickListener {
            val url = "https://demo.atlus.cloud/transaction-details/${transactions[position].hash}"
            val openUrl = Intent(Intent.ACTION_VIEW)
            openUrl.data = android.net.Uri.parse(url)
            context.startActivity(openUrl)
        }
    }

    override fun getItemCount() = transactions.size
}
