package org.zus.bolt.helloworld.ui

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
import org.zus.bolt.helloworld.models.AppType
import org.zus.bolt.helloworld.models.WalletModel
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

        binding.cvBolt.setOnClickListener {
            val actionSelectAppToCreateWallet =
                SelectAppFragmentDirections.actionSelectAppFragmentToCreateWalletFragment(AppType.BOLT)
            if (!Utils(requireContext()).isWalletExist()) {
                findNavController().navigate(actionSelectAppToCreateWallet)
            } else {
                mainViewModel.wallet =
                    Gson().fromJson(Utils(requireContext()).readWalletFromFileJSON(),
                        WalletModel::class.java)
                mainViewModel.setWalletJson(Utils(requireContext()).readWalletFromFileJSON())
                findNavController().navigate(R.id.action_selectAppFragment_to_boltFragment)
            }
        }
        binding.cvVult.setOnClickListener {
            val actionSelectAppToCreateWallet =
                SelectAppFragmentDirections.actionSelectAppFragmentToCreateWalletFragment(AppType.VULT)
            if (!Utils(requireContext()).isWalletExist()) {
                findNavController().navigate(actionSelectAppToCreateWallet)
            } else {
                mainViewModel.wallet =
                    Gson().fromJson(Utils(requireContext()).readWalletFromFileJSON(),
                        WalletModel::class.java)
                mainViewModel.setWalletJson(Utils(requireContext()).readWalletFromFileJSON())
                findNavController().navigate(R.id.action_selectAppFragment_to_vultFragment)
            }
        }
        return binding.root
    }
}
