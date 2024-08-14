package com.example.myapplication.view.reports

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import com.example.myapplication.R
import com.example.myapplication.data.CombinedCategoryIcon
import com.example.myapplication.data.IncomeExpenseListData
import com.example.myapplication.databinding.FragmentKeyBroadBottomReportsBinding
import com.example.myapplication.entity.Category
import com.example.myapplication.viewModel.CategoryViewModel
import com.example.myapplication.viewModel.CategoryViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.DecimalFormat
import java.time.LocalDate

class KeyBroadBottomReportsFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentKeyBroadBottomReportsBinding

    private var numberSequence = StringBuilder()

    private var categoryData: CombinedCategoryIcon? = null

    private val categoryViewModel: CategoryViewModel by viewModels {
        CategoryViewModelFactory(requireActivity().application)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheet =
                (dialogInterface as BottomSheetDialog).findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundColor(resources.getColor(R.color.black, null))

            bottomSheet?.viewTreeObserver?.addOnGlobalLayoutListener {
                val rect = android.graphics.Rect()
                bottomSheet.getWindowVisibleDisplayFrame(rect)
            }
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentKeyBroadBottomReportsBinding.inflate(inflater, container, false)

        setupNumberButtons()

        binding.nameCategoryTv.text = categoryData?.categoryName

        binding.textViewNumberDisplay.text = formatNumberWithDots(numberSequence.toString())

        setupNightMode()

        return binding.root
    }

    private fun setupNightMode() {
        val currentNightMode =
            this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {
                val buttonsWithText = listOf(
                    binding.buttonSeven,
                    binding.buttonEight,
                    binding.buttonNine,
                    binding.buttonFour,
                    binding.buttonFive,
                    binding.buttonSix,
                    binding.buttonOne,
                    binding.buttonTwo,
                    binding.buttonZero,
                    binding.buttonThree,
                    binding.buttonComma,
                    binding.none
                )

                val buttonsWithColorFilter = listOf(
                    binding.buttonKeyBoardDelete,
                    binding.successKeyBoardBtn,
                    binding.buttonClose,
                    binding.buttonTrash
                )

                for (button in buttonsWithText) {
                    button.setBackgroundResource(R.drawable.background_white)
                    button.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                }

                for (button in buttonsWithColorFilter) {
                    button.setBackgroundResource(R.drawable.background_white)
                    button.setColorFilter(ContextCompat.getColor(requireContext(), R.color.black))
                }

                binding.note.setBackgroundResource(R.drawable.background_white)
                binding.main.setBackgroundResource(R.color.background_keyboard)
            }

            Configuration.UI_MODE_NIGHT_YES -> {
                binding.main.setBackgroundResource(R.color.black)
            }
        }
    }

    @SuppressLint("NewApi")
    private fun setupNumberButtons() {
        val buttonIds = listOf(
            binding.buttonZero, binding.buttonOne, binding.buttonTwo,
            binding.buttonThree, binding.buttonFour, binding.buttonFive,
            binding.buttonSix, binding.buttonSeven, binding.buttonEight,
            binding.buttonNine, binding.buttonClose, binding.buttonTrash,
            binding.buttonComma, binding.buttonKeyBoardDelete,
            binding.successKeyBoardBtn,
        )

        for (button in buttonIds) {
            button.setOnClickListener { onNumberButtonClick(it) }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onNumberButtonClick(view: View) {
        var displayText = ""
        val numberToAdd = when (view.id) {
            R.id.buttonZero -> "0"
            R.id.buttonOne -> "1"
            R.id.buttonTwo -> "2"
            R.id.buttonThree -> "3"
            R.id.buttonFour -> "4"
            R.id.buttonFive -> "5"
            R.id.buttonSix -> "6"
            R.id.buttonSeven -> "7"
            R.id.buttonEight -> "8"
            R.id.buttonNine -> "9"
            R.id.buttonComma -> ","
            R.id.buttonClose -> {
                dismiss()
                ""
            }

            R.id.buttonKeyBoardDelete -> {
                if (numberSequence.isNotEmpty()) {
                    numberSequence.deleteCharAt(numberSequence.length - 1)
                }
                ""
            }

            R.id.buttonTrash -> {
                numberSequence.clear()
                ""
            }

            R.id.successKeyBoardBtn -> {
                updateBudgetCategory()
                ""
            }

            else -> ""
        }

        if (numberToAdd == "," && numberSequence.contains(',')) {
            displayText = formatNumberWithDots(numberSequence.toString())
            binding.textViewNumberDisplay.text = displayText
        } else {
            if (numberToAdd != "") {
                if (numberSequence.isNotEmpty()) {
                    if (canAddMoreDigits(numberSequence.toString())) {
                        if (numberSequence.length < 10) {
                            numberSequence.append(numberToAdd)
                        } else {
                            numberSequence.setCharAt(numberSequence.length - 1, numberToAdd[0])
                        }
                    } else {
                        numberSequence.setCharAt(numberSequence.length - 1, numberToAdd[0])
                    }
                } else {
                    if (numberSequence.length < 10) {
                        numberSequence.append(numberToAdd)
                    } else {
                        numberSequence.setCharAt(numberSequence.length - 1, numberToAdd[0])
                    }
                }
            }
        }
        binding.textViewNumberDisplay.text = formatNumberWithDots(numberSequence.toString())
    }

    private fun updateBudgetCategory(type: String = "") {
        val categoryNew = categoryData?.let {
            Category(
                id = it.idCategory,
                name = it.categoryName,
                icon = it.icon,
                type = it.categoryType,
                source = it.source,
                budget = numberSequence.toString()
            )
        }

        categoryNew?.let {
            categoryViewModel.viewModelScope.launch(Dispatchers.IO) {
                val insertedId = categoryViewModel.update(it)
                if (insertedId.toString() == "kotlin.Unit") {
                    dismiss()
                }
            }
        }
    }

    fun setupDataCategory(category: CombinedCategoryIcon) {
        categoryData = category
        numberSequence.append(category.budget)
    }
}

private fun formatNumberWithDots(number: String): String {
    val hasComma = number.contains(',')

    val isNegative = number.startsWith('-')

    val cleanNumber = number.replace("[^0-9,]".toRegex(), "")
    return when {
        cleanNumber.isEmpty() -> ""
        else -> {
            val parts = cleanNumber.split(',')
            val integerPart = parts[0]
            val decimalPart = if (parts.size > 1) parts[1] else ""
            val formatter = DecimalFormat("#,###")
            formatter.groupingSize = 3
            formatter.isGroupingUsed = true

            val formattedIntegerPart = formatter.format(BigDecimal(integerPart))
            val result = if (hasComma) {
                if (decimalPart.isNotEmpty()) {
                    "$formattedIntegerPart,$decimalPart"
                } else {
                    "$formattedIntegerPart,"
                }
            } else {
                formattedIntegerPart
            }

            if (isNegative) "-$result" else result
        }
    }
}

private fun canAddMoreDigits(number: String): Boolean {
    val parts = number.split(",")
    return if (parts.size > 1) {
        parts[1].length < 2
    } else {
        true
    }

}

