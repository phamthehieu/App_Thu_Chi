package com.example.myapplication.utilities

import android.content.Context
import android.util.Log
import com.example.myapplication.database.CategoryDatabase
import com.example.myapplication.entity.Account
import com.example.myapplication.entity.Category
import com.example.myapplication.entity.HistoryAccount
import com.example.myapplication.entity.IncomeExpenseList
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.FileReader
import java.io.FileWriter

object DatabaseSyncHelper {

    enum class DataType {
        ACCOUNTS,
        CATEGORIES,
        HISTORY_ACCOUNTS,
        INCOME_EXPENSE_LIST
    }


    suspend fun syncDatabaseWithDrive(driveService: Drive, context: Context, dataType: DataType) {
        when (dataType) {
            DataType.ACCOUNTS -> {
                val accountFile = exportAccountsToJson(context)
                uploadFileToDrive(driveService, accountFile, "AccountsBackup.json")
            }

            DataType.CATEGORIES -> {
                val categoryFile = exportCategoriesToJson(context)
                uploadFileToDrive(driveService, categoryFile, "CategoriesBackup.json")
            }

            DataType.HISTORY_ACCOUNTS -> {
                val historyAccountFile = exportHistoryAccountsToJson(context)
                uploadFileToDrive(driveService, historyAccountFile, "HistoryAccountsBackup.json")
            }

            DataType.INCOME_EXPENSE_LIST -> {
                val incomeExpenseListFile = exportIncomeExpenseListToJson(context)
                uploadFileToDrive(
                    driveService,
                    incomeExpenseListFile,
                    "IncomeExpenseListBackup.json"
                )
            }
        }
    }

    suspend fun syncDatabaseWithDriveAll(driveService: Drive, context: Context): Boolean {
        try {
            val incomeExpenseListFile = exportIncomeExpenseListToJson(context)
            val accountFile = exportAccountsToJson(context)
            val categoryFile = exportCategoriesToJson(context)
            val historyAccountFile = exportHistoryAccountsToJson(context)

            uploadFileToDrive(driveService, incomeExpenseListFile, "IncomeExpenseListBackup.json")
            uploadFileToDrive(driveService, historyAccountFile, "HistoryAccountsBackup.json")
            uploadFileToDrive(driveService, accountFile, "AccountsBackup.json")
            uploadFileToDrive(driveService, categoryFile, "CategoriesBackup.json")
            return true
        } catch (e: Exception) {
            return false
        }
    }

    private suspend fun exportAccountsToJson(context: Context): java.io.File {
        val accountList = mutableListOf<Account>()
        val accounts = CategoryDatabase.getDatabase(context, CoroutineScope(Dispatchers.IO))
            .accountDao()
            .getAllAccountsUpdate()
            .first()
        accountList.addAll(accounts)
        val tempFile = java.io.File(context.cacheDir, "AccountsBackup.json")
        withContext(Dispatchers.IO) {
            FileWriter(tempFile).use { writer ->
                val gson = Gson()
                gson.toJson(accountList, writer)
            }
        }
        return tempFile
    }

    private suspend fun exportCategoriesToJson(context: Context): java.io.File {
        val categoriesList = mutableListOf<Category>()
        val categoryDao = CategoryDatabase.getDatabase(context, CoroutineScope(Dispatchers.IO))
            .categoryDao()
            .getAllCategoryUpdate()
            .first()
        categoriesList.addAll(categoryDao)

        val tempFile = java.io.File(context.cacheDir, "CategoriesBackup.json")
        withContext(Dispatchers.IO) {
            FileWriter(tempFile).use { writer ->
                val gson = Gson()
                gson.toJson(categoriesList, writer)
            }
        }
        return tempFile;
    }


