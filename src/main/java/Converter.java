import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import org.json.JSONObject;
import util.SpreadSheet;
import util.Utils;

import javax.swing.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Author : Josia50
 * Finished on : 16/01/2020
 */
public class Converter {

    public static Gson GSON = new Gson().newBuilder().setPrettyPrinting().create();

    public static void main(String[] args) {
          String url = JOptionPane.showInputDialog(null, "Enter url", "Sheets to nice JSON", JOptionPane.OK_OPTION);

        System.out.println(convert(Utils.getJsonFromURL(url)));
    }

    public static String convert(String json) {
        SpreadSheet sheet = GSON.fromJson(json, SpreadSheet.class);
        List<String> fields = getFieldNames(sheet);
        int checkvalue = (sheet.feed.entry[sheet.feed.entry.length - 1].gs$cell.row) * fields.size();

        if (checkvalue > sheet.feed.entry.length) {
            sheet = fixEmptyFields(fields.size(), sheet, sheet.feed.entry[sheet.feed.entry.length - 1].gs$cell.row);
        }


        List<SpreadSheet.Entry> entries = Arrays.asList(sheet.feed.entry);
        JSONArray object = new JSONArray();

        for (int x = 0; x < (sheet.feed.entry.length / fields.size()); x++) {
            JSONObject jsonObject = new JSONObject();

            if (x == 0) continue;

            for (int i = 0; i < fields.size(); i++) {
                int selection = (x * fields.size()) + i;
                String field = fields.get(i);

                jsonObject.put(field, entries.get(selection).gs$cell.$t);
            }

            object.put(jsonObject);
        }

        return object.toString();
    }

    public static List<String> getFieldNames(SpreadSheet sheet) {
        List<String> fields = new ArrayList<>();

        for (SpreadSheet.Entry entry : sheet.feed.entry) {

            if (entry.gs$cell.row > 1) break;

            fields.add(entry.gs$cell.$t);
        }


        return fields;
    }

    public static SpreadSheet fixEmptyFields(int fieldAmount, SpreadSheet sheet, int rowsMax) {
        try {
            SpreadSheet newsheet = new SpreadSheet();
            SpreadSheet.Entry[] entries = sheet.feed.entry;
            int max = fieldAmount * rowsMax;

            newsheet.feed.entry = new SpreadSheet.Entry[max];
            SpreadSheet.Entry[] newEntry = newsheet.feed.entry;
            List<Integer> passed = new ArrayList<>();

            // Initialize all fields
            for (int a = 0; a < max; a++) {
                newEntry[a] = new SpreadSheet.Entry();
                newEntry[a].gs$cell = new SpreadSheet.Cell();
            }


            // Put everything into new Array at their respected location
            for (int x = 0; x < entries.length; x++) {

                SpreadSheet.Entry entry = entries[x];
                int selection = ((entry.gs$cell.row - 1) * fieldAmount) + (entry.gs$cell.col - 1);
                passed.add(selection);
                SpreadSheet.Entry entry1 = newEntry[selection];

                entry1.gs$cell.col = entry.gs$cell.col;
                entry1.gs$cell.row = entry.gs$cell.row;
                entry1.gs$cell.$t = entry.gs$cell.$t;
            }

            // Fill in the empty fields in array with "Empty"
            for (int is = 0; is < max; is++) {

                if (!passed.contains(is)) {
                    SpreadSheet.Entry entry1 = newEntry[is];
                    entry1.gs$cell.col = (is - (is / fieldAmount * fieldAmount)) + 1;
                    entry1.gs$cell.row = is / fieldAmount;
                    entry1.gs$cell.$t = "Empty";
                }
            }

            return newsheet;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
