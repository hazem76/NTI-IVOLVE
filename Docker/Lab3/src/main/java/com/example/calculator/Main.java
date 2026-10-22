package com.example.calculator;

public class Main {
    public static void main(String[] args) {
        Calculator calc = new Calculator();
        System.out.println("5 + 3 = " + calc.sum(5, 3));
        System.out.println("10 - 4 = " + calc.subtract(10, 4));
        System.out.println("6 * 7 = " + calc.multiply(6, 7));
        System.out.println("15 / 3 = " + calc.divide(15, 3));
    }
}