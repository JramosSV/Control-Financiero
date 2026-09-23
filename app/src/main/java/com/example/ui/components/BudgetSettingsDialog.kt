package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.entity.BudgetEntity
import com.example.ui.model.CategoryRegistry
import com.example.ui.model.FinanceFormatters

@Composable
fun BudgetSettingsDialog(
    currentMonthYear: String,
    existingBudgets: List<BudgetEntity>,
    onDismiss: () -> Unit,
    onSaveBudgets: (List<BudgetEntity>) -> Unit
) {
    val overallBudget = existingBudgets.firstOrNull { it.category == "TOTAL" }

    var totalLimitText by remember {
        mutableStateOf(overallBudget?.limitAmount?.let { if (it > 0) it.toString() else "" } ?: "1800.00")
    }

    var thresholdPercent by remember {
        mutableFloatStateOf(overallBudget?.alertThresholdPercent ?: 0.80f)
    }

    // Category limits map
    val categoryBudgetsMap = remember(existingBudgets) {
        val map = mutableStateMapOf<String, String>()
        CategoryRegistry.expenseCategories.forEach { cat ->
            val found = existingBudgets.firstOrNull { it.category.equals(cat.name, ignoreCase = true) }
            if (found != null && found.limitAmount > 0) {
                map[cat.name] = found.limitAmount.toString()
            }
        }
        map
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("budget_settings_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Dialog Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Límites de Presupuesto",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = FinanceFormatters.formatMonthYearDisplay(currentMonthYear),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Overall Monthly Limit
                Text(
                    text = "Presupuesto General del Mes ($)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = totalLimitText,
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            totalLimitText = it
                        }
                    },
                    prefix = { Text("$ ") },
                    placeholder = { Text("Ej. 1800.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_total_budget_limit")
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Alert Threshold Slider
                Text(
                    text = "Umbral de Alerta Automática: ${(thresholdPercent * 100).toInt()}%",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Recibirás alertas preventivas cuando los gastos alcancen este porcentaje del presupuesto.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Slider(
                    value = thresholdPercent,
                    onValueChange = { thresholdPercent = it },
                    valueRange = 0.50f..0.95f,
                    steps = 8,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                // Category Budgets
                Text(
                    text = "Límites por Categoría de Gasto",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Define topes mensuales específicos para controlar áreas clave como Alimentación o Transporte.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                CategoryRegistry.expenseCategories.forEach { cat ->
                    val limitVal = categoryBudgetsMap[cat.name] ?: ""
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(cat.color.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = cat.icon,
                                    contentDescription = null,
                                    tint = cat.color,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = cat.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        OutlinedTextField(
                            value = limitVal,
                            onValueChange = { newVal ->
                                if (newVal.isEmpty() || newVal.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                                    if (newVal.isBlank()) {
                                        categoryBudgetsMap.remove(cat.name)
                                    } else {
                                        categoryBudgetsMap[cat.name] = newVal
                                    }
                                }
                            },
                            prefix = { Text("$", style = MaterialTheme.typography.bodySmall) },
                            placeholder = { Text("Sin límite", style = MaterialTheme.typography.bodySmall) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .width(130.dp)
                                .height(54.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Save button
                Button(
                    onClick = {
                        val resultBudgets = mutableListOf<BudgetEntity>()

                        // Total Budget
                        val totalLimit = totalLimitText.toDoubleOrNull() ?: 0.0
                        if (totalLimit > 0) {
                            resultBudgets.add(
                                BudgetEntity(
                                    id = overallBudget?.id ?: 0,
                                    monthYear = currentMonthYear,
                                    category = "TOTAL",
                                    limitAmount = totalLimit,
                                    alertThresholdPercent = thresholdPercent
                                )
                            )
                        }

                        // Category budgets
                        categoryBudgetsMap.forEach { (catName, limitStr) ->
                            val limit = limitStr.toDoubleOrNull() ?: 0.0
                            if (limit > 0) {
                                val existing = existingBudgets.firstOrNull { it.category == catName }
                                resultBudgets.add(
                                    BudgetEntity(
                                        id = existing?.id ?: 0,
                                        monthYear = currentMonthYear,
                                        category = catName,
                                        limitAmount = limit,
                                        alertThresholdPercent = thresholdPercent
                                    )
                                )
                            }
                        }

                        onSaveBudgets(resultBudgets)
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_save_budgets")
                ) {
                    Text(
                        text = "Guardar Presupuestos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
