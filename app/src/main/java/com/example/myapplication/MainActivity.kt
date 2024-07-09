package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.view.chart.ChartFragment
import com.example.myapplication.view.home.HomeFragment
import com.example.myapplication.view.reports.ReportsFragment
import com.example.myapplication.view.revenue_and_expenditure.RevenueAndExpenditureActivity
import com.example.myapplication.view.user.UserFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(HomeFragment())
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_home -> replaceFragment(HomeFragment())
                R.id.bottom_chart -> replaceFragment(ChartFragment())
                R.id.bottom_report -> replaceFragment(ReportsFragment())
                R.id.bottom_user  -> replaceFragment(UserFragment())
            }
            true
        }

        binding.addFabBtn.setOnClickListener {
            startActivity(Intent(this, RevenueAndExpenditureActivity::class.java))
        }
        binding.addFabBtn.setColorFilter(ContextCompat.getColor(this, R.color.black1))
    }

    private fun replaceFragment(fragment: Fragment) {
        val bundle = Bundle()
        fragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.bottomFragment, fragment)
            .commit()
    }

}