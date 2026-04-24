package com.example.app_vaidulla_21;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    // Обработчик нажатия кнопки – обратите внимание на название метода
    public void onClickGoToSecond(View view) {
        Intent intent = new Intent(MainActivity.this, ActivityTwo.class);
        startActivity(intent);
    }
}