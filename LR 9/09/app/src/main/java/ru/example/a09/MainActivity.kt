package ru.example.lab09

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LabScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()

    var title by rememberSaveable { mutableStateOf("") }
    var priority by rememberSaveable { mutableStateOf(1) }
    var filter by rememberSaveable { mutableStateOf(TaskFilter.ALL.name) }
    var priorityFilter by rememberSaveable { mutableStateOf<Int?>(null) }
    var sortByTitle by rememberSaveable { mutableStateOf(false) }

    var rows by remember { mutableStateOf(emptyList<Task>()) }
    var counts by remember { mutableStateOf(emptyList<PriorityCount>()) }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }

    fun refresh() {
        if (busy) return
        busy = true
        scope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    TasksDb(context).use { db ->
                        val list = db.read(
                            filter = TaskFilter.valueOf(filter),
                            priority = priorityFilter,
                            sortByTitle = sortByTitle
                        )
                        list to db.countByPriority()
                    }
                }
                rows = result.first
                counts = result.second
                status = "Записей: ${rows.size}"
            } catch (e: android.database.SQLException) {
                status = "Ошибка базы: ${e.message}"
            } finally {
                busy = false
            }
        }
    }

    fun add() {
        val input = title.trim()
        if (input.isEmpty() || busy) return
        busy = true
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    TasksDb(context).use { db ->
                        db.add(input, priority)
                    }
                }
                title = ""
                busy = false
                refresh()
            } catch (e: android.database.SQLException) {
                status = "Ошибка базы: ${e.message}"
                busy = false
            }
        }
    }

    fun setDone(id: Long, done: Boolean) {
        scope.launch {
            withContext(Dispatchers.IO) {
                TasksDb(context).use { it.setDone(id, done) }
            }
            refresh()
        }
    }

    fun delete(id: Long) {
        scope.launch {
            withContext(Dispatchers.IO) {
                TasksDb(context).use { it.delete(id) }
            }
            refresh()
        }
    }

    LaunchedEffect(Unit) { refresh() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Учебные задачи", style = MaterialTheme.typography.headlineSmall)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Название задачи") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("P", style = MaterialTheme.typography.labelSmall)
                Text("$priority")
            }
            Column {
                listOf(1, 2, 3).forEach { p ->
                    TextButton(onClick = { priority = p }) { Text("$p") }
                }
            }
        }

        Button(onClick = { add() }, enabled = !busy && title.isNotBlank()) {
            Text("Добавить")
        }

        Text("Фильтр:")
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TaskFilter.values().forEach { f ->
                FilterChip(
                    selected = filter == f.name,
                    onClick = { filter = f.name; refresh() },
                    label = { Text(f.name) }
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            FilterChip(
                selected = priorityFilter == null,
                onClick = { priorityFilter = null; refresh() },
                label = { Text("Все P") }
            )
            listOf(1, 2, 3).forEach { p ->
                FilterChip(
                    selected = priorityFilter == p,
                    onClick = { priorityFilter = p; refresh() },
                    label = { Text("P$p") }
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = sortByTitle, onCheckedChange = { sortByTitle = it; refresh() })
            Text("Сортировать по title (COLLATE NOCASE)")
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(rows, key = { it.id }) { task ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = task.done,
                            onCheckedChange = { setDone(task.id, it) }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(task.title)
                            Text(
                                "id=${task.id}, priority=${task.priority}",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        TextButton(onClick = { delete(task.id) }) {
                            Text("Удалить")
                        }
                    }
                }
            }
        }

        Text("По приоритетам (GROUP BY):")
        Text(counts.joinToString(", ") { "P${it.priority}: ${it.count}" })

        Text(status, style = MaterialTheme.typography.bodySmall)
    }
}