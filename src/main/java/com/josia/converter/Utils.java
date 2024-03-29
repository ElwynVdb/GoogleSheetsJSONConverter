package com.josia.converter;

import java.io.*;
import java.net.URL;

public class Utils {

    public static String getJsonFromURL(String url) {

        try {
            URL URL = new URL(url);
            BufferedReader reader = new BufferedReader(new InputStreamReader(URL.openStream()));
            return bufferedReaderToString(reader);
        } catch (IOException e) {
            System.out.println("Please define a valid URL");
        }

        return null;
    }

    public static String bufferedReaderToString(BufferedReader e) {
        StringBuilder builder = new StringBuilder();
        String line;

        try {
            while ((line = e.readLine()) != null) {
                builder.append(line);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return builder.toString();
    }

    public static void writeToFile(Object object) {
        try {
            File f = new File("output.json");
            boolean createdFile = true;

            if (!f.exists()) {
                createdFile = f.createNewFile();
            }

            if(createdFile) {
                FileWriter writer = new FileWriter(f);
                writer.write((String) object);
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String createSpreadSheetUrl(String spreadsheetID, int page) {
        return "https://spreadsheets.google.com/feeds/cells/" + spreadsheetID + "/" + page + "/public/full?alt=json";
    }
}
