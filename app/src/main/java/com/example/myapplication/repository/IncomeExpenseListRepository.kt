    package com.example.myapplication.repository

    import androidx.annotation.WorkerThread
    import com.example.myapplication.dao.IncomeExpenseListDao
    import com.example.myapplication.data.CategoryWithIncomeExpenseList
    import com.example.myapplication.entity.IncomeExpenseList
    import kotlinx.coroutines.flow.Flow
    import org.threeten.bp.YearMonth

    class IncomeExpenseListRepository(private val incomeExpenseListDao: IncomeExpenseListDao) {
        val allIncomeExpenseList: Flow<List<IncomeExpenseList>> = incomeExpenseListDao.getAllIncomeExpenseList()

        @WorkerThread
        fun insert(incomeExpenseList: IncomeExpenseList) {
            incomeExpenseListDao.insert(incomeExpenseList)
        }


        fun getIncomeExpenseList(year: String, month: String, categoryId: Int): Flow<List<CategoryWithIncomeExpenseList>> {
            return incomeExpenseListDao.getIncomeExpenseListByMonthYearIdCategory(year, month, categoryId)
        }

        fun getIncomeExpenseList(year: String, month: String): Flow<List<CategoryWithIncomeExpenseList>> {
            return incomeExpenseListDao.getIncomeExpenseListByMonthYear(year, month)
        }

        @WorkerThread
        suspend fun deleteIncomeExpenseList(incomeExpenseList: IncomeExpenseList) {
            incomeExpenseListDao.delete(incomeExpenseList)
        }

        @WorkerThread
        suspend fun updateIncomeExpenseList(incomeExpenseList: IncomeExpenseList) {
            incomeExpenseListDao.update(incomeExpenseList)
        }

    }