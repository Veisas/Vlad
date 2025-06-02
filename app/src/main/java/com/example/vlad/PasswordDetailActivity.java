import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class PasswordDetailActivity extends AppCompatActivity {
    private TextView textViewService, textViewLogin, textViewPassword, textViewNotes;
    private DatabaseHelper databaseHelper;
    private int passwordId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_detail);
        
        passwordId = getIntent().getIntExtra("PASSWORD_ID", -1);
        if (passwordId == -1) {
            finish(); // Если ID пароля не передан, закрываем активность
            return;
        }
        
        databaseHelper = new DatabaseHelper(this);
        
        textViewService = findViewById(R.id.textViewService);
        textViewLogin = findViewById(R.id.textViewLogin);
        textViewPassword = findViewById(R.id.textViewPassword);
        textViewNotes = findViewById(R.id.textViewNotes);
        
        loadPasswordDetails();
    }
    
    private void loadPasswordDetails() {
        Cursor cursor = databaseHelper.getAllPasswords(databaseHelper.getUserId(getIntent().getStringExtra("USERNAME")));
        if (cursor.moveToFirst()) {
            do {
                int currentId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_PASSWORD_ID));
                if (currentId == passwordId) {
                    textViewService.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_SERVICE)));
                    textViewLogin.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_LOGIN)));
                    
                    // Получение и отображение расшифрованного пароля
                    String decryptedPassword = databaseHelper.getPasswordById(passwordId);
                    textViewPassword.setText(decryptedPassword);
                    
                    String notes = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NOTES));
                    if (notes != null && !notes.isEmpty()) {
                        textViewNotes.setText(notes);
                    } else {
                        textViewNotes.setText("Нет заметок");
                    }
                    break;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.password_detail_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_delete_password) {
            boolean isDeleted = databaseHelper.deletePassword(passwordId);
            if (isDeleted) {
                Toast.makeText(this, "Пароль удален", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Ошибка при удалении пароля", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
}