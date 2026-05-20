package com.example.expensetracker

import android.R.attr.rating
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.expensetracker.provider.ExpensesContract
import org.junit.After

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import kotlin.toString

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

@RunWith(AndroidJUnit4::class)
class ExpensesProviderTest {
    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.expensetracker", appContext.packageName)
    }

    private lateinit var context: Context
    private lateinit var resolver: ContentResolver

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        resolver = context.contentResolver

        val values = ContentValues().apply {
            put(ExpensesContract.Accounts.COLUMN_ACCOUNT_ID, "plaid_id_init_123")
            put(ExpensesContract.Accounts.COLUMN_AVAILABLE_BALANCE, 2500.50)
            put(ExpensesContract.Accounts.COLUMN_CURRENT_BALANCE, 2600.00)
            put(ExpensesContract.Accounts.COLUMN_CURRENCY_CODE, "GBP")
            put(ExpensesContract.Accounts.COLUMN_NAME, "Initial Checking")
            put(ExpensesContract.Accounts.COLUMN_BANK_NAME, "Monzo Bank")
            put(ExpensesContract.Accounts.COLUMN_TYPE, "depository")
            put(ExpensesContract.Accounts.COLUMN_MASK, "4444")
        }
        Log.d("setup", values.toString())
        val uri = resolver.insert(ExpensesContract.Accounts.CONTENT_URI, values)
    }

    @Test
    fun testQueryAllAccounts() {
        val cursor = resolver.query(ExpensesContract.Accounts.CONTENT_URI, null, null, null, null)
        assertNotNull(cursor)

        assertTrue("Cursor is empty", cursor!!.moveToFirst())

        do {
            val accountId = cursor.getString(cursor.getColumnIndexOrThrow(ExpensesContract.Accounts.COLUMN_ACCOUNT_ID))
            val currentBalance = cursor.getDouble(cursor.getColumnIndexOrThrow(ExpensesContract.Accounts.COLUMN_CURRENT_BALANCE))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(ExpensesContract.Accounts.COLUMN_NAME))
            val bankName = cursor.getString(cursor.getColumnIndexOrThrow(ExpensesContract.Accounts.COLUMN_BANK_NAME))


            Log.d("testQueryAllAccounts", "AccID: $accountId, Name: $name, Bank: $bankName, Bal: £$currentBalance")


            assertNotNull(accountId)
            assertNotNull(name)
            assertNotNull(bankName)
            assertTrue(currentBalance >= 0.0)

        } while (cursor.moveToNext())

        cursor.close()
    }

    @Test
    fun testInsertAccount() {
        val values = ContentValues().apply {
            put(ExpensesContract.Accounts.COLUMN_ACCOUNT_ID, "plaid_test_id_999")
            put(ExpensesContract.Accounts.COLUMN_AVAILABLE_BALANCE, 50.00)
            put(ExpensesContract.Accounts.COLUMN_CURRENT_BALANCE, 55.20)
            put(ExpensesContract.Accounts.COLUMN_CURRENCY_CODE, "GBP")
            put(ExpensesContract.Accounts.COLUMN_NAME, "Test Savings")
            put(ExpensesContract.Accounts.COLUMN_BANK_NAME, "Barclays")
            put(ExpensesContract.Accounts.COLUMN_TYPE, "depository")
            put(ExpensesContract.Accounts.COLUMN_MASK, "1111")
        }
        Log.d("testInsertAccount", values.toString())
        val uri = resolver.insert(ExpensesContract.Accounts.CONTENT_URI, values)
        assertNotNull(uri)
        Log.d("testInsertAccount", uri.toString())

        val accountId = uri!!.lastPathSegment
        Log.d("testInsertAccount", accountId.toString())
        assertEquals("plaid_test_id_999", accountId)
    }

    @Test
    fun testDeleteAccount() {
        val uri = ExpensesContract.Accounts.CONTENT_URI
        val uniqueId = "plaid_to_delete_777"

        val values = ContentValues().apply {
            put(ExpensesContract.Accounts.COLUMN_ACCOUNT_ID, uniqueId)
            put(ExpensesContract.Accounts.COLUMN_AVAILABLE_BALANCE, 10.00)
            put(ExpensesContract.Accounts.COLUMN_CURRENT_BALANCE, 10.00)
            put(ExpensesContract.Accounts.COLUMN_CURRENCY_CODE, "GBP")
            put(ExpensesContract.Accounts.COLUMN_NAME, "Temporary Account")
            put(ExpensesContract.Accounts.COLUMN_BANK_NAME, "Sandbox Bank")
            put(ExpensesContract.Accounts.COLUMN_TYPE, "depository")
            put(ExpensesContract.Accounts.COLUMN_MASK, "0000")
        }
        val insertUri = resolver.insert(uri, values)
        assertNotNull("Insert failed", insertUri)

        val targetItemUri = Uri.withAppendedPath(uri, uniqueId)

        val deleteCount = resolver.delete(targetItemUri, null, null)
        assertEquals("Delete failed", 1, deleteCount)

        val cursor = resolver.query(targetItemUri, null, null, null, null)
        assertNotNull(cursor)
        assertFalse("Account should be deleted", cursor!!.moveToFirst())
        cursor.close()
    }

    @Test(expected = IllegalArgumentException::class)
    fun testQueryInvalidUri() {
        resolver.query(Uri.parse("content://com.example.expensetracker.provider/invalid"), null, null, null, null)
    }

    @After
    fun tearDown() {
        // clear the database if needed after test
    }

}



