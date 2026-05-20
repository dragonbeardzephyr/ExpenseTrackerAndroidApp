package com.example.expensetracker.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MultilineChart
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.expensetracker.ui.screens.*


enum class Screens(
    val label: String,
    val icon: ImageVector,
) {
    Home("Home", Icons.Default.Home),
    Accounts("Accounts", Icons.Default.AccountBalance),
    Transactions("Transactions", Icons.AutoMirrored.Filled.ViewList),
    Splitter("Split", Icons.Default.Add),
    Expenses("Budget", Icons.AutoMirrored.Filled.MultilineChart),
    Settings("Settings", Icons.Default.Settings)
}

@Composable
fun ExpensesApp(onAddAccountClick: () -> Unit) {

    val viewModel: ExpensesViewModel = viewModel()
    val navController: NavHostController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = Modifier,
        bottomBar = {
            NavigationBar {
                val navBarItems = listOf(Screens.Home, Screens.Transactions, Screens.Expenses)

                navBarItems.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.name,
                        onClick = {
                            navController.navigate(screen.name) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.label
                            )
                        },
                        label = { Text(screen.label) }
                    )
                }
            }
        })
    { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screens.Home.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = Screens.Home.name) {
                HomeScreen(viewModel, navController)
            }
            composable(route = Screens.Accounts.name) {
                AccountsScreen(viewModel, navController, onAddAccountClick)
            }
            composable(route = Screens.Transactions.name) {
                TransactionsScreen(viewModel, navController)
            }
            composable(route = Screens.Splitter.name + "/{transactionId}") { backStackEntry ->
                val transactionId = backStackEntry.arguments?.getString("transactionId")
                SplitterScreen(viewModel, navController, transactionId)
            }
            composable(route = Screens.Expenses.name) {
                ExpensesScreen(viewModel, navController)
            }
            composable(route = Screens.Settings.name) {
                SettingsScreen(viewModel, navController)
            }
        }
    }
}