package org.zus.helloworld.ui.bolt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.zus.helloworld.R
import org.zus.helloworld.databinding.GenericBottomSheetDetailsFragmentBinding
import org.zus.helloworld.databinding.RowDetailsListItemBinding
import org.zus.helloworld.models.bolt.TransactionModel
import org.zus.helloworld.models.selectapp.DetailsListModel
import org.zus.helloworld.models.selectapp.DetailsModel
import org.zus.helloworld.ui.selectapp.DetailsListAdapter
import org.zus.helloworld.utils.Utils.Companion.getConvertedDateTime

class TransactionBottomSheetFragment(
    private val transactionModel: TransactionModel,
) : BottomSheetDialogFragment() {
    private lateinit var binding: GenericBottomSheetDetailsFragmentBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = GenericBottomSheetDetailsFragmentBinding.inflate(inflater, container, false)

        binding.tvPageTitle.text = getString(R.string.transaction_details)

        val signatureAndHashes = DetailsListModel(
            title = "Signature and Hashes",
            detailsList = listOf(
                DetailsModel(
                    title = "Transaction Hash: ${transactionModel.hash}",
                    value = transactionModel.hash,
                    showArrowButton = false
                ),
                DetailsModel(
                    title = "Block Hash: ${transactionModel.block_hash}",
                    value = transactionModel.block_hash,
                    showArrowButton = false
                ),
                DetailsModel(
                    title = "Output Hash: ${transactionModel.output_hash}",
                    value = transactionModel.output_hash,
                    showArrowButton = false
                ),
                DetailsModel(
                    title = "Client Id: ${transactionModel.client_id}",
                    value = transactionModel.client_id,
                    showArrowButton = false
                ),
                DetailsModel(
                    title = "To Client Id: ${transactionModel.to_client_id}",
                    value = transactionModel.to_client_id,
                    showArrowButton = false
                ),
                DetailsModel(
                    title = "Signature: ${transactionModel.signature}",
                    value = transactionModel.signature,
                    showArrowButton = false
                ),
            )
        )

        val amountDetails = DetailsListModel(
            title = "Amount Details",
            detailsList = listOf(
                DetailsModel(
                    title = "Status: ${transactionModel.status}",
                    value = transactionModel.status.toString(),
                    showArrowButton = false
                ),
                DetailsModel(
                    title = "Value: ${transactionModel.value}",
                    value = transactionModel.value.toString(),
                    showArrowButton = false
                ),
                DetailsModel(
                    title = "Fee: ${transactionModel.fee}",
                    value = transactionModel.fee.toString(),
                    showArrowButton = false
                ),
                DetailsModel(
                    title = "Date: ${(transactionModel.creation_date / 1000000000).getConvertedDateTime()}",
                    value = transactionModel.creation_date.toString(),
                    showArrowButton = false
                )
            )
        )

        val explorer = DetailsListModel(
            title = "Explorer",
            detailsList = listOf(
                DetailsModel(
                    title = "Explorer: https://demo.atlus.cloud/transaction-details/${transactionModel.hash}",
                    value = "https://demo.atlus.cloud/transaction-details/${transactionModel.hash}",
                    showArrowButton = true
                )
            )
        )

        val detailsList = listOf(signatureAndHashes, amountDetails, explorer)

        binding.detailsListView.removeAllViews()

        for (detailsModel in detailsList) {
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
