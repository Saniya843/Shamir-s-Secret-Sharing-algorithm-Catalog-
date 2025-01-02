package com.shamir;

// Main.java
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.FileReader;
import java.io.FileWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class Main {
    // Point class to store x,y coordinates
    static class Point {
        int x;
        BigInteger y;

        Point(int x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
    }

    // Helper function to convert from any base to decimal
    private static BigInteger convertFromBase(String number, int base) {
        if (base <= 10) {
            return new BigInteger(number, base);
        }

        // For bases > 10, handle alphabetic digits manually
        String digits = "0123456789abcdef";
        BigInteger decimal = BigInteger.ZERO;
        number = number.toLowerCase();

        for (int i = 0; i < number.length(); i++) {
            int digit = digits.indexOf(number.charAt(i));
            if (digit == -1 || digit >= base) {
                throw new IllegalArgumentException("Invalid digit for given base");
            }
            decimal = decimal.multiply(BigInteger.valueOf(base)).add(BigInteger.valueOf(digit));
        }

        return decimal;
    }

    // Lagrange interpolation to find the constant term
    private static BigInteger findConstantTerm(List<Point> points) {
        BigInteger result = BigInteger.ZERO;
        int n = points.size();

        // For each point, calculate its contribution to the constant term
        for (int i = 0; i < n; i++) {
            BigInteger term = points.get(i).y;
            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            // Calculate the Lagrange basis polynomial
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    numerator = numerator.multiply(BigInteger.valueOf(-points.get(j).x));
                    denominator = denominator.multiply(
                            BigInteger.valueOf(points.get(i).x - points.get(j).x)
                    );
                }
            }

            // Add this point's contribution
            result = result.add(term.multiply(numerator).divide(denominator));
        }

        return result;
    }

    // Process input JSON and find secret
    private static BigInteger findSecret(String jsonContent) throws Exception {
        JSONParser parser = new JSONParser();
        JSONObject data = (JSONObject) parser.parse(jsonContent);

        // Extract k (minimum points needed)
        JSONObject keys = (JSONObject) data.get("keys");
        long k = (Long) keys.get("k");
        long n = (Long) keys.get("n");

        // Convert points and store them
        List<Point> points = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            JSONObject pointData = (JSONObject) data.get(String.valueOf(i));
            if (pointData != null) {
                int base = Integer.parseInt((String) pointData.get("base"));
                String value = (String) pointData.get("value");
                points.add(new Point(i, convertFromBase(value, base)));
            }
        }

        // We only need k points to find the secret
        return findConstantTerm(points.subList(0, (int) k));
    }

    // Create test case files and process them
    private static void createTestCase1() throws Exception {
        String testCase1 = """
        {
            "keys": {
                "n": 4,
                "k": 3
            },
            "1": {
                "base": "10",
                "value": "4"
            },
            "2": {
                "base": "2",
                "value": "111"
            },
            "3": {
                "base": "10",
                "value": "12"
            },
            "6": {
                "base": "4",
                "value": "213"
            }
        }""";

        try (FileWriter file = new FileWriter("testcase1.json")) {
            file.write(testCase1);
        }
    }

    private static void createTestCase2() throws Exception {
        String testCase2 = """
        {
            "keys": {
                "n": 10,
                "k": 7
            },
            "1": {
                "base": "6",
                "value": "13444211440455345511"
            },
            "2": {
                "base": "15",
                "value": "aed7015a346d63"
            },
            "3": {
                "base": "15",
                "value": "6aeeb69631c227c"
            },
            "4": {
                "base": "16",
                "value": "e1b5e05623d881f"
            },
            "5": {
                "base": "8",
                "value": "316034514573652620673"
            },
            "6": {
                "base": "3",
                "value": "2122212201122002221120200210011020220200"
            },
            "7": {
                "base": "3",
                "value": "20120221122211000100210021102001201112121"
            },
            "8": {
                "base": "6",
                "value": "20220554335330240002224253"
            },
            "9": {
                "base": "12",
                "value": "45153788322a1255483"
            },
            "10": {
                "base": "7",
                "value": "1101613130313526312514143"
            }
        }""";

        try (FileWriter file = new FileWriter("testcase2.json")) {
            file.write(testCase2);
        }
    }

    public static void main(String[] args) {
        try {
            // Create test case files
            createTestCase1();
            createTestCase2();

            // Process both test cases
            String content1 = new String(java.nio.file.Files.readAllBytes(
                    java.nio.file.Paths.get("testcase1.json")));
            String content2 = new String(java.nio.file.Files.readAllBytes(
                    java.nio.file.Paths.get("testcase2.json")));

            System.out.println("Secret for Test Case 1: " + findSecret(content1));
            System.out.println("Secret for Test Case 2: " + findSecret(content2));

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}