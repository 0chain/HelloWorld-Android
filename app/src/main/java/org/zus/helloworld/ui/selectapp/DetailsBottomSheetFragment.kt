package org.zus.helloworld.ui.selectapp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.zus.helloworld.databinding.DetailsFragmentBinding
import org.zus.helloworld.models.selectapp.DetailsModel

class DetailsBottomSheetFragment(
    private val detailsModel: DetailsModel,
) : BottomSheetDialogFragment() {
    private lateinit var binding: DetailsFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DetailsFragmentBinding.inflate(inflater, container, false)

        /* Header title. */
        binding.tvHeaderTitle.text = detailsModel.title
        binding.btnBack.setOnClickListener {
            this.dismiss()
        }

        binding.tvDetails.text = detailsModel.value
        binding.tvDetails.setOnLongClickListener {
            val clipboard =
                requireActivity().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("label", detailsModel.value)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(requireContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show()
            true
        }

        return binding.root
    }
}
