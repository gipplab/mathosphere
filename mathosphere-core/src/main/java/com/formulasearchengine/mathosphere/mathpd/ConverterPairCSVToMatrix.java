package com.formulasearchengine.mathosphere.mathpd;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.flink.api.java.tuple.Tuple2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Quickly hacked tool to convert pairs with distances in rows to matrix representation
 * Felix Hamborg
 */
public class ConverterPairCSVToMatrix {
    private static final CSVFormat CSV_FORMAT = CSVFormat.RFC4180.withSkipHeaderRecord();

    private static Tuple2<String, String> getDocumentIDsFromRow(CSVRecord row) {
        return new Tuple2<>(row.get(0), row.get(1));
    }

    private static double getDistanceFromRow(CSVRecord row, int distanceIndex) {
        return Double.valueOf(row.get(2 + distanceIndex));
    }

    private static List<Double> getOrderedRow(final HashMap<Tuple2<String, String>, Double> matrix, final List<String> orderedDimensionValues, String rowName) {
        final List<Double> orderedCellValues = new ArrayList<>();

        for (String dimensionValue : orderedDimensionValues) {
            double value = matrix.getOrDefault(new Tuple2<String, String>(rowName, dimensionValue), -10000.0);
            orderedCellValues.add(value);
        }

        return orderedCellValues;
    }

    private static List<Object> getRowWithDescriptionInFirstCol(String rowName, List<Double> values) {
        List<Object> entries = new ArrayList<>();
        entries.add(rowName);
        entries.addAll(values);

        return entries;
    }

    private static void writeOrderedMatrix(HashMap<Tuple2<String, String>, Double> matrix, String filepath) throws Exception {
        final List<String> orderedRowValues = new ArrayList<>();
        final List<String> orderedColValues = new ArrayList<>();
        System.out.println("merging " + matrix.size() + " keys (matrix size in cells: " + (matrix.size() * matrix.size()) + ")");
        int tmpCounter = 0;
        for (Tuple2<String, String> key : matrix.keySet()) {
            if (!orderedRowValues.contains(key.f0))
                orderedRowValues.add(key.f0);
            if (!orderedColValues.contains(key.f1))
                orderedColValues.add(key.f1);

            if (++tmpCounter % 100000 == 0) {
                System.out.println("merged " + tmpCounter + " keys (" + (tmpCounter / (float) matrix.size()) + ")");
            }
        }
        // sort
        System.out.println("sorting rows");
        Collections.sort(orderedRowValues);
        System.out.println("sorting columns");
        Collections.sort(orderedColValues);

        // print to disk
        final CSVPrinter printer = new CSVPrinter(new FileWriter(filepath), CSV_FORMAT);

        // write first row (header)
        List<String> tmpHeader = new ArrayList<>();
        tmpHeader.add("");
        tmpHeader.addAll(orderedColValues);
        printer.printRecord(tmpHeader);

        System.out.println("writing " + orderedRowValues.size() + " records");
        for (String rowName : orderedRowValues) {
            printer.printRecord(
                    getRowWithDescriptionInFirstCol(
                            rowName,
                            getOrderedRow(matrix, orderedColValues, rowName)));
        }
        printer.close();
    }

    public static void main(String[] args) throws Exception {
        System.out.println("number of args given = " + args.length);
        String in = null;
        if (args.length == 0) {
            System.out.println("input file name? ");
            BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
            in = buffer.readLine().trim();
        } else {
            in = args[0];
        }
        final String outbase = in + "_out_";

        for (int i = 0; i < 5; i++) {
            System.out.println("parsing file to CSV: " + in);
            final CSVParser parser = CSVParser.parse(new File(in), Charset.defaultCharset(), CSV_FORMAT);
            System.out.println("finished parsing");

            System.out.println("creating matrix (" + i + ")");
            final HashMap<Tuple2<String, String>, Double> matrix = new HashMap<>();
            for (CSVRecord row : parser) {
                Tuple2<String, String> key = getDocumentIDsFromRow(row);
                if (matrix.containsKey(key)) {
                    throw new RuntimeException("matrix already contains key: " + key);
                }

                matrix.put(key, getDistanceFromRow(row, i));
            }
            System.out.println("finished creating matrix (" + i + ")");

            System.out.println("writing matrix");
            writeOrderedMatrix(matrix, outbase + i + ".csv");
            System.out.println("finished writing matrix");

            // reset parser
            parser.close();
        }
    }
}
