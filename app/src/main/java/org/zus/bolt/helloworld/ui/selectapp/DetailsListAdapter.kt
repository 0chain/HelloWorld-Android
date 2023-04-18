package org.zus.bolt.helloworld.ui.selectapp

import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.text.SpannableString
import android.text.SpannedString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import org.zus.bolt.helloworld.R
import org.zus.bolt.helloworld.databinding.DialogItemViewBinding
import org.zus.bolt.helloworld.databinding.RowDetailsBinding
import org.zus.bolt.helloworld.models.selectapp.DetailsModel
import org.zus.bolt.helloworld.ui.TAG_CREATE_WALLET
import org.zus.bolt.helloworld.utils.Utils.Companion.isValidJson
import org.zus.bolt.helloworld.utils.Utils.Companion.isValidUrl
import org.zus.bolt.helloworld.utils.Utils.Companion.prettyJsonFormat

class DetailsListAdapter(
    private val fragmentActivity: FragmentActivity,
    private val detailsList: List<DetailsModel>,
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
        rowDetailsBinding.tvDetailTitle.text = detailsList[position].title

        rowDetailsBinding.rowDetailsRoot.setOnLongClickListener {
            val clipboard =
                fragmentActivity.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(
                android.content.ClipData.newPlainText(
                    detailsList[position].title,
                    detailsList[position].value
                )
            )
            Toast.makeText(
                fragmentActivity,
                "${detailsList[position].title} Copied to clipboard",
                Toast.LENGTH_SHORT
            ).show()
            true
        }

        if (detailsList[position].value.isValidUrl()) {
            val text = SpannableString(detailsList[position].title).apply {
                if (detailsList[position].title.isValidUrl()) {
                    rowDetailsBinding.tvDetailTitle.setTextColor(
                        ContextCompat.getColor(
                            fragmentActivity,
                            R.color.color_url
                        )
                    )
                } else {
                    val colonIndex = detailsList[position].title.indexOf(":")
                    setSpan(
                        ForegroundColorSpan(
                            ContextCompat.getColor(
                                fragmentActivity,
                                R.color.color_url
                            )
                        ),
                        colonIndex,
                        detailsList[position].title.length,
                        SpannedString.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }

            rowDetailsBinding.tvDetailTitle.text = text

            rowDetailsBinding.rowDetailsRoot.setOnClickListener {
                val openUrl = android.content.Intent(android.content.Intent.ACTION_VIEW)
                openUrl.data = android.net.Uri.parse(detailsList[position].value)
                fragmentActivity.startActivity(openUrl)
            }
        }

        if (detailsList[position].showArrowButton) {
            rowDetailsBinding.ibnArrowDetails.visibility = View.VISIBLE

            rowDetailsBinding.rowDetailsRoot.setOnClickListener {
                val detailsBottomSheetDialogFragment =
                    DetailsBottomSheetFragment(detailsList[position])
                detailsBottomSheetDialogFragment.show(
                    fragmentActivity.supportFragmentManager,
                    "detailsBottomSheetDialogFragment"
                )
            }
        } else {
            rowDetailsBinding.ibnArrowDetails.visibility = View.GONE
        }

        return rowDetailsBinding.root
    }
}
