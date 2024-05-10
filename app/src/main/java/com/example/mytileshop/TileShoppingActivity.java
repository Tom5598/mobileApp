package com.example.mytileshop;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.MenuItemCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytileshop.model.ProductItem;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class TileShoppingActivity extends AppCompatActivity implements CartOps {
    private static final String LOG_TAG = TileShoppingActivity.class.getName();
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firebaseFirestore;
    private RecyclerView recyclerView;
    private ArrayList<ProductItem> itemListArray;
    private CollectionReference productCollectionsRef;
    private ShopItemAdapter shopItemAdapter;
    private final int GRID = 3;
    private int limitSize = 16;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tile_shopping);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            Log.d(LOG_TAG,"Login successful!");
        } else {
            finish();
        }
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, GRID));
        itemListArray = new ArrayList<>();
        shopItemAdapter = new ShopItemAdapter(this, itemListArray, this);
        recyclerView.setAdapter(shopItemAdapter);

        firebaseFirestore= FirebaseFirestore.getInstance();
        productCollectionsRef= firebaseFirestore.collection("Products");
        obtainData();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        this.registerReceiver(broadcastReceiver,intentFilter);

    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action==null){
                return;
            }
            switch (action){
                case Intent.ACTION_POWER_CONNECTED:
                    limitSize = 16;
                    break;
                case Intent.ACTION_POWER_DISCONNECTED:
                    limitSize = 6;
                    break;
            }
            obtainData();
        }
    };

    private void obtainData() {
        itemListArray.clear();
        productCollectionsRef
                .orderBy("name", Query.Direction.ASCENDING)
                .limit(limitSize)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot doc: queryDocumentSnapshots){
                    itemListArray.add(doc.toObject(ProductItem.class));
                }
                if (itemListArray.isEmpty()){
                    finish();
                }
                shopItemAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.app_list_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.searchbar_on_menu);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String string) {
                shopItemAdapter.getFilter().filter(string);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.userDataBttn) {
            Intent intent = new Intent(TileShoppingActivity.this, UserDataActivity.class);
            startActivity(intent);
        }
        if (item.getItemId() == R.id.logoutBttn) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(TileShoppingActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        if (item.getItemId() == R.id.cart) {
            Intent intent = new Intent(TileShoppingActivity.this, CartActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void addItemToCart(ProductItem product) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference cartRef = FirebaseFirestore.getInstance().collection("Carts").document(userId);

        cartRef.update("items", FieldValue.arrayUnion(
                new HashMap<String, Object>() {{
                    put("productId", product.getId());
                    put("quantity", 1); // Update logic here if needed
                }}
        )).addOnSuccessListener(aVoid -> {
            Log.d("TileShoppingActivity", "Item added to cart: " + product.getName());
        }).addOnFailureListener(e -> {
            Log.e("TileShoppingActivity", "Error adding item to cart", e);
        });
    }
}