package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.entity.BudgetEntity
import com.example.data.entity.TransactionEntity
import com.example.data.entity.TransactionType
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class FinanceUiState(
    val selectedMonthYear: String = "2026-09",
    val filterType: String = "ALL", // "ALL", "EXPENSE", "INCOME"
    val selectedCategoryFilter: String? = null,
    val searchQuery: String = "",
    val availableMonths: List<String> = listOf("2026-09", "2026-08"),
    val isAddEditSheetOpen: Boolean = false,
    val editingTransaction: TransactionEntity? = null,
    val isBudgetDialogOpen: Boolean = false
)

class FinanceViewModel(
    application: Application,
    private val repository: FinanceRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(FinanceUiState())
    val uiState: StateFlow<FinanceUiState> = _uiState.asStateFlow()

    init {
        // Seed initial data if first launch
        viewModelScope.launch {
            repository.seedInitialDataIfNeeded("2026-09")
        }
    }

    // Observe distinct months
    val availableMonths: StateFlow<List<String>> = repository.getDistinctMonths()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf("2026-09")
        )

    // Current month transactions flow
    @OptIn(ExperimentalCoroutinesApi::class)
    val rawMonthTransactions: StateFlow<List<TransactionEntity>> = _uiState
        .flatMapLatest { state ->
            repository.getTransactionsByMonth(state.selectedMonthYear)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filtered transactions for list display
    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        rawMonthTransactions,
        _uiState
    ) { txs, state ->
        txs.filter { tx ->
            val matchType = when (state.filterType) {
                "EXPENSE" -> tx.type == TransactionType.EXPENSE
                "INCOME" -> tx.type == TransactionType.INCOME
                else -> true
            }
            val matchCategory = state.selectedCategoryFilter == null ||
                    tx.category.equals(state.selectedCategoryFilter, ignoreCase = true)

            val matchQuery = state.searchQuery.isBlank() ||
                    tx.title.contains(state.searchQuery, ignoreCase = true) ||
                    tx.notes.contains(state.searchQuery, ignoreCase = true) ||
                    tx.category.contains(state.searchQuery, ignoreCase = true)

            matchType && matchCategory && matchQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Budgets for current month
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentMonthBudgets: StateFlow<List<BudgetEntity>> = _uiState
        .flatMapLatest { state ->
            repository.getBudgetsForMonth(state.selectedMonthYear)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onPreviousMonth() {
        val current = _uiState.value.selectedMonthYear
        val parts = current.split("-")
        var year = parts[0].toInt()
        var month = parts[1].toInt()

        month -= 1
        if (month < 1) {
            month = 12
            year -= 1
        }
        val newMonth = "%04d-%02d".format(year, month)
        _uiState.value = _uiState.value.copy(selectedMonthYear = newMonth)
    }

    fun onNextMonth() {
        val current = _uiState.value.selectedMonthYear
        val parts = current.split("-")
        var year = parts[0].toInt()
        var month = parts[1].toInt()

        month += 1
        if (month > 12) {
            month = 1
            year += 1
        }
        val newMonth = "%04d-%02d".format(year, month)
        _uiState.value = _uiState.value.copy(selectedMonthYear = newMonth)
    }

    fun setMonthYear(monthYear: String) {
        _uiState.value = _uiState.value.copy(selectedMonthYear = monthYear)
    }

    fun setFilterType(type: String) {
        _uiState.value = _uiState.value.copy(filterType = type)
    }

    fun setCategoryFilter(category: String?) {
        _uiState.value = _uiState.value.copy(selectedCategoryFilter = category)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun openAddTransactionSheet() {
        _uiState.value = _uiState.value.copy(isAddEditSheetOpen = true, editingTransaction = null)
    }

    fun openEditTransactionSheet(transaction: TransactionEntity) {
        _uiState.value = _uiState.value.copy(isAddEditSheetOpen = true, editingTransaction = transaction)
    }

    fun closeAddEditSheet() {
        _uiState.value = _uiState.value.copy(isAddEditSheetOpen = false, editingTransaction = null)
    }

    fun openBudgetDialog() {
        _uiState.value = _uiState.value.copy(isBudgetDialogOpen = true)
    }

    fun closeBudgetDialog() {
        _uiState.value = _uiState.value.copy(isBudgetDialogOpen = false)
    }

    fun saveTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            if (transaction.id == 0L) {
                repository.insertTransaction(transaction)
            } else {
                repository.updateTransaction(transaction)
            }
            closeAddEditSheet()
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun saveBudgets(budgets: List<BudgetEntity>) {
        viewModelScope.launch {
            budgets.forEach { budget ->
                repository.saveBudget(budget)
            }
            closeBudgetDialog()
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            repository.seedInitialDataIfNeeded(_uiState.value.selectedMonthYear)
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = AppDatabase.getDatabase(application)
                    val repo = FinanceRepository(db.transactionDao(), db.budgetDao())
                    return FinanceViewModel(application, repo) as T
                }
            }
    }
}
