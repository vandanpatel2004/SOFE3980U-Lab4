package com.ontariotechu.sofe3980U;

import java.io.FileReader;
import java.util.List;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

/**
 * Evaluate Single Variable Continuous Regression
 */
public class App {

    public static void main(String[] args) {
        String[] files = {"model_1.csv", "model_2.csv", "model_3.csv"};

        double bestMSE = Double.MAX_VALUE;
        double bestMAE = Double.MAX_VALUE;
        double bestMARE = Double.MAX_VALUE;

        String bestMSEFile = "";
        String bestMAEFile = "";
        String bestMAREFile = "";

        for (String file : files) {
            Metrics m = evaluate(file);

            System.out.println("for " + file);
            System.out.println("\tMSE =" + m.mse);
            System.out.println("\tMAE =" + m.mae);
            System.out.println("\tMARE =" + m.mare);

            if (m.mse < bestMSE) {
                bestMSE = m.mse;
                bestMSEFile = file;
            }
            if (m.mae < bestMAE) {
                bestMAE = m.mae;
                bestMAEFile = file;
            }
            if (m.mare < bestMARE) {
                bestMARE = m.mare;
                bestMAREFile = file;
            }
        }

        System.out.println("According to MSE, The best model is " + bestMSEFile);
        System.out.println("According to MAE, The best model is " + bestMAEFile);
        System.out.println("According to MARE, The best model is " + bestMAREFile);
    }

    static Metrics evaluate(String filePath) {
        FileReader filereader;
        List<String[]> allData;

        try {
            filereader = new FileReader(filePath);
            CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build();
            allData = csvReader.readAll();
        } catch (Exception e) {
            System.out.println("Error reading the CSV file: " + filePath);
            return new Metrics();
        }

        double mse = 0.0;
        double mae = 0.0;
        double mare = 0.0;
        double epsilon = 0.000001;

        int n = allData.size();

        for (String[] row : allData) {
            double yTrue = Double.parseDouble(row[0]);
            double yPred = Double.parseDouble(row[1]);

            double error = yTrue - yPred;

            mse += error * error;
            mae += Math.abs(error);
            mare += Math.abs(error) / (Math.abs(yTrue) + epsilon);
        }

        Metrics m = new Metrics();
        m.mse = mse / n;
        m.mae = mae / n;
        m.mare = mare / n;

        return m;
    }

    static class Metrics {
        double mse;
        double mae;
        double mare;
    }
}