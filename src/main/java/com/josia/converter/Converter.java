package com.josia.converter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/** @author Josia */
public class Converter {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) {
        String spreadSheetID = displayInputScreen();

        if (spreadSheetID == null || spreadSheetID.length() == 0) {
            displayErrorScreen("You have entered the wrong spreadsheetID", "Invalid spreadsheetID");
            return;
        }

        JSONArray finalJsonResult = getJSONArrayFromSpreadSheet(spreadSheetID, 1, JSONReturnType.VALUE);

        if (finalJsonResult == null || finalJsonResult.length() == 0) {
            displayErrorScreen("This spreadsheet returned nothing", "Invalid spreadsheet");
            return;
        }

        Utils.writeToFile(finalJsonResult.toString(2));
    }

    public static JSONArray getJSONArrayFromSpreadSheet(String spreadsheetID, int page, JSONReturnType jsonReturnType){
        String rawSpreadsheetJson = Utils.getJsonFromURL(Utils.createSpreadSheetUrl(spreadsheetID, page));
        return Converter.convertSpreadsheetToJson(rawSpreadsheetJson, jsonReturnType);
    }

    private static JSONArray convertSpreadsheetToJson(String rawSpreadsheetJson, JSONReturnType jsonReturnType) {

        SpreadSheet spreadSheet = SpreadSheet.getFromJson(rawSpreadsheetJson);

        if (spreadSheet == null) {
            displayErrorScreen("You have entered the wrong spreadsheetID", "Invalid spreadsheetID");
            return null;
        }

        switch (jsonReturnType) {
            default:
            case VALUE:
                return createValueJson(spreadSheet);
            case HORIZONTAL_ARRAY:
                return createHorizontalJsonArray(spreadSheet);
            case VERTICAL_ARRAY:
                return createVerticalJsonArray(spreadSheet);
        }
    }

    private static JSONArray createValueJson(SpreadSheet spreadSheet) {
        List<String> keys = getKeys(spreadSheet);
        SpreadSheet.Cell finalCell = spreadSheet.getFeed().getEntry()[spreadSheet.getFeed().getEntry().length - 1].getCell();
        int finalRow = finalCell.getRow();
        int totalCellCount = keys.size() * finalRow;

        spreadSheet = fixEmptyFields(keys, totalCellCount, spreadSheet);

        JSONArray jsonArray = new JSONArray();

        for (int i = 0; i < (totalCellCount / keys.size()); i++) {
            if (i == 0) continue; // Ignore keys

            JSONObject jsonObject = new JSONObject();

            for (int j = 0; j < keys.size(); j++) {
                String key = keys.get(j);
                String content = spreadSheet.getFeed().getEntry()[(i * keys.size()) + j].getCell().getContent();

                jsonObject.put(key, content);
            }

            jsonArray.put(jsonObject);
        }

        return jsonArray;
    }

    private static SpreadSheet fixEmptyFields(List<String> keys, int totalCellCount, SpreadSheet oldSheet) {
        SpreadSheet newSheet = oldSheet;
        SpreadSheet.Feed feed = oldSheet.getFeed();

        if (totalCellCount > feed.getEntry().length) {
            newSheet = new SpreadSheet();
            newSheet.getFeed().setEntry(new SpreadSheet.Entry[totalCellCount]);
            SpreadSheet.Entry[] newEntry = newSheet.getFeed().getEntry();

            // Initialize
            for (int x = 0; x < totalCellCount; x++) {
                newEntry[x] = new SpreadSheet.Entry();
                newEntry[x].setCell(new SpreadSheet.Cell());
            }

            List<Integer> validPositions = new ArrayList<>();

            for (SpreadSheet.Entry entry : feed.getEntry()) {
                SpreadSheet.Cell cell = entry.getCell();
                int placementPosition = (keys.size() * (cell.getRow() - 1)) + (cell.getColumn() - 1);
                validPositions.add(placementPosition);
                newEntry[placementPosition].getCell().copyCell(cell);
            }

            SpreadSheet.Cell emptyCell = new SpreadSheet.Cell();
            emptyCell.setContent("Empty");

            for (int j = 0; j < totalCellCount; j++) {
                if (validPositions.contains(j)) continue;

                SpreadSheet.Cell cell = newEntry[j].getCell();

                cell.copyCell(emptyCell);
                cell.setRow(j / keys.size());
                cell.setColumn(j - (j / keys.size()));
            }
        }

        return newSheet;
    }

    private static List<String> getKeys(SpreadSheet spreadSheet) {
        List<String> keys = new ArrayList<>();

        for (SpreadSheet.Entry e : spreadSheet.getFeed().getEntry()) {
            if (e.getCell().getRow() != 1) break;
            keys.add(e.getCell().getContent());
        }

        return keys;
    }

    private static JSONArray createVerticalJsonArray(SpreadSheet spreadSheet) {
        JSONArray array = new JSONArray();
        int entryCount = spreadSheet.getFeed().getEntry().length;
        int column = 0;

        while (array.length() != entryCount) {

            for (SpreadSheet.Entry entry : spreadSheet.getFeed().getEntry()) {
                SpreadSheet.Cell cell = entry.getCell();

                if (cell.getColumn() != column) continue;

                array.put(entry.getCell().getContent());
            }

            column++;
        }

        return array;
    }

    private static JSONArray createHorizontalJsonArray(SpreadSheet spreadSheet) {
        JSONArray array = new JSONArray();

        for (SpreadSheet.Entry entry : spreadSheet.getFeed().getEntry()) {
            String content = entry.getCell().getContent();

            if (content != null && content.length() > 0) {
                array.put(content);
            }
        }

        return array;
    }

    private static String displayInputScreen() {
        return JOptionPane.showInputDialog(null, "Enter spreadSheetID", "Spreadsheet to JSON", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void displayErrorScreen(Object message, String title) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public static Gson getGSON() {
        return GSON;
    }
}
