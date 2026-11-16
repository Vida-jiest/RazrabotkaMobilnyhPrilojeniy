package ru.example.lab09

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class Task(
    val id: Long,
    val title: String,
    val done: Boolean,
    val priority: Int
)

data class PriorityCount(val priority: Int, val count: Int)

enum class TaskFilter(val sql: String?) {
    ALL(null),
    DONE("done = 1"),
    NOT_DONE("done = 0")
}

class TasksDb(context: Context) : SQLiteOpenHelper(
    context, DB_NAME, null, DB_VERSION
), AutoCloseable {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TITLE TEXT NOT NULL,
                $COL_DONE INTEGER NOT NULL DEFAULT 0,
                $COL_PRIORITY INTEGER NOT NULL DEFAULT 1
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE")
        onCreate(db)
    }

    fun add(title: String, priority: Int = 1): Long {
        val values = ContentValues().apply {
            put(COL_TITLE, title)
            put(COL_DONE, 0)
            put(COL_PRIORITY, priority.coerceIn(1, 3))
        }
        return writableDatabase.insert(TABLE, null, values)
    }

    fun read(
        filter: TaskFilter = TaskFilter.ALL,
        priority: Int? = null,
        sortByTitle: Boolean = false
    ): List<Task> {
        val whereParts = mutableListOf<String>()
        val args = mutableListOf<String>()

        filter.sql?.let { whereParts += it }
        if (priority != null) {
            whereParts += "$COL_PRIORITY = ?"
            args += priority.toString()
        }
        val where = if (whereParts.isEmpty()) null else whereParts.joinToString(" AND ")
        val order = if (sortByTitle) "$COL_TITLE COLLATE NOCASE" else "$COL_ID"

        val result = mutableListOf<Task>()
        readableDatabase.query(
            TABLE, null, where, args.toTypedArray(), null, null, order
        ).use { cursor ->
            val iId = cursor.getColumnIndexOrThrow(COL_ID)
            val iTitle = cursor.getColumnIndexOrThrow(COL_TITLE)
            val iDone = cursor.getColumnIndexOrThrow(COL_DONE)
            val iPriority = cursor.getColumnIndexOrThrow(COL_PRIORITY)
            while (cursor.moveToNext()) {
                result += Task(
                    id = cursor.getLong(iId),
                    title = cursor.getString(iTitle),
                    done = cursor.getInt(iDone) == 1,
                    priority = cursor.getInt(iPriority)
                )
            }
        }
        return result
    }

    fun setDone(id: Long, done: Boolean): Int {
        val values = ContentValues().apply { put(COL_DONE, if (done) 1 else 0) }
        return writableDatabase.update(
            TABLE, values, "$COL_ID = ?", arrayOf(id.toString())
        )
    }

    fun delete(id: Long): Int =
        writableDatabase.delete(TABLE, "$COL_ID = ?", arrayOf(id.toString()))

    fun countByPriority(): List<PriorityCount> {
        val result = mutableListOf<PriorityCount>()
        readableDatabase.rawQuery(
            "SELECT $COL_PRIORITY, COUNT(*) FROM $TABLE GROUP BY $COL_PRIORITY ORDER BY $COL_PRIORITY",
            null
        ).use { c ->
            while (c.moveToNext()) {
                result += PriorityCount(c.getInt(0), c.getInt(1))
            }
        }
        return result
    }

    companion object {
        private const val DB_NAME = "lab09.db"
        private const val DB_VERSION = 1
        private const val TABLE = "tasks"
        private const val COL_ID = "id"
        private const val COL_TITLE = "title"
        private const val COL_DONE = "done"
        private const val COL_PRIORITY = "priority"
    }
}