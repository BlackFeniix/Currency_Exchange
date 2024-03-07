package com.blackhito.presentation.viewpager

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.blackhito.presentation.fragments.CurrencySecondFragment

class SecondViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int): Fragment {
        val fragment = CurrencySecondFragment()
        fragment.arguments = Bundle().apply {
            putInt(POSITION_FRAGMENT, position)
        }

        return fragment
    }

    companion object {
        const val POSITION_FRAGMENT = "POSITION_FRAGMENT"
    }
}