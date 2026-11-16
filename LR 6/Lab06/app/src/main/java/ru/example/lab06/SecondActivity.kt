package ru.example.lab06

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.example.lab06.ui.theme.Lab06Theme

class SecondActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        val name = intent.getStringExtra("name")

        setContent {
            Lab06Theme {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (name.isNullOrBlank()) "Имя не передано" else "Привет, $name!",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Button(onClick = {
                        val intent = Intent(this@SecondActivity, ThirdActivity::class.java)
                        intent.putExtra("name", name)
                        intent.putExtra("course", "3 курс")
                        startActivity(intent)
                    }) {
                        Text("Перейти к подтверждению")
                    }
                }
            }
        }
    }
    override fun onStart() { super.onStart(); Log.d(TAG, "onStart") }
    override fun onResume() { super.onResume(); Log.d(TAG, "onResume") }
    override fun onPause() { super.onPause(); Log.d(TAG, "onPause") }
    override fun onStop() { super.onStop(); Log.d(TAG, "onStop") }
    override fun onDestroy() { super.onDestroy(); Log.d(TAG, "onDestroy") }
    companion object { const val TAG = "SecondActivity" }
}