package com.example.expensetracker.data

import androidx.lifecycle.LiveData

class ExpensesRepository(private val expensesDao: ExpensesDao) {

    // Account

    suspend fun insertAccount(account: Account) {
        expensesDao.insertAccount(account)
    }
    fun getAccounts(): LiveData<List<Account>> {
        return expensesDao.getAccounts()
    }

    // Net Worth

    suspend fun insertNetWorth(netWorth: NetWorth) {
        expensesDao.insertNetWorth(netWorth)
    }

    fun getNetWorth(date: String): LiveData<NetWorth> {
        return expensesDao.getNetWorth(date)
    }

    fun getAllNetWorth(): LiveData<List<NetWorth>> {
        return expensesDao.getAllNetWorth()
    }


    // Transactions

    suspend fun insertTransaction(transaction: Transaction) {
        expensesDao.insertTransaction(transaction)
    }

    fun getAllTransactions(): LiveData<List<Transaction>> {
        return expensesDao.getAllTransactions()
    }

    fun getTransactionsByAccount(accId: String): LiveData<List<Transaction>> {
        return expensesDao.getTransactionsByAccount(accId)
    }


    // Split Transactions

    suspend fun insertSplitTransactions(split: SplitTransaction) {
        expensesDao.insertSplitTransaction(split)
        expensesDao.updateSplitStatus(split.parent_id, true)
    }

    fun getSplitTransactionsByTransaction(transactionId: String): LiveData<List<SplitTransaction>> {
        return expensesDao.getSplitTransactionsByTransaction(transactionId)
    }



}
