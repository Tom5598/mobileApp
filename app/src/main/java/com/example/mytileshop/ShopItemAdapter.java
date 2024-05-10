package com.example.mytileshop;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mytileshop.model.ProductItem;

import java.util.ArrayList;

public class ShopItemAdapter extends RecyclerView.Adapter<ShopItemAdapter.ViewHolder> implements Filterable {
    private ArrayList<ProductItem> shopItemData;
    private ArrayList<ProductItem> shopItemDataAll;
    private Context context;
    private CartOps cartOps;
    private int lastPos = -1;

    ShopItemAdapter(Context context, ArrayList<ProductItem> productItemArrayList, CartOps cartOps) {
        this.shopItemData = productItemArrayList;
        this.shopItemDataAll = productItemArrayList;
        this.context = context;
        this.cartOps = cartOps;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ShopItemAdapter.ViewHolder holder, int pos) {
        ProductItem currentItem = shopItemData.get(pos);
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

    @Override
    public Filter getFilter() {
        return shopFilter;
    }

    private Filter shopFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<ProductItem> filteredList = new ArrayList<>();
            FilterResults filterResults = new FilterResults();
            if (constraint == null || constraint.length() == 0){
                filterResults.count = shopItemDataAll.size();
                filterResults.values = shopItemDataAll;
            }else {
                String filter = constraint.toString().toLowerCase().trim();
                for (ProductItem item : shopItemDataAll){
                    if (item.getName().toLowerCase().contains(filter)){
                        filteredList.add(item);
                    }
                }
                filterResults.count = filteredList.size();
                filterResults.values = filteredList;
            }

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            shopItemData = (ArrayList<ProductItem>) results.values;
            notifyDataSetChanged();
        }
    };
    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewTitle;
        private TextView textViewDesc;
        private TextView textViewPrice;
        private RatingBar ratingBar;
        private ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.itemTitle);
            textViewDesc = itemView.findViewById(R.id.subTitle);
            textViewPrice = itemView.findViewById(R.id.price);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            imageView = itemView.findViewById(R.id.itemImage);


        }

        public void bindTo(ProductItem currentItem) {
            textViewTitle.setText(currentItem.getName());
            textViewDesc.setText(currentItem.getDesc());
            textViewPrice.setText(currentItem.getPrice());
            ratingBar.setRating(currentItem.getRating());
            Glide.with(context).load(currentItem.getImgRes()).into(imageView);
            itemView.findViewById(R.id.add_to_cart).setOnClickListener(v -> {
                Log.d("TileShoppingActivity", "Add to cart clicked." + currentItem.getName());
                cartOps.addItemToCart(currentItem);
            });

        }
    }
}
