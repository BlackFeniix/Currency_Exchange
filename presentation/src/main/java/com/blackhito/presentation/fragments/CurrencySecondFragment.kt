package com.blackhito.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.blackhito.domain.preferences_storage.getBalanceFromCharCode
import com.blackhito.presentation.R
import com.blackhito.presentation.databinding.FragmentCurrencyBinding
import com.blackhito.presentation.util.Utils
import com.blackhito.presentation.viewmodels.CurrencyViewModel

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
        binding.textViewSign.text = getString(R.string.plus_sign)
        viewModel.upperCurrency.observe(viewLifecycleOwner) {
            binding.changeRatio.text = getString(
                R.string.exchange_rate,
                Utils.getCurrencySymbol(viewModel.lowerCurrency.value?.charCode),
                viewModel.lowerToUpperRatio,
                Utils.getCurrencySymbol(it.charCode)
            )
        }
        viewModel.lowerCurrency.observe(viewLifecycleOwner) {
            binding.currencyName.text = it.charCode
            binding.currencyUserAmount.text = getString(
                R.string.current_currency_balance,
                viewModel.userBalance.getBalanceFromCharCode(it.charCode),
                Utils.getCurrencySymbol(it.charCode)
            )
            binding.changeRatio.text = getString(
                R.string.exchange_rate,
                Utils.getCurrencySymbol(it.charCode),
                viewModel.lowerToUpperRatio,
                Utils.getCurrencySymbol(viewModel.upperCurrency.value?.charCode)
            )
        }

        viewModel.userLowerInput.observe(viewLifecycleOwner) {
            if (it.isEmpty())
                binding.textViewSign.visibility = View.INVISIBLE
            else
                binding.textViewSign.visibility = View.VISIBLE
            if (binding.currencyAmount.text.toString() != it)
                binding.currencyAmount.setText(it)
        }

        binding.currencyAmount.doAfterTextChanged {
            viewModel.lowerFieldInput = it.toString()
            viewModel.updateInputField(false)
            binding.currencyAmount.setSelection(it.toString().length)
        }

    }
}