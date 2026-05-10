package com.example.expensetracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel

import com.example.expensetracker.data.ExpensesDatabase
import com.example.expensetracker.data.ExpensesRepository

import kotlinx.coroutines.launch

class ExpensesViewModel(application: Application) : AndroidViewModel(application){

    private val expensesRepository: ExpensesRepository

    init {
        val expensesDao = ExpensesDatabase.getDatabase(application).expensesDao()
        expensesRepository = ExpensesRepository(expensesDao)
    }

    val accounts = expensesRepository.getAccounts()
    val transactions = expensesRepository.getAllTransactions()


    fun insertAccount() {}
    fun removeAccount() {}

    fun importTransactions() {}
    fun updateTransactions() {}


    fun insertSplitTransaction() {}

    fun updateSplitTransaction() {}

    fun deleteSplitTransaction() {}






    // stuff to connect to api
    // connect accounts
    // connect transactions

}