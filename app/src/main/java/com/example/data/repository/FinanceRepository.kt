package com.example.data.repository

import com.example.data.dao.BudgetDao
import com.example.data.dao.TransactionDao
import com.example.data.entity.BudgetEntity
import com.example.data.entity.TransactionEntity
import com.example.data.entity.TransactionType
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao
) {
    fun getAllTransactions(): Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    fun getTransactionsByMonth(monthYear: String): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByMonth(monthYear)

    fun getDistinctMonths(): Flow<List<String>> = transactionDao.getDistinctMonths()

    fun getBudgetsForMonth(monthYear: String): Flow<List<BudgetEntity>> =
        budgetDao.getBudgetsForMonth(monthYear)

    suspend fun insertTransaction(transaction: TransactionEntity): Long =
        transactionDao.insertTransaction(transaction)

    suspend fun updateTransaction(transaction: TransactionEntity) =
        transactionDao.updateTransaction(transaction)

    suspend fun deleteTransaction(transaction: TransactionEntity) =
        transactionDao.deleteTransaction(transaction)

    suspend fun deleteTransactionById(id: Long) =
        transactionDao.deleteTransactionById(id)

    suspend fun saveBudget(budget: BudgetEntity): Long =
        budgetDao.insertOrUpdateBudget(budget)

    suspend fun deleteBudget(monthYear: String, category: String) =
        budgetDao.deleteBudget(monthYear, category)

    suspend fun seedInitialDataIfNeeded(currentMonthYear: String = "2026-09") {
        if (transactionDao.getCount() == 0) {
            val cal = Calendar.getInstance()

            // Seed September 2026 transactions
            val transactions = listOf(
                TransactionEntity(
                    title = "Nómina Quincenal",
                    amount = 1850.00,
                    type = TransactionType.INCOME,
                    category = "Salario",
                    dateMillis = cal.apply { set(2026, Calendar.SEPTEMBER, 1, 9, 0) }.timeInMillis,
                    monthYear = currentMonthYear,
                    notes = "Primer pago mensual"
                ),
                TransactionEntity(
                    title = "Trabajo Freelance Diseño",
                    amount = 450.00,
                    type = TransactionType.INCOME,
                    category = "Negocio / Ventas",
                    dateMillis = cal.apply { set(2026, Calendar.SEPTEMBER, 5, 14, 30) }.timeInMillis,
                    monthYear = currentMonthYear,
                    notes = "Consultoría web"
                ),
                TransactionEntity(
                    title = "Alquiler Apartamento",
                    amount = 750.00,
                    type = TransactionType.EXPENSE,
                    category = "Vivienda",
                    dateMillis = cal.apply { set(2026, Calendar.SEPTEMBER, 2, 10, 0) }.timeInMillis,
                    monthYear = currentMonthYear,
                    notes = "Mensualidad fija"
                ),
                TransactionEntity(
                    title = "Supermercado Mensual",
                    amount = 265.50,
                    type = TransactionType.EXPENSE,
                    category = "Alimentación",
                    dateMillis = cal.apply { set(2026, Calendar.SEPTEMBER, 3, 16, 20) }.timeInMillis,
                    monthYear = currentMonthYear,
                    notes = "Compras despensa familiar"
                ),
                TransactionEntity(
                    title = "Gasolina Vehículo",
                    amount = 65.00,
                    type = TransactionType.EXPENSE,
                    category = "Transporte",
                    dateMillis = cal.apply { set(2026, Calendar.SEPTEMBER, 4, 8, 45) }.timeInMillis,
                    monthYear = currentMonthYear,
                    notes = "Tanque lleno"
                ),
                TransactionEntity(
                    title = "Servicio Internet y Fibra",
                    amount = 55.00,
                    type = TransactionType.EXPENSE,
                    category = "Servicios",
                    dateMillis = cal.apply { set(2026, Calendar.SEPTEMBER, 6, 11, 15) }.timeInMillis,
                    monthYear = currentMonthYear,
                    notes = "Plan 300 Mbps"
                ),
                TransactionEntity(
                    title = "Cena Restaurante La Terraza",
                    amount = 84.00,
                    type = TransactionType.EXPENSE,
                    category = "Alimentación",
                    dateMillis = cal.apply { set(2026, Calendar.SEPTEMBER, 8, 20, 30) }.timeInMillis,
                    monthYear = currentMonthYear,
                    notes = "Cena fin de semana"
                ),
                TransactionEntity(
                    title = "Plataformas de Streaming",
                    amount = 32.00,
                    type = TransactionType.EXPENSE,
                    category = "Entretenimiento",
                    dateMillis = cal.apply { set(2026, Calendar.SEPTEMBER, 9, 12, 0) }.timeInMillis,
                    monthYear = currentMonthYear,
                    notes = "Netflix y Spotify"
                ),
                TransactionEntity(
                    title = "Cafetería y Snacks",
                    amount = 42.50,
                    type = TransactionType.EXPENSE,
                    category = "Alimentación",
                    dateMillis = cal.apply { set(2026, Calendar.SEPTEMBER, 11, 17, 10) }.timeInMillis,
                    monthYear = currentMonthYear,
                    notes = "Cafés durante semana laboral"
                ),
                TransactionEntity(
                    title = "Electricidad y Agua",
                    amount = 88.00,
                    type = TransactionType.EXPENSE,
                    category = "Servicios",
                    dateMillis = cal.apply { set(2026, Calendar.SEPTEMBER, 12, 9, 30) }.timeInMillis,
                    monthYear = currentMonthYear,
                    notes = "Recibo bimensual"
                ),
                TransactionEntity(
                    title = "Farmacia y Vitaminas",
                    amount = 38.00,
                    type = TransactionType.EXPENSE,
                    category = "Salud",
                    dateMillis = cal.apply { set(2026, Calendar.SEPTEMBER, 13, 18, 0) }.timeInMillis,
                    monthYear = currentMonthYear,
                    notes = "Suplementos de salud"
                ),
                TransactionEntity(
                    title = "Mantenimiento Frenos Auto",
                    amount = 95.00,
                    type = TransactionType.EXPENSE,
                    category = "Transporte",
                    dateMillis = cal.apply { set(2026, Calendar.SEPTEMBER, 14, 15, 0) }.timeInMillis,
                    monthYear = currentMonthYear,
                    notes = "Cambio de pastillas"
                ),
                TransactionEntity(
                    title = "Compras Supermercado Frescos",
                    amount = 78.25,
                    type = TransactionType.EXPENSE,
                    category = "Alimentación",
                    dateMillis = cal.apply { set(2026, Calendar.SEPTEMBER, 15, 11, 45) }.timeInMillis,
                    monthYear = currentMonthYear,
                    notes = "Verduras y carnes"
                )
            )
            transactionDao.insertAll(transactions)

            // Seed Budgets for September 2026
            val budgets = listOf(
                BudgetEntity(
                    monthYear = currentMonthYear,
                    category = "TOTAL",
                    limitAmount = 1800.00,
                    alertThresholdPercent = 0.80f
                ),
                BudgetEntity(
                    monthYear = currentMonthYear,
                    category = "Alimentación",
                    limitAmount = 500.00,
                    alertThresholdPercent = 0.80f // 265.50 + 84.00 + 42.50 + 78.25 = 470.25 (94% -> ALERT!)
                ),
                BudgetEntity(
                    monthYear = currentMonthYear,
                    category = "Transporte",
                    limitAmount = 200.00,
                    alertThresholdPercent = 0.80f // 65 + 95 = 160 (80% -> ALERT!)
                ),
                BudgetEntity(
                    monthYear = currentMonthYear,
                    category = "Vivienda",
                    limitAmount = 800.00,
                    alertThresholdPercent = 0.85f
                ),
                BudgetEntity(
                    monthYear = currentMonthYear,
                    category = "Servicios",
                    limitAmount = 180.00,
                    alertThresholdPercent = 0.80f
                ),
                BudgetEntity(
                    monthYear = currentMonthYear,
                    category = "Entretenimiento",
                    limitAmount = 120.00,
                    alertThresholdPercent = 0.80f
                )
            )
            budgetDao.insertAll(budgets)
        }
    }
}
