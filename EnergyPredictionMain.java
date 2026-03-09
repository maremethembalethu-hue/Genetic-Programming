import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.lang.Math;

public class EnergyPredictionMain {
    static List<String> uct_timestamp = new ArrayList<>();
    static List<Double> electricLoad = new ArrayList<>();
    static String header;
    static final int PopSize = 20;
    static final int MaxDepth = 7;
    static final int n = 7;
    static final int MAXGEN = 1;
    static final double CROSSOVER = 0.8;
    static final double MUTATION = 0.2;
    static final int TOURNAMENT = 2;
    static final int MAXTREENODES = 5;
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

    public static void main(String[] args) {

        System.out.println("Running from: " + System.getProperty("user.dir"));
        File csvFile = new File("Residential_Energy_Dataset_UK- 2014-2020.csv");
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

        System.out.println("Pass the generate fitness case and also terminal set");
        generateFitnessCases();

        System.out.println("Generated the fitness case");
        buildTerminalSet(n);
        System.out.println("Buided the terminal set");
        // Generate the Intial Population
        List<Node> population = new ArrayList<>();
        for (int i = 0; i <= PopSize; i++) {
            population.add(makeTrees(0));
        }
        System.out.println("Generated the populations =========  " + population.toString());

        // Evaluate all
        List<Double> rawFitness = new ArrayList<>();
        for (Node prog : population) {
            rawFitness.add(evaRawFittness(prog));
        }
        System.out.println("Generated the Raw fitnesses");
        List<Double> fitnesses = normalizeFitness(rawFitness);
        System.out.println("Generated the normalize fitnesses");
        // Evolutionary loop
        for (int i = 0; i < MAXGEN; i++) {

            List<Node> newPop = new ArrayList<>();
            int bestIdx = findBestIndex(fitnesses);
            newPop.add(population.get(bestIdx).copy());
            System.out.println("Generated new POP  " + newPop);
            Node childA = new Node();
            Node chileB = new Node();
            Node childElse = new Node();
            while (newPop.size() < PopSize) {

                if (random.nextDouble() < CROSSOVER) {

                    Node parentA = tournamentSelect(population, fitnesses, TOURNAMENT);
                    Node parentB = tournamentSelect(population, fitnesses, TOURNAMENT);
                    System.out.println("Generated selected 1  " + parentA);
                    System.out.println("Generated selected 2  " + parentB);
                    Node[] kids = crossover(parentA, parentB);
                    System.out.println("Generated Kids " + kids[0].toString());
                    if (random.nextDouble() < MUTATION) {
                        childA = mutate(kids[0]);
                    }
                    if (random.nextDouble() < MUTATION) {
                        chileB = mutate(kids[1]);
                    }
                    newPop.add(limitTreeSize(childA));
                    newPop.add(limitTreeSize(chileB));

                    // System.out.println("Generated Added to the new POP " + newPop);
                } else {
                    Node parent = tournamentSelect(population, fitnesses, TOURNAMENT);

                    if (random.nextDouble() < MUTATION) {
                        childElse = mutate(parent);
                    } else {
                        childElse = parent.copy();
                        newPop.add(limitTreeSize(childElse));
                    }
                }

            }

            population = newPop;

            rawFitness.clear();
            fitnesses.clear();
            for (Node prog : population) {
                rawFitness.add(evaRawFittness(prog));
            }

            fitnesses = normalizeFitness(rawFitness);
        }

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

    public static Node tournamentSelect(List<Node> pup, List<Double> fit, int size) {

        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < pup.size(); i++)
            indices.add(i);

        Collections.shuffle(indices);

        int bestIdx = indices.get(0);

        for (int k = 1; k < size; k++) {
            int i = indices.get(k);
            if (fit.get(i) < fit.get(bestIdx))
                bestIdx = i;
        }

        return pup.get(bestIdx);
    }

