import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EnergyPredictionMain {
    static List<String> uct_timestamp = new ArrayList<>();
    static List<Double> electricLoad = new ArrayList<>();
    static String header;
    static final int PopSize = 200;
    static final int MaxDepth = 7;
    static final int n = 7;
    static Random random = new Random();

    static final String[] FUNCTIONS = {
            "ADD",
            "SUB",
            "MUL",
            "DIV",
    };
    static final String[] TERMINALS = {
            "LOAD0",
            "LOAD1",
            "LOAD2",
            "LOAD3",
            "CONST"
    };

    public static void main(String args[]) {

        File csvFile = new File("Residential_Energy_Dataset_UK-2014-2020.csv");
        try {
            Scanner readCSV = new Scanner(csvFile);
            header = readCSV.nextLine();

            while (readCSV.hasNextLine()) {
                uct_timestamp.add(readCSV.nextLine().split(",")[0]);
                electricLoad.add(Double.parseDouble(readCSV.nextLine().split(",")[1]));
            }

            readCSV.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        // Generate the Intial Population
        List<Node> population = new ArrayList<>();
        for (int i = 0; i <= PopSize; i++) {
            population.add(makeTrees());
        }

    }

    public static Node makeTrees() {
        // Randomly making full or grow trees at random

        int depth = random.nextInt(6) + 2;

        if (random.nextDouble() < 0.5) {
            return makeFullTree(depth);
        } else {
            return makeGrowTree(depth);
        }
    }

    public static Node makeTerminalNode() {
        // Create a random terminal node

        String termNode = TERMINALS[random.nextInt(TERMINALS.length)];
        if (termNode.equals("CONST")) {
            double num = -5 + (10 * random.nextDouble());
            return new Node("term", "CONST", num);
        }
        return new Node("term", termNode);
    }

    public static Node makeFullTree(int depth) {
        // Full method, grows trees until they are full all leaves

        if (depth <= 1) {
            return makeTerminalNode();
        }

        String typeOfFun = FUNCTIONS[random.nextInt(FUNCTIONS.length)];
        List<Node> children = new ArrayList<>();
        if (typeOfFun.equals("SQRT")) {
            children.add(makeFullTree(depth - 1));
        } else {
            for (int i = 0; i < 2; i++) {
                children.add(makeFullTree(depth - 1));
            }
        }

        return new Node("fun", typeOfFun, children);

    }

    public static Node makeGrowTree(int depth) {
        // Grow method at any node, randomly select the terminal or function
        if (depth <= 1) {
            return makeTerminalNode();
        }

        if (random.nextInt(TERMINALS.length + FUNCTIONS.length) < TERMINALS.length) {
            return makeTerminalNode();
        }

        String typeOfFun = FUNCTIONS[random.nextInt(FUNCTIONS.length)];
        List<Node> children = new ArrayList<>();
        if (typeOfFun.equals("SQRT")) {
            children.add(makeFullTree(depth - 1));
        } else {
            for (int i = 0; i < 2; i++) {
                children.add(makeFullTree(depth - 1));
            }
        }

        return new Node("fun", typeOfFun, children);

    }

}
