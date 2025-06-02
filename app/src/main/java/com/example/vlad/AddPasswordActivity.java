import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AddPasswordActivity extends AppCompatActivity {
    private EditText editTextService, editTextLogin, editTextPassword, editTextNotes;
    private CheckBox checkBoxShowPassword;
    private Button buttonSave;
    private int userId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_password);
        
        userId = getIntent().getIntExtra("USER_ID", -1);
        if (userId == -1) {
            finish(); // Если ID пользователя не передан, закрываем активность
            return;
        }
        
        editTextService = findViewById(R.id.editTextService);
        editTextLogin = findViewById(R.id.editTextLogin);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextNotes = findViewById(R.id.editTextNotes);
        checkBoxShowPassword = findViewById(R.id.checkBoxShowPassword);
        buttonSave = findViewById(R.id.buttonSave);
        
        // Обработка показа/скрытия пароля
        checkBoxShowPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    editTextPassword.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    editTextPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                editTextPassword.setSelection(editTextPassword.getText().length());
            }
        });
        
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePassword();
            }
        });
    }
    
    private void savePassword() {
        String service = editTextService.getText().toString().trim();
        String login = editTextLogin.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String notes = editTextNotes.getText().toString().trim();
        
        if (service.isEmpty() || login.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните обязательные поля", Toast.LENGTH_SHORT).show();
            return;
        }
        
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        boolean isSuccess = databaseHelper.addPassword(userId, service, login, password, notes);
        
        if (isSuccess) {
            Toast.makeText(this, "Пароль сохранен", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Ошибка при сохранении пароля", Toast.LENGTH_SHORT).show();
        }
    }
}