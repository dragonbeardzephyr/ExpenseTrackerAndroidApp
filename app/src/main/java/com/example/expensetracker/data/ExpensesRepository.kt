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

    suspend fun getAccountsStatic(): List<Account> {
        return expensesDao.getAccountsStatic()
    }

    suspend fun getAccountByIdStatic(accId: String): Account? {
        return expensesDao.getAccountByIdStatic(accId)
    }

    // Net Worth

    suspend fun insertNetWorth(netWorth: NetWorth) {
        expensesDao.insertNetWorth(netWorth)
    }

    fun getNetWorth(): LiveData<NetWorth> {
        return expensesDao.getNetWorth()
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

    fun getTransactionByIdStatic(transactionId: String): Transaction? {
        return expensesDao.getTransactionByIdStatic(transactionId)
    }



    // Split Transactions

    suspend fun insertSplitTransaction(split: SplitTransaction) {
        expensesDao.insertSplitTransaction(split)
        expensesDao.updateSplitStatus(split.parent_id, true)
    }


    suspend fun deleteSplitTransaction(splitId: Int, parentId: String) {
        expensesDao.deleteSplitTransaction(splitId)

        val remainingSplits = expensesDao.getSplitCount(parentId)

        if (remainingSplits == 0) {
            expensesDao.updateSplitStatus(parentId, false)
        }
    }


    fun getSplitTransactionsByTransaction(transactionId: String): LiveData<List<SplitTransaction>> {
        return expensesDao.getSplitTransactionsByTransaction(transactionId)
    }



    // Categories


    suspend fun insertCategory(category: Category): Int {
        return expensesDao.insertCategory(category).toInt()
    }

    fun getAllCategories(): LiveData<List<Category>> {
        return expensesDao.getAllCategories()
    }

    fun getCategoryById(catId: Int): LiveData<Category> {
        return expensesDao.getCategoryById(catId)
    }

    suspend fun getCategoryCount(): Int {
        return expensesDao.getCategoryCount()
    }

    suspend fun getCategoryByPlaid(plaidString: String): Category? {
        return expensesDao.getCategoryByPlaid(plaidString)
    }


    // Budgets

    suspend fun insertBudget(budget: Budget) {
        expensesDao.insertBudget(budget)
    }

    fun getBudgetsForMonth(month: String): LiveData<List<Budget>> {
        return expensesDao.getBudgetsForMonth(month)
    }

    suspend fun deleteBudget(budgetId: Int) {
        expensesDao.deleteBudget(budgetId)
    }




}
