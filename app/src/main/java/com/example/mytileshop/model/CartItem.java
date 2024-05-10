package com.example.mytileshop.model;

import com.example.mytileshop.QuantityChangeListener;

public class CartItem implements QuantityChangeListener {
    private String id;
    private String name;
    private int price;
    private int imgRes;
    private int quantity;
    private QuantityChangeListener quantityChangeListener;
    public CartItem(){}
    public CartItem(ProductItem p){
        this.id=p.getId();
        this.name = p.getName();
        this.price = Integer.parseInt(p.getPrice().replaceAll("\\D+",""));
        this.imgRes = p.getImgRes();
        this.quantity = 1;
    }
    public CartItem(String id, String name, int price, int imgRes, int quantity) {
        this.id=id;
        this.name = name;
        this.price = price;
        this.imgRes = imgRes;
        this.quantity = quantity;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getImgRes() {
        return imgRes;
    }

    public void setImgRes(int imgRes) {
        this.imgRes = imgRes;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        if (quantityChangeListener != null) {
            quantityChangeListener.onQuantityChanged();
        }
    }

    public String getId() {
        return id;
    }

    @Override
    public void onQuantityChanged() {

    }
    public void setQuantityChangeListener(QuantityChangeListener listener) {
        this.quantityChangeListener = listener;
    }
}
