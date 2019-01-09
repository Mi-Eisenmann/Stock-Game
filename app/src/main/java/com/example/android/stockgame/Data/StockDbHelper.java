package com.example.android.stockgame.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class StockDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = StockDbHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "portfolio.db";
    private static final int DATABASE_VERSION = 1;

    public StockDbHelper(Context context) { super(context, DATABASE_NAME, null, DATABASE_VERSION); }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_PORTFOLIO_TABLE = "CREATE TABLE " + StockContract.StockEntry.TABLE_NAME + " ("
                + StockContract.StockEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + StockContract.StockEntry.COLUMN_STOCK_NAME + " TEXT NOT NULL, "
                + StockContract.StockEntry.COLUMN_STOCK_SYMBOL + " TEXT NOT NULL, "
                + StockContract.StockEntry.COLUMN_STOCK_AMOUNT + " INTEGER /*DEFAULT 0*/, "
                + StockContract.StockEntry.COLUMN_STOCK_PRICE + " REAL /*DEFAULT 0*/, "
                + StockContract.StockEntry.COLUMN_STOCK_TOTALVALUE + " REAL /*DEFAULT 0*/);";

        db.execSQL(SQL_CREATE_PORTFOLIO_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
