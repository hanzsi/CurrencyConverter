package com.example.currencyconverter;

public class ExchangeRate {
    private String currencyName;
    private double rateForOneEuro;
    private String capital;

    public ExchangeRate(String currencyName, String capital, double rateForOneEuro) {
        this.currencyName = currencyName;
        this.rateForOneEuro = rateForOneEuro;
        this.capital = capital;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public double getRateForOneEuro() {
        return rateForOneEuro;
    }

    public String getCapital(){return capital;}

    public void setRateForOneEuro(double newRate){
        rateForOneEuro = newRate;
    }
}
