# Lab 2: Building and Packaging Application with Gradle

## Objective
Build and package a Java calculator application using Gradle, run unit tests, and verify the application works.

## Prerequisites
- Java JDK 17 or higher installed
- Apache Gradle 8.x or higher installed
- Git

## Setup

### 1. Clone the Repository
```bash
git clone https://github.com/Ibrahim-Adel15/CalculatorGradleApp.git
cd CalculatorGradleApp
```

### 2. Verify Java Installation
```bash
java -version
# Expected output: OpenJDK version "17.x.x" ... 

gradle --version
# Expected output: Gradle 8.x.x
```

### 3. Run Unit Tests
```bash
gradle test
```
**Results:** All tests should pass. The calculator module tests verify basic arithmetic operations.

### 4. Build the Application
```bash
gradle clean build
```
**Results:** Build succeeds, generating `build/libs/calculator.jar`.

### 5. Run the Application
```bash
gradle run
# Or: java -jar build/libs/calculator.jar
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
- ✅ Java and Gradle installed and configured
- ✅ Unit tests passed
- ✅ JAR artifact built at `build/libs/calculator.jar`
- ✅ Application runs correctly with expected output

## File Structure
```
CalculatorGradleApp/
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
├─ build.gradle
├─ settings.gradle
├─ gradle/
│   └─ wrapper/
│      └─ gradle-wrapper.properties
├─ build/
│   └─libs/
│      └─ calculator.jar
└─ README.md (this file)
```