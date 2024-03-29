package org.zus.helloworld.ui.selectapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import org.zus.helloworld.R
import org.zus.helloworld.databinding.GenericBottomSheetDetailsFragmentBinding
import org.zus.helloworld.databinding.RowDetailsListItemBinding
import org.zus.helloworld.models.NetworkModel
import org.zus.helloworld.models.selectapp.DetailsListModel
import org.zus.helloworld.models.selectapp.DetailsModel
import org.zus.helloworld.utils.Utils

class NetworkDetailsBottomScreenFragment : BottomSheetDialogFragment() {
    lateinit var binding: GenericBottomSheetDetailsFragmentBinding
    lateinit var networkModel: NetworkModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding =
            GenericBottomSheetDetailsFragmentBinding.inflate(inflater, container, false)

        binding.tvPageTitle.text = getString(R.string.network_details_title)

        networkModel = Gson().fromJson(
            Utils(requireContext()).getConfigFromAssets("config.json"),
            NetworkModel::class.java
        )

        val networkDetailsModel = DetailsListModel(
            title = getString(R.string.details),
            detailsList = listOf(
                DetailsModel(
                    title = "Name: ${networkModel.domainUrl}",
                    value = networkModel.domainUrl,
                    showArrowButton = false
                ),
                DetailsModel(
                    title = "Url: ${networkModel.config.blockWorker}",
                    value = networkModel.config.blockWorker,
                    showArrowButton = false
                ),
                DetailsModel(
                    title = "0box Url: ${networkModel.zboxUrl}",
                    value = networkModel.zboxUrl,
                    showArrowButton = false
                )
            )
        )

        val url = DetailsListModel(
            title = "",
            detailsList = listOf(
                DetailsModel(
                    title = networkModel.config.blockWorker,
                    value = networkModel.config.blockWorker,
                    showArrowButton = false
                )
            )
        )


        val networkDetails = listOf(
            networkDetailsModel,
            url
        )

        binding.detailsListView.removeAllViews()

        for (detailsModel in networkDetails) {
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
