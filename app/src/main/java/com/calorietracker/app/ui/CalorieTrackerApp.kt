package com.calorietracker.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calorietracker.app.data.CalorieStore
import com.calorietracker.app.model.FoodEntry
import com.calorietracker.app.model.Goals
import com.calorietracker.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

private data class MealDef(val id: String, val label: String, val icon: ImageVector)

private val MEALS = listOf(
    MealDef("breakfast", "Breakfast", Icons.Filled.LocalCafe),
    MealDef("lunch", "Lunch", Icons.Filled.WbSunny),
    MealDef("dinner", "Dinner", Icons.Filled.NightsStay),
    MealDef("snack", "Snack", Icons.Filled.Fastfood)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalorieTrackerApp(store: CalorieStore) {
    var tab by remember { mutableStateOf("today") }
    var dayOffset by remember { mutableStateOf(0) }
    var goals by remember { mutableStateOf(store.loadGoals()) }
    var history by remember { mutableStateOf(store.loadHistory()) }
    val dateKey = remember(dayOffset) { store.todayKey(dayOffset) }
    var entries by remember(dateKey) { mutableStateOf(store.loadEntries(dateKey)) }
    var showAdd by remember { mutableStateOf(false) }

    fun persistEntries(list: List<FoodEntry>) {
        entries = list
        store.saveEntries(dateKey, list)
        history = store.loadHistory()
    }

    Scaffold(
        containerColor = Bg,
        bottomBar = {
            NavigationBar(containerColor = Surface, contentColor = InkSoft) {
                NavigationBarItem(
                    selected = tab == "today",
                    onClick = { tab = "today" },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Today") },
                    label = { Text("Today") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Primary,
                        selectedTextColor = Primary,
                        indicatorColor = PrimaryLight
                    )
                )
                NavigationBarItem(
                    selected = tab == "progress",
                    onClick = { tab = "progress" },
                    icon = { Icon(Icons.Filled.ShowChart, contentDescription = "Progress") },
                    label = { Text("Progress") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Primary,
                        selectedTextColor = Primary,
                        indicatorColor = PrimaryLight
                    )
                )
                NavigationBarItem(
                    selected = tab == "goals",
                    onClick = { tab = "goals" },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Goals") },
                    label = { Text("Goals") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Primary,
                        selectedTextColor = Primary,
                        indicatorColor = PrimaryLight
                    )
                )
            }
        },
        floatingActionButton = {
            if (tab == "today" && dayOffset == 0) {
                FloatingActionButton(
                    onClick = { showAdd = true },
                    containerColor = Ink,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add food")
                }
            }
        }
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (tab) {
                "today" -> TodayScreen(
                    dateLabel = prettyDate(dateKey, store),
                    entries = entries,
                    goals = goals,
                    dayOffset = dayOffset,
                    onPrev = { dayOffset -= 1 },
                    onNext = { if (dayOffset < 0) dayOffset += 1 },
                    onDelete = { id -> persistEntries(entries.filter { it.id != id }) }
                )
                "progress" -> ProgressScreen(goals = goals, history = history, store = store)
                "goals" -> GoalsScreen(goals = goals, onSave = { g -> goals = g; store.saveGoals(g) })
            }

            if (showAdd) {
                AddFoodSheet(
                    onDismiss = { showAdd = false },
                    onAdd = { entry ->
                        persistEntries(entries + entry)
                        showAdd = false
                    }
                )
            }
        }
    }
}

private fun prettyDate(key: String, store: CalorieStore): String {
    return when (key) {
        store.todayKey(0) -> "Today"
        store.todayKey(-1) -> "Yesterday"
        else -> key
    }
}

private fun greeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 5 -> "Still up?"
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        hour < 21 -> "Good evening"
        else -> "Good night"
    }
}

@Composable
private fun TodayScreen(
    dateLabel: String,
    entries: List<FoodEntry>,
    goals: Goals,
    dayOffset: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onDelete: (Long) -> Unit
) {
    val consumed = entries.sumOf { it.calories }
    val proteinTotal = entries.sumOf { it.protein }
    val carbsTotal = entries.sumOf { it.carbs }
    val fatTotal = entries.sumOf { it.fat }
    val remaining = goals.calories - consumed
    val over = remaining < 0
    val pct = if (goals.calories > 0) consumed.toFloat() / goals.calories else 0f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
    ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(greeting(), fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onPrev, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous day", tint = InkSoft)
                        }
                        Text(dateLabel, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = InkSoft)
                        IconButton(onClick = onNext, modifier = Modifier.size(28.dp), enabled = dayOffset < 0) {
                            Icon(
                                Icons.Filled.ChevronRight,
                                contentDescription = "Next day",
                                tint = if (dayOffset < 0) InkSoft else Line
                            )
                        }
                    }
                }
                Box(
                    Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PrimaryLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = Primary)
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Surface)
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.Center) {
                    RingProgress(pct = pct, over = over)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${abs(remaining)}",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 30.sp,
                            color = if (over) Danger else Ink
                        )
                        Text(
                            if (over) "kcal over" else "kcal left",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = InkSoft
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "$consumed eaten  ·  ${goals.calories} goal",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = InkSoft
                )
            }
            Spacer(Modifier.height(16.dp))
        }

        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Surface)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MacroBar("Protein", proteinTotal, goals.protein, Primary)
                MacroBar("Carbs", carbsTotal, goals.carbs, Accent)
                MacroBar("Fat", fatTotal, goals.fat, Danger)
            }
            Spacer(Modifier.height(20.dp))
            Text("Today's log", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Ink)
            Spacer(Modifier.height(8.dp))
        }

        if (entries.isEmpty()) {
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Surface)
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Nothing logged yet — tap + to add your first meal.",
                        color = InkSoft,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        MEALS.forEach { meal ->
            val mealItems = entries.filter { it.meal == meal.id }
            if (mealItems.isNotEmpty()) {
                item {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(meal.icon, contentDescription = null, tint = InkSoft, modifier = Modifier.size(14.dp))
                            Text(
                                meal.label.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink,
                                letterSpacing = 0.6.sp
                            )
                        }
                        Text(
                            "${mealItems.sumOf { it.calories }} kcal",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = InkSoft
                        )
                    }
                    HorizontalDivider(color = Ink, thickness = 1.5.dp)
                }
                items(mealItems, key = { it.id }) { e ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(e.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Ink)
                            Text(
                                "P${e.protein} · C${e.carbs} · F${e.fat}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = InkSoft
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "${e.calories}",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = Ink
                            )
                            IconButton(onClick = { onDelete(e.id) }, modifier = Modifier.size(28.dp)) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Delete",
                                    tint = InkSoft,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = Line, thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
private fun RingProgress(pct: Float, over: Boolean, sizeDp: Dp = 176.dp, strokeDp: Dp = 14.dp) {
    val clamped = pct.coerceIn(0f, 1f)
    val color = if (over) Danger else Primary
    Canvas(modifier = Modifier.size(sizeDp)) {
        val strokePx = strokeDp.toPx()
        val diameter = size.minDimension
        val topLeft = androidx.compose.ui.geometry.Offset(
            (size.width - diameter) / 2f + strokePx / 2f,
            (size.height - diameter) / 2f + strokePx / 2f
        )
        val arcSize = Size(diameter - strokePx, diameter - strokePx)
        drawArc(
            color = Line,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokePx, cap = StrokeCap.Round)
        )
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360f * clamped,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokePx, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun MacroBar(label: String, value: Int, goal: Int, color: Color) {
    val pct = if (goal > 0) (value.toFloat() / goal).coerceIn(0f, 1f) else 0f
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.width(58.dp), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Ink)
        Box(
            Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(Line)
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(pct)
                    .clip(RoundedCornerShape(50))
                    .background(color)
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            "$value/${goal}g",
            modifier = Modifier.width(68.dp),
            textAlign = TextAlign.End,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = InkSoft
        )
    }
}

@Composable
private fun ProgressScreen(goals: Goals, history: Map<String, Int>, store: CalorieStore) {
    val days = remember(history) {
        (6 downTo 0).map { i ->
            val key = store.todayKey(-i)
            key to (history[key] ?: 0)
        }
    }
    val maxWeek = max(goals.calories, days.maxOfOrNull { it.second } ?: 0).coerceAtLeast(1)
    val avg = if (days.isNotEmpty()) days.sumOf { it.second } / days.size else 0

    Column(
        Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text("Your week", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = Ink)
        Spacer(Modifier.height(16.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Surface)
                .padding(16.dp)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                days.forEach { (key, total) ->
                    val heightFraction = (total.toFloat() / maxWeek).coerceIn(0.02f, 1f)
                    val isToday = key == store.todayKey(0)
                    val overGoal = total > goals.calories
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(0.6f)
                                .fillMaxHeight(heightFraction)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    when {
                                        overGoal -> Danger
                                        isToday -> Primary
                                        else -> PrimaryLight
                                    }
                                )
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                days.forEach { (key, _) ->
                    Text(
                        dayLetter(key),
                        modifier = Modifier.weight(1f),
                        fontSize = 9.sp,
                        color = InkSoft,
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Surface)
                .padding(16.dp)
        ) {
            Text("7-day average", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Ink)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    "$avg",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 28.sp,
                    color = Primary
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "kcal/day",
                    fontSize = 13.sp,
                    color = InkSoft,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}

private fun dayLetter(key: String): String {
    return try {
        val parts = key.split("-").map { it.toInt() }
        val cal = Calendar.getInstance()
        cal.set(parts[0], parts[1] - 1, parts[2])
        val fmt = SimpleDateFormat("EEEEE", Locale.US)
        fmt.format(cal.time)
    } catch (e: Exception) {
        "?"
    }
}

@Composable
private fun GoalsScreen(goals: Goals, onSave: (Goals) -> Unit) {
    var calories by remember { mutableStateOf(goals.calories.toString()) }
    var protein by remember { mutableStateOf(goals.protein.toString()) }
    var carbs by remember { mutableStateOf(goals.carbs.toString()) }
    var fat by remember { mutableStateOf(goals.fat.toString()) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text("Daily goals", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = Ink)
        Spacer(Modifier.height(16.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Surface)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GoalField("Calories", "kcal", calories) { calories = it }
            GoalField("Protein", "g", protein) { protein = it }
            GoalField("Carbs", "g", carbs) { carbs = it }
            GoalField("Fat", "g", fat) { fat = it }
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                onSave(
                    Goals(
                        calories = calories.toIntOrNull() ?: goals.calories,
                        protein = protein.toIntOrNull() ?: goals.protein,
                        carbs = carbs.toIntOrNull() ?: goals.carbs,
                        fat = fat.toIntOrNull() ?: goals.fat
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Color.White),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Save goals", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "These targets shape your ring and macro bars on the Today screen.",
            fontSize = 12.sp,
            color = InkSoft,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun GoalField(label: String, unit: String, value: String, onChange: (String) -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Ink)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = value,
                onValueChange = { onChange(it.filter { c -> c.isDigit() }) },
                modifier = Modifier.width(90.dp),
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.End
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(Modifier.width(6.dp))
            Text(unit, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = InkSoft)
        }
    }
}

private fun defaultMeal(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 11 -> "breakfast"
        hour < 16 -> "lunch"
        hour < 21 -> "dinner"
        else -> "snack"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddFoodSheet(onDismiss: () -> Unit, onAdd: (FoodEntry) -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf("") }
    var meal by remember { mutableStateOf(defaultMeal()) }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }

    val canSave = name.isNotBlank() && (calories.toIntOrNull() ?: 0) > 0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Surface
    ) {
        Column(
            Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Add food", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = InkSoft)
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("What did you eat?") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MEALS.forEach { m ->
                    val active = meal == m.id
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (active) PrimaryLight else Bg)
                            .clickable { meal = m.id }
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            m.icon,
                            contentDescription = null,
                            tint = if (active) Primary else InkSoft,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            m.label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (active) Primary else InkSoft
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField("kcal", calories) { calories = it }
                NumberField("protein g", protein) { protein = it }
                NumberField("carbs g", carbs) { carbs = it }
                NumberField("fat g", fat) { fat = it }
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    onAdd(
                        FoodEntry(
                            id = System.currentTimeMillis(),
                            name = name.trim(),
                            meal = meal,
                            calories = calories.toIntOrNull() ?: 0,
                            protein = protein.toIntOrNull() ?: 0,
                            carbs = carbs.toIntOrNull() ?: 0,
                            fat = fat.toIntOrNull() ?: 0
                        )
                    )
                },
                enabled = canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Ink,
                    contentColor = Color.White,
                    disabledContainerColor = Line
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Add to log", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun RowScope.NumberField(label: String, value: String, onChange: (String) -> Unit) {
    Column(modifier = Modifier.weight(1f)) {
        OutlinedTextField(
            value = value,
            onValueChange = { onChange(it.filter { c -> c.isDigit() }) },
            placeholder = { Text("0") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = TextStyle(fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Text(label, fontSize = 9.sp, color = InkSoft, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}
