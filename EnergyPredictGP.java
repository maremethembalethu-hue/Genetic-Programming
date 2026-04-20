import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.lang.Math;

public class EnergyPredictGP {
    static List<String> uct_timestamp = new ArrayList<>();
    static List<Double> electricLoad = new ArrayList<>();
    static String header;
    static final int PopSize = 100;
    static final int MaxDepth = 6;
    static final int n = 7;
    static final int MAXGEN = 25;
    static final double CROSSOVER = 0.8;
    static double MUTATION = 0.2; // default mutation rate
    static final int TOURNAMENT = 5;
    static final int MAXTREENODES = 63;
    static double[][] X_train;
    static double[][] X_test;
    static double[] y_train; // Contains the targets
    static double[] y_test;
    static int SEED = 200;
    static int NOOFRUN = 10;
    static Random random = new Random(SEED);
    static final double SUCCESS_THRESHOLD = 0.000272;
    static List<Integer> successGenerations = new ArrayList<>();

    // Start Time
    static long startTime = 0;
    // End Time
    static long endtime = 0;

    // Average of the best fitness
    static List<Double> bestFitnesses = new ArrayList<>();

    static final String[] FUNCTIONS = {
            "ADD",
            "SUB",
            "MUL",
            "DIV",
    };
    static String[] TERMINALS;

    public static void main(String[] args) {

        File csvFile = new File("Residential_Energy_Dataset_UK- 2014-2020.csv");
        try {
            Scanner readCSV = new Scanner(csvFile);
            header = readCSV.nextLine();
            while (readCSV.hasNextLine()) {
                String[] parts = readCSV.nextLine().split(",");
                uct_timestamp.add(parts[0]);
                electricLoad.add(Double.parseDouble(parts[1]));
            }
            readCSV.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        generateFitnessCases();
        buildTerminalSet(n);
        System.out.println("Genetic Programming for Residential Energy Prediction:");
        tick();

        List<Double> allBestTrainErrors = new ArrayList<>();
        List<Double> allBestTestErrors = new ArrayList<>();
        List<Double> allRunAvgMSE = new ArrayList<>();
        List<Long> allRunTimes = new ArrayList<>();
        List<String> allBestEquations = new ArrayList<>();
        for (int j = 0; j < NOOFRUN; j++) {

            random = new Random(SEED + j);
            System.out.println("===== RUN " + j
                    + " (seed " + (SEED + j) + ") =====");

            Node bestTreeThisRun = null;
            double bestMSEThisRun = Double.MAX_VALUE;

            // Sum of best of generation MSE values, used to compute per-run average
            double sumGenBestMSE = 0.0;

            List<Node> population = new ArrayList<>();
            for (int i = 0; i < PopSize; i++) {
                population.add(makeTrees());
            }

            List<Double> arrMseFitness = new ArrayList<>();
            for (Node prog : population) {
                arrMseFitness.add(mseFitness(prog, y_train, X_train));
            }

            long runStart = System.currentTimeMillis();

            int successGen = -1;
            for (int gen = 0; gen < MAXGEN; gen++) {

                int bestIdx = findBestIndex(arrMseFitness);
                double bestGenMSE = arrMseFitness.get(bestIdx);
                if (successGen == -1 && bestGenMSE < SUCCESS_THRESHOLD) {
                    successGen = gen; // record the generation we first succeeded
                }
                sumGenBestMSE += bestGenMSE;

                if (bestGenMSE < bestMSEThisRun) {
                    bestMSEThisRun = bestGenMSE;
                    bestTreeThisRun = population.get(bestIdx).copy();
                }

                List<Node> newPop = new ArrayList<>();
                newPop.add(population.get(bestIdx).copy());

                int remainingSize = PopSize - 1;
                int mutationSize = (int) Math.round(remainingSize * MUTATION);
                int crossoverSize = remainingSize - mutationSize;

                int crossoverUsed = 0;
                int mutationUsed = 0;
                while (newPop.size() < PopSize) {

                    if (crossoverUsed < crossoverSize) {
                        Node parentA = tournamentSelect(population, arrMseFitness, TOURNAMENT);
                        Node parentB = tournamentSelect(population, arrMseFitness, TOURNAMENT);
                        Node[] kids = crossover(parentA, parentB);

                        Node childA = kids[0];
                        // Making complex tree if crossover produced a terminal
                        if ("term".equals(childA.getType()))
                            childA = mutate(childA);

                        Node childB = kids[1];
                        if ("term".equals(childB.getType()))
                            childB = mutate(childB);

                        if (newPop.size() < PopSize) {
                            newPop.add(limitTreeSize(childA));
                            crossoverUsed++;
                        }
                        if (newPop.size() < PopSize) {
                            newPop.add(limitTreeSize(childB));
                            crossoverUsed++;
                        }
                    } else {
                        Node parent = tournamentSelect(population, arrMseFitness, TOURNAMENT);
                        Node child = parent.copy();

                        // Making complex tree if reproduction would copy a terminal
                        if ("term".equals(parent.getType()))
                            child = mutate(child);

                        newPop.add(limitTreeSize(child));
                        mutationUsed++;
                    }
                }

                population = newPop;
                arrMseFitness.clear();
                for (Node prog : population) {
                    arrMseFitness.add(mseFitness(prog, y_train, X_train));
                }
            }
            if (successGen == -1) {
                successGenerations.add(MAXGEN + 1);
            } else {
                successGenerations.add(successGen);
            }
            long runTime = System.currentTimeMillis() - runStart;

            double runAvgMSE = sumGenBestMSE / MAXGEN;
            double testError = mseFitness(bestTreeThisRun, y_test, X_test);

            allBestTrainErrors.add(bestMSEThisRun);
            allBestTestErrors.add(testError);
            allRunAvgMSE.add(runAvgMSE);
            allRunTimes.add(runTime);
            allBestEquations.add(bestTreeThisRun.toString());
            bestFitnesses.add(bestMSEThisRun);

            // System.out.println("Run " + j + " (seed " + (SEED + j) + ")");
            System.out.println("  Best train MSE      : " + bestMSEThisRun);
            System.out.println("  Best test MSE       : " + testError);
            System.out.println("  Avg best-gen MSE    : " + runAvgMSE);
            System.out.println("  Run time (ms)       : " + runTime);
            System.out.println("  Best equation       : " + bestTreeThisRun.toString());
            System.out.println();
        }

        tock();

        double sumTrain = 0, sumTest = 0, sumAvg = 0;
        for (int i = 0; i < NOOFRUN; i++) {
            sumTrain += allBestTrainErrors.get(i);
            sumTest += allBestTestErrors.get(i);
            sumAvg += allRunAvgMSE.get(i);
        }
        double meanTrain = sumTrain / NOOFRUN;
        double meanTest = sumTest / NOOFRUN;
        double meanAvg = sumAvg / NOOFRUN;

        double varTrain = 0, varTest = 0;
        for (int i = 0; i < NOOFRUN; i++) {
            varTrain += Math.pow(allBestTrainErrors.get(i) - meanTrain, 2);
            varTest += Math.pow(allBestTestErrors.get(i) - meanTest, 2);
        }
        double stdTrain = Math.sqrt(varTrain / NOOFRUN);
        double stdTest = Math.sqrt(varTest / NOOFRUN);

        int bestRunIdx = findBestIndex(allBestTrainErrors);
        long totalTimeMs = endtime - startTime;

        System.out.println(" Results Across " + NOOFRUN + " Runs");
        System.out.println("Train MSE — mean: " + meanTrain + "  std: " + stdTrain);
        System.out.println("Test  MSE — mean: " + meanTest + "  std: " + stdTest);
        System.out.println("Avg best-gen MSE across runs: " + meanAvg);
        System.out.println("Best run: " + bestRunIdx
                + "  train MSE: " + allBestTrainErrors.get(bestRunIdx)
                + "  equation: " + allBestEquations.get(bestRunIdx));
        System.out.println("Total wall time (ms): " + totalTimeMs);
        computeEffort(successGenerations, PopSize);
    }

    public static void computeEffort(List<Integer> successGenerations, int popSize) {

        int targetGen = 20;
        int successes = 0;

        // Count successes using a simple loop
        for (int g : successGenerations) {
            if (g >= 0 && g <= targetGen) {
                successes++;
            }
        }

        double p = (double) successes / successGenerations.size();

        System.out.println("\nEffort Prediction");
        System.out.println("Successes by gen " + targetGen + ": " + successes + "/" + successGenerations.size());
        System.out.println("Success probability p = " + p);

        if (p > 0) {
            double q = 1 - p;
            int runsNeeded = (int) Math.ceil(Math.log(0.01) / Math.log(q));
            long effort = (long) runsNeeded * popSize * (targetGen + 1);

            System.out.println("Runs needed for success: " + runsNeeded);
            System.out.println("Computational effort: " + effort + " evaluations");
        } else {
            System.out.println("No successes yet.");
        }
    }

    // Start time
    private static void tick() {
        startTime = System.currentTimeMillis();
    }

    // End time
    private static void tock() {
        endtime = System.currentTimeMillis();
    }

    public static void generateFitnessCases() {

        // Total fitness cases = 201604 - n
        // int totalRows = electricLoad.size() - n;
        int totalRows = 101604 - n;
        double[][] X = new double[totalRows][n];
        double[] y = new double[totalRows];

        for (int i = 0; i < totalRows; i++) {
            // Fill load columns
            for (int load = 0; load < n; load++) {

                X[i][load] = electricLoad.get(i + load);
            }
            // value right after the loads
            y[i] = electricLoad.get(i + n);
        }
        // Train/Test split (80/20)
        int splitIndex = (int) (totalRows * 0.8);

        // Training data
        X_train = new double[splitIndex][n];
        y_train = new double[splitIndex];

        // Test data
        X_test = new double[totalRows - splitIndex][n];
        y_test = new double[totalRows - splitIndex];

        for (int i = 0; i < splitIndex; i++) {
            X_train[i] = X[i];
            y_train[i] = y[i];
        }

        for (int i = splitIndex; i < totalRows; i++) {
            X_test[i - splitIndex] = X[i];
            y_test[i - splitIndex] = y[i];
        }

    }

    public static Node tournamentSelect(List<Node> pup, List<Double> fit, int size) {
        // Get all the index of the population
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < pup.size(); i++)
            indices.add(i);

        Collections.shuffle(indices);
        // Get the best index based on the shuffled index with the given size
        int bestIdx = indices.get(0);
        for (int k = 1; k < size; k++) {
            int i = indices.get(k);
            if (fit.get(i) < fit.get(bestIdx))
                bestIdx = i;
        }

        return pup.get(bestIdx);
    }

