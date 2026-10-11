package Bai_5.Bai_4;

import java.math.BigDecimal;
import java.math.MathContext;

/** Xu ly mot yeu cau CALC, khong phu thuoc ket noi mang. */
final class Calculator {
    private Calculator() { }

    static String calculate(String request) {
        String[] parts = request.trim().split("\\s+");
        if (parts.length != 4 || !parts[0].equals("CALC")) {
            return "ERR INVALID_FORMAT";
        }
        String operator = parts[1];
        if (!operator.equals("+") && !operator.equals("-")
                && !operator.equals("*") && !operator.equals("/")) {
            return "ERR UNSUPPORTED_OPERATOR";
        }
        BigDecimal left;
        BigDecimal right;
        try {
            left = new BigDecimal(parts[2]);
            right = new BigDecimal(parts[3]);
        } catch (NumberFormatException e) {
            return "ERR INVALID_NUMBER";
        }
        try {
            BigDecimal result;
            switch (operator) {
                case "+": result = left.add(right); break;
                case "-": result = left.subtract(right); break;
                case "*": result = left.multiply(right); break;
                case "/":
                    if (right.signum() == 0) return "ERR DIVIDE_BY_ZERO";
                    result = left.divide(right, MathContext.DECIMAL128);
                    break;
                default: return "ERR UNSUPPORTED_OPERATOR";
            }
            result = result.stripTrailingZeros();
            // So thong thuong in 300 thay vi 3E+2; so rat lon dung dang mu.
            String value = result.scale() >= -100 && result.scale() <= 100
                    ? result.toPlainString() : result.toString();
            return "OK " + value;
        } catch (ArithmeticException e) {
            return "ERR ARITHMETIC_ERROR";
        }
    }
}
