package com.example.myapplication.view.account

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplication.R
import com.example.myapplication.adapter.IconAddNewAccountAdapter
import com.example.myapplication.data.AccountIconFormat
import com.example.myapplication.data.AccountType
import com.example.myapplication.databinding.ActivityAddNewAccountBinding
import com.example.myapplication.entity.Account
import com.example.myapplication.entity.DailyReminder
import com.example.myapplication.entity.Icon
import com.example.myapplication.utilities.AccountTypeProvider.accountTypes
import com.example.myapplication.utilities.DeleteDialogUtils
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
    private var accountEdit: AccountIconFormat? = null

    private val accountTypeViewModel: AccountViewModel by viewModels {
        AccountViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNewAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        setupBackground()

        setupNightMode()

        val checkType = intent.getStringExtra("type")

        if (checkType == "edit") {
            setupEditAccount()
        }

    }

    private fun setupNightMode() {
        val currentNightMode = this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {
                val color = ContextCompat.getColor(this, R.color.yellow)
                this.window.statusBarColor = color
                binding.main.setBackgroundResource(R.color.background_keyboard)
                binding.title.setBackgroundResource(R.color.yellow)
                binding.addNewAccountBtn.setColorFilter(ContextCompat.getColor(this, R.color.black))
                binding.nameAccountEt.setBackgroundResource(R.drawable.background_white)
                binding.selectAccountType.setBackgroundResource(R.drawable.background_white)
                binding.iconType.setColorFilter(ContextCompat.getColor(this, R.color.black))
                binding.amountAccountLL.setBackgroundResource(R.drawable.background_white)
                binding.recyclerIconNewAccountView.setBackgroundResource(R.drawable.background_white)
                binding.noteAccountEt.setBackgroundResource(R.drawable.background_white)
                binding.deleteAccountBtn.setColorFilter(ContextCompat.getColor(this, R.color.black))
            }

            Configuration.UI_MODE_NIGHT_YES -> {
                val color = ContextCompat.getColor(this, R.color.grayHeader)
                this.window.statusBarColor = color
                binding.main.setBackgroundResource(R.color.black)
                binding.title.setBackgroundResource(R.color.grayHeader)
                binding.addNewAccountBtn.setColorFilter(ContextCompat.getColor(this, R.color.white))
                binding.nameAccountEt.setBackgroundResource(R.drawable.background_input)
                binding.selectAccountType.setBackgroundResource(R.drawable.background_input)
                binding.iconType.setColorFilter(ContextCompat.getColor(this, R.color.white))
                binding.amountAccountLL.setBackgroundResource(R.drawable.background_input)
                binding.recyclerIconNewAccountView.setBackgroundResource(R.drawable.background_input)
                binding.noteAccountEt.setBackgroundResource(R.drawable.background_input)
                binding.deleteAccountBtn.setColorFilter(ContextCompat.getColor(this, R.color.white))
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupEditAccount() {
        val account = intent.getParcelableExtra<AccountIconFormat>("Account")
        if (account != null) {
            accountEdit = account
            nameAccount = account.nameAccount
            noteAccount = account.note
            amountAccount = account.amountAccount
            typeAccount = account.typeAccount
            selectedIcon = Icon(
                id = account.icon,
                iconResource = account.iconResource,
                type = account.typeIcon,
            )

            val normalizedNumberSequence = account.amountAccount.replace(',', '.').toDouble()
            val expenseFormatter = DecimalFormat("#,###.##")

            binding.nameAccountEt.setText(account.nameAccount)
            binding.noteAccountEt.setText(account.note)
            binding.valueAmountAccountTv.text = expenseFormatter.format(normalizedNumberSequence)
            binding.textTypeAccountTv.text = accountTypes[account.typeAccount].name
            binding.textTypeTitleAccountTv.text = accountTypes[account.typeAccount].type
            binding.titleName.text = "Sửa"
            binding.deleteAccountBtn.visibility = android.view.View.VISIBLE

            binding.deleteAccountBtn.setOnClickListener {
                deleteAccount(account)
            }
            setupRecyclerView()
        } else {
            finish()
        }
    }

    private fun deleteAccount(account: AccountIconFormat) {
        DeleteDialogUtils.showDeleteDialog(
            this,
            account,
            accountTypeViewModel
        ) {}
    }

    private fun setupBackground() {
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

    private fun setupRecyclerView() {
        iconViewModel.allIcons.observe(this, Observer { icons ->
            val filteredCategories = icons.filter { it.type == "Account" }

            val layoutManager = GridLayoutManager(this, 5)
            val adapter = IconAddNewAccountAdapter(filteredCategories, this, selectedIcon)
            binding.recyclerIconNewAccountView.adapter = adapter
            binding.recyclerIconNewAccountView.layoutManager = layoutManager
            if (filteredCategories.isNotEmpty()) {
                adapter.notifyItemChanged(0)
            }
        })
    }

    private fun setupDataCreateAccount() {

        if (nameAccount.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên tài khoản", Toast.LENGTH_SHORT).show()
        } else if (amountAccount.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show()
        } else if (selectedIcon == null) {
            Toast.makeText(this, "Vui lòng chọn biểu tượng", Toast.LENGTH_SHORT).show()
        } else {
            val checkType = intent.getStringExtra("type")

            if (checkType == "edit") {
                val data = Account(
                    id = accountEdit!!.id,
                    nameAccount = nameAccount,
                    typeAccount = typeAccount,
                    amountAccount = amountAccount,
                    icon = selectedIcon!!.id,
                    note = noteAccount
                )

                accountTypeViewModel.updateAccount(data).observe(this) { result ->
                    if (result) {
                        Toast.makeText(this, "Sửa tài khoản thành công", Toast.LENGTH_SHORT).show()
                        val dataFormatWithIcon = AccountIconFormat(
                            id = accountEdit!!.id,
                            nameAccount = nameAccount,
                            typeAccount = typeAccount,
                            amountAccount = amountAccount,
                            icon = selectedIcon!!.id,
                            note = noteAccount,
                            iconResource = selectedIcon!!.iconResource,
                            typeIcon = selectedIcon!!.type
                        )
                        val resultIntent = Intent().apply {
                            putExtra("updatedAccount", dataFormatWithIcon)
                        }
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    } else {
                        Toast.makeText(this, "Sửa tài khoản thất bại", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
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
