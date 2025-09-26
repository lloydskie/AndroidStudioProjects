# ProGuard rules for the ToDo application

# Keep all public classes and their public methods
-keep public class * {
    public *;
}

# Keep the ToDoItem data class
-keep class com.example.todo.data.ToDoItem {
    *;
}

# Keep the MainActivity class
-keep class com.example.todo.MainActivity {
    *;
}

# Keep the layout resources
-keep class * extends android.view.View {
    <init>(android.content.Context);
    <init>(android.content.Context, android.util.AttributeSet);
}

# Keep the application class
-keep class com.example.todo.Application {
    *;
}

# Add any additional rules as needed for libraries or specific classes
# For example, if using Retrofit or Gson, you may need to keep certain classes or fields.