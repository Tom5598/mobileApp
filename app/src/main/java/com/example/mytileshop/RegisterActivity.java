package com.example.mytileshop;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final int CODE = 420;
    private static final String LOG_TAG = RegisterActivity.class.getName();
    private static final String PREFKEY = RegisterActivity.class.getPackage().toString();
    private SharedPreferences sharedPreferences;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private EditText ET_Username;
    private EditText ET_Password;
    private EditText ET_PasswordAgain;
    private EditText ET_Email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        int code = getIntent().getIntExtra("CODE", -1);
        if (code != 420) {
            finish();
        }
        sharedPreferences = getSharedPreferences(PREFKEY, MODE_PRIVATE);
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        ET_Username = findViewById(R.id.editTextUserName);
        ET_Password = findViewById(R.id.editTextPassword);
        ET_PasswordAgain = findViewById(R.id.editTextPasswordAgain);
        ET_Email = findViewById(R.id.editTextTextEmailAddress);
        String strUserName = sharedPreferences.getString("useremail", "");
        ET_Username.setText(strUserName);


    }


    public void register(View view) {
        String strUserName = ET_Username.getText().toString();
        String strPassword = ET_Password.getText().toString();
        String strPasswordAgain = ET_PasswordAgain.getText().toString();
        String strEmail = ET_Email.getText().toString();
        if (strEmail.isEmpty() || strUserName.isEmpty() || strPassword.isEmpty() || strPasswordAgain.isEmpty()) {
            Log.e(LOG_TAG, "Certain input fields are empty!");
            return;
        }
        if (!strPassword.equals(strPasswordAgain)) {
            Log.e(LOG_TAG, "Passwords are not the same!");
            return;
        }

        firebaseAuth.createUserWithEmailAndPassword(strEmail, strPassword).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    createUserDocument(firebaseUser);
                    createInitialCart(firebaseUser.getUid());
                    startShopActivity();
                } else {
                    Toast.makeText(RegisterActivity.this, "Registration failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void createUserDocument(FirebaseUser user) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", user.getEmail());
        userData.put("uid", user.getUid());
        userData.put("address", "Unkown");
        userData.put("phonenumber", "Unkown");

        firebaseFirestore.collection("Users").document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> Log.d("RegisterActivity", "User Document Created"))
                .addOnFailureListener(e -> Log.e("RegisterActivity", "Error adding user document", e));
    }

    private void createInitialCart(String userId) {
        Map<String, Object> cartData = new HashMap<>();
        cartData.put("items", new ArrayList<>()); // Start with an empty cart

        firebaseFirestore.collection("Carts").document(userId)
                .set(cartData)
                .addOnSuccessListener(aVoid -> Log.d("RegisterActivity", "Cart Document Created"))
                .addOnFailureListener(e -> Log.e("RegisterActivity", "Error adding cart document", e));
    }

    private void startShopActivity() {
        Intent intent = new Intent(this, TileShoppingActivity.class);
        intent.putExtra("CODE", CODE);
        startActivity(intent);
    }

    public void cancel(View view) {
        finish();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String chosen = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}