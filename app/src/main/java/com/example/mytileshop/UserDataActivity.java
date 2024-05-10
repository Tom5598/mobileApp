package com.example.mytileshop;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.firestore.DocumentReference;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytileshop.model.CartItem;
import com.example.mytileshop.model.OrderItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDataActivity extends AppCompatActivity {
    private static final String LOG_TAG = UserDataActivity.class.getName();
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firebaseFirestore;
    private RecyclerView recyclerView;
    private ArrayList<OrderItem> orderList;
    private OrderAdapter orderAdapter;
    private final int GRID = 1;
    private String userId;
    private TextView textViewEmail;
    private EditText editTextPostalAddress;
    private EditText editTextPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_data);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            Log.d(LOG_TAG, "Current user retrieved.");
        } else {
            Intent intent = new Intent(UserDataActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            finish();
        }
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firebaseFirestore= FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.recyclerViewOrders);
        recyclerView.setLayoutManager(new GridLayoutManager(this, GRID));
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(this, orderList);
        recyclerView.setAdapter(orderAdapter);
        textViewEmail = findViewById(R.id.TVUserDataEmail);
        editTextPostalAddress = findViewById(R.id.editTextTextPostalAddress);
        editTextPhone = findViewById(R.id.editTextPhone);

        getUserData();
        fetchOrders();
    }
    private void getUserData(){
        firebaseFirestore.collection("Users")
                .whereEqualTo("uid", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QueryDocumentSnapshot d = (QueryDocumentSnapshot) task.getResult().getDocuments().get(0);
                        textViewEmail.setText(d.get("email").toString());
                        editTextPostalAddress.setText(d.get("address").toString());
                        editTextPhone.setText(d.get("phonenumber").toString());
                    } else {
                        Log.e(LOG_TAG, "Error getting orders: ", task.getException());
                    }
                });
    }
    private void fetchOrders() {
        Query q = firebaseFirestore.collection("Orders")
                .whereEqualTo("uid", userId)
                .orderBy("date", Query.Direction.DESCENDING);

                q.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            OrderItem order = document.toObject(OrderItem.class);
                            order.setOrderId(document.getId());
                            orderList.add(order);
                        }
                        orderAdapter.notifyDataSetChanged();
                    } else {
                        Log.e(LOG_TAG, "Error getting orders: ", task.getException());
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.app_menu, menu);
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.userDataBttn) {
            Intent intent = new Intent(UserDataActivity.this, UserDataActivity.class);
            startActivity(intent);
        }
        if (item.getItemId() == R.id.back_arrow) {
            Intent intent = new Intent(UserDataActivity.this, TileShoppingActivity.class);
            startActivity(intent);
        }
        if (item.getItemId() == R.id.logoutBttn) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(UserDataActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateUserData(View view) {
        String phone = editTextPhone.getText().toString();
        String address = editTextPostalAddress.getText().toString();
        if (!TextUtils.isEmpty(phone) && !TextUtils.isEmpty(address)) {
            if (firebaseUser != null) {
                Map<String, Object> updates = new HashMap<>();
                updates.put("phonenumber", phone);
                updates.put("address", address);

                firebaseFirestore.collection("Users").document(userId)
                        .update(updates)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "User data updated successfully!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to update user data.", Toast.LENGTH_SHORT).show();
                            Log.e("updateUserData", "Error updating document", e);
                        });

                getUserData();
            } else {
                Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Phone number or address cannot be empty.", Toast.LENGTH_SHORT).show();
        }
    }

}