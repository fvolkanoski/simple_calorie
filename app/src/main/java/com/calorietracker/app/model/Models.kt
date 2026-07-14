package com.calorietracker.app.model

data class FoodEntry(
    val id: Long,
    val name: String,
    val meal: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int
)

data class Goals(
    val calories: Int = 2200,
    val protein: Int = 140,
    val carbs: Int = 250,
    val fat: Int = 70
)
