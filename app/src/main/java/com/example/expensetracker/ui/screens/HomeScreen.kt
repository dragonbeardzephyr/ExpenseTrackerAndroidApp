package com.example.expensetracker.ui.screens

import android.R.attr.text
import android.content.Context
import android.net.ConnectivityManager
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReadMore
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.expensetracker.data.Account
import com.example.expensetracker.data.NetWorth
import com.example.expensetracker.ui.NetworkBroadcastReceiver
import com.example.expensetracker.ui.ExpensesViewModel
import com.example.expensetracker.ui.Screens
import java.time.YearMonth
import kotlin.text.ifEmpty


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ExpensesViewModel = viewModel(),
    navController: NavHostController
) {

    val context = LocalContext.current

    @Suppress("DEPRECATION")
    NetworkBroadcastReceiver(systemAction = ConnectivityManager.CONNECTIVITY_ACTION) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        val isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting

        viewModel.updateNetworkStatus(isConnected)
    }


    val allAccounts by viewModel.accounts.observeAsState(initial = emptyList())
    //val netWorth by viewModel.netWorth.observeAsState(initial = null)
    val totalBalance by viewModel.derivedNetWorth.observeAsState(initial = 0.0)

    val transactions by viewModel.transactions.observeAsState(initial = emptyList())

    val thisMonthsTransactions = transactions.filter { transaction ->
        transaction.date.month == YearMonth.now().month
    }

    val recentTransactions = remember(transactions) {
        transactions.sortedByDescending { it.date }.take(3)
    }

    val accountMap = remember(allAccounts) { allAccounts.associateBy { it.account_id } }

    val categoryMap by viewModel.categoryMap.observeAsState(initial = emptyMap())

    val spending = thisMonthsTransactions.sumOf { it.amount }

    val budgets by viewModel.budgetsForMonth.observeAsState(initial = emptyList())

    val totalBudget = budgets.sumOf { it.limit }


    Scaffold(
        modifier = Modifier,
        topBar = {
            TopAppBar(

                title = {
                    Row() {
                        Text(text = "Home")

                        if (!viewModel.deviceOnline) {
                            Text(
                                text = "Offline",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFF3B30)
                            )
                        }
                    }
                        },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.Start

        ) { // Accounts

            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Accounts", style = MaterialTheme.typography.titleLarge)
                IconButton(
                    onClick = {
                        navController.navigate(Screens.Accounts.name)
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Accounts")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            AccountsList(allAccounts, totalBalance)

            Spacer(modifier = Modifier.height(24.dp))

            Text("Monthly Stats", style = MaterialTheme.typography.titleLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) { //Spending  Box
                    Text(text = "Spending")
                    Text(text = "£${"%.2f".format(spending)}")

                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) { // Budget Box


                    Text(text = "Budget")

                    Text(text = "£${"%.2f".format(totalBudget)}")
                }

            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Recent Transactions")


            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                recentTransactions.forEachIndexed { index, transaction ->
                    val associatedAccount = accountMap[transaction.account_id]
                    val bankLabel = associatedAccount?.bankName?.ifEmpty { "Bank" } ?: "Unknown Bank"
                    val accountNickname = associatedAccount?.name ?: "Account"

                    val matchedCategory = categoryMap[transaction.cat_id]?.cat_name  ?: "Uncategorised"


                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),

                        )
                    {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = transaction.merchant_name.ifEmpty { transaction.name },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        textDecoration = if (transaction.is_excluded) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
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

        if (viewModel.showOfflineAlert) {
            AlertDialog(
                onDismissRequest = { viewModel.showOfflineAlert = false },
                title = { Text(text = "Connection Offline") },
                text = { Text(text = "Unable to connect to real time bank data. Remote synchronization is paused, but you can continue using your local data safely.") },
                confirmButton = {
                    Button(onClick = { viewModel.showOfflineAlert = false }) {
                        Text(text = "Got it")
                    }
                }
            )
        }

    }
}



@Composable
fun AccountsList(
    accounts:List<Account>,
    netWorth: Double
) {

    Row {
        ElevatedCard(
            modifier = Modifier
                .width(90.dp)
                .height(70.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "£${"%.2f".format(netWorth)}")
                Text(text = "")
                Text(text = "Net Worth")
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {

            items(accounts.size) { index ->
                AccountBox(accounts[index])
            }

        }
    }
}


@Composable
fun AccountBox(account: Account) {
    ElevatedCard(
        modifier = Modifier
            .width(90.dp)
            .height(70.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "£${"%.2f".format(account.current_balance)}")
            Text(text = account.name)
            Text(text = account.bankName)
        }
    }
}