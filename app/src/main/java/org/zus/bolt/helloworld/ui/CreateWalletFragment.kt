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
import org.zus.bolt.helloworld.databinding.CreateWalletFragmentBinding
import org.zus.bolt.helloworld.models.WalletModel
import org.zus.bolt.helloworld.ui.mainactivity.MainViewModel
import zcncore.Zcncore

class CreateWalletFragment : Fragment() {

    private var _binding: CreateWalletFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = CreateWalletFragmentBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btCreateWallet.setOnClickListener {
            try {
                Zcncore.createWallet { status, walletJson, error ->
                    if (status == 0L) {
                        try {
                            Gson().fromJson(walletJson, WalletModel::class.java).let {
                                viewModel.wallet = it
                                Zcncore.setWalletInfo(walletJson, false)
                                requireActivity().runOnUiThread {
                                    findNavController().navigate(R.id.action_createWalletFragment_to_boltFragment)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        print("Error: $error")
                    }
                }
            } catch (e: Exception) {
                print("Error: $e")
            }
        }
        binding.btRecoverWallet.setOnClickListener {

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
