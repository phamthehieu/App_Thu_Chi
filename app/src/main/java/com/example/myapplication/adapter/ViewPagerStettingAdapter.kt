package com.example.myapplication.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.myapplication.view.category.ExpenseSettingFragment
import com.example.myapplication.view.category.IncomeSettingFragment

class ViewPagerStettingAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    private val fragments: List<Fragment> = listOf(
        ExpenseSettingFragment(),
        IncomeSettingFragment()
    )

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}
