package com.stjepan;

/**
 * Created by sgornik on 4.1.2017..
 */
public class ConfigInfo
{
    public static final String DEFAULT_EXXCEL_FILENAME = "currency.xlsx";
    public static final int DEFAULT_FLOAT_DECIMALS = 6;

    /// Excel filename that needs to be updated
    private String excelFilename;
    /// Number of decimal digits in the Exccel cell
    private int floatDecimals;

    public void setExcelFilename(String excelFilename) {
        this.excelFilename = excelFilename;
    }

    public void setFloatDecimals(int floatDecimals) {
        this.floatDecimals = floatDecimals;
    }

    public String getExcelFilename() {
        return excelFilename;
    }

    public int getFloatDecimals() {
        return floatDecimals;
    }

    ConfigInfo()
    {
        excelFilename = new String(DEFAULT_EXXCEL_FILENAME);
        floatDecimals = DEFAULT_FLOAT_DECIMALS;
    }


}
