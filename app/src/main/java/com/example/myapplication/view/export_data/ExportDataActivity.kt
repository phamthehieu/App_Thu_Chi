package com.example.myapplication.view.export_data

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MediatorLiveData
import com.example.myapplication.data.CategoryWithIncomeExpenseList
import com.example.myapplication.data.HistoryAccountWithAccount
import com.example.myapplication.databinding.ActivityExportDataBinding
import com.example.myapplication.view.calendar.CalendarDialogFragment
import com.example.myapplication.viewModel.HistoryAccountViewModel
import com.example.myapplication.viewModel.HistoryAccountViewModelFactory
import com.example.myapplication.viewModel.IncomeExpenseListFactory
import com.example.myapplication.viewModel.IncomeExpenseListModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDate

class ExportDataActivity : AppCompatActivity(), CalendarDialogFragment.OnDateSelectedListener {

    private lateinit var binding: ActivityExportDataBinding

    
    private var selectedDateStart = LocalDate.now()

    
    private var selectedDateEnd = LocalDate.now()

    private var checkDate = false

    private val incomeExpenseListModel: IncomeExpenseListModel by viewModels {
        IncomeExpenseListFactory(this.application)
    }

    private val historyAccountViewModel: HistoryAccountViewModel by viewModels {
        HistoryAccountViewModelFactory(this.application)
    }

