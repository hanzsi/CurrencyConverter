package com.example.currencyconverter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ExchangeRateDatabase exchangeRateDatabase;
    private CustomAdapter customAdapter;
    private Spinner currencyFrom;
    private Spinner currencyTo;
    private Toolbar toolbar;
    private EditText amount;
    private TextView result;

    private ShareActionProvider shareActionProvider;
    private ExchangeRateUpdateRunnable runnable;

    private static final int JOB_ID = 101;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Create adapter object
        exchangeRateDatabase = new ExchangeRateDatabase();
        customAdapter = new CustomAdapter(exchangeRateDatabase);

        //Set adapter
        currencyFrom = (Spinner)findViewById(R.id.currencyFromSpinner);
        currencyFrom.setAdapter(customAdapter);

        //Set adapter
        currencyTo = (Spinner)findViewById(R.id.currencyToSpinner);
        currencyTo.setAdapter(customAdapter);

        //Set toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        //StrictMode.setThreadPolicy(policy);

        updateService();


    }

    public void calculateButtonClicked(View view){
        amount = findViewById(R.id.amount);
        result = findViewById(R.id.calculatedAmount);

        //If it is empty -> Basic text is 0.00
        if(amount.getText().toString().length() == 0){
            amount.setText(String.format(
                    Locale.getDefault(),
                    "%.2f",
                    0.00));
            return;
        } else {
            //Otherwise calculate the exchange and set the format to %.2f
            double amountToChange = Double.parseDouble(amount.getText().toString());
            currencyFrom = findViewById(R.id.currencyFromSpinner);
            currencyTo = findViewById(R.id.currencyToSpinner);
            String currencyFromString = (String) currencyFrom.getSelectedItem();
            String currencyToString = (String) currencyTo.getSelectedItem();

            result.setText(String.format(
                    Locale.getDefault(),
                    "%.2f",
                    exchangeRateDatabase.convert(amountToChange, currencyFromString, currencyToString)));
            /*
            setShareText(String.format(
                    Locale.getDefault(),
                    "%.2f",
                    exchangeRateDatabase.convert(amountToChange, currencyFromString, currencyToString)));

             */
        }
    }

    //Creating the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        MenuItem shareItem = menu.findItem(R.id.action_share);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        setShareText(null);
        return true;
    }

    //Sharing conversion text with a button to other apps
    private void setShareText(String text){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        if(text != null){
            shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        }
        shareActionProvider.setShareIntent(shareIntent);
    }

    //By selecting a menu item, start the appropriate activity
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.currencyListActivityMenuItem:
                Intent intent = new Intent(MainActivity.this, CurrencyListActivity.class);
                MainActivity.this.startActivity(intent);
                return true;
            case R.id.refreshRatesMenuItem:
                //updateCurrencies();
                runnable = new ExchangeRateUpdateRunnable(this);
                new Thread(runnable).start();
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

        //Obtain preferences & get editing access
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        //Values to store

        amount = findViewById(R.id.amount);
        String amountString = amount.getText().toString();
        editor.putString("amountKey", amountString);

        currencyFrom = findViewById(R.id.currencyFromSpinner);
        int positionFromSpinner = currencyFrom.getSelectedItemPosition();
        editor.putInt("currencyFromKey", positionFromSpinner);

        currencyTo = findViewById(R.id.currencyToSpinner);
        int positionToSpinner = currencyTo.getSelectedItemPosition();
        editor.putInt("currencyToKey", positionToSpinner);

        //to store new rates even if the app is closed
        for(String rate : exchangeRateDatabase.getCurrencies()){
            String newRate = Double.toString(exchangeRateDatabase.getExchangeRate(rate));
            editor.putString(rate, newRate);
        }

        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);

        amount = findViewById(R.id.amount);
        currencyFrom = findViewById(R.id.currencyFromSpinner);
        currencyTo = findViewById(R.id.currencyToSpinner);

        String amountString = prefs.getString("amountKey", "");
        int positionFromSpinner = prefs.getInt("currencyFromKey",0);
        int positionToSpinner = prefs.getInt("currencyToKey",1);

        amount.setText(amountString);
        currencyFrom.setSelection(positionFromSpinner);
        currencyTo.setSelection(positionToSpinner);

        //Set the new exchange rate back to the program, so the currency rate is the same as before closing
        for(String rate : exchangeRateDatabase.getCurrencies()){
            String newRate = prefs.getString(rate,"0.00");
            if(!("0.00".equals(rate))){
                exchangeRateDatabase.setExchangeRate(rate, Double.parseDouble(newRate));
            }
        }
    }

    public void updateService(){

        ComponentName serviceName = new ComponentName(this, UpdateCurrencyJobService.class);
        JobInfo jobInfo = new JobInfo.Builder(JOB_ID, serviceName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setRequiresDeviceIdle(false)
                .setRequiresCharging(false)
                .setPeriodic(86400000) //24*60*60*1000ms
                .setPersisted(true)
                .build();

        JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);


    }



}