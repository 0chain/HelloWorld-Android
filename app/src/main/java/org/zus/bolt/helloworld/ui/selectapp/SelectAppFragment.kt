package org.zus.bolt.helloworld.ui.selectapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import org.zus.bolt.helloworld.R
import org.zus.bolt.helloworld.databinding.SelectAppFragmentBinding
import org.zus.bolt.helloworld.models.bolt.WalletModel
import org.zus.bolt.helloworld.models.vult.AllocationModel
import org.zus.bolt.helloworld.ui.mainactivity.MainViewModel
import org.zus.bolt.helloworld.utils.Utils

class SelectAppFragment : Fragment() {
    lateinit var binding: SelectAppFragmentBinding
    lateinit var mainViewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = SelectAppFragmentBinding.inflate(inflater, container, false)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        binding.cvWalletDetails.setOnClickListener {
            val walletDetailsBottomScreenFragment =
                WalletDetailsBottomScreenFragment(mainViewModel.wallet!!)
            walletDetailsBottomScreenFragment.show(
                parentFragmentManager,
                "WalletDetailsBottomScreenFragment"
            )
        }
        binding.cvAllocationDetails.setOnClickListener {
            val allocationDetailsBottomScreenFragment =
                AllocationDetailsBottomScreenFragment(AllocationModel())
            allocationDetailsBottomScreenFragment.show(
                parentFragmentManager,
                "AllocationDetailsBottomScreenFragment"
            )
        }
        binding.cvNetworkDetails.setOnClickListener {
            val networkDetailsBottomScreenFragment =
                NetworkDetailsBottomScreenFragment()
            networkDetailsBottomScreenFragment.show(
                parentFragmentManager,
                "NetworkDetailsBottomScreenFragment"
            )
        }

        binding.cvBolt.setOnClickListener {
            if (!Utils(requireContext()).isWalletExist()) {
                findNavController().navigate(R.id.action_selectAppFragment_to_boltFragment)
            } else {
                mainViewModel.wallet =
                    Gson().fromJson(
                        Utils(requireContext()).readWalletFromFileJSON(),
                        WalletModel::class.java
                    )
                mainViewModel.setWalletJson(Utils(requireContext()).readWalletFromFileJSON())
                findNavController().navigate(R.id.action_selectAppFragment_to_boltFragment)
            }
        }
        binding.cvVult.setOnClickListener {
            if (!Utils(requireContext()).isWalletExist()) {
                findNavController().navigate(R.id.action_selectAppFragment_to_vultFragment)
            } else {
                mainViewModel.wallet =
                    Gson().fromJson(
                        Utils(requireContext()).readWalletFromFileJSON(),
                        WalletModel::class.java
                    )
                mainViewModel.setWalletJson(Utils(requireContext()).readWalletFromFileJSON())
                findNavController().navigate(R.id.action_selectAppFragment_to_vultFragment)
            }
        }
        return binding.root
    }
}
