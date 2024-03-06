package com.blackhito.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.blackhito.presentation.databinding.FragmentCurrencyChangeBinding
import com.blackhito.presentation.viewmodels.CurrencyViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CurrencyChangeFragment : Fragment() {
    private lateinit var binding: FragmentCurrencyChangeBinding
    private val viewModel: CurrencyViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCurrencyChangeBinding.inflate(inflater, container,false)
        binding.button.setOnClickListener {
            viewModel.load()
        }
        viewModel.liveData.observe(viewLifecycleOwner) {
            binding.text.text = it.toString()
        }
        return binding.root
    }
}