package com.example.mytileshop;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.MenuItemCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytileshop.model.CartItem;
import com.example.mytileshop.model.ProductItem;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartActivity extends AppCompatActivity {
    private static final String LOG_TAG = CartActivity.class.getName();
    private static final String CHANNEL_ID = "purchase_notifications";
    private static final int NOTIFICATION_ID = 1;

    private FirebaseUser firebaseUser;
    private FirebaseFirestore firebaseFirestore;
    private String userID;
    private RecyclerView recyclerView;
    private ArrayList<CartItem> itemListArray;
    private CartAdapter cartAdapter;
    private final int GRID = 2;
    private TextView totalPriceTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            Log.d(LOG_TAG, "Current user retrieved.");
        } else {
            Intent intent = new Intent(CartActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            finish();
        }
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firebaseFirestore=FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.recyclerViewCart);
        recyclerView.setLayoutManager(new GridLayoutManager(this, GRID));
        itemListArray = new ArrayList<>();
        cartAdapter = new CartAdapter(this, itemListArray);
        recyclerView.setAdapter(cartAdapter);
        totalPriceTextView = findViewById(R.id.totalPriceTextView);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 0);
            }
        }
        fetchCartItems();

    }
    //Notification related------>
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("CartActivity", "Notification permission granted");
            } else {
                Log.d("CartActivity", "Notification permission denied");
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Purchase Notifications";
            String description = "Notifications for completed purchases";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendPurchaseNotification() {
        createNotificationChannel();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission not granted", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Purchase Completed")
                .setContentText("Thank you for your purchase! Your order has been placed successfully.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
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
            Intent intent = new Intent(CartActivity.this, UserDataActivity.class);
            startActivity(intent);
        }
        if (item.getItemId() == R.id.back_arrow) {
            Intent intent = new Intent(CartActivity.this, TileShoppingActivity.class);
            startActivity(intent);
        }
        if (item.getItemId() == R.id.logoutBttn) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(CartActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
    private void fetchCartItems() {
        firebaseFirestore.collection("Carts").document(userID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> items = (List<Map<String, Object>>) documentSnapshot.get("items");
                        if (null == items){
                            Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_LONG).show();
                        }else {
                            if (items.isEmpty()) {
                                Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_LONG).show();
                            } else {
                                for (Map<String, Object> item : items) {
                                    fetchProductDetails(item);
                                }
                            }
                        }

                    } else {
                        Toast.makeText(this, "Your cart was not created. Contact support!", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> Log.e("CartActivity", "Error loading cart items", e));
    }

    private void fetchProductDetails(Map<String, Object> cartItem) {
        String productId = (String) cartItem.get("productId");
        firebaseFirestore.collection("Products").document(productId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        CartItem c = new CartItem(documentSnapshot.toObject(ProductItem.class));
                        itemListArray.add(c);
                        cartAdapter.notifyDataSetChanged();
                        calculateTotal();
                    }
                })
                .addOnFailureListener(e -> Log.e("CartActivity", "Error loading product details", e));
    }

    public void placeOrder(View view) {
        if (itemListArray.isEmpty()) {
            Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
            return;
        }
        double total = calculateTotal();
        createOrder(total);
    }

    double calculateTotal() {
        double total = 0;
        for (CartItem item : itemListArray) {
            total += item.getPrice() * item.getQuantity();
        }
        totalPriceTextView.setText(String.valueOf(total));
        return total;
    }

    private void createOrder(double total) {
        String userId = firebaseUser.getUid();
        Map<String, Object> orderDetails = new HashMap<>();
        orderDetails.put("uid", userId);
        orderDetails.put("totalPrice", total);
        orderDetails.put("date", new Timestamp(new Date()));
        orderDetails.put("status", "Pending");

        List<Map<String, Object>> products = new ArrayList<>();
        for (CartItem item : itemListArray) {
            Map<String, Object> productDetail = new HashMap<>();
            productDetail.put("productId", item.getId());
            productDetail.put("name", item.getName());
            productDetail.put("price", item.getPrice());
            productDetail.put("quantity", item.getQuantity());
            products.add(productDetail);
        }
        orderDetails.put("products", products);

        firebaseFirestore.collection("Orders")
                .add(orderDetails)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_LONG).show();
                    clearCart(userId);
                    sendPurchaseNotification();
                    Intent intent = new Intent(this, TileShoppingActivity.class);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to place order.", Toast.LENGTH_LONG).show());
    }

    private void clearCart(String userId) {
        DocumentReference cartRef = firebaseFirestore.collection("Carts").document(userId);
        cartRef.update("items", FieldValue.delete())
                .addOnSuccessListener(aVoid -> {
                    itemListArray.clear();
                    cartAdapter.notifyDataSetChanged();
                    Toast.makeText(this, "Cart has been cleared.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Log.e(LOG_TAG, "Error clearing the cart", e));
    }
}