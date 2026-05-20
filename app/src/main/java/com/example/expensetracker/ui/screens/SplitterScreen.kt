package com.example.expensetracker.ui.screens

import android.R.attr.name
import android.R.attr.onClick
import android.widget.ToggleButton
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.expensetracker.data.Account
import com.example.expensetracker.data.Category
import com.example.expensetracker.data.SplitTransaction
import com.example.expensetracker.data.Transaction
import com.example.expensetracker.ui.ExpensesViewModel
import com.example.expensetracker.ui.Screens
import kotlinx.coroutines.coroutineScope
import kotlin.collections.get

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitterScreen(
    viewModel: ExpensesViewModel = viewModel(),
    navController: NavHostController,
    transactionId: String?
) {

    val allTransactions: List<Transaction> by viewModel.transactions.observeAsState(initial = emptyList())
    val allAccounts: List<Account> by viewModel.accounts.observeAsState(initial = emptyList())

    val transaction: Transaction? = remember(allTransactions, transactionId) {
        allTransactions.find { it.transaction_id == transactionId }
    }

    val account: Account? = remember(allAccounts, transaction) {
        allAccounts.find { it.account_id == transaction?.account_id }
    }

    val splitTransactions: List<SplitTransaction> by viewModel.getSplitTransactionsById(
        transactionId ?: ""
    ).observeAsState(initial = emptyList())

    val categoryMap: Map<Int, Category> by viewModel.categoryMap.observeAsState(initial = emptyMap())

    val categoriesList by viewModel.categories.observeAsState(initial = emptyList())

    // test data

    //val splitTransactions = listOf(
    //    SplitTransaction(1, transactionId ?: "", 5.0, "Split 1", false, null, null),
    //    SplitTransaction(2, transactionId ?: "", 10.0, "Split 2", false, null, null)
    //)


    var splitSum = splitTransactions.sumOf { it.amount }
    val amount = transaction?.amount ?: 0.0
    var unsplitAmount = amount - splitSum


    var showDialog by remember { mutableStateOf(false) }
    var selectedSplit by remember { mutableStateOf<SplitTransaction?>(null) }


    if (transaction == null || account == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Transaction not found", style = MaterialTheme.typography.bodyLarge)
        }
    } else {

        Scaffold(
            modifier = Modifier,
            topBar = {
                TopAppBar(
                    title = { Text(text = "Splitter") },
                    actions = {
                        IconButton(onClick = {
                            navController.navigate(Screens.Transactions.name)
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back Button"
                            )
                        }
                    }
                )
            }

        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize() //!!!!!!
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                )
                { //Main transaciton deets
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = transaction.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = transaction.merchant_name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                text = "£${"%.2f".format(amount)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black
                            )

                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = account.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = account.bankName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = transaction.date.toLocalDate().toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        val category = categoryMap[transaction.cat_id]?.cat_name ?: ""

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }


                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                )
                {

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Unsplit Amount",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )

                            Text(
                                text = "£${"%.2f".format(unsplitAmount)}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )

                        }

                    }

                    FloatingActionButton(
                        modifier = Modifier.padding(start = 16.dp),
                        onClick = {
                            if (splitSum < amount) { //Wont split if ther eis no more to split
                                selectedSplit = null
                                showDialog = true
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Split")
                    }

                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!transaction.is_split) { // Show button here that prompts to the scan receipt for autosplit

                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        onClick = { },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = "Back Button")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Scan Receipt",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }


                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {//split transactionss
                    items(items = splitTransactions) { st ->

                        val category = categoryMap[st.cat_id]?.cat_name ?: ""

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            onClick = {
                                selectedSplit = st
                                showDialog = true
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = st.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Text(
                                        text = category,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                }
                                Text(
                                    text = "£${"%.2f".format(st.amount)}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

            }

        }

        if (showDialog) {


            SplitEditor(
                split = selectedSplit,
                parentId = transaction?.transaction_id ?: "",
                defaultCategoryId = transaction?.cat_id,
                categories = categoriesList,
                unsplitAmount = unsplitAmount,
                onDismiss = { showDialog = false },
                onConfirm = {
                    viewModel.insertSplitTransaction(split = it)
                    showDialog = false
                },
                onDelete = {
                    viewModel.deleteSplitTransaction(it.split_id, it.parent_id)
                    showDialog = false
                }
            )
        }

    }

}





@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitEditor(
    split: SplitTransaction?,
    parentId: String,
    defaultCategoryId: Int?,
    categories: List<Category>,
    unsplitAmount: Double,
    onDismiss: () -> Unit,
    onConfirm: (SplitTransaction) -> Unit,
    onDelete: (SplitTransaction) -> Unit
) {
    // Dynamic initialization of fields depending on execution modes
    var nameInput by remember { mutableStateOf(split?.name ?: "") }
    var amountInput by remember { mutableStateOf(split?.amount?.toString() ?: "") }

    var isExcludedInput by remember { mutableStateOf(split?.is_excluded ?: false) }

    var dropdownExpanded by remember { mutableStateOf(false) }
    var selectedCategoryId by remember { mutableStateOf(split?.cat_id ?: defaultCategoryId) }
    var selectedCategoryLabel =
        categories.find { it.cat_id == selectedCategoryId }?.cat_name ?: ""

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (split == null) "Add Split" else "Edit Split") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Name") },
                        modifier = Modifier.weight(1f),
                    )

                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountAmount -> amountInput = amountAmount },
                        label = { Text("Amount") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedCategoryLabel,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Category") },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),

                            )

                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.cat_name) },
                                    onClick = {
                                        selectedCategoryId = category.cat_id
                                        dropdownExpanded = false
                                    }
                                )
                            }

                        }

                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "Excluded", style = MaterialTheme.typography.labelSmall)
                        Switch(
                            checked = isExcludedInput,
                            onCheckedChange = { isExcludedInput = it }
                        )
                    }
                }
            }
        },

        confirmButton = {
            Button(
                onClick = {
                    val parsedPriceValue = amountInput.toDoubleOrNull() ?: 0.0

                    if (parsedPriceValue <= unsplitAmount + (split?.amount ?: 0.0)) {

                        if (nameInput.isNotEmpty() && parsedPriceValue > 0.0 ) {

                            val updatedSplit = SplitTransaction(
                                split_id = split?.split_id ?: 0,
                                parent_id = parentId,
                                amount = parsedPriceValue,
                                name = nameInput,
                                is_excluded = isExcludedInput,
                                cat_id = selectedCategoryId
                            )


                            onConfirm(updatedSplit)

                        }
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // If we are editing an existing item, show the red delete button
                if (split != null) {
                    TextButton(
                        onClick = { onDelete(split) }
                    ) {
                        Text(
                            text = "Delete",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

