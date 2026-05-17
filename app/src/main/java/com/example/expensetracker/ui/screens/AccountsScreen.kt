package com.example.expensetracker.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DomainAdd
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.expensetracker.data.Account
import com.example.expensetracker.ui.ExpensesViewModel
import com.example.expensetracker.ui.Screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    viewModel: ExpensesViewModel = viewModel(),
    navController: NavHostController,
    onAddAccountClick: () -> Unit
) {

    //val allAccounts = listOf(
        //Account(account_id = "1", available_balance = 100.0, current_balance = 100.0, currency_code = "GBP", name = "Checking", type = "Checking", mask = "1234"),
        //Account(account_id = "2", available_balance = 123.0, current_balance = 125.0, currency_code = "GBP", name = "Savings", type = "Savings", mask = "1235")
    //)
    //val netWorth = 225

    val accounts by viewModel.accounts.observeAsState(initial = emptyList())
    val netWorth by viewModel.derivedNetWorth.observeAsState(initial = 0.0)

    // here we should also pull some aggregate data of net worth over previous months, to form a line chart

    fun addOfflineAccount() {}

    Scaffold(
        modifier = Modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = "Accounts") },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screens.Home.name)
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back Button")
                    }
                }
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
            Box {
                Text(text = "Hello from Accounts")
            }//Title

            Box { //Show total net worth here?

            }

            Row {
                FloatingActionButton(//Add account button online
                    onClick = onAddAccountClick
                ) {
                    Icon(Icons.Filled.DomainAdd, contentDescription = "Add Bank Account")
                }
                Box {}//Add account button offline
            }// Add acounts section

            Button(//Add account button onffline
                onClick = { addOfflineAccount() }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Offline Account")
            }

            LazyColumn(modifier = Modifier.padding()) // List of accounts
            {
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                        Text(text = "Net Worth")
                        Text(text = "£$netWorth")
                    }
                }

                items(accounts.size) { index ->
                    AccountCard(account = accounts[index])
                }


            }

        }
    }


}


@Composable
fun AccountCard(account: Account) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = account.name, style = MaterialTheme.typography.titleLarge)
            Text(text = account.bankName)
            Text(text = account.type)
            Text(text = "Balance: ${account.currency_code} ${account.current_balance}")
            Text(text = "**** ${account.mask}", style = MaterialTheme.typography.bodySmall)
        }
    }

}