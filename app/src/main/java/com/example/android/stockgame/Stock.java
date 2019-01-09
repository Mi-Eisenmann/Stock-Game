package com.example.android.stockgame;

public class Stock {

    private String mSymbol;
    private String mName;
    private double mValue;

    public Stock(){

    }
    public Stock(String sym, String name, double val){
        mSymbol = sym;
        mName = name;
        mValue = val;
    }

    public void setSymbol(String symbol){mSymbol = symbol;}
    public void setName(String name){mName = name;}
    public void setValue(double value){mValue = value;}

    public String getSymbol(){return mSymbol;}
    public String getName(){return mName;}
    public double getValue(){return mValue;}
}
