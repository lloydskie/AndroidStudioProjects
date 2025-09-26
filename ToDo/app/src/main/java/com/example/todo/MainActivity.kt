package com.example.todo

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
    val dueDate: String = ""
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
                onAdd = { title, notes, dueDate ->
                    todos = todos + TodoItem(title, notes, dueDate = dueDate)
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
                onEdit = { newTitle, newNotes, newDueDate ->
                    todos = todos.toMutableList().also {
                        it[editingTodoIndex] = todo.copy(title = newTitle, notes = newNotes, dueDate = newDueDate)
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
    onEdit: (String, String, String) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var notes by remember { mutableStateOf(initialNotes) }
    var dueDate by remember { mutableStateOf(initialDueDate) }
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
                if (showDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                val millis = datePickerState.selectedDateMillis
                                if (millis != null) {
                                    dueDate = dateFormat.format(Date(millis))
                                }
                                showDatePicker = false
                            }) { Text("OK") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                        }
                    ) {
                        DatePicker(state = datePickerState, dateValidator = { millis -> millis == null || millis >= today })
                    }
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(
                    onClick = {
                        if (title.isNotBlank()) onEdit(title, notes, dueDate)
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
fun AddTodoDialog(onAdd: (String, String, String) -> Unit, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
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
                if (showDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                val millis = datePickerState.selectedDateMillis
                                if (millis != null) {
                                    dueDate = dateFormat.format(Date(millis))
                                }
                                showDatePicker = false
                            }) { Text("OK") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                        }
                    ) {
                        DatePicker(state = datePickerState, dateValidator = { millis -> millis == null || millis >= today })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) onAdd(title, notes, dueDate)
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