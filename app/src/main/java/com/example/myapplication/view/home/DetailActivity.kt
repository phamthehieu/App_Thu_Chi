package com.example.myapplication.view.home

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.adapter.ImageDetailAdapter
import com.example.myapplication.data.HistoryAccountWithAccount
import com.example.myapplication.data.IncomeExpenseListData
import com.example.myapplication.databinding.ActivityDetailBinding
import com.example.myapplication.entity.HistoryAccount
import com.example.myapplication.entity.IncomeExpenseList
import com.example.myapplication.view.revenue_and_expenditure.RevenueAndExpenditureActivity
import com.example.myapplication.viewModel.IncomeExpenseListFactory
import com.example.myapplication.viewModel.IncomeExpenseListModel
import com.google.gson.Gson
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    private var itemDetail: IncomeExpenseListData? = null

    private var itemAccount: HistoryAccount? = null

    private var listImage: MutableList<Uri> = mutableListOf()

    private val incomeExpenseListModel: IncomeExpenseListModel by viewModels {
        IncomeExpenseListFactory(this.application)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener {
            finish()
        }

        val json = intent.getStringExtra("itemDetail")

        val jsonAccount = intent.getStringExtra("accountDetail")

        if (json != null) {
            itemDetail = json.let {
                Gson().fromJson(it, IncomeExpenseListData::class.java)
            }
        } else if (jsonAccount != null) {
            itemAccount = jsonAccount.let {
                Gson().fromJson(it, HistoryAccount::class.java)
            }
        }

        binding.editDetailBtn.setOnClickListener {
            itemDetail?.let { dataDetail ->
                val gson = Gson()
                val data = gson.toJson(dataDetail)
                val intent = Intent(this, RevenueAndExpenditureActivity::class.java)
                intent.putExtra("itemToEdit", data)
                startActivity(intent)
            }
        }

        binding.deleteDetailBtn.setOnClickListener {
            conformDelete()
        }

        setUpBackground()

        setupData()
    }

    private fun conformDelete() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.layout_confirm_delete)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val backDeleteBtn = dialog.findViewById<View>(R.id.backDeleteBtn)
        val successDeleteBtn = dialog.findViewById<View>(R.id.successDeleteBtn)

        backDeleteBtn.setOnClickListener {
            dialog.dismiss()
        }

        successDeleteBtn.setOnClickListener {
            itemDetail?.let { itemData ->
                val itemToDelete = IncomeExpenseList(
                    id = itemData.id,
                    note = itemData.note,
                    amount = itemData.amount,
                    date = itemData.date,
                    categoryId = itemData.categoryId,
                    type = itemData.type,
                    image = itemData.image,
                    categoryName = itemData.categoryName,
                    iconResource = itemData.iconResource,
                    accountId = itemData.accountId
                )
                GlobalScope.launch {
                    incomeExpenseListModel.deleteIncomeExpenseListModel(itemToDelete)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@DetailActivity, "Đã xóa thành công", Toast.LENGTH_SHORT)
                            .show()
                        dialog.dismiss()
                        finish()
                    }
                }
            }
        }

        dialog.show()
    }


    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupData() {

        binding.iconIV.setImageResource(itemDetail?.iconResource ?: 0)
        binding.iconIV.setColorFilter(ContextCompat.getColor(this, R.color.black))

        binding.nameCategoryTV.text = itemDetail?.categoryName
        if (itemDetail?.type == "Income") {
            binding.nameTypeTV.text = getString(R.string.income)
        } else {
            binding.nameTypeTV.text = getString(R.string.expense)
        }

        val amount = itemDetail?.amount?.replace(",", ".")?.toDouble()
        val expenseFormatter = DecimalFormat("#,###.##")
        val amountFormat = expenseFormatter.format(amount)
        binding.amountTitleTV.text = amountFormat

        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val date = LocalDate.parse(itemDetail?.date, dateFormatter)
        binding.dateTitleTV.text = "${date.dayOfMonth} thg ${date.monthValue}, ${date.year}"

        binding.noteTitleTV.text = itemDetail?.note

        val dataImage = itemDetail?.image?.replace("[", "")?.replace("]", "")?.split(", ")
        dataImage?.distinct()?.forEach { imageString ->
            if (imageString.isNotBlank()) {
                val uri = Uri.parse(imageString)
                listImage.add(uri)
            }
        }

        if (listImage.isNotEmpty()) {
            val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            binding.recyclerImageView.layoutManager = layoutManager
            val adapter = ImageDetailAdapter(listImage)
            binding.recyclerImageView.adapter = adapter
            binding.recyclerImageView.visibility = View.VISIBLE
        } else {
            binding.recyclerImageView.visibility = View.GONE
        }
    }

    private fun setUpBackground() {
        val currentNightMode =
            this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {
                val color = ContextCompat.getColor(this, R.color.yellow)
                this.window.statusBarColor = color
                binding.title.setBackgroundColor(ContextCompat.getColor(this, R.color.yellow))
                binding.titleText.setTextColor(ContextCompat.getColor(this, R.color.black))
                binding.backBtn.setColorFilter(ContextCompat.getColor(this, R.color.black))
            }

            Configuration.UI_MODE_NIGHT_YES -> {
                val color = ContextCompat.getColor(this, R.color.grayHeader)
                this.window.statusBarColor = color
                binding.title.setBackgroundColor(ContextCompat.getColor(this, R.color.grayHeader))
                binding.titleText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.backBtn.setColorFilter(ContextCompat.getColor(this, R.color.white))
            }
        }
    }
}