package com.wenlu.sc4023groupproject;

import com.opencsv.*;
import java.io.*;
import java.util.*;

public class HDBColumnStoreManager {

    private static final String INPUT = "ResalePricesSingapore.csv";
    private static final String COLUMN_DIR = "dataset_columns_excel/";

    public static void main(String[] args) {
        File inputFile = new File(INPUT);
        if (!inputFile.exists()) {
            System.out.println("File not found: " + INPUT);
            return;
        }

        // Create the directory
        new File(COLUMN_DIR).mkdirs();

        // These will be individual files that Excel can open
        String[] fileNames = {
            "month.csv", "year.csv", "town.csv", "flat_type.csv", 
            "block.csv", "street_name.csv", "storey_range.csv", 
            "floor_area.csv", "flat_model.csv", "lease_date.csv", "price.csv"
        };

        BufferedWriter[] writers = new BufferedWriter[fileNames.length];

        try (CSVReader reader = new CSVReaderBuilder(new FileReader(INPUT)).build()) {
            
            String[] headers = reader.readNext();
            if (headers == null) return;

            Map<String, Integer> colMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                colMap.put(headers[i].trim().toLowerCase(), i);
            }

            // Initialize writers for each column file
            for (int i = 0; i < fileNames.length; i++) {
                writers[i] = new BufferedWriter(new FileWriter(COLUMN_DIR + fileNames[i]));
                // Adding a header to each individual file so it looks good in Excel
                writers[i].write(fileNames[i].replace(".csv", ""));
                writers[i].newLine();
            }

            System.out.println("Streaming data into column-oriented CSV files...");

            String[] row;
            int count = 0;
            while ((row = reader.readNext()) != null) {
                
                // --- Cleaning Logic ---
                String monthRaw = getValue(row, colMap, "month");
                String monthNum = "";
                String year = "";

                if (monthRaw != null && monthRaw.contains("-")) {
                    String[] parts = monthRaw.split("-");
                    if (parts.length == 2) {
                        year = parts[0]; 
                        monthNum = parts[1];
                    }
                }

                String[] cleanedValues = {
                    monthNum, year, getValue(row, colMap, "town"), 
                    getValue(row, colMap, "flat_type"), getValue(row, colMap, "block"), 
                    getValue(row, colMap, "street_name"), getValue(row, colMap, "storey_range"), 
                    getValue(row, colMap, "floor_area_sqm"), getValue(row, colMap, "flat_model"), 
                    getValue(row, colMap, "lease_commence_date"), getValue(row, colMap, "resale_price")
                };

                // Write to respective files
                for (int i = 0; i < writers.length; i++) {
                    writers[i].write(cleanedValues[i]);
                    writers[i].newLine();
                }
                
                count++;
                if (count % 10000 == 0) System.out.println("Processed " + count + " rows...");
            }

            System.out.println("Finished! Created " + fileNames.length + " files in " + COLUMN_DIR);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            for (BufferedWriter bw : writers) {
                try { if (bw != null) bw.close(); } catch (IOException ignored) {}
            }
        }
    }

    private static String getValue(String[] row, Map<String, Integer> indexMap, String columnName) {
        Integer index = indexMap.get(columnName);
        return (index != null && index < row.length) ? row[index] : "";
    }
}