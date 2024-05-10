package com.example.mytileshop.model;

public class ProductItem {
    private String id;
    private String name;
    private String price;
    private String desc;
    private float rating;
    private int imgRes;

    public ProductItem(){}
    public ProductItem(String id, String name, String price, String desc, float rating, int imgRes) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.desc = desc;
        this.rating = rating;
        this.imgRes = imgRes;
    }
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getPrice() {
        return price;
    }
    public String getDesc() {
        return desc;
    }
    public float getRating() {
        return rating;
    }
    public int getImgRes() {
        return imgRes;
    }


}
