package com.example.vlad;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private ListView listViewPasswords;
    private TextView textViewNoPasswords;
    private DatabaseHelper databaseHelper;
    private String username;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Получение имени пользователя из Intent
        username = getIntent().getStringExtra("USERNAME");
        if (username == null) {
            finish(); // Если имя пользователя не передано, закрываем активность
            return;
        }

        databaseHelper = new DatabaseHelper(this);
        userId = databaseHelper.getUserId(username);

        listViewPasswords = findViewById(R.id.listViewPasswords);
        textViewNoPasswords = findViewById(R.id.textViewNoPasswords);

        // Настройка списка паролей
        setupPasswordList();

        // Обработка клика по элементу списка
        listViewPasswords.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, PasswordDetailActivity.class);
                intent.putExtra("PASSWORD_ID", (int) id);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupPasswordList(); // Обновление списка при возвращении на экран
    }

    private void setupPasswordList() {
        Cursor cursor = databaseHelper.getAllPasswords(userId);

        if (cursor.getCount() == 0) {
            textViewNoPasswords.setVisibility(View.VISIBLE);
            listViewPasswords.setVisibility(View.GONE);
        } else {
            textViewNoPasswords.setVisibility(View.GONE);
            listViewPasswords.setVisibility(View.VISIBLE);

            String[] fromColumns = {DatabaseHelper.COLUMN_SERVICE, DatabaseHelper.COLUMN_LOGIN};
            int[] toViews = {R.id.textViewService, R.id.textViewLogin};

            SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                    this,
                    R.layout.password_list_item,
                    cursor,
                    fromColumns,
                    toViews,
                    0);

            listViewPasswords.setAdapter(adapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add_password) {
            Intent intent = new Intent(this, AddPasswordActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_logout) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}