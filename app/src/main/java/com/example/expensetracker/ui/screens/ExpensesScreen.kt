package com.example.expensetracker.ui.screens

import android.R.attr.category
import android.R.attr.mode
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.modifierLocalOf
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.expensetracker.ui.ExpensesViewModel
import com.example.expensetracker.ui.Screens
import kotlin.collections.listOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    viewModel: ExpensesViewModel = viewModel(),
    navController: NavHostController
) {
    var selectedView by remember { mutableStateOf(0) }

    val categories = listOf("Food", "Transport", "Entertainment", "Other")

    Scaffold (
        modifier = Modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = "Spending/Budget") },
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

            SingleChoiceSegmentedButton(
                options = listOf("Spending", "Budget"),
                selectedIndex = selectedView,
                onSelectionChanged = { selectedView = it }
            )

            when (selectedView) {
                0 -> SpendingView(
                    modifier=Modifier.weight(1f),
                    categories = categories

                )
                1 -> BudgetView(
                    modifier=Modifier.weight(1f),
                    categories = categories

                )
            }

        }

    }

}


@Composable
fun CategoryList(mode: Int, categories: List<String>) {

    Text(text = "Hello from Category List")

    LazyColumn {

        items(categories) {category ->
            Text(text = category)
        }

        items(5) { index ->
            Text(text = "Category: $index")
        }
    }
}



@Composable
fun SpendingView(
    modifier: Modifier = Modifier,
    categories: List<String>
) {
    Text(text = "Hello from Spending")

    Column(modifier = modifier) {
        //chart then

        CategoryList(0, categories)

    }

}



@Composable
fun BudgetView(
    modifier: Modifier = Modifier,
    categories: List<String>
) {
    Text(text = "Hello from Budget")

    Column(modifier = modifier) {
        // chart then

        CategoryList(1, categories)
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



