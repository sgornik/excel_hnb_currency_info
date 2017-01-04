package com.stjepan;

import org.apache.poi.ss.usermodel.CellStyle;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by sgornik on 23.12.2016..
 */
public class ExcelFileInfo {
                // ID   Column position
    private Map<String, Integer> currencies = null;
    private Date lastDate;
    private CellStyle cellDateStyle = null;
    private CellStyle cellDataStyle = null;
    private static long DAY_IN_MS = 86400000;

    public CellStyle getCellDateStyle() {
        return cellDateStyle;
    }

    public void setCellDateStyle(CellStyle cellDateStyle) {
        if (null == this.cellDateStyle)
        {
            this.cellDateStyle = cellDateStyle;
        }
    }

    public CellStyle getCellDataStyle() {
        return cellDataStyle;
    }

    public void setCellDataStyle(CellStyle cellDataStyle) {
        if (null == this.cellDataStyle)
        {
            this.cellDataStyle = cellDataStyle;
        }
    }

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
