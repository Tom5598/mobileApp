package com.example.mytileshop;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.mytileshop.databinding.ActivityMainBinding;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final int CODE = 420;
    private static final String PREFKEY = MainActivity.class.getPackage().toString();
    private static final String LOG_TAG = MainActivity.class.getName();
    private EditText ET_Email;
    private EditText ET_Password;
    private SharedPreferences sharedPreferences;
    private FirebaseAuth firebaseAuth;
    private ImageView logo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ET_Email = findViewById(R.id.editTextUserEmail);
        ET_Password = findViewById(R.id.editTextPassword);
        sharedPreferences = getSharedPreferences(PREFKEY, MODE_PRIVATE);
        firebaseAuth = FirebaseAuth.getInstance();
        logo = findViewById(R.id.imageViewLogo);

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(3000);
        logo.startAnimation(fadeIn);
    }


    public void login(View view) {
        String strUserEmail = ET_Email.getText().toString();
        String strPassword = ET_Password.getText().toString();
        firebaseAuth.signInWithEmailAndPassword(strUserEmail, strPassword).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    startShopActivity();
                } else {
                    Toast.makeText(MainActivity.this, "Login failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Log.i("MainActivity", "Login info:" + strUserEmail + " -:- " + strPassword);
    }


    public void register(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        intent.putExtra("CODE", CODE);
        startActivity(intent);
    }

    private void startShopActivity() {
        Intent intent = new Intent(this, TileShoppingActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("useremail", ET_Email.getText().toString());
        editor.apply();
    }


}