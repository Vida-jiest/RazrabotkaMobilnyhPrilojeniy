package ru.example.lab08

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

// 1. DataStore создаётся ОДИН раз на уровне файла (требование методички)
val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

val NAME_KEY = stringPreferencesKey("name")
val LARGE_TEXT_KEY = booleanPreferencesKey("large_text")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Lab08Screen()
                }
            }
        }
    }
}

@Composable
fun Lab08Screen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Состояния UI
    var name by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var isLargeText by remember { mutableStateOf(false) }
    var textSize by remember { mutableStateOf(16f) }

    var status by remember { mutableStateOf("Загрузка...") }
    var isReady by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Загрузка данных при первом запуске
    LaunchedEffect(Unit) {
        try {
            // Чтение настроек из DataStore
            val preferences = context.settingsDataStore.data.first()
            name = preferences[NAME_KEY] ?: ""
            isLargeText = preferences[LARGE_TEXT_KEY] ?: false
            textSize = if (isLargeText) 20f else 16f

            // Чтение файла в фоновом потоке (Dispatchers.IO)
            note = withContext(Dispatchers.IO) {
                readNoteFromFile(context)
            }
            status = "Готово"
            isReady = true
        } catch (e: IOException) {
            status = "Ошибка чтения данных"
        }
    }

    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
        Text("Статус: $status", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))

        // Поле ввода имени
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Имя") },
            enabled = isReady,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Переключатель крупного текста (Самостоятельное задание)
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Крупный текст", modifier = Modifier.weight(1f))
            Switch(
                checked = isLargeText,
                onCheckedChange = {
                    isLargeText = it
                    textSize = if (it) 20f else 16f
                },
                enabled = isReady
            )
        }

        // Зависимый Slider (недоступен, если Switch выключен)
        Slider(
            value = textSize,
            onValueChange = { textSize = it },
            valueRange = 14f..24f,
            enabled = isReady && isLargeText,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Многострочная заметка
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Заметка") },
            enabled = isReady,
            modifier = Modifier.fillMaxWidth().height(150.dp),
            textStyle = LocalTextStyle.current.copy(fontSize = textSize.sp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Кнопки управления
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = {
                    scope.launch {
                        status = "Сохранение..."
                        try {
                            // 1. Сохранение в DataStore
                            context.settingsDataStore.edit { prefs ->
                                prefs[NAME_KEY] = name.trim()
                                prefs[LARGE_TEXT_KEY] = isLargeText
                            }
                            // 2. Сохранение в файл в фоновом потоке
                            withContext(Dispatchers.IO) {
                                saveNoteToFile(context, note)
                            }
                            // Статус меняется ТОЛЬКО после реального завершения записи
                            status = "Успешно сохранено"
                        } catch (e: IOException) {
                            status = "Ошибка записи: ${e.message}"
                        }
                    }
                },
                enabled = isReady
            ) {
                Text("Сохранить")
            }

            Button(
                onClick = { showDeleteDialog = true },
                enabled = isReady && note.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Удалить заметку")
            }
        }
    }

    // Диалог подтверждения удаления (Самостоятельное задание)
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Подтверждение") },
            text = { Text("Вы уверены, что хотите удалить заметку? Ранее сохраненный текст будет утрачен.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        try {
                            withContext(Dispatchers.IO) {
                                deleteNoteFile(context)
                            }
                            note = ""
                            status = "Заметка удалена"
                        } catch (e: IOException) {
                            // Обработка ошибки без уничтожения ранее сохраненного текста в UI
                            status = "Ошибка удаления: ${e.message}"
                        }
                    }
                    showDeleteDialog = false
                }) {
                    Text("Да")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

// --- Функции для работы с внутренними файлами (используют .use() для закрытия потоков) ---

private fun saveNoteToFile(context: Context, content: String) {
    context.openFileOutput("notes.txt", Context.MODE_PRIVATE).use { outputStream ->
        outputStream.write(content.toByteArray(Charsets.UTF_8))
    }
}

private fun readNoteFromFile(context: Context): String {
    return try {
        context.openFileInput("notes.txt").use { inputStream ->
            inputStream.bufferedReader(Charsets.UTF_8).readText()
        }
    } catch (e: IOException) {
        "" // Если файла еще нет, возвращаем пустую строку
    }
}

private fun deleteNoteFile(context: Context) {
    val deleted = context.deleteFile("notes.txt")
    if (!deleted) throw IOException("Не удалось удалить файл")
}