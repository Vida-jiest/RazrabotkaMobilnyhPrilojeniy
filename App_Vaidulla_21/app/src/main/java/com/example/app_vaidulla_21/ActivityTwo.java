package com.example.app_vaidulla_21;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class ActivityTwo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two);
    }

    public void onClickBack(View view) {
        finish(); // закрывает текущее Activity и возвращает на предыдущее
    }
}