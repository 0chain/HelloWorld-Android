package org.zus.bolt.helloworld.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import org.zus.bolt.helloworld.R
import org.zus.bolt.helloworld.databinding.CreateWalletFragmentBinding
import org.zus.bolt.helloworld.models.bolt.WalletModel
import org.zus.bolt.helloworld.ui.mainactivity.MainViewModel
import org.zus.bolt.helloworld.utils.Utils
import zcncore.Zcncore
import java.io.FileNotFoundException

public const val TAG_CREATE_WALLET: String = "CreateWalletFragment"

class CreateWalletFragment : Fragment() {

    private var _binding: CreateWalletFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel


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
        Handler(Looper.getMainLooper()).postDelayed({

            try {
                val walletJsonStringFromFile = Utils(requireContext()).readWalletFromFileJSON()

                Log.i(
                    TAG_CREATE_WALLET,
                    "walletJsonStringFromFile: ${walletJsonStringFromFile.isBlank()}"
                )
                Log.i(
                    TAG_CREATE_WALLET,
                    "walletJsonStringFromFile: ${walletJsonStringFromFile.isEmpty()}"
                )
                Log.i(
                    TAG_CREATE_WALLET,
                    "walletJsonStringFromFile: ${walletJsonStringFromFile.isNullOrBlank()}"
                )
                Log.i(
                    TAG_CREATE_WALLET,
                    "walletJsonStringFromFile: ${walletJsonStringFromFile.isNullOrEmpty()}"
                )

                if (walletJsonStringFromFile.isBlank() || walletJsonStringFromFile.isEmpty()) {
                    Zcncore.createWallet { status, walletJson, error ->
                        if (status == 0L) {
                            Log.i(TAG_CREATE_WALLET, "New Wallet created successfully")
                            Utils(requireContext()).saveWalletAsFile(walletJson)
                            processWallet(walletJson)
                        } else {
                            Log.e(TAG_CREATE_WALLET, "Error: $error")
                        }
                    }
                } else {
                    Log.i(TAG_CREATE_WALLET, "Wallet already exists")
                    processWallet(walletJsonStringFromFile)

                }

            } catch (e: FileNotFoundException) {
                Log.d(TAG_CREATE_WALLET, "File not found")
                Zcncore.createWallet { status, walletJson, error ->
                    if (status == 0L) {
                        Log.i(TAG_CREATE_WALLET, "New Wallet created successfully")
                        Utils(requireContext()).saveWalletAsFile(walletJson)
                        processWallet(walletJson)

                    } else {
                        Log.e(TAG_CREATE_WALLET, "Error: $error")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG_CREATE_WALLET, "Error: ${e.message}", e)
            }
        }, 1000)
    }

    private fun processWallet(walletJson: String) {
        try {
            val walletModel = Gson().fromJson(walletJson, WalletModel::class.java)
            viewModel.wallet = walletModel
            val wallet: WalletModel = viewModel.wallet!!
            wallet.walletJson = walletJson
            Zcncore.setWalletInfo(walletJson, false)
            requireActivity().runOnUiThread {
                findNavController().navigate(R.id.action_createWalletFragment_to_selectAppFragment)
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
