package com.example.expensetracker.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.* // Pulls in Home, List, etc.
import androidx.compose.material3.* // Pulls in NavigationBar, NavigationBarItem, Icon, Text, Scaffold
import androidx.compose.runtime.* // REQUIRED for 'by' and 'getValue' logic
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
    Transactions("History", Icons.Default.List),
    Splitter("Split", Icons.Default.CallSplit),
    Expenses("Budget", Icons.Default.PieChart),
    Settings("Settings", Icons.Default.Settings)
}

@Composable
fun ExpensesApp() {

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
                                contentDescription = null
                            )
                        },
                        label = { Text(screen.label) }
                    )
                }
            }
        }) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screens.Home.name,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(route = Screens.Home.name) {
                    HomeScreen(viewModel, navController)
                }
                composable(route = Screens.Accounts.name) {
                    AccountsScreen(viewModel, navController)
                }
                composable(route = Screens.Transactions.name) {
                    TransactionsScreen(viewModel, navController)
                }
                composable(route = Screens.Splitter.name) {
                    SplitterScreen(viewModel, navController)
                }
                composable(route = Screens.Expenses.name) {
                    // You called it Spending/Budget - create this file in ui/screens
                    ExpensesScreen(viewModel, navController)
                }
                composable(route = Screens.Settings.name) {
                    SettingsScreen(viewModel, navController)
                }

            }

        }

}

