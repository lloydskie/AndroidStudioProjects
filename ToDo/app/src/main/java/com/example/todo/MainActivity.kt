package com.example.todo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.todo.ui.theme.ToDoTheme
import java.text.SimpleDateFormat
import java.util.*

// Data model now includes a stable id to map reminders to tasks and recurring task support

data class TodoItem(
    val title: String,
    val notes: String = "",
    val isDone: Boolean = false,
    val dueDate: String = "",
    val category: String = "Personal",
    val priority: String = "Medium",
    val id: String = UUID.randomUUID().toString(),
    val isRecurring: Boolean = false,
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val customInterval: Int = 1, // for custom intervals (e.g., every 3 days)
    val lastCompleted: String = "", // track when last completed for recurring tasks
    val originalId: String = "", // to link recurring instances to original task
    val createdDate: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
    val completedDate: String = "",
    val completionPercentage: Int = 0, // 0-100 for progress tracking
    val comments: List<TaskComment> = emptyList(),
    val attachments: List<TaskAttachment> = emptyList(),
    val streak: Int = 0 // for recurring tasks
)

data class TaskComment(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val timestamp: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
)

data class TaskAttachment(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: String, // "image", "document", "link"
    val uri: String,
    val timestamp: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
)

data class ActivityLogEntry(
    val id: String = UUID.randomUUID().toString(),
    val taskId: String,
    val taskTitle: String,
    val action: String, // "created", "completed", "updated", "deleted", "comment_added", etc.
    val details: String = "",
    val timestamp: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
)

enum class RecurrenceType {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY,
    CUSTOM_DAYS
}

enum class ViewMode {
    LIST,
    CALENDAR
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ToDoTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun EnsureNotificationPermission() {
    val context = LocalContext.current
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permission = Manifest.permission.POST_NOTIFICATIONS
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { /* no-op */ }
        LaunchedEffect(Unit) {
            val granted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
            if (!granted) launcher.launch(permission)
        }
    }
}

@Composable
fun MainScreen() {
    EnsureNotificationPermission()
    val context = LocalContext.current

    var todos by remember { mutableStateOf(listOf<TodoItem>()) }
    var activityLog by remember { mutableStateOf(listOf<ActivityLogEntry>()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showActivityDialog by remember { mutableStateOf(false) }
    var showTaskDetailsDialog by remember { mutableStateOf(false) }
    var editingTodo by remember { mutableStateOf<TodoItem?>(null) }
    var selectedTodoForDetails by remember { mutableStateOf<TodoItem?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedPriority by remember { mutableStateOf("All") }
    var showCompleted by remember { mutableStateOf(true) }
    var viewMode by remember { mutableStateOf(ViewMode.LIST) }
    val systemDark = isSystemInDarkTheme()
    var isDarkTheme by remember { mutableStateOf(systemDark) }

    // Filter logic
    val filteredTodos = remember(todos, searchQuery, selectedCategory, selectedPriority, showCompleted) {
        todos.filter { todo ->
            val matchesSearch = searchQuery.isBlank() ||
                todo.title.contains(searchQuery, ignoreCase = true) ||
                todo.notes.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == "All" || todo.category == selectedCategory
            val matchesPriority = selectedPriority == "All" || todo.priority == selectedPriority
            val matchesCompletion = showCompleted || !todo.isDone

            matchesSearch && matchesCategory && matchesPriority && matchesCompletion
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF4A90E2)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top App Bar with theme toggle and view mode
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Tasks",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    // Theme Toggle
                    IconButton(onClick = { isDarkTheme = !isDarkTheme }) {
                        Icon(
                            if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = "Toggle theme"
                        )
                    }
                    // Activity Log Button
                    IconButton(onClick = { showActivityDialog = true }) {
                        Icon(Icons.Filled.History, contentDescription = "Activity Log")
                    }
                    // View Mode Toggle
                    IconButton(
                        onClick = {
                            viewMode = if (viewMode == ViewMode.LIST) ViewMode.CALENDAR else ViewMode.LIST
                        }
                    ) {
                        Icon(
                            if (viewMode == ViewMode.LIST) Icons.Filled.CalendarToday else Icons.AutoMirrored.Filled.List,
                            contentDescription = "Toggle view mode"
                        )
                    }
                }
            }

            // Progress Overview
            if (todos.isNotEmpty()) {
                ProgressOverview(todos = todos)
            }

            // Search and Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search tasks...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                    modifier = Modifier.weight(1f),
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(
                                onClick = { searchQuery = "" }
                            ) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { showFilterDialog = true },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Filled.FilterList,
                        contentDescription = "Filter",
                        tint = if (selectedCategory != "All" || selectedPriority != "All" || !showCompleted)
                            Color(0xFF4A90E2) else Color.Gray
                    )
                }
            }

            // Active filters display
            if (selectedCategory != "All" || selectedPriority != "All" || !showCompleted) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (selectedCategory != "All") {
                        item {
                            FilterChip(
                                onClick = { selectedCategory = "All" },
                                label = { Text("Category: $selectedCategory") },
                                selected = true,
                                trailingIcon = { Icon(Icons.Filled.Clear, contentDescription = "Remove filter") }
                            )
                        }
                    }
                    if (selectedPriority != "All") {
                        item {
                            FilterChip(
                                onClick = { selectedPriority = "All" },
                                label = { Text("Priority: $selectedPriority") },
                                selected = true,
                                trailingIcon = { Icon(Icons.Filled.Clear, contentDescription = "Remove filter") }
                            )
                        }
                    }
                    if (!showCompleted) {
                        item {
                            FilterChip(
                                onClick = { showCompleted = true },
                                label = { Text("Hide Completed") },
                                selected = true,
                                trailingIcon = { Icon(Icons.Filled.Clear, contentDescription = "Remove filter") }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Content based on view mode
            when (viewMode) {
                ViewMode.LIST -> {
                    // Todo List
                    if (filteredTodos.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (todos.isEmpty()) "No tasks yet. Tap the + button to add your first task!"
                                       else "No tasks match your search or filters.",
                                color = Color(0xFFBDBDBD),
                                fontSize = 18.sp,
                                lineHeight = 24.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            itemsIndexed(filteredTodos) { index, todo ->
                                EnhancedTodoDetailScreen(
                                    todo = todo,
                                    onEdit = {
                                        editingTodo = todo
                                        showEditDialog = true
                                    },
                                    onToggleComplete = {
                                        val updatedTodos = handleTaskCompletion(todos, todo, context)
                                        todos = updatedTodos
                                        // Log activity
                                        val action = if (todo.isDone) "uncompleted" else "completed"
                                        activityLog = activityLog + ActivityLogEntry(
                                            taskId = todo.id,
                                            taskTitle = todo.title,
                                            action = action,
                                            details = "Task $action by user"
                                        )
                                    },
                                    onViewDetails = {
                                        selectedTodoForDetails = todo
                                        showTaskDetailsDialog = true
                                    }
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
                ViewMode.CALENDAR -> {
                    CalendarView(todos = filteredTodos)
                }
            }
        }

        // Dialogs
        if (showAddDialog) {
            AddTodoDialog(
                onAdd = { title: String, notes: String, dueDate: String, category: String, priority: String, isRecurring: Boolean, recurrenceType: RecurrenceType, customInterval: Int ->
                    val newItem = TodoItem(title = title, notes = notes, dueDate = dueDate, category = category, priority = priority, isRecurring = isRecurring, recurrenceType = recurrenceType, customInterval = customInterval)
                    todos = todos + newItem
                    // Log activity
                    activityLog = activityLog + ActivityLogEntry(
                        taskId = newItem.id,
                        taskTitle = newItem.title,
                        action = "created",
                        details = "New task created with category: $category, priority: $priority"
                    )
                    if (!newItem.isDone && newItem.dueDate.isNotBlank()) {
                        ReminderScheduler.schedule(context, newItem.id, newItem.title, newItem.notes, newItem.dueDate)
                    }
                    showAddDialog = false
                },
                onDismiss = { showAddDialog = false }
            )
        }

        if (showEditDialog && editingTodo != null) {
            val todo = editingTodo!!
            EditTodoDialog(
                initialTitle = todo.title,
                initialNotes = todo.notes,
                initialDueDate = todo.dueDate,
                initialCategory = todo.category,
                initialPriority = todo.priority,
                onEdit = { newTitle: String, newNotes: String, newDueDate: String, newCategory: String, newPriority: String ->
                    todos = todos.map {
                        if (it.id == todo.id) {
                            val updated = it.copy(title = newTitle, notes = newNotes, dueDate = newDueDate, category = newCategory, priority = newPriority)
                            ReminderScheduler.cancel(context, todo.id)
                            if (!updated.isDone && updated.dueDate.isNotBlank()) {
                                ReminderScheduler.schedule(context, updated.id, updated.title, updated.notes, updated.dueDate)
                            }
                            // Log activity
                            activityLog = activityLog + ActivityLogEntry(
                                taskId = todo.id,
                                taskTitle = newTitle,
                                action = "updated",
                                details = "Task details modified"
                            )
                            updated
                        } else it
                    }
                    showEditDialog = false
                    editingTodo = null
                },
                onDelete = {
                    ReminderScheduler.cancel(context, todo.id)
                    todos = todos.filter { it.id != todo.id }
                    // Log activity
                    activityLog = activityLog + ActivityLogEntry(
                        taskId = todo.id,
                        taskTitle = todo.title,
                        action = "deleted",
                        details = "Task permanently deleted"
                    )
                    showEditDialog = false
                    editingTodo = null
                },
                onDismiss = {
                    showEditDialog = false
                    editingTodo = null
                }
            )
        }

        if (showFilterDialog) {
            FilterDialog(
                currentCategory = selectedCategory,
                currentPriority = selectedPriority,
                showCompleted = showCompleted,
                onCategoryChange = { selectedCategory = it },
                onPriorityChange = { selectedPriority = it },
                onShowCompletedChange = { showCompleted = it },
                onDismiss = { showFilterDialog = false }
            )
        }

        if (showActivityDialog) {
            ActivityLogDialog(
                activityLog = activityLog,
                onDismiss = { showActivityDialog = false }
            )
        }

        if (showTaskDetailsDialog && selectedTodoForDetails != null) {
            TaskDetailsDialog(
                todo = selectedTodoForDetails!!,
                onDismiss = {
                    showTaskDetailsDialog = false
                    selectedTodoForDetails = null
                },
                onAddComment = { comment ->
                    todos = todos.map {
                        if (it.id == selectedTodoForDetails!!.id) {
                            val newComment = TaskComment(text = comment)
                            val updated = it.copy(comments = it.comments + newComment)
                            // Log activity
                            activityLog = activityLog + ActivityLogEntry(
                                taskId = it.id,
                                taskTitle = it.title,
                                action = "comment_added",
                                details = "Comment added: ${comment.take(50)}${if (comment.length > 50) "..." else ""}"
                            )
                            updated
                        } else it
                    }
                    selectedTodoForDetails = todos.find { it.id == selectedTodoForDetails!!.id }
                },
                onUpdateProgress = { percentage ->
                    todos = todos.map {
                        if (it.id == selectedTodoForDetails!!.id) {
                            val updated = it.copy(completionPercentage = percentage)
                            // Log activity
                            activityLog = activityLog + ActivityLogEntry(
                                taskId = it.id,
                                taskTitle = it.title,
                                action = "progress_updated",
                                details = "Progress updated to $percentage%"
                            )
                            updated
                        } else it
                    }
                    selectedTodoForDetails = todos.find { it.id == selectedTodoForDetails!!.id }
                }
            )
        }
    }
}

@Suppress("unused")
@Composable
fun TodoDetailScreen(
    todo: TodoItem,
    onEdit: () -> Unit,
    onToggleComplete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp, start = 24.dp, end = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = todo.isDone,
                onCheckedChange = { onToggleComplete() },
                modifier = Modifier.size(32.dp),
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF4A90E2),
                    uncheckedColor = Color(0xFF4A90E2),
                    checkmarkColor = Color.White
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = todo.title,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF222222)
            )
            // Recurring indicator
            if (todo.isRecurring && todo.recurrenceType != RecurrenceType.NONE) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "🔄",
                    fontSize = 20.sp,
                    color = Color(0xFF4A90E2)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                Text(
                    text = todo.category,
                    color = Color(0xFF4A90E2),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
                Text(
                    text = todo.priority,
                    color = when (todo.priority) {
                        "High" -> Color.Red
                        "Medium" -> Color(0xFFFFA500)
                        else -> Color(0xFF4CAF50)
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "More",
                    tint = Color(0xFFBDBDBD)
                )
            }
        }
        if (todo.dueDate.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Due: ${todo.dueDate}",
                color = Color(0xFFBDBDBD),
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        if (todo.isRecurring && todo.recurrenceType != RecurrenceType.NONE) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Repeats: ${getRecurrenceDisplay(todo.recurrenceType, todo.customInterval)}",
                color = Color(0xFF4A90E2),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Notes",
            color = Color(0xFFBDBDBD),
            fontSize = 18.sp,
            modifier = Modifier.padding(start = 8.dp, top = 8.dp)
        )
        if (todo.notes.isNotBlank()) {
            Text(
                text = todo.notes,
                color = Color(0xFF757575),
                fontSize = 18.sp,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }
    }
}

fun getRecurrenceDisplay(recurrenceType: RecurrenceType, customInterval: Int): String {
    return when (recurrenceType) {
        RecurrenceType.DAILY -> "Daily"
        RecurrenceType.WEEKLY -> "Weekly"
        RecurrenceType.MONTHLY -> "Monthly"
        RecurrenceType.CUSTOM_DAYS -> "Every $customInterval days"
        RecurrenceType.NONE -> ""
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AddTodoDialog(
    onAdd: (title: String, notes: String, dueDate: String, category: String, priority: String, isRecurring: Boolean, recurrenceType: RecurrenceType, customInterval: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Personal") }
    var priority by remember { mutableStateOf("Medium") }
    var isRecurring by remember { mutableStateOf(false) }
    var recurrenceType by remember { mutableStateOf(RecurrenceType.DAILY) }
    var customInterval by remember { mutableIntStateOf(1) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val dueDateDisplay = dueDate
    val today = remember { Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis }
    val datePickerState = rememberDatePickerState(
        initialDisplayedMonthMillis = today
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New To-Do") },
        text = {
            LazyColumn {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes") },
                        modifier = Modifier.padding(top = 8.dp).fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = dueDateDisplay,
                        onValueChange = {},
                        label = { Text("Due Date") },
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth()
                            .clickable { showDatePicker = true },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Filled.Event, contentDescription = "Pick date")
                            }
                        }
                    )
                }

                // Recurring Task Section
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .clickable { isRecurring = !isRecurring }
                    ) {
                        Checkbox(
                            checked = isRecurring,
                            onCheckedChange = { isRecurring = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Recurring Task", fontWeight = FontWeight.Medium)
                    }
                }

                if (isRecurring) {
                    item {
                        // Recurrence Type Dropdown
                        val recurrenceTypes = listOf("Daily", "Weekly", "Monthly", "Custom Days")
                        var expandedRecurrence by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.padding(top = 8.dp)) {
                            OutlinedTextField(
                                value = when (recurrenceType) {
                                    RecurrenceType.DAILY -> "Daily"
                                    RecurrenceType.WEEKLY -> "Weekly"
                                    RecurrenceType.MONTHLY -> "Monthly"
                                    RecurrenceType.CUSTOM_DAYS -> "Custom Days"
                                    else -> "Daily"
                                },
                                onValueChange = {},
                                label = { Text("Repeat") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expandedRecurrence = true },
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { expandedRecurrence = true }) {
                                        Icon(Icons.Filled.MoreVert, contentDescription = "Select recurrence")
                                    }
                                }
                            )
                            DropdownMenu(
                                expanded = expandedRecurrence,
                                onDismissRequest = { expandedRecurrence = false }
                            ) {
                                recurrenceTypes.forEachIndexed { index, type ->
                                    DropdownMenuItem(
                                        text = { Text(type) },
                                        onClick = {
                                            recurrenceType = when (index) {
                                                0 -> RecurrenceType.DAILY
                                                1 -> RecurrenceType.WEEKLY
                                                2 -> RecurrenceType.MONTHLY
                                                3 -> RecurrenceType.CUSTOM_DAYS
                                                else -> RecurrenceType.DAILY
                                            }
                                            expandedRecurrence = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (recurrenceType == RecurrenceType.CUSTOM_DAYS) {
                        item {
                            OutlinedTextField(
                                value = customInterval.toString(),
                                onValueChange = {
                                    val newValue = it.toIntOrNull()
                                    if (newValue != null && newValue > 0) {
                                        customInterval = newValue
                                    }
                                },
                                label = { Text("Every X days") },
                                modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                }

                // Category Dropdown
                item {
                    val categories = listOf("Personal", "Work", "School")
                    var expanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.padding(top = 8.dp)) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            label = { Text("Category") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = true },
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { expanded = true }) {
                                    Icon(Icons.Filled.MoreVert, contentDescription = "Select category")
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        category = cat
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Priority Dropdown
                item {
                    val priorities = listOf("Low", "Medium", "High")
                    var expandedPriority by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.padding(top = 8.dp)) {
                        OutlinedTextField(
                            value = priority,
                            onValueChange = {},
                            label = { Text("Priority") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedPriority = true },
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { expandedPriority = true }) {
                                    Icon(Icons.Filled.MoreVert, contentDescription = "Select priority")
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = expandedPriority,
                            onDismissRequest = { expandedPriority = false }
                        ) {
                            priorities.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text(p) },
                                    onClick = {
                                        priority = p
                                        expandedPriority = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(
                            title,
                            notes,
                            dueDate,
                            category,
                            priority,
                            isRecurring,
                            if (isRecurring) recurrenceType else RecurrenceType.NONE,
                            customInterval
                        )
                    }
                }
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    var showWarning by remember { mutableStateOf(false) }
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null && millis >= today) {
                        dueDate = dateFormat.format(Date(millis))
                        showDatePicker = false
                    } else {
                        showWarning = true
                    }
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            CustomDatePicker(state = datePickerState)
        }
    }
    if (showWarning) {
        AlertDialog(
            onDismissRequest = { showWarning = false },
            title = { Text("Invalid Date") },
            text = { Text("You cannot select a past date. Please choose today or a future date.") },
            confirmButton = {
                TextButton(onClick = { showWarning = false }) { Text("OK") }
            }
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun EditTodoDialog(
    initialTitle: String,
    initialNotes: String,
    initialDueDate: String,
    initialCategory: String,
    initialPriority: String,
    onEdit: (newTitle: String, newNotes: String, newDueDate: String, newCategory: String, newPriority: String) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var notes by remember { mutableStateOf(initialNotes) }
    var dueDate by remember { mutableStateOf(initialDueDate) }
    var category by remember { mutableStateOf(initialCategory) }
    var priority by remember { mutableStateOf(initialPriority) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val dueDateDisplay = dueDate
    val today = remember { Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = try { dateFormat.parse(initialDueDate)?.time } catch (_: Exception) { null },
        initialDisplayedMonthMillis = today
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit To-Do") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.padding(top = 8.dp)
                )
                OutlinedTextField(
                    value = dueDateDisplay,
                    onValueChange = {},
                    label = { Text("Due Date") },
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clickable { showDatePicker = true },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Filled.Event, contentDescription = "Pick date")
                        }
                    }
                )
                var showWarning by remember { mutableStateOf(false) }
                if (showDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                val millis = datePickerState.selectedDateMillis
                                if (millis != null && millis >= today) {
                                    dueDate = dateFormat.format(Date(millis))
                                    showDatePicker = false
                                } else {
                                    showWarning = true
                                }
                            }) { Text("OK") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                        }
                    ) {
                        CustomDatePicker(state = datePickerState)
                    }
                }
                if (showWarning) {
                    AlertDialog(
                        onDismissRequest = { showWarning = false },
                        title = { Text("Invalid Date") },
                        text = { Text("You cannot select a past date. Please choose today or a future date.") },
                        confirmButton = {
                            TextButton(onClick = { showWarning = false }) { Text("OK") }
                        }
                    )
                }
                // Category Dropdown
                val categories = listOf("Personal", "Work", "School")
                var expanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.padding(top = 8.dp)) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        label = { Text("Category") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Filled.MoreVert, contentDescription = "Select category")
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                // Priority Dropdown
                val priorities = listOf("Low", "Medium", "High")
                var expandedPriority by remember { mutableStateOf(false) }
                Box(modifier = Modifier.padding(top = 8.dp)) {
                    OutlinedTextField(
                        value = priority,
                        onValueChange = {},
                        label = { Text("Priority") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedPriority = true },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { expandedPriority = true }) {
                                Icon(Icons.Filled.MoreVert, contentDescription = "Select priority")
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = expandedPriority,
                        onDismissRequest = { expandedPriority = false }
                    ) {
                        priorities.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p) },
                                onClick = {
                                    priority = p
                                    expandedPriority = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(
                    onClick = {
                        if (title.isNotBlank()) onEdit(title, notes, dueDate, category, priority)
                    }
                ) { Text("Save") }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = onDelete
                ) { Text("Delete", color = Color.Red) }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun FilterDialog(
    currentCategory: String,
    currentPriority: String,
    showCompleted: Boolean,
    onCategoryChange: (String) -> Unit,
    onPriorityChange: (String) -> Unit,
    onShowCompletedChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Tasks") },
        text = {
            Column {
                // Category Filter
                Text("Category", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                val categories = listOf("All", "Personal", "Work", "School")
                categories.forEach { category ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCategoryChange(category) }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = currentCategory == category,
                            onClick = { onCategoryChange(category) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(category)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Priority Filter
                Text("Priority", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                val priorities = listOf("All", "Low", "Medium", "High")
                priorities.forEach { priority ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPriorityChange(priority) }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = currentPriority == priority,
                            onClick = { onPriorityChange(priority) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(priority)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Show Completed Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onShowCompletedChange(!showCompleted) }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = showCompleted,
                        onCheckedChange = onShowCompletedChange
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Show completed tasks")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onCategoryChange("All")
                onPriorityChange("All")
                onShowCompletedChange(true)
            }) {
                Text("Clear All")
            }
        }
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CustomDatePicker(state: DatePickerState) {
    Box {
        DatePicker(state = state)
    }
}


// Utility function to handle task completion and recurring task logic
fun handleTaskCompletion(todos: List<TodoItem>, completedTodo: TodoItem, context: Context): List<TodoItem> {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today = dateFormat.format(Date())

    return todos.map { todo ->
        if (todo.id == completedTodo.id) {
            val toggled = todo.copy(
                isDone = !todo.isDone,
                lastCompleted = if (!todo.isDone) today else ""
            )

            // Handle reminders
            if (toggled.isDone) {
                ReminderScheduler.cancel(context, toggled.id)
            } else if (toggled.dueDate.isNotBlank()) {
                ReminderScheduler.schedule(context, toggled.id, toggled.title, toggled.notes, toggled.dueDate)
            }

            toggled
        } else {
            todo
        }
    }.let { updatedTodos ->
        // If completing a recurring task, create the next instance
        val completedTask = updatedTodos.find { it.id == completedTodo.id }
        if (completedTask?.isDone == true && completedTask.isRecurring && completedTask.recurrenceType != RecurrenceType.NONE) {
            val nextDueDate = calculateNextDueDate(completedTask.dueDate, completedTask.recurrenceType, completedTask.customInterval)
            if (nextDueDate.isNotBlank()) {
                val nextTask = completedTask.copy(
                    id = UUID.randomUUID().toString(),
                    isDone = false,
                    dueDate = nextDueDate,
                    lastCompleted = "",
                    originalId = completedTask.originalId.ifBlank { completedTask.id }
                )

                // Schedule reminder for the new instance
                ReminderScheduler.schedule(context, nextTask.id, nextTask.title, nextTask.notes, nextTask.dueDate)

                updatedTodos + nextTask
            } else {
                updatedTodos
            }
        } else {
            updatedTodos
        }
    }
}

// Calculate the next due date based on recurrence type
fun calculateNextDueDate(currentDueDate: String, recurrenceType: RecurrenceType, customInterval: Int): String {
    if (currentDueDate.isBlank()) return ""

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return try {
        val currentDate = dateFormat.parse(currentDueDate) ?: return ""
        val calendar = Calendar.getInstance().apply { time = currentDate }

        when (recurrenceType) {
            RecurrenceType.DAILY -> calendar.add(Calendar.DAY_OF_MONTH, 1)
            RecurrenceType.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            RecurrenceType.MONTHLY -> calendar.add(Calendar.MONTH, 1)
            RecurrenceType.CUSTOM_DAYS -> calendar.add(Calendar.DAY_OF_MONTH, customInterval)
            RecurrenceType.NONE -> return ""
        }

        dateFormat.format(calendar.time)
    } catch (_: Exception) {
        ""
    }
}

// Progress Overview component
@Composable
fun ProgressOverview(todos: List<TodoItem>) {
    val totalTasks = todos.size
    val completedTasks = todos.count { it.isDone }
    val completionPercentage = if (totalTasks > 0) (completedTasks * 100) / totalTasks else 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Progress Overview",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$completedTasks/$totalTasks completed",
                    fontSize = 14.sp,
                    color = Color(0xFF757575)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { completionPercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF4A90E2),
                trackColor = Color(0xFFE0E0E0),
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$completionPercentage% Complete",
                fontSize = 14.sp,
                color = Color(0xFF4A90E2),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    ToDoTheme {
        MainScreen()
    }
}

// Enhanced Todo Detail Screen with progress tracking and additional features
@Composable
fun EnhancedTodoDetailScreen(
    todo: TodoItem,
    onEdit: () -> Unit,
    onToggleComplete: () -> Unit,
    onViewDetails: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onViewDetails() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (todo.isDone) Color(0xFFF5F5F5) else Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = todo.isDone,
                    onCheckedChange = { onToggleComplete() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF4A90E2),
                        uncheckedColor = Color(0xFF4A90E2),
                        checkmarkColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = todo.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (todo.isDone) Color(0xFF757575) else Color(0xFF222222)
                        )

                        if (todo.isRecurring && todo.recurrenceType != RecurrenceType.NONE) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🔄",
                                fontSize = 16.sp,
                                color = Color(0xFF4A90E2)
                            )
                        }

                        if (todo.comments.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                Icons.AutoMirrored.Filled.Comment,
                                contentDescription = "Has comments",
                                tint = Color(0xFF4A90E2),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    if (todo.notes.isNotBlank()) {
                        Text(
                            text = todo.notes,
                            fontSize = 14.sp,
                            color = Color(0xFF757575),
                            maxLines = 2,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // Progress bar for individual task progress
                    if (todo.completionPercentage > 0 && !todo.isDone) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LinearProgressIndicator(
                                progress = { todo.completionPercentage / 100f },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFF4A90E2),
                                trackColor = Color(0xFFE0E0E0)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${todo.completionPercentage}%",
                                fontSize = 12.sp,
                                color = Color(0xFF4A90E2),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Category badge
                        Surface(
                            color = Color(0xFF4A90E2).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Text(
                                text = todo.category,
                                fontSize = 12.sp,
                                color = Color(0xFF4A90E2),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // Priority indicator
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = when (todo.priority) {
                                        "High" -> Color.Red
                                        "Medium" -> Color(0xFFFFA500)
                                        else -> Color(0xFF4CAF50)
                                    },
                                    shape = CircleShape
                                )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(onClick = onEdit) {
                            Icon(
                                Icons.Filled.MoreVert,
                                contentDescription = "More options",
                                tint = Color(0xFFBDBDBD)
                            )
                        }
                    }

                    if (todo.dueDate.isNotBlank()) {
                        Text(
                            text = "Due: ${todo.dueDate}",
                            fontSize = 12.sp,
                            color = Color(0xFF757575),
                            modifier = Modifier.padding(top = 4.dp, end = 12.dp)
                        )
                    }

                    if (todo.streak > 0) {
                        Text(
                            text = "🔥 ${todo.streak} day streak",
                            fontSize = 12.sp,
                            color = Color(0xFFFF5722),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 4.dp, end = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

// Calendar View component
@Composable
fun CalendarView(todos: List<TodoItem>) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)

    // Group todos by date
    val todosByDate = remember(todos) {
        todos.filter { it.dueDate.isNotBlank() }
             .groupBy { it.dueDate }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Calendar View",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Generate days for current month
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1

        // Days of week header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                    Text(
                        text = day,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Calendar grid
        val totalCells = ((daysInMonth + firstDayOfWeek + 6) / 7) * 7
        items(totalCells / 7) { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(7) { dayOfWeek ->
                    val cellIndex = week * 7 + dayOfWeek
                    val dayOfMonth = cellIndex - firstDayOfWeek + 1

                    if (dayOfMonth in 1..daysInMonth) {
                        val dateString = String.format(Locale.getDefault(), "%04d-%02d-%02d", currentYear, currentMonth + 1, dayOfMonth)
                        val todosForDay = todosByDate[dateString] ?: emptyList()

                        CalendarDayCell(
                            dayOfMonth = dayOfMonth,
                            todos = todosForDay,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Today's tasks section
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Today's Tasks",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        val today = dateFormat.format(Date())
        val todayTasks = todosByDate[today] ?: emptyList()

        if (todayTasks.isEmpty()) {
            item {
                Text(
                    text = "No tasks for today",
                    color = Color(0xFF757575),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            items(todayTasks) { todo ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (todo.isDone) Color(0xFFF5F5F5) else Color.White
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (todo.isDone) "✓" else "○",
                            fontSize = 16.sp,
                            color = if (todo.isDone) Color(0xFF4CAF50) else Color(0xFF757575)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = todo.title,
                            fontSize = 16.sp,
                            color = if (todo.isDone) Color(0xFF757575) else Color(0xFF222222)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDayCell(
    dayOfMonth: Int,
    todos: List<TodoItem>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .background(
                color = if (todos.isNotEmpty()) Color(0xFF4A90E2).copy(alpha = 0.1f) else Color.Transparent,
                shape = RoundedCornerShape(4.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = dayOfMonth.toString(),
                fontSize = 14.sp,
                color = Color(0xFF222222)
            )
            if (todos.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Color(0xFF4A90E2), CircleShape)
                )
            }
        }
    }
}

// Activity Log Dialog component
@Composable
fun ActivityLogDialog(
    activityLog: List<ActivityLogEntry>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.History, contentDescription = "Activity Log")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Activity Log")
            }
        },
        text = {
            if (activityLog.isEmpty()) {
                Text("No activity yet. Start creating and managing tasks to see your activity here!")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                ) {
                    items(activityLog.sortedByDescending { it.timestamp }) { entry ->
                        ActivityLogItem(entry)
                        if (entry != activityLog.last()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun ActivityLogItem(entry: ActivityLogEntry) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.taskTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val actionIcon = when (entry.action) {
                        "created" -> "✨"
                        "completed" -> "✅"
                        "uncompleted" -> "🔄"
                        "updated" -> "✏️"
                        "deleted" -> "🗑️"
                        "comment_added" -> "💬"
                        "progress_updated" -> "📊"
                        else -> "📝"
                    }
                    Text(
                        text = actionIcon,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = entry.action.replace("_", " ").replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                        },
                        fontSize = 14.sp,
                        color = Color(0xFF4A90E2),
                        fontWeight = FontWeight.Medium
                    )
                }
                if (entry.details.isNotBlank()) {
                    Text(
                        text = entry.details,
                        fontSize = 12.sp,
                        color = Color(0xFF757575),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            Text(
                text = formatTimestamp(entry.timestamp),
                fontSize = 12.sp,
                color = Color(0xFF757575)
            )
        }
    }
}

// Task Details Dialog component
@Composable
fun TaskDetailsDialog(
    todo: TodoItem,
    onDismiss: () -> Unit,
    onAddComment: (String) -> Unit,
    onUpdateProgress: (Int) -> Unit
) {
    var newComment by remember { mutableStateOf("") }
    var progressValue by remember { mutableIntStateOf(todo.completionPercentage) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Timeline, contentDescription = "Task Details")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Task Details")
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
            ) {
                // Task Info
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = todo.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (todo.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = todo.notes,
                                    fontSize = 14.sp,
                                    color = Color(0xFF757575)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            // Progress Section
                            Text(
                                text = "Progress",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Slider(
                                value = progressValue.toFloat(),
                                onValueChange = { progressValue = it.toInt() },
                                valueRange = 0f..100f,
                                steps = 19, // 5% increments
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "$progressValue% Complete",
                                fontSize = 14.sp,
                                color = Color(0xFF4A90E2),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (progressValue != todo.completionPercentage) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        onUpdateProgress(progressValue)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Update Progress")
                                }
                            }

                            // Task Stats
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Category", fontSize = 12.sp, color = Color(0xFF757575))
                                    Text(todo.category, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                }
                                Column {
                                    Text("Priority", fontSize = 12.sp, color = Color(0xFF757575))
                                    Text(
                                        text = todo.priority,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = when (todo.priority) {
                                            "High" -> Color.Red
                                            "Medium" -> Color(0xFFFFA500)
                                            else -> Color(0xFF4CAF50)
                                        }
                                    )
                                }
                                if (todo.streak > 0) {
                                    Column {
                                        Text("Streak", fontSize = 12.sp, color = Color(0xFF757575))
                                        Text("🔥 ${todo.streak}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }

                            if (todo.dueDate.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Due Date", fontSize = 12.sp, color = Color(0xFF757575))
                                Text(todo.dueDate, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Comments Section
                item {
                    Text(
                        text = "Comments (${todo.comments.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Add comment
                    OutlinedTextField(
                        value = newComment,
                        onValueChange = { newComment = it },
                        placeholder = { Text("Add a comment...") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (newComment.isNotBlank()) {
                                        onAddComment(newComment)
                                        newComment = ""
                                    }
                                }
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Comment, contentDescription = "Add comment")
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Comments list
                if (todo.comments.isEmpty()) {
                    item {
                        Text(
                            text = "No comments yet. Add one above!",
                            fontSize = 14.sp,
                            color = Color(0xFF757575),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    items(todo.comments.sortedByDescending { it.timestamp }) { comment ->
                        CommentItem(comment)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Attachments Section (placeholder for future implementation)
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Attachments (${todo.attachments.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (todo.attachments.isEmpty()) {
                        Text(
                            text = "No attachments",
                            fontSize = 14.sp,
                            color = Color(0xFF757575)
                        )
                    } else {
                        // Placeholder for attachments display
                        todo.attachments.forEach { attachment ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.Attachment,
                                        contentDescription = "Attachment",
                                        tint = Color(0xFF4A90E2)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(attachment.name, fontWeight = FontWeight.Medium)
                                        Text(
                                            attachment.type,
                                            fontSize = 12.sp,
                                            color = Color(0xFF757575)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun CommentItem(comment: TaskComment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "💬",
                    fontSize = 16.sp
                )
                Text(
                    text = formatTimestamp(comment.timestamp),
                    fontSize = 12.sp,
                    color = Color(0xFF757575)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = comment.text,
                fontSize = 14.sp
            )
        }
    }
}

// Utility function to format timestamps
fun formatTimestamp(timestamp: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = sdf.parse(timestamp)
        val now = Date()
        val diffInMs = now.time - (date?.time ?: 0)
        val diffInMinutes = diffInMs / (1000 * 60)
        val diffInHours = diffInMs / (1000 * 60 * 60)
        val diffInDays = diffInMs / (1000 * 60 * 60 * 24)

        when {
            diffInMinutes < 1 -> "Just now"
            diffInMinutes < 60 -> "${diffInMinutes}m ago"
            diffInHours < 24 -> "${diffInHours}h ago"
            diffInDays < 7 -> "${diffInDays}d ago"
            else -> {
                val displaySdf = SimpleDateFormat("MMM dd", Locale.getDefault())
                displaySdf.format(date ?: Date())
            }
        }
    } catch (_: Exception) {
        "Unknown"
    }
}
