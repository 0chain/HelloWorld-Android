package org.zus.bolt.helloworld.ui.selectapp

import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
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

        if (detailsList[position].showArrowButton) {
            rowDetailsBinding.ibnArrowDetails.visibility = View.VISIBLE

            rowDetailsBinding.rowDetailsRoot.setOnClickListener {
                Log.i(TAG_CREATE_WALLET, "clicking on the dialog button")

                val dialogBinding =
                    DialogItemViewBinding.inflate(LayoutInflater.from(fragmentActivity))

                if (detailsList[position].value.isValidUrl()) {
                    dialogBinding.tvDetails.text = detailsList[position].value
                    AlertDialog.Builder(fragmentActivity)
                        .setView(dialogBinding.root)
                        .setTitle(detailsList[position].title)
                        .setNeutralButton(fragmentActivity.getString(R.string.open)) { _, _ ->
                            val openUrl = android.content.Intent(android.content.Intent.ACTION_VIEW)
                            openUrl.data = android.net.Uri.parse(detailsList[position].value)
                            fragmentActivity.startActivity(openUrl)
                        }
                        .setPositiveButton(fragmentActivity.getString(R.string.copy)) { _, _ ->
                            val clipboard =
                                fragmentActivity.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(
                                android.content.ClipData.newPlainText(
                                    "wallet json",
                                    detailsList[position].value
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
                } else if (detailsList[position].title.equals("Allocation ID:")) {
                    dialogBinding.tvDetails.text = detailsList[position].value
                    AlertDialog.Builder(fragmentActivity)
                        .setView(dialogBinding.root)
                        .setTitle(detailsList[position].title)
                        .setNeutralButton(fragmentActivity.getString(R.string.open)) { _, _ ->
                            val openUrl = android.content.Intent(android.content.Intent.ACTION_VIEW)
                            openUrl.data =
                                android.net.Uri.parse("https://explorer.avax.network/tx/" + detailsList[position].value)
                            fragmentActivity.startActivity(openUrl)
                        }
                        .setPositiveButton(fragmentActivity.getString(R.string.copy)) { _, _ ->
                            val clipboard =
                                fragmentActivity.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(
                                android.content.ClipData.newPlainText(
                                    "wallet json",
                                    detailsList[position].value
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
                } else if (detailsList[position].value.isValidJson()) {
                    dialogBinding.tvDetails.text = detailsList[position].value.prettyJsonFormat()
                    AlertDialog.Builder(fragmentActivity)
                        .setView(dialogBinding.root)
                        .setTitle(detailsList[position].title)
                        .setPositiveButton(fragmentActivity.getString(R.string.copy)) { _, _ ->
                            val clipboard =
                                fragmentActivity.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(
                                android.content.ClipData.newPlainText(
                                    "wallet json",
                                    detailsList[position].value
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
                    dialogBinding.tvDetails.text = detailsList[position].value
                    AlertDialog.Builder(fragmentActivity)
                        .setView(dialogBinding.root)
                        .setTitle(detailsList[position].title)
                        .setPositiveButton(fragmentActivity.getString(R.string.copy)) { _, _ ->
                            val clipboard =
                                fragmentActivity.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(
                                android.content.ClipData.newPlainText(
                                    "wallet json",
                                    detailsList[position].value
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
        } else {
            rowDetailsBinding.ibnArrowDetails.visibility = View.GONE
        }

        return rowDetailsBinding.root
    }
}
