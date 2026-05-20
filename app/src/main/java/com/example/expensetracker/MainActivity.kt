package com.example.expensetracker

import android.content.BroadcastReceiver
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.expensetracker.ui.ExpensesApp
import com.example.expensetracker.ui.ExpensesViewModel
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import com.plaid.link.FastOpenPlaidLink
import com.plaid.link.result.LinkSuccess
import com.plaid.link.result.LinkExit
import com.plaid.link.configuration.LinkTokenConfiguration
import com.plaid.link.Plaid

class MainActivity: ComponentActivity() {

    private val viewModel: ExpensesViewModel by viewModels()


    private val linkAccountToPlaid = registerForActivityResult(FastOpenPlaidLink()) { result ->
        when (result) {
            is LinkSuccess -> {
                val publicToken = result.publicToken
                viewModel.exchangePublicToken(publicToken)
            }
            is LinkExit -> {
                Log.e("Plaid", "User exited or error: ${result.error?.displayMessage}")
            }
        }
    }

    fun openPlaidLink() {
        viewModel.getLinkToken { linkToken ->
            val configuration = LinkTokenConfiguration.Builder()
                .token(linkToken)
                .build()
            val handler = Plaid.create(application, configuration)
            linkAccountToPlaid.launch(handler) // This launches the bank login screen
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExpenseTrackerTheme {
                ExpensesApp(onAddAccountClick = { openPlaidLink() })
            }
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }


}