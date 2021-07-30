package converter;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

        if (spreadsheetID != null) {
            Utils.writeToFile(Converter.convert(Utils.getJsonFromURL("https://spreadsheets.google.com/feeds/cells/" + spreadsheetID + "/1/public/full?alt=json"), JSONType.VALUE));
        } else {
            System.out.println("Please provide a valid SheetID");
        }
    }

    /**
     * External Conversions
     */
    public static String convertToJSON(String spreadsheetID, JSONType type) {
        return convert(Utils.getJsonFromURL("https://spreadsheets.google.com/feeds/cells/" + spreadsheetID + "/1/public/full?alt=json"), type);
    }

    /**
     * External Conversions with page selection
     */
    public static String convertToJSONPage(String spreadsheetID, JSONType type) {
        return convert(Utils.getJsonFromURL("https://spreadsheets.google.com/feeds/cells/" + spreadsheetID + "/public/full?alt=json"), type);
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
                int checkvalue = (spreadSheet.getFeed().getEntry()[spreadSheet.getFeed().getEntry().length - 1].getCell().getRow()) * keys.size();

                if (checkvalue > spreadSheet.getFeed().getEntry().length) {
                    spreadSheet = fixEmptyFields(keys.size(), spreadSheet, spreadSheet.getFeed().getEntry()[spreadSheet.getFeed().getEntry().length - 1].getCell().getRow());
                }

                List<SpreadSheet.Entry> entries = Arrays.asList(spreadSheet.getFeed().getEntry());
                JSONArray array = new JSONArray();

                for (int x = 0; x < (spreadSheet.getFeed().getEntry().length / keys.size()); x++) {
                    JSONObject jsonObject = new JSONObject();

                    if (x == 0) continue; // Ingore keys

                    for (int i = 0; i < keys.size(); i++) {
                        int selection = (x * keys.size()) + i;
                        String key = keys.get(i);

                        jsonObject.put(key, entries.get(selection).getCell().getContent());
                    }

                    array.put(jsonObject);
                }

                return array.toString(2);
            } catch (JsonSyntaxException e) {
            } catch (JSONException e) {
            }
        }

        return "Couldn't convert, Is the Spreadsheet published?";
    }

    public static String convert(String json, JSONType type) {
        if (json != null) {

            // Get Key, Value output (First row are keys)
            if (type == JSONType.VALUE) {
                return convert(json);
            }

            // Get Array Output (All Fields are values)
            if (type == JSONType.ARRAY) {
                SpreadSheet spreadSheet = GSON.fromJson(json, SpreadSheet.class);
                List<SpreadSheet.Entry> entries = Arrays.asList(spreadSheet.getFeed().getEntry());
                ArrayList<String> strings = new ArrayList<>();
                ArrayList<SpreadSheet.Entry> passed = new ArrayList<>();
                boolean finished = false;
                int passedCol = 0;

                while (!finished) {

                    for (int x = 0; x < entries.size(); x++) {
                        SpreadSheet.Entry entry = entries.get(x);
                        int col = entry.getCell().getColumn();

                        // Check if field is in right collumn
                        if (passedCol != col) continue;

                        if (!passed.contains(entry)) {
                            strings.add(entry.getCell().getContent());
                            passed.add(entry);
                        }
                    }

                    if (entries.size() == passed.size()) {
                        finished = true;
                    }

                    passedCol++;
                }

                String[] list = strings.toArray(new String[strings.size()]);
                return GSON.toJson(list);
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

        for (SpreadSheet.Entry entry : sheet.getFeed().getEntry()) {

            if (entry.getCell().getRow() > 1) break; // Don't continue if it's not first row

            keys.add(entry.getCell().getContent());
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
        SpreadSheet.Entry[] entries = spreadSheet.getFeed().getEntry();

        SpreadSheet newsheet = new SpreadSheet();
        newsheet.getFeed().setEntry(new SpreadSheet.Entry[max]);
        SpreadSheet.Entry[] newEntry = newsheet.getFeed().getEntry();


        // Initialize all values
        for (int x = 0; x < max; x++) {
            newEntry[x] = new SpreadSheet.Entry();
            newEntry[x].setCell(new SpreadSheet.Cell());
        }

        // Put everything into new Array at their required location
        for (int x = 0; x < entries.length; x++) {

            SpreadSheet.Entry entry = entries[x];
            int selection = ((entry.getCell().getRow() - 1) * fieldAmount) + (entry.getCell().getColumn() - 1);
            validValues.add(selection);
            SpreadSheet.Entry entry1 = newEntry[selection];

            SpreadSheet.Cell oldCell = entry.getCell();
            SpreadSheet.Cell newCell = entry1.getCell();

            newCell.setColumn(oldCell.getColumn());
            newCell.setRow(oldCell.getRow());
            newCell.setContent(oldCell.getContent());
        }

        // Fill in the empty values in array with "Empty"
        for (int x = 0; x < max; x++) {

            if (!validValues.contains(x)) {
                SpreadSheet.Entry entry1 = newEntry[x];
                SpreadSheet.Cell emptyCell = entry1.getCell();

                emptyCell.setColumn((x - (x / (fieldAmount * fieldAmount))) + 1);
                emptyCell.setRow(x / fieldAmount);
                emptyCell.setContent("Empty");
            }
        }

        return newsheet;
    }
}
