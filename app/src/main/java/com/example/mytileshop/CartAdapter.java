package com.example.mytileshop;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mytileshop.model.CartItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder>  {
    private ArrayList<CartItem> shopItemData;
    private Context context;
    private EditText quantityEditText;
    private int lastPos = -1;

    CartAdapter(Context context, ArrayList<CartItem> productItemArrayList) {
        this.shopItemData = productItemArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.cart_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CartAdapter.ViewHolder holder, int pos) {
        CartItem currentItem = shopItemData.get(pos);
        holder.bindTo(currentItem);
        if (holder.getAdapterPosition()> lastPos){
            Animation animation = AnimationUtils.loadAnimation(context,R.anim.item_flow_in);
            holder.itemView.startAnimation(animation);
            lastPos= holder.getAdapterPosition();
        }
    }

    @Override
    public int getItemCount() {
        return shopItemData.size();
    }
    public void removeItem(int position) {
        shopItemData.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, shopItemData.size());

    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView textViewTitle;
        private TextView textViewPrice;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.cartItemTitle);
            textViewPrice = itemView.findViewById(R.id.cartPrice);
            imageView = itemView.findViewById(R.id.cartItemImage);
            quantityEditText = itemView.findViewById(R.id.cartEditTextNumberSigned);

            quantityEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    int quantity;
                    try {
                        quantity = Integer.parseInt(s.toString());
                    } catch (NumberFormatException e) {
                        quantity = 1;
                    }
                    CartItem item = shopItemData.get(getAdapterPosition());
                    if (quantity<1){
                        item.setQuantity(1);
                    }else {
                        item.setQuantity(Math.min(quantity, 999));
                    }
                }
            });
        }

        public void bindTo(CartItem currentItem) {
            textViewTitle.setText(currentItem.getName());
            textViewPrice.setText(String.valueOf(currentItem.getPrice()) );
            Glide.with(context).load(currentItem.getImgRes()).into(imageView);
            quantityEditText.setText(String.valueOf(currentItem.getQuantity()));

            currentItem.setQuantityChangeListener(() -> {
                ((CartActivity) context).calculateTotal();
            });
            itemView.findViewById(R.id.delete_from_cart).setOnClickListener(v -> {
                Log.d("CartActivity", "Delete from cart clicked.");
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    removeItemFromFirestore(currentItem.getId(), position);
                }
            });
        }
        private void removeItemFromFirestore(String productId, int position) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DocumentReference cartRef = FirebaseFirestore.getInstance().collection("Carts").document(userId);
            cartRef.get().addOnSuccessListener(documentSnapshot -> {
                List<Map<String, Object>> items = (List<Map<String, Object>>) documentSnapshot.get("items");
                List<Map<String, Object>> updatedItems = new ArrayList<>();
                for (Map<String, Object> item : items) {
                    if (!item.get("productId").equals(productId)) {
                        updatedItems.add(item);
                    }
                }
                cartRef.update("items", updatedItems)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("CartActivity", "Item removed from Firestore cart");
                            removeItem(position);
                            ((CartActivity) context).calculateTotal();

                        })
                        .addOnFailureListener(e -> Log.e("CartActivity", "Error removing item from Firestore cart", e));
            });
        }

    }
}
