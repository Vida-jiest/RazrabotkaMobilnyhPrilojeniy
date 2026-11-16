package ru.example.lab10

import android.content.ContentValues
import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.example.lab10.ui.theme.Lab10Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Lab10Theme {
                LabScreen()
            }
        }
    }
}

@Composable
fun LabScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()

    // Поля теперь пустые по умолчанию
    var groupName by rememberSaveable { mutableStateOf("") }
    var student1 by rememberSaveable { mutableStateOf("") }
    var student2 by rememberSaveable { mutableStateOf("") }
    var text by remember { mutableStateOf("Нажмите для запроса") }

    fun executeQuery(action: String, block: (android.database.sqlite.SQLiteDatabase) -> String) {
        text = "Выполняется..."
        scope.launch {
            text = withContext(Dispatchers.IO) {
                try {
                    SchoolDb(context).use { helper ->
                        val db = helper.writableDatabase
                        block(db)
                    }
                } catch (e: Exception) {
                    "Ошибка: ${e.message}"
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("ЛР10: SQLite, JOIN, транзакции, миграции", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(value = groupName, onValueChange = { groupName = it }, label = { Text("Название группы") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = student1, onValueChange = { student1 = it }, label = { Text("Имя студента 1") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = student2, onValueChange = { student2 = it }, label = { Text("Имя студента 2") }, modifier = Modifier.fillMaxWidth())

        // 1. Сохранить
        Button(onClick = {
            executeQuery("Сохранено") { db ->
                val groupId = 1
                val tGroups = "groups"
                db.insertWithOnConflict(tGroups, null, ContentValues().apply { put("id", groupId); put("title", groupName) }, android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE)

                db.insertWithOnConflict("student", null, ContentValues().apply { put("id", 100); put("name", student1); put("group_id", groupId) }, android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE)
                db.insertWithOnConflict("student", null, ContentValues().apply { put("id", 101); put("name", student2); put("group_id", groupId) }, android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE)
                "Группа и студенты сохранены."
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("1. Сохранить группу и двух студентов") }

        // 2. JOIN
        Button(onClick = {
            executeQuery("JOIN выполнен") { db ->
                val tGroups = "groups"
                try {
                    db.rawQuery("SELECT s.name, g.title FROM student s INNER JOIN $tGroups g ON s.group_id = g.id", null).use { c ->
                        val sb = StringBuilder()
                        if (c.count == 0) {
                            sb.append("Нет данных для JOIN (сначала нажмите кнопку 1)")
                        } else {
                            while (c.moveToNext()) { sb.append("${c.getString(0)} / ${c.getString(1)}\n") }
                        }
                        sb.toString()
                    }
                } catch (e: Exception) {
                    "Ошибка (возможно, таблица еще не создана): сначала нажмите кнопку 1"
                }
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("2. Показать INNER JOIN") }

        // 3. Успешная транзакция
        Button(onClick = {
            executeQuery("Транзакция завершена") { db ->
                val before = db.rawQuery("SELECT COUNT(*) FROM student", null).use { it.moveToFirst(); it.getInt(0) }
                db.beginTransaction()
                try {
                    db.insert("student", null, ContentValues().apply { put("id", 200); put("name", "Транзакция1"); put("group_id", 1) })
                    db.insert("student", null, ContentValues().apply { put("id", 201); put("name", "Транзакция2"); put("group_id", 1) })
                    db.setTransactionSuccessful()
                } finally { db.endTransaction() }
                val after = db.rawQuery("SELECT COUNT(*) FROM student", null).use { it.moveToFirst(); it.getInt(0) }
                "Успешная транзакция: было $before, стало $after (+${after - before})"
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("3. Транзакция: успешная") }

        // 4. Откат транзакции
        Button(onClick = {
            executeQuery("Откат завершен") { db ->
                val before = db.rawQuery("SELECT COUNT(*) FROM student", null).use { it.moveToFirst(); it.getInt(0) }
                db.beginTransaction()
                try {
                    db.insert("student", null, ContentValues().apply { put("id", 300); put("name", "Откат1"); put("group_id", 1) })
                    throw Exception("Искусственная ошибка")
                } catch (e: Exception) {
                } finally { db.endTransaction() }
                val after = db.rawQuery("SELECT COUNT(*) FROM student", null).use { it.moveToFirst(); it.getInt(0) }
                "Откат: было $before, стало $after; строка id=300 существует=${after > before}"
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("4. Транзакция: откат") }

        // 5. Внешний ключ
        Button(onClick = {
            executeQuery("Проверка ключа") { db ->
                try {
                    db.insertOrThrow("student", null, ContentValues().apply { put("id", 400); put("name", "Ошибка"); put("group_id", 999) })
                    "Ошибка не выявлена (это плохо)"
                } catch (e: SQLiteConstraintException) {
                    "Ожидаемо отклонено: ${e.message}"
                }
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("5. Проверить внешний ключ") }

        // 6. Показать количество и схему
        Button(onClick = {
            executeQuery("Схема получена") { db ->
                val tGroups = "groups"
                val studentCount = db.rawQuery("SELECT COUNT(*) FROM student", null).use { it.moveToFirst(); it.getInt(0) }
                val groupCount = db.rawQuery("SELECT COUNT(*) FROM $tGroups", null).use { it.moveToFirst(); it.getInt(0) }

                val cursor = db.rawQuery("PRAGMA table_info(student)", null)
                val columns = StringBuilder()
                while (cursor.moveToNext()) { columns.append("${cursor.getString(1)} : ${cursor.getString(2)}\n") }
                cursor.close()

                "Студентов: $studentCount\nГрупп: $groupCount\nСхема student:\n$columns"
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("6. Показать количество и схему") }

        // 7. ON DELETE RESTRICT
        Button(onClick = {
            executeQuery("Удаление группы") { db ->
                val tGroups = "groups"
                try {
                    db.delete(tGroups, "id = ?", arrayOf("1"))
                    "Группа удалена (студенты остались без группы — это плохо)"
                } catch (e: SQLiteConstraintException) {
                    "Удаление группы id=1 отклонено: ${e.message}"
                }
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("7. Проверить ON DELETE RESTRICT") }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text)
    }
}