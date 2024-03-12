package com.blackhito.presentation.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.blackhito.domain.preferences_storage.getBalanceFromCharCode
import com.blackhito.presentation.R
import com.blackhito.presentation.databinding.FragmentCurrencyBinding
import com.blackhito.presentation.util.Utils
import com.blackhito.presentation.viewmodels.CurrencyViewModel

class CurrencyFirstFragment : Fragment() {
    private lateinit var binding: FragmentCurrencyBinding
    private val viewModel: CurrencyViewModel by viewModels(ownerProducer = { requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCurrencyBinding.inflate(layoutInflater, container, false)

        viewModel.lowerCurrency.observe(viewLifecycleOwner) {
            binding.changeRatio.text = getString(
                R.string.exchange_rate,
                Utils.getCurrencySymbol(viewModel.upperCurrency.value?.charCode),
                viewModel.upperToLowerRatio,
                Utils.getCurrencySymbol(it.charCode)
            )
        }

        viewModel.upperCurrency.observe(viewLifecycleOwner) {
            binding.currencyName.text = it.charCode
            binding.currencyUserAmount.text = getString(
                R.string.current_currency_balance,
                viewModel.userBalance.getBalanceFromCharCode(it.charCode),
                Utils.getCurrencySymbol(it.charCode)
            )
            binding.changeRatio.text = getString(
                R.string.exchange_rate,
                Utils.getCurrencySymbol(it.charCode),
                viewModel.upperToLowerRatio,
                Utils.getCurrencySymbol(viewModel.lowerCurrency.value?.charCode)
            )
        }

        viewModel.userUpperInput.observe(viewLifecycleOwner) {
            Log.e("LOG", "userUpperInput - $it - ${viewModel.userLowerInput.value}")
            Utils.setEditTextInputSize(it.length, binding.currencyAmount)
            if (it.isEmpty())
                binding.textViewSign.visibility = View.INVISIBLE
            else
                binding.textViewSign.visibility = View.VISIBLE

            val selection = binding.currencyAmount.selectionEnd
            val length = it.length
            if (binding.currencyAmount.text.toString() != it)
                binding.currencyAmount.setText(
                    it.toString()
                )
            binding.currencyAmount.setSelection(
                selection.coerceAtMost(length)
            )
        }

        binding.textViewSign.text = getString(R.string.minus_sign)

        binding.currencyAmount.addTextChangedListener {
            Log.e("LOG", "userUpperInput.addTextChangedListener - $it")
            viewModel.setUpperInputField(it.toString())
        }

        return binding.root
    }

//   private fun setEditTextInputSize(length: Int, editText: EditText) {
//       when(length) {
//           in 0..5 -> editText.setTextSize(TypedValue.COMPLEX_UNIT_SP,48f)
//           in 6..10 -> editText.setTextSize(TypedValue.COMPLEX_UNIT_SP,32f)
//           in 11..15 -> editText.setTextSize(TypedValue.COMPLEX_UNIT_SP,24f)
//           in 16..20 -> editText.setTextSize(TypedValue.COMPLEX_UNIT_SP,16f)
//           else -> editText.setTextSize(TypedValue.COMPLEX_UNIT_SP,12f)
//       }
//   }
}