package com.example.currencyconverter;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.net.URL;
import java.net.URLConnection;

public class UpdateCurrencyJobService  extends JobService {

    UpdateCurrencyAsyncTask updateCurrencyAsyncTask = new UpdateCurrencyAsyncTask(this);

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }


    //Async
    private static class UpdateCurrencyAsyncTask extends AsyncTask<JobParameters, Void, JobParameters> {

        private final JobService jobService;

        public UpdateCurrencyAsyncTask(JobService jobService){
            this.jobService = jobService;
        }

        @Override
        protected JobParameters doInBackground(JobParameters... jobParameters) {
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

            //Store new values from service
            SharedPreferences preferences = jobService.getSharedPreferences("ServiceUpdate", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            //Apply & store new rates
            for(String rate : exchangeRateDatabase.getCurrencies()){
                String newRate = Double.toString(exchangeRateDatabase.getExchangeRate(rate));
                editor.putString(rate, newRate);
            }
            editor.apply();

            return jobParameters[0];
        }

        @Override
        protected void onPostExecute(JobParameters jobParameters) {
            jobService.jobFinished(jobParameters, false);
        }
    }
}