    companion object {
        private const val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1001
    }


    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExportDataBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkAndRequestPermissions()
        setupUI()
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {

            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_CODE_WRITE_EXTERNAL_STORAGE
                )
            }
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Đã cấp quyền truy cập bộ nhớ", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Không thể xuất dữ liệu nếu không có quyền truy cập bộ nhớ", Toast.LENGTH_SHORT).show()
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun setupUI() {
        binding.timeStart.text = "${selectedDateStart.dayOfMonth} thg ${selectedDateStart.monthValue}, ${selectedDateStart.year}"
        binding.timeEnd.text = "${selectedDateEnd.dayOfMonth} thg ${selectedDateEnd.monthValue}, ${selectedDateEnd.year}"

        binding.timeStartLayout.setOnClickListener {
            checkDate = true
            showCustomDialogAddDateStart()
        }

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.timeEndLayout.setOnClickListener {
            checkDate = false
            showCustomDialogAddDateEnd()
        }

        binding.exportDataBtn.setOnClickListener {
            binding.spinKit.visibility = View.VISIBLE
            if (selectedDateEnd.isBefore(selectedDateStart)) {
                Toast.makeText(this, "Ngày kết thúc không thể nhỏ hơn ngày bắt đầu", Toast.LENGTH_SHORT).show()
                binding.spinKit.visibility = View.GONE
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    getDataToExport()
                }
            }
        }

    }

    private fun getDataToExport() {

        val combinedData = MediatorLiveData<Pair<List<CategoryWithIncomeExpenseList>, List<HistoryAccountWithAccount>>>()

        val incomeExpenseData = incomeExpenseListModel.getIncomeExpenseListByDateRange(
            selectedDateStart.toString(),
            selectedDateEnd.toString()
        )

        val historyAccountData = historyAccountViewModel.getHistoryAccountListByDateRange(
            selectedDateStart.toString(),
            selectedDateEnd.toString()
        )

        combinedData.addSource(incomeExpenseData) { incomeData ->
            val currentHistoryData = combinedData.value?.second ?: emptyList()
            combinedData.value = Pair(incomeData, currentHistoryData)
        }

        combinedData.addSource(historyAccountData) { historyData ->
            val currentIncomeData = combinedData.value?.first ?: emptyList()
            combinedData.value = Pair(currentIncomeData, historyData)
        }

        combinedData.observe(this) { (incomeData, historyData) ->
            CoroutineScope(Dispatchers.IO).launch {
                exportDataToExcel(incomeData, historyData)
            }
        }
    }

    private fun showCustomDialogAddDateEnd() {
        val calendarDialogFragment = CalendarDialogFragment()
        val bundle = Bundle()
        bundle.putString("selectedDate", selectedDateEnd.toString())
        bundle.putString("type", "reminder")
        calendarDialogFragment.arguments = bundle
        calendarDialogFragment.setOnDateSelectedListener(this)
        calendarDialogFragment.show(supportFragmentManager, "CalendarDialogFragment")
    }

    private fun showCustomDialogAddDateStart() {
        val calendarDialogFragment = CalendarDialogFragment()
        val bundle = Bundle()
        bundle.putString("selectedDate", selectedDateStart.toString())
        bundle.putString("type", "reminder")
        calendarDialogFragment.arguments = bundle
        calendarDialogFragment.setOnDateSelectedListener(this)
        calendarDialogFragment.show(supportFragmentManager, "CalendarDialogFragment")
    }

    @SuppressLint("SetTextI18n")
    override fun onReceiveDate(year: String, month: String, day: String) {
        if (checkDate) {
            selectedDateStart = LocalDate.of(year.toInt(), month.toInt(), day.toInt())
            binding.timeStart.text = "${selectedDateStart.dayOfMonth} thg ${selectedDateStart.monthValue}, ${selectedDateStart.year}"
        } else {
            selectedDateEnd = LocalDate.of(year.toInt(), month.toInt(), day.toInt())
            binding.timeEnd.text = "${selectedDateEnd.dayOfMonth} thg ${selectedDateEnd.monthValue}, ${selectedDateEnd.year}"
        }
    }

    private suspend fun exportDataToExcel(
        incomeExpenseData: List<CategoryWithIncomeExpenseList>,
        historyAccountData: List<HistoryAccountWithAccount>
    ) {
        val workbook = XSSFWorkbook()
        val incomeExpenseSheet = workbook.createSheet("Thu_Chi_Danh_Sach")
        var rowNum = 0
        var row: Row = incomeExpenseSheet.createRow(rowNum++)
        var cell: Cell

        val headers = listOf("Tên danh mục", "Số Tiền", "Kiểu", "Ngày giao dịch", "Ghi chú")
        for (i in headers.indices) {
            cell = row.createCell(i)
            cell.setCellValue(headers[i])
        }

        for (item in incomeExpenseData) {
            row = incomeExpenseSheet.createRow(rowNum++)
            row.createCell(0).setCellValue(item.category.name)
            row.createCell(1).setCellValue(item.incomeExpense.amount.toString())
            row.createCell(2).setCellValue(if (item.incomeExpense.type == "Expense") "Chi phí" else "Thu nhập")
            row.createCell(3).setCellValue(item.incomeExpense.date)
            row.createCell(4).setCellValue(item.incomeExpense.note)
        }

        val historyAccountSheet = workbook.createSheet("Dao_Dich_Tai_Khoan")
        rowNum = 0
        row = historyAccountSheet.createRow(rowNum++)

        val historyHeaders = listOf("Tài khoản chuyển", "Tài khoản nhận", "Số tiền giao dịch", "Ngày giao dịch")
        for (i in historyHeaders.indices) {
            cell = row.createCell(i)
            cell.setCellValue(historyHeaders[i])
        }

        for (item in historyAccountData) {
            row = historyAccountSheet.createRow(rowNum++)
            row.createCell(0).setCellValue(item.accountTransfer.nameAccount)
            row.createCell(1).setCellValue(item.accountReceive.nameAccount)
            row.createCell(2).setCellValue(item.historyAccount.transferAmount.toString())
            row.createCell(3).setCellValue(item.historyAccount.date)
        }

        val fileName = "Thu_Chi_App_Thong_Ke_Tu_${selectedDateStart}_Den_${selectedDateEnd}.xlsx"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/MyAppExports")
            }

            val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
            if (uri != null) {
                try {
                    resolver.openOutputStream(uri).use { outputStream ->
                        workbook.write(outputStream)
                        withContext(Dispatchers.Main) {
                            binding.spinKit.visibility = View.GONE
                            Toast.makeText(
                                this@ExportDataActivity,
                                "Xuất file thành công: $fileName lưu trong thư mục Documents/MyAppExports",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    withContext(Dispatchers.Main) {
                        binding.spinKit.visibility = View.GONE
                    }
                    workbook.close()
                }
            }
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            try {
                withContext(Dispatchers.IO) {
                    FileOutputStream(file).use { fileOutputStream ->
                        workbook.write(fileOutputStream)
                        withContext(Dispatchers.Main) {
                            binding.spinKit.visibility = View.GONE
                            Toast.makeText(
                                this@ExportDataActivity,
                                "Xuất file thành công: $fileName lưu trong thư mục Tải Về (Downloads)",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()

            } finally {
                workbook.close()
                withContext(Dispatchers.Main) {
                    binding.spinKit.visibility = View.GONE
                }
            }
        }
    }

}