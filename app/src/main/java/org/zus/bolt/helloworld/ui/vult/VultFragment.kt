package org.zus.bolt.helloworld.ui.vult

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import org.zus.bolt.helloworld.databinding.VultFragmentBinding
import org.zus.bolt.helloworld.ui.bolt.BoltViewModel
import org.zus.bolt.helloworld.ui.mainactivity.MainViewModel
import org.zus.bolt.helloworld.utils.Utils
import zcncore.Zcncore

const val TAG_VULT = "VultFragment"

class VultFragment : Fragment() {
    private lateinit var binding: VultFragmentBinding
    private lateinit var vultViewModel: VultViewModel
    private lateinit var boltViewModel: BoltViewModel
    private lateinit var mainViewModel: MainViewModel

    private lateinit var startFileActivityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = VultFragmentBinding.inflate(inflater, container, false)
        vultViewModel = ViewModelProvider(requireActivity())[VultViewModel::class.java]
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        boltViewModel = ViewModelProvider(requireActivity())[BoltViewModel::class.java]

        startFileActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val result: Intent? = result.data
                    if (result != null) {
                        /* Uploading files. */
                        vultViewModel.uploadFile(
                            filePathURI = result.data?.path,
                            fileAttr = result.data?.scheme
                        )
                    }
                }
            }

        // Storage SDK initialization and wallet initialization.
        vultViewModel.storageSDK =
            VultViewModel.initZboxStorageSDK(Utils.config, mainViewModel.wallet!!.walletJson)
        /*binding.btnCreateAllocation.setOnClickListener {
            vultViewModel.createAllocation(
                allocationName = "test allocation",
                dataShards = 2,
                parityShards = 2,
                allocationSize = 2147483648,
                expirationNanoSeconds = 3600000000000,
                lockTokens = Zcncore.convertToValue(0.5)
            )
        }*/

//
//        binding.btnUploadFile.setOnClickListener {
//            val getFile = Intent(Intent.ACTION_GET_CONTENT).apply {
//                type = "*/*"
//                addCategory(Intent.CATEGORY_OPENABLE)
//                addCategory(Intent.CATEGORY_DEFAULT)
//            }
//            startFileActivityResultLauncher.launch(getFile)
//        }
//
//        binding.btnDownloadFile.setOnClickListener {
//
//        }
        return binding.root
    }
}
