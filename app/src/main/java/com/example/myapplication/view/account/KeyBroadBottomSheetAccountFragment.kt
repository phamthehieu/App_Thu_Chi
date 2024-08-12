package com.example.myapplication.view.account

import android.app.Dialog
import android.content.Context
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
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentKeyBroadBottomSheetAccountBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.math.BigDecimal
import java.text.DecimalFormat

class KeyBroadBottomSheetAccountFragment : BottomSheetTypeAccountFragment() {

    private lateinit var binding: FragmentKeyBroadBottomSheetAccountBinding

    private val numberSequence = StringBuilder()
    private var numberSequence2 = StringBuilder()
    private var total: BigDecimal = BigDecimal.ZERO

    interface OnNumberSequenceListener {
        fun onNumberSequenceEntered(numberSequence: String)
    }

    private lateinit var numberSequenceListener: OnNumberSequenceListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnNumberSequenceListener) {
            numberSequenceListener = context
        } else {
            throw RuntimeException("$context must implement OnNumberSequenceListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheet =
                (dialogInterface as BottomSheetDialog).findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundColor(resources.getColor(R.color.black, null))
        }
        return dialog
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentKeyBroadBottomSheetAccountBinding.inflate(inflater, container, false)

        val amountAccount = arguments?.getDouble("amountAccount", 0.0) ?: 0.0
        if (amountAccount != 0.0) {
            numberSequence.clear()
            val formattedAmount = BigDecimal(amountAccount).toPlainString()
            val formattedAmountWithComma = formattedAmount.replace('.', ',')
            numberSequence.append(formattedAmountWithComma)
            binding.textViewNumberDisplay.text = DecimalFormat("#,###.##").format(amountAccount)
        }

        setupNumberButtons()

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
                    binding.buttonPlus,
                    binding.buttonMinus
                )

                val buttonsWithColorFilter = listOf(
                    binding.buttonKeyBoardDelete,
                    binding.successKeyBoardBtn,
                    binding.buttonClose
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


    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupNumberButtons() {
        val buttonIds = listOf(
            binding.buttonZero, binding.buttonOne, binding.buttonTwo,
            binding.buttonThree, binding.buttonFour, binding.buttonFive,
            binding.buttonSix, binding.buttonSeven, binding.buttonEight,
            binding.buttonNine, binding.buttonPlus, binding.buttonMinus,
            binding.buttonComma, binding.buttonKeyBoardDelete,
            binding.successKeyBoardBtn, binding.buttonClose
        )

        for (button in buttonIds) {
            button.setOnClickListener { onNumberButtonClick(it) }
        }
    }

    private var calculationMark = ""
    private var calculation = false

    @RequiresApi(Build.VERSION_CODES.O)
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
            R.id.buttonClose -> {
                dismiss()
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
                        numberSequenceListener.onNumberSequenceEntered(numberSequence.toString())
                        dismiss()
                    }
                } else {
                    if (numberSequence2.isNotEmpty()) {
                        if (calculationMark != "") {
                            calculationMark = ""
                            binding.successKeyBoardBtn.setImageResource(R.drawable.ic_tick_30)
                            binding.successKeyBoardBtn.setBackgroundResource(R.color.yellow)
                        } else {
                            numberSequenceListener.onNumberSequenceEntered(numberSequence2.toString())
                            dismiss()
                        }
                    }
                }
                ""
            }

            else -> ""
        }
        Log.d("Hieu48", "Number to add: $numberToAdd - $numberSequence")
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
}