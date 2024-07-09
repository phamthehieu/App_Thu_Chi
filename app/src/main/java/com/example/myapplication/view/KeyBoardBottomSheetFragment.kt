package com.example.myapplication.view

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.viewModels
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.example.myapplication.R
import com.example.myapplication.data.CombinedCategoryIcon
import com.example.myapplication.databinding.LayoutKeyboardAddBinding
import com.example.myapplication.view.calender.CalendarDialogFragment
import com.example.myapplication.viewModel.IncomeExpenseListFactory
import com.example.myapplication.viewModel.IncomeExpenseListModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.Calendar

class KeyBoardBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: LayoutKeyboardAddBinding

    private val numberSequence = StringBuilder()
    private val numberSequence2 = StringBuilder()
    private var total: BigDecimal = BigDecimal.ZERO

    private var selectedDate: String = ""
    private var categoryData: CombinedCategoryIcon? = null

    private val incomeExpenseListModel: IncomeExpenseListModel by viewModels {
        IncomeExpenseListFactory(requireContext())
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheet =
                (dialogInterface as BottomSheetDialog).findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as View?
            bottomSheet?.setBackgroundColor(resources.getColor(R.color.black, null))

            bottomSheet?.viewTreeObserver?.addOnGlobalLayoutListener {
                val rect = android.graphics.Rect()
                bottomSheet.getWindowVisibleDisplayFrame(rect)
                val screenHeight = bottomSheet.rootView.height
                val keypadHeight = screenHeight - rect.bottom

                if (keypadHeight > screenHeight * 0.15) {
                    hideCustomKeyboard()
                } else {
                    showCustomKeyboard()
                }
            }
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutKeyboardAddBinding.inflate(inflater, container, false)

        binding.nameCategoryEt.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showDefaultKeyboard()
            }
        }

        binding.nameCategoryEt.setOnClickListener {
            showDefaultKeyboard()
        }

        setupNumberButtons()

        return binding.root
    }

    private fun setupNumberButtons() {
        val buttonIds = listOf(
            binding.buttonZero, binding.buttonOne, binding.buttonTwo,
            binding.buttonThree, binding.buttonFour, binding.buttonFive,
            binding.buttonSix, binding.buttonSeven, binding.buttonEight,
            binding.buttonNine, binding.buttonPlus, binding.buttonMinus,
            binding.buttonComma, binding.buttonKeyBoardDelete,
            binding.successKeyBoardBtn, binding.buttonCalendar
        )

        for (button in buttonIds) {
            button.setOnClickListener { onNumberButtonClick(it) }
        }
    }

    private var calculationMark = ""
    private var calculation = false
    private fun onNumberButtonClick(view: View) {
        var mark = ""
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
            R.id.buttonCalendar -> {
                showCustomDialogAddCategory()
                ""
            }

            R.id.buttonKeyBoardDelete -> {
                if (numberSequence.isNotEmpty()) {
                    numberSequence.deleteCharAt(numberSequence.length - 1)
                } else {
                    if (calculationMark.isNotEmpty()) {
                        calculationMark = ""
                        binding.successKeyBoardBtn.setImageResource(R.drawable.ic_tick_30)
                        binding.successKeyBoardBtn.setBackgroundResource(R.color.yellow)
                    } else {
                        if (numberSequence2.isNotEmpty()) {
                            numberSequence2.deleteCharAt(numberSequence2.length - 1)
                        }
                    }
                }
                ""
            }

            R.id.buttonPlus -> {
                if (calculationMark.isNotEmpty()) {
                    if (numberSequence.isNotEmpty()) {
                        calculation = true
                        mark = " + "
                    } else {
                        calculationMark = " + "
                        binding.successKeyBoardBtn.setImageResource(R.drawable.ic_equal_sign_30)
                        binding.successKeyBoardBtn.setBackgroundResource(R.color.yellow)
                    }
                } else if (numberSequence.isNotEmpty()) {
                    numberSequence2.append(numberSequence.toString())
                    numberSequence.clear()
                    calculationMark = " + "
                    binding.successKeyBoardBtn.setImageResource(R.drawable.ic_equal_sign_30)
                    binding.successKeyBoardBtn.setBackgroundResource(R.color.yellow)
                } else {
                    if (numberSequence2.isNotEmpty()) {
                        calculationMark = " + "
                        binding.successKeyBoardBtn.setImageResource(R.drawable.ic_equal_sign_30)
                        binding.successKeyBoardBtn.setBackgroundResource(R.color.yellow)
                    } else {
                        numberSequence.append("0")
                    }

                }
                ""
            }

            R.id.buttonMinus -> {
                if (calculationMark.isNotEmpty()) {
                    if (numberSequence.isNotEmpty()) {
                        calculation = true
                        mark = " - "
                    } else {
                        calculationMark = " - "
                        binding.successKeyBoardBtn.setImageResource(R.drawable.ic_equal_sign_30)
                        binding.successKeyBoardBtn.setBackgroundResource(R.color.yellow)
                    }
                } else if (numberSequence.isNotEmpty()) {
                    numberSequence2.append(numberSequence.toString())
                    numberSequence.clear()
                    calculationMark = " - "
                    binding.successKeyBoardBtn.setImageResource(R.drawable.ic_equal_sign_30)
                    binding.successKeyBoardBtn.setBackgroundResource(R.color.yellow)
                } else {
                    if (numberSequence2.isNotEmpty()) {
                        calculationMark = " - "
                        binding.successKeyBoardBtn.setImageResource(R.drawable.ic_equal_sign_30)
                        binding.successKeyBoardBtn.setBackgroundResource(R.color.yellow)
                    }
                }
                ""
            }

            R.id.successKeyBoardBtn -> {
                if (numberSequence.isNotEmpty()) {
                    if (calculationMark != "") {
                        doCalculations(mark)
                        binding.successKeyBoardBtn.setImageResource(R.drawable.ic_tick_30)
                        binding.successKeyBoardBtn.setBackgroundResource(R.color.yellow)
                    } else {
                        saveDataToServer(1)
                    }
                } else {
                    if (numberSequence2.isNotEmpty()) {
                        if (calculationMark != "") {
                            calculationMark = ""
                            binding.successKeyBoardBtn.setImageResource(R.drawable.ic_tick_30)
                            binding.successKeyBoardBtn.setBackgroundResource(R.color.yellow)
                        } else {
                            saveDataToServer(2)
                        }
                    }
                }
                ""
            }

            else -> ""
        }

        if (numberToAdd == "," && numberSequence.contains(',')) {
            displayText = formatNumberWithDots(numberSequence.toString())

            if (numberSequence2.isNotEmpty()) {
                displayText =
                    formatNumberWithDots(numberSequence2.toString()) + calculationMark + displayText
            }

            binding.textViewNumberDisplay.text = displayText
        } else {
            if (calculation) {
                doCalculations(mark)
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

            if (numberSequence.toString() != "0" && numberSequence.isNotEmpty() || numberSequence2.isNotEmpty()) {
                binding.successKeyBoardBtn.setBackgroundResource(R.drawable.background_yellow)
                binding.successKeyBoardBtn.setColorFilter(Color.BLACK)
            } else {
                binding.successKeyBoardBtn.setBackgroundResource(R.drawable.background_gray)
                binding.successKeyBoardBtn.setColorFilter(Color.WHITE)
            }

            if (numberToAdd == "" && numberSequence.isEmpty() && numberSequence2.isEmpty()) {
                binding.textViewNumberDisplay.text = "0"
            } else {
                displayText = formatNumberWithDots(numberSequence.toString())

                if (numberSequence2.isNotEmpty()) {
                    displayText =
                        formatNumberWithDots(numberSequence2.toString()) + calculationMark + displayText
                }

                binding.textViewNumberDisplay.text = displayText
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

    private fun saveDataToServer(type: Int) {
        Log.d("Hieu", "284")
    }

    private fun doCalculations(mark: String) {
        val number1 = numberSequence2.toString().replace(',', '.').toBigDecimalOrNull()
        val number2 = numberSequence.toString().replace(',', '.').toBigDecimalOrNull()
        if (calculationMark == " + ") {
            if (number1 != null && number2 != null) {
                total = number1 + number2
            }
            numberSequence2.clear()
            numberSequence2.append(total.toString().replace('.', ','))
            numberSequence.clear()
            calculationMark = mark
            calculation = false
        } else {
            if (number1 != null && number2 != null) {
                total = number1 - number2
            }
            numberSequence2.clear()
            numberSequence2.append(total.toString().replace('.', ','))
            numberSequence.clear()
            calculationMark = mark
            calculation = false
        }
    }

    private fun formatNumberWithDots(number: String): String {

        val hasComma = number.contains(',')

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
                if (hasComma) {
                    if (decimalPart.isNotEmpty()) {
                        "$formattedIntegerPart,$decimalPart"
                    } else {
                        "$formattedIntegerPart,"
                    }
                } else {
                    formattedIntegerPart
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun onReceiveDate(year: String, month: String, dayOfMonth: String) {
        val selectedDate = "$dayOfMonth/$month/$year"
        val todayCalendar = Calendar.getInstance()
        val isSameDay = (year.toInt() == todayCalendar.get(Calendar.YEAR)
                && month.toInt() == todayCalendar.get(Calendar.MONTH) + 1
                && dayOfMonth.toInt() == todayCalendar.get(Calendar.DAY_OF_MONTH))
        if (!isSameDay) {
            val buttonCalendar = binding.buttonCalendar
            buttonCalendar.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            buttonCalendar.text = "$dayOfMonth thg $month $year"
            buttonCalendar.textSize = 14f
        }
    }

    fun categoryData(category: CombinedCategoryIcon) {
        categoryData = category
    }

    private fun showCustomDialogAddCategory() {
        val calendarDialogFragment = CalendarDialogFragment()
        calendarDialogFragment.setTargetFragment(this, 0)
        calendarDialogFragment.show(parentFragmentManager, "CalendarDialogFragment")
    }

    override fun onResume() {
        super.onResume()
        binding.nameCategoryEt.setOnEditorActionListener { _, _, _ ->
            hideKeyboard()
            showCustomKeyboard()
            true
        }
        showDefaultKeyboard()
    }

    private fun showDefaultKeyboard() {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.nameCategoryEt, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun showCustomKeyboard() {
        binding.keyboard.visibility = View.VISIBLE
        binding.keyboard.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(200)
            .start()
    }

    private fun hideCustomKeyboard() {
        binding.keyboard.animate()
            .alpha(0f)
            .translationY(binding.keyboard.height.toFloat())
            .setDuration(200)
            .withEndAction {
                binding.keyboard.visibility = View.GONE
            }
            .start()
    }

    private fun hideKeyboard() {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.nameCategoryEt.windowToken, 0)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}
