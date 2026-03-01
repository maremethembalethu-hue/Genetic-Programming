import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

public class EnergyPredictionMain {
    static List<String> uct_timestamp = new ArrayList<>();
    static List<Double> electricLoad = new ArrayList<>();
    static String header;

    public static void main(String args[]) {

        File csvFile = new File("Residential_Energy_Dataset_UK-2014-2020.csv");
        try {
            Scanner readCSV = new Scanner(csvFile);
            header = readCSV.nextLine();

            while (readCSV.hasNextLine()) {
                uct_timestamp.add(readCSV.nextLine().split(",")[0]);
                electricLoad.add(Double.parseDouble(readCSV.nextLine().split(",")[1]));
            }

        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }

}
