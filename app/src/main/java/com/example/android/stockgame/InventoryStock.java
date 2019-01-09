package com.example.android.stockgame;

public class InventoryStock {

    private String mName;
    private String mAmount;
    private String mPrice;
    private String mTotal;

    public InventoryStock(){

    }
    public InventoryStock(String name, String amount, String price, String total){
        mAmount = amount;
        mName = name;
        mPrice = price;
        mTotal = total;
    }

    public void setName(String name){mName = name;}
    public void setAmount(String amount) {mAmount = amount;}
    public void setPrice(String price){mPrice = price;}
    public void setTotal(String total){mTotal = total;}

    public String getName(){return mName;}
    public String getAmount(){return mAmount;}
    public String getPrice(){return mPrice;}
    public String getTotal(){return mTotal;}
}
