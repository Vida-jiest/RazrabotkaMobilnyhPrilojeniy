package ru.example.lab06

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.example.lab06.ui.theme.Lab06Theme

class ThirdActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        val name = intent.getStringExtra("name")
        val course = intent.getStringExtra("course")

        setContent {
            Lab06Theme {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Экран подтверждения", style = MaterialTheme.typography.headlineSmall)
                    Text("Имя: ${name ?: "Не указано"}")
                    Text("Курс: ${course ?: "Не указан"}")
                    Button(onClick = { finish() }) { Text("Подтвердить") }
                }
            }
        }
    }
    override fun onStart() { super.onStart(); Log.d(TAG, "onStart") }
    override fun onResume() { super.onResume(); Log.d(TAG, "onResume") }
    override fun onPause() { super.onPause(); Log.d(TAG, "onPause") }
    override fun onStop() { super.onStop(); Log.d(TAG, "onStop") }
    override fun onDestroy() { super.onDestroy(); Log.d(TAG, "onDestroy") }
    companion object { const val TAG = "ThirdActivity" }
}