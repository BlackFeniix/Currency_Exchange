package com.blackhito.presentation.fragments

import android.os.Bundle
import android.util.Log
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
import com.blackhito.presentation.util.Utils
import com.blackhito.presentation.viewmodels.CurrencyViewModel
import com.blackhito.presentation.viewpager.FirstViewPagerAdapter
import com.blackhito.presentation.viewpager.SecondViewPagerAdapter
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
                            binding.progressBar.visibility = View.VISIBLE
                            binding.upperWindow.visibility = View.INVISIBLE
                            binding.lowerWindow.visibility = View.INVISIBLE
                            binding.imageViewArrow.visibility = View.INVISIBLE
                            binding.toolbarTitle.visibility = View.INVISIBLE
                            binding.toolbarButton.visibility = View.INVISIBLE
                            viewModel.updateAllDataRegularly()
                        }

                        is NetworkState.NetworkSuccess -> {
                            binding.progressBar.visibility = View.INVISIBLE
                            binding.upperWindow.visibility = View.VISIBLE
                            binding.lowerWindow.visibility = View.VISIBLE
                            binding.imageViewArrow.visibility = View.VISIBLE
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

        viewPager = binding.upperWindow
        viewPager2 = binding.lowerWindow

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.e("LOG", "Upper pager - $position")
                viewModel.updatePosition(position, true)
            }
        })
        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.e("LOG", "Lower pager - $position")
                viewModel.updatePosition(position,false)
            }
        })

        viewPager.adapter = firstViewPagerAdapter
        viewPager2.adapter = secondViewPagerAdapter

        binding.toolbarButton.setOnClickListener {
            viewModel.checkRequirementsForTransaction()
        }

        lifecycleScope.launch {
            viewModel.textNotification.observe(viewLifecycleOwner) {
                val exchangeDialogFragment = ExchangeDialogFragment(it)
                exchangeDialogFragment.show(childFragmentManager, "alertDialog")
            }
        }

        lifecycleScope.launch {
            viewModel.upperCurrency.observe(viewLifecycleOwner) {
                binding.toolbarTitle.text = getString(
                    R.string.exchange_rate,
                    Utils.getCurrencySymbol(viewModel.upperCurrency.value?.charCode),
                    viewModel.upperToLowerRatio,
                    Utils.getCurrencySymbol(viewModel.lowerCurrency.value?.charCode)
                )
            }
        }
        lifecycleScope.launch {
            viewModel.lowerCurrency.observe(viewLifecycleOwner) {
                binding.toolbarTitle.text = getString(
                    R.string.exchange_rate,
                    Utils.getCurrencySymbol(viewModel.upperCurrency.value?.charCode),
                    viewModel.upperToLowerRatio,
                    Utils.getCurrencySymbol(viewModel.lowerCurrency.value?.charCode)
                )
            }
        }

    }
}