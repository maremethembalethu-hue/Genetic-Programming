import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.lang.Math;

public class EnergyPredictionMain {
    static List<String> uct_timestamp = new ArrayList<>();
    static List<Double> electricLoad = new ArrayList<>();
    static String header;
    static final int PopSize = 200;
    static final int MaxDepth = 7;
    static final int n = 7;
    static double[][] X_train;
    static double[][] X_test;
    static double[] y_train; // Contains the targets
    static double[] y_test;
    static Random random = new Random();

    static final String[] FUNCTIONS = {
            "ADD",
            "SUB",
            "MUL",
            "DIV",
    };
    static String[] TERMINALS;

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

        generateFitnessCases();
        buildTerminalSet(n);

        // Generate the Intial Population
        List<Node> population = new ArrayList<>();
        for (int i = 0; i <= PopSize; i++) {
            population.add(makeTrees());
        }

        // Evaluate all
        List<Double> rawFitness = new ArrayList<>();
        for (Node prog : population) {
            rawFitness.add(evaRawFittness(prog));
        }

        List<Double> fitnesses = normalizeFitness(rawFitness);

    }

    public static List<Double> normalizeFitness(List<Double> fitness) {

        double totalAjustedFitness = 0;
        List<Double> adj = new ArrayList<>();
        List<Double> normalize = new ArrayList<>();
        for (Double fit : fitness) {
            double adjusted = 1 / (1 + fit);
            adj.add(adjusted);
            totalAjustedFitness += adjusted;
        }

        for (Double norm : adj) {

            normalize.add(norm / totalAjustedFitness);
        }

        return normalize;

    }

    public static void generateFitnessCases() {

        // Total fitness cases = 201604 - n
        int totalRows = electricLoad.size() - n;

        double[][] X = new double[totalRows][n];
        double[] y = new double[totalRows];

        for (int i = 0; i < totalRows; i++) {
            // Fill lag columns
            for (int lag = 0; lag < n; lag++) {

                X[i][lag] = electricLoad.get(i + lag);
            }
            // Target = value right after the lags
            y[i] = electricLoad.get(i + n);
        }
        // ── STEP 3: Train/Test split (80/20) ──
        int splitIndex = (int) (totalRows * 0.8);

        // Training data
        X_train = new double[splitIndex][n];
        y_train = new double[splitIndex];

        // Test data
        X_test = new double[totalRows - splitIndex][n];
        y_test = new double[totalRows - splitIndex];

        // Copy rows into train
        for (int i = 0; i < splitIndex; i++) {
            X_train[i] = X[i];
            y_train[i] = y[i];
        }

        // Copy rows into test
        for (int i = splitIndex; i < totalRows; i++) {
            X_test[i - splitIndex] = X[i];
            y_test[i - splitIndex] = y[i];
        }

    }

    private static int findIndex(List<Double> arr) {
        int maxIndex = 0;

        for (int i = 1; i < arr.size(); i++) {

            if (arr.get(i) > maxIndex) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    public static double evaRawFittness(Node prog) {
        double rawFit = 0;

        for (int i = 0; i < y_test.length; i++) {

            rawFit += Math.abs(y_test[i] - evaluate(prog, X_test[i]));

        }
        return rawFit;
    }

    public static double evaluate(Node prog, double[] load) {
        // Interpreter for our tree pragram.
        // Walks the tree Recursively, evaluate the children

        double result = 0;
        if (prog.getVal().equals("term")) {

            // Is it a constant (ERC)?
            if (prog.getVal().equals("CONST")) {
                return prog.getConstVal();
            } else {
                int lagNumber = Integer.parseInt(
                        prog.getVal().replace("LOAD_", ""));

                return load[lagNumber];
            }
        }

        List<Double> childValues = new ArrayList<>();
        for (Node child : prog.getChildren()) {
            childValues.add(evaluate(child, load));
        }

        String fn = prog.getVal();

        switch (fn) {
            case "ADD":
                result = childValues.get(0) + childValues.get(1);
                break;
            case "SUB":
                result = childValues.get(0) - childValues.get(1);
                break;
            case "MUL":
                result = childValues.get(0) * childValues.get(1);
                break;
            case "DIV":
                result = childValues.get(0) / childValues.get(1);
                break;
            default:
                result = 0.0;
                break;
        }
        return result;

    }

    public static void buildTerminalSet(int lagCount) {
        int n = lagCount;

        // n lag terminals + 1 CONST slot
        TERMINALS = new String[n + 1];

        // Fill lag terminals dynamically
        for (int i = 0; i < n; i++) {
            TERMINALS[i] = "LOAD_" + (i + 1);
            // lag_1, lag_2, lag_3 ... lag_n
        }

        // Last slot is always CONST
        TERMINALS[n] = "CONST";
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
