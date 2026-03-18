package com.ontariotechu.sofe3980U;

import java.io.FileReader;
import java.util.List;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class App {

    public static void main(String[] args) {
        String filePath = "model.csv";

        FileReader filereader;
        List<String[]> allData;

        try {
            filereader = new FileReader(filePath);
            CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build();
            allData = csvReader.readAll();
        } catch (Exception e) {
            System.out.println("Error reading the CSV file");
            return;
        }

        double ce = 0.0;
        double epsilon = 0.000001;
        int[][] confusion = new int[5][5];

        for (String[] row : allData) {
            int yTrue = Integer.parseInt(row[0]);
            double[] probs = new double[5];

            for (int i = 0; i < 5; i++) {
                probs[i] = Double.parseDouble(row[i + 1]);
            }

            double trueProb = Math.max(probs[yTrue - 1], epsilon);
            ce += -Math.log(trueProb);

            int predictedClass = 1;
            double maxProb = probs[0];

            for (int i = 1; i < 5; i++) {
                if (probs[i] > maxProb) {
                    maxProb = probs[i];
                    predictedClass = i + 1;
                }
            }

            confusion[predictedClass - 1][yTrue - 1]++;
        }

        ce /= allData.size();

        System.out.println("CE =" + ce);
        System.out.println("Confusion matrix");
        System.out.println("\t\ty=1\t y=2\t y=3\t y=4\t y=5");

        for (int i = 0; i < 5; i++) {
            System.out.print("\ty^=" + (i + 1) + "\t");
            for (int j = 0; j < 5; j++) {
                System.out.print(confusion[i][j] + "\t");
            }
            System.out.println();
        }
    }
}