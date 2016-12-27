package com.stjepan;

import java.io.*;
import java.math.BigDecimal;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.*;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import static org.apache.poi.ss.usermodel.CellType.*;

public class HnbApp {

    private final String USER_AGENT = "Mozilla/5.0";
    private static final String EXCEL_FILENAME = "Tečajevi.xlsx";
    private static final long DAY_IN_MS = 86400000;
    private static final int FLOAT_DECIMALS = 6;

    // offsets inside data array
    private static final int GBP_OFFSET = 0;
    private static final int USD_OFFSET = 1;
    private static final int EUR_OFFSET = 2;

    private static final boolean MAKE_COPY = true;

    public static void main(String[] args) throws Exception {

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        HnbApp hnbApp = new HnbApp();
        ExcelFileInfo fileInfo = new ExcelFileInfo();
        CurrencyParser parsedData;
        boolean deleteCopy = true;
        File copyFile = new File(EXCEL_FILENAME + ".backup");

        copyFile(new File(EXCEL_FILENAME), copyFile);

        System.out.println("Reading last date from Excel file...");
        try
        {
            fileInfo = hnbApp.readLastDateFromExcelFile(EXCEL_FILENAME);
        }
        catch (IOException ex)
        {
            System.out.println("Problem reading Excel file : " + ex.getMessage());
            deleteCopy = false;
        }

        System.out.println("Sending request...");



        parsedData = hnbApp.sendPost(fileInfo);

        // TODO : Prepare header and data
        // TODO : Send the request
        // TODO : Read the JSON data and fill the object data
        // TODO : Fill the Excel file with required data

        System.out.println("Filling Excel data...");
        try
        {
            hnbApp.fillExcelData(fileInfo, parsedData);
        }
        catch (IOException ex)
        {
            System.out.println("Problem filling Excel data : " + ex.getMessage());
            deleteCopy = false;
        }

        if (true == deleteCopy)
        {
            copyFile.delete();
        }

    }

    private ExcelFileInfo readLastDateFromExcelFile(String newFileName) throws IOException
    {
        FileInputStream excelFileStream = new FileInputStream(new File(newFileName));
        Workbook workbook = new XSSFWorkbook(excelFileStream);
        Sheet firstSheet = workbook.getSheetAt(0);
        ExcelFileInfo fileInfo = new ExcelFileInfo();

        Iterator<Row> rowIterator = firstSheet.iterator();
        Cell cell = null;

        Row lastRow = null;

        while (rowIterator.hasNext())
        {
            // Just pass all the rows and get to the last one

            Row nextRow = rowIterator.next();
            Iterator<Cell> cellIterator = nextRow.cellIterator();

            System.out.println("Row number : " + nextRow.getRowNum());

            while (cellIterator.hasNext()) {
                cell = cellIterator.next();

                switch (cell.getCellTypeEnum())
                {
                    case STRING :
                        // TODO : Replace with container from config file
                        if (cell.getStringCellValue().equalsIgnoreCase("EUR") ||
                                cell.getStringCellValue().equalsIgnoreCase("USD") ||
                                cell.getStringCellValue().equalsIgnoreCase("GBP"))
                        {
                            System.out.println("Value of the cell " + cell.getColumnIndex() + " : " + cell.getStringCellValue());
                            fileInfo.addCurrency(cell.getStringCellValue(), cell.getColumnIndex());
                        }
                    default :
                        break;
                }

                System.out.print(cell.toString());
                System.out.print(" - ");
            }
            System.out.println();

            lastRow = nextRow;
        }

        // This is for the last row, get the first cell. Should be date
        cell = lastRow.getCell(lastRow.getFirstCellNum());

        Date lastCellDate = cell.getDateCellValue();

        fileInfo.addLastDate(lastCellDate);

        return fileInfo;
    }

