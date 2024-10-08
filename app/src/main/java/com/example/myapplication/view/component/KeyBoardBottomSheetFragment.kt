package com.example.myapplication.view.component

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.viewModels
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.data.AccountIconFormat
import com.example.myapplication.data.CombinedCategoryIcon
import com.example.myapplication.data.IncomeExpenseListData
import com.example.myapplication.databinding.LayoutKeyboardAddBinding
import com.example.myapplication.entity.IncomeExpenseList
import com.example.myapplication.view.account.BottomSelectedAccountFragment
import com.example.myapplication.view.calendar.CalendarDialogFragment
import com.example.myapplication.viewModel.AccountViewModel
import com.example.myapplication.viewModel.AccountViewModelFactory
import com.example.myapplication.viewModel.ImageViewModel
import com.example.myapplication.viewModel.IncomeExpenseListFactory
import com.example.myapplication.viewModel.IncomeExpenseListModel
import com.example.myapplication.viewModel.SharedAccountSelectedModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.math.BigDecimal
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

class KeyBoardBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: LayoutKeyboardAddBinding

    private val numberSequence = StringBuilder()
    private var numberSequence2 = StringBuilder()
    private var total: BigDecimal = BigDecimal.ZERO
    private var checkEdit: Boolean = false

    private var selectedDate = LocalDate.now()

    private var categoryData: CombinedCategoryIcon? = null
    private var nameCategory: String = ""

    private var listImage: MutableList<Uri> = mutableListOf()
    private var itemEdit: IncomeExpenseListData? = null

    private val imageViewModel: ImageViewModel by activityViewModels()

    private var selectedAccount: AccountIconFormat? = null

    private val sharedViewModel: SharedAccountSelectedModel by activityViewModels()

    private val incomeExpenseListModel: IncomeExpenseListModel by viewModels {
        IncomeExpenseListFactory(requireActivity().application)
    }

    private val accountTypeViewModel: AccountViewModel by viewModels {
        AccountViewModelFactory(requireActivity().application)
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

        setupBackground()

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
                    binding.successKeyBoardBtn
                )

                for (button in buttonsWithText) {
                    button.setBackgroundResource(R.drawable.background_white)
                    button.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                }

                for (button in buttonsWithColorFilter) {
                    button.setBackgroundResource(R.drawable.background_white)
                    button.setColorFilter(ContextCompat.getColor(requireContext(), R.color.black))
                }
                binding.buttonCalendar.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.yellow
                    )
                )
                binding.buttonCalendar.setBackgroundResource(R.drawable.background_white)
                binding.selectedAccount.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
                binding.note.setBackgroundResource(R.drawable.background_white)
                binding.imageArtists.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
            }

            Configuration.UI_MODE_NIGHT_YES -> {

            }
        }
    }

    private fun setupBackground() {
        binding.nameCategoryEt.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showDefaultKeyboard()
            }
        }

        binding.nameCategoryEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                nameCategory = s.toString()
            }

        })

        binding.nameCategoryEt.setOnClickListener {
            showDefaultKeyboard()
        }


        binding.selectedImageBtn.setOnClickListener {
            if (listImage.size >= 1) {
                val keyboard = CustomListPhotoFragment()
                keyboard.show(childFragmentManager, "customListPhoto")
            } else {
                val keyboard = BottomSheetSelectedImageFragment()
                keyboard.show(childFragmentManager, "bottomSheetSelectedImage")
            }

        }

        sharedViewModel.selectedAccount.observe(viewLifecycleOwner) { account ->
            selectedAccount = account
            binding.selectedAccount.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.yellow
                )
            )
        }

        binding.selectedAccount.setOnClickListener {
            val keyboard = BottomSelectedAccountFragment()
            keyboard.show(childFragmentManager, "bottomSheetSelectedAccount")
        }

        imageViewModel.imageUris.observe(viewLifecycleOwner, Observer { uris ->
            listImage.clear()
            listImage.addAll(uris)
            if (uris.size >= 1) {
                binding.totalPhotoEt.visibility = View.VISIBLE
                binding.totalPhotoEt.text = uris.size.toString()
            } else {
                binding.totalPhotoEt.visibility = View.GONE
            }
        })

        setupNumberButtons()

        itemEdit?.let {
            listImage.clear()
            imageViewModel.clearImageUris()
            numberSequence.append(it.amount)
            val displayText = formatNumberWithDots(numberSequence.toString())
            binding.textViewNumberDisplay.text = displayText
            val dateString = it.date
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            selectedDate = LocalDate.parse(dateString, formatter)
            onReceiveDate(
                selectedDate.year.toString(),
                selectedDate.monthValue.toString(),
                selectedDate.dayOfMonth.toString()
            )
            val dataImage = it.image.replace("[", "").replace("]", "").split(", ")
            dataImage.distinct().forEach { imageString ->
                if (imageString.isNotEmpty()) {
                    val uri = Uri.parse(imageString)
                    if (!listImage.contains(uri)) {
                        listImage.add(uri)
                        imageViewModel.addImageUri(uri)
                    }
                }
            }
        }

        onReceiveDate(
            selectedDate.year.toString(),
            selectedDate.monthValue.toString(),
            selectedDate.dayOfMonth.toString()
        )

        if (checkEdit) {
            if (itemEdit!!.accountId.toInt() != -1) {
                accountTypeViewModel.getAccountsByAccountId(itemEdit!!.accountId.toInt())
                    .observe(viewLifecycleOwner) { account ->
                        val data = AccountIconFormat(
                            id = account.account.id,
                            nameAccount = account.account.nameAccount,
                            typeAccount = account.account.typeAccount,
                            amountAccount = account.account.amountAccount,
                            icon = account.account.icon,
                            note = account.account.note,
                            iconResource = account.icon.iconResource,
                            typeIcon = account.icon.type
                        )
                        selectedAccount = data
                        sharedViewModel.selectAccount(data)
                    }
            } else {
                val data = AccountIconFormat(
                    id = -1,
                    nameAccount = "",
                    typeAccount = -1,
                    amountAccount = "",
                    icon = -1,
                    note = "",
                    iconResource = -1,
                    typeIcon = "Account"
                )
                selectedAccount = data
                sharedViewModel.selectAccount(data)
            }
        } else {
            val sharedPreferences =
                requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val switchState = sharedPreferences.getBoolean("switchState", false)

            if (switchState) {
                val keyboard = BottomSelectedAccountFragment()
                keyboard.show(childFragmentManager, "bottomSheetSelectedAccount")
            }
        }

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

        if (numberSequence.isEmpty() && numberToAdd === ",") {
            binding.textViewNumberDisplay.text = "0"
        } else {
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
                                    numberSequence.setCharAt(
                                        numberSequence.length - 1,
                                        numberToAdd[0]
                                    )
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
        val accountId = if (selectedAccount == null) "-1" else selectedAccount!!.id.toString()

        if (!checkEdit) {
            if (type == 1) {
                if (numberSequence.isEmpty()) {
                    Toast.makeText(requireContext(), "Vui lòng nhập số tiền!", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    val incomeExpenseList = IncomeExpenseList(
                        note = nameCategory,
                        amount = numberSequence.toString(),
                        date = selectedDate.toString(),
                        categoryId = categoryData!!.idCategory,
                        type = categoryData!!.source,
                        image = listImage.toString(),
                        categoryName = categoryData!!.categoryName,
                        iconResource = categoryData!!.iconResource,
                        accountId = accountId
                    )
                    incomeExpenseListModel.insert(incomeExpenseList)
                        .observe(viewLifecycleOwner) { isSuccess ->
                            if (isSuccess) {
                                val intent = Intent(requireContext(), MainActivity::class.java)
                                startActivity(intent)
                                dismiss()
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Thêm không thành công!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            } else {
                if (numberSequence2.isEmpty()) {
                    Toast.makeText(requireContext(), "Vui lòng nhập số tiền!", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    val incomeExpenseList = IncomeExpenseList(
                        note = nameCategory,
                        amount = numberSequence2.toString(),
                        date = selectedDate.toString(),
                        categoryId = categoryData!!.idCategory,
                        type = categoryData!!.source,
                        image = listImage.toString(),
                        categoryName = categoryData!!.categoryName,
                        iconResource = categoryData!!.iconResource,
                        accountId = accountId
                    )
                    incomeExpenseListModel.insert(incomeExpenseList)
                        .observe(viewLifecycleOwner) { isSuccess ->
                            if (isSuccess) {
                                val intent = Intent(requireContext(), MainActivity::class.java)
                                startActivity(intent)
                                dismiss()
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Thêm không thành công!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            }
        } else {
            if (type == 1) {
                if (numberSequence.isEmpty()) {
                    Toast.makeText(requireContext(), "Vui lòng nhập số tiền!", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    val incomeExpenseList = IncomeExpenseList(
                        id = itemEdit!!.id,
                        note = nameCategory,
                        amount = numberSequence.toString(),
                        date = selectedDate.toString(),
                        categoryId = categoryData!!.idCategory,
                        type = categoryData!!.source,
                        image = listImage.toString(),
                        categoryName = categoryData!!.categoryName,
                        iconResource = categoryData!!.iconResource,
                        accountId = itemEdit!!.accountId
                    )
                    incomeExpenseListModel.updateIncomeExpenseListModel(incomeExpenseList)
                        .observe(viewLifecycleOwner) { isSuccess ->
                            if (isSuccess) {
                                val intent = Intent(requireContext(), MainActivity::class.java)
                                startActivity(intent)
                                dismiss()
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Chỉnh sửa không thành công!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            } else {
                if (numberSequence2.isEmpty()) {
                    Toast.makeText(requireContext(), "Vui lòng nhập số tiền!", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    val incomeExpenseList = IncomeExpenseList(
                        id = itemEdit!!.id,
                        note = nameCategory,
                        amount = numberSequence2.toString(),
                        date = selectedDate.toString(),
                        categoryId = categoryData!!.idCategory,
                        type = categoryData!!.source,
                        image = listImage.toString(),
                        categoryName = categoryData!!.categoryName,
                        iconResource = categoryData!!.iconResource,
                        accountId = itemEdit!!.accountId
                    )
                    incomeExpenseListModel.insert(incomeExpenseList)
                        .observe(viewLifecycleOwner) { isSuccess ->
                            if (isSuccess) {
                                val intent = Intent(requireContext(), MainActivity::class.java)
                                startActivity(intent)
                                dismiss()
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Chỉnh sửa không thành công!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            }
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

    fun categoryData(
        category: CombinedCategoryIcon,
        dataEdit: IncomeExpenseListData? = null,
        dateSelected: String? = null
    ) {
        if (dataEdit != null) {
            itemEdit = dataEdit
            checkEdit = true
        }
        if (dateSelected != null) {
            selectedDate = LocalDate.parse(dateSelected)
        }

        categoryData = category
    }

    @SuppressLint("SetTextI18n")
    fun onReceiveDate(year: String, month: String, dayOfMonth: String) {
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
        selectedDate = LocalDate.of(year.toInt(), month.toInt(), dayOfMonth.toInt())
    }

    private fun showCustomDialogAddCategory() {
        val calendarDialogFragment = CalendarDialogFragment()
        val bundle = Bundle()
        bundle.putString("selectedDate", selectedDate.toString())
        bundle.putString("type", "bottom")
        calendarDialogFragment.arguments = bundle
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
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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
