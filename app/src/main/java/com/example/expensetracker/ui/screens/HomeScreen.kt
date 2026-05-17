package com.example.expensetracker.ui.screens

import android.R.attr.text
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.expensetracker.data.Account
import com.example.expensetracker.data.NetWorth
import com.example.expensetracker.ui.ExpensesViewModel
import com.example.expensetracker.ui.Screens


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ExpensesViewModel = viewModel(),
    navController: NavHostController
) {


    val allAccounts by viewModel.accounts.observeAsState(initial = emptyList())
    //val netWorth by viewModel.netWorth.observeAsState(initial = null)
    val totalBalance by viewModel.derivedNetWorth.observeAsState(initial = 0.0)



    Scaffold(
        modifier = Modifier,
        topBar = {
            TopAppBar(

                title = { Text(text = "Home") },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screens.Settings.name)
                    }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
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

        ) { // Accounts

            Row (
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Accounts")
                IconButton(
                    onClick = {
                        navController.navigate(Screens.Accounts.name)
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Accounts")
                }
            }

            AccountsList(allAccounts, totalBalance)

            Text("Monthly Stats")
            Row {
                Box { //Spending  Box
                    Text(text = "Spending")
                }
                Box { // Budget Box
                    Text(text = "Budget")
                }
            }

            Row {
                Box { // ?? Box
                    Text(text = "Unknown")
                }
                Box { // ?? Box
                    Text(text = "Unknown")
                }
            }

        }
    }
}



@Composable
fun AccountsList(
    accounts:List<Account>,
    netWorth: Double
) {
    LazyRow {

        item {
            Card {
                Text(text = netWorth.toString() ?: "0.0")
                Text(text = "Net Worth")
            }
        }

        items(accounts.size) { index ->
            AccountBox(accounts[index])
        }

    }
}


@Composable
fun AccountBox(account: Account) {
    Card {
        Text(text = "£" + account.current_balance.toString())
        Text(text = account.name)
        Text(text = account.bankName)
    }
}