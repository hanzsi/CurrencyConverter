package com.example.currencyconverter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

public class CustomAdapter extends BaseAdapter {

    private ExchangeRateDatabase exchangeRateDatabase;

    public CustomAdapter(ExchangeRateDatabase exchangeRateDatabase){
        this. exchangeRateDatabase =  exchangeRateDatabase;
    }



    @Override
    public int getCount() {
        return  exchangeRateDatabase.getCurrencies().length;
    }

    @Override
    public Object getItem(int position) {
        return  exchangeRateDatabase.getCurrencies()[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        Context context = viewGroup.getContext();

        if(view == null){
            //LayoutInflater to make the XML file into View objects.
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.spinner_layout, null, false);
        }

        //Currency short name
        String currencyName =  exchangeRateDatabase.getCurrencies()[position];
        TextView currencyNameView = view.findViewById(R.id.spinner_currencyName_view);
        currencyNameView.setText(currencyName);

        //Currency rate
        double currencyRate = exchangeRateDatabase.getExchangeRate(currencyName);
        TextView currencyRateView = view.findViewById(R.id.spinner_value_view);
        String currencyRateText = Double.toString(currencyRate);
        currencyRateView.setText(currencyRateText);

        //Flag
        ImageView flagView = view.findViewById(R.id.flags_view);
        int flagID = context.getResources().getIdentifier("flag_" + currencyName.toLowerCase(), "drawable",
                context.getPackageName());
        flagView.setImageResource(flagID);


        return view;
    }
}
