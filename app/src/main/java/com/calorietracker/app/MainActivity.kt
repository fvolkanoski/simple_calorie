package com.calorietracker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.calorietracker.app.data.CalorieStore
import com.calorietracker.app.ui.CalorieTrackerApp
import com.calorietracker.app.ui.theme.CalorieTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val store = CalorieStore(applicationContext)
        setContent {
            CalorieTrackerTheme {
                CalorieTrackerApp(store = store)
            }
        }
    }
}
