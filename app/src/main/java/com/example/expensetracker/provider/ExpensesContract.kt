package com.example.expensetracker.provider

import android.net.Uri

object ExpensesContract {
    const val AUTHORITY = "com.example.expensetracker.provider"
    val BASE_CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY")

    object Accounts {
        const val PATH_ACCOUNTS = "accounts"
        val CONTENT_URI: Uri = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ACCOUNTS)
        const val CONTENT_TYPE = "vnd.android.cursor.dir/vnd.$AUTHORITY.$PATH_ACCOUNTS"
        const val CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.$AUTHORITY.$PATH_ACCOUNTS"

        const val COLUMN_ACCOUNT_ID = "account_id"
        const val COLUMN_AVAILABLE_BALANCE = "available_balance"
        const val COLUMN_CURRENT_BALANCE = "current_balance"
        const val COLUMN_CURRENCY_CODE = "currency_code"
        const val COLUMN_NAME = "name"
        const val COLUMN_BANK_NAME = "bankName"
        const val COLUMN_TYPE = "type"
        const val COLUMN_MASK = "mask"
    }
}