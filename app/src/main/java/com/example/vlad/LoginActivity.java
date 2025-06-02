import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    private EditText editTextUsername, editTextPassword, editTextConfirmPassword;
    private Button buttonRegister;
    private DatabaseHelper databaseHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        databaseHelper = new DatabaseHelper(this);
        
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }
    
    private void registerUser() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (databaseHelper.checkUsername(username)) {
            Toast.makeText(this, "Пользователь с таким именем уже существует", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (databaseHelper.registerUser(username, password)) {
            Toast.makeText(this, "Регистрация прошла успешно", Toast.LENGTH_SHORT).show();
            finish(); // Возврат к экрану входа
        } else {
            Toast.makeText(this, "Ошибка регистрации", Toast.LENGTH_SHORT).show();
        }
    }
}