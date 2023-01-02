package org.zus.bolt.helloworld.ui.bolt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import org.zus.bolt.helloworld.databinding.BoltFragmentBinding
import org.zus.bolt.helloworld.ui.mainactivity.MainViewModel
import java.util.*

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class BoltFragment : Fragment() {

    private var _binding: BoltFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainViewModel: MainViewModel
    private lateinit var boltViewModel: BoltViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = BoltFragmentBinding.inflate(inflater, container, false)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        boltViewModel = ViewModelProvider(this)[BoltViewModel::class.java]
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Timer().schedule(object : TimerTask() {
            override fun run() {
                requireActivity().runOnUiThread {
                    binding.zcnBalance.text = "${boltViewModel.getWalletBalance()} ZCN"
                }
            }
        }, 0, 10000)

        /* Receive token faucet transaction. */
        binding.mreceiveToken.setOnClickListener {
            boltViewModel.receiveFaucet()
        }

        /* Send token to an address. */
        binding.msendToken.setOnClickListener {
            boltViewModel.sendTransaction(binding.receiverClientId.text.toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