    private void fillExcelData(ExcelFileInfo fileInfo, CurrencyParser data) throws IOException
    {
        FileInputStream excelFileStream = new FileInputStream(new File(EXCEL_FILENAME));
        Workbook workbook = new XSSFWorkbook(excelFileStream);
        Sheet firstSheet = workbook.getSheetAt(0);

        Iterator<Row> rowIterator = firstSheet.iterator();
        Cell cell = null;

        Row lastRow = null;

        int eurPos = -1;
        int usdPos = -1;
        int gbpPos = -1;

        while (rowIterator.hasNext())
        {
            // Just pass all the rows and get to the last one

            Row nextRow = rowIterator.next();
            Iterator<Cell> cellIterator = nextRow.cellIterator();

            System.out.println("Row number : " + nextRow.getRowNum());

            lastRow = nextRow;
        }

        // This is for the last row, get the first cell. Should be date
        cell = lastRow.getCell(lastRow.getFirstCellNum());

        System.out.println("Last cel content : " + cell.toString());

        if (DateUtil.isCellDateFormatted((cell)))
        {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            // Important!!!, get in UTC, not in local time
            Calendar cal = Calendar.getInstance();
            // get current date
            long currentTimeMs = cal.getTimeInMillis();
            long currentTimeMsDelta = currentTimeMs % DAY_IN_MS;
            currentTimeMs -= currentTimeMsDelta;

            Date currentDate = new Date(currentTimeMs);

            LocalDate localDate = LocalDate.now();
            System.out.println(DateTimeFormatter.ofPattern("yyy/MM/dd").format(localDate));

            System.out.println("Time at the start : " + currentDate.toString());

            //binds the style you need to the cell.
            CellStyle dateCellStyle = workbook.createCellStyle();
            short df = workbook.createDataFormat().getFormat("dd.mm.yyyy");
            dateCellStyle.setDataFormat(df);

            List<CurrencyInfo> gbpList = data.getCurrencyData("GBP");
            List<CurrencyInfo> usdList = data.getCurrencyData("USD");
            List<CurrencyInfo> eurList = data.getCurrencyData("EUR");

            int numOfElements = gbpList.size();
            int lastRowIndex = lastRow.getRowNum();


            // Date is greater or equal to today date, then update
            for (int i = 0;i<numOfElements;i++)
            {
                gbpPos = fileInfo.getCurrencyPos("GBP");
                usdPos = fileInfo.getCurrencyPos("USD");
                eurPos = fileInfo.getCurrencyPos("EUR");
                Row newRow = firstSheet.createRow(lastRowIndex + i + 1);
                Date lastDate = fileInfo.getLastDate();
                Date newDate = new Date(lastDate.getTime()+DAY_IN_MS * (long)(i+1));

                // USD
                Cell usdCellDate = newRow.createCell(usdPos);
                Cell usdCellBuy = newRow.createCell(usdPos + 1);
                Cell usdCellAverage = newRow.createCell(usdPos + 2);
                Cell usdCellSell = newRow.createCell(usdPos + 3);
                usdCellDate.setCellValue(newDate);
                usdCellDate.setCellStyle(dateCellStyle);

                usdCellBuy.setCellValue(new BigDecimal(usdList.get(i).buyExchangeRate.replace(',','.')).setScale(FLOAT_DECIMALS).floatValue());
                usdCellAverage.setCellValue(new BigDecimal(usdList.get(i).averageExchangeRate.replace(',','.')).setScale(FLOAT_DECIMALS).floatValue());
                usdCellSell.setCellValue(new BigDecimal(usdList.get(i).sellExchangeRate.replace(',','.')).setScale(FLOAT_DECIMALS).floatValue());

                // EUR
                Cell eurCellDate = newRow.createCell(eurPos);
                Cell eurCellBuy = newRow.createCell(eurPos + 1);
                Cell eurCellAverage = newRow.createCell(eurPos + 2);
                Cell eurCellSell = newRow.createCell(eurPos + 3);
                eurCellDate.setCellValue(newDate);
                eurCellDate.setCellStyle(dateCellStyle);

                //String correctFloatString =  data[counter -1].buyExchangeRate.replace(',','.');
                eurCellBuy.setCellValue(new BigDecimal(eurList.get(i).buyExchangeRate.replace(',','.')).setScale(FLOAT_DECIMALS).floatValue());
                eurCellAverage.setCellValue(new BigDecimal(eurList.get(i).averageExchangeRate.replace(',','.')).setScale(FLOAT_DECIMALS).floatValue());
                eurCellSell.setCellValue(new BigDecimal(eurList.get(i).sellExchangeRate.replace(',','.')).setScale(FLOAT_DECIMALS).floatValue());


                // GBP
                Cell gbpCellDate = newRow.createCell(gbpPos);
                Cell gbpCellBuy = newRow.createCell(gbpPos + 1);
                Cell gbpCellAverage = newRow.createCell(gbpPos + 2);
                Cell gbpCellSell = newRow.createCell(gbpPos + 3);
                gbpCellDate.setCellValue(newDate);
                gbpCellDate.setCellStyle(dateCellStyle);

                gbpCellBuy.setCellValue(new BigDecimal(gbpList.get(i).buyExchangeRate.replace(',','.')).setScale(FLOAT_DECIMALS).floatValue());
                gbpCellAverage.setCellValue(new BigDecimal(gbpList.get(i).averageExchangeRate.replace(',','.')).setScale(FLOAT_DECIMALS).floatValue());
                gbpCellSell.setCellValue(new BigDecimal(gbpList.get(i).sellExchangeRate.replace(',','.')).setScale(FLOAT_DECIMALS).floatValue());
            }

        }
        else // layout in the excel table is not good
        {
            System.out.println("Layout inside the Excel table is not good");
        }

        excelFileStream.close();

        FileOutputStream outFile =new FileOutputStream(new File(EXCEL_FILENAME));
        workbook.write(outFile);
        outFile.close();

        workbook.close();
    }

    private CurrencyParser sendPost(ExcelFileInfo fileInfo) throws Exception {

        String url = "http://www.hnb.hr/temeljne-funkcije/monetarna-politika/tecajna-lista/tecajna-lista?p_p_id=tecajnalistacontroller_WAR_hnbtecajnalistaportlet&p_p_lifecycle=2&p_p_state=normal&p_p_mode=view&p_p_cacheability=cacheLevelPage&p_p_col_id=column-2&p_p_col_count=2";

        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);

        // add header
        post.setHeader("User-Agent", USER_AGENT);
        post.setHeader("Accept-Encoding", "zip, deflate");
        post.setHeader("Accept-Language", "hr-HR,hr;q=0.8,en-US;q=0.6,en;q=0.4");
        post.setHeader("Origin", "http://www.hnb.hr");
        post.setHeader("Upgrade-Insecure-Requests", "1");
        post.setHeader("Content-Type", "application/x-www-form-urlencoded");
        post.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        post.setHeader("Cache-Control", "max-age=0");
        post.setHeader("Referer", "http://www.hnb.hr/temeljne-funkcije/monetarna-politika/tecajna-lista/tecajna-lista");
        post.setHeader("Connection", "keep-alive");


        LocalDate localDate = LocalDate.now();
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("_tecajnalistacontroller_WAR_hnbtecajnalistaportlet_pageNum", ""));
        urlParameters.add(new BasicNameValuePair("_tecajnalistacontroller_WAR_hnbtecajnalistaportlet_dateFromMin", ""));
        urlParameters.add(new BasicNameValuePair("_tecajnalistacontroller_WAR_hnbtecajnalistaportlet_dateToMax", ""));
        urlParameters.add(new BasicNameValuePair("_tecajnalistacontroller_WAR_hnbtecajnalistaportlet_yearMin", ""));
        urlParameters.add(new BasicNameValuePair("_tecajnalistacontroller_WAR_hnbtecajnalistaportlet_yearMax", ""));
        urlParameters.add(new BasicNameValuePair("_tecajnalistacontroller_WAR_hnbtecajnalistaportlet_dateMaxDatePicker", "16.12.2016"));
        urlParameters.add(new BasicNameValuePair("_tecajnalistacontroller_WAR_hnbtecajnalistaportlet_vrstaReport", "1"));
        urlParameters.add(new BasicNameValuePair("year", "-1"));
        urlParameters.add(new BasicNameValuePair("yearLast", "-1"));
        urlParameters.add(new BasicNameValuePair("_tecajnalistacontroller_WAR_hnbtecajnalistaportlet_month", "-1"));
        urlParameters.add(new BasicNameValuePair("_tecajnalistacontroller_WAR_hnbtecajnalistaportlet_datumVrsta", "3"));
        urlParameters.add(new BasicNameValuePair("_tecajnalistacontroller_WAR_hnbtecajnalistaportlet_dateOn", DateTimeFormatter.ofPattern("dd.MM.yyyy").format(localDate)));
        urlParameters.add(new BasicNameValuePair("_tecajnalistacontroller_WAR_hnbtecajnalistaportlet_dateFrom", fileInfo.getFirstNextDateString()));
        urlParameters.add(new BasicNameValuePair("_tecajnalistacontroller_WAR_hnbtecajnalistaportlet_dateTo", DateTimeFormatter.ofPattern("dd.MM.yyyy").format(localDate)));

        int counter = 0;
        for (String currency : fileInfo.getCurrencies())
        {
            urlParameters.add(new BasicNameValuePair("izborValuta", currency));
            counter++;
        }

        /*
        urlParameters.add(new BasicNameValuePair("izborValuta", "EUR"));
        urlParameters.add(new BasicNameValuePair("izborValuta", "GBP"));
        urlParameters.add(new BasicNameValuePair("izborValuta", "USD"));
        urlParameters.add(new BasicNameValuePair("_izborValuta", "3"));
        */
        urlParameters.add(new BasicNameValuePair("_izborValuta", "" + counter));
        urlParameters.add(new BasicNameValuePair("_tecajnalistacontroller_WAR_hnbtecajnalistaportlet_vrstaTecaja", "-1"));
        urlParameters.add(new BasicNameValuePair("_tecajnalistacontroller_WAR_hnbtecajnalistaportlet_fileTypeForDownload", "JSON"));

        post.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(post);
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + post.getEntity());
        System.out.println("Response Code : " +
                response.getStatusLine().getStatusCode());

        //BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        InputStream initialStream = response.getEntity().getContent();

        byte[] buffer = new byte[8 * 1024];
        StringBuffer result = new StringBuffer();
        File targetFile = new File("targetFile.zip");
        targetFile.setWritable(true);
        targetFile.setReadable(true);
        FileOutputStream outStream = new FileOutputStream(targetFile);

        int bytesRead;
        while ((bytesRead = initialStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
            result.append(buffer);
        }

        outStream.close();
        String newFileName = "exchange_currency.json";

        File tempFile1 = unZip(targetFile, true, newFileName);

        Gson gson = new Gson();
        JsonReader reader = new JsonReader((new FileReader(newFileName)));
        //List<CurrencyInfo> data = gson.fromJson(reader, CurrencyInfo.class);
        CurrencyInfo[] data = gson.fromJson(reader, CurrencyInfo[].class);
        CurrencyParser parsedData = new CurrencyParser();

        for (int i = 0;i<data.length;i++)
        {
            CurrencyInfo element = data[i];
            parsedData.addCurrencyInfo(element);
        }

        return parsedData;

    }

    public static File unZip(File infile, boolean deleteGzipfileOnSuccess, String newFileName) throws IOException {
        ZipInputStream gin = new ZipInputStream(new FileInputStream(infile));
        ZipEntry entry= null;
        FileOutputStream fos = null;
        File outFile = new File(infile.getParent(), newFileName);
        int len;
        try
        {
            int i;
            int offset = 0;
            fos = new FileOutputStream(outFile);
            byte[] buf = new byte[100000];
            entry = gin.getNextEntry();
            while ((len = gin.read(buf)) > 0) {
                fos.write(buf, 0, len);
                for (i = 0;i<len;i++)
                {
                    System.out.print((char)buf[i]);
                }
                System.out.println("");
            }

            gin.close();
            fos.close();
            if (deleteGzipfileOnSuccess) {
                infile.delete();
            }
            return outFile;
        } catch (ZipException ex)
        {
            System.out.println(ex.getMessage());
        } catch (IOException ex)
        {
            System.out.println(ex.getMessage());
        } finally{
            if (gin != null) {
                gin.close();
            }
            if (fos != null) {
                fos.close();
            }
        }

        return outFile;
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if(!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        }
        finally {
            if(source != null) {
                source.close();
            }
            if(destination != null) {
                destination.close();
            }
        }
    }

}