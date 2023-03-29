package edu.uob;

public enum DBKeyWords {
    DATABASE,
    TABLE,
    INTO,
    VALUES,
    FROM,
    WHERE,
    AND,
    OR,
    SET,
    ON,
    TRUE,
    FALSE,
    NULL,
    LIKE,
    ADD,
    DROP;

    public static DBKeyWords parse(String token) throws ParseException {
        return switch (token.toUpperCase()) {
            case "DATABASE" -> DATABASE;
            case "TABLE" -> TABLE;
            case "INTO" -> INTO;
            case "VALUES" -> VALUES;
            case "FROM" -> FROM;
            case "WHERE" -> WHERE;
            case "AND" -> AND;
            case "OR" -> OR;
            case "SET" -> SET;
            case "ON" -> ON;
            case "TRUE" -> TRUE;
            case "FALSE" -> FALSE;
            case "NULL" -> NULL;
            case "ADD" -> ADD;
            case "DROP" -> DROP;
            case "LIKE" -> LIKE;
            default -> throw new ParseException("Invalid DBKeyWords: " + token);
        };
    }

    public static boolean isKeyword(String token) {
        try {
            return parse(token) instanceof DBKeyWords;
        } catch (ParseException e) {
            return false;
        }
    }
    public static boolean isTargetType(DBKeyWords targetType, String value) {
        try {
            return parse(value).equals(targetType);
        } catch (ParseException e) {
            return false;
        }
    }
}
