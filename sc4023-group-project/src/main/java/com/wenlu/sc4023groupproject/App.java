package com.wenlu.sc4023groupproject;

import com.opencsv.*;
import java.io.*;
import java.util.*;

public class App 
{
    private static final String INPUT = "ResalePricesSingapore.csv";
    private static final String OUTPUT = "ResalePricesSingapore_cleaned.csv";

    public static void main( String[] args )
    {
        File inputFile = new File(INPUT);

        if (!inputFile.exists()) {
            System.out.println("File not found: " + INPUT);
            return;
        }

        try (
            CSVReader reader = new CSVReaderBuilder(new FileReader(INPUT))
                    .withSkipLines(0)
                    .build();

            ICSVWriter writer = new CSVWriterBuilder(new FileWriter(OUTPUT))
                    .withSeparator(';')
                    .build();
        ) {

            List<String[]> rows = reader.readAll();

            if (rows.isEmpty()) {
                System.out.println("Empty file.");
                return;
            }

            // Read headers
            String[] headers = rows.get(0);

            Map<String, Integer> columnIndex = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                columnIndex.put(headers[i].trim().toLowerCase(), i);
            }

            // Write new headers
            String[] newHeader = {
                    "month_name",
                    "year",
                    "town",
                    "flat_type",
                    "block",
                    "street_name",
                    "storey_range",
                    "floor_area_sqm",
                    "flat_model",
                    "lease_commence_date",
                    "resale_price"
            };

            writer.writeNext(newHeader);

            // Process rows
            for (int i = 1; i < rows.size(); i++) {

                String[] row = rows.get(i);

                String monthRaw = getValue(row, columnIndex, "month");

                String monthName = "";
                String year = "";

                if (monthRaw != null && monthRaw.contains("-")) {
                    String[] parts = monthRaw.split("-");
                    if (parts.length == 2) {
                        monthName = parts[0];
                        try {
                            year = String.valueOf(2000 + Integer.parseInt(parts[1]));
                        } catch (NumberFormatException ignored) {}
                    }
                }

                String[] newRow = {
                        monthName,
                        year,
                        getValue(row, columnIndex, "town"),
                        getValue(row, columnIndex, "flat_type"),
                        getValue(row, columnIndex, "block"),
                        getValue(row, columnIndex, "street_name"),
                        getValue(row, columnIndex, "storey_range"),
                        getValue(row, columnIndex, "floor_area_sqm"),
                        getValue(row, columnIndex, "flat_model"),
                        getValue(row, columnIndex, "lease_commence_date"),
                        getValue(row, columnIndex, "resale_price")
                };

                writer.writeNext(newRow);
            }

            System.out.println("File saved as: " + OUTPUT);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static String getValue(String[] row, Map<String, Integer> indexMap, String columnName) {
        Integer index = indexMap.get(columnName);
        if (index != null && index < row.length) {
            return row[index];
        }
        return "";
    }
}

