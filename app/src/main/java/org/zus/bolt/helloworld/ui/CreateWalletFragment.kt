package org.zus.bolt.helloworld.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.gson.Gson
import org.zus.bolt.helloworld.R
import org.zus.bolt.helloworld.databinding.CreateWalletFragmentBinding
import org.zus.bolt.helloworld.models.AppType
import org.zus.bolt.helloworld.models.WalletModel
import org.zus.bolt.helloworld.ui.mainactivity.MainViewModel
import org.zus.bolt.helloworld.utils.Utils
import zcncore.Zcncore
import java.io.FileNotFoundException

public const val TAG_CREATE_WALLET: String = "CreateWalletFragment"

class CreateWalletFragment : Fragment() {

    private var _binding: CreateWalletFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel
    val args: CreateWalletFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        _binding = CreateWalletFragmentBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btCreateWallet.setOnClickListener {
            try {
                val walletJsonStringFromFile = Utils(requireContext()).readWalletFromFileJSON()
                if (walletJsonStringFromFile.isNullOrBlank() || walletJsonStringFromFile.isNullOrEmpty()) {
                    Zcncore.createWallet { status, walletJson, error ->
                        if (status == 0L) {
                            Utils(requireContext()).saveWalletAsFile(walletJson)
                            processWallet(walletJson)
                        } else {
                            Log.e(TAG_CREATE_WALLET, "Error: $error")
                        }
                    }
                } else {
                    processWallet(walletJsonStringFromFile)

                }

            } catch (e: FileNotFoundException) {
                Log.d(TAG_CREATE_WALLET, "File not found")
                Zcncore.createWallet { status, walletJson, error ->
                    if (status == 0L) {
                        Utils(requireContext()).saveWalletAsFile(walletJson)
                        processWallet(walletJson)
                    } else {
                        Log.e(TAG_CREATE_WALLET, "Error: $error")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG_CREATE_WALLET, "Error: ${e.message}", e)
            }
        }
    }

    private fun processWallet(walletJson: String) {
        try {
            val walletModel = Gson().fromJson(walletJson, WalletModel::class.java)
            viewModel.wallet = walletModel
            val wallet: WalletModel = viewModel.wallet!!
            wallet.walletJson = walletJson
            Zcncore.setWalletInfo(walletJson, false)
            requireActivity().runOnUiThread {
                when (args.appType) {
                    AppType.BOLT -> findNavController().navigate(R.id.action_createWalletFragment_to_boltFragment)
                    AppType.VULT -> findNavController().navigate(R.id.action_createWalletFragment_to_vultFragment)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG_CREATE_WALLET, "Error: ${e.message}", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
