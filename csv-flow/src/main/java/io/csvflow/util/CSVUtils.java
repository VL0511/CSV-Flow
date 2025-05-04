package io.csvflow.util;

import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CSVUtils {
    public static File generateCSVFileWithHeadersOnly(List<String> columnHeaders) throws IOException {
        File csvFile = File.createTempFile("export_headers_", ".csv");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile));
             CSVPrinter csvPrinter =
                     new CSVPrinter(writer
                             , org.apache.commons.csv.CSVFormat.DEFAULT
                             .withHeader(columnHeaders.toArray(new String[0])))) {
        }

        return csvFile;
    }
    
    public static File generateCSVFile(List<String> columnHeaders, List<Object[]> tableData) throws IOException {
        File csvFile = File.createTempFile("export_", ".csv");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile));
             CSVPrinter csvPrinter = new CSVPrinter(writer, org.apache.commons.csv.CSVFormat.DEFAULT.withHeader(columnHeaders.toArray(new String[0])))) {

            for (Object[] row : tableData) {
                csvPrinter.printRecord(row);
            }
        }

        return csvFile;
    }
}