    private suspend fun exportHistoryAccountsToJson(context: Context): java.io.File {
        val listHistoryAccount = mutableListOf<HistoryAccount>()
        val historyAccountDao =
            CategoryDatabase.getDatabase(context, CoroutineScope(Dispatchers.IO))
                .historyAccountDao()
                .allHistoryAccount()
                .first()
        listHistoryAccount.addAll(historyAccountDao)

        val tempFile = java.io.File(context.cacheDir, "HistoryAccountsBackup.json")
        try {
            withContext(Dispatchers.IO) {
                FileWriter(tempFile).use { writer ->
                    val gson = Gson()
                    gson.toJson(listHistoryAccount, writer)
                }
            }
            Log.d("DatabaseSyncHelper", "History accounts exported to ${tempFile.absolutePath}")
        } catch (e: Exception) {
            Log.e("DatabaseSyncHelper", "Failed to export history accounts", e)
        }
        return tempFile
    }


    private suspend fun exportIncomeExpenseListToJson(context: Context): java.io.File {
        val listIncomeExpenseList = mutableListOf<IncomeExpenseList>()
        val incomeExpenseListDao =
            CategoryDatabase.getDatabase(context, CoroutineScope(Dispatchers.IO))
                .incomeExpenseListDao()
                .getAllIncomeExpenseList()
                .first()
        listIncomeExpenseList.addAll(incomeExpenseListDao)

        val tempFile = java.io.File(context.cacheDir, "IncomeExpenseListBackup.json")
        withContext(Dispatchers.IO) {
            FileWriter(tempFile).use { writer ->
                val gson = Gson()
                gson.toJson(listIncomeExpenseList, writer)
            }
        }
        return tempFile
    }

    private suspend fun uploadFileToDrive(
        driveService: Drive,
        file: java.io.File,
        fileName: String
    ) {
        withContext(Dispatchers.IO) {
            val existingFileId = findFileIdByName(driveService, fileName)

            val fileMetadata = File().apply {
                name = fileName
                mimeType = "application/json"
            }

            val mediaContent = FileContent("application/json", file)

            val fileResult: File? = if (existingFileId != null) {
                driveService.files().update(existingFileId, fileMetadata, mediaContent)
                    .execute()
            } else {
                driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute()
            }

            if (fileResult != null) {
                Log.d("DatabaseSyncHelper", "File uploaded successfully with ID: ${fileResult.id}")
            } else {
                Log.e("DatabaseSyncHelper", "File upload failed")
            }
        }
    }


    suspend fun downloadAndRestoreDatabase(driveService: Drive, context: Context) {
        val accountFileId = findFileIdByName(driveService, "AccountsBackup.json")
        val categoryFileId = findFileIdByName(driveService, "CategoriesBackup.json")
        val historyAccountFileId = findFileIdByName(driveService, "HistoryAccountsBackup.json")
        val incomeExpenseListFileId = findFileIdByName(driveService, "IncomeExpenseListBackup.json")

        accountFileId?.let { downloadAndRestoreAccounts(driveService, it, context) }
        categoryFileId?.let { downloadAndRestoreCategories(driveService, it, context) }
        historyAccountFileId?.let { downloadAndRestoreHistoryAccounts(driveService, it, context) }
        incomeExpenseListFileId?.let {
            downloadAndRestoreIncomeExpenseList(
                driveService,
                it,
                context
            )
        }
    }

    private suspend fun downloadAndRestoreAccounts(
        driveService: Drive,
        fileId: String,
        context: Context
    ) {
        val tempFile = java.io.File(context.cacheDir, "AccountsBackup.json")
        try {
            withContext(Dispatchers.IO) {
                driveService.files().get(fileId)
                    .executeMediaAndDownloadTo(tempFile.outputStream())
            }
            restoreAccountsFromJson(context, tempFile)
        } catch (e: Exception) {
            Log.e("hieu185", "Failed to download and restore file", e)
        }
    }

