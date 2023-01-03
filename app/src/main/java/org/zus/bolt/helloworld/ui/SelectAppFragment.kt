package org.zus.bolt.helloworld.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.zus.bolt.helloworld.R
import org.zus.bolt.helloworld.databinding.SelectAppFragmentBinding

class SelectAppFragment : Fragment() {
    lateinit var binding: SelectAppFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = SelectAppFragmentBinding.inflate(inflater, container, false)

        binding.cvBolt.setOnClickListener {
            findNavController().navigate(R.id.action_selectAppFragment_to_boltFragment)
        }
        binding.cvVult.setOnClickListener {
            findNavController().navigate(R.id.action_selectAppFragment_to_vultFragment)
        }
        return binding.root
    }
}
