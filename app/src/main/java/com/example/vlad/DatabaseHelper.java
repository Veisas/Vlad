import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "PasswordManager.db";
    private static final int DATABASE_VERSION = 1;
    
    // Таблица пользователей
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    
    // Таблица паролей
    private static final String TABLE_PASSWORDS = "passwords";
    private static final String COLUMN_PASSWORD_ID = "password_id";
    private static final String COLUMN_SERVICE = "service";
    private static final String COLUMN_LOGIN = "login";
    private static final String COLUMN_PASSWORD_VALUE = "password_value";
    private static final String COLUMN_NOTES = "notes";
    private static final String COLUMN_USER_FK = "user_fk";
    
    // Ключ для шифрования (в реальном приложении должен быть более безопасным)
    private static final String ENCRYPTION_KEY = "MySuperSecretKey123";
    
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Создание таблицы пользователей
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USERNAME + " TEXT UNIQUE,"
                + COLUMN_PASSWORD + " TEXT" + ")";
        db.execSQL(CREATE_USERS_TABLE);
        
        // Создание таблицы паролей
        String CREATE_PASSWORDS_TABLE = "CREATE TABLE " + TABLE_PASSWORDS + "("
                + COLUMN_PASSWORD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_SERVICE + " TEXT,"
                + COLUMN_LOGIN + " TEXT,"
                + COLUMN_PASSWORD_VALUE + " TEXT,"
                + COLUMN_NOTES + " TEXT,"
                + COLUMN_USER_FK + " INTEGER,"
                + "FOREIGN KEY(" + COLUMN_USER_FK + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")" + ")";
        db.execSQL(CREATE_PASSWORDS_TABLE);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PASSWORDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }
    
    // Регистрация нового пользователя
    public boolean registerUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        // Хеширование пароля перед сохранением
        String hashedPassword = hashPassword(password);
        
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, hashedPassword);
        
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }
    
    // Проверка существования пользователя
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String hashedPassword = hashPassword(password);
        
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_USERNAME + " = ?" + " AND " + COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {username, hashedPassword};
        
        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        
        return count > 0;
    }
    
    // Проверка, существует ли пользователь с таким именем
    public boolean checkUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};
        
        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        
        return count > 0;
    }
    
    // Получение ID пользователя по имени
    public int getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};
        
        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(COLUMN_USER_ID));
            cursor.close();
            return id;
        }
        cursor.close();
        return -1;
    }
    
    // Добавление нового пароля
    public boolean addPassword(int userId, String service, String login, String password, String notes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        try {
            String encryptedPassword = encrypt(password);
            values.put(COLUMN_SERVICE, service);
            values.put(COLUMN_LOGIN, login);
            values.put(COLUMN_PASSWORD_VALUE, encryptedPassword);
            values.put(COLUMN_NOTES, notes);
            values.put(COLUMN_USER_FK, userId);
            
            long result = db.insert(TABLE_PASSWORDS, null, values);
            return result != -1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Получение всех паролей пользователя
    public Cursor getAllPasswords(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {
                COLUMN_PASSWORD_ID,
                COLUMN_SERVICE,
                COLUMN_LOGIN,
                COLUMN_PASSWORD_VALUE,
                COLUMN_NOTES
        };
        String selection = COLUMN_USER_FK + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};
        
        return db.query(TABLE_PASSWORDS, columns, selection, selectionArgs, null, null, COLUMN_SERVICE + " ASC");
    }
    
    // Получение пароля по ID
    public String getPasswordById(int passwordId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_PASSWORD_VALUE};
        String selection = COLUMN_PASSWORD_ID + " = ?";
        String[] selectionArgs = {String.valueOf(passwordId)};
        
        Cursor cursor = db.query(TABLE_PASSWORDS, columns, selection, selectionArgs, null, null, null);
        if (cursor.moveToFirst()) {
            String encryptedPassword = cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD_VALUE));
            cursor.close();
            try {
                return decrypt(encryptedPassword);
            } catch (Exception e) {
                e.printStackTrace();
                return "Error";
            }
        }
        cursor.close();
        return "";
    }
    
    // Удаление пароля
    public boolean deletePassword(int passwordId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_PASSWORDS, COLUMN_PASSWORD_ID + " = ?", new String[]{String.valueOf(passwordId)}) > 0;
    }
    
    // Хеширование пароля (для аутентификации)
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.encodeToString(hash, Base64.DEFAULT);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password; // В реальном приложении так делать не стоит
        }
    }
    
    // Шифрование пароля
    private String encrypt(String value) throws Exception {
        SecretKeySpec key = generateKey();
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedValue = cipher.doFinal(value.getBytes());
        return Base64.encodeToString(encryptedValue, Base64.DEFAULT);
    }
    
    // Дешифрование пароля
    private String decrypt(String encrypted) throws Exception {
        SecretKeySpec key = generateKey();
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedValue = Base64.decode(encrypted, Base64.DEFAULT);
        byte[] decryptedValue = cipher.doFinal(decodedValue);
        return new String(decryptedValue);
    }
    
    // Генерация ключа для шифрования
    private SecretKeySpec generateKey() throws Exception {
        byte[] key = new byte[16];
        byte[] keyBytes = ENCRYPTION_KEY.getBytes("UTF-8");
        System.arraycopy(keyBytes, 0, key, 0, Math.min(keyBytes.length, key.length));
        return new SecretKeySpec(key, "AES");
    }
}