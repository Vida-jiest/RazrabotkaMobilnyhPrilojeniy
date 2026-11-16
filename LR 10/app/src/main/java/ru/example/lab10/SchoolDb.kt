package ru.example.lab10

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SchoolDb(context: Context) : SQLiteOpenHelper(context, "school.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("PRAGMA foreign_keys=ON;")

        val tableGroups = "groups"
        val tableStudent = "student"

        db.execSQL("CREATE TABLE $tableGroups (id INTEGER PRIMARY KEY, title TEXT NOT NULL)")
        db.execSQL("CREATE TABLE $tableStudent (id INTEGER PRIMARY KEY, name TEXT NOT NULL, group_id INTEGER NOT NULL, FOREIGN KEY(group_id) REFERENCES $tableGroups(id) ON DELETE RESTRICT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE student ADD COLUMN email TEXT DEFAULT 'no-email@example.com'")
        }
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }
}