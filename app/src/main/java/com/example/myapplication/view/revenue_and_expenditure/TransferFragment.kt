package com.example.myapplication.view.revenue_and_expenditure

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.data.AccountIconFormat
import com.example.myapplication.data.HistoryAccountWithAccount
import com.example.myapplication.data.IncomeExpenseListData
import com.example.myapplication.databinding.FragmentTransferBinding
import com.example.myapplication.entity.Account
import com.example.myapplication.entity.HistoryAccount
import com.example.myapplication.view.account.BottomSelectAccountTransferFragment
import com.example.myapplication.view.calendar.CalendarDialogFragment
import com.example.myapplication.view.component.BottomSheetSelectedImageFragment
import com.example.myapplication.view.component.CustomListPhotoFragment
import com.example.myapplication.viewModel.AccountViewModel
import com.example.myapplication.viewModel.AccountViewModelFactory
import com.example.myapplication.viewModel.HistoryAccountViewModel
import com.example.myapplication.viewModel.HistoryAccountViewModelFactory
import com.example.myapplication.viewModel.ImageViewModel
import com.example.myapplication.viewModel.SharedAccountSelectedModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

class TransferFragment : Fragment() {

    private lateinit var binding: FragmentTransferBinding

    private var account1: AccountIconFormat? = null
    private var isAccount1Selected: Boolean = false
    private var account2: AccountIconFormat? = null

    private val numberSequence = StringBuilder()
    private var numberSequence2 = StringBuilder()
    private var total: BigDecimal = BigDecimal.ZERO
    private var checkEdit: Boolean = false
    
    private var selectedDate = LocalDate.now()

    private var note: String = ""

    private var listImage: MutableList<Uri> = mutableListOf()

    private val imageViewModel: ImageViewModel by activityViewModels()

    private var selectedAccount: AccountIconFormat? = null

    private val sharedViewModel: SharedAccountSelectedModel by activityViewModels()

    private var itemAccount: HistoryAccountWithAccount? = null

    private var dateSelected: String? = null

    private val historyAccountViewModel: HistoryAccountViewModel by viewModels {
        HistoryAccountViewModelFactory(requireActivity().application)
    }

    private val accountTypeViewModel: AccountViewModel by viewModels {
        AccountViewModelFactory(requireActivity().application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val json = it.getString("itemEdit")
            itemAccount = json?.let { jsonStr ->
                Gson().fromJson(jsonStr, HistoryAccountWithAccount::class.java)
            }
            dateSelected = it.getString("dateSelected")
        }
    }

    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTransferBinding.inflate(inflater, container, false)

        setupNightMode()

        setupBackground()

        setupKeyBoard()

        onReceiveDate(
            selectedDate.year.toString(),
            selectedDate.monthValue.toString(),
            selectedDate.dayOfMonth.toString()
        )

        getDataEdit()

