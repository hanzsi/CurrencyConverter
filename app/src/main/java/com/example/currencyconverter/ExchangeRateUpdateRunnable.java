package com.example.currencyconverter;

import android.util.Log;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.net.URL;
import java.net.URLConnection;

public class ExchangeRateUpdateRunnable implements Runnable{

    private MainActivity mainActivity;

    public ExchangeRateUpdateRunnable(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void run() {

            String queryString = "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";

            ExchangeRateDatabase exchangeRateDatabase = new ExchangeRateDatabase();

            //Open connection, and go through the xml. When the tag is "Cube" and it has 2 parameters,
            //We get the value of currency and rate + parse to double & set the values by setExchangeRate method.
            try{
                URL url = new URL(queryString);
                URLConnection connection = url.openConnection();

                XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
                parser.setInput(connection.getInputStream(), connection.getContentEncoding());

                int eventType = parser.getEventType();

                while(eventType != XmlPullParser.END_DOCUMENT){
                    if(eventType == XmlPullParser.START_TAG){
                        if("Cube".equals(parser.getName()) && parser.getAttributeCount() == 2){
                            String currencyName = parser.getAttributeValue(null, "currency");
                            String currencyRate = parser.getAttributeValue(null, "rate");
                            double currencyRateDouble = Double.parseDouble(currencyRate);

                            exchangeRateDatabase.setExchangeRate(currencyName, currencyRateDouble);
                        }
                    }
                    eventType = parser.next();
                }
            } catch (Exception e) {
                Log.e("RefreshRate", "Can't query!");
                e.printStackTrace();

            }

            //Showing a toast when the update is finished
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast toast = Toast.makeText(mainActivity.getApplicationContext(),
                            "Currencies update finished!", Toast.LENGTH_LONG);
                    toast.show();
                }
            });

        }

    }




