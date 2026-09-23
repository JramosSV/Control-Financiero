package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.TransactionEntity
import com.example.data.entity.TransactionType
import com.example.ui.model.CategoryRegistry
import com.example.ui.model.FinanceFormatters
import com.example.ui.theme.BlueInfo
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.RoseExpense
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

enum class ChartType(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    DONUT("Por Categoría", Icons.Default.PieChart),
    BARS("Ingresos vs Gastos", Icons.Default.BarChart),
    TREND("Tendencia Temporal", Icons.Default.ShowChart)
}

data class CategorySlice(
    val category: String,
    val amount: Double,
    val percentage: Float,
    val color: Color,
    val count: Int,
    val startAngle: Float,
    val sweepAngle: Float
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InteractiveChartsSection(
    transactions: List<TransactionEntity>,
    modifier: Modifier = Modifier
) {
    var selectedChartType by remember { mutableStateOf(ChartType.DONUT) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("interactive_charts_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with title and subtle interaction tip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Análisis Interactivo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = "Toca para explorar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Toca los datos",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Chart Mode Selector
            TabRow(
                selectedTabIndex = selectedChartType.ordinal,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedChartType.ordinal]),
                        color = MaterialTheme.colorScheme.primary,
                        height = 3.dp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                ChartType.values().forEach { chart ->
                    Tab(
                        selected = selectedChartType == chart,
                        onClick = { selectedChartType = chart },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = chart.icon,
                                    contentDescription = chart.title,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = chart.title,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selectedChartType == chart) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chart Body based on selection
            AnimatedContent(
                targetState = selectedChartType,
                label = "ChartContentAnimation"
            ) { targetType ->
                when (targetType) {
                    ChartType.DONUT -> InteractiveCategoryDonutChart(transactions = transactions)
                    ChartType.BARS -> InteractiveIncomeExpenseBarChart(transactions = transactions)
                    ChartType.TREND -> InteractiveCashflowTrendChart(transactions = transactions)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InteractiveCategoryDonutChart(
    transactions: List<TransactionEntity>,
    modifier: Modifier = Modifier
) {
    val expenseTransactions = remember(transactions) {
        transactions.filter { it.type == TransactionType.EXPENSE }
    }
    val totalExpense = remember(expenseTransactions) {
        expenseTransactions.sumOf { it.amount }
    }

    if (totalExpense <= 0.0) {
        EmptyChartPlaceholder(message = "No hay egresos registrados en este periodo para graficar.")
        return
    }

    // Build slices
    val slices = remember(expenseTransactions, totalExpense) {
        val grouped = expenseTransactions.groupBy { it.category }
        var currentAngle = -90f
        grouped.map { (cat, list) ->
            val sum = list.sumOf { it.amount }
            val pct = (sum / totalExpense).toFloat()
            val sweep = pct * 360f
            val slice = CategorySlice(
                category = cat,
                amount = sum,
                percentage = pct,
                color = CategoryRegistry.getColorForCategory(cat),
                count = list.size,
                startAngle = currentAngle,
                sweepAngle = sweep
            )
            currentAngle += sweep
            slice
        }.sortedByDescending { it.amount }
    }

    var selectedSlice by remember { mutableStateOf<CategorySlice?>(null) }
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(slices) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Interactive Donut Canvas
        Box(
            modifier = Modifier
                .size(230.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(210.dp)
                    .pointerInput(slices) {
                        detectTapGestures { offset ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val touchVec = offset - center
                            val distance = touchVec.getDistance()
                            val innerRadius = (size.width / 2f) * 0.52f
                            val outerRadius = (size.width / 2f) * 0.98f

                            if (distance in innerRadius..outerRadius) {
                                // Calculate touch angle in [0..360] where -90 deg is top (0 deg)
                                var angle = (atan2(touchVec.y, touchVec.x) * 180f / PI).toFloat()
                                // Normalize angle to match startAngle (-90 to 270)
                                if (angle < -90f) angle += 360f

                                val clicked = slices.firstOrNull { slice ->
                                    val end = slice.startAngle + slice.sweepAngle
                                    angle >= slice.startAngle && angle < end
                                }
                                selectedSlice = if (selectedSlice == clicked) null else clicked
                            } else {
                                selectedSlice = null
                            }
                        }
                    }
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val strokeWidth = 36.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2f

                slices.forEach { slice ->
                    val isSelected = selectedSlice == slice
                    val scale = if (isSelected) 1.08f else 1.0f
                    val currentStroke = if (isSelected) strokeWidth * 1.15f else strokeWidth

                    drawArc(
                        color = slice.color,
                        startAngle = slice.startAngle,
                        sweepAngle = slice.sweepAngle * animationProgress.value,
                        useCenter = false,
                        topLeft = Offset(
                            center.x - radius * scale,
                            center.y - radius * scale
                        ),
                        size = Size(radius * 2f * scale, radius * 2f * scale),
                        style = Stroke(width = currentStroke, cap = StrokeCap.Round)
                    )
                }
            }

            // Center details (Dynamic based on selected slice or total)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                if (selectedSlice != null) {
                    val slice = selectedSlice!!
                    Text(
                        text = slice.category,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = slice.color,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    Text(
                        text = FinanceFormatters.formatMoney(slice.amount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "${(slice.percentage * 100).toInt()}% del total",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = "Total Egresos",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = FinanceFormatters.formatMoney(totalExpense),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = RoseExpense,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "${slices.size} categorías",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Interactive Categories Legend Chips
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            slices.forEach { slice ->
                val isSelected = selectedSlice == slice
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSelected) slice.color.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                        .border(
                            width = if (isSelected) 1.5.dp else 0.5.dp,
                            color = if (isSelected) slice.color else Color.Transparent,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable {
                            selectedSlice = if (selectedSlice == slice) null else slice
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(slice.color)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = slice.category,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${(slice.percentage * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

data class WeeklyComparison(
    val label: String,
    val income: Double,
    val expense: Double
)

@Composable
fun InteractiveIncomeExpenseBarChart(
    transactions: List<TransactionEntity>,
    modifier: Modifier = Modifier
) {
    if (transactions.isEmpty()) {
        EmptyChartPlaceholder(message = "No hay transacciones para comparar ingresos y egresos.")
        return
    }

    // Group transactions into 4 weekly brackets
    val weeksData = remember(transactions) {
        val week1 = transactions.filter {
            val cal = java.util.Calendar.getInstance().apply { timeInMillis = it.dateMillis }
            cal.get(java.util.Calendar.DAY_OF_MONTH) in 1..7
        }
        val week2 = transactions.filter {
            val cal = java.util.Calendar.getInstance().apply { timeInMillis = it.dateMillis }
            cal.get(java.util.Calendar.DAY_OF_MONTH) in 8..14
        }
        val week3 = transactions.filter {
            val cal = java.util.Calendar.getInstance().apply { timeInMillis = it.dateMillis }
            cal.get(java.util.Calendar.DAY_OF_MONTH) in 15..21
        }
        val week4 = transactions.filter {
            val cal = java.util.Calendar.getInstance().apply { timeInMillis = it.dateMillis }
            cal.get(java.util.Calendar.DAY_OF_MONTH) >= 22
        }

        listOf(
            WeeklyComparison("Sem 1 (1-7)", week1.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }, week1.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }),
            WeeklyComparison("Sem 2 (8-14)", week2.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }, week2.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }),
            WeeklyComparison("Sem 3 (15-21)", week3.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }, week3.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }),
            WeeklyComparison("Sem 4 (22+)", week4.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }, week4.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount })
        )
    }

    val maxAmount = remember(weeksData) {
        val maxVal = weeksData.flatMap { listOf(it.income, it.expense) }.maxOrNull() ?: 1.0
        if (maxVal <= 0.0) 100.0 else maxVal * 1.15
    }

    var selectedWeek by remember { mutableStateOf<WeeklyComparison?>(weeksData.firstOrNull()) }
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(weeksData) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Legend and Selected Tooltip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(EmeraldPrimary))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ingresos", style = MaterialTheme.typography.labelSmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(RoseExpense))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Gastos", style = MaterialTheme.typography.labelSmall)
                }
            }

            if (selectedWeek != null) {
                val net = selectedWeek!!.income - selectedWeek!!.expense
                Text(
                    text = "Neto: ${if (net >= 0) "+" else ""}${FinanceFormatters.formatMoney(net)}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (net >= 0) EmeraldPrimary else RoseExpense
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Canvas for animated dual bars
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .pointerInput(weeksData) {
                    detectTapGestures { offset ->
                        val barGroupWidth = size.width / weeksData.size
                        val index = (offset.x / barGroupWidth).toInt().coerceIn(0, weeksData.size - 1)
                        selectedWeek = weeksData[index]
                    }
                }
        ) {
            val width = size.width
            val height = size.height - 30.dp.toPx()
            val groupWidth = width / weeksData.size
            val barWidth = 16.dp.toPx()
            val cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())

            // Draw horizontal grid lines
            for (i in 1..3) {
                val y = height * (1f - i / 4f)
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.35f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            weeksData.forEachIndexed { i, week ->
                val groupCenterX = groupWidth * i + groupWidth / 2f
                val incomeBarX = groupCenterX - barWidth - 3.dp.toPx()
                val expenseBarX = groupCenterX + 3.dp.toPx()

                val incomeHeight = ((week.income / maxAmount) * height * animProgress.value).toFloat()
                val expenseHeight = ((week.expense / maxAmount) * height * animProgress.value).toFloat()

                val isSelected = selectedWeek == week

                // Highlight background column for selected week
                if (isSelected) {
                    drawRoundRect(
                        color = Color.Gray.copy(alpha = 0.12f),
                        topLeft = Offset(groupWidth * i + 4.dp.toPx(), 0f),
                        size = Size(groupWidth - 8.dp.toPx(), height + 24.dp.toPx()),
                        cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                    )
                }

                // Draw Income Bar
                drawRoundRect(
                    color = if (isSelected) EmeraldPrimary else EmeraldPrimary.copy(alpha = 0.8f),
                    topLeft = Offset(incomeBarX, height - incomeHeight),
                    size = Size(barWidth, incomeHeight.coerceAtLeast(4.dp.toPx())),
                    cornerRadius = cornerRadius
                )

                // Draw Expense Bar
                drawRoundRect(
                    color = if (isSelected) RoseExpense else RoseExpense.copy(alpha = 0.8f),
                    topLeft = Offset(expenseBarX, height - expenseHeight),
                    size = Size(barWidth, expenseHeight.coerceAtLeast(4.dp.toPx())),
                    cornerRadius = cornerRadius
                )
            }
        }

        // Labels under bars
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            weeksData.forEach { week ->
                val isSelected = selectedWeek == week
                Text(
                    text = week.label.substringBefore(" "),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { selectedWeek = week }
                )
            }
        }

        // Detail Tooltip below chart
        if (selectedWeek != null) {
            val week = selectedWeek!!
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = week.label,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "+${FinanceFormatters.formatMoney(week.income)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                        Text(
                            text = "-${FinanceFormatters.formatMoney(week.expense)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = RoseExpense
                        )
                    }
                }
            }
        }
    }
}

data class DayPoint(
    val day: Int,
    val balance: Double,
    val expense: Double
)

@Composable
fun InteractiveCashflowTrendChart(
    transactions: List<TransactionEntity>,
    modifier: Modifier = Modifier
) {
    if (transactions.isEmpty()) {
        EmptyChartPlaceholder(message = "No hay datos suficientes para mostrar la curva de tendencia.")
        return
    }

    // Aggregate cumulative balance day-by-day (1 to 30)
    val points = remember(transactions) {
        val sorted = transactions.sortedBy { it.dateMillis }
        val dayGroups = (1..30).map { day ->
            val dayTxs = sorted.filter {
                val cal = java.util.Calendar.getInstance().apply { timeInMillis = it.dateMillis }
                cal.get(java.util.Calendar.DAY_OF_MONTH) <= day
            }
            val totalIn = dayTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val totalEx = dayTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

            val todayEx = sorted.filter {
                val cal = java.util.Calendar.getInstance().apply { timeInMillis = it.dateMillis }
                cal.get(java.util.Calendar.DAY_OF_MONTH) == day && it.type == TransactionType.EXPENSE
            }.sumOf { it.amount }

            DayPoint(day, totalIn - totalEx, todayEx)
        }
        dayGroups
    }

    val minBalance = remember(points) { points.minOfOrNull { it.balance } ?: 0.0 }
    val maxBalance = remember(points) {
        val maxVal = points.maxOfOrNull { it.balance } ?: 100.0
        if (maxVal == minBalance) maxVal + 100.0 else maxVal
    }

    var selectedDayIndex by remember { mutableStateOf(points.lastIndex.coerceAtLeast(0)) }
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(points) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Current Scrubber State
        val currentPoint = points.getOrNull(selectedDayIndex) ?: points.last()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Día ${currentPoint.day} del mes",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Balance acumulado: ${FinanceFormatters.formatMoney(currentPoint.balance)}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (currentPoint.balance >= 0) EmeraldPrimary else RoseExpense
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Line and Gradient Area Canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .pointerInput(points) {
                    detectTapGestures { offset ->
                        val stepX = size.width / (points.size - 1).coerceAtLeast(1)
                        val idx = (offset.x / stepX).toInt().coerceIn(0, points.size - 1)
                        selectedDayIndex = idx
                    }
                }
        ) {
            val width = size.width
            val height = size.height - 20.dp.toPx()
            val stepX = width / (points.size - 1).coerceAtLeast(1)
            val range = (maxBalance - minBalance).toFloat().coerceAtLeast(1f)

            val path = Path()
            val fillPath = Path()

            points.forEachIndexed { i, pt ->
                val x = i * stepX
                val normalizedY = ((pt.balance - minBalance).toFloat() / range) * animProgress.value
                val y = height - (normalizedY * height)

                if (i == 0) {
                    path.moveTo(x, y)
                    fillPath.moveTo(x, height)
                    fillPath.lineTo(x, y)
                } else {
                    path.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
            }

            fillPath.lineTo(width, height)
            fillPath.close()

            // Draw Area Fill Gradient
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        BlueInfo.copy(alpha = 0.35f),
                        BlueInfo.copy(alpha = 0.02f)
                    ),
                    startY = 0f,
                    endY = height
                )
            )

            // Draw Curve Line
            drawPath(
                path = path,
                color = BlueInfo,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw Scrubber Indicator
            val activeX = selectedDayIndex * stepX
            val activeNormalizedY = ((currentPoint.balance - minBalance).toFloat() / range) * animProgress.value
            val activeY = height - (activeNormalizedY * height)

            // Vertical line
            drawLine(
                color = BlueInfo.copy(alpha = 0.5f),
                start = Offset(activeX, 0f),
                end = Offset(activeX, height),
                strokeWidth = 1.5.dp.toPx()
            )

            // Point dot
            drawCircle(
                color = Color.White,
                radius = 7.dp.toPx(),
                center = Offset(activeX, activeY)
            )
            drawCircle(
                color = BlueInfo,
                radius = 5.dp.toPx(),
                center = Offset(activeX, activeY)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Día 1", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Día 15", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Día 30", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun EmptyChartPlaceholder(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
