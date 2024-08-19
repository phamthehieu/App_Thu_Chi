package com.example.myapplication.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.myapplication.view.revenue_and_expenditure.IncomeFragment
import com.example.myapplication.view.revenue_and_expenditure.SpendingFragment
import com.example.myapplication.view.revenue_and_expenditure.TransferFragment
import com.google.gson.Gson
import java.time.LocalDate

class ViewPagerSearchAdapter(
    activity: FragmentActivity
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                val fragment = SpendingFragment()
                val bundle = Bundle()
                bundle.putString("dataSearch", "dataSearch")
                fragment.arguments = bundle
                fragment
            }

            1 -> {
                val fragment = IncomeFragment()
                val bundle = Bundle()
                bundle.putString("dataSearch", "dataSearch")
                fragment.arguments = bundle
                fragment
            }

            else -> SpendingFragment()
        }
    }
}