package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.TransactionEntity
import com.example.ui.FinanceViewModel
import com.example.ui.components.AddEditTransactionSheet
import com.example.ui.components.AutomaticBudgetAlertsBanner
import com.example.ui.components.BudgetSettingsDialog
import com.example.ui.components.InteractiveChartsSection
import com.example.ui.components.MainBalanceCard
import com.example.ui.components.MonthSelectorHeader
import com.example.ui.components.TransactionItemCard
import com.example.ui.model.CategoryRegistry
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.RoseExpense

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val rawTransactions by viewModel.rawMonthTransactions.collectAsStateWithLifecycle()
    val filteredTransactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val budgets by viewModel.currentMonthBudgets.collectAsStateWithLifecycle()

    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var showCategoryFilterMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                        }
                        Text(
                            text = "Control Financiero",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.openBudgetDialog() },
                        modifier = Modifier.testTag("action_open_budgets")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Configurar Presupuestos",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { viewModel.resetDemoData() },
                        modifier = Modifier.testTag("action_reset_data")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Restaurar Datos de Demostración",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openAddTransactionSheet() },
                containerColor = EmeraldPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_transaction")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Movimiento")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nuevo Movimiento", fontWeight = FontWeight.Bold)
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Month Navigation Header
            item {
                MonthSelectorHeader(
                    currentMonthYear = uiState.selectedMonthYear,
                    onPreviousMonth = { viewModel.onPreviousMonth() },
                    onNextMonth = { viewModel.onNextMonth() },
                    onSelectMonthDialog = { /* Already responsive with arrows */ }
                )
            }

            // 2. Automatic Monthly Budget Alerts Banner
            item {
                AutomaticBudgetAlertsBanner(
                    transactions = rawTransactions,
                    budgets = budgets,
                    onConfigureBudgetsClick = { viewModel.openBudgetDialog() }
                )
            }

            // 3. Main Total Balance & Monthly Budget Gauge Card
            item {
                MainBalanceCard(
                    transactions = rawTransactions,
                    budgets = budgets
                )
            }

            // 4. Interactive Charts Section (Donut, Dual Bars, Trend Curve)
            item {
                InteractiveChartsSection(transactions = rawTransactions)
            }

            // 5. Transactions Section Header & Controls
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Movimientos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${filteredTransactions.size} registros",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Search text field
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Buscar por concepto, nota o categoría...") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_transactions_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Type Filter Chips & Category Dropdown
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = uiState.filterType == "ALL",
                                onClick = { viewModel.setFilterType("ALL") },
                                label = { Text("Todos") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = uiState.filterType == "EXPENSE",
                                onClick = { viewModel.setFilterType("EXPENSE") },
                                label = { Text("Gastos") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RoseExpense.copy(alpha = 0.2f),
                                    selectedLabelColor = RoseExpense
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = uiState.filterType == "INCOME",
                                onClick = { viewModel.setFilterType("INCOME") },
                                label = { Text("Ingresos") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldPrimary.copy(alpha = 0.2f),
                                    selectedLabelColor = EmeraldDark
                                )
                            )
                        }
                        item {
                            Box {
                                FilterChip(
                                    selected = uiState.selectedCategoryFilter != null,
                                    onClick = { showCategoryFilterMenu = true },
                                    label = {
                                        Text(uiState.selectedCategoryFilter ?: "Categoría")
                                    },
                                    trailingIcon = {
                                        if (uiState.selectedCategoryFilter != null) {
                                            Icon(
                                                Icons.Default.Clear,
                                                contentDescription = "Quitar filtro de categoría",
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clickable { viewModel.setCategoryFilter(null) }
                                            )
                                        } else {
                                            Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                )

                                DropdownMenu(
                                    expanded = showCategoryFilterMenu,
                                    onDismissRequest = { showCategoryFilterMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Todas las categorías") },
                                        onClick = {
                                            viewModel.setCategoryFilter(null)
                                            showCategoryFilterMenu = false
                                        }
                                    )
                                    (CategoryRegistry.expenseCategories + CategoryRegistry.incomeCategories)
                                        .map { it.name }
                                        .distinct()
                                        .forEach { catName ->
                                            DropdownMenuItem(
                                                text = { Text(catName) },
                                                onClick = {
                                                    viewModel.setCategoryFilter(catName)
                                                    showCategoryFilterMenu = false
                                                }
                                            )
                                        }
                                }
                            }
                        }
                    }
                }
            }

            // 6. Transactions List Items
            if (filteredTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No se encontraron movimientos",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Intenta cambiar los filtros o agrega un nuevo movimiento con el botón (+).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            } else {
                items(filteredTransactions, key = { it.id }) { tx ->
                    TransactionItemCard(
                        transaction = tx,
                        onEditClick = { viewModel.openEditTransactionSheet(it) },
                        onDeleteClick = { transactionToDelete = it }
                    )
                }
            }
        }
    }

    // Modal Add / Edit Sheet
    if (uiState.isAddEditSheetOpen) {
        AddEditTransactionSheet(
            initialTransaction = uiState.editingTransaction,
            currentMonthYear = uiState.selectedMonthYear,
            onDismiss = { viewModel.closeAddEditSheet() },
            onSave = { viewModel.saveTransaction(it) }
        )
    }

    // Modal Budget Settings Dialog
    if (uiState.isBudgetDialogOpen) {
        BudgetSettingsDialog(
            currentMonthYear = uiState.selectedMonthYear,
            existingBudgets = budgets,
            onDismiss = { viewModel.closeBudgetDialog() },
            onSaveBudgets = { viewModel.saveBudgets(it) }
        )
    }

    // Delete Confirmation Dialog
    if (transactionToDelete != null) {
        val tx = transactionToDelete!!
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Eliminar Transacción") },
            text = { Text("¿Estás seguro de que deseas eliminar \"${tx.title}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTransaction(tx)
                        transactionToDelete = null
                    },
                    modifier = Modifier.testTag("btn_confirm_delete_tx")
                ) {
                    Text("Eliminar", color = RoseExpense, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
