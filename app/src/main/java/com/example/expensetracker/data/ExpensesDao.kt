package com.example.expensetracker.data
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.OnConflictStrategy
import androidx.room.Insert

import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert

import java.time.LocalDate

@Dao
interface ExpensesDao {
    // Account


    @Upsert() // do we want to ignore or replace?
    suspend fun insertAccount(account: Account)

    @Query("SELECT * FROM accounts")
    fun getAccounts(): LiveData<List<Account>>

    @Query("SELECT * FROM accounts")
    suspend fun getAccountsStatic(): List<Account>

    @Query("SELECT * FROM accounts WHERE account_id = :accId LIMIT 1")
    suspend fun getAccountByIdStatic(accId: String): Account?


    // Net Worth

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNetWorth(netWorth: NetWorth)

    @Query("SELECT * FROM net_worth ORDER BY date DESC LIMIT 1")
    fun getNetWorth(): LiveData<NetWorth>

    @Query("SELECT * FROM net_worth") // for line chart
    fun getAllNetWorth(): LiveData<List<NetWorth>>


    // Transactions

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Query("SELECT * FROM transactions")
    fun getAllTransactions(): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE account_id = :accId")
    fun getTransactionsByAccount(accId: String): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE transaction_id = :transactionId LIMIT 1")
    fun getTransactionByIdStatic(transactionId: String): Transaction?



    // Split Transactions

    @Upsert()
    suspend fun insertSplitTransaction(split: SplitTransaction)

    @Query("DELETE FROM split_transactions WHERE split_id = :splitId")
    suspend fun deleteSplitTransaction(splitId: Int)


    @Query("SELECT * FROM split_transactions WHERE parent_id = :transactionId")
    fun getSplitTransactionsByTransaction(transactionId: String): LiveData<List<SplitTransaction>>

    @Query("SELECT * FROM split_transactions")
    fun getAllSplitTransactions(): LiveData<List<SplitTransaction>>

    @Query("UPDATE transactions SET is_split = :isSplit WHERE transaction_id = :transactionId")
    suspend fun updateSplitStatus(transactionId: String, isSplit: Boolean)

    @Query("SELECT COUNT(*) FROM split_transactions WHERE parent_id = :transactionId")
    suspend fun getSplitCount(transactionId: String): Int



    //Categories


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Delete()
    suspend fun deleteCategory(category: Category)

    @Query("SELECT * FROM categories")
    fun getAllCategories(): LiveData<List<Category>>

    @Query("SELECT * FROM categories WHERE cat_id = :catId")
    fun getCategoryById(catId: Int): LiveData<Category>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int

    @Query("SELECT * FROM categories WHERE cat_plaid = :plaidString LIMIT 1")
    suspend fun getCategoryByPlaid(plaidString: String): Category?


    //Budgets

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget)

    @Query("SELECT * FROM budgets WHERE month = :month")
    fun getBudgetsForMonth(month: String): LiveData<List<Budget>>

    @Query("DELETE FROM budgets WHERE budget_id = :budgetId")
    suspend fun deleteBudget(budgetId: Int)











}
