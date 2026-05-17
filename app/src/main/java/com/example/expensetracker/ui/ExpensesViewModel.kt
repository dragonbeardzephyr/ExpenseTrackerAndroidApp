package com.example.expensetracker.ui

import android.R.attr.name
import android.app.Application
import android.text.TextUtils.split
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import kotlin.math.exp

class ExpensesViewModel(application: Application) : AndroidViewModel(application){

    private val expensesRepository: ExpensesRepository
    private val preferenceManager = ExpensePreferenceManager(application)

    init {
        val expensesDao = ExpensesDatabase.getDatabase(application).expensesDao()
        expensesRepository = ExpensesRepository(expensesDao)
        checkAndSync()
    }

    val accounts: LiveData<List<Account>> = expensesRepository.getAccounts()
    val transactions: LiveData<List<Transaction>> = expensesRepository.getAllTransactions()

    val derivedNetWorth: LiveData<Double> = accounts.map { accountList ->
        accountList.sumOf { it.current_balance }
    }

    val clientUserId = "user_id"

    private val plaidApi = Retrofit.Builder()
        .baseUrl("https://sandbox.plaid.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(PlaidApiHandler::class.java)


    fun exchangePublicToken(publicToken: String) {
        viewModelScope.launch {
            try {
                val request = TokenExchangeRequest(
                    client_id = Secrets.CLIENT_ID,
                    secret = Secrets.SECRET,
                    public_token = publicToken
                )
                val response = plaidApi.exchangeToken(request)

                preferenceManager.saveAccessToken(response.access_token)
                preferenceManager.saveSyncCursor("")

                // Triggers fresh structural sync automatically
                syncAccounts()
                syncTransactions()

            } catch (e: Exception) {
                Log.e("PlaidError", "Exchange failed", e)
            }
        }
    }

    fun getLinkToken(onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val user = LinkTokenUser(clientUserId, "legal name", "447467983412", "email@address.com")
                val request = LinkTokenRequest(
                    Secrets.CLIENT_ID, Secrets.SECRET, user, "Expense Tracker",
                    listOf("transactions"), listOf("GB"), "en", "com.example.expensetracker"
                )
                val response = plaidApi.createLinkToken(request)
                onSuccess(response.link_token)
            } catch (e: Exception) {
                Log.e("PlaidError", "Failed to create link token", e)
            }
        }
    }

    fun checkAndSync() {
        viewModelScope.launch {
            val lastSync = preferenceManager.lastSyncTime.first()
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastSync > 1000) {
                syncAccounts()
                syncTransactions()
                preferenceManager.saveSyncTime(currentTime)
            }
        }
    }


    suspend fun syncAccounts() {
        val token = preferenceManager.accessToken.first()
        if (token != null) {
            try {
                val request = AccountsGetRequest(
                    client_id = Secrets.CLIENT_ID,
                    secret = Secrets.SECRET,
                    access_token = token
                )
                val response = plaidApi.getAccounts(request)

                response.accounts.forEach { plaidAccount ->
                    // Check if this account already exists in the database
                    val savedAccount = expensesRepository.getAccountByIdStatic(plaidAccount.account_id)

                    // Safe Rule: Keep the bank name if it's already there, otherwise default to "Bank"
                    val resolvedBankName = savedAccount?.bankName ?: "Bank"

                    val dbAccount = Account(
                        account_id = plaidAccount.account_id,
                        available_balance = plaidAccount.balances.available ?: 0.0,
                        current_balance = plaidAccount.balances.current,
                        currency_code = plaidAccount.balances.iso_currency_code ?: "USD",
                        name = plaidAccount.name,
                        type = plaidAccount.type,
                        mask = plaidAccount.mask ?: "",
                        bankName = resolvedBankName
                    )
                    expensesRepository.insertAccount(dbAccount)
                }

                // Calculate historical net worth from your static table states
                val updatedAccountsList = expensesRepository.getAccountsStatic()
                val databaseNetWorthCalculated = updatedAccountsList.sumOf { it.current_balance }

                expensesRepository.insertNetWorth(
                    NetWorth(
                        date = LocalDate.now(),
                        amount = databaseNetWorthCalculated
                    )
                )
            } catch (e: Exception) {
                Log.e("PlaidError", "Sync failed: ${e.message}")
            }
        }
    }

    suspend fun syncTransactions() {
        val token = preferenceManager.accessToken.first()

        if (token != null) {
            var cursor = preferenceManager.syncCursor.first()
            try {
                var hasMore = true
                while (hasMore) {
                    val request = TransactionsSyncRequest(Secrets.CLIENT_ID, Secrets.SECRET, token, cursor)
                    val response = plaidApi.syncTransactions(request)

                    response.added.forEach { plaidTransaction ->
                        val dbTransaction = Transaction(
                            transaction_id = plaidTransaction.transaction_id,
                            account_id = plaidTransaction.account_id,
                            amount = plaidTransaction.amount,
                            transaction_code = plaidTransaction.payment_meta?.reference_number ?: "",
                            date = LocalDate.parse(plaidTransaction.date).atStartOfDay(),
                            merchant_name = plaidTransaction.merchant_name ?: "",
                            name = plaidTransaction.name,
                            is_excluded = false,
                            cat_primary = plaidTransaction.personal_finance_category?.primary,
                            cat_detailed = plaidTransaction.personal_finance_category?.detailed,
                            pending = plaidTransaction.pending,
                            is_split = false
                        )
                        expensesRepository.insertTransaction(dbTransaction)
                    }

                    response.modified.forEach { plaidTransaction ->
                        val dbTransaction = Transaction(
                            transaction_id = plaidTransaction.transaction_id,
                            account_id = plaidTransaction.account_id,
                            amount = plaidTransaction.amount,
                            transaction_code = plaidTransaction.payment_meta?.reference_number ?: "",
                            date = LocalDate.parse(plaidTransaction.date).atStartOfDay(),
                            merchant_name = plaidTransaction.merchant_name ?: "",
                            name = plaidTransaction.name,
                            is_excluded = false,
                            cat_primary = plaidTransaction.personal_finance_category?.primary,
                            cat_detailed = plaidTransaction.personal_finance_category?.detailed,
                            pending = plaidTransaction.pending,
                            is_split = false
                        )
                        expensesRepository.insertTransaction(dbTransaction)
                    }

                    cursor = response.next_cursor
                    hasMore = response.has_more
                    preferenceManager.saveSyncCursor(cursor)
                }
            } catch (e: Exception) {
                Log.e("PlaidTransactionSync", "Failed syncing transactions: ${e.message}", e)
            }
        }
    }


    fun insertAccount() {} //Offline Accounts
    fun removeAccount() {}


    fun getSplitTransactionsById(transactionId: String): LiveData<List<SplitTransaction>> {
        return expensesRepository.getSplitTransactionsByTransaction(transactionId)
    }

    fun insertSplitTransaction(split: SplitTransaction) {
        viewModelScope.launch {
            expensesRepository.insertSplitTransaction(split)
        }

    }

    fun updateSplitTransaction(split: SplitTransaction) {
        viewModelScope.launch {
            expensesRepository.insertSplitTransaction(split)
        }
    }

    fun deleteSplitTransaction(splitId: Int, parentId: String) {
        viewModelScope.launch {
            expensesRepository.deleteSplitTransaction(splitId, parentId)
        }
    }
}