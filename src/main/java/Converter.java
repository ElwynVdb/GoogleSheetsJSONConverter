import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONObject;
import util.SpreadSheet;
import util.Utils;

import javax.swing.*;
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

        if (url != null)
            System.out.println(convert(Utils.getJsonFromURL(url)));
        else
            System.out.println("Please provide an URL");
    }

    /**
     * Convert Google Sheets API Json to clean JSON
     *
     * @param json
     * @return
     */
    public static String convert(String json) {
        if (json != null) {

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

        return "Couldn't convert";
    }

    /**
     * Get first row values
     *
     * @param sheet
     * @return List<String>
     */
    public static List<String> getFieldNames(SpreadSheet sheet) {
        List<String> fields = new ArrayList<>();

        for (SpreadSheet.Entry entry : sheet.feed.entry) {

            if (entry.gs$cell.row > 1) break;

            fields.add(entry.gs$cell.$t);
        }

        return fields;
    }

    /**
     * Fix any empty fields (Prevent JSON writing to mess up)
     *
     * @param fieldAmount
     * @param sheet
     * @param rowsMax
     * @return
     */
    public static SpreadSheet fixEmptyFields(int fieldAmount, SpreadSheet sheet, int rowsMax) {

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
        for (int i = 0; i < max; i++) {

            if (!passed.contains(i)) {
                SpreadSheet.Entry entry1 = newEntry[i];
                entry1.gs$cell.col = (i - (i / fieldAmount * fieldAmount)) + 1;
                entry1.gs$cell.row = i / fieldAmount;
                entry1.gs$cell.$t = "Empty";
            }
        }

        return newsheet;
    }
}
