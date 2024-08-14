package com.example.myapplication.adapter

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.Log
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.HistoryAccountWithAccount
import com.example.myapplication.data.IncomeExpenseListData
import com.example.myapplication.entity.HistoryAccount
import java.text.DecimalFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class IncomeExpenseListAdapter(
    private val groupedIconMap: Map<String, List<Any>>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(incomeExpense: Any)
    }

    companion object {
        const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    private val dataList: MutableList<Any> = mutableListOf<Any>().apply {
        groupedIconMap.forEach { (type, incomeExpenseList) ->
            add(type)
            addAll(incomeExpenseList)
        }
    }

    private var selectedItemPosition: Int = RecyclerView.NO_POSITION

    fun getSelectedItem(): Any? {
        return if (selectedItemPosition != RecyclerView.NO_POSITION) {
            dataList[selectedItemPosition]
        } else {
            null
        }
    }

    @SuppressLint("SuspiciousIndentation")
    fun removeItem(position: Int) {
        if (position != RecyclerView.NO_POSITION) {
          selectedItemPosition = position
            notifyItemRemoved(position)
        }
    }

    data class Totals(var totalIncome: Double = 0.0, var totalExpense: Double = 0.0)

    private val totalsMap = mutableMapOf<String, Totals>()

    init {
        calculateTotals()
    }

    private fun calculateTotals() {
        for (item in dataList) {
            if (item is IncomeExpenseListData) {
                val date = item.date
                val amount = parseAmount(item.amount)
                val totals = totalsMap.getOrPut(date) { Totals() }

                if (item.type == "Income") {
                    totals.totalIncome += amount
                } else if (item.type == "Expense") {
                    totals.totalExpense += amount
                }
            }
        }
    }

    private fun parseAmount(amountStr: String): Double {
        val cleanedAmountStr = amountStr.replace(".", "").replace(",", ".")
        return try {
            cleanedAmountStr.toDouble()
        } catch (e: NumberFormatException) {
            0.0
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_row_item_header_category_home, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_row_item_category_home, parent, false)
            IncomeExpenseViewHolder(view)
        }
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateTV: TextView = itemView.findViewById(R.id.dateTV)
        private val weekdaysTV: TextView = itemView.findViewById(R.id.weekdaysTV)
        private val expenseTV: TextView = itemView.findViewById(R.id.expenseTV)
        private val incomeTV: TextView = itemView.findViewById(R.id.incomeTV)

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(title: String, totals: Totals) {
            try {
                val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val date = LocalDate.parse(title, dateFormatter)

                val dayOfWeek = date.dayOfWeek

                val dayOfWeekString = when (dayOfWeek) {
                    DayOfWeek.MONDAY -> "Thứ Hai"
                    DayOfWeek.TUESDAY -> "Thứ Ba"
                    DayOfWeek.WEDNESDAY -> "Thứ Tư"
                    DayOfWeek.THURSDAY -> "Thứ Năm"
                    DayOfWeek.FRIDAY -> "Thứ Sáu"
                    DayOfWeek.SATURDAY -> "Thứ Bảy"
                    DayOfWeek.SUNDAY -> "Chủ Nhật"
                    else -> "Không xác định"
                }

                val newDateFormatter = DateTimeFormatter.ofPattern("dd 'thg' MM")
                val formattedDate = date.format(newDateFormatter)

                val expenseFormatter = DecimalFormat("#,###.##")
                val formattedExpense = expenseFormatter.format(totals.totalExpense)
                val formattedIncome = expenseFormatter.format(totals.totalIncome)

                dateTV.text = formattedDate
                weekdaysTV.text = dayOfWeekString
                expenseTV.text = formattedExpense
                incomeTV.text = formattedIncome

            } catch (e: DateTimeParseException) {
                Log.e("Hieu62", "Invalid date format: $title", e)
            }
        }
    }

    inner class IncomeExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnCreateContextMenuListener {
        private val iconCategoryIV: ImageView = itemView.findViewById(R.id.iconCategoryIV)
        private val nameCategoryTV: TextView = itemView.findViewById(R.id.nameCategoryTV)
        private val amountCategoryTV: TextView = itemView.findViewById(R.id.amountCategoryTV)
        private val checkImage: RelativeLayout = itemView.findViewById(R.id.checkImage)
        private val iconArrow: ImageView = itemView.findViewById(R.id.iconArrow)
        private val amountAccount2TV: TextView = itemView.findViewById(R.id.nameAccount2)
        private val iconArrowMount: ImageView = itemView.findViewById(R.id.iconArrowMount)
        private val backgroundIcon: FrameLayout = itemView.findViewById(R.id.backgroundIcon)

        init {
            itemView.setOnCreateContextMenuListener(this)
            itemView.setOnLongClickListener {
                selectedItemPosition = adapterPosition
                false
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(item: Any) {
            when (item) {
                is IncomeExpenseListData -> {
                    iconArrowMount.visibility = View.GONE
                    iconArrow.visibility = View.GONE
                    amountAccount2TV.visibility = View.GONE
                    if (item.image.isNotEmpty() && item.image.trim().isNotEmpty() && item.image.trim() != "[]") {
                        checkImage.visibility = View.VISIBLE
                    } else {
                        checkImage.visibility = View.GONE
                    }
                    iconCategoryIV.setImageResource(item.iconResource)
                    iconCategoryIV.setColorFilter(Color.BLACK)
                    if (item.note.isEmpty()) {
                        nameCategoryTV.text = item.categoryName
                    } else {
                        nameCategoryTV.text = item.note
                    }

                    val amount = item.amount.replace(",", ".").toDouble()
                    val expenseFormatter = DecimalFormat("#,###.##")
                    val formattedAmount = expenseFormatter.format(amount)

                    if (item.type == "Expense") {
                        amountCategoryTV.text = "-$formattedAmount"
                    } else {
                        amountCategoryTV.text = formattedAmount
                    }

                    val drawable = ContextCompat.getDrawable(
                        itemView.context,
                        R.drawable.setting_background_item
                    ) as GradientDrawable
                    drawable.setColor(generateRandomLightColor())
                    backgroundIcon.background = drawable
                }

                is HistoryAccountWithAccount -> {
                    iconArrowMount.visibility = View.VISIBLE
                    iconArrow.visibility = View.VISIBLE
                    amountAccount2TV.visibility = View.VISIBLE
                    val currentNightMode = itemView.context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

                    iconCategoryIV.setImageResource(item.historyAccount.icon)
                    iconCategoryIV.setColorFilter(Color.BLACK)
                    nameCategoryTV.text = item.historyAccount.nameAccountTransfer
                    amountAccount2TV.text = item.historyAccount.nameAccountReceive

                    val amount = item.historyAccount.transferAmount.replace(",", ".").toDouble()
                    val expenseFormatter = DecimalFormat("#,###.##")
                    val formattedAmount = expenseFormatter.format(amount)
                    amountCategoryTV.text = formattedAmount

                    if (item.historyAccount.image.isNotEmpty() && item.historyAccount.image.trim().isNotEmpty() && item.historyAccount.image.trim() != "[]") {
                        checkImage.visibility = View.VISIBLE
                    } else {
                        checkImage.visibility = View.GONE
                    }

                    if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
                        iconArrowMount.setColorFilter(ContextCompat.getColor(itemView.context, R.color.white))
                        iconArrow.setColorFilter(ContextCompat.getColor(itemView.context, R.color.white))
                    } else {
                        iconArrowMount.setColorFilter(ContextCompat.getColor(itemView.context, R.color.black))
                        iconArrow.setColorFilter(ContextCompat.getColor(itemView.context, R.color.black))
                    }

                    val drawable = ContextCompat.getDrawable(
                        itemView.context,
                        R.drawable.setting_background_item
                    ) as GradientDrawable
                    drawable.setColor(ContextCompat.getColor(itemView.context, R.color.yellow))
                    backgroundIcon.background = drawable
                }

                else -> {
                    Log.e("IncomeExpenseViewHolder", "Unexpected item type: ${item::class.java}")
                }
            }
        }


        override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
            val inflater = MenuInflater(v.context)
            inflater.inflate(R.menu.context_menu, menu)
        }

        private fun generateRandomLightColor(): Int {
            val random = java.util.Random()
            val baseColor =
                Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))
            val lightColor = blendWithWhite(baseColor, 0.5f)
            return if (lightColor == Color.WHITE || lightColor == Color.BLACK) generateRandomLightColor() else lightColor
        }

        private fun blendWithWhite(color: Int, ratio: Float): Int {
            val red = Color.red(color)
            val green = Color.green(color)
            val blue = Color.blue(color)

            val blendedRed = (red + (255 - red) * ratio).toInt()
            val blendedGreen = (green + (255 - green) * ratio).toInt()
            val blendedBlue = (blue + (255 - blue) * ratio).toInt()

            return Color.argb(255, blendedRed, blendedGreen, blendedBlue)
        }
    }


    override fun getItemViewType(position: Int): Int {
        return if (dataList[position] is String) {
            TYPE_HEADER
        } else {
            TYPE_ITEM
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = dataList[position]
        when (holder) {
            is HeaderViewHolder -> {
                val totals = totalsMap[item as String] ?: Totals()
                holder.bind(item, totals)
            }

            is IncomeExpenseViewHolder -> {
                holder.bind(item)
            }
        }

        holder.itemView.setOnClickListener {
            when (holder) {
                is IncomeExpenseViewHolder -> {
                    itemClickListener.onItemClick(item)
                }
                is HeaderViewHolder -> {
                    itemClickListener.onItemClick(item)
                }
            }
        }
    }

}
