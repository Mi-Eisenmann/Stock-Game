package com.example.android.stockgame;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.android.stockgame.Stock_Object.InventoryStock;

import java.util.ArrayList;

public class StockAdapter extends ArrayAdapter<InventoryStock> {


    public StockAdapter(Context context, ArrayList<InventoryStock> stocks) {
        super(context,0,stocks);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View listItemView = convertView;
        if(listItemView == null){
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);
        }

        InventoryStock currentStock = getItem(position);

        TextView nameTextView = (TextView) listItemView.findViewById(R.id.list_name);
        nameTextView.setText( currentStock.getName() );

        TextView amountTextView = (TextView) listItemView.findViewById(R.id.list_amount);
        amountTextView.setText( currentStock.getAmount() );

        TextView priceTextView = (TextView) listItemView.findViewById(R.id.list_price);
        priceTextView.setText( currentStock.getPrice() );

        TextView totalTextView = (TextView) listItemView.findViewById(R.id.list_total);
        totalTextView.setText( currentStock.getTotal() );

        return listItemView;
    }
}
