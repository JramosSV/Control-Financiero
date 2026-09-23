package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import com.example.data.entity.BudgetEntity
import com.example.data.entity.TransactionEntity
import com.example.data.entity.TransactionType
import com.example.ui.model.FinanceFormatters
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberLight
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.RoseDark
import com.example.ui.theme.RoseExpense
import com.example.ui.theme.RoseLight

enum class AlertSeverity {
    HEALTHY,
    WARNING,
    DANGER
}

data class BudgetAlert(
    val category: String, // "TOTAL" or specific category
    val spent: Double,
    val limit: Double,
    val percentage: Float, // e.g. 0.85f or 1.10f
    val severity: AlertSeverity,
    val title: String,
    val message: String
)

@Composable
fun AutomaticBudgetAlertsBanner(
    transactions: List<TransactionEntity>,
    budgets: List<BudgetEntity>,
    onConfigureBudgetsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alerts = remember(transactions, budgets) {
        val list = mutableListOf<BudgetAlert>()
        val expenseTxs = transactions.filter { it.type == TransactionType.EXPENSE }
        val totalSpent = expenseTxs.sumOf { it.amount }

        // 1. Check Overall Budget
        val totalBudget = budgets.firstOrNull { it.category == "TOTAL" }
        if (totalBudget != null && totalBudget.limitAmount > 0) {
            val ratio = (totalSpent / totalBudget.limitAmount).toFloat()
            val threshold = totalBudget.alertThresholdPercent

            if (ratio >= 1.0f) {
                val exceeded = totalSpent - totalBudget.limitAmount
                list.add(
                    BudgetAlert(
                        category = "TOTAL",
                        spent = totalSpent,
                        limit = totalBudget.limitAmount,
                        percentage = ratio,
                        severity = AlertSeverity.DANGER,
                        title = "¡Presupuesto Mensual Excedido!",
                        message = "Has superado el límite mensual en ${FinanceFormatters.formatMoney(exceeded)} (${(ratio * 100).toInt()}% consumido)."
                    )
                )
            } else if (ratio >= threshold) {
                val remaining = totalBudget.limitAmount - totalSpent
                list.add(
                    BudgetAlert(
                        category = "TOTAL",
                        spent = totalSpent,
                        limit = totalBudget.limitAmount,
                        percentage = ratio,
                        severity = AlertSeverity.WARNING,
                        title = "Alerta: Presupuesto al ${(ratio * 100).toInt()}%",
                        message = "Te quedan ${FinanceFormatters.formatMoney(remaining)} de margen para este mes."
                    )
                )
            }
        }

        // 2. Check Category Budgets
        val categoryBudgets = budgets.filter { it.category != "TOTAL" }
        for (b in categoryBudgets) {
            val catSpent = expenseTxs.filter { it.category.equals(b.category, ignoreCase = true) }.sumOf { it.amount }
            if (b.limitAmount > 0) {
                val ratio = (catSpent / b.limitAmount).toFloat()
                if (ratio >= 1.0f) {
                    val diff = catSpent - b.limitAmount
                    list.add(
                        BudgetAlert(
                            category = b.category,
                            spent = catSpent,
                            limit = b.limitAmount,
                            percentage = ratio,
                            severity = AlertSeverity.DANGER,
                            title = "Límite superado en ${b.category}",
                            message = "Gastado: ${FinanceFormatters.formatMoney(catSpent)} de ${FinanceFormatters.formatMoney(b.limitAmount)} (+${FinanceFormatters.formatMoney(diff)} exceso)."
                        )
                    )
                } else if (ratio >= b.alertThresholdPercent) {
                    val remaining = b.limitAmount - catSpent
                    list.add(
                        BudgetAlert(
                            category = b.category,
                            spent = catSpent,
                            limit = b.limitAmount,
                            percentage = ratio,
                            severity = AlertSeverity.WARNING,
                            title = "Alerta preventiva: ${b.category} al ${(ratio * 100).toInt()}%",
                            message = "Has consumido ${FinanceFormatters.formatMoney(catSpent)}. Margen restante: ${FinanceFormatters.formatMoney(remaining)}."
                        )
                    )
                }
            }
        }

        list.sortedByDescending { if (it.severity == AlertSeverity.DANGER) 2 else 1 }
    }

    var isExpanded by remember { mutableStateOf(false) }

    if (alerts.isEmpty()) {
        // Healthy State Card
        val totalBudget = budgets.firstOrNull { it.category == "TOTAL" }
        val totalSpent = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

        Card(
            modifier = modifier
                .fillMaxWidth()
                .testTag("budget_alert_healthy_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = EmeraldLight.copy(alpha = 0.5f)
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Presupuesto saludable",
                            tint = EmeraldDark,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Presupuesto Saludable",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldDark
                        )
                        val textSubtitle = if (totalBudget != null && totalBudget.limitAmount > 0) {
                            val pct = ((totalSpent / totalBudget.limitAmount) * 100).toInt()
                            "Gastos al $pct% de ${FinanceFormatters.formatMoney(totalBudget.limitAmount)}"
                        } else {
                            "Tus gastos mensuales están dentro del rango óptimo."
                        }
                        Text(
                            text = textSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldDark.copy(alpha = 0.85f)
                        )
                    }
                }
                OutlinedButton(
                    onClick = onConfigureBudgetsClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldDark),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_configure_budget")
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Límites", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        return
    }

    // Active Alerts (Warning or Danger)
    val highestSeverity = if (alerts.any { it.severity == AlertSeverity.DANGER }) AlertSeverity.DANGER else AlertSeverity.WARNING
    val mainAlert = alerts.first()

    val containerColor = if (highestSeverity == AlertSeverity.DANGER) RoseLight.copy(alpha = 0.65f) else AmberLight.copy(alpha = 0.7f)
    val borderColor = if (highestSeverity == AlertSeverity.DANGER) RoseExpense else AmberWarning
    val iconColor = if (highestSeverity == AlertSeverity.DANGER) RoseDark else AmberDark
    val textColor = if (highestSeverity == AlertSeverity.DANGER) RoseDark else AmberDark

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("budget_alert_active_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(borderColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (highestSeverity == AlertSeverity.DANGER) Icons.Default.NotificationsActive else Icons.Default.Warning,
                            contentDescription = "Alerta",
                            tint = iconColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = mainAlert.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            if (alerts.size > 1) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(borderColor)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "+${alerts.size - 1}",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Text(
                            text = mainAlert.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = textColor.copy(alpha = 0.9f)
                        )
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Ocultar detalles" else "Ver detalles",
                    tint = textColor
                )
            }

            // Progress Indicator for main alert
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { mainAlert.percentage.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (mainAlert.severity == AlertSeverity.DANGER) RoseExpense else AmberWarning,
                trackColor = borderColor.copy(alpha = 0.25f)
            )

            // Expanded List of all Alerts
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    alerts.forEachIndexed { index, alert ->
                        if (index > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.6f))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = alert.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (alert.severity == AlertSeverity.DANGER) RoseDark else AmberDark
                                )
                                Text(
                                    text = alert.message,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${(alert.percentage * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (alert.severity == AlertSeverity.DANGER) RoseExpense else AmberWarning
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onConfigureBudgetsClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = textColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_manage_budget_from_alert")
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ajustar Límites de Presupuesto")
                    }
                }
            }
        }
    }
}
