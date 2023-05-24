package org.zus.helloworld.ui.bolt

import android.app.AlertDialog
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBindings
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.*
import org.zus.helloworld.R
import org.zus.helloworld.databinding.BoltFragmentBinding
import org.zus.helloworld.ui.mainactivity.MainViewModel
import org.zus.helloworld.utils.ZcnSDK
import zcncore.Zcncore

public const val TAG_BOLT: String = "BoltFragment"

class BoltFragment : Fragment() {

    private lateinit var binding: BoltFragmentBinding
    private lateinit var mainViewModel: MainViewModel
    private lateinit var boltViewModel: BoltViewModel
    private lateinit var onBackPressedCallback: OnBackPressedCallback
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        binding = BoltFragmentBinding.inflate(inflater, container, false)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        boltViewModel = ViewModelProvider(this)[BoltViewModel::class.java]
        binding.zcnDollarValue.text = getString(R.string._1_zcn_0_0001_usd, 0.0, 0.0)

        binding.zcnBalance.text = getString(R.string.zcn_balance, "0")
        binding.zcnDollar.text = getString(R.string.zcn_dollar, 0.0f)

        CoroutineScope(Dispatchers.Main).launch {
            val usd = ZcnSDK.zcnToUsd(1.0)
            binding.zcnDollarValue.text = getString(R.string._1_zcn_0_0001_usd, 1.0, usd)
        }

        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (boltViewModel.isRefreshLiveData.value != true) {
                    findNavController().popBackStack()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )

        boltViewModel.isRefreshLiveData.observe(viewLifecycleOwner) { isRefresh ->
            if (!isRefresh) {
                requireActivity().runOnUiThread {
                    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                }
            } else {
                requireActivity().runOnUiThread {
                    requireActivity().window.setFlags(
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    )
                }
            }
            binding.swipeRefresh.isRefreshing = isRefresh
        }

        boltViewModel.snackbarMessageLiveData.observe(viewLifecycleOwner) { message ->
            if (message.isNotBlank()) {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            }
        }

        CoroutineScope(Dispatchers.Main).launch {
            val calls = async {
                updateBalance()
                delay(1500)
                updateTransactions()
            }
            awaitAll(calls)
        }

        /* Setting the adapters. */
        val transactionsAdapter =
            TransactionsAdapter(requireContext(), childFragmentManager, listOf())
        binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTransactions.adapter = transactionsAdapter

        boltViewModel.transactionsLiveData.observe(viewLifecycleOwner) { transactions ->
            transactionsAdapter.transactions = transactions
            transactionsAdapter.notifyDataSetChanged()
        }
        boltViewModel.balanceLiveData.observe(viewLifecycleOwner) { balance ->
            binding.zcnBalance.text = getString(R.string.zcn_balance, balance)
            CoroutineScope(Dispatchers.Main).launch {
                val dollar = ZcnSDK.zcnToUsd(balance.toDouble())
                try {
                    binding.zcnDollar.text = getString(R.string.zcn_dollar, dollar)
                } catch (e: Exception) {
                    Log.e(TAG_BOLT, "onCreateView: ${e.message}")
                }
            }
        }

        /* Receive token faucet transaction. */
        binding.mFaucet.setOnClickListener {
            /* updating the balance after 3 seconds.*/
            CoroutineScope(Dispatchers.Main).launch {
                boltViewModel.receiveFaucet()
                val calls = async {
                    updateBalance()
                    delay(1500)
                    updateTransactions()
                }
                awaitAll(calls)
            }
        }

        /* Send token to an address. */
        binding.msendToken.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            val dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.send_transaction_dialog, null)
            builder.setTitle("Send Token")
                .setView(dialogView)
                .setPositiveButton("Send") { _, _ ->
                    val address =
                        dialogView.findViewById<TextView>(R.id.et_to_client_id).text.toString()
                    val amount = dialogView.findViewById<TextView>(R.id.amount).text.toString()
                    if (amount.isBlank() || amount.toDouble() <= 0.0f) {
                        val amountTIL = ViewBindings.findChildViewById<TextInputLayout>(
                            dialogView,
                            R.id.amountTextInputLayout
                        )
                        amountTIL?.error = "Amount should be greater than 0"
                    } else {
                        CoroutineScope(Dispatchers.Main).launch {
                            boltViewModel.sendTransaction(address, amount)
                        }
                    }
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
            builder.create().show()
        }

        binding.mreceiveToken.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            val dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.receive_transaction_dialog, null)
            dialogView.findViewById<MaterialTextView>(R.id.tv_receiver_client_id).text =
                buildString {
                    append(
                        mainViewModel.wallet?.mClientId?.substring(
                            0,
                            16
                        )
                    )
                    append("...")
                    append(mainViewModel.wallet?.mClientId?.substring(mainViewModel.wallet?.mClientId?.length!! - 16))
                }
            builder.setTitle("Receive Token")
                .setView(dialogView)
                .setCancelable(true)
                .setNeutralButton("Copy Client ID") { _, _ ->
                    //copy to clipboard
                    val clipboard =
                        requireActivity().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(
                        android.content.ClipData.newPlainText(
                            "client id",
                            mainViewModel.wallet?.mClientId
                        )
                    )
                }
            builder.create().show()
        }

        binding.swipeRefresh.setOnRefreshListener {
            CoroutineScope(Dispatchers.Main).launch {
                val calls = async {
                    updateBalance()
                    delay(1500)
                    updateTransactions()
                }
                awaitAll(calls)
            }
        }
        return binding.root
    }

    /* Get transactions. */
    private suspend fun updateTransactions() {
        boltViewModel.getTransactions(
            fromClientId = "",
            toClientId = mainViewModel.wallet?.mClientId ?: "",
            sortOrder = Sort.getSort(SortEnum.DESC),
            limit = 20,
            offset = 0
        )

        delay(1500)

        boltViewModel.getTransactions(
            fromClientId = mainViewModel.wallet?.mClientId ?: "",
            toClientId = "",
            sortOrder = Sort.getSort(SortEnum.DESC),
            limit = 20,
            offset = 0
        )
    }

    private suspend fun updateBalance() {
        boltViewModel.getWalletBalance()
    }
}


