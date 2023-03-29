package edu.uob;

public enum CmdType {
    USE,
    CREATE,
    DROP,
    ALTER,
    INSERT,
    SELECT,
    UPDATE,
    DELETE,
    JOIN;

    public static CmdType parse(String token) throws ParseException {
        return switch (token.toUpperCase()) {
            case "USE" -> USE;
            case "CREATE" -> CREATE;
            case "DROP" -> DROP;
            case "ALTER" -> ALTER;
            case "INSERT" -> INSERT;
            case "SELECT" -> SELECT;
            case "UPDATE" -> UPDATE;
            case "DELETE" -> DELETE;
            case "JOIN" -> JOIN;
            default -> throw new ParseException("Invalid CommandType: " + token);
        };
    }
}
