package com.example.expensetracker.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    viewModel: ExpensesViewModel = viewModel(),
    navController: NavHostController
) {

    val allAccounts = listOf(
        Account(account_id = "1", available_balance = 100.0, current_balance = 100.0, currency_code = "GBP", name = "Checking", type = "Checking", mask = "1234"),
        Account(account_id = "2", available_balance = 123.0, current_balance = 125.0, currency_code = "GBP", name = "Savings", type = "Savings", mask = "1235")
    )
    val netWorth = 225

    // here we should also pull some aggregate data of net worth over previous months, to form a line chart


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
            Box() {
                Text(text = "Hello from Accounts")
            }//Title

            Box() { //Show total net worth here?

            }

            Row() {
                Box() {}//Add account button online
                Box() {}//Add account button offline
            }// Add acounts section

            Box() {}//Subtitle

            LazyColumn() // List of accounts
            {
                item() {
                    Row() {

                        Text(text = "Net Worth")
                        Text(text = "£$netWorth")
                    }
                }

                items(allAccounts.size) { index ->
                    AccountCard(account = allAccounts[index])
                }

            }

        }
    }


}


@Composable
fun AccountCard(account: Account) {
    Row() {

        Text(text = account.name)

        Text(text = "£" + account.current_balance.toString())
    }

}