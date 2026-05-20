package com.example.expensetracker.ui.screens

import android.R.attr.angle
import android.R.attr.category
import android.R.attr.mode
import android.R.attr.name
import android.R.attr.onClick
import android.R.attr.text
import android.service.autofill.Validators.or
import android.text.TextUtils.split
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.modifier.modifierLocalOf
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.expensetracker.data.Budget
import com.example.expensetracker.data.Category
import com.example.expensetracker.ui.ExpensesViewModel
import com.example.expensetracker.ui.Screens
import kotlin.collections.listOf
import kotlin.collections.map
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.input.KeyboardType
import com.example.expensetracker.data.SplitTransaction
import java.time.format.DateTimeFormatter


val colours = listOf(
    Color(0xFF8346EF), Color(0xFF00D2BE), Color(0xFFF19002),
    Color(0xFFFFE600), Color(0xFF4BB9EA), Color(0xFF1EB224),
    Color(0xFF1EB224), Color(0xFF1EB224), Color(0xFF1EB224)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    viewModel: ExpensesViewModel = viewModel(),
    navController: NavHostController
) {
    var selectedView by remember { mutableStateOf(1) }

    var showAddBudgetDialog by remember { mutableStateOf(false) }

    var selectedBudget by remember { mutableStateOf<Budget?>(null) }

    //var selectedMonth by remember { mutableStateOf(java.time.YearMonth.now().toString()) }

    var monthMenuExpanded by remember { mutableStateOf(false) }

    val allTransactions by viewModel.transactions.observeAsState(emptyList())
    val categories by viewModel.categories.observeAsState(emptyList())
    val currentBudgets by viewModel.budgetsForMonth.observeAsState(emptyList())

    val allSplitTransactions by viewModel.allSplitTransactions.observeAsState(emptyList())

    val availableMonthsList = remember(allTransactions) {
        val currentCalendarMonth = java.time.YearMonth.now().toString()
        val structuralSet = allTransactions.map { transaction ->
            transaction.date.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        }.toMutableSet()

        structuralSet.add(currentCalendarMonth)
        structuralSet.toList().sortedDescending()
    }

    val filteredTransactionsForMonth = remember(allTransactions, viewModel.selectedMonth) {
        allTransactions.filter { transaction ->
            transaction.date.format(DateTimeFormatter.ofPattern("yyyy-MM")) == viewModel.selectedMonth
        }
    }


    val filteredBudgetsForMonth = remember(currentBudgets, viewModel.selectedMonth) {
        currentBudgets.filter { it.month == viewModel.selectedMonth }
    }


    val spendingMap: Map<Int, Double> = remember(filteredTransactionsForMonth, allSplitTransactions) {
        val calculatedMap = mutableMapOf<Int, Double>()

        filteredTransactionsForMonth.forEach { parentTransaction ->
            if (!parentTransaction.is_excluded) {
                var parentTotal = parentTransaction.amount

                if (parentTransaction.is_split) {
                    val splitTransactions =
                        allSplitTransactions.filter { it.parent_id == parentTransaction.transaction_id }

                    splitTransactions.forEach { splitTransaction ->
                        if (!splitTransaction.is_excluded) {

                            calculatedMap[splitTransaction.cat_id ?: 0] =
                                calculatedMap.getOrDefault(
                                    splitTransaction.cat_id ?: 0, 0.0
                                ) + splitTransaction.amount

                            parentTotal -= splitTransaction.amount
                        }
                    }

                }

                calculatedMap[parentTransaction.cat_id ?: 0] =
                    calculatedMap.getOrDefault(parentTransaction.cat_id ?: 0, 0.0) + parentTotal
            }
        }
        calculatedMap
    }

    val unbudgetedCategories = remember(categories, filteredBudgetsForMonth) {
        categories.filter { cat ->
            currentBudgets.none { budget -> budget.cat_id == cat.cat_id }
        }
    }


    Scaffold (
        modifier = Modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = "Spending/Budget") },
                actions = {
                    Box() {
                        Row(
                            modifier = Modifier
                                .clickable { monthMenuExpanded = true }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select Month")
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(
                                text = viewModel.selectedMonth,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        DropdownMenu(
                            expanded = monthMenuExpanded,
                            onDismissRequest = {monthMenuExpanded = false}
                        ) {
                            availableMonthsList.forEach { monthString ->
                                DropdownMenuItem(
                                    text = { Text(text = monthString) },
                                    onClick = {
                                        viewModel.changeSelectedMonth(monthString)
                                        monthMenuExpanded = false
                                    }
                                )
                            }
                        }

                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedView == 1 && unbudgetedCategories.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
                        selectedBudget = null
                        showAddBudgetDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.primary
                    ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Budget")
                }
            }
        }

    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            SingleChoiceSegmentedButton(
                options = listOf("Spending", "Budget"),
                selectedIndex = selectedView,
                onSelectionChanged = { selectedView = it }
            )

            when (selectedView) {
                0 -> {
                    if (allTransactions.isEmpty()) {
                        Text(text = "No Transactions")
                    } else {
                        SpendingView(
                            modifier = Modifier.weight(1f),
                            categories = categories,
                            spendingMap = spendingMap
                        )
                    }
                }

                1 -> {
                    if (currentBudgets.isEmpty()) {
                        Text(text = "No Budgets set")
                    } else {
                        BudgetView(
                            modifier=Modifier.weight(1f),
                            categories = categories,
                            budgets = currentBudgets,
                            spendingMap = spendingMap,
                            onBudgetClick = {
                                selectedBudget = it
                                showAddBudgetDialog = true

                            }
                        )
                    }
                }
            }

        }

        if (showAddBudgetDialog && unbudgetedCategories.isNotEmpty()) {

            val availableCategoriesForDialog = remember(unbudgetedCategories, selectedBudget, categories) {
                if (selectedBudget != null) {
                    val matchingCat = categories.find { it.cat_id == selectedBudget?.cat_id }
                    if (matchingCat != null) listOf(matchingCat) + unbudgetedCategories else unbudgetedCategories
                } else {
                    unbudgetedCategories
                }
            }

            AddBudgetDialog(
                budget = selectedBudget,
                onDismiss = { showAddBudgetDialog = false },
                unbudgetedCategories = availableCategoriesForDialog, // unbudgeted plus current category
                onConfirm = { updatedBudget ->
                    viewModel.insertBudget(updatedBudget)
                    showAddBudgetDialog = false
                    },
                onDelete = { budgetToDelete ->
                    viewModel.removeBudget(budgetToDelete.budget_id)
                    showAddBudgetDialog = false}
                //showAddBudgetDialog = showAddBudgetDialog
            )
        }

    }

}


@Composable
fun CategoryList(
    mode: Int,
    categories: List<Category>,
    spendingMap: Map<Int, Double>,
    budgets: List<Budget> = emptyList(),
    categoryColourMap: Map<Int, Color> = emptyMap(),
    onItemClick: (Category) -> Unit = {}
) {

    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)) {
        Text(text = "Categories")
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(categories) {category ->
            val totalSpent = spendingMap[category.cat_id] ?: 0.0

            val borderColour = categoryColourMap[category.cat_id] ?: Color.Transparent

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = if (mode == 1 && borderColour != Color.Transparent) BorderStroke(2.dp, borderColour) else null,
                onClick = { onItemClick(category) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = category.cat_name)

                    if (mode == 0) {
                        Text(text = "£${"%.2f".format(totalSpent)}", fontWeight = FontWeight.Bold)
                    } else if (mode == 1) {

                        val budget = budgets.find { it.cat_id == category.cat_id }
                        val limit = budget?.limit ?: 0.0
                        Text(text = "£${"%.2f".format(totalSpent)}"+ " / £${"%.2f".format(limit)}", fontWeight = FontWeight.Bold)
                    }
                }
            }

        }

    }
}



@Composable
fun SpendingView(
    modifier: Modifier = Modifier,
    categories: List<Category>,
    spendingMap: Map<Int, Double>,
) {

    Column(modifier = modifier) {
        //chart then
        val spentCategories = remember(categories, spendingMap) {
            categories.filter {
                spendingMap[it.cat_id] ?: 0.0 > 0.0
            }.sortedByDescending { spendingMap[it.cat_id] ?: 0.0}
        }

        CategoryList(0, spentCategories, spendingMap, budgets = emptyList())

    }

}



@Composable
fun BudgetView(
    modifier: Modifier = Modifier,
    categories: List<Category>,
    budgets: List<Budget>,
    spendingMap: Map<Int, Double>,
    onBudgetClick: (Budget) -> Unit
) {

    val categoryColourMap = remember(budgets, spendingMap) {
        budgets.associate { budget ->
            val targetId = budget.cat_id ?: 0
            val index = budgets.indexOf(budget)
            val spend = spendingMap[targetId] ?: 0.0
            val colourBase = colours[index % colours.size]
            val operationalColor = if (spend > budget.limit) Color(0xFFFF0000) else colourBase
            targetId to operationalColor
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                BudgetPieChart(
                    budgets = budgets,
                    spendingMap = spendingMap
                )
            }
        }

        val budgetedCategories = remember(categories, budgets) {
            categories.filter { cat ->
                budgets.any { budget -> budget.cat_id == cat.cat_id }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {

            CategoryList(
                mode = 1,
                categories = budgetedCategories,
                spendingMap = spendingMap,
                budgets = budgets,
                categoryColourMap = categoryColourMap,
                onItemClick = { clickedCategory ->
                    val associatedBudget = budgets.find { it.cat_id == clickedCategory.cat_id }
                    if (associatedBudget != null) onBudgetClick(associatedBudget)
                })
        }
    }

}




@Composable
fun SingleChoiceSegmentedButton(
    options: List<String>,
    selectedIndex: Int = 0,
    onSelectionChanged: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {

    SingleChoiceSegmentedButtonRow(
        modifier = modifier.fillMaxWidth()
    ) {
        options.forEachIndexed {index, label ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size
                ),
                onClick = { onSelectionChanged(index)},
                selected = selectedIndex == index,
                label = {Text(label)}
            )
        }
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBudgetDialog(
    unbudgetedCategories: List<Category>,
    onDismiss: () -> Unit,
    onConfirm: (Budget) -> Unit,
    onDelete: (Budget) -> Unit,
    //showAddBudgetDialog: Boolean,
    budget: Budget? = null,
) {

    var nameInput by remember { mutableStateOf(budget?.budget_name ?: "") }
    var limitInput by remember { mutableStateOf(budget?.limit?.toString() ?: "") }

    var dropdownExpanded by remember { mutableStateOf(false) }

    var selectedCategoryIndex by remember {
        mutableStateOf(value =
            if (budget != null) {
                val foundId = unbudgetedCategories.indexOfFirst { it.cat_id == budget.cat_id }
                if (foundId != -1) foundId else 0
            } else 0
    )}

    val selectedCategoryLabel = unbudgetedCategories.getOrNull(selectedCategoryIndex)?.cat_name ?: "Select Category"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {Text(if (budget == null) "Add Budget" else "Edit Budget")},
        text = {
            Column() {
                Row {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Name") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = limitInput,
                        onValueChange = { limitInput = it},
                        label = { Text("Amount") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = !dropdownExpanded },

                ) {
                    OutlinedTextField(
                        value = selectedCategoryLabel,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Category") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),

                        )

                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        unbudgetedCategories.forEachIndexed { index, category ->
                            DropdownMenuItem(
                                text = { Text(category.cat_name) },
                                onClick = {
                                    selectedCategoryIndex = index
                                    dropdownExpanded = false
                                }
                            )
                        }

                    }

                }

            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedLimitValue = limitInput.toDoubleOrNull() ?: 0.0
                    val selectedCategory = unbudgetedCategories.getOrNull(selectedCategoryIndex)

                    if (nameInput.isNotEmpty() && parsedLimitValue > 0.0 && selectedCategory != null) {
                        val updatedBudget = Budget(
                            budget_id = budget?.budget_id ?: 0,
                            budget_name = nameInput,
                            limit = parsedLimitValue,
                            cat_id = selectedCategory.cat_id,
                            month = budget?.month ?: java.time.YearMonth.now().toString()
                        )

                        onConfirm(updatedBudget)

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
                // If we are editing an existing item, show the delete button
                if (budget != null) {
                    TextButton(
                        onClick = { onDelete(budget) }
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


@Composable
fun BudgetPieChart(
    budgets: List<Budget>,
    spendingMap: Map<Int, Double>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier.wrapContentSize(),
        contentAlignment = Alignment.Center
    ) {

        if (budgets.isEmpty()) return

        val totalBudgetLimit = remember(budgets) { budgets.sumOf { it.limit }.toFloat() }

        if (totalBudgetLimit == 0f) return

        val angle = 360f / budgets.size

        var startAngle = -90f

        Canvas(modifier = modifier.size(280.dp)) {

            val centerPoint = Offset(size.width / 2, size.height / 2)

            val neutralCircleRadius = size.width / 3.5f
            //val maxCanvasRadius = size.width / 2f  //Adjust after reviewing in test

            budgets.forEachIndexed {index, budget ->
                //val angle = (budget.limit.toFloat() / totalBudgetLimit) * 360f
                val spend = spendingMap[budget.cat_id] ?: 0.0
                val spendRatio = if (budget.limit > 0) (spend / budget.limit).toFloat() else 0f

                val radiusFactor = if (spendRatio > 1.0f) { //Slower radius gain for over budgeted view, capped at 1.2 to prevent other categories from downscaling
                    (1.0f + (spendRatio - 1.0f) * 0.15f).coerceAtMost(1.2f)
                } else {
                    spendRatio
                }

                val sectorRadius = (radiusFactor * neutralCircleRadius)

                val baseSectorColour = colours[index % colours.size]

                val sectorColour = if (spend > budget.limit) {
                    Color(0xFFFF4D4D).copy(alpha = 0.85f)
                } else {
                    baseSectorColour.copy(alpha = 0.8f)
                }

                if (sectorRadius > 0f) {
                    drawArc(
                        color = sectorColour,
                        startAngle = startAngle,
                        sweepAngle = angle,
                        useCenter = true,
                        topLeft = centerPoint - Offset(sectorRadius, sectorRadius),
                        size = Size(sectorRadius * 2, sectorRadius * 2)
                    )
                }


                drawArc(
                    color = Color.White,
                    startAngle = startAngle,
                    sweepAngle = angle,
                    useCenter = true,
                    topLeft = centerPoint - Offset(sectorRadius, sectorRadius),
                    size = Size(sectorRadius * 2, sectorRadius * 2),
                    style = Stroke(width = 1.dp.toPx())
                )

                startAngle += angle

            }


            drawCircle(
                color = Color.Black.copy(alpha = 0.5f),
                radius = neutralCircleRadius,
                center = centerPoint,
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            )

        }

    }


}

