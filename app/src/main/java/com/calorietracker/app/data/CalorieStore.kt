package com.calorietracker.app.data

import android.content.Context
import com.calorietracker.app.model.FoodEntry
import com.calorietracker.app.model.Goals
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Simple on-device persistence. Everything is stored in SharedPreferences as
 * JSON strings - no external database dependency, no network calls.
 * All data stays on the phone.
 */
class CalorieStore(context: Context) {
    private val prefs = context.getSharedPreferences("calorie_tracker", Context.MODE_PRIVATE)

    fun todayKey(offsetDays: Int = 0): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, offsetDays)
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return fmt.format(cal.time)
    }

    fun loadGoals(): Goals {
        val raw = prefs.getString("goals", null) ?: return Goals()
        return try {
            val o = JSONObject(raw)
            Goals(
                calories = o.optInt("calories", 2200),
                protein = o.optInt("protein", 140),
                carbs = o.optInt("carbs", 250),
                fat = o.optInt("fat", 70)
            )
        } catch (e: Exception) {
            Goals()
        }
    }

    fun saveGoals(goals: Goals) {
        val o = JSONObject()
        o.put("calories", goals.calories)
        o.put("protein", goals.protein)
        o.put("carbs", goals.carbs)
        o.put("fat", goals.fat)
        prefs.edit().putString("goals", o.toString()).apply()
    }

    fun loadEntries(dateKey: String): List<FoodEntry> {
        val raw = prefs.getString("entries:$dateKey", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                FoodEntry(
                    id = o.getLong("id"),
                    name = o.getString("name"),
                    meal = o.getString("meal"),
                    calories = o.getInt("calories"),
                    protein = o.optInt("protein", 0),
                    carbs = o.optInt("carbs", 0),
                    fat = o.optInt("fat", 0)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveEntries(dateKey: String, entries: List<FoodEntry>) {
        val arr = JSONArray()
        for (e in entries) {
            val o = JSONObject()
            o.put("id", e.id)
            o.put("name", e.name)
            o.put("meal", e.meal)
            o.put("calories", e.calories)
            o.put("protein", e.protein)
            o.put("carbs", e.carbs)
            o.put("fat", e.fat)
            arr.put(o)
        }
        prefs.edit().putString("entries:$dateKey", arr.toString()).apply()

        val total = entries.sumOf { it.calories }
        val history = loadHistory().toMutableMap()
        history[dateKey] = total
        saveHistory(history)
    }

    fun loadHistory(): Map<String, Int> {
        val raw = prefs.getString("history", null) ?: return emptyMap()
        return try {
            val o = JSONObject(raw)
            val map = mutableMapOf<String, Int>()
            val keys = o.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                map[k] = o.getInt(k)
            }
            map
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun saveHistory(history: Map<String, Int>) {
        val o = JSONObject()
        for ((k, v) in history) o.put(k, v)
        prefs.edit().putString("history", o.toString()).apply()
    }
}
