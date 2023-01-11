package org.zus.bolt.helloworld.ui.selectapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.zus.bolt.helloworld.databinding.GenericBottomSheetDetailsFragmentBinding
import org.zus.bolt.helloworld.models.vult.AllocationModel
import org.zus.bolt.helloworld.utils.Utils

class AllocationDetailsBottomScreenFragment(
    private val allocationModel: AllocationModel
) : BottomSheetDialogFragment() {
    lateinit var binding: GenericBottomSheetDetailsFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = GenericBottomSheetDetailsFragmentBinding.inflate(inflater, container, false)

        binding.tvPageTitle.text = "Allocation Details"

        val allocationDetails: MutableList<Pair<String, String>> =
            mutableListOf<Pair<String, String>>().apply {
                add(Pair("Allocation ID:", allocationModel[0].id))
                add(Pair("Name:", allocationModel[0].name))
                add(Pair("Expiration:", Utils.getDateTime(allocationModel[0].expirationDate)))
                add(Pair("Size:", Utils.getStorage(allocationModel[0].size)))
            }

        val linearArrayAdapter = DetailsListAdapter(allocationDetails)

        binding.detailsListView.adapter = linearArrayAdapter

        return binding.root
    }
}
