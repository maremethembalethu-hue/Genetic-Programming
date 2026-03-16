# Genetic Programming for Electricity Load Prediction

## Introduction

This project implements **Genetic Programming (GP)** in Java to perform **electricity load prediction** using historical residential energy consumption data. The system evolves mathematical expression trees that predict the next electricity load value based on previously observed readings.

The model performs **symbolic regression**, where GP searches for mathematical expressions that minimise prediction error. The fitness of each individual program is evaluated using **Mean Absolute Error (MAE)**.

The dataset used contains electricity load measurements collected from UK residential households between **2014 and 2020** at **15-minute intervals**.


## Requirements

Before running the program, ensure the following are installed:

* **Java Development Kit (JDK) 11 or newer**
* At least **512 MB of available memory**
* A terminal environment (Command Prompt, PowerShell, Bash, or Linux terminal)

You can verify Java installation by running:
java -version
and
javac -version

## Project Structure

Assignment 1/
    EnergyPredictGP.java
    Node.java
    NodePt.java
    Residential_Energy_Dataset_UK- 2014-2020.csv
    Makefile
    run.bat
    README.md


The dataset file must remain in the same directory as the Java files when compiling and running the program.

## How to Run (Linux / macOS)

1. Open a terminal.
2. Navigate to the project directory.

Compile the program:
make compile

Run the program:
make run

Run and compile programs:
make all

Clean the .class:
make clean


If additional memory is needed:

## How to Run (Windows)

### Using Command Prompt

1. Open **Command Prompt**.
2. Navigate to the project directory.

Compile:
javac EnergyPredictGP.java Node.java NodeList.java

Run:
java EnergyPredictGP

Run and compile:
.\run or .\run.bat

### Using PowerShell

If using **PowerShell**, the commands are the same:

Compile:
javac EnergyPredictGP.java Node.java NodeList.java


Run:
java EnergyPredictGP

Run and compile:
.\run or .\run.bat

## Expected Output

During execution, the program prints:

* Generation progress
* Best fitness values
* Results for each run
* A final summary of prediction performance
