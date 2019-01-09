package com.example.android.stockgame.Data;

import android.provider.BaseColumns;

public class StockContract {

    private StockContract(){}

    public static final class StockEntry implements BaseColumns {

        public final static String TABLE_NAME = "stocks";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_STOCK_NAME = "name";
        public final static String COLUMN_STOCK_SYMBOL = "symbol";
        public final static String COLUMN_STOCK_AMOUNT = "amount";
        public final static String COLUMN_STOCK_PRICE = "price";
        public final static String COLUMN_STOCK_TOTALVALUE = "totalvalue";
    }



}
