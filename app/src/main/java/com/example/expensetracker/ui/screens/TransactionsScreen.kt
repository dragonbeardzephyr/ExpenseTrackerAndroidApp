package com.example.expensetracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.expensetracker.data.Transaction
import com.example.expensetracker.ui.ExpensesViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: ExpensesViewModel = viewModel(),
    navController: NavHostController
) {

    var query by rememberSaveable { mutableStateOf("") }
    var expanded = false

    //var searchResults by rememberSaveable { mutableStateOf(emptyList<Transaction>()) }

    val allTransactions = listOf(
        Transaction(transaction_id = "1", account_id = "3", transaction_code = "1234", date = "2023-04-01", name = "Groceries", amount = 50.0, merchant_name = "Target", is_excluded = false, cat_primary = "Food", cat_detailed = "Groceries", pending = false, is_split = false),
        Transaction(transaction_id = "2", account_id = "3", transaction_code = "1234", date = "2023-04-05", name = "Subscription", amount = 50.0, merchant_name = "Netlfix", is_excluded = false, cat_primary = "Entertainment", cat_detailed = "Media", pending = false, is_split = false)
    )

    val filteredTransactions = allTransactions.filter {
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
                onQueryChange = { query = it},
                expanded = expanded,
                onExpandedChange = { },
                searchResults = emptyList()
            )

            TransactionList(
                list = filteredTransactions,
                onResultClick = { }
            )

        }




    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    searchResults: List<Transaction>,
    modifier: Modifier = Modifier,

) {
    // Track expanded state of search bar

    SearchBar(
        modifier = Modifier
            .fillMaxWidth(),
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = { onExpandedChange(false) },
                expanded = expanded,
                onExpandedChange = onExpandedChange,
                placeholder = { Text("Search transactions...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )
        },
        expanded = expanded,
        onExpandedChange = onExpandedChange,
    ) { }

}


@Composable
fun TransactionList(
    list: List<Transaction>,
    onResultClick: (String) -> Unit
) {
    LazyColumn {
        items(count = list.size) { index ->
            val transaction = list[index]
            ListItem(
                headlineContent = { Text(transaction.name)},
                supportingContent = { Text(transaction.amount.toString())},
                leadingContent = { Text(transaction.merchant_name)},
                modifier = Modifier.clickable { onResultClick(transaction.transaction_id) }
            )
        }
    }
}