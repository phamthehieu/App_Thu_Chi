package com.example.myapplication.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.myapplication.view.reports.AccountFragment
import com.example.myapplication.view.reports.AnalysisFragment

class ViewPagerReportAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    private val fragments: List<Fragment> = listOf(
        AnalysisFragment(),
        AccountFragment()
    )

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}