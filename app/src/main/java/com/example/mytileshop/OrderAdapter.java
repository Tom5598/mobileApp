package com.example.mytileshop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytileshop.model.OrderItem;

import java.util.ArrayList;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private Context context;
    private ArrayList<OrderItem> orderList;

    public OrderAdapter(Context context, ArrayList<OrderItem> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.order_list, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderItem orderItem = orderList.get(position);
        holder.bind(orderItem);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderIdTextView, orderDateTextView, orderStatusTextView, orderTotalPriceTextView;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdTextView = itemView.findViewById(R.id.orderID);
            orderDateTextView = itemView.findViewById(R.id.orderDate);
            orderStatusTextView = itemView.findViewById(R.id.orderStatus);
            orderTotalPriceTextView = itemView.findViewById(R.id.orderTotal);
        }

        void bind(OrderItem orderItem) {
            orderIdTextView.setText(String.format("Order ID: %s", orderItem.getOrderId()));
            if (orderItem.getDate() != null) {
                orderDateTextView.setText(orderItem.getDate().toDate().toString());
            } else {
                orderDateTextView.setText("Date not available.");
            }
            orderStatusTextView.setText(String.format("Order Status: %s", orderItem.getStatus()));
            orderTotalPriceTextView.setText(String.format(Locale.US, "%.0f Ft", orderItem.getTotalPrice()));
        }
    }
}
