package com.example.expensetracker.ui.screens

import android.R.attr.fontWeight
import android.R.attr.label
import android.R.attr.onClick
import android.R.attr.text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReadMore
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ReadMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.expensetracker.data.Account
import com.example.expensetracker.data.Transaction
import com.example.expensetracker.ui.ExpensesViewModel
import com.example.expensetracker.ui.Screens
import kotlinx.coroutines.selects.select
import java.text.NumberFormat
import java.time.LocalDateTime
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

    val accountFilteredTransactions = if (selectedAccountId == null) {
        allTransactions
    } else {
        allTransactions.filter { it.account_id == selectedAccountId }
    }

    val filteredTransactions = accountFilteredTransactions.filter {
        it.name.contains(query, ignoreCase = true)
                ||
                it.merchant_name.contains(query, ignoreCase = true)
                //||
                //it.cat_primary.contains(query, ignoreCase = true)
                //||
                //it.cat_detailed.contains(query, ignoreCase = true)
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
                navController = navController
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

    // Determine the text to display on the closed dropdown box
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
                readOnly = true, // Prevents typing; forces dropdown selection
                label = { Text("Filter by Account") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(), // Material 3 anchoring modifier
                shape = MaterialTheme.shapes.medium
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                // Option 1: Clear the filter (Show All)
                DropdownMenuItem(
                    text = { Text("All Accounts") },
                    onClick = {
                        onAccountSelected(null)
                        expanded = false
                    }
                )

                // Option 2: Dynamic list of linked accounts
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
    navController: NavHostController
) {

    val groupedTransactions = remember(list) {
        list.sortedByDescending { it.date }
            .groupBy { it.date.toLocalDate() }
    }

    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.getDefault())
    }

    if (list.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "No transactions found", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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

                items(transactionsForDate) { transaction ->
                    val associatedAccount = accountMap[transaction.account_id]
                    val bankLabel = associatedAccount?.bankName?.ifEmpty { "Bank" } ?: "Unknown Bank"
                    val accountNickname = associatedAccount?.name ?: "Account"

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

                                Text(
                                    text = transaction.merchant_name.ifEmpty { transaction.name },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )


                                Text(
                                    text = transaction.cat_primary ?: "",
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
                            val amount = if (!expense) -1 * transaction.amount else transaction.amount
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

