# Lab 1: Building and Packaging Java Application with Maven

## Objective
Build and package a Java calculator application using Maven, run unit tests, and verify the application works.

## Prerequisites
- Java JDK 17 or higher installed
- Apache Maven 3.9.6 or higher installed
- Git

## Setup

### 1. Clone the Repository
```bash
git clone https://github.com/Ibrahim-Adel15/CalculatorMavenApp.git
cd CalculatorMavenApp
```

### 2. Verify Java Installation
```bash
java -version
# Expected output: OpenJDK version "17.x.x" ... 

mvn -version
# Expected output: Apache Maven 3.9.6 ...
```

### 3. Run Unit Tests
```bash
mvn test
```
**Results:** All tests should pass. The calculator module tests verify basic arithmetic operations (add, subtract, multiply, divide).

### 4. Build the Application
```bash
mvn clean package
```
**Results:** Build succeeds, generating `target/calculator.jar`.

### 5. Run the Application
```bash
java -jar target/calculator.jar
```
**Expected Output:**
```
Calculator> 5 + 3
8
Calculator> 10 - 4
6
Calculator> 6 * 7
42
Calculator> 15 / 3
5
Calculator> exit
```

## Verification Summary
- ✅ Java and Maven installed and configured
- ✅ Unit tests passed
- ✅ JAR artifact built at `target/calculator.jar`
- ✅ Application runs correctly with expected output

## File Structure
```
CalculatorMavenApp/
├─ src/
│  ├─ main/
│  │  └─ java/
│  │     └─ com.example.calculator/
│  │        ├─ Calculator.java
│  │        └─ Main.java
│  └─ test/
│     └─ java/
│        └─ com.example.calculator/
│           └─ CalculatorTest.java
├─ pom.xml
├─ target/
│   └─ calculator.jar
└─ README.md (this file)
```