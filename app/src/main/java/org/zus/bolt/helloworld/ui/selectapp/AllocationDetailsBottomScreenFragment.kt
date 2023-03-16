package org.zus.bolt.helloworld.ui.selectapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.zus.bolt.helloworld.R
import org.zus.bolt.helloworld.databinding.GenericBottomSheetDetailsFragmentBinding
import org.zus.bolt.helloworld.models.vult.AllocationModel
import org.zus.bolt.helloworld.utils.Utils
import org.zus.bolt.helloworld.utils.Utils.Companion.getConvertedDateTime
import org.zus.bolt.helloworld.utils.Utils.Companion.getConvertedSize

class AllocationDetailsBottomScreenFragment(
    private val allocationModel: AllocationModel
) : BottomSheetDialogFragment() {
    private lateinit var binding: GenericBottomSheetDetailsFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = GenericBottomSheetDetailsFragmentBinding.inflate(inflater, container, false)

        binding.tvPageTitle.text = getString(R.string.allocation_details_title)

        val allocationDetails: MutableList<Pair<String, String>> =
            mutableListOf<Pair<String, String>>().apply {
                add(Pair("Allocation ID:", allocationModel[0].id))
                add(Pair("Name:", allocationModel[0].name))
                add(Pair("Expiration:", allocationModel[0].expirationDate.getConvertedDateTime()))
                add(Pair("Size:", allocationModel[0].size.getConvertedSize()))
            }

        val linearArrayAdapter = DetailsListAdapter(requireActivity(), allocationDetails)

        binding.detailsListView.adapter = linearArrayAdapter

        return binding.root
    }
}
