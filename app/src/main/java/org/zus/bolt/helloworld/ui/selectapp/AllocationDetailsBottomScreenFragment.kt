package org.zus.bolt.helloworld.ui.selectapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.zus.bolt.helloworld.R
import org.zus.bolt.helloworld.databinding.GenericBottomSheetDetailsFragmentBinding
import org.zus.bolt.helloworld.databinding.RowDetailsListItemBinding
import org.zus.bolt.helloworld.models.selectapp.DetailsListModel
import org.zus.bolt.helloworld.models.selectapp.DetailsModel
import org.zus.bolt.helloworld.ui.vult.VultViewModel
import org.zus.bolt.helloworld.utils.Utils.Companion.getConvertedDateTime
import org.zus.bolt.helloworld.utils.Utils.Companion.getConvertedSize
import zbox.Allocation

class AllocationDetailsBottomScreenFragment(
    private val allocationModel: Allocation,
) : BottomSheetDialogFragment() {
    private lateinit var binding: GenericBottomSheetDetailsFragmentBinding
    private lateinit var vultViewModel: VultViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = GenericBottomSheetDetailsFragmentBinding.inflate(inflater, container, false)
        vultViewModel = ViewModelProvider(requireActivity())[VultViewModel::class.java]

        binding.tvPageTitle.text = getString(R.string.allocation_details_title)

        CoroutineScope(Dispatchers.Main).launch {

            val statsModel = vultViewModel.getAllocation()?.let { vultViewModel.getStats(it.stats) }

            val allocationDetailsModel = DetailsListModel(
                title = getString(R.string.allocation_details_title),
                detailsList = listOf(
                    DetailsModel(
                        title = "Allocation ID",
                        value = allocationModel.id,
                        showArrowButton = false
                    ),
                    DetailsModel(
                        title = "Name",
                        value = allocationModel.name,
                        showArrowButton = false
                    ),
                    DetailsModel(
                        title = "Expiration",
                        value = allocationModel.expiration.getConvertedDateTime(),
                        showArrowButton = false
                    ),
                    DetailsModel(
                        title = "Size",
                        value = allocationModel.size.getConvertedSize(),
                        showArrowButton = false
                    ),
                    DetailsModel(
                        title = "Used Size",
                        value = statsModel?.used_size?.getConvertedSize() ?: "0",
                        showArrowButton = false
                    )
                )
            )

            val shardsAndChallengesDetails = DetailsListModel(
                title = getString(R.string.shards_and_challenges_details_title),
                detailsList = listOf(
                    DetailsModel(
                        title = "Data Shards",
                        value = allocationModel.dataShards.toString(),
                        showArrowButton = false
                    ),
                    DetailsModel(
                        title = "Parity Shards",
                        value = allocationModel.parityShards.toString(),
                        showArrowButton = false
                    ),
                    DetailsModel(
                        title = "Number of Writes",
                        value = statsModel?.num_of_writes.toString(),
                        showArrowButton = false
                    ),
                    DetailsModel(
                        title = "Number of Reads",
                        value = statsModel?.num_of_reads.toString(),
                        showArrowButton = false
                    ),
                    DetailsModel(
                        title = "Number of Challenges",
                        value = statsModel?.num_success_challenges.toString(),
                        showArrowButton = false
                    ),
                    DetailsModel(
                        title = "Latest Closed Challenge",
                        value = statsModel?.latest_closed_challenge ?: "No value provided",
                        showArrowButton = false
                    )
                )
            )

            val allocationDetails = listOf(
                allocationDetailsModel,
                shardsAndChallengesDetails
            )

            binding.detailsListView.removeAllViews()

            for (detailsModel in allocationDetails) {
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

        }
        return binding.root
    }
}
