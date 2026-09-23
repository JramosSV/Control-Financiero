package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "budgets",
    indices = [Index(value = ["monthYear", "category"], unique = true)]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val monthYear: String, // e.g. "2026-09"
    val category: String,  // "TOTAL" for overall budget, or specific category name
    val limitAmount: Double,
    val alertThresholdPercent: Float = 0.80f // 80% default threshold
)