        return binding.root
    }

    
    private fun setupDataEdit() {
        checkEdit = true
        val amountFormatter = DecimalFormat("#,###.##")

        val data1 = account1?.amountAccount?.replace(",", ".")?.toDoubleOrNull()
        if (data1 != null) {
            val amountData1 = amountFormatter.format(data1)
            binding.nameAccount1TV.text = account1?.nameAccount
            binding.amountAccount1TV.visibility = View.VISIBLE
            binding.amountAccount1TV.text = amountData1
        } else {
            Log.e("Hieu127", "account1 amountAccount is not a valid number")
        }

        val data2 = account2?.amountAccount?.replace(",", ".")?.toDoubleOrNull()
        if (data2 != null) {
            val amountData2 = amountFormatter.format(data2)
            binding.nameAccount2TV.text = account2?.nameAccount
            binding.amountAccount2TV.visibility = View.VISIBLE
            binding.amountAccount2TV.text = amountData2
        } else {
            Log.e("Hieu127", "account2 amountAccount is not a valid number")
        }
        val amountFormat = itemAccount?.historyAccount?.transferAmount?.replace(",", ".")?.toDoubleOrNull()

        val dataImage = itemAccount?.historyAccount?.image?.replace("[", "")?.replace("]", "")?.split(", ")
        dataImage?.distinct()?.forEach { imageString ->
            if (imageString.isNotEmpty()) {
                val uri = Uri.parse(imageString)
                if (!listImage.contains(uri)) {
                    listImage.add(uri)
                    imageViewModel.addImageUri(uri)
                }
            }
        }

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dateString = itemAccount!!.historyAccount.date
        selectedDate = LocalDate.parse(dateString, formatter)
        onReceiveDate(
            selectedDate.year.toString(),
            selectedDate.monthValue.toString(),
            selectedDate.dayOfMonth.toString()
        )
        numberSequence.append(amountFormatter.format(amountFormat))
        note = itemAccount?.historyAccount?.note.toString()
        binding.nameCategoryEt.setText(itemAccount?.historyAccount?.note)
        binding.textViewNumberDisplay.text = amountFormatter.format(amountFormat)
        binding.amountReceiveTv.text = amountFormatter.format(amountFormat)
        binding.amountSendTv.text = amountFormatter.format(amountFormat)
        binding.amountLayout1.visibility = View.VISIBLE
        binding.amountLayout2.visibility = View.VISIBLE
        showKeyboardLayout()
        setupBackground()
    }


    
    private fun getDataEdit() {
        if (itemAccount != null) {
            val accountTransferLiveData = accountTypeViewModel.getAccountsByAccountId(itemAccount!!.historyAccount.idAccountTransfer)
            val accountReceiveLiveData = accountTypeViewModel.getAccountsByAccountId(itemAccount!!.historyAccount.idAccountReceive)

            val mediatorLiveData = MediatorLiveData<Pair<AccountIconFormat?, AccountIconFormat?>>()

            mediatorLiveData.addSource(accountTransferLiveData) { account ->
                account1 = AccountIconFormat(
                    id = account.account.id,
                    nameAccount = account.account.nameAccount,
                    typeAccount = account.account.typeAccount,
                    amountAccount = account.account.amountAccount,
                    icon = account.account.icon,
                    note = account.account.note,
                    iconResource = account.icon.iconResource,
                    typeIcon = account.icon.type
                )
                mediatorLiveData.value = Pair(account1, account2)
            }

            mediatorLiveData.addSource(accountReceiveLiveData) { account ->
                account2 = AccountIconFormat(
                    id = account.account.id,
                    nameAccount = account.account.nameAccount,
                    typeAccount = account.account.typeAccount,
                    amountAccount = account.account.amountAccount,
                    icon = account.account.icon,
                    note = account.account.note,
                    iconResource = account.icon.iconResource,
                    typeIcon = account.icon.type
                )
                mediatorLiveData.value = Pair(account1, account2)
            }

            mediatorLiveData.observe(viewLifecycleOwner) { pair ->
                if (pair.first != null && pair.second != null) {
                    setupDataEdit()
                }
            }
        }
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

    
    private fun setupBackground() {

        binding.nameCategoryEt.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showDefaultKeyboard()
            }
        }
        binding.nameCategoryEt.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                hideCustomKeyboard()
            }
        }

        binding.nameCategoryEt.setOnClickListener {
            hideCustomKeyboard()
        }

        binding.nameCategoryEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                note = s.toString()
            }

        })


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

    @SuppressLint("SetTextI18n")
    
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
            binding.amountReceiveTv.text = displayText
            binding.amountSendTv.text = displayText
            binding.amountLayout1.visibility = View.VISIBLE
            binding.amountLayout2.visibility = View.VISIBLE
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
                binding.amountLayout1.visibility = View.GONE
                binding.amountLayout2.visibility = View.GONE
            } else {
                displayText = formatNumberWithDots(numberSequence.toString())

                if (numberSequence2.isNotEmpty()) {
                    displayText =
                        formatNumberWithDots(numberSequence2.toString()) + calculationMark + displayText
                }

                binding.textViewNumberDisplay.text = displayText
                if (calculationMark === "") {
                    binding.amountReceiveTv.text = displayText
                    binding.amountSendTv.text = displayText
                    binding.amountLayout1.visibility = View.VISIBLE
                    binding.amountLayout2.visibility = View.VISIBLE
                } else {
                    if (numberSequence.isNotEmpty()) {
                        val number1 =
                            numberSequence2.toString().replace(',', '.').toBigDecimalOrNull()
                        val number2 =
                            numberSequence.toString().replace(',', '.').toBigDecimalOrNull()
                        var dataTotal: BigDecimal = BigDecimal.ZERO
                        if (calculationMark == " + ") {
                            if (number1 != null && number2 != null) {
                                dataTotal = number1 + number2
                            }

                        } else {
                            if (number1 != null && number2 != null) {
                                dataTotal = number1 - number2
                            }
                        }
                        val dataString = dataTotal.toString().replace('.', ',')
                        binding.amountReceiveTv.text = formatNumberWithDots(dataString)
                        binding.amountSendTv.text = formatNumberWithDots(dataString)
                        binding.amountLayout1.visibility = View.VISIBLE
                        binding.amountLayout2.visibility = View.VISIBLE
                    } else {
                        binding.amountReceiveTv.text =
                            formatNumberWithDots(numberSequence2.toString())
                        binding.amountSendTv.text = formatNumberWithDots(numberSequence2.toString())
                        binding.amountLayout1.visibility = View.VISIBLE
                        binding.amountLayout2.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    
    private fun saveDataToServer(type: Int) {
        if (checkEdit) {
            if (type == 1) {
                if (numberSequence.isEmpty()) {
                    Toast.makeText(requireContext(), "Vui lòng nhập số tiền!", Toast.LENGTH_SHORT).show()
                } else {
                    val historyAccount = HistoryAccount(
                        id = itemAccount!!.historyAccount.id,
                        idAccountTransfer = account1!!.id,
                        nameAccountTransfer = account1!!.nameAccount,
                        idAccountReceive = account2!!.id,
                        nameAccountReceive = account2!!.nameAccount,
                        transferAmount = numberSequence.toString(),
                        date = selectedDate.toString(),
                        image = listImage.toString(),
                        note = note,
                        icon = itemAccount!!.historyAccount.icon,
                        type = itemAccount!!.historyAccount.note
                    )
                    historyAccountViewModel.updateHistoryAccount(historyAccount)
                        .observe(viewLifecycleOwner) { success ->
                            if (success) {
                                val listAccount = setupListAccount(numberSequence)
                                accountTypeViewModel.updateListAccounts(listAccount)
                                    .observe(viewLifecycleOwner) { success ->
                                        if (success) {
                                            Toast.makeText(
                                                requireContext(),
                                                "Sửa chuyển khoản thành công!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            val intent = Intent(requireContext(), MainActivity::class.java)
                                            startActivity(intent)
                                        } else {
                                            Toast.makeText(
                                                requireContext(),
                                                "Sửa chuyển khoản không thành công!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Sửa chuyển khoản không thành công!",
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
                    val historyAccount = HistoryAccount(
                        id = itemAccount!!.historyAccount.id,
                        idAccountTransfer = account1!!.id,
                        nameAccountTransfer = account1!!.nameAccount,
                        idAccountReceive = account2!!.id,
                        nameAccountReceive = account2!!.nameAccount,
                        transferAmount = numberSequence2.toString(),
                        date = selectedDate.toString(),
                        image = listImage.toString(),
                        note = note,
                        icon = itemAccount!!.historyAccount.icon,
                        type = itemAccount!!.historyAccount.note
                    )
                    historyAccountViewModel.updateHistoryAccount(historyAccount)
                        .observe(viewLifecycleOwner) { success ->
                            if (success) {
                                val listAccount = setupListAccount(numberSequence2)
                                accountTypeViewModel.updateListAccounts(listAccount)
                                    .observe(viewLifecycleOwner) { success ->
                                        if (success) {
                                            Toast.makeText(
                                                requireContext(),
                                                "Sửa chuyển khoản thành công!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            val intent =
                                                Intent(requireContext(), MainActivity::class.java)
                                            startActivity(intent)
                                        } else {
                                            Toast.makeText(
                                                requireContext(),
                                                "Sửa chuyển khoản không thành công!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Sửa chuyển khoản không thành công!",
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
                    val historyAccount = HistoryAccount(
                        idAccountTransfer = account1!!.id,
                        nameAccountTransfer = account1!!.nameAccount,
                        idAccountReceive = account2!!.id,
                        nameAccountReceive = account2!!.nameAccount,
                        transferAmount = numberSequence.toString(),
                        date = selectedDate.toString(),
                        image = listImage.toString(),
                        note = note,
                        icon = R.drawable.ic_data_transfer_24,
                        type = "historyAccount"
                    )
                    historyAccountViewModel.insert(historyAccount)
                        .observe(viewLifecycleOwner) { success ->
                            if (success) {
                                val listAccount = setupListAccount(numberSequence)
                                accountTypeViewModel.updateListAccounts(listAccount)
                                    .observe(viewLifecycleOwner) { success ->
                                        if (success) {
                                            Toast.makeText(
                                                requireContext(),
                                                "Chuyển khoản thành công!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            val intent =
                                                Intent(requireContext(), MainActivity::class.java)
                                            startActivity(intent)
                                        } else {
                                            Toast.makeText(
                                                requireContext(),
                                                "Thêm không thành công!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
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
                }
                else {
                    val historyAccount = HistoryAccount(
                        idAccountTransfer = account1!!.id,
                        nameAccountTransfer = account1!!.nameAccount,
                        idAccountReceive = account2!!.id,
                        nameAccountReceive = account2!!.nameAccount,
                        transferAmount = numberSequence2.toString(),
                        date = selectedDate.toString(),
                        image = listImage.toString(),
                        note = note,
                        icon = R.drawable.ic_data_transfer_24,
                        type = "historyAccount"
                    )
                    historyAccountViewModel.insert(historyAccount)
                        .observe(viewLifecycleOwner) { success ->
                            if (success) {
                                val listAccount = setupListAccount(numberSequence2)
                                accountTypeViewModel.updateListAccounts(listAccount)
                                    .observe(viewLifecycleOwner) { success ->
                                        if (success) {
                                            Toast.makeText(
                                                requireContext(),
                                                "Chuyển khoản thành công!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            val intent =
                                                Intent(requireContext(), MainActivity::class.java)
                                            startActivity(intent)
                                        } else {
                                            Toast.makeText(
                                                requireContext(),
                                                "Thêm không thành công!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
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
        }
    }

    private fun setupListAccount(number: StringBuilder): List<Account> {
        val amountAccount1 = account1?.amountAccount?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
        val amountAccount2 = account2?.amountAccount?.replace(",", ".")?.toDoubleOrNull() ?: 0.0

        val numberSequenceValue = number.toString().replace(",", ".").toDoubleOrNull() ?: 0.0

        val newAmountAccount1 = amountAccount1 - numberSequenceValue
        val newAmountAccount2 = amountAccount2 + numberSequenceValue

        val listAccount = listOf(
            Account(
                id = account1!!.id,
                nameAccount = account1!!.nameAccount,
                typeAccount = account1!!.typeAccount,
                amountAccount = newAmountAccount1.toString(),
                icon = account1!!.icon,
                note = account1!!.note

            ),
            Account(
                id = account2!!.id,
                nameAccount = account2!!.nameAccount,
                typeAccount = account2!!.typeAccount,
                amountAccount = newAmountAccount2.toString(),
                icon = account2!!.icon,
                note = account2!!.note
            )
        )
        return listAccount
    }

    
    private fun showCustomDialogAddCategory() {
        val calendarDialogFragment = CalendarDialogFragment()
        val bundle = Bundle()
        bundle.putString("selectedDate", selectedDate.toString())
        bundle.putString("type", "transfer")
        calendarDialogFragment.arguments = bundle
        calendarDialogFragment.setTargetFragment(this, 0)
        calendarDialogFragment.show(parentFragmentManager, "CalendarDialogFragment")
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

    private fun showKeyboardLayout() {
        binding.keyboardLayout.visibility = View.VISIBLE
        val animate = TranslateAnimation(
            0f,
            0f,
            binding.keyboardLayout.height.toFloat(),
            0f
        )
        animate.duration = 300
        animate.fillAfter = true
        binding.keyboardLayout.startAnimation(animate)
    }

    private fun hideKeyboardLayout() {
        val animate = TranslateAnimation(
            0f,
            0f,
            0f,
            binding.keyboardLayout.height.toFloat()
        )
        animate.duration = 300
        animate.fillAfter = true
        animate.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                binding.keyboardLayout.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        binding.keyboardLayout.startAnimation(animate)
    }

    private fun setupKeyBoard() {
        childFragmentManager.setFragmentResultListener("requestKey", this) { _, bundle ->
            val selectedAccount = bundle.getParcelable<AccountIconFormat>("selected_account")
            selectedAccount?.let { account ->
                if (isAccount1Selected) {
                    if (account == account2) {
                        account2 = null
                        binding.nameAccount2TV.text = ""
                        binding.amountAccount2TV.visibility = View.GONE
                    }
                    val data = account.amountAccount.replace(",", ".").toDouble()
                    val amountFormatter = DecimalFormat("#,###.##")
                    val amountData = amountFormatter.format(data)
                    account1 = account
                    binding.nameAccount1TV.text = account.nameAccount
                    binding.amountAccount1TV.visibility = View.VISIBLE
                    binding.amountAccount1TV.text = amountData
                } else {
                    if (account == account1) {
                        account1 = null
                        binding.nameAccount1TV.text = ""
                        binding.amountAccount1TV.visibility = View.GONE
                    }
                    val data = account.amountAccount.replace(",", ".").toDouble()
                    val amountFormatter = DecimalFormat("#,###.##")
                    val amountData = amountFormatter.format(data)
                    account2 = account
                    binding.nameAccount2TV.text = account.nameAccount
                    binding.amountAccount2TV.visibility = View.VISIBLE
                    binding.amountAccount2TV.text = amountData
                }
            }

            if (account1 != null && account2 != null) {
                showKeyboardLayout()
            } else {
                hideKeyboardLayout()
            }
        }

        binding.layoutAccount1.setOnClickListener {
            isAccount1Selected = true
            val bottomSelectAccountTransferFragment =
                BottomSelectAccountTransferFragment.newInstance(account1, account2, true)
            bottomSelectAccountTransferFragment.show(
                childFragmentManager,
                "bottomSelectAccountTransferFragment"
            )
        }

        binding.layoutAccount2.setOnClickListener {
            isAccount1Selected = false
            val bottomSelectAccountTransferFragment =
                BottomSelectAccountTransferFragment.newInstance(account1, account2, false)
            bottomSelectAccountTransferFragment.show(
                childFragmentManager,
                "bottomSelectAccountTransferFragment"
            )
        }
    }

    override fun onResume() {
        super.onResume()
        binding.nameCategoryEt.setOnEditorActionListener { _, _, _ ->
            hideKeyboard()
            showCustomKeyboard()
            true
        }

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

    private fun showDefaultKeyboard() {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.nameCategoryEt, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.nameCategoryEt.windowToken, 0)
    }

}