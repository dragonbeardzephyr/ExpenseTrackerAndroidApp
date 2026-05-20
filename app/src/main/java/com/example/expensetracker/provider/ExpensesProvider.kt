package com.example.expensetracker.provider

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.example.expensetracker.data.Account
import com.example.expensetracker.data.ExpensesDao
import com.example.expensetracker.data.ExpensesDatabase

class ExpensesProvider: ContentProvider() {

    companion object {
        private const val ACCOUNTS = 100
        private const val ACCOUNT_ID = 101

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(ExpensesContract.AUTHORITY, ExpensesContract.Accounts.PATH_ACCOUNTS, ACCOUNTS)
            addURI(ExpensesContract.AUTHORITY, "${ExpensesContract.Accounts.PATH_ACCOUNTS}/*", ACCOUNT_ID)
        }
    }

    private lateinit var expensesDao: ExpensesDao


    override fun onCreate(): Boolean {
        expensesDao = ExpensesDatabase.Companion.getDatabase(context!!).expensesDao()
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs:
        Array<String>?,
        sortOrder: String?
    ): Cursor? {
        val match = uriMatcher.match(uri)
        return when (match) {
            ACCOUNTS -> expensesDao.getAllAccountsCursor()
            ACCOUNT_ID -> {
                val id = ContentUris.parseId(uri)
                expensesDao.getAccountItemCursor(id.toString())
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val newRowId = when (com.example.expensetracker.provider.ExpensesProvider.Companion.uriMatcher.match(uri)) {
            ACCOUNTS -> {
                val account = Account(
                    account_id = values?.getAsString(ExpensesContract.Accounts.COLUMN_ACCOUNT_ID) ?: "",
                    available_balance = values?.getAsDouble(ExpensesContract.Accounts.COLUMN_AVAILABLE_BALANCE) ?: 0.0,
                    current_balance = values?.getAsDouble(ExpensesContract.Accounts.COLUMN_CURRENT_BALANCE) ?: 0.0,
                    currency_code = values?.getAsString(ExpensesContract.Accounts.COLUMN_CURRENCY_CODE) ?: "GBP",
                    name = values?.getAsString(ExpensesContract.Accounts.COLUMN_NAME) ?: "",
                    bankName = values?.getAsString(ExpensesContract.Accounts.COLUMN_BANK_NAME) ?: "",
                    type = values?.getAsString(ExpensesContract.Accounts.COLUMN_TYPE) ?: "",
                    mask = values?.getAsString(ExpensesContract.Accounts.COLUMN_MASK) ?: ""
                )
                expensesDao.insertAccountCP(account)
            }
            else -> throw IllegalArgumentException("Invalid URI for insert: $uri")
        }

        if (newRowId >= 0) {
            context?.contentResolver?.notifyChange(uri, null)
            val alphanumericId = values?.getAsString(ExpensesContract.Accounts.COLUMN_ACCOUNT_ID) ?: ""
            return Uri.withAppendedPath(uri, alphanumericId)
        }
        return null
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        throw UnsupportedOperationException("Update operation is not supported")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val match = com.example.expensetracker.provider.ExpensesProvider.Companion.uriMatcher.match(uri)
        return when (match) {
            ACCOUNT_ID -> {
                val accountId = uri.lastPathSegment ?: throw IllegalArgumentException("Missing alphanumeric identifier")
                expensesDao.getAccountItemCursor(accountId)
                val cursor = expensesDao.getAccountItemCursor(accountId)
                if (cursor.moveToFirst()) {
                    val account = Account(
                        account_id = cursor.getString(cursor.getColumnIndexOrThrow(ExpensesContract.Accounts.COLUMN_ACCOUNT_ID)),
                        available_balance = cursor.getDouble(cursor.getColumnIndexOrThrow(ExpensesContract.Accounts.COLUMN_AVAILABLE_BALANCE)),
                        current_balance = cursor.getDouble(cursor.getColumnIndexOrThrow(ExpensesContract.Accounts.COLUMN_CURRENT_BALANCE)),
                        currency_code = cursor.getString(cursor.getColumnIndexOrThrow(ExpensesContract.Accounts.COLUMN_CURRENCY_CODE)),
                        name = cursor.getString(cursor.getColumnIndexOrThrow(ExpensesContract.Accounts.COLUMN_NAME)),
                        bankName = cursor.getString(cursor.getColumnIndexOrThrow(ExpensesContract.Accounts.COLUMN_BANK_NAME)),
                        type = cursor.getString(cursor.getColumnIndexOrThrow(ExpensesContract.Accounts.COLUMN_TYPE)),
                        mask = cursor.getString(cursor.getColumnIndexOrThrow(ExpensesContract.Accounts.COLUMN_MASK))
                    )
                    cursor.close()
                    expensesDao.deleteAccountCP(account)
                } else {
                    cursor.close()
                    0  // No movie found to delete
                }
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            ACCOUNTS -> ExpensesContract.Accounts.CONTENT_TYPE
            ACCOUNT_ID -> ExpensesContract.Accounts.CONTENT_ITEM_TYPE
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }






}