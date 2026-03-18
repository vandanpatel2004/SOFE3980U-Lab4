package com.ontariotechu.sofe3980U;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class App {

    public static void main(String[] args) {
        String[] files = {"model_1.csv", "model_2.csv", "model_3.csv"};

        double bestBCE = Double.MAX_VALUE;
        double bestAccuracy = -1;
        double bestPrecision = -1;
        double bestRecall = -1;
        double bestF1 = -1;
        double bestAUC = -1;

        String bestBCEFile = "";
        String bestAccuracyFile = "";
        String bestPrecisionFile = "";
        String bestRecallFile = "";
        String bestF1File = "";
        String bestAUCFile = "";

        for (String file : files) {
            Metrics m = evaluate(file);

            System.out.println("for " + file);
            System.out.println("\tBCE =" + m.bce);
            System.out.println("\tConfusion matrix");
            System.out.println("\t\t\t y=1 \t y=0");
            System.out.println("\t\ty^=1 \t" + m.tp + "\t" + m.fp);
            System.out.println("\t\ty^=0 \t" + m.fn + "\t" + m.tn);
            System.out.println("\tAccuracy =" + m.accuracy);
            System.out.println("\tPrecision =" + m.precision);
            System.out.println("\tRecall =" + m.recall);
            System.out.println("\tf1 score =" + m.f1);
            System.out.println("\tauc roc =" + m.auc);

            if (m.bce < bestBCE) {
                bestBCE = m.bce;
                bestBCEFile = file;
            }
            if (m.accuracy > bestAccuracy) {
                bestAccuracy = m.accuracy;
                bestAccuracyFile = file;
            }
            if (m.precision > bestPrecision) {
                bestPrecision = m.precision;
                bestPrecisionFile = file;
            }
            if (m.recall > bestRecall) {
                bestRecall = m.recall;
                bestRecallFile = file;
            }
            if (m.f1 > bestF1) {
                bestF1 = m.f1;
                bestF1File = file;
            }
            if (m.auc > bestAUC) {
                bestAUC = m.auc;
                bestAUCFile = file;
            }
        }

        System.out.println("According to BCE, The best model is " + bestBCEFile);
        System.out.println("According to Accuracy, The best model is " + bestAccuracyFile);
        System.out.println("According to Precision, The best model is " + bestPrecisionFile);
        System.out.println("According to Recall, The best model is " + bestRecallFile);
        System.out.println("According to F1 score, The best model is " + bestF1File);
        System.out.println("According to AUC ROC, The best model is " + bestAUCFile);
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

        double epsilon = 0.000001;
        double bce = 0.0;

        int tp = 0;
        int fp = 0;
        int tn = 0;
        int fn = 0;

        List<Point> rocPoints = new ArrayList<>();

        int n = allData.size();
        int positives = 0;
        int negatives = 0;

        for (String[] row : allData) {
            int yTrue = Integer.parseInt(row[0]);
            if (yTrue == 1) positives++;
            else negatives++;
        }

        for (String[] row : allData) {
            int yTrue = Integer.parseInt(row[0]);
            double yPred = Double.parseDouble(row[1]);

            yPred = Math.max(epsilon, Math.min(1.0 - epsilon, yPred));
            bce += yTrue * Math.log(yPred) + (1 - yTrue) * Math.log(1 - yPred);

            int yBinary = (yPred >= 0.5) ? 1 : 0;

            if (yBinary == 1 && yTrue == 1) tp++;
            else if (yBinary == 1 && yTrue == 0) fp++;
            else if (yBinary == 0 && yTrue == 0) tn++;
            else fn++;
        }

        bce = -bce / n;

        for (int i = 0; i <= 100; i++) {
            double th = i / 100.0;
            int rocTP = 0;
            int rocFP = 0;

            for (String[] row : allData) {
                int yTrue = Integer.parseInt(row[0]);
                double yPred = Double.parseDouble(row[1]);

                if (yTrue == 1 && yPred >= th) rocTP++;
                if (yTrue == 0 && yPred >= th) rocFP++;
            }

            double tpr = (double) rocTP / positives;
            double fpr = (double) rocFP / negatives;

            rocPoints.add(new Point(fpr, tpr));
        }

        rocPoints.sort(Comparator.comparingDouble(p -> p.x));

        double auc = 0.0;
        for (int i = 1; i < rocPoints.size(); i++) {
            Point p1 = rocPoints.get(i - 1);
            Point p2 = rocPoints.get(i);
            auc += (p1.y + p2.y) * Math.abs(p2.x - p1.x) / 2.0;
        }

        Metrics m = new Metrics();
        m.bce = bce;
        m.tp = tp;
        m.fp = fp;
        m.tn = tn;
        m.fn = fn;
        m.accuracy = (double) (tp + tn) / (tp + tn + fp + fn);
        m.precision = (double) tp / (tp + fp);
        m.recall = (double) tp / (tp + fn);
        m.f1 = 2.0 * m.precision * m.recall / (m.precision + m.recall);
        m.auc = auc;

        return m;
    }

    static class Metrics {
        double bce;
        int tp;
        int fp;
        int tn;
        int fn;
        double accuracy;
        double precision;
        double recall;
        double f1;
        double auc;
    }

    static class Point {
        double x;
        double y;

        Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}