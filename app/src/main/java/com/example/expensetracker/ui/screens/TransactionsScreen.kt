package com.example.expensetracker.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReadMore
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.expensetracker.data.Account
import com.example.expensetracker.data.Category
import com.example.expensetracker.data.Transaction
import com.example.expensetracker.ui.ExpensesViewModel
import com.example.expensetracker.ui.Screens
import java.time.format.DateTimeFormatter
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: ExpensesViewModel = viewModel(),
    navController: NavHostController
) {

    var query by rememberSaveable { mutableStateOf("") }

    //var searchResults by rememberSaveable { mutableStateOf(emptyList<Transaction>()) }

    var selectedAccountId by rememberSaveable { mutableStateOf<String?>(null) }

    val allTransactions by viewModel.transactions.observeAsState(initial = emptyList())

    val allAccounts by viewModel.accounts.observeAsState(initial = emptyList())

    val accountMap = remember(allAccounts) { allAccounts.associateBy { it.account_id } }

    val categoryMap by viewModel.categoryMap.observeAsState(initial = emptyMap())

    val accountFilteredTransactions = if (selectedAccountId == null) {
        allTransactions
    } else {
        allTransactions.filter { it.account_id == selectedAccountId }
    }

    val filteredTransactions = accountFilteredTransactions.filter {
        val category = categoryMap[it.cat_id]?.cat_name ?: ""

        it.name.contains(query, ignoreCase = true)
                ||
                it.merchant_name.contains(query, ignoreCase = true)
                ||
                category.contains(query, ignoreCase = true)

    }



    Scaffold(
        modifier = Modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = "Transactions") },
                actions = {}
            )
        }

    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            TransactionSearchBar(
                query = query,
                onQueryChange = { query = it }
            )

            AccountFilterBar(
                accounts = allAccounts,
                selectedAccountId = selectedAccountId,
                onAccountSelected = { id -> selectedAccountId = id }
            )

            TransactionList(
                list = filteredTransactions,
                accountMap = accountMap,
                categoryMap = categoryMap,
                navController = navController,
                modifier = Modifier.weight(1f),
                viewModel = viewModel
            )

        }




    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountFilterBar(
    accounts: List<Account>,
    selectedAccountId: String?,
    onAccountSelected: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val currentSelectionLabel = if (selectedAccountId == null) {
        "All Accounts"
    } else {
        accounts.find { it.account_id == selectedAccountId }?.name ?: "Unknown Account"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = currentSelectionLabel,
                onValueChange = {},
                readOnly = true,
                label = { Text("Filter by Account") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("All Accounts") },
                    onClick = {
                        onAccountSelected(null)
                        expanded = false
                    }
                )

                accounts.forEach { account ->
                    DropdownMenuItem(
                        text = { Text(account.bankName + " " + account.name) },
                        onClick = {
                            onAccountSelected(account.account_id)
                            expanded = false
                        }
                    )
                }
            }
        }
    }

}




@Composable
fun TransactionSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        placeholder = { Text("Search transactions...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear text")
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.medium
    )
}


@Composable
fun TransactionList(
    list: List<Transaction>,
    accountMap: Map<String, Account>,
    categoryMap: Map<Int, Category>,
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: ExpensesViewModel = viewModel()
) {

    val groupedTransactions = remember(list) {
        list.sortedByDescending { it.date }
            .groupBy { it.date.toLocalDate() }
    }

    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.getDefault()) // eg: May 1, 2023
    }


    if (list.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "No transactions found", style = MaterialTheme.typography.bodyLarge)
        }

    } else {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            groupedTransactions.forEach { (date, transactionsForDate) ->
                item {
                    Text(
                        text = date.format(dateFormatter),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 4.dp)
                    )
                }

                items(transactionsForDate, key = { it.transaction_id }) { transaction ->
                    val associatedAccount = accountMap[transaction.account_id]
                    val bankLabel = associatedAccount?.bankName?.ifEmpty { "Bank" } ?: "Unknown Bank"
                    val accountNickname = associatedAccount?.name ?: "Account"

                    val matchedCategory = categoryMap[transaction.cat_id]?.cat_name  ?: "Uncategorised"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        onClick = {
                            if (transaction.amount > 0) navController.navigate(Screens.Splitter.name + "/${transaction.transaction_id}") },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),

                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {

                                    Row {
                                        Text(
                                            text = transaction.merchant_name.ifEmpty { transaction.name },
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )

                                        if (transaction.is_excluded) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Excluded Transaction Indicator",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                    }




                                    Text(
                                        text = matchedCategory,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                    )

                                    if (transaction.merchant_name.isNotEmpty() && transaction.name != transaction.merchant_name) {
                                        Text(
                                            text = transaction.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(bottom = 2.dp)
                                        )
                                    }

                                    Text(
                                        text = "$bankLabel • $accountNickname",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }




                                if (transaction.is_split) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ReadMore,
                                        contentDescription = "Split Transaction Indicator",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                val expense = transaction.amount > 0
                                val amount =
                                    if (!expense) -1 * transaction.amount else transaction.amount
                                val plus = if (expense) "" else "+"

                                Text(
                                    text = "$plus£${"%.2f".format(amount)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (expense) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

