package com.company;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class GetRates {

    public static void main(String[] args) throws FileNotFoundException, IOException {


        String[] currencies = {"USD", "EUR", "RUB", "GBP", "CHF", "PLN", "JPY", "CAD", "AUD", "DKK", "NOK", "SEK", "CZK", "ILS", "HUF"};
        String url = "http://minfin.com.ua/currency/";


        ArrayList<String> name = new ArrayList<>();
        ArrayList<String> buyRate = new ArrayList<>();
        ArrayList<String> sellRate = new ArrayList<>();

//        getGlobalStrings(url, name, buyRate, sellRate);
//        trimCardValues(buyRate, sellRate);

//        globalRateParser(buyRate, sellRate);
//        getCsv(name, buyRate, sellRate, "global");
//        pushToFtp("global");
//        for (String str :
//                buyRate) {
//            System.out.println(str);
//        }

        for (int i = 0; i < currencies.length; i++) {
            String endUrl = url + "/banks/" + currencies[i] + "/";
            getStrings(endUrl, name, buyRate, sellRate);
            trimCardValues(buyRate, sellRate);
            getCsv(name, buyRate, sellRate, currencies[i]);
        }

        for (String str :
                currencies) {
            pushToFtp(str);
        }
    }

//    private static void getGlobalStrings(String url, ArrayList<String> name, ArrayList<String> buyRate, ArrayList<String> sellRate) throws IOException {
//        Document doc;
//        Elements curNames;
//        Elements buyRates;
//        Elements sellRates;
//
//        try {
//            doc = Jsoup.connect(url).userAgent("Chrome").get();
//            curNames = doc.select("div.mfm-grey-bg").select("table.mfm-table.mfcur-table-lg.mfcur-table-lg-currency.has-no-tfoot").select("td.mfcur-table-cur");
//            buyRates = doc.select("div.mfm-grey-bg").select("table.mfm-table.mfcur-table-lg.mfcur-table-lg-currency.has-no-tfoot").select("td.mfm-text-nowrap");
//            sellRates = doc.select("div.mfm-grey-bg").select("table.mfm-table.mfcur-table-lg.mfcur-table-lg-currency.has-no-tfoot").select("td.mfm-text-nowrap");
//            for (int i = 0; i < 6; i++) {
//                name.add(curNames.get(i).text());
//                System.out.println(curNames.get(i).text());
//            }
//            for (int i = 0; i < 12; i++) {
//                buyRate.add(buyRates.get(i).text());
//                //System.out.println(buyRate.get(i));
//            }
//            for (int i = 0; i < 12; i++) {
//                sellRate.add(sellRates.get(i).text());
//                //System.out.println(buyRate.get(i));
//            }
//
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//
//    }

//    public static void globalRateParser(ArrayList<String> buy, ArrayList<String> sell) {
//        ArrayList<String> tempBuy = new ArrayList<>();
//        ArrayList<String> tempSell = new ArrayList<>();
//        String delim = "[ /]+";
//        String[] tokens;
//
//        for (String string
//                :
//                buy) {
//            tokens = string.split(delim);
//            tempBuy.add(tokens[0]);
//            tempSell.add(tokens[1]);
//        }
//        buy.clear();
//        buy.addAll(tempBuy);
//        sell.clear();
//        sell.addAll(tempSell);
//        tempBuy.clear();
//        tempSell.clear();
//
//
//    }

    private static void getStrings(String url, ArrayList<String> name, ArrayList<String> buyRate, ArrayList<String> sellRate) {
        Document doc;
        Elements bankNames;
        Elements buyRates;
        Elements sellRates;
        try {
            doc = Jsoup.connect(url).userAgent("Chrome").get();
            bankNames = doc.select("div.mfm-grey-bg").select("tbody.list").select("tr").select("td.mfcur-table-bankname");
            buyRates = doc.select("div.mfm-grey-bg").select("tbody.list").select("tr").select("td.mfm-text-right.mfm-pr0");
            sellRates = doc.select("div.mfm-grey-bg").select("tbody.list").select("tr").select("td.mfm-text-left.mfm-pl0");
            for (Element elements :
                    bankNames) {
                name.add(elements.text());
            }
            bankNames.clear();
            for (Element elements :
                    buyRates) {
                buyRate.add(elements.text());
            }
            buyRates.clear();
            for (Element elements :
                    sellRates) {
                sellRate.add(elements.text());
            }
            sellRates.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void trimCardValues(ArrayList<String> buy, ArrayList<String> sell) {

        ArrayList<String> tempBuy = new ArrayList<>();
        ArrayList<String> tempSell = new ArrayList<>();

        for (int j = 0; j < buy.size(); j++) {
            if (j % 2 == 0) {
                tempBuy.add(buy.get(j));
                tempSell.add(sell.get(j));
            }
        }
        buy.clear();
        buy.addAll(tempBuy);
        sell.clear();
        sell.addAll(tempSell);
        tempBuy.clear();
        tempSell.clear();
    }

    private static void getCsv(ArrayList<String> name, ArrayList<String> buyRate, ArrayList<String> sellRate, String str) throws FileNotFoundException {
        PrintWriter printWriter = new PrintWriter(str + ".csv");
        for (int i = 0; i < name.size(); i++) {
            printWriter.println(name.get(i) + ";" + buyRate.get(i) + ";" + sellRate.get(i));
        }
        printWriter.close();
        name.clear();
        buyRate.clear();
        sellRate.clear();
    }

    private static void pushToFtp(String str) {
        FTPClient ftpClient = new FTPClient();
        String filename = (str + ".csv");
        System.out.println(filename);
        try (FileInputStream fis = new FileInputStream(filename)) {
            ftpClient.connect("deathstormclan.ru");
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
            ftpClient.login("rates@deathstormclan.ru", "SOMEPSWD");
            ftpClient.storeFile(filename, fis);
            ftpClient.logout();
            ftpClient.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
