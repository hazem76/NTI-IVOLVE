package com.example.calculator;

import static org.junit.Assert.*;

import org.junit.Test;

public class CalculatorTest {

    @Test
    public void testSum() {
        Calculator calc = new Calculator();
        assertEquals(5 + 3, calc.sum(5, 3), 0.0);
    }

    @Test
    public void testSubtract() {
        Calculator calc = new Calculator();
        assertEquals(10 - 4, calc.subtract(10, 4), 0.0);
    }

    @Test
    public void testMultiply() {
        Calculator calc = new Calculator();
        assertEquals(6 * 7, calc.multiply(6, 7), 0.0);
    }

    @Test
    public void testDivide() {
        Calculator calc = new Calculator();
        assertEquals(15 / 3.0, calc.divide(15, 3), 0.0);
    }

    @Test(expected = ArithmeticException.class)
    public void testDivideByZero() {
        Calculator calc = new Calculator();
        calc.divide(1, 0);
    }
}