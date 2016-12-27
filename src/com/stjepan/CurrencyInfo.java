package com.stjepan;

import com.google.gson.annotations.SerializedName;

/**
 * Created by sgornik on 20.12.2016..
 */
public class CurrencyInfo {
    @SerializedName("Broj tečajnice") public String exchangeSessionNumber;
    @SerializedName("Datum primjene") public String date;
    @SerializedName("Država") public String country;
    @SerializedName("Šifra valute") public String currencyNumber;
    @SerializedName("Valuta") public String currencyShort;
    @SerializedName("Jedinica") public int unit;
    @SerializedName("Kupovni za devize") public String buyExchangeRate;
    @SerializedName("Srednji za devize") public String averageExchangeRate;
    @SerializedName("Prodajni za devize") public String sellExchangeRate;
}
