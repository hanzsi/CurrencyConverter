package com.example.currencyconverter;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;


public class CurrencyListActivity extends AppCompatActivity {

    ListView view;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.currency_list_view);

        ExchangeRateDatabase exchangeRateDatabase = new ExchangeRateDatabase();
        AdapterCurrencyListActivity adapter = new AdapterCurrencyListActivity(exchangeRateDatabase);

        view = findViewById(R.id.currencyListView);
        view.setAdapter(adapter);

        //Clicking on a list item in CurrencyListActivity -> Opening G.Maps & showing the capital
        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                String selectedItem = (String) adapter.getItem(pos);
                String capital = exchangeRateDatabase.getCapital(selectedItem);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0`?q="+ capital));
                startActivity(intent);

            }
        });
    }





}