    private suspend fun restoreAccountsFromJson(context: Context, file: java.io.File) {
        withContext(Dispatchers.IO) {
            val bufferedReader = BufferedReader(FileReader(file))
            val gson = Gson()
            val accounts = gson.fromJson(bufferedReader, Array<Account>::class.java).toList()
            val accountDao =
                CategoryDatabase.getDatabase(context, CoroutineScope(Dispatchers.IO)).accountDao()
            accountDao.insertListAccounts(accounts)
        }
    }

    private suspend fun downloadAndRestoreCategories(
        driveService: Drive,
        fileId: String,
        context: Context
    ) {
        val tempFile = java.io.File(context.cacheDir, "CategoriesBackup.json")
        try {
            withContext(Dispatchers.IO) {
                driveService.files().get(fileId)
                    .executeMediaAndDownloadTo(tempFile.outputStream())
            }
            restoreCategoriesFromJson(context, tempFile)
        } catch (e: Exception) {
            Log.e("hieu185", "Failed to download and restore file", e)
        }
    }

    private suspend fun restoreCategoriesFromJson(context: Context, file: java.io.File) {
        withContext(Dispatchers.IO) {
            val bufferedReader = BufferedReader(FileReader(file))
            val gson = Gson()
            val categories = gson.fromJson(bufferedReader, Array<Category>::class.java).toList()

            val categoryDao =
                CategoryDatabase.getDatabase(context, CoroutineScope(Dispatchers.IO)).categoryDao()
            categoryDao.insertAllCategory(categories)
        }
    }

    private suspend fun downloadAndRestoreHistoryAccounts(
        driveService: Drive,
        fileId: String,
        context: Context
    ) {
        val tempFile = java.io.File(context.cacheDir, "HistoryAccountsBackup.json")
        try {
            withContext(Dispatchers.IO) {
                driveService.files().get(fileId)
                    .executeMediaAndDownloadTo(tempFile.outputStream())
            }
            restoreHistoryAccountsFromJson(context, tempFile)
        } catch (e: Exception) {
            Log.e("hieu185", "Failed to download and restore file", e)
        }
    }

    private suspend fun restoreHistoryAccountsFromJson(context: Context, file: java.io.File) {
        withContext(Dispatchers.IO) {
            val bufferedReader = BufferedReader(FileReader(file))
            val gson = Gson()
            val historyAccounts =
                gson.fromJson(bufferedReader, Array<HistoryAccount>::class.java).toList()

            val historyAccountDao =
                CategoryDatabase.getDatabase(context, CoroutineScope(Dispatchers.IO))
                    .historyAccountDao()
            historyAccountDao.insertList(historyAccounts)
        }
    }

    private suspend fun downloadAndRestoreIncomeExpenseList(
        driveService: Drive,
        fileId: String,
        context: Context
    ) {
        val tempFile = java.io.File(context.cacheDir, "IncomeExpenseListBackup.json")
        try {
            withContext(Dispatchers.IO) {
                driveService.files().get(fileId)
                    .executeMediaAndDownloadTo(tempFile.outputStream())
            }
            restoreIncomeExpenseListFromJson(context, tempFile)
        } catch (e: Exception) {
            Log.e("hieu185", "Failed to download and restore file", e)
        }
    }


    private suspend fun restoreIncomeExpenseListFromJson(context: Context, file: java.io.File) {
        withContext(Dispatchers.IO) {
            val bufferedReader = BufferedReader(FileReader(file))
            val gson = Gson()
            val incomeExpenseList =
                gson.fromJson(bufferedReader, Array<IncomeExpenseList>::class.java).toList()
            val incomeExpenseListDao =
                CategoryDatabase.getDatabase(context, CoroutineScope(Dispatchers.IO))
                    .incomeExpenseListDao()
            incomeExpenseListDao.insertList(incomeExpenseList)
        }
    }

    private suspend fun findFileIdByName(driveService: Drive, fileName: String): String? {
        return withContext(Dispatchers.IO) {
            val result: FileList = driveService.files().list()
                .setQ("name = '$fileName'")
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute()
            result.files.firstOrNull()?.id
        }
    }
}