    private static int findBestIndex(List<Double> arr) {
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
        // if (prog == null) {
        // return 0.0; // default value for missing nodes
        // }

        double result = 0;

        if ("term".equals(prog.getType())) {
            if ("CONST".equals(prog.getVal())) {
                return prog.getConstVal();
            } else {
                int lagNumber = Integer.parseInt(prog.getVal().replace("LOAD_", ""));
                return load[lagNumber];
            }
        }

        List<Double> childValues = new ArrayList<>();
        for (Node child : prog.getChildren()) {
            if (child != null) {
                childValues.add(evaluate(child, load));
            }
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
            TERMINALS[i] = "LOAD_" + (i);
        }

        // Last slot is always CONST
        TERMINALS[n] = "CONST";
    }

    public static Node makeTrees(int depth) {
        // Randomly making full or grow trees at random
        if (depth == 0) {
            depth = random.nextInt(MaxDepth - 2 + 1) + 2;
        }

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
            children.add(makeGrowTree(depth - 1));
        } else {
            for (int i = 0; i < 2; i++) {
                children.add(makeGrowTree(depth - 1));
            }
        }

        return new Node("fun", typeOfFun, children);

    }

    public static List<NodeList> getNodes(Node root) {
        List<NodeList> result = new ArrayList<>();
        walk(root, null, null, result);
        return result;
    }

    private static void walk(Node node, Node parent, Integer index, List<NodeList> result) {
        result.add(new NodeList(node, parent, index));

        for (int i = 0; i < node.children.size(); i++) {
            walk(node.children.get(i), node, i, result);
        }
    }

    public static Node[] crossover(Node parentA, Node parentB) {

        Node childA = parentA.copy();
        Node childB = parentB.copy();

        List<NodeList> nodesA = getNodes(childA);
        List<NodeList> nodesB = getNodes(childB);

        if (nodesA.size() > 1 && nodesB.size() > 1) {

            List<NodeList> internalA = new ArrayList<>();
            List<NodeList> internalB = new ArrayList<>();

            for (NodeList ni : nodesA) {
                if (!ni.node.getType().equals("term")) {
                    internalA.add(ni);
                }
            }

            for (NodeList ni : nodesB) {
                if (!ni.node.getType().equals("term")) {
                    internalB.add(ni);
                }
            }

            NodeList selectedA;
            NodeList selectedB;

            if (random.nextDouble() < 0.9 && !internalA.isEmpty() && !internalB.isEmpty()) {
                selectedA = internalA.get(random.nextInt(internalA.size()));
                selectedB = internalB.get(random.nextInt(internalB.size()));
            } else {
                selectedA = nodesA.get(random.nextInt(nodesA.size()));
                selectedB = nodesB.get(random.nextInt(nodesB.size()));
            }

            if (selectedA.parent == null) {
                childA = selectedB.node.copy();
            } else {
                selectedA.parent.setChildren(selectedA.index, selectedB.node.copy());
            }

            if (selectedB.parent == null) {
                childB = selectedA.node.copy();
            } else {
                selectedB.parent.setChildren(selectedB.index, selectedA.node.copy());
            }
        }

        return new Node[] { childA, childB };
    }

    public static Node mutate(Node prog) {

        Node mt = prog.copy();
        List<NodeList> nodes = getNodes(mt);
        Random rand = new Random();
        List<NodeList> inter_nodes = new ArrayList<>();

        for (NodeList n : nodes) {
            if (n.parent != null) {
                inter_nodes.add(n);
            }
        }

        if (!inter_nodes.isEmpty()) {
            NodeList chosen = inter_nodes.get(rand.nextInt(inter_nodes.size()));
            Node newTree = makeTrees(rand.nextInt(3) + 1);
            chosen.parent.setChildren(chosen.index, newTree);
        } else {
            mt = makeTrees(0);
        }
        return mt;
    }

    public static Node limitTreeSize(Node prg) {

        if (prg.size() > MAXTREENODES) {

            List<NodeList> nodes = getNodes(prg);

            for (int i = MAXTREENODES; i < nodes.size(); i++) {

                NodeList entry = nodes.get(i);

                if (entry != null && entry.parent != null) {
                    entry.parent.getChildren().remove(entry.index);
                }
            }
        }

        return prg;
    }
}
