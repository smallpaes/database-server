package edu.uob;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import edu.uob.InterpretException.*;

public enum ValueType {
    STRING,
    BOOLEAN,
    FLOAT,
    INTEGER,
    NULL;

    public static ValueType parseType(String value)  {
        boolean isTrueType = DBKeyWords.isTargetType(DBKeyWords.TRUE, value);
        boolean isFalseType = DBKeyWords.isTargetType(DBKeyWords.FALSE, value);
        if (isTrueType || isFalseType) { return BOOLEAN; }
        if (DBKeyWords.isTargetType(DBKeyWords.NULL, value)) { return NULL; }
        if (value.matches("^[-+]?\\d+$")) { return INTEGER; }
        if (value.matches("^[-+]?\\d+(\\.\\d+)?$")) { return FLOAT; }
        return STRING;
    }

    public static Boolean isComparableTypes(String firstValue, String secondValue) {
        boolean isSameType = parseType(firstValue).equals(parseType(secondValue));
        boolean isBothNumber = isNumber(firstValue) && isNumber(secondValue);
        return isSameType || isBothNumber;
    }

    public static Boolean isNumber(String value) {
        return switch (parseType(value)) {
            case FLOAT, INTEGER -> true;
            default -> false;
        };
    }

    public static String retrieveStringFromQuote(String value) throws StringWithNoQuoteException {
        Pattern pattern = Pattern.compile("^'(.*?)'$");
        Matcher matcher = pattern.matcher(value);
        if (!matcher.find()) { throw new StringWithNoQuoteException(value); }
        return matcher.group(1);
    }

    public static boolean compareRawToTargetValue(String comparator, String rawValue, String targetValue) {
        boolean isNullType = ValueType.parseType(targetValue).equals(ValueType.NULL);
        boolean isBooleanType = ValueType.parseType(targetValue).equals(ValueType.BOOLEAN);
        if (!isValueComparable(comparator, targetValue)) { return false; }
        switch (comparator.toLowerCase()) {
            case ">" -> { return Double.parseDouble(rawValue) > Double.parseDouble(targetValue); }
            case "<" -> { return Double.parseDouble(rawValue) < Double.parseDouble(targetValue); }
            case ">=" -> { return Double.parseDouble(rawValue) >= Double.parseDouble(targetValue); }
            case "<=" -> { return Double.parseDouble(rawValue) <= Double.parseDouble(targetValue); }
            case "==" -> {
                if (isNullType || isBooleanType) { return rawValue.equalsIgnoreCase(targetValue); }
                return rawValue.equals(targetValue);
            }
            case "!=" -> {
                if (isNullType || isBooleanType) { return !rawValue.equalsIgnoreCase(targetValue); }
                return !rawValue.equals(targetValue);
            }
            case "like" -> { return rawValue.contains(targetValue); }
            default -> { return false; }
        }
    }

    private static boolean isValueComparable(String comparator, String targetValue) {
        boolean isNumberType = ValueType.isNumber(targetValue);
        boolean isStringType = ValueType.parseType(targetValue).equals(ValueType.STRING);
        switch (comparator.toLowerCase()) {
            case ">", "<", ">=", "<=" -> { return isNumberType; }
            case "like" -> { return isStringType; }
            default -> { return true; }
        }
    }
}
