package com.example.android.stockgame.Data;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.example.android.stockgame.Data.StockContract.StockEntry;

import com.example.android.stockgame.Inventory;
import com.example.android.stockgame.Stock_Object.Stock;

public class Database_Manipulation {

    Database_Searches database_searches = new Database_Searches();
    //Inventory inventory = new Inventory();

    // add a Stock to the database
    public void addStock(String name, String symbol, String amount, String price, double inputTotal) {

        // Gets the database in write mode
        SQLiteDatabase db = Inventory.mDbHelper.getWritableDatabase();

        // collect the information for the database entry
        ContentValues values = new ContentValues();
        values.put(StockEntry.COLUMN_STOCK_NAME, name);
        values.put(StockEntry.COLUMN_STOCK_SYMBOL, symbol);
        values.put(StockEntry.COLUMN_STOCK_AMOUNT, amount);
        values.put(StockEntry.COLUMN_STOCK_PRICE, price);
        values.put(StockEntry.COLUMN_STOCK_TOTALVALUE, inputTotal);

        // insert and update the database entry ID
        long newRowId = db.insert(StockEntry.TABLE_NAME, null, values);

    }


    // add a Stock (in the form of a stock-object) to the database <-- Newer Version^^
    public void addStock(Stock stock, int number) {

        // Gets the database in write mode
        SQLiteDatabase db = Inventory.mDbHelper.getWritableDatabase();

        // Get the values out of the stock
        String name = stock.getName();
        String symbol = stock.getSymbol();
        int amount = number;
        double price = stock.getValue();
        //double priceD = stock.getValue();
        // price = (int) Math.round(priceD*100);
        double inputTotal = (double) amount * price;

        // Check whether that stock alread exists in the database
        boolean isAlreadyInDatabase = database_searches.containsStock(symbol);

        // Check whether the user has enough money
        double userMoney = database_searches.getUserMoney();

        if (userMoney > inputTotal) {

            // The stock is not in the databse yet --> Create new entry
            if (isAlreadyInDatabase == false) {

                ContentValues values = new ContentValues();
                values.put(StockEntry.COLUMN_STOCK_NAME, name);
                values.put(StockEntry.COLUMN_STOCK_SYMBOL, symbol);
                values.put(StockEntry.COLUMN_STOCK_AMOUNT, amount);
                values.put(StockEntry.COLUMN_STOCK_PRICE, price);
                values.put(StockEntry.COLUMN_STOCK_TOTALVALUE, inputTotal);

                long newRowId = db.insert(StockEntry.TABLE_NAME, null, values);

                // Update the user money
                updateMoney(inputTotal);
            }

            // The stock is in the database already --> Update the entry
            else {
                // index of the corresponding database row
                long rowIndex = database_searches.indexOfStock(symbol);
                String rowIndexString = String.valueOf(rowIndex);

                // Current stock amount of that type
                long currentAmount = database_searches.getStockAmount(rowIndexString);
                int currentAmountInt = (int) currentAmount;

                // Update the corresponding row
                int newAmount = amount + currentAmountInt;
                updateStockAmount(rowIndex, name, symbol, newAmount, price);

                // Update the user money
                updateMoney(inputTotal);
            }

        }

        // If the user does not have enough money
        else{
            //Toast.makeText(this,"You don't have enough money", Toast.LENGTH_SHORT).show();
        }
    }


    public void updateStockAmount (long id, String name, String symbol, int amount, double price){

        double inputTotal = price*amount;

        SQLiteDatabase db = Inventory.mDbHelper.getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(StockEntry.COLUMN_STOCK_NAME, name);
        values.put(StockEntry.COLUMN_STOCK_SYMBOL, symbol);
        values.put(StockEntry.COLUMN_STOCK_AMOUNT, amount);
        values.put(StockEntry.COLUMN_STOCK_PRICE, price);
        values.put(StockEntry.COLUMN_STOCK_TOTALVALUE, inputTotal);

        db.update("stocks", values, "_ID="+id,null);
    }

    // Update the users money
    public void updateMoney (double cost){

        long tempCost = Math.round(cost*100);
        long currentMoney = Math.round(database_searches.getUserMoney()*100);
        double newTotal = ((double) currentMoney - (double) tempCost)/100;

        SQLiteDatabase db = Inventory.mDbHelper.getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(StockEntry.COLUMN_STOCK_NAME, "Money");
        values.put(StockEntry.COLUMN_STOCK_SYMBOL, "---");
        values.put(StockEntry.COLUMN_STOCK_AMOUNT, "---");
        values.put(StockEntry.COLUMN_STOCK_PRICE, "---");
        values.put(StockEntry.COLUMN_STOCK_TOTALVALUE, newTotal);

        db.update("stocks", values, "_ID=1",null);
    }


