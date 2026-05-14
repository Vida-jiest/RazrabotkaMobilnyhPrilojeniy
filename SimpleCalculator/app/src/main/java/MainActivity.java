package com.example.simplecalculator;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.text.TextUtils;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TextInputEditText etNum1, etNum2;
    Button btnAdd, btnSub, btnMult, btnDiv, btnClear;
    TextView tvResult;
    String oper = "";

    final int MENU_RESET_ID = 1;
    final int MENU_QUIT_ID = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        etNum1 = findViewById(R.id.etNum1);
        etNum2 = findViewById(R.id.etNum2);
        btnAdd = findViewById(R.id.btnAdd);
        btnSub = findViewById(R.id.btnSub);
        btnMult = findViewById(R.id.btnMult);
        btnDiv = findViewById(R.id.btnDiv);
        btnClear = findViewById(R.id.btnClear);
        tvResult = findViewById(R.id.tvResult);

        btnAdd.setOnClickListener(this);
        btnSub.setOnClickListener(this);
        btnMult.setOnClickListener(this);
        btnDiv.setOnClickListener(this);
        btnClear.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        // Кнопка очистки
        if (id == R.id.btnClear) {
            etNum1.setText("");
            etNum2.setText("");
            tvResult.setText("✨ Результат ✨");
            return;
        }

        // Проверка на пустые поля
        if (TextUtils.isEmpty(etNum1.getText().toString()) ||
                TextUtils.isEmpty(etNum2.getText().toString())) {
            tvResult.setText("⚠️ Заполните оба поля!");
            return;
        }

        float num1 = Float.parseFloat(etNum1.getText().toString());
        float num2 = Float.parseFloat(etNum2.getText().toString());
        float result = 0;

        // Выбор операции
        if (id == R.id.btnAdd) {
            oper = "+";
            result = num1 + num2;
        } else if (id == R.id.btnSub) {
            oper = "-";
            result = num1 - num2;
        } else if (id == R.id.btnMult) {
            oper = "×";
            result = num1 * num2;
        } else if (id == R.id.btnDiv) {
            if (num2 == 0) {
                tvResult.setText("❌ Ошибка: деление на ноль!");
                return;
            }
            oper = "÷";
            result = num1 / num2;
        }

        // Вывод результата (форматирование до 2 знаков)
        tvResult.setText(String.format("%.2f %s %.2f = %.2f", num1, oper, num2, result));
    }

    // Создание меню (три точки)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_RESET_ID, 0, "🔄 Сброс");
        menu.add(0, MENU_QUIT_ID, 0, "🚪 Выход");
        return super.onCreateOptionsMenu(menu);
    }

    // Обработка нажатий на меню
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == MENU_RESET_ID) {
            // Сброс всех полей
            etNum1.setText("");
            etNum2.setText("");
            tvResult.setText("✨ Результат ✨");
        } else if (item.getItemId() == MENU_QUIT_ID) {
            // Выход из приложения
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}