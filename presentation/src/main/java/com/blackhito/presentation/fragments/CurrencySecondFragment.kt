package com.blackhito.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.blackhito.presentation.R
import com.blackhito.presentation.databinding.FragmentCurrencyBinding
import com.blackhito.presentation.util.Utils
import com.blackhito.presentation.viewmodels.CurrencyViewModel
import com.blackhito.presentation.viewpager.FirstViewPagerAdapter.Companion.POSITION_FRAGMENT

class CurrencySecondFragment : Fragment() {
    private lateinit var binding: FragmentCurrencyBinding
    private val viewModel: CurrencyViewModel by viewModels(ownerProducer = { requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCurrencyBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.takeIf { it.containsKey(POSITION_FRAGMENT) }?.apply {
            val position = getInt(POSITION_FRAGMENT)
            viewModel.updatePosition(position, CurrencyViewModel.SECOND_WINDOW)

            viewModel.secondCurrencyPresentation.observe(viewLifecycleOwner) {
                binding.currencyName.text = it.charCode
                binding.currencyUserAmount.text = getString(
                    R.string.current_amount_currency,
                    it.userBalance,
                    Utils.getCurrencySymbol(it.charCode)
                )
                binding.changeRatio.text = getString(
                    R.string.exchange_rate,
                    Utils.getCurrencySymbol(it.charCode),
                    viewModel.currentExchangeRatioSecond.value?.toDouble(),
                    Utils.getCurrencySymbol(viewModel.firstCurrencyPresentation.value?.charCode)
                )
                if (it.userInputAmount!=0.0)
                    binding.currencyAmount.setText(it.userInputAmount.toString())
            }
//            binding.currencyAmount.addTextChangedListener(
//                afterTextChanged = {
//                    viewModel.changeFirstCurrencyPresentation(it.toString())
//                }
//            )
        }
    }
}