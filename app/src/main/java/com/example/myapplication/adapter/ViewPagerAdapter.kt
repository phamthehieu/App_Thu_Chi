package com.example.myapplication.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.myapplication.view.revenue_and_expenditure.IncomeFragment
import com.example.myapplication.view.revenue_and_expenditure.SpendingFragment
import com.example.myapplication.view.revenue_and_expenditure.TransferFragment

class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    private val fragments = listOf(
        SpendingFragment(),
        IncomeFragment(),
        TransferFragment()
    )

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}