package com.stjepan;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by sgornik on 23.12.2016..
 */
public class ExcelFileInfo {
                // ID   Column position
    private Map<String, Integer> currencies = null;
    private Date lastDate;
    private static long DAY_IN_MS = 86400000;

    ExcelFileInfo()
    {
        currencies = new HashMap<String, Integer>();
    }

    public Date getLastDate()
    {
        return lastDate;
    }

    public String getFirstNextDateString() {
        String output;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

        output = dateFormat.format(lastDate.getTime() + DAY_IN_MS);   // prints 31/05/2011
        return output;
    }

    public void addLastDate(Date date)
    {
        lastDate = date;
    }



    public void addCurrency(String currency, int currencyPos)
    {
        currencies.put(currency, currencyPos);
    }

    public Integer getCurrencyPos(String currency) {
        return currencies.get(currency);
    }

    public List<String> getCurrencies() {
        ArrayList list = new ArrayList();
        for (Map.Entry<String, Integer> pair : currencies.entrySet())
        {
            list.add(pair.getKey());
        }
        return list;
    }
}
