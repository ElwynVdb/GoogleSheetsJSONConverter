package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class Utils
{
    public static String getJsonFromURL(String url) {

        try {
            URL URL = new URL(url);
            BufferedReader reader = new BufferedReader(new InputStreamReader(URL.openStream()));
            String line = bufferedReaderToString(reader);
            return line;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String bufferedReaderToString(BufferedReader e) {
        StringBuilder builder = new StringBuilder();
        String line = "";

        try{
            while ((line = e.readLine()) != null) {
                builder.append(line);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return builder.toString();
    }
}
