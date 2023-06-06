package org.zus.helloworld.ui.selectapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.zus.helloworld.R
import org.zus.helloworld.databinding.ContentMainBinding
import org.zus.helloworld.databinding.SelectAppFragmentBinding
import org.zus.helloworld.models.bolt.WalletModel
import org.zus.helloworld.ui.mainactivity.MainViewModel
import org.zus.helloworld.ui.vult.VultViewModel
import org.zus.helloworld.utils.Utils

class SelectAppFragment : Fragment() {
    lateinit var binding: SelectAppFragmentBinding
    lateinit var mainViewModel: MainViewModel
    lateinit var vultViewModel: VultViewModel
    lateinit var contentMainBinding: ContentMainBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = SelectAppFragmentBinding.inflate(inflater, container, false)
        contentMainBinding = ContentMainBinding.bind(
            requireActivity().findViewById(R.id.content_main)
        )

        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        vultViewModel = ViewModelProvider(requireActivity())[VultViewModel::class.java]

        binding.cvWalletDetails.setOnClickListener {
            val walletDetailsBottomScreenFragment =
                WalletDetailsBottomScreenFragment(mainViewModel.wallet!!)
            walletDetailsBottomScreenFragment.show(
                parentFragmentManager,
                "WalletDetailsBottomScreenFragment"
            )
        }
        binding.cvAllocationDetails.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    contentMainBinding.progressContentMain.visibility = View.VISIBLE
                    val allocationModel = vultViewModel.getAllocation()
                    contentMainBinding.progressContentMain.visibility = View.GONE
                    if (allocationModel != null)
                        AllocationDetailsBottomScreenFragment(allocationModel).show(
                            parentFragmentManager,
                            "AllocationDetailsBottomScreenFragment"
                        )
                    else
                        Snackbar.make(
                            binding.root,
                            "Error: Allocation not found",
                            Snackbar.LENGTH_LONG
                        ).show()
                } catch (e: Exception) {
                    contentMainBinding.progressContentMain.visibility = View.GONE
                    Snackbar.make(
                        binding.root,
                        "Error: ${e.message}",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

}
