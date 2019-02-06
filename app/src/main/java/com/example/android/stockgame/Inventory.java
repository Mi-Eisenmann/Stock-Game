package com.example.android.stockgame;

import android.content.Intent;
import android.database.Cursor;
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

import com.example.android.stockgame.Data.Database_Manipulation;
import com.example.android.stockgame.Data.Database_Searches;
import com.example.android.stockgame.Data.StockDbHelper;
import com.example.android.stockgame.Data.StockContract.StockEntry;
import com.example.android.stockgame.Stock_Object.InventoryStock;
import com.example.android.stockgame.Stock_Object.Stock;

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
    public static StockDbHelper mDbHelper;

    // Connection to other classes
    Database_Searches database_searches = new Database_Searches();
    Database_Manipulation database_manipulation = new Database_Manipulation();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        mDbHelper = new StockDbHelper(this);

        // Create the money row when the app is started
        if (database_searches.containsStock("---") == false){
            //addStock("Total", "---","---","---",10000);
            database_manipulation.addStock("Money", "---", "---", "---", 10000);
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

    public void displayDatabaseInfo() {

        // Read in the stock list
        ArrayList<InventoryStock> stocks = stockList();

        // Add the total value
        String total = database_searches.getTotalValue();
        stocks.add(0, new InventoryStock("Total", "----", "----", total));

        // Add the users money
        String money = String.valueOf( database_searches.getUserMoney() );
        stocks.add(1, new InventoryStock("Money", "----", "----", money));

        // Design choice for the header
        stocks.add(2, new InventoryStock("Name", "Amount", "Price", "Total"));
        stocks.add(3, new InventoryStock("----", "----", "----", "----"));

        StockAdapter adapter = new StockAdapter(this, stocks);
        ListView listView = (ListView) findViewById(R.id.listinventory);
        listView.setAdapter(adapter);
    }

    // Returns a list of all stocks in possession for displaying purposes.
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

    // The Update button that updates the information for all stocks in possession.
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
                long id = database_searches.getIdFromSymbol(symb);
                String ID = String.valueOf(id);

                // Calculate the resulting total value:
                long amount = database_searches.getStockAmount(ID);
                //Toast.makeText(this,String.valueOf(amount),Toast.LENGTH_SHORT).show();
                double total = amount * searchedStock.getValue();
                String Total = String.valueOf(total);
                //Toast.makeText(this,String.valueOf(total),Toast.LENGTH_SHORT).show();

                // Update the database entries
                //modifyEntry(String.valueOf( symbolColumnIndex ),"price",stockPrice);
                database_manipulation.modifyEntry(ID,"price",stockPrice);
                database_manipulation.modifyEntry(ID,"totalvalue",Total);

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


    // Stock creation out of the information that have been read in
    public Stock StockBuild(String JSONinput) {
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


    // For adding new entries to the database we need the last entry
    /*private int getLastId() {
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
    }*/


    // The Buy Section <--> Getting updated information for a certain stock

    private static final String Request_Url_firstPart =
            "https://api.iextrading.com/1.0/stock/";
    private static final String Request_Url_secondPart =
            "/batch?types=quote&range=1m&last=10";

    // Tag for the log messages
    public static final String LOG_TAG = Inventory.class.getSimpleName();

    // Defining the stock here, so the Buy-Button can use it.
    public static Stock searchedStock = new Stock();

    // Search for information about the stock to buy
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
            Toast.makeText(this, "I don't know this stock",Toast.LENGTH_SHORT).show();
        }
    }

    // Forming the URL-String into a real URL object
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

    // Read In during the Buy-Procedure
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

    // JSON Request for the Buying-Procedure
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

    // Buy the desired stock
    public void BuyButton(View view){
        database_manipulation.addStock(searchedStock, 1);

    }

    // Back from Buying to the inventory
    public void SearchInventoryButton(View view) {
        Intent intent=new Intent(view.getContext(),Inventory.class);
        view.getContext().startActivity(intent);
        //setContentView(R.layout.activity_inventory);
        //displayDatabaseInfo();
    }



    // Sell page <--> Selling already owned stocks

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
        int id = database_searches.getIdFromSymbol(input);
        /*int idtwo = getIdFromSymbol("wmt");
        Toast.makeText(this,input,Toast.LENGTH_SHORT).show();
        Toast.makeText(this,String.valueOf(id),Toast.LENGTH_SHORT).show();
        Toast.makeText(this,String.valueOf(idtwo),Toast.LENGTH_SHORT).show();*/
        String Amount = "0";
        if (id == -1) { // If the stock is not in the databse
            Amount = "0";
        }
        else { // If it is in the database
            long amount = database_searches.getStockAmount(String.valueOf(id));
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

    // JSON Request for information about the chosen stock type
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

    // The button to sell the desired stock type
    public void SearchSellButton (View view) {

        String name = searchedStock.getName();
        String symbol = searchedStock.getSymbol();
        double price = searchedStock.getValue();
        int id = database_searches.getIdFromSymbol(symbol);

        // Check whether the stock exists in the database
        if (id == -1){
            Toast.makeText(this,"You don't own this stock",Toast.LENGTH_SHORT).show();
        }
        else {
            long amount = database_searches.getStockAmount(String.valueOf(id));
            int Amount = (int) amount;

            if (Amount > 0) {
                // Reduce the stock amount by 1
                database_manipulation.updateStockAmount(id, name, symbol, Amount - 1, price);


                if (Amount == 1) {
                    if (id > 1) { // Don't delete the money row
                        // Remove the last entry from the database
                        database_manipulation.removeLastDatabaseEntry(id);
                    }
                }

                // Update the output field
                TextView outPutSymbol = (TextView) findViewById(R.id.sell_stock_amount);
                outPutSymbol.setText(String.valueOf(Amount-1));

                // Update the users money
                database_manipulation.updateMoney(-price);


            } else {
                Toast.makeText(this, "You don't own this stock", Toast.LENGTH_SHORT).show();
            }

        }
    }

}

