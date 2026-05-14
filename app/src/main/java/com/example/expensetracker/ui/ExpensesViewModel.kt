package com.example.expensetracker.ui

import android.R.attr.phoneNumber
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.Account

import com.example.expensetracker.data.ExpensesDatabase
import com.example.expensetracker.data.ExpensesRepository
import com.example.expensetracker.data.LinkTokenRequest
import com.example.expensetracker.data.LinkTokenUser
import com.example.expensetracker.data.NetWorth
import com.example.expensetracker.data.Secrets
import com.example.expensetracker.data.Transaction

import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Arrays


class ExpensesViewModel(application: Application) : AndroidViewModel(application){

    private val expensesRepository: ExpensesRepository

    init {
        val expensesDao = ExpensesDatabase.getDatabase(application).expensesDao()
        expensesRepository = ExpensesRepository(expensesDao)
    }

    private val clientId = Secrets.CLIENT_ID // These must be made secret in the end
    private val sandboxKey = Secrets.SECRET


    val accounts: LiveData<List<Account>> = expensesRepository.getAccounts()
    val transactions: LiveData<List<Transaction>> = expensesRepository.getAllTransactions()

    val clientUserId = "user_id"

    fun getLinkToken(onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            val user = LinkTokenUser(
                clientUserId,
                "legal name",
                "447467983412",
                "email@address.com"
            )

            val request = LinkTokenRequest(
                Secrets.CLIENT_ID,
                Secrets.SECRET,
                user,
                "Expense Tracker",
                listOf("transactions"),
                listOf("GB"),
                "en",
                "com.example.expensetracker"
            )
            // Use Retrofit to POST to https://sandbox.plaid.com/link/token/create

            val token = "random stuff"
            onSuccess(token)
        }
    }



    suspend fun updateNetWorth(accounts: List<Account>) {
        viewModelScope.launch {
            // Calculate latest net worth

            var latestNetWorth = 0.0

            for (account in accounts) {
                latestNetWorth += account.current_balance
            }

            expensesRepository.insertNetWorth(
                NetWorth(
                    date = LocalDate.now(),
                    amount = latestNetWorth
                )
            )
            // Update UI or set seomwhere to be accessed
        }
    }



    fun insertAccount() {}
    fun removeAccount() {}

    fun importTransactions() {}
    fun updateTransactions() {} //syncTransactions??

    fun insertTransaction() {}
    fun updateTransaction() {}
    fun deleteTransaction() {}


    fun insertSplitTransaction() {}

    fun updateSplitTransaction() {}

    fun deleteSplitTransaction() {}






    // stuff to connect to api
    // connect accounts
    // connect transactions

}