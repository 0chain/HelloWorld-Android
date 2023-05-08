package org.zus.helloworld.ui

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
import org.zus.helloworld.R
import org.zus.helloworld.databinding.CreateWalletFragmentBinding
import org.zus.helloworld.models.bolt.WalletModel
import org.zus.helloworld.ui.mainactivity.MainViewModel
import org.zus.helloworld.ui.vult.ALLOCATION_NAME
import org.zus.helloworld.ui.vult.ALLOCATION_SIZE
import org.zus.helloworld.ui.vult.DATA_SHARDS
import org.zus.helloworld.ui.vult.EXPIRATION_SECONDS
import org.zus.helloworld.ui.vult.LOCK_TOKENS
import org.zus.helloworld.ui.vult.PARITY_SHARDS
import org.zus.helloworld.ui.vult.VultViewModel
import org.zus.helloworld.utils.Utils
import org.zus.helloworld.utils.Utils.Companion.isValidJson
import org.zus.helloworld.utils.ZcnSDK
import zcncore.Zcncore
import java.io.FileNotFoundException

public const val TAG_CREATE_WALLET: String = "CreateWalletFragment"

class CreateWalletFragment : Fragment() {

    private lateinit var binding: CreateWalletFragmentBinding
    private lateinit var mainViewModel: MainViewModel
    private lateinit var vultViewModel: VultViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

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
                "walletJsonStringFromFile: ${walletJsonStringFromFile.isBlank()}"
            )
            if (walletJsonStringFromFile.isBlank() || walletJsonStringFromFile.isEmpty()) {
                try {
                    val walletJson = Zcncore.createWalletOffline()
                    if (walletJson != null && walletJson.isValidJson()) {
                        Log.i(TAG_CREATE_WALLET, "New Wallet created successfully")
                        Utils(requireContext()).saveWalletAsFile(walletJson)
                        processWallet(walletJson)
                    } else {
                        Log.e(TAG_CREATE_WALLET, "Error: $walletJson")
                    }
                } catch (e: Exception) {
                    Log.e(TAG_CREATE_WALLET, "Error: ${e.message}", e)
                }
            } else {
                Log.i(TAG_CREATE_WALLET, "Wallet already exists")
                processWallet(walletJsonStringFromFile)

            }

        } catch (e: FileNotFoundException) {
            Log.d(TAG_CREATE_WALLET, "File not found")
            try {
                val walletJson = Zcncore.createWalletOffline()
                if (walletJson != null && walletJson.isValidJson()) {
                    Log.i(TAG_CREATE_WALLET, "New Wallet created successfully")
                    Utils(requireContext()).saveWalletAsFile(walletJson)
                    processWallet(walletJson)
                } else {
                    Log.e(TAG_CREATE_WALLET, "Error: $walletJson")
                }
            } catch (e: Exception) {
                Log.e(TAG_CREATE_WALLET, "Error: ${e.message}", e)
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
            Log.i(TAG_CREATE_WALLET, "Wallet json: $walletJson")
            Zcncore.setWalletInfo(walletJson, false)
            // ZcnSDK.readPoolLock(1.0,0.0)
            CoroutineScope(Dispatchers.Main).launch {
                binding.btCreateWallet.text = getString(R.string.creating_allocation)

                vultViewModel.storageSDK =
                    VultViewModel.initZboxStorageSDK(
                        Utils(requireContext()).config,
                        Utils(requireContext()).readWalletFromFileJSON()
                    )

                binding.btCreateWallet.setOnClickListener {
                    binding.progressView.visibility = View.VISIBLE
                    mainViewModel.createWalletSemaphore.postValue(true)
                    binding.btCreateWallet.text = getString(R.string.creating_allocation)
                    CoroutineScope(Dispatchers.Main).launch {
                        createAllocation()
                        mainViewModel.createWalletSemaphore.postValue(false)
                    }
                }

                if (vultViewModel.getAllocation() == null) {
                    val walletBalance =
                        ZcnSDK.getWalletBalance(mainViewModel.wallet?.mClientId ?: "")
                    if (walletBalance < 10.0) {
                        ZcnSDK.faucet("pour", "{Pay day}", 10.0)
                    }
                    createAllocation()
                }
                mainViewModel.createWalletSemaphore.observe(
                    viewLifecycleOwner
                ) { isCreatingAllocation ->
                    if (!isCreatingAllocation) {
                        binding.progressView.visibility = View.GONE
                        findNavController().navigate(R.id.action_createWalletFragment_to_selectAppFragment)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG_CREATE_WALLET, "Error: ${e.message}", e)
        }
    }

    private suspend fun createAllocation() = vultViewModel.createAllocation(
        allocationName = ALLOCATION_NAME,
        dataShards = DATA_SHARDS,
        parityShards = PARITY_SHARDS,
        allocationSize = ALLOCATION_SIZE,
        expirationSeconds = EXPIRATION_SECONDS,
        lockTokens = LOCK_TOKENS,
    )
}
