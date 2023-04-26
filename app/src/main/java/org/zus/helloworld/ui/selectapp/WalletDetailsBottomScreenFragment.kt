package org.zus.helloworld.ui.selectapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.zus.helloworld.R
import org.zus.helloworld.databinding.GenericBottomSheetDetailsFragmentBinding
import org.zus.helloworld.databinding.RowDetailsListItemBinding
import org.zus.helloworld.models.bolt.WalletModel
import org.zus.helloworld.models.selectapp.DetailsListModel
import org.zus.helloworld.models.selectapp.DetailsModel
import org.zus.helloworld.utils.Utils.Companion.prettyJsonFormat

class WalletDetailsBottomScreenFragment(
    private val walletModel: WalletModel,
) : BottomSheetDialogFragment() {
    private lateinit var binding: GenericBottomSheetDetailsFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = GenericBottomSheetDetailsFragmentBinding.inflate(inflater, container, false)

        binding.tvPageTitle.text = getString(R.string.wallet_details_title)

        val walletDetailsListModel = DetailsListModel(
            title = getString(R.string.details),
            detailsList = listOf(
                DetailsModel(
                    title = "ClientID",
                    value = walletModel.mClientId,
                    showArrowButton = true
                ),
                DetailsModel(
                    title = "Private Encryption Key",
                    value = walletModel.mKeys[0].mPrivateKey,
                    showArrowButton = true
                ),
                DetailsModel(
                    title = "Public Encryption Key",
                    value = walletModel.mKeys[0].mPublicKey,
                    showArrowButton = true
                ),
                DetailsModel(
                    title = "Mnemonics",
                    value = walletModel.mMnemonics,
                    showArrowButton = true
                )
            )
        )
        val walletJsonModel = DetailsListModel(
            title = getString(R.string.wallet_json_title),
            detailsList = listOf(
                DetailsModel(
                    title = walletModel.walletJson.prettyJsonFormat(),
                    value = walletModel.walletJson,
                    showArrowButton = false
                )
            )
        )

        val detailsListModel = listOf(walletDetailsListModel, walletJsonModel)
        //linear layout adapter

        binding.detailsListView.removeAllViews()

        for (detailsModel in detailsListModel) {
            val rowDetailsListItemBindings = RowDetailsListItemBinding.inflate(
                LayoutInflater.from(requireActivity()),
                binding.detailsListView,
                false
            )
            rowDetailsListItemBindings.tvDetails.text = detailsModel.title
            rowDetailsListItemBindings.detailsListView.adapter = DetailsListAdapter(
                requireActivity(),
                detailsModel.detailsList
            )

            binding.detailsListView.addView(rowDetailsListItemBindings.root)
        }

        return binding.root
    }
}
