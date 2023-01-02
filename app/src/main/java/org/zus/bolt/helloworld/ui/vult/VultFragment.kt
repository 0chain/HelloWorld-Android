package org.zus.bolt.helloworld.ui.vult

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.zus.bolt.helloworld.databinding.VultFragmentBinding


class VultFragment : Fragment() {
    private lateinit var binding: VultFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = VultFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }
}
