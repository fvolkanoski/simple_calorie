package com.calorietracker.app.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.calorietracker.app.MainActivity
import com.calorietracker.app.data.CalorieStore
import kotlin.math.abs

/**
 * Small home-screen widget showing today's remaining calories.
 * Reads from the same CalorieStore the app uses, so it always shows
 * whatever was last saved - no separate data path.
 */
class CalorieWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val store = CalorieStore(context)
        val goals = store.loadGoals()
        val entries = store.loadEntries(store.todayKey(0))
        val consumed = entries.sumOf { it.calories }
        val remaining = goals.calories - consumed
        val over = remaining < 0

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFF1B2B22))
                    .padding(12.dp)
                    .clickable(actionStartActivity<MainActivity>()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${abs(remaining)}",
                    style = TextStyle(
                        color = ColorProvider(if (over) Color(0xFFE8836B) else Color.White),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = if (over) "kcal over" else "kcal left",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFDCEBE2)),
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

/** Entry point Android calls when it wants the widget drawn or refreshed. */
class CalorieWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CalorieWidget()
}
