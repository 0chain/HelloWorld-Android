package org.zus.bolt.helloworld.ui.selectapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.zus.bolt.helloworld.R
import org.zus.bolt.helloworld.databinding.GenericBottomSheetDetailsFragmentBinding
import org.zus.bolt.helloworld.models.bolt.WalletModel

class WalletDetailsBottomScreenFragment(
    private val walletModel: WalletModel
) : BottomSheetDialogFragment() {
    private lateinit var binding: GenericBottomSheetDetailsFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = GenericBottomSheetDetailsFragmentBinding.inflate(inflater, container, false)

        binding.tvPageTitle.text = getString(R.string.wallet_details_title)

        val walletDetails: MutableList<Pair<String, String>> = mutableListOf<Pair<String, String>>()
            .apply {
                add(Pair("Client ID:", walletModel.mClientId))
                add(Pair("Client Key:", walletModel.mClientKey))
                add(Pair("Public Key:", walletModel.mKeys[0].mPublicKey))
                add(Pair("Private Key:", walletModel.mKeys[0].mPrivateKey))
                add(Pair("Mnemonic:", walletModel.mMnemonics))
            }


        //linear layout adapter
        val linearArrayAdapter = DetailsListAdapter(walletDetails)

        binding.detailsListView.adapter = linearArrayAdapter

        return binding.root
    }
}