    private static int findBestIndex(List<Double> arr) {
        // Finding the best index inside the array
        int maxIndex = 0;
        double bestIndex = arr.get(0);

        for (int i = 1; i < arr.size(); i++) {

            if (arr.get(i) < bestIndex) {
                maxIndex = i;
                bestIndex = arr.get(i);
            }
        }
        return maxIndex;
    }

    public static double mseFitness(Node prog, double[] targetArr, double[][] datasetArr) {
        double sumSqError = 0.0;
        int cases = targetArr.length;

        for (int i = 0; i < cases; i++) {
            double prediction = evaluate(prog, datasetArr[i]);

            double error = prediction - targetArr[i];
            sumSqError += error * error; // squared not absolute
        }

        return sumSqError / cases; // MSE
    }

    public static double evaluate(Node prog, double[] load) {
        double result = 0;
        // If it's terminal, get the terminal values
        if ("term".equals(prog.getType())) {
            if ("CONST".equals(prog.getVal())) {
                return prog.getConstVal();
            } else {
                int loadNumber = Integer.parseInt(prog.getVal().replace("LOAD_", ""));
                if (loadNumber >= load.length)
                    return 0.0;
                return load[loadNumber];
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
                double b = childValues.get(1);
                // PROTECTED division
                if (Math.abs(b) > 0.001) {
                    result = childValues.get(0) / b;
                } else {
                    result = 1.0;
                }
                break;
            default:
                result = 0.0;
        }

        // Clamp runaway values
        if (Double.isNaN(result) || Double.isInfinite(result) || Math.abs(result) > 10) {
            return 0.0;
        }

        return result;
    }

    public static void buildTerminalSet(int loadCount) {
        int n = loadCount;

        // n load terminals + 1 CONST slot
        TERMINALS = new String[n + 1];

        // Fill load
        for (int i = 0; i < n; i++) {
            TERMINALS[i] = "LOAD_" + (i);
        }

        // Last slot is always CONST
        TERMINALS[n] = "CONST";
    }

    // Ramped half-and-half randomly choose FULL or GROW, at a random depth.
    public static Node makeTrees() {
        // randomize between 2 and MaxDepth

        int depth = 2 + random.nextInt(MaxDepth - 1);

        if (random.nextDouble() < 0.5) {
            return makeFullTree(depth);
        } else {
            return makeGrowTree(depth, true);
        }
    }

    // Terminal node default constant
    public static Node makeTerminalNode() {
        String termNode = TERMINALS[random.nextInt(TERMINALS.length)];
        if (termNode.equals("CONST")) {
            double num = -5.0 + (10.0 * random.nextDouble());
            return new Node("term", "CONST", num);
        }
        return new Node("term", termNode); // explicit safe default
    }

    public static Node makeFullTree(int depth) {

        // If maximum depth reached, create a terminal node
        if (depth <= 1) {
            return makeTerminalNode();
        }

        // Randomly choose a function
        String typeOfFun = FUNCTIONS[random.nextInt(FUNCTIONS.length)];

        List<Node> children = new ArrayList<>();

        // All current functions are binary SQRT if arity is 1
        if (typeOfFun.equals("SQRT")) {
            children.add(makeFullTree(depth - 1));
        } else {
            children.add(makeFullTree(depth - 1));
            children.add(makeFullTree(depth - 1));
        }

        return new Node("fun", typeOfFun, children);
    }

    public static Node makeGrowTree(int depth, Boolean isRoot) {
        if (depth <= 0) { // base case
            return makeTerminalNode();
        }

        // 30% chance of terminal
        if (!isRoot && random.nextDouble() < 0.3) {
            return makeTerminalNode();
        }

        String typeOfFun = FUNCTIONS[random.nextInt(FUNCTIONS.length)];
        List<Node> children = new ArrayList<>();
        if (typeOfFun.equals("SQRT")) {
            children.add(makeGrowTree(depth - 1, false));
        } else {
            for (int i = 0; i < 2; i++) {
                children.add(makeGrowTree(depth - 1, false));
            }
        }
        return new Node("fun", typeOfFun, children);
    }

    // Helpes to get the nodes, nodes and it's references
    public static List<NodePt> getNodes(Node root) {
        List<NodePt> nodes = new ArrayList<>();
        if (root != null) {
            addAllNodes(root, null, 0, nodes); // recursive collection
        }
        return nodes;
    }

    // recursive helper
    private static void addAllNodes(Node node, Node parent, int index, List<NodePt> nodes) {
        nodes.add(new NodePt(node, parent, index));
        for (int i = 0; i < node.children.size(); i++) {
            addAllNodes(node.children.get(i), node, i, nodes);
        }
    }

    public static Node[] crossover(Node parentA, Node parentB) {
        Node childA = parentA.copy();
        Node childB = parentB.copy();

        List<NodePt> nodesA = getNodes(childA);
        List<NodePt> nodesB = getNodes(childB);

        if (!nodesA.isEmpty() && !nodesB.isEmpty()) {
            // pick any node
            NodePt pickA = nodesA.get(random.nextInt(nodesA.size()));
            NodePt pickB = nodesB.get(random.nextInt(nodesB.size()));

            // 90% chance to swap real subtrees instead of terminals
            if (random.nextDouble() < 0.9 && !pickA.node.getType().equals("term")) {
                Node branchA = pickA.node.copy();
                Node branchB = pickB.node.copy();
                // Swap subtress
                if (pickA.parent == null)
                    childA = branchB;
                else
                    pickA.parent.setChildren(pickA.index, branchB);

                if (pickB.parent == null)
                    childB = branchA;
                else
                    pickB.parent.setChildren(pickB.index, branchA);
            }
        }

        return new Node[] { childA, childB };
    }

    public static Node mutate(Node prog) {
        // replace a random subtree with a new random subtree.
        Node mt = prog.copy();
        List<NodePt> allNodes = getNodes(mt);
        List<NodePt> choosens = new ArrayList<>();
        for (NodePt r : allNodes) {
            if (r.parent != null)
                choosens.add(r); // skip root
        }
        if (!choosens.isEmpty()) {
            NodePt chosen = choosens.get(random.nextInt(choosens.size()));
            // replace with fresh subtree
            chosen.parent.setChildren(chosen.index, makeGrowTree(2 + random.nextInt(2), true));
        } else {
            mt = makeTrees();
        }
        return mt;
    }

    public static Node limitTreeSize(Node tree) {
        // Limiting tress, for bloated trees
        while (tree.size() > MAXTREENODES) {
            List<NodePt> all = getNodes(tree);
            List<NodePt> internals = new ArrayList<>();
            // get internal nodes
            for (NodePt r : all) {
                if (!r.node.getType().equals("term") && r.parent != null)
                    internals.add(r);
            }
            if (internals.isEmpty())
                break;
            // radomly selected node replace with terminal node
            NodePt victim = internals.get(random.nextInt(internals.size()));
            victim.parent.setChildren(victim.index, makeTerminalNode());
        }
        return tree;
    }
}
