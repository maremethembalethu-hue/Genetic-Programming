import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class EnergyPredictionMain {

    public static void main(String args[]){

        File csvFile = new File("Residential_Energy_Dataset_UK-2014-2020.csv");
        try {
            Scanner readCSV = new Scanner(csvFile);
            readCSV.hasNextLine();

            while()

        } catch (Exception e) {
            // TODO: handle exception
        }



    }

}
