# Genetic Programming for Electricity Load Prediction

## Introduction

This project implements **Genetic Programming (GP)** and **Structural-Based Genetic Programming (SBGP)** in Java to perform **electricity load prediction** using historical residential energy consumption data.

The system evolves mathematical expression trees that predict future electricity load values based on previously observed readings.

The model performs **symbolic regression**, where GP searches for mathematical expressions that minimise prediction error. The fitness of each individual program is evaluated using **Mean Absolute Error (MAE)**.

The dataset contains electricity load measurements collected from UK residential households between **2014 and 2020** at **15-minute intervals**.

---

## Requirements

Before running the program, ensure the following are installed:

- **Java Development Kit (JDK) 11 or newer**
- At least **512 MB of available memory**
- A terminal environment (Command Prompt, PowerShell, Bash, or Linux terminal)

Verify installation:

```
java -version
javac -version
```

---

## Project Structure

```
Assignment 1/

    EnergyPredictGP.java              (Standard GP)
    EnergyPredictSBGP.java            (Structural-Based GP)
    Node.java
    NodePt.java
    Residential_Energy_Dataset_UK-2014-2020.csv
    Makefile
    run.bat
    README.md
```

> The dataset file must remain in the same directory as the Java files.

---

## How to Run (Linux / macOS)

### Compile all files

```
make compile
```

### Run Standard GP

```
make run-gp
```

### Run Structural-Based GP (SBGP)

```
make run-sbgp
```

### Compile and Run GP

```
make all-gp
```

### Compile and Run SBGP

```
make all-sbgp
```

### Clean compiled files

```
make clean
```

---

## How to Run (Windows)

### Using run.bat (Recommended)

```
.\run.bat
```

You will be prompted to choose:
- `1` → Standard GP
- `2` → Structural-Based GP (SBGP)

### Manual Compilation

```
javac EnergyPredictGP.java EnergyPredictSBGP.java Node.java NodePt.java
```

Run GP:

```
java EnergyPredictGP
```

Run SBGP:

```
java EnergyPredictSBGP
```

---

## Parameter Configuration

All parameters can be modified directly in:

- `EnergyPredictGP.java`
- `EnergyPredictSBGP.java`

### Key Parameters

 Parameter | Description |

 `PopSize` = Population size 
 `MaxDepth` = Maximum tree depth 
 `MAXGEN` = Number of generations 
 `CROSSOVER` = Crossover rate
 `MUTATION` = Mutation rate 
`TOURNAMENT` = Tournament size 

---

## Dataset Size Control (IMPORTANT)

The parameter controlling how much data is loaded is:

```java
static int numForDataset = 0;
```

### Dataset Modes

| Value | Description |
 `0` = Loads half of the dataset 
 `1` = Loads 10,000 rows 
 `2+` = Loads the full dataset 

### Performance and Runtime Considerations

The size of the dataset directly impacts execution time:

- **Larger datasets** = more accurate results but slower execution
- **Smaller datasets** = faster execution for testing and debugging

**Recommendation:**
- Use `numForDataset = 0` or `1` during development and testing
- Use `numForDataset >= 2` for final evaluation

### Purpose of Dataset Control

The `numForDataset` parameter was introduced to address long runtime issues. By allowing partial dataset loading, the system enables faster experimentation while maintaining the ability to scale to the full dataset when required.

---

## If No Improvement Is Observed

If the algorithm does not improve over generations, consider adjusting the following parameters:

- Mutation rate (`MUTATION`)
- Crossover rate (`CROSSOVER`)
- Population size (`PopSize`)
- Maximum tree depth (`MaxDepth`)
- Tournament size (`TOURNAMENT`)
- Dataset size (`numForDataset`)

Reducing the dataset size can help to:

- Identify issues faster
- Improve debugging speed
- Reduce long execution times

---

## Expected Output

During execution, the program prints:

- Generation progress
- Best fitness values
- Results for each run
- Final summary of prediction performance