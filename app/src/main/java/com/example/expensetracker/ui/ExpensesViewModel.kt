package com.example.expensetracker.ui
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.*
import com.example.expensetracker.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.time.YearMonth

class ExpensesViewModel(application: Application) : AndroidViewModel(application) {

    private val expensesRepository: ExpensesRepository
    private val preferenceManager = ExpensePreferenceManager(application)

    init {
        val expensesDao = ExpensesDatabase.getDatabase(application).expensesDao()
        expensesRepository = ExpensesRepository(expensesDao)

        viewModelScope.launch {
            if (expensesRepository.getCategoryCount() == 0) {
                val basePresets = listOf(
                    Category(
                        cat_id = 1,
                        cat_name = "Groceries",
                        cat_type = "general",
                        cat_plaid = "FOOD_AND_DRINK_GROCERIES"
                    ),
                    Category(
                        cat_id = 2,
                        cat_name = "Eating Out",
                        cat_type = "general",
                        cat_plaid = "FOOD_AND_DRINK_RESTAURANT"
                    ),
                    Category(
                        cat_id = 3,
                        cat_name = "Bills & Utilities",
                        cat_type = "general",
                        cat_plaid = "BILLS_AND_UTILITIES"
                    ),
                    Category(
                        cat_id = 4,
                        cat_name = "Entertainment",
                        cat_type = "general",
                        cat_plaid = "ENTERTAINMENT"
                    ),
                    Category(
                        cat_id = 5,
                        cat_name = "Transport",
                        cat_type = "general",
                        cat_plaid = "TRAVEL_TRANSPORTATION"
                    ),
                    Category(
                        cat_id = 6,
                        cat_name = "Shopping",
                        cat_type = "general",
                        cat_plaid = "TRANSFER_DEPOSIT_SHOPPING"
                    ),
                    Category(
                        cat_id = 7,
                        cat_name = "Other",
                        cat_type = "general",
                        cat_plaid = "OTHER"
                    ),
                )
                basePresets.forEach { insertCategory(it) }
            }
        }

        checkAndSync()

    }

    val accounts: LiveData<List<Account>> = expensesRepository.getAccounts()
    val transactions: LiveData<List<Transaction>> = expensesRepository.getAllTransactions()

    val allSplitTransactions: LiveData<List<SplitTransaction>> = expensesRepository.getAllSplitTransactions()

    val derivedNetWorth: LiveData<Double> = accounts.map { accountList ->
        accountList.sumOf { it.current_balance }
    }

    val categories: LiveData<List<Category>> = expensesRepository.getAllCategories()

    val categoryMap: LiveData<Map<Int, Category>> = categories.map { list ->
        list.associateBy { it.cat_id }
    }

    private val _selectedMonthLive = MutableLiveData(YearMonth.now().toString())

    var selectedMonth by mutableStateOf(java.time.YearMonth.now().toString())
        private set // debugged liek this, idk what this mean :(

    val budgetsForMonth: LiveData<List<Budget>> = _selectedMonthLive.switchMap {
        expensesRepository.getBudgetsForMonth(selectedMonth)
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
                    client_id = BuildConfig.CLIENT_ID,
                    secret = BuildConfig.SECRET,
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
                val user = LinkTokenUser(
                    clientUserId,
                    "legal name",
                    "447467983412",
                    "email@address.com"
                )
                val request = LinkTokenRequest(
                    BuildConfig.CLIENT_ID, BuildConfig.SECRET, user, "Expense Tracker",
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

                val itemRequest = ItemGetRequest(
                    client_id = BuildConfig.CLIENT_ID,
                    secret = BuildConfig.SECRET,
                    access_token = token
                )

                val itemResponse = plaidApi.getItemDetails(itemRequest)

                val institutionId = itemResponse.item.institution_id

                var bankName = "Bank"
                if (!institutionId.isNullOrEmpty()) {
                    try {
                        val instRequest = InstitutionGetByIdRequest(
                            client_id = BuildConfig.CLIENT_ID,
                            secret = BuildConfig.SECRET,
                            institution_id = institutionId,
                            country_codes = listOf("GB")
                        )
                        val instResponse = plaidApi.getInstitutionById(instRequest)
                        bankName = instResponse.institution.name
                    } catch (e: Exception) {
                        Log.e("PlaidError", "Failed to resolve institution display name: ${e.message}")
                    }
                }

                val request = AccountsGetRequest(
                    client_id = BuildConfig.CLIENT_ID,
                    secret = BuildConfig.SECRET,
                    access_token = token
                )

                val response = plaidApi.getAccounts(request)

                withContext(Dispatchers.IO) {
                    response.accounts.forEach { plaidAccount ->
                        // Check if this account already exists in the database
                        val savedAccount =
                            expensesRepository.getAccountByIdStatic(plaidAccount.account_id)


                        val resolvedBankName =
                            if (savedAccount != null && savedAccount.bankName != "Bank") {
                                // SAFE GUARD: If a valid bank name is already there, KEEP IT! Sync won't overwrite it.
                                savedAccount.bankName
                            } else {
                                bankName
                            }

                        val dbAccount = Account(
                            account_id = plaidAccount.account_id,
                            available_balance = plaidAccount.balances.available ?: 0.0,
                            current_balance = plaidAccount.balances.current,
                            currency_code = plaidAccount.balances.iso_currency_code ?: "GBP",
                            name = plaidAccount.name,
                            type = plaidAccount.type,
                            mask = plaidAccount.mask ?: "",
                            bankName = resolvedBankName
                        )
                        expensesRepository.insertAccount(dbAccount)
                    }

                    // Calculate historical net worth from your static table states
                    val updatedAccountsList = expensesRepository.getAccountsStatic()
                    val databaseNetWorthCalculated =
                        updatedAccountsList.sumOf { it.current_balance }

                    expensesRepository.insertNetWorth(
                        NetWorth(
                            date = LocalDate.now(),
                            amount = databaseNetWorthCalculated
                        )
                    )
                }

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
                    val request = TransactionsSyncRequest(
                        BuildConfig.CLIENT_ID,
                        BuildConfig.SECRET,
                        token,
                        cursor
                    )
                    val response = plaidApi.syncTransactions(request)
                    withContext(Dispatchers.IO) {
                        response.added.forEach { plaidTransaction ->
                            Log.i("PlaidTransactionSync", "Added transaction: $plaidTransaction")

                            val detailedStr =
                                plaidTransaction.personal_finance_category?.detailed ?: ""
                            val primaryStr =
                                plaidTransaction.personal_finance_category?.primary ?: ""

                            var resolvedCategory =
                                expensesRepository.getCategoryByPlaid(detailedStr)

                            if (resolvedCategory == null) {
                                resolvedCategory =
                                    expensesRepository.getCategoryByPlaid(primaryStr)
                            }

                            var targetCatId: Int? = resolvedCategory?.cat_id

                            if (resolvedCategory == null) {

                                val plaidCategory = detailedStr.ifEmpty { primaryStr }

                                val cleanPlaidCat =
                                    if (detailedStr.isNotEmpty() && primaryStr.isNotEmpty() && detailedStr.startsWith(
                                            primaryStr
                                        )
                                    ) {
                                        detailedStr.removePrefix("${primaryStr}_")
                                    } else {
                                        plaidCategory
                                    }

                                if (cleanPlaidCat.isEmpty() ) {
                                    targetCatId = 7
                                } else {
                                    val humanReadable = cleanPlaidCat
                                        .replace("_", " ")
                                        .lowercase()
                                        .split(" ")
                                        .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }


                                    resolvedCategory = Category(
                                        cat_id = 0, // For the auto increment
                                        cat_name = humanReadable,
                                        cat_type = "general",
                                        cat_plaid = plaidCategory,
                                        is_excluded = false
                                    )

                                    val newCatId = expensesRepository.insertCategory(resolvedCategory)
                                    targetCatId = newCatId
                                }
                            }

                            val dbTransaction = Transaction(
                                transaction_id = plaidTransaction.transaction_id,
                                account_id = plaidTransaction.account_id,
                                amount = plaidTransaction.amount,
                                transaction_code = plaidTransaction.payment_meta?.reference_number
                                    ?: "",
                                date = LocalDate.parse(plaidTransaction.date).atStartOfDay(),
                                merchant_name = plaidTransaction.merchant_name ?: "",
                                name = plaidTransaction.name,
                                is_excluded = false,
                                cat_id = targetCatId,
                                pending = plaidTransaction.pending,
                                is_split = false
                            )
                            expensesRepository.insertTransaction(dbTransaction)
                        }

                        response.modified.forEach { plaidTransaction ->
                            Log.i("PlaidTransactionSync", "Modified transaction: $plaidTransaction")

                            val oldTransaction = expensesRepository.getTransactionByIdStatic(plaidTransaction.transaction_id)

                            val detailedStr =
                                plaidTransaction.personal_finance_category?.detailed ?: ""
                            val primaryStr =
                                plaidTransaction.personal_finance_category?.primary ?: ""

                            var resolvedCategory =
                                expensesRepository.getCategoryByPlaid(detailedStr)

                            if (resolvedCategory == null) {
                                resolvedCategory =
                                    expensesRepository.getCategoryByPlaid(primaryStr)
                            }

                            var targetCatId: Int? = resolvedCategory?.cat_id

                            if (resolvedCategory == null) {

                                val plaidCategory = detailedStr.ifEmpty { primaryStr }

                                val cleanPlaidCat =
                                    if (detailedStr.isNotEmpty() && primaryStr.isNotEmpty() && detailedStr.startsWith(
                                            primaryStr
                                        )
                                    ) {
                                        detailedStr.removePrefix("${primaryStr}_")
                                    } else {
                                        plaidCategory
                                    }

                                if (cleanPlaidCat.isEmpty() ) {
                                    targetCatId = 7
                                } else {
                                    val humanReadable = cleanPlaidCat
                                        .replace("_", " ")
                                        .lowercase()
                                        .split(" ")
                                        .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }


                                    resolvedCategory = Category(
                                        cat_id = 0, // For the auto increment
                                        cat_name = humanReadable,
                                        cat_type = "general",
                                        cat_plaid = plaidCategory,
                                        is_excluded = false
                                    )

                                    val newCatId = expensesRepository.insertCategory(resolvedCategory)
                                    targetCatId = newCatId
                                }
                            }

                            val dbTransaction = Transaction(
                                transaction_id = plaidTransaction.transaction_id,
                                account_id = plaidTransaction.account_id,
                                amount = plaidTransaction.amount,
                                transaction_code = plaidTransaction.payment_meta?.reference_number
                                    ?: "",
                                date = LocalDate.parse(plaidTransaction.date).atStartOfDay(),
                                merchant_name = plaidTransaction.merchant_name ?: "",
                                name = plaidTransaction.name,
                                is_excluded = oldTransaction?.is_excluded ?: false,
                                cat_id = targetCatId,
                                pending = plaidTransaction.pending,
                                is_split = oldTransaction?.is_split ?: false
                            )
                            expensesRepository.insertTransaction(dbTransaction)
                        }
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


    fun insertCategory(category: Category) {
        viewModelScope.launch {
            expensesRepository.insertCategory(category)
        }
    }


    fun insertBudget(targetBudget: Budget) {
        viewModelScope.launch {
            expensesRepository.insertBudget(targetBudget)
        }
    }

    fun removeBudget(budgetId: Int) {
        viewModelScope.launch {
            expensesRepository.deleteBudget(budgetId)
        }
    }

    fun changeSelectedMonth(newMonth: String) {
        selectedMonth = newMonth
        _selectedMonthLive.value = newMonth
    }


}
