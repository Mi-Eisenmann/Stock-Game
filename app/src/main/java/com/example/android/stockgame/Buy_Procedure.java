package com.example.android.stockgame;

import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.stockgame.Stock_Object.Stock;

import java.io.IOException;

public class Buy_Procedure extends AppCompatActivity {

    // Defining the stock here, so the Buy-Button can use it.
    Stock searchedStock = new Stock();

    Inventory main = new Inventory();

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
            JSON = main.ReadIn();
        } catch (IOException e) {
            Toast.makeText(this, "I don't know this stock",Toast.LENGTH_SHORT).show();
            //Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // Check whether the stock exists or nor
        if (JSON != "") { // Stock exists
            // Build the Stock element
            searchedStock = main.StockBuild(JSON);

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
}
