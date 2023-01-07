package org.zus.bolt.helloworld.ui.bolt

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.runBlocking
import org.zus.bolt.helloworld.R
import org.zus.bolt.helloworld.databinding.BoltFragmentBinding
import org.zus.bolt.helloworld.ui.mainactivity.MainViewModel
import zcncore.Zcncore
import java.util.*

public const val TAG_BOLT: String = "BoltFragment"

class BoltFragment : Fragment() {

    private var _binding: BoltFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainViewModel: MainViewModel
    private lateinit var boltViewModel: BoltViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        _binding = BoltFragmentBinding.inflate(inflater, container, false)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        boltViewModel = ViewModelProvider(this)[BoltViewModel::class.java]
        return binding.root

    }

    private fun updateBalance() {
        runBlocking {
            requireActivity().runOnUiThread {
                boltViewModel.getWalletBalance().observe(viewLifecycleOwner) { balance ->
                    binding.zcnBalance.text = getString(R.string.zcn_balance, balance)
                    binding.zcnDollar.text = getString(
                        R.string.zcn_dollar,
                        Zcncore.convertTokenToUSD(balance.toDouble())
                    )
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.zcnBalance.text = getString(R.string.zcn_balance, "0")
        binding.zcnDollar.text = getString(R.string.zcn_dollar, 0.0f)
        updateBalance()

/* Setting the adapters. */
        val transactionsAdapter = TransactionsAdapter(requireContext(), listOf())
        binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTransactions.adapter = transactionsAdapter

        boltViewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            transactionsAdapter.transactions = transactions
            transactionsAdapter.notifyDataSetChanged()
        }  /* Receive token faucet transaction. */
        binding.mFaucet.setOnClickListener {
            /* updating the balance after 3 seconds.*/
            runBlocking {
                boltViewModel.receiveFaucet()
                Timer().schedule(object : TimerTask() {
                    override fun run() {
                        updateBalance()
                    }
                }, 1000)
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
                    runBlocking {
                        boltViewModel.sendTransaction(address, amount)
                        Timer().schedule(object : TimerTask() {
                            override fun run() {
                                updateBalance()
                            }
                        }, 1000)
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
                mainViewModel.wallet?.mClientId?.substring(0, 25) + "..."
            builder.setTitle("Receive Token")
                .setView(dialogView)
                .setCancelable(true)
                .setNeutralButton("Copy Client ID") { _, _ ->
                    //copy to clipboard
                    var clipboard =
                        requireActivity().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(
                        android.content.ClipData.newPlainText(
                            "text",
                            mainViewModel.wallet?.mClientId
                        )
                    )
                }
            builder.create().show()
        }


        updateTransactions()
        binding.swipeRefresh.setOnRefreshListener {
            updateBalance()
            updateTransactions()
            binding.swipeRefresh.isRefreshing = false
        }

    }

    /* Get transactions. */
    private fun updateTransactions() {
        runBlocking {
            boltViewModel.getTransactions(
                fromClientId = mainViewModel.wallet?.mClientId ?: "",
                toClientId = "",
                sortOrder = Sort.getSort(SortEnum.DESC),
                limit = 20,
                offset = 0
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


