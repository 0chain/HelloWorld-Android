package org.zus.bolt.helloworld.ui.bolt

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.zus.bolt.helloworld.R
import org.zus.bolt.helloworld.models.TransactionModel
import org.zus.bolt.helloworld.utils.Utils

class TransactionsAdapter(
    var context: Context,
    var transactions: List<TransactionModel>,
) : RecyclerView.Adapter<TransactionsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView

        init {
            textView = view.findViewById(R.id.tv_transactions_rv_item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.row_wallet_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = context.getString(
            /* resId = */
            R.string.transaction_list_item,
            /* position = */
            position + 1,
            /* hash = */
            transactions[position].hash.substring(0, 6),
            /* date time= */
            Utils.getDateTime(transactions[position].creation_date),
        )
    }

    override fun getItemCount() = transactions.size
}
