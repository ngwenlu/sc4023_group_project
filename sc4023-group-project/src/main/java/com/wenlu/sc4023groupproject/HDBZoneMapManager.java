package com.wenlu.sc4023groupproject;

import com.opencsv.*;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class HDBZoneMapManager {
    
    private static final String INPUT_DIR = "dataset_columns_excel/";
    private static final String OUTPUT_DIR = "metadata/";
    private static final String[] FILENAMES = {
        "year.csv", "floor_area.csv", "lease_date.csv", "price.csv"
    };
    private static final int ZONE_SIZE = 10;

    public static void main(String[] args) {
        // Check if input files are present
        for (int i = 0; i < FILENAMES.length; i++) {
            File inputFile = new File(INPUT_DIR + FILENAMES[i]);
            if (!inputFile.exists()) {
                System.out.println("File not found: " + INPUT_DIR + FILENAMES[i]);
                return;
            }
        }        

        // Create output directory
        new File(OUTPUT_DIR).mkdirs();

        // Zone Map Creation
        for (int i = 0; i < FILENAMES.length; i++) {
            System.out.println("Creating zone map metadata for " + FILENAMES[i] + " ...");

            BufferedWriter writer = null;

            try {
                // Initialize new writer for current file
                writer = new BufferedWriter(new FileWriter(OUTPUT_DIR + FILENAMES[i]));
                writer.write("Start,End,Min,Max");
                writer.newLine();

                // Create reader for current file
                try (CSVReader reader = new CSVReaderBuilder(new FileReader(INPUT_DIR + FILENAMES[i])).build()) {

                    String[] header = reader.readNext();
                    if (header == null) continue;

                    // Read rows and find min max
                    String[] row;
                    int count = 0, last_written = 0;
                    BigDecimal min_value = new BigDecimal(Double.MAX_VALUE), max_value = new BigDecimal(Double.MIN_VALUE), value;
                    while ((row = reader.readNext()) != null) {
                        value = new BigDecimal(row[0]);
                        min_value = value.compareTo(min_value) < 0 ? value : min_value;
                        max_value = value.compareTo(max_value) > 0 ? value : max_value;

                        count++;
                        if (count % ZONE_SIZE == 0) {
                            String metadata_row = String.format("%d,%d,%s,%s", 
                                count - ZONE_SIZE + 1, count,
                                min_value.setScale(2, RoundingMode.HALF_UP).toPlainString(), 
                                max_value.setScale(2, RoundingMode.HALF_UP).toPlainString()
                            );
                            writer.write(metadata_row);
                            writer.newLine();

                            min_value = new BigDecimal(Double.MAX_VALUE);
                            max_value = new BigDecimal(Double.MIN_VALUE);
                            last_written = count;
                        }

                        if (count % 10000 == 0) System.out.println("Processed " + count + " rows...");
                    }

                    // Add remaining rows
                    if (last_written < count) {
                        String metadata_row = String.format("%d,%d,%s,%s", 
                            count - ZONE_SIZE + 1, count,
                            min_value.setScale(2, RoundingMode.HALF_UP).toPlainString(), 
                            max_value.setScale(2, RoundingMode.HALF_UP).toPlainString()
                        );
                        writer.write(metadata_row);
                        writer.newLine();
                    }
                } 
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (writer != null) writer.close();
                } catch (IOException ignored) {}
            }
        }
    }
}
