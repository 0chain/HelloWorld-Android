package org.zus.bolt.helloworld.ui.selectapp

import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.zus.bolt.helloworld.R
import org.zus.bolt.helloworld.databinding.RowDetailsBinding
import org.zus.bolt.helloworld.utils.Utils.Companion.isValidJson
import org.zus.bolt.helloworld.utils.Utils.Companion.isValidUrl

class DetailsListAdapter(
    private val detailsList: List<Pair<String, String>>,
) : BaseAdapter() {
    override fun getCount(): Int = detailsList.size

    override fun getItem(position: Int): Any {
        return detailsList[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    @SuppressLint("RestrictedApi")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowDetailsBinding = RowDetailsBinding.inflate(LayoutInflater.from(parent?.context))
        rowDetailsBinding.tvDetailTitle.text = detailsList[position].first
        rowDetailsBinding.tvDetailValue.text =
            detailsList[position].second
        rowDetailsBinding.tvDetailValue.setAutoSizeTextTypeUniformWithConfiguration(
            8,
            20,
            1,
            TypedValue.COMPLEX_UNIT_SP
        )
        if (detailsList[position].second.isValidUrl()) {
            rowDetailsBinding.tvDetailValue.text = detailsList[position].second
            rowDetailsBinding.tvDetailValue.setTextColor(ContextCompat.getColor(parent!!.context,
                R.color.color_url))
            rowDetailsBinding.tvDetailValue.setOnClickListener {
                val openUrl = Intent(Intent.ACTION_VIEW)
                openUrl.data = android.net.Uri.parse(detailsList[position].second)
                parent.context.startActivity(openUrl)
            }
        } else if (detailsList[position].second.isValidJson()) {
            val prettyWalletJson = GsonBuilder().setPrettyPrinting().create().toJson(
                Gson().fromJson(detailsList[position].second, Any::class.java)
            )
            rowDetailsBinding.tvDetailValue.text = prettyWalletJson
            rowDetailsBinding.tvDetailValue.setOnClickListener {

                Log.i("TAG", "getView: clicking on the button")
                var clipboard =
                    parent!!.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(
                    android.content.ClipData.newPlainText(
                        "wallet json",
                        detailsList[position].second
                    )
                )
            }
        } else if (detailsList[position].first.equals("Allocation ID:")) {
            rowDetailsBinding.tvDetailValue.text = detailsList[position].second
            rowDetailsBinding.tvDetailValue.setTextColor(ContextCompat.getColor(parent!!.context,
                R.color.color_url))
            rowDetailsBinding.tvDetailValue.setOnClickListener {
                val url =
                    "https://demo.atlus.cloud/allocation-details/${detailsList[position].second}"
                val openUrl = Intent(Intent.ACTION_VIEW)
                openUrl.data = android.net.Uri.parse(url)
                parent.context.startActivity(openUrl)
            }
        }
        return rowDetailsBinding.root
    }
}
