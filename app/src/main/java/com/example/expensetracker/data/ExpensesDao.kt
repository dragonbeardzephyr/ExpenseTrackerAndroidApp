package com.example.expensetracker.data
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.OnConflictStrategy
import androidx.room.Insert

import androidx.room.Query
import java.time.LocalDate

@Dao
interface ExpensesDao {
    // Account


    @Insert(onConflict = OnConflictStrategy.REPLACE) // do we want to ignore or replace?
    suspend fun insertAccount(account: Account)

    @Query("SELECT * FROM accounts")
    fun getAccounts(): LiveData<List<Account>>


    // Net Worth

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNetWorth(netWorth: NetWorth)

    @Query("SELECT * FROM net_worth ORDER BY date DESC LIMIT 1")
    fun getNetWorth(date: String): LiveData<NetWorth>

    @Query("SELECT * FROM net_worth") // for line chart
    fun getAllNetWorth(): LiveData<List<NetWorth>>


    // Transactions

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Query("SELECT * FROM transactions")
    fun getAllTransactions(): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE account_id = :accId")
    fun getTransactionsByAccount(accId: String): LiveData<List<Transaction>>


    // Split Transactions

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSplitTransaction(split: SplitTransaction)

    @Query("SELECT * FROM split_transactions WHERE parent_id = :transactionId")
    fun getSplitTransactionsByTransaction(transactionId: String): LiveData<List<SplitTransaction>>

    @Query("UPDATE transactions SET is_split = :isSplit WHERE transaction_id = :transactionId")
    suspend fun updateSplitStatus(transactionId: String, isSplit: Boolean)
}