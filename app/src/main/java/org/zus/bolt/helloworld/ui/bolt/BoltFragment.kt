package org.zus.bolt.helloworld.ui.bolt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import org.zus.bolt.helloworld.R
import org.zus.bolt.helloworld.databinding.BoltFragmentBinding
import org.zus.bolt.helloworld.ui.mainactivity.MainViewModel
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
        requireActivity().runOnUiThread {
            boltViewModel.getWalletBalance().observe(viewLifecycleOwner) { balance ->
                binding.zcnBalance.text = getString(R.string.zcn_balance, balance)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.zcnBalance.text = getString(R.string.zcn_balance, "0")

        updateBalance()

        /* getting the updated balance by refreshing. */
        binding.mRefresh.setOnClickListener {
            updateBalance()
        }
        /* Receive token faucet transaction. */
        binding.mreceiveToken.setOnClickListener {
            /* updating the balance after 3 seconds.*/
            boltViewModel.receiveFaucet()
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    updateBalance()
                }
            }, 1000)
        }

        /* Send token to an address. */
        binding.msendToken.setOnClickListener {
            boltViewModel.sendTransaction(binding.receiverClientId.text.toString())
            /* updating the balance after 3 seconds.*/
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    updateBalance()
                }
            }, 1000)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