    // Updating a certain database entry
    public void modifyEntry(String position, String column, String value){
        SQLiteDatabase db = Inventory.mDbHelper.getReadableDatabase();

        String name = database_searches.getEntryInformation(position, "name");
        String symbol = database_searches.getEntryInformation(position, "symbol");
        String amount = database_searches.getEntryInformation(position, "amount");
        String price = database_searches.getEntryInformation(position, "price");
        String total = database_searches.getEntryInformation(position, "totalvalue");

        if (column == "name"){
            name = value;
        } else if (column == "symbol") {
            symbol = value;
        } else if (column == "amount") {
            amount = value;
        } else if (column == "price") {
            price = value;
        } else if (column == "totalvalue") {
            total = value;
        } else {
            //Toast.makeText(this, "Unknown Column", Toast.LENGTH_SHORT).show();
        }

        //Toast.makeText(this,price,Toast.LENGTH_SHORT).show();

        ContentValues values = new ContentValues();
        values.put(StockEntry.COLUMN_STOCK_NAME, name);
        values.put(StockEntry.COLUMN_STOCK_SYMBOL, symbol);
        values.put(StockEntry.COLUMN_STOCK_AMOUNT, amount);
        values.put(StockEntry.COLUMN_STOCK_PRICE, price);
        values.put(StockEntry.COLUMN_STOCK_TOTALVALUE, total);

        //db.update("stocks", values, "_ID=?",new String[]{ position });
        db.update("stocks", values, "_ID="+position,null);

    }



    // Update the stock entry
    /*public void updateAllStocks(View view) {

        // Find all symbols
        String symb = "";
        //ArrayList<String> symbolList = new ArrayList<String>();

        SQLiteDatabase db = Inventory.mDbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT symbol FROM stocks WHERE _ID >? AND name !=?", new String[]{"1","Name"});

        int symbolColumnIndex = cursor.getColumnIndex(StockEntry.COLUMN_STOCK_SYMBOL);

        while (cursor.moveToNext()) {
            symb = cursor.getString(symbolColumnIndex);
            String symbolLow = symb.toLowerCase();
            //symbolList.add(symb);
            //Toast.makeText(this, symbolLow, Toast.LENGTH_SHORT).show();

            // Seems to be bad practice, but allows to do network operations in the main thread,
            // which otherwise seems to be forbidden.
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);

            // Read in the stock
            String JSON = null;
            try {
                Inventory inventory = new Inventory();
                JSON = inventory.ReadInUpdate(symbolLow);

            } catch (IOException e) {
                //Toast.makeText(this, "I don't know this stock", Toast.LENGTH_SHORT).show();
                //Log.e(LOG_TAG, "Problem making the HTTP request.", e);
            }

            // Check whether the stock exists or not
            if (JSON != "") { // Stock exists
                // Build the Stock element
                Inventory inventory = new Inventory();
                Inventory.searchedStock = inventory.StockBuild(JSON);

                // Read the Stock data out
                String stockPrice = Double.toString(Inventory.searchedStock.getValue());
                //Toast.makeText(this, String.valueOf(symbolColumnIndex), Toast.LENGTH_SHORT).show();
                //Toast.makeText(this, symb, Toast.LENGTH_SHORT).show();
                //Toast.makeText(this, stockPrice, Toast.LENGTH_SHORT).show();

                //Getting the correct Index for the entry to be updated:
                long id = database_searches.getIdFromSymbol(symb);
                String ID = String.valueOf(id);

                // Calculate the resulting total value:
                long amount = database_searches.getStockAmount(ID);
                //Toast.makeText(this,String.valueOf(amount),Toast.LENGTH_SHORT).show();
                double total = amount * inventory.searchedStock.getValue();
                String Total = String.valueOf(total);
                //Toast.makeText(this,String.valueOf(total),Toast.LENGTH_SHORT).show();

                // Update the database entries
                //modifyEntry(String.valueOf( symbolColumnIndex ),"price",stockPrice);
                modifyEntry(ID,"price",stockPrice);
                modifyEntry(ID,"totalvalue",Total);

            }
        }
        cursor.close();

        // Check for the updated info <--> Debugging of the update procedure
        //int checkPos = getIdFromSymbol("GOOG");
        //String CheckPos = Integer.toString(checkPos);
        //Toast.makeText(this,CheckPos,Toast.LENGTH_LONG).show();

        //String CheckName = getEntryInformation(CheckPos,"name");
        //Toast.makeText(this,CheckName,Toast.LENGTH_LONG).show();

        //String CheckPrice = getEntryInformation(CheckPos,"price");
        //Toast.makeText(this,CheckPrice,Toast.LENGTH_LONG).show();

        //Toast.makeText(this,CheckName + ": " + CheckPrice,Toast.LENGTH_LONG).show();


        Inventory inventory = new Inventory();
        inventory.displayDatabaseInfo();

        //Toast.makeText(this, "Database updated", Toast.LENGTH_SHORT).show();
    }*/

    // Removing the last entry of the database (mostly used for debugging purposses ;-) )
    public void removeLastDatabaseEntry(long row) {
        SQLiteDatabase db = Inventory.mDbHelper.getWritableDatabase();

        String table = "stocks";
        String whereClause = "_id=?";
        String[] whereArgs = new String[] { String.valueOf( row ) };
        long newRowId = db.delete(table, whereClause, whereArgs);
    }

}
