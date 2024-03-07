package com.blackhito.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.blackhito.domain.NetworkState
import com.blackhito.presentation.R
import com.blackhito.presentation.databinding.FragmentCurrencyChangeBinding
import com.blackhito.presentation.model.TransactionStatus
import com.blackhito.presentation.util.Utils
import com.blackhito.presentation.viewmodels.CurrencyViewModel
import com.blackhito.presentation.viewpager.FirstViewPagerAdapter
import com.blackhito.presentation.viewpager.SecondViewPagerAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CurrencyChangeFragment : Fragment() {
    private lateinit var binding: FragmentCurrencyChangeBinding
    private val viewModel: CurrencyViewModel by viewModels()

    private lateinit var firstViewPagerAdapter: FirstViewPagerAdapter
    private lateinit var secondViewPagerAdapter: SecondViewPagerAdapter

    private lateinit var viewPager: ViewPager2
    private lateinit var viewPager2: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCurrencyChangeBinding.inflate(inflater, container, false)
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.networkStateFlow.collect {
                    when (it) {
                        is NetworkState.NetworkDownload -> {
                            binding.firstWindow.visibility = View.INVISIBLE
                            binding.secondWindow.visibility = View.INVISIBLE
                            binding.imageView.visibility = View.INVISIBLE
                            binding.toolbarTitle.visibility = View.INVISIBLE
                            binding.toolbarButton.visibility = View.INVISIBLE
                            viewModel.loadCurrencyFromNetwork()

                        }

                        is NetworkState.NetworkSuccess -> {
                            binding.firstWindow.visibility = View.VISIBLE
                            binding.secondWindow.visibility = View.VISIBLE
                            binding.imageView.visibility = View.VISIBLE
                            binding.toolbarTitle.visibility = View.VISIBLE
                            binding.toolbarButton.visibility = View.VISIBLE
                            showViewPager()
                        }

                        is NetworkState.NetworkError -> {}
                    }
                }
            }
        }
        return binding.root
    }

    private fun showViewPager() {
        firstViewPagerAdapter =
            FirstViewPagerAdapter(this)
        secondViewPagerAdapter =
            SecondViewPagerAdapter(this)

        viewPager = binding.firstWindow
        viewPager2 = binding.secondWindow

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewModel.updatePosition(position, CurrencyViewModel.FIRST_WINDOW)
            }
        })
        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewModel.updatePosition(position, CurrencyViewModel.SECOND_WINDOW)
            }
        })

        viewPager.adapter = firstViewPagerAdapter
        viewPager2.adapter = secondViewPagerAdapter

        binding.toolbarButton.setOnClickListener {
            viewModel.updateUserBalance()
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.transactionStatus.collect {
                    when (it) {
                        is TransactionStatus.ErrorEmptyFields -> {

                        }

                        is TransactionStatus.ErrorNotEnoughCurrency -> {

                        }

                        is TransactionStatus.ErrorSameCurrency -> {

                        }

                        is TransactionStatus.Success -> {
                            MaterialAlertDialogBuilder(requireContext())
                                .setMessage("Success")
                                .setCancelable(false)
                                .setPositiveButton("ok") { _, _ ->
                                }
                                .show()
                        }
                    }
                }
            }
        }
        lifecycleScope.launch {
            viewModel.currentExchangeRatioFirst.observe(viewLifecycleOwner) {
                binding.toolbarTitle.text = getString(
                    R.string.exchange_rate,
                    Utils.getCurrencySymbol(viewModel.firstCurrencyPresentation.value?.charCode),
                    viewModel.currentExchangeRatioFirst.value?.toDouble(),
                    Utils.getCurrencySymbol(viewModel.secondCurrencyPresentation.value?.charCode)
                )
            }
        }
    }
}