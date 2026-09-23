package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.entity.BudgetEntity
import com.example.data.entity.TransactionEntity
import com.example.data.entity.TransactionType
import com.example.ui.components.MainBalanceCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val dummyTransactions = listOf(
      TransactionEntity(
        id = 1,
        title = "Nómina",
        amount = 1800.0,
        type = TransactionType.INCOME,
        category = "Salario",
        dateMillis = System.currentTimeMillis(),
        monthYear = "2026-09"
      ),
      TransactionEntity(
        id = 2,
        title = "Supermercado",
        amount = 350.0,
        type = TransactionType.EXPENSE,
        category = "Alimentación",
        dateMillis = System.currentTimeMillis(),
        monthYear = "2026-09"
      )
    )
    val dummyBudgets = listOf(
      BudgetEntity(
        id = 1,
        monthYear = "2026-09",
        category = "TOTAL",
        limitAmount = 1500.0
      )
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        MainBalanceCard(
          transactions = dummyTransactions,
          budgets = dummyBudgets
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}

