data class ToDoItem(
    val id: Long,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false
)