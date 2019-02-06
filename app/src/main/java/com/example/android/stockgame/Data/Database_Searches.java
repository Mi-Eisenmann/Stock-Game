package com.example.android.stockgame.Data;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.example.android.stockgame.Data.StockContract.StockEntry;

import com.example.android.stockgame.Inventory;

public class Database_Searches {


    // Determine the index of a specific stock in the database
    public long indexOfStock (String Symbol) {

        SQLiteDatabase db = Inventory.mDbHelper.getReadableDatabase();

        String query = "SELECT _ID FROM stocks WHERE symbol =?";
        return DatabaseUtils.longForQuery(db, query, new String[]{ Symbol });
    }


    // Read out the amount of a specific stock you posses
    public long getStockAmount (String position) {

        SQLiteDatabase db = Inventory.mDbHelper.getReadableDatabase();

        String query = "SELECT amount FROM stocks WHERE _ID =?";
        return DatabaseUtils.longForQuery(db, query, new String [] {position} );
    }


    // Do we posses a certain stock?
    public boolean containsStock(String Symbol) {

        SQLiteDatabase db = Inventory.mDbHelper.getReadableDatabase();

        //String Query = "SELECT * FROM " + "stocks" + " WHERE " + "symbol" + " =?"
        Cursor cursor = db.rawQuery("SELECT * FROM stocks WHERE symbol =?", new String[]{Symbol});

        boolean hasObject = false;
        if (cursor.moveToFirst()) {
            hasObject = true;
        }

        cursor.close();
        //db.close();
        return hasObject;

    }


    // Calculate the sum of the current money and the values of all stocks
    public String getTotalValue(){
        double currentTotal = 0;
        double totalTotal = 0;

        SQLiteDatabase db = Inventory.mDbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT totalvalue FROM stocks", new String[]{});

        int totalColumnIndex = cursor.getColumnIndex(StockEntry.COLUMN_STOCK_TOTALVALUE);

        while (cursor.moveToNext()) {
            currentTotal = cursor.getDouble(totalColumnIndex);
            totalTotal += currentTotal;
        }
        cursor.close();

        // Round it to two digits
        totalTotal = (double) Math.round(totalTotal*100)/100;

        return String.valueOf( totalTotal );
    }

    // Read out the users money
    public double getUserMoney() {
        double currentTotal = 0;

        SQLiteDatabase db = Inventory.mDbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT totalvalue FROM stocks WHERE _ID =?", new String[]{"1"});

        int totalColumnIndex = cursor.getColumnIndex(StockEntry.COLUMN_STOCK_TOTALVALUE);

        // Iterate through all the returned rows in the cursor
        while (cursor.moveToNext()) {
            currentTotal = cursor.getDouble(totalColumnIndex);
        }
        cursor.close();

        String Text = Double.toString(currentTotal);
        //Toast.makeText(this, Text, Toast.LENGTH_SHORT).show();

        return currentTotal;
    }


    // For modifying a stock-entry (after buying or selling) we need the corresponding database-ID to this stock
    public int getIdFromSymbol(String symbol) {
        int id = -1;
        try {
            SQLiteDatabase db = Inventory.mDbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT _ID FROM stocks WHERE symbol =?", new String[]{symbol});
            if (cursor.getCount() > 0) {
                if (cursor.moveToFirst()) {
                    id = cursor.getInt(0);
                }
                //  cursor.close();
                return id;
            }
            else {
                //Toast.makeText(this,"Stopped here",Toast.LENGTH_LONG).show();
                return -1;
            }
        } catch (Exception e) {
            //Toast.makeText(this,"Didn't work",Toast.LENGTH_LONG).show();
            return -1;
        }

    }


    // Return all information to a certain stock from the database
    public String getEntryInformation(String position, String column) {
        long longOutput = 0;
        double doubleOutput = 0;
        String stringOutput = "";

        SQLiteDatabase db = Inventory.mDbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT "+ column + " FROM stocks WHERE _ID =?", new String[]{position});

        int idColumnIndex = cursor.getColumnIndex(StockEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(StockEntry.COLUMN_STOCK_NAME);
        int symbolColumnIndex = cursor.getColumnIndex(StockEntry.COLUMN_STOCK_SYMBOL);
        int amountColumnIndex = cursor.getColumnIndex(StockEntry.COLUMN_STOCK_AMOUNT);
        int priceColumnIndex = cursor.getColumnIndex(StockEntry.COLUMN_STOCK_PRICE);
        int totalColumnIndex = cursor.getColumnIndex(StockEntry.COLUMN_STOCK_TOTALVALUE);

        if (column == "name") {
            while (cursor.moveToNext()) {
                stringOutput = cursor.getString(nameColumnIndex);
            }
            cursor.close();

            //String Text = Double.toString(doubleOutput);
            //Toast.makeText(this, stringOutput, Toast.LENGTH_SHORT).show();

            cursor.close();
            return stringOutput;
        }
        else if (column == "symbol") {
            while (cursor.moveToNext()) {
                stringOutput = cursor.getString(symbolColumnIndex);
            }
            cursor.close();

            //String Text = Double.toString(doubleOutput);
            //Toast.makeText(this, stringOutput, Toast.LENGTH_SHORT).show();

            cursor.close();
            return stringOutput;
        }
        else if (column == "amount") {
            while (cursor.moveToNext()) {
                longOutput = cursor.getLong(amountColumnIndex);
            }
            cursor.close();

            String Text = Long.toString(longOutput);
            //Toast.makeText(this, Text, Toast.LENGTH_SHORT).show();

            cursor.close();
            return Text;
        }
        else if (column == "price") {
            while (cursor.moveToNext()) {
                doubleOutput = cursor.getDouble(priceColumnIndex);
            }
            cursor.close();

            String Text = Double.toString(doubleOutput);
            //Toast.makeText(this, Text, Toast.LENGTH_SHORT).show();

            cursor.close();
            return Text;
        }
        else if (column == "totalvalue") {
            while (cursor.moveToNext()) {
                doubleOutput = cursor.getDouble(totalColumnIndex);
            }
            cursor.close();

            String Text = Double.toString(doubleOutput);
            //Toast.makeText(this, Text, Toast.LENGTH_SHORT).show();

            cursor.close();
            return Text;
        }
        else {
            //Toast.makeText(this,"Invalid database column",Toast.LENGTH_SHORT).show();
            cursor.close();
            return "";
        }
    }


}
