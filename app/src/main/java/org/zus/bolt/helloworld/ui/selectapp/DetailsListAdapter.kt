package org.zus.bolt.helloworld.ui.selectapp

import android.annotation.SuppressLint
import android.app.ActionBar.LayoutParams
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.graphics.Typeface
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.marginStart
import androidx.core.view.setMargins
import androidx.fragment.app.FragmentActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textview.MaterialTextView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.zus.bolt.helloworld.R
import org.zus.bolt.helloworld.databinding.DialogItemViewBinding
import org.zus.bolt.helloworld.databinding.RowDetailsBinding
import org.zus.bolt.helloworld.ui.TAG_CREATE_WALLET
import org.zus.bolt.helloworld.utils.Utils.Companion.getShortFormattedString
import org.zus.bolt.helloworld.utils.Utils.Companion.isValidJson
import org.zus.bolt.helloworld.utils.Utils.Companion.isValidUrl

class DetailsListAdapter(
    private val fragmentActivity: FragmentActivity,
    private val detailsList: List<Pair<String, String>>,
) : BaseAdapter() {
    private val customTitle = { position: Int ->
        MaterialTextView(fragmentActivity).apply {
            setText(detailsList[position].first)
            setTextColor(
                ContextCompat.getColor(
                    fragmentActivity,
                    R.color.normal_text_color
                )
            )
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            setTypeface(typeface, Typeface.BOLD)
            val layoutParams = ConstraintLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.apply {
                marginStart = 8
                topMargin = 8
                marginEnd = 8
                bottomMargin = 8
            }
        }
    }

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
            detailsList[position].second.getShortFormattedString()
        rowDetailsBinding.tvDetailValue.setAutoSizeTextTypeUniformWithConfiguration(
            8,
            20,
            1,
            TypedValue.COMPLEX_UNIT_SP
        )

        rowDetailsBinding.root.setOnClickListener {
            Log.i(TAG_CREATE_WALLET, "clicking on the dialog button")

            val dialogBinding =
                DialogItemViewBinding.inflate(LayoutInflater.from(fragmentActivity))

            if (detailsList[position].second.isValidUrl()) {
                dialogBinding.tvDetails.text = detailsList[position].second
                AlertDialog.Builder(fragmentActivity)
                    .setView(dialogBinding.root)
                    .setTitle(detailsList[position].first)
                    .setNeutralButton(fragmentActivity.getString(R.string.open)) { _, _ ->
                        val openUrl = android.content.Intent(android.content.Intent.ACTION_VIEW)
                        openUrl.data = android.net.Uri.parse(detailsList[position].second)
                        fragmentActivity.startActivity(openUrl)
                    }
                    .setPositiveButton(fragmentActivity.getString(R.string.copy)) { _, _ ->
                        val clipboard =
                            fragmentActivity.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(
                            android.content.ClipData.newPlainText(
                                "wallet json",
                                detailsList[position].second
                            )
                        )
                        Toast.makeText(
                            fragmentActivity,
                            "Copied to clipboard",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .setNegativeButton(fragmentActivity.getString(R.string.cancel)) { dialog, _ ->
                        dialog.dismiss()

                    }
                    .create()
                    .show()
            } else if (detailsList[position].first.equals("Allocation ID:")) {
                dialogBinding.tvDetails.text = detailsList[position].second
                AlertDialog.Builder(fragmentActivity)
                    .setView(dialogBinding.root)
                    .setTitle(detailsList[position].first)
                    .setNeutralButton(fragmentActivity.getString(R.string.open)) { _, _ ->
                        val openUrl = android.content.Intent(android.content.Intent.ACTION_VIEW)
                        openUrl.data =
                            android.net.Uri.parse("https://explorer.avax.network/tx/" + detailsList[position].second)
                        fragmentActivity.startActivity(openUrl)
                    }
                    .setPositiveButton(fragmentActivity.getString(R.string.copy)) { _, _ ->
                        val clipboard =
                            fragmentActivity.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(
                            android.content.ClipData.newPlainText(
                                "wallet json",
                                detailsList[position].second
                            )
                        )
                        Toast.makeText(
                            fragmentActivity,
                            "Copied to clipboard",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .setNegativeButton(fragmentActivity.getString(R.string.cancel)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            } else if (detailsList[position].second.isValidJson()) {
                val prettyWalletJson = GsonBuilder().setPrettyPrinting().create().toJson(
                    Gson().fromJson(detailsList[position].second, Any::class.java)
                )
                dialogBinding.tvDetails.text = prettyWalletJson
                AlertDialog.Builder(fragmentActivity)
                    .setView(dialogBinding.root)
                    .setTitle(detailsList[position].first)
                    .setPositiveButton(fragmentActivity.getString(R.string.copy)) { _, _ ->
                        val clipboard =
                            fragmentActivity.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(
                            android.content.ClipData.newPlainText(
                                "wallet json",
                                detailsList[position].second
                            )
                        )
                        Toast.makeText(
                            fragmentActivity,
                            "Copied to clipboard",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .setNegativeButton(fragmentActivity.getString(R.string.cancel)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            } else {
                dialogBinding.tvDetails.text = detailsList[position].second
                AlertDialog.Builder(fragmentActivity)
                    .setView(dialogBinding.root)
                    .setTitle(detailsList[position].first)
                    .setPositiveButton(fragmentActivity.getString(R.string.copy)) { _, _ ->
                        val clipboard =
                            fragmentActivity.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(
                            android.content.ClipData.newPlainText(
                                "wallet json",
                                detailsList[position].second
                            )
                        )
                        Toast.makeText(
                            fragmentActivity,
                            "Copied to clipboard",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .setNegativeButton(fragmentActivity.getString(R.string.cancel)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }
        }
        return rowDetailsBinding.root
    }
}
