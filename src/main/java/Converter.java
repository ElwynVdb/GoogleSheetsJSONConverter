import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.json.JSONArray;
import org.json.JSONException;
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
        String spreadsheetID = JOptionPane.showInputDialog(null, "Enter SpreadSheetID", "Spreadsheet to JSON", JOptionPane.OK_OPTION);

        if (spreadsheetID != null)
            System.out.println(convert(Utils.getJsonFromURL("https://spreadsheets.google.com/feeds/cells/" + spreadsheetID + "/1/public/full?alt=json")));
        else
            System.out.println("Please provide a valid SheetID");
    }

    /**
     * Convert Google Sheets API Json to clean JSON
     *
     * @param json
     * @return
     */
    public static String convert(String json) {
        if (json != null) {
            try {
                SpreadSheet spreadSheet = GSON.fromJson(json, SpreadSheet.class);
                List<String> keys = getKeys(spreadSheet);
                int checkvalue = (spreadSheet.feed.entry[spreadSheet.feed.entry.length - 1].gs$cell.row) * keys.size();

                if (checkvalue > spreadSheet.feed.entry.length) {
                    spreadSheet = fixEmptyFields(keys.size(), spreadSheet, spreadSheet.feed.entry[spreadSheet.feed.entry.length - 1].gs$cell.row);
                }

                List<SpreadSheet.Entry> entries = Arrays.asList(spreadSheet.feed.entry);
                JSONArray array = new JSONArray();

                for (int x = 0; x < (spreadSheet.feed.entry.length / keys.size()); x++) {
                    JSONObject jsonObject = new JSONObject();

                    if (x == 0) continue;

                    for (int i = 0; i < keys.size(); i++) {
                        int selection = (x * keys.size()) + i;
                        String key = keys.get(i);

                        jsonObject.put(key, entries.get(selection).gs$cell.$t);
                    }

                    array.put(jsonObject);
                }

                return array.toString();

            } catch (JsonSyntaxException e) {
            } catch (JSONException e) {
            }
        }

        return "Couldn't convert, Is the Spreadsheet published?";
    }

    /**
     * Get first row values
     *
     * @param sheet
     * @return List<String>
     */
    public static List<String> getKeys(SpreadSheet sheet) {
        List<String> keys = new ArrayList<>();

        for (SpreadSheet.Entry entry : sheet.feed.entry) {

            if (entry.gs$cell.row > 1) break;

            keys.add(entry.gs$cell.$t);
        }

        return keys;
    }

    /**
     * Fix any empty fields (Prevent JSON writing to mess up)
     *
     * @param fieldAmount
     * @param spreadSheet
     * @param rowsMax
     * @return
     */
    public static SpreadSheet fixEmptyFields(int fieldAmount, SpreadSheet spreadSheet, int rowsMax) {

        int max = fieldAmount * rowsMax;
        List<Integer> validValues = new ArrayList<>();
        SpreadSheet.Entry[] entries = spreadSheet.feed.entry;

        SpreadSheet newsheet = new SpreadSheet();
        newsheet.feed.entry = new SpreadSheet.Entry[max];
        SpreadSheet.Entry[] newEntry = newsheet.feed.entry;


        // Initialize all values
        for (int a = 0; a < max; a++) {
            newEntry[a] = new SpreadSheet.Entry();
            newEntry[a].gs$cell = new SpreadSheet.Cell();
        }

        // Put everything into new Array at their required location
        for (int x = 0; x < entries.length; x++) {

            SpreadSheet.Entry entry = entries[x];
            int selection = ((entry.gs$cell.row - 1) * fieldAmount) + (entry.gs$cell.col - 1);
            validValues.add(selection);
            SpreadSheet.Entry entry1 = newEntry[selection];

            entry1.gs$cell.col = entry.gs$cell.col;
            entry1.gs$cell.row = entry.gs$cell.row;
            entry1.gs$cell.$t = entry.gs$cell.$t;
        }

        // Fill in the empty values in array with "Empty"
        for (int i = 0; i < max; i++) {

            if (!validValues.contains(i)) {
                SpreadSheet.Entry entry1 = newEntry[i];
                entry1.gs$cell.col = (i - (i / fieldAmount * fieldAmount)) + 1;
                entry1.gs$cell.row = i / fieldAmount;
                entry1.gs$cell.$t = "Empty";
            }
        }

        return newsheet;
    }
}
