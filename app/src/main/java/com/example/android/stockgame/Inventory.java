package com.example.android.stockgame;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.stockgame.Data.StockDbHelper;
import com.example.android.stockgame.Data.StockContract.StockEntry;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;


public class Inventory extends AppCompatActivity {

    /** Database helper that will provide us access to the database */
    public StockDbHelper mDbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        mDbHelper = new StockDbHelper(this);

        // Create the money row when the app is started
        if (containsStock("---") == false){
            //addStock("Total", "---","---","---",10000);
            addStock("Money", "---", "---", "---", 10000);
        }

        FloatingActionButton fab1 = findViewById(R.id.fab1);
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent=new Intent(view.getContext(),Link1.class);
                //view.getContext().startActivity(intent);
                setContentView(R.layout.buy);
            }
        });

        FloatingActionButton fab2 = findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent=new Intent(view.getContext(),Link2.class);
                //view.getContext().startActivity(intent);
                setContentView(R.layout.sell);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        // Display the database on the main page
        displayDatabaseInfo();

    }

    public void inventoryToAdd(View view) {
        //Toast.makeText(this,"Test",Toast.LENGTH_SHORT).show();
        setContentView(R.layout.buy);
    }

    /*public void inventoryAddExample(View view) {
        Stock stock = new Stock("symbol", "Name", (double) 1.33);
        addStock(stock,1);
        displayDatabaseInfo();
    }*/

    private void displayDatabaseInfo() {

        // Read in the stock list
        ArrayList<InventoryStock> stocks = stockList();

        // Add the total value
        String total = getTotalValue();
        stocks.add(0, new InventoryStock("Total", "----", "----", total));

        // Add the users money
        String money = String.valueOf( getUserMoney() );
        stocks.add(1, new InventoryStock("Money", "----", "----", money));

        // Design choice for the header
        stocks.add(2, new InventoryStock("Name", "Amount", "Price", "Total"));
        stocks.add(3, new InventoryStock("----", "----", "----", "----"));

        StockAdapter adapter = new StockAdapter(this, stocks);
        ListView listView = (ListView) findViewById(R.id.listinventory);
        listView.setAdapter(adapter);
    }

    public ArrayList<InventoryStock> stockList() {

        // Declare the arraylist to be filled with the stocks
        ArrayList<InventoryStock> stockList = new ArrayList<InventoryStock>();

        // Create and/or open a database to read from it
        // Create database helper
        StockDbHelper mDbHelper = new StockDbHelper(this);

        // Gets the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                StockEntry._ID,
                StockEntry.COLUMN_STOCK_NAME,
                StockEntry.COLUMN_STOCK_SYMBOL,
                StockEntry.COLUMN_STOCK_AMOUNT,
                StockEntry.COLUMN_STOCK_PRICE,
                StockEntry.COLUMN_STOCK_TOTALVALUE };

        // Perform a query on the pets table
        Cursor cursor = db.query(
                StockEntry.TABLE_NAME,   // The table to query
                projection,            // The columns to return
                null,                  // The columns for the WHERE clause
                null,                  // The values for the WHERE clause
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null);                   // The sort order

        try {
            // Figure out the index of each column
            int idColumnIndex = cursor.getColumnIndex(StockEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(StockEntry.COLUMN_STOCK_NAME);
            int symbolColumnIndex = cursor.getColumnIndex(StockEntry.COLUMN_STOCK_SYMBOL);
            int amountColumnIndex = cursor.getColumnIndex(StockEntry.COLUMN_STOCK_AMOUNT);
            int priceColumnIndex = cursor.getColumnIndex(StockEntry.COLUMN_STOCK_PRICE);
            int totalColumnIndex = cursor.getColumnIndex(StockEntry.COLUMN_STOCK_TOTALVALUE);

            // Iterate through all the returned rows in the cursor
            while (cursor.moveToNext()) {
                // Use that index to extract the String or Int value of the word
                // at the current row the cursor is on.
                int currentID = cursor.getInt(idColumnIndex);
                String currentName = cursor.getString(nameColumnIndex);
                String currentSymbol = cursor.getString(symbolColumnIndex);
                int currentAmount = cursor.getInt(amountColumnIndex);
                double currentPrice = cursor.getDouble(priceColumnIndex);
                double currentTotal = cursor.getDouble(totalColumnIndex);

                // Round to two decimals
                double currentPriceRound = (double) Math.round(currentPrice*100)/100;
                double currentTotalRound = (double) Math.round(currentTotal*100)/100;

                // Build the stock
                if (currentID > 1) { // Exclude the users money
                    InventoryStock currentStock = new InventoryStock();
                    currentStock.setName(currentName);
                    currentStock.setAmount(String.valueOf(currentAmount));
                    currentStock.setPrice(String.valueOf(currentPriceRound));
                    currentStock.setTotal(String.valueOf(currentTotalRound));

                    stockList.add(currentStock);
                }

            }
        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }

        return stockList;

    }

    // add a Stock to the database
    private void addStock(String name, String symbol, String amount, String price, double inputTotal) {

        // Gets the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

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
    private void addStock(Stock stock, int number) {

        // Gets the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Get the values out of the stock
        String name = stock.getName();
        String symbol = stock.getSymbol();
        int amount = number;
        double price = stock.getValue();
        //double priceD = stock.getValue();
        // price = (int) Math.round(priceD*100);
        double inputTotal = (double) amount * price;

        // Check whether that stock alread exists in the database
        boolean isAlreadyInDatabase = containsStock(symbol);

        // Check whether the user has enough money
        double userMoney = getUserMoney();

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
                long rowIndex = indexOfStock(symbol);
                String rowIndexString = String.valueOf(rowIndex);

                // Current stock amount of that type
                long currentAmount = getStockAmount(rowIndexString);
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
            Toast.makeText(this,"You don't have enough money", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateAllStocks(View view) {

        // Find all symbols
        String symb = "";
        //ArrayList<String> symbolList = new ArrayList<String>();

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
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
                JSON = ReadInUpdate(symbolLow);

            } catch (IOException e) {
                //Toast.makeText(this, "I don't know this stock", Toast.LENGTH_SHORT).show();
                Log.e(LOG_TAG, "Problem making the HTTP request.", e);
            }

            // Check whether the stock exists or not
            if (JSON != "") { // Stock exists
                // Build the Stock element
                searchedStock = StockBuild(JSON);

                // Read the Stock data out
                String stockPrice = Double.toString(searchedStock.getValue());
                //Toast.makeText(this, String.valueOf(symbolColumnIndex), Toast.LENGTH_SHORT).show();
                //Toast.makeText(this, symb, Toast.LENGTH_SHORT).show();
                //Toast.makeText(this, stockPrice, Toast.LENGTH_SHORT).show();

                //Getting the correct Index for the entry to be updated:
                long id = getIdFromSymbol(symb);
                String ID = String.valueOf(id);

                // Calculate the resulting total value:
                long amount = getStockAmount(ID);
                //Toast.makeText(this,String.valueOf(amount),Toast.LENGTH_SHORT).show();
                double total = amount * searchedStock.getValue();
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



        displayDatabaseInfo();

        Toast.makeText(this, "Database updated", Toast.LENGTH_SHORT).show();
    }

    // Calculate the sum of the current money and the values of all stocks
    private String getTotalValue(){
        double currentTotal = 0;
        double totalTotal = 0;

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

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

    private double getUserMoney() {
        double currentTotal = 0;

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

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

    private void updateMoney (double cost){

        long tempCost = Math.round(cost*100);
        long currentMoney = Math.round(getUserMoney()*100);
        double newTotal = ((double) currentMoney - (double) tempCost)/100;

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(StockEntry.COLUMN_STOCK_NAME, "Money");
        values.put(StockEntry.COLUMN_STOCK_SYMBOL, "---");
        values.put(StockEntry.COLUMN_STOCK_AMOUNT, "---");
        values.put(StockEntry.COLUMN_STOCK_PRICE, "---");
        values.put(StockEntry.COLUMN_STOCK_TOTALVALUE, newTotal);

        db.update("stocks", values, "_ID=1",null);
    }

    // When a certain stock is to be adapted in the database, get the correspodning ID here
    private long indexOfStock (String Symbol) {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String query = "SELECT _ID FROM stocks WHERE symbol =?";
        return DatabaseUtils.longForQuery(db, query, new String[]{ Symbol });
    }


    private long getStockAmount (String position) {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String query = "SELECT amount FROM stocks WHERE _ID =?";
        return DatabaseUtils.longForQuery(db, query, new String [] {position} );
    }

    // Updating a certain database entry
    private void modifyEntry(String position, String column, String value){
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String name = getEntryInformation(position, "name");
        String symbol = getEntryInformation(position, "symbol");
        String amount = getEntryInformation(position, "amount");
        String price = getEntryInformation(position, "price");
        String total = getEntryInformation(position, "totalvalue");

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
            Toast.makeText(this, "Unknown Column", Toast.LENGTH_SHORT).show();
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

    // Return all information to a certain stock from the database
    private String getEntryInformation(String position, String column) {
        long longOutput = 0;
        double doubleOutput = 0;
        String stringOutput = "";

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

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
            Toast.makeText(this,"Invalid database column",Toast.LENGTH_SHORT).show();
            cursor.close();
            return "";
        }
    }

    private void updateStockAmount (long id, String name, String symbol, int amount, double price){

        double inputTotal = price*amount;

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(StockEntry.COLUMN_STOCK_NAME, name);
        values.put(StockEntry.COLUMN_STOCK_SYMBOL, symbol);
        values.put(StockEntry.COLUMN_STOCK_AMOUNT, amount);
        values.put(StockEntry.COLUMN_STOCK_PRICE, price);
        values.put(StockEntry.COLUMN_STOCK_TOTALVALUE, inputTotal);

        db.update("stocks", values, "_ID="+id,null);
    }

    // Do we posses a certain stock?
    private boolean containsStock(String Symbol) {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

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

    // For adding new entries to the database we need the last entry
    private int getLastId() {
        int mx = -1;
        try {
            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT MAX(_ID) AS _ID FROM stocks", new String[]{});
            if (cursor != null)
                if (cursor.moveToFirst()) {
                    mx = cursor.getInt(0);
                }
            //  cursor.close();
            return mx;
        } catch (Exception e) {

            return -1;
        }
    }

    // For modifying a stock-entry (after buying or selling) we need the corresponding database-ID to this stock
    public int getIdFromSymbol(String symbol) {
        int id = -1;
        try {
            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT _ID FROM stocks WHERE symbol =?", new String[]{symbol});
            if (cursor.getCount() > 0) {
                if (cursor.moveToFirst()) {
                    id = cursor.getInt(0);
                }
                //  cursor.close();
                return id;
            }
            else {
                Toast.makeText(this,"Stopped here",Toast.LENGTH_LONG).show();
                return -1;
            }
        } catch (Exception e) {
            Toast.makeText(this,"Didn't work",Toast.LENGTH_LONG).show();
            return -1;
        }

    }

    public void removeLastDatabaseEntry(long row) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String table = "stocks";
        String whereClause = "_id=?";
        String[] whereArgs = new String[] { String.valueOf( row ) };
        long newRowId = db.delete(table, whereClause, whereArgs);
    }


    // The Search Section <--> Getting updated information for a certain stock

    private static final String Request_Url_firstPart =
            "https://api.iextrading.com/1.0/stock/";
    private static final String Request_Url_secondPart =
            "/batch?types=quote&range=1m&last=10";

    // Tag for the log messages
    public static final String LOG_TAG = Inventory.class.getSimpleName();

    // Defining the stock here, so the Buy-Button can use it.
    Stock searchedStock = new Stock();

    public void searchOnClick(View view) {

        // Seems to be bad practice, but allows to do network operations in the main thread,
        // which otherwise seems to be forbidden.
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);


        // Read in the desired stock symbol
        EditText inPut = (EditText) findViewById(R.id.stocksearch);
        String input = inPut.getText().toString();

        // Define the output fields where the data shall be outputted
        TextView outPutName = (TextView) findViewById(R.id.search_stock_name);
        TextView outPutSymbol = (TextView) findViewById(R.id.search_stock_symbol);
        TextView outPutPrice = (TextView) findViewById(R.id.search_stock_price);

        // Read in the stock
        String JSON = null;
        try {
            JSON = ReadIn();
        } catch (IOException e) {
            Toast.makeText(this, "I don't know this stock",Toast.LENGTH_SHORT).show();
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // Check whether the stock exists or nor
        if (JSON != "") { // Stock exists
            // Build the Stock element
            searchedStock = StockBuild(JSON);

            // Read the Stock data out
            String stockName = searchedStock.getName();
            String stockSymbol = searchedStock.getSymbol();
            String stockPrice = Double.toString(searchedStock.getValue());

            // Write it into the output fields
            outPutName.setText(stockName);
            outPutSymbol.setText(stockSymbol);
            outPutPrice.setText(stockPrice);

            // Make the result layout region visible again
            LinearLayout result_area = (LinearLayout) findViewById(R.id.search_result);
            result_area.setVisibility(View.VISIBLE);
        }
        else{ // Stock does not exist
            setContentView(R.layout.buy);
        }
    }

    private URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException exception) {
            Log.e(LOG_TAG, "Error with creating URL", exception);
            return null;
        }
        return url;
    }

    private String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    public String ReadIn() throws IOException{
        EditText inPut = (EditText) findViewById(R.id.stocksearch);
        String input = inPut.getText().toString();

        String stockLower = input.toLowerCase();
        String fullUrl = Request_Url_firstPart + stockLower + Request_Url_secondPart;

        URL url = createUrl(fullUrl);

        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            Toast.makeText(this, "I don't know this stock",Toast.LENGTH_SHORT).show();
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
                Toast.makeText(this, "I don't know this stock",Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the earthquake JSON results.", e);
            Toast.makeText(this, "I don't know this stock",Toast.LENGTH_SHORT).show();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;

    }

    // For the updating procedure
    public String ReadInUpdate(String symbol) throws IOException{

        String stockLower = symbol.toLowerCase();
        String fullUrl = Request_Url_firstPart + stockLower + Request_Url_secondPart;
        //Toast.makeText(this, fullUrl,Toast.LENGTH_LONG).show();

        URL url = createUrl(fullUrl);

        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            Toast.makeText(this, "URL is null",Toast.LENGTH_SHORT).show();
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
                Toast.makeText(this, "Response code not 200",Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the JSON results.", e);
            Toast.makeText(this, "Problem retrieving the JSON results.",Toast.LENGTH_SHORT).show();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;

    }

    // Stock creation out of the information that were read in
    private Stock StockBuild(String JSONinput) {
        try {
            JSONObject baseJsonResponse = new JSONObject(JSONinput);
            JSONObject data = baseJsonResponse.getJSONObject("quote");
            //JSONArray featureArray = baseJsonResponse.getJSONArray("quote");

            //JSONObject data = featureArray.getJSONObject(0);

            String name = data.getString("companyName");
            String symbol = data.getString("symbol");
            double price = data.getDouble("close");

            return new Stock(symbol, name, price);

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem creating the Stock", e);
        }
        return null;
    }

    public void BuyButton(View view){
        addStock(searchedStock, 1);

    }

    public void SearchInventoryButton(View view) {
        Intent intent=new Intent(view.getContext(),Inventory.class);
        view.getContext().startActivity(intent);
        //setContentView(R.layout.activity_inventory);
        //displayDatabaseInfo();
    }


    // Sell page
    public void sellButton (View view) {
        setContentView(R.layout.sell);
    }

    public void sellOnClick(View view) {

        // Seems to be bad practice, but allows to do network operations in the main thread,
        // which otherwise seems to be forbidden.
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);


        // Read in the desired stock symbol
        EditText inPut = (EditText) findViewById(R.id.sell_stock);
        String Input = inPut.getText().toString();
        String input = Input.toUpperCase(); // The symbols are saved in uppercases

        // Check for the amount of that stock the user owns
        int id = getIdFromSymbol(input);
        /*int idtwo = getIdFromSymbol("wmt");
        Toast.makeText(this,input,Toast.LENGTH_SHORT).show();
        Toast.makeText(this,String.valueOf(id),Toast.LENGTH_SHORT).show();
        Toast.makeText(this,String.valueOf(idtwo),Toast.LENGTH_SHORT).show();*/
        String Amount = "0";
        if (id == -1) { // If the stock is not in the databse
            Amount = "0";
        }
        else { // If it is in the database
            long amount = getStockAmount(String.valueOf(id));
            Amount = String.valueOf(amount);
        }

        // Define the output fields where the data shall be outputted
        TextView outPutName = (TextView) findViewById(R.id.sell_stock_name);
        TextView outPutPrice = (TextView) findViewById(R.id.sell_stock_price);
        TextView outPutSymbol = (TextView) findViewById(R.id.sell_stock_amount);

        // Read in the stock
        String JSON = null;
        try {
            JSON = ReadInSell();
        } catch (IOException e) {
            Toast.makeText(this, "I don't know this stock",Toast.LENGTH_SHORT).show();
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }
        // Checking whether the stock exists or not
        if (JSON != "") { // Stock exists
            // Build the Stock element
            searchedStock = StockBuild(JSON);

            // Read the Stock data out
            String stockName = searchedStock.getName();
            String stockSymbol = Amount; //searchedStock.getSymbol();
            String stockPrice = Double.toString(searchedStock.getValue());

            // Write it into the output fields
            outPutName.setText(stockName);
            outPutSymbol.setText(stockSymbol);
            outPutPrice.setText(stockPrice);

            // Make the result layout region visible again
            LinearLayout result_area = (LinearLayout) findViewById(R.id.sell_result);
            result_area.setVisibility(View.VISIBLE);
        }
        else { // Stock does not exist
            Toast.makeText(this,"I don't know this stock",Toast.LENGTH_SHORT).show();
            setContentView(R.layout.sell);
        }
    }


    public String ReadInSell() throws IOException {
        EditText inPut = (EditText) findViewById(R.id.sell_stock);
        String input = inPut.getText().toString();

        String stockLower = input.toLowerCase();
        String fullUrl = Request_Url_firstPart + stockLower + Request_Url_secondPart;

        URL url = createUrl(fullUrl);

        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the earthquake JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;

    }

    public void SearchSellButton (View view) {

        String name = searchedStock.getName();
        String symbol = searchedStock.getSymbol();
        double price = searchedStock.getValue();
        int id = getIdFromSymbol(symbol);

        // Check whether the stock exists in the database
        if (id == -1){
            Toast.makeText(this,"You don't own this stock",Toast.LENGTH_SHORT).show();
        }
        else {
            long amount = getStockAmount(String.valueOf(id));
            int Amount = (int) amount;

            if (Amount > 0) {
                // Reduce the stock amount by 1
                updateStockAmount(id, name, symbol, Amount - 1, price);


                if (Amount == 1) {
                    if (id > 1) { // Don't delete the money row
                        // Remove the last entry from the database
                        removeLastDatabaseEntry(id);
                    }
                }

                // Update the output field
                TextView outPutSymbol = (TextView) findViewById(R.id.sell_stock_amount);
                outPutSymbol.setText(String.valueOf(Amount-1));

                // Update the users money
                updateMoney(-price);


            } else {
                Toast.makeText(this, "You don't own this stock", Toast.LENGTH_SHORT).show();
            }

        }
    }

}

