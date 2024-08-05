package com.example.myapplication.view.account

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplication.adapter.IconAddNewAccountAdapter
import com.example.myapplication.data.AccountType
import com.example.myapplication.databinding.ActivityAddNewAccountBinding
import com.example.myapplication.entity.Account
import com.example.myapplication.entity.Icon
import com.example.myapplication.viewModel.AccountViewModel
import com.example.myapplication.viewModel.AccountViewModelFactory
import com.example.myapplication.viewModel.IconViewModel
import com.example.myapplication.viewModel.IconViewModelFactory
import java.text.DecimalFormat

class AddNewAccountActivity : AppCompatActivity(),
    IconAddNewAccountAdapter.OnIconClickListener,
    BottomSheetTypeAccountFragment.OnAccountTypeSelectedListener,
    KeyBroadBottomSheetAccountFragment.OnNumberSequenceListener {

    private lateinit var binding: ActivityAddNewAccountBinding

    private val iconViewModel: IconViewModel by viewModels {
        IconViewModelFactory(application)
    }
    private var selectedIcon: Icon? = null
    private var amountAccount: String = ""
    private var nameAccount: String = ""
    private var noteAccount: String = ""
    private var typeAccount: Int = 0
    private var selectedAccountType: AccountType? = null

    private val accountTypeViewModel: AccountViewModel by viewModels {
        AccountViewModelFactory(application)
    }

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

        binding.amountAccountLL.setOnClickListener {
            val keyboardBottomSheet = KeyBroadBottomSheetAccountFragment()
            if (amountAccount.isNotEmpty()) {
                val bundle = Bundle()
                val normalizedNumberSequence = amountAccount.replace(',', '.')
                val number = normalizedNumberSequence.toDouble()
                bundle.putDouble("amountAccount", number)
                keyboardBottomSheet.arguments = bundle
            }
            keyboardBottomSheet.show(supportFragmentManager, keyboardBottomSheet.tag)
        }

        binding.backBtn.setOnClickListener {
            finish()
        }


        binding.nameAccountEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                nameAccount = s.toString()
            }

        })

        binding.noteAccountEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                noteAccount = s.toString()
            }

        })
        binding.addNewAccountBtn.setOnClickListener {
            setupDataCreateAccount()
        }

    }

    private fun setupDataCreateAccount() {
        if (nameAccount.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên tài khoản", Toast.LENGTH_SHORT).show()
        } else if (amountAccount.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show()
        } else if (selectedIcon == null) {
            Toast.makeText(this, "Vui lòng chọn biểu tượng", Toast.LENGTH_SHORT).show()
        } else {
            val data = Account(
                nameAccount = nameAccount,
                typeAccount = typeAccount,
                amountAccount = amountAccount,
                icon = selectedIcon!!.id,
                note = noteAccount
            )
            accountTypeViewModel.insert(data).observe(this) { result ->
                if (result) {
                    Toast.makeText(this, "Thêm tài khoản thành công", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Thêm tài khoản thất bại", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    override fun onAccountTypeSelected(accountType: AccountType, position: Int) {
        selectedAccountType = accountType
        binding.textTypeAccountTv.text = accountType.name
        binding.textTypeTitleAccountTv.text = accountType.type
        typeAccount = position
    }

    override fun onIconClick(icon: Icon) {
        selectedIcon = icon
    }

    override fun onNumberSequenceEntered(numberSequence: String) {
        try {
            val normalizedNumberSequence = numberSequence.replace(',', '.')
            val number = normalizedNumberSequence.toDouble()
            amountAccount = numberSequence
            val expenseFormatter = DecimalFormat("#,###.##")
            val formattedNumberSequence = expenseFormatter.format(number)

            binding.valueAmountAccountTv.text = formattedNumberSequence
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }
    }

}
