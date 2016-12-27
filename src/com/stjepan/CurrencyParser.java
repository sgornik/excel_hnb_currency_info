package com.stjepan;

import java.util.*;

/**
 * Created by sgornik on 23.12.2016..
 */
public class CurrencyParser {
    //////MEMBERS
    private Map<String, List<CurrencyInfo>> currencyData;

    //////METHODS

    CurrencyParser()
    {
        currencyData = new HashMap<String, List<CurrencyInfo>>();
    }

    public void addCurrencyInfo(CurrencyInfo element)
    {
        String currency = element.currencyShort;
        List<CurrencyInfo> tempList = getCurrencyData(currency);
        if (null == tempList)
        {
            tempList = new ArrayList<CurrencyInfo>();
        }
        tempList.add(element);
        currencyData.put(currency, tempList);
    }

    public List<CurrencyInfo> getCurrencyData(String currency) {
        return currencyData.get(currency);
    }

    public void setCurrencyData(CurrencyInfo[] data)
    {
        for (CurrencyInfo currencyElement : data)
        {
            // Get the currency of the element
            String keyCurrencyShort = currencyElement.currencyShort;

            // Get the list for that currency
            List<CurrencyInfo> currencyList = currencyData.get(keyCurrencyShort);

            currencyList.add(currencyElement);
        }
    }
}
