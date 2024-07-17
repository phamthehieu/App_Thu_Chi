package com.example.myapplication.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.myapplication.data.IncomeExpenseListData
import com.example.myapplication.view.revenue_and_expenditure.IncomeFragment
import com.example.myapplication.view.revenue_and_expenditure.SpendingFragment
import com.example.myapplication.view.revenue_and_expenditure.TransferFragment
import com.google.gson.Gson

class ViewPagerAdapter(activity: FragmentActivity, private val itemEdit: IncomeExpenseListData?) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                val fragment = SpendingFragment()
                itemEdit?.let {
                    if (it.type == "Expense") {
                        val bundle = Bundle()
                        bundle.putString("itemEdit", Gson().toJson(it))
                        fragment.arguments = bundle
                    }
                }
                fragment
            }
            1 -> {
                val fragment = IncomeFragment()
                itemEdit?.let {
                    if (it.type == "Income") {
                        val bundle = Bundle()
                        bundle.putString("itemEdit", Gson().toJson(it))
                        fragment.arguments = bundle
                    }
                }
                fragment
            }
            else -> TransferFragment()
        }
    }
}
