package org.zus.helloworld.ui.selectapp

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import org.zus.helloworld.R
import org.zus.helloworld.databinding.DialogItemViewBinding

class CustomDialog(
    private val context: Context,
) {
    private lateinit var binding: DialogItemViewBinding
    private var cancellable = true

    private fun initView() {
        binding = DialogItemViewBinding.inflate(LayoutInflater.from(context))
    }

    private fun hide() {
        if (alertDialog != null) {
            alertDialog!!.dismiss()
        }
    }

    fun show() {
        initView()
        builder!!.setView(binding.root)
        alertDialog = builder!!.create()
        alertDialog!!.setCancelable(cancellable)
        alertDialog!!.show()
    }

    fun setTitle(title: String?): CustomDialog {
        initView()
        builder!!.setTitle(title)
        return this
    }

    fun setMessage(message: String?): CustomDialog {
        initView()
        binding.tvDetails.text = message
        return this
    }

    fun setCancellable(cancellable: Boolean): CustomDialog {
        this.cancellable = cancellable
        return this
    }

    fun setPositiveButton(text: String, callback: () -> Unit): CustomDialog {
        initView()
        builder!!.setPositiveButton(text) { _, _ ->
            callback()
        }
        return this
    }

    fun setNegativeButton(text: String, callback: () -> Unit): CustomDialog {
        initView()
        builder!!.setNegativeButton(R.string.cancel) { _, _ ->
            callback()
        }
        return this
    }

    fun setNeutralButton(text: String, callback: () -> Unit): CustomDialog {
        initView()
        builder!!.setNeutralButton(text) { _, _ ->
            callback()
        }
        return this
    }

    override fun toString(): String {
        return "CustomDialog: alertDialog = " + alertDialog + " view = " + binding.root + " cancellable = " + cancellable
    }

    companion object {
        private var alertDialog: AlertDialog? = null
        private var builder: AlertDialog.Builder? = null
        private var layoutInflater: LayoutInflater? = null
        fun init(context: Context?): CustomDialog {
            val dialog = context?.let { CustomDialog(it) } ?: throw Exception("Context is null")
            layoutInflater = LayoutInflater.from(context)
            builder = AlertDialog.Builder(context!!)
            return dialog
        }

        fun removeUnusedDialogs() {
            alertDialog = null
            builder = null
            layoutInflater = null
        }
    }
}
