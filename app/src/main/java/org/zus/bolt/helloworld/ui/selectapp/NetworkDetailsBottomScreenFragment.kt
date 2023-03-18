package org.zus.bolt.helloworld.ui.selectapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import org.zus.bolt.helloworld.R
import org.zus.bolt.helloworld.databinding.GenericBottomSheetDetailsFragmentBinding
import org.zus.bolt.helloworld.models.NetworkModel
import org.zus.bolt.helloworld.utils.Utils

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

        val networkDetails: MutableList<Pair<String, String>> =
            mutableListOf<Pair<String, String>>().apply {
                add(Pair("Network Name:", networkModel.domainUrl))
                add(Pair("Network Url:", networkModel.config.blockWorker))
                add(Pair("0box Url:", networkModel.zboxUrl))
            }
        val linearArrayAdapter = DetailsListAdapter(requireActivity(), networkDetails)

        binding.detailsListView.adapter = linearArrayAdapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //TOdo: add click listaeresn for network urls.
        /*binding.detailsListView.getChildAt(2).setOnClickListener {
            openUrl(networkModel.zboxUrl)
        }
        binding.detailsListView.getChildAt(3).setOnClickListener {
            openUrl(networkModel.config.blockWorker)
        }*/
    }

    private fun openUrl(url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
    }
}
