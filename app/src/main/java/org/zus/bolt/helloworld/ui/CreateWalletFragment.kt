package org.zus.bolt.helloworld.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.zus.bolt.helloworld.R
import org.zus.bolt.helloworld.databinding.CreateWalletFragmentBinding
import org.zus.bolt.helloworld.models.bolt.WalletModel
import org.zus.bolt.helloworld.ui.mainactivity.MainViewModel
import org.zus.bolt.helloworld.ui.vult.VultViewModel
import org.zus.bolt.helloworld.utils.Utils
import org.zus.bolt.helloworld.utils.ZcnSDK
import zcncore.Zcncore
import java.io.FileNotFoundException
import java.util.*

public const val TAG_CREATE_WALLET: String = "CreateWalletFragment"

class CreateWalletFragment : Fragment() {

    private lateinit var binding: CreateWalletFragmentBinding
    private lateinit var mainViewModel: MainViewModel
    private lateinit var vultViewModel: VultViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = CreateWalletFragmentBinding.inflate(inflater, container, false)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        vultViewModel = ViewModelProvider(requireActivity())[VultViewModel::class.java]
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.progressView.visibility = View.VISIBLE

        try {
            val walletJsonStringFromFile = Utils(requireContext()).readWalletFromFileJSON()

            Log.i(
                TAG_CREATE_WALLET,
                "walletJsonStringFromFile: ${walletJsonStringFromFile.isNullOrBlank()}"
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
    }

    private fun processWallet(walletJson: String) {
        try {
            val walletModel = Gson().fromJson(walletJson, WalletModel::class.java)
            mainViewModel.wallet = walletModel
            val wallet: WalletModel = mainViewModel.wallet!!
            Log.e(TAG_CREATE_WALLET, walletModel.mMnemonics)
            wallet.walletJson = walletJson
            Zcncore.setWalletInfo(walletJson, false)
            // ZcnSDK().readPoolLock(1.0,0.0)
            runBlocking {
                CoroutineScope(Dispatchers.IO).launch {
                    requireActivity().runOnUiThread {
                        binding.btCreateWallet.text = getString(R.string.creating_allocation)
                    }

                    vultViewModel.storageSDK =
                        VultViewModel.initZboxStorageSDK(
                            Utils(requireContext()).config,
                            Utils(requireContext()).readWalletFromFileJSON()
                        )

                    if (vultViewModel.getAllocation() == null) {
                        ZcnSDK().faucet("pour", "{Pay day}", 10.0)
                        vultViewModel.createAllocation(
                            allocationName = "test allocation",
                            dataShards = 2,
                            parityShards = 2,
                            allocationSize = 2147483648,
                            expirationSeconds = Date().time / 1000 + 30000,
                            lockTokens = Zcncore.convertToValue(1.0),
                        )
                    }
                    requireActivity().runOnUiThread {
                        binding.progressView.visibility = View.GONE
                        findNavController().navigate(R.id.action_createWalletFragment_to_selectAppFragment)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG_CREATE_WALLET, "Error: ${e.message}", e)
        }
    }
}
