package com.example.ui.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.entity.TransactionType
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BlueInfo
import com.example.ui.theme.CyanUtilities
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IndigoEducation
import com.example.ui.theme.Navy700
import com.example.ui.theme.OrangeShopping
import com.example.ui.theme.PinkEntertainment
import com.example.ui.theme.PurpleInvest
import com.example.ui.theme.RoseExpense
import com.example.ui.theme.TealHealth
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CategoryItem(
    val name: String,
    val color: Color,
    val icon: ImageVector,
    val type: TransactionType
)

object CategoryRegistry {
    val expenseCategories = listOf(
        CategoryItem("Alimentación", AmberWarning, Icons.Default.Fastfood, TransactionType.EXPENSE),
        CategoryItem("Transporte", BlueInfo, Icons.Default.DirectionsCar, TransactionType.EXPENSE),
        CategoryItem("Vivienda", IndigoEducation, Icons.Default.Home, TransactionType.EXPENSE),
        CategoryItem("Servicios", CyanUtilities, Icons.Default.Lightbulb, TransactionType.EXPENSE),
        CategoryItem("Entretenimiento", PinkEntertainment, Icons.Default.Movie, TransactionType.EXPENSE),
        CategoryItem("Salud", TealHealth, Icons.Default.LocalHospital, TransactionType.EXPENSE),
        CategoryItem("Educación", PurpleInvest, Icons.Default.School, TransactionType.EXPENSE),
        CategoryItem("Compras", OrangeShopping, Icons.Default.ShoppingBag, TransactionType.EXPENSE),
        CategoryItem("Otros Gastos", Navy700, Icons.Default.MoreHoriz, TransactionType.EXPENSE)
    )

    val incomeCategories = listOf(
        CategoryItem("Salario", EmeraldPrimary, Icons.Default.Work, TransactionType.INCOME),
        CategoryItem("Negocio / Ventas", OrangeShopping, Icons.Default.Store, TransactionType.INCOME),
        CategoryItem("Inversiones", PurpleInvest, Icons.Default.TrendingUp, TransactionType.INCOME),
        CategoryItem("Regalos", PinkEntertainment, Icons.Default.CardGiftcard, TransactionType.INCOME),
        CategoryItem("Otros Ingresos", BlueInfo, Icons.Default.AccountBalance, TransactionType.INCOME)
    )

    fun getCategory(name: String, type: TransactionType): CategoryItem {
        val list = if (type == TransactionType.INCOME) incomeCategories else expenseCategories
        return list.firstOrNull { it.name.equals(name, ignoreCase = true) }
            ?: (expenseCategories + incomeCategories).firstOrNull { it.name.equals(name, ignoreCase = true) }
            ?: CategoryItem(name, Color(0xFF64748B), Icons.Default.MoreHoriz, type)
    }

    fun getColorForCategory(name: String): Color {
        return (expenseCategories + incomeCategories).firstOrNull { it.name.equals(name, ignoreCase = true) }?.color
            ?: Color(0xFF64748B)
    }
}

object FinanceFormatters {
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 2
    }

    private val dateFormat = SimpleDateFormat("dd MMM, yyyy", Locale("es", "ES"))
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale("es", "ES"))

    fun formatMoney(amount: Double): String {
        return currencyFormat.format(amount)
    }

    fun formatDate(timeMillis: Long): String {
        return dateFormat.format(Date(timeMillis))
    }

    fun formatTime(timeMillis: Long): String {
        return timeFormat.format(Date(timeMillis))
    }

    fun formatMonthYearDisplay(monthYear: String): String {
        // monthYear is "YYYY-MM"
        return try {
            val parts = monthYear.split("-")
            val year = parts[0]
            val month = parts[1].toInt()
            val monthNames = listOf(
                "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
            )
            "${monthNames[month - 1]} $year"
        } catch (e: Exception) {
            monthYear
        }
    }
}
