package com.example.myapplication.view.reports

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplication.adapter.IconAddNewAccountAdapter
import com.example.myapplication.data.AccountType
import com.example.myapplication.databinding.ActivityAddNewAccountBinding
import com.example.myapplication.entity.Icon
import com.example.myapplication.viewModel.IconViewModel
import com.example.myapplication.viewModel.IconViewModelFactory

class AddNewAccountActivity : AppCompatActivity(), IconAddNewAccountAdapter.OnIconClickListener,
    BottomSheetTypeAccountFragment.OnAccountTypeSelectedListener {

    private lateinit var binding: ActivityAddNewAccountBinding

    private var selectedAccountType: AccountType? = null

    private val iconViewModel: IconViewModel by viewModels {
        IconViewModelFactory(application)
    }

    private var selectedIcon: Icon? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNewAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        iconViewModel.allIcons.observe(this, Observer { icons ->
            val filteredCategories = icons.filter { it.type == "Account" }

            val layoutManager = GridLayoutManager(this, 5)
            val adapter = IconAddNewAccountAdapter(filteredCategories, this)
            binding.recyclerIconNewAccountView.adapter = adapter
            binding.recyclerIconNewAccountView.layoutManager = layoutManager
        })

        binding.selectAccountType.setOnClickListener {
            val accountTypeBottomSheet = BottomSheetTypeAccountFragment()
            accountTypeBottomSheet.setOnAccountTypeSelectedListener(this)
            accountTypeBottomSheet.show(supportFragmentManager, accountTypeBottomSheet.tag)
        }
    }

    override fun onAccountTypeSelected(accountType: AccountType) {
        selectedAccountType = accountType
        binding.textTypeAccountTv.text = accountType.name
        binding.textTypeTitleAccountTv.text = accountType.type
    }

    override fun onIconClick(icon: Icon) {
        selectedIcon = icon
        Log.d("Hieu48", "Icon được chọn: $icon")
    }
}