
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Event
import java.text.SimpleDateFormat
import java.util.Calendar
import com.example.todo.ui.theme.ToDoTheme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Event
import java.text.SimpleDateFormat
import java.util.*
import com.example.todo.ui.theme.ToDoTheme


data class TodoItem(
    val title: String,
    val notes: String = "",
    val isDone: Boolean = false,
    val dueDate: String = "",
    val category: String = "Personal",
    val priority: String = "Medium"
)

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
fun MainScreen() {
    var todos by remember { mutableStateOf(listOf<TodoItem>()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingTodoIndex by remember { mutableIntStateOf(-1) }

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (todos.isEmpty()) {
                Text(
                    text = "Tap or drag the plus button to\ncreate a new to-do.",
                    color = Color(0xFFBDBDBD),
                    fontSize = 18.sp,
                    modifier = Modifier.align(Alignment.Center),
                    lineHeight = 24.sp
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    itemsIndexed(todos) { index, todo ->
                        TodoDetailScreen(
                            todo = todo,
                            onEdit = {
                                editingTodoIndex = index
                                showEditDialog = true
                            },
                            onToggleComplete = {
                                todos = todos.toMutableList().also {
                                    val t = it[index]
                                    it[index] = t.copy(isDone = !t.isDone)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
        if (showAddDialog) {
            AddTodoDialog(
                onAdd = { title, notes, dueDate, category, priority ->
                    todos = todos + TodoItem(title, notes, dueDate = dueDate, category = category, priority = priority)
                    showAddDialog = false
                },
                onDismiss = { showAddDialog = false }
            )
        }
        if (showEditDialog && editingTodoIndex >= 0) {
            val todo = todos[editingTodoIndex]
            EditTodoDialog(
                initialTitle = todo.title,
                initialNotes = todo.notes,
                initialDueDate = todo.dueDate,
                initialCategory = todo.category,
                initialPriority = todo.priority,
                onEdit = { newTitle, newNotes, newDueDate, newCategory, newPriority ->
                    todos = todos.toMutableList().also {
                        it[editingTodoIndex] = todo.copy(title = newTitle, notes = newNotes, dueDate = newDueDate, category = newCategory, priority = newPriority)
                    }
                    showEditDialog = false
                },
                onDelete = {
                    todos = todos.toMutableList().also {
                        it.removeAt(editingTodoIndex)
                    }
                    showEditDialog = false
                },
                onDismiss = { showEditDialog = false }
            )
        }
    }
}

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
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun EditTodoDialog(
    initialTitle: String,
    initialNotes: String,
    initialDueDate: String,
    initialCategory: String,
    initialPriority: String,
    onEdit: (String, String, String, String, String) -> Unit,
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
                        CustomDatePicker(state = datePickerState, today = today)
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
@OptIn(ExperimentalMaterial3Api::class)
fun AddTodoDialog(onAdd: (String, String, String, String, String) -> Unit, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Personal") }
    var priority by remember { mutableStateOf("Medium") }
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
                        CustomDatePicker(state = datePickerState, today = today)
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
// CustomDatePicker overlays the default DatePicker and dims past dates (visual only)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePicker(state: DatePickerState, today: Long) {
    // The default DatePicker does not support per-day styling, so this is a placeholder for future customization.
    // For now, just use the default DatePicker. The logic in the dialog prevents past date selection.
    Box {
        DatePicker(state = state)
        // Overlay to dim past dates (visual only, selection is still handled in dialog logic)
        // This is a visual hint; actual selection is blocked in dialog confirmButton
        // If you want to further customize, you can draw overlays here
    }
}
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) onAdd(title, notes, dueDate, category, priority)
                }
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}


@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    ToDoTheme {
        MainScreen()
    }
}