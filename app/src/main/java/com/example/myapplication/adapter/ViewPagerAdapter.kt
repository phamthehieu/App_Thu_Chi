package com.example.myapplication.adapter

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.myapplication.data.IncomeExpenseListData
import com.example.myapplication.view.revenue_and_expenditure.IncomeFragment
import com.example.myapplication.view.revenue_and_expenditure.SpendingFragment
import com.example.myapplication.view.revenue_and_expenditure.TransferFragment
import com.google.gson.Gson
import java.time.LocalDate

class ViewPagerAdapter(activity: FragmentActivity, private val itemEdit: IncomeExpenseListData?, private val date: LocalDate?) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                val fragment = SpendingFragment()
                val bundle = Bundle()
                itemEdit?.let {
                    if (it.type == "Expense") {
                        bundle.putString("itemEdit", Gson().toJson(it))
                        fragment.arguments = bundle
                    }
                }
                if (date != null) {
                    bundle.putString("dateSelected", date.toString())
                    fragment.arguments = bundle
                }

                fragment
            }
            1 -> {
                val fragment = IncomeFragment()
                val bundle = Bundle()
                itemEdit?.let {
                    if (it.type == "Income") {
                        bundle.putString("itemEdit", Gson().toJson(it))
                        fragment.arguments = bundle
                    }
                }
                if (date != null) {
                    bundle.putString("dateSelected", date.toString())
                    fragment.arguments = bundle
                }
                fragment
            }
            2 -> {
                val fragment = TransferFragment()
                val bundle = Bundle()
                itemEdit?.let {
                    if (it.type == "Transfer") {
                        bundle.putString("itemEdit", Gson().toJson(it))
                        fragment.arguments = bundle
                    }
                }
                if (date != null) {
                    bundle.putString("dateSelected", date.toString())
                    fragment.arguments = bundle
                }
                fragment
            }
            else -> TransferFragment()
        }
    }
}
