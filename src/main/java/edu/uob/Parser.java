package edu.uob;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    private DBCmd cmd;
    private final String[] tokens;
    private int currentIdx;
    private boolean isOutOfBound = false;

    public Parser(String[] tokens) {
        this.tokens = tokens;
        this.currentIdx = 0;
    }

    public DBCmd parse() throws ParseException {
        CmdType cmdType = CmdType.parse(tokens[currentIdx]);
        switch (cmdType) {
            case USE -> {
                parseUse();
                cmd = new UseCMD();
                return cmd;
            }
            case CREATE -> {
                parseCreate();
                cmd = new CreateCMD();
                return cmd;
            }
            case DROP -> {
                parseDrop();
                cmd = new DropCMD();
                return cmd;
            }
            case ALTER -> {
                parseAlter();
                cmd = new AlterCMD();
                return cmd;
            }
            case INSERT -> {
                parseInsert();
                cmd = new InsertCMD();
                return cmd;
            }
            case SELECT -> {
                parseSelect();
                cmd = new SelectCMD();
                return cmd;
            }
            case UPDATE -> {
                parseUpdate();
                cmd = new UpdateCMD();
                return cmd;
            }
            case DELETE -> {
                parseDelete();
                cmd = new DeleteCMD();
                return cmd;
            }
            case JOIN -> {
                parseJoin();
                cmd = new JoinCMD();
                return cmd;
            }
            default -> throw new ParseException("Invalid CommandType: " + cmdType);
        }
    }

    private void parseClosingBracket() throws ParseException {
        if (isOutOfBound || !tokens[currentIdx].equals(";")) {
            throw new ParseException("Invalid Command: Lack of ending ;" );
        }
    }

    private void parseUse() throws ParseException {
        moveOn();
        parseDataBaseName();
        parseClosingBracket();
    }

    private void parseNameValueList() throws ParseException {
        parseNameValuePair();
        if (isOutOfBound || !tokens[currentIdx].equals(",")) { return; }
        moveOn();
        parseNameValueList();
    }

    private void parseNameValuePair() throws ParseException {
        parseAttributeName();
        if (!tokens[currentIdx].equals("=")) {
            throw new ParseException("Invalid NameValuePair: Lack of '='" );
        }
        moveOn();
        if (!isValue(tokens[currentIdx])) {
            throw new ParseException("Invalid NameValuePair: Invalid Value" );
        }
        moveOn();
    }

    private void parseCreate() throws ParseException {
        moveOn();
        DBKeyWords keyWord = DBKeyWords.parse(tokens[currentIdx]);
        if (keyWord.equals(DBKeyWords.DATABASE)) {
            parseCreateDatabase();
        } else if (keyWord.equals(DBKeyWords.TABLE)) {
            parseCreateTable();
        } else {
            throw new ParseException("Invalid Create Command: Lack of command TABLE or DATABASE");
        }
        parseClosingBracket();
    }

    private void parseUpdate() throws ParseException {
        moveOn();
        parseTableName(tokens[currentIdx]);
        moveOn();
        if (!DBKeyWords.parse(tokens[currentIdx]).equals(DBKeyWords.SET)) {
            throw new ParseException("Invalid UPDATE Command: Lack of SET" );
        }
        moveOn();
        parseNameValueList();
        if (!DBKeyWords.parse(tokens[currentIdx]).equals(DBKeyWords.WHERE)) {
            throw new ParseException("Invalid UPDATE Command: Lack of WHERE" );
        }
        moveOn();
        parseCondition();
        parseClosingBracket();
    }

    private void parseSelect() throws ParseException {
        moveOn();
        parseWildAttribList();
        if (!DBKeyWords.isTargetType(DBKeyWords.FROM, tokens[currentIdx])) {
            throw new ParseException("Invalid SELECT Command: Lack of FROM" );
        }
        moveOn();
        parseTableName(tokens[currentIdx]);
        moveOn();
        if (isOutOfBound || tokens[currentIdx].equals(";")) { return; }
        if (!DBKeyWords.isTargetType(DBKeyWords.WHERE, tokens[currentIdx])) {
            throw new ParseException("Invalid SELECT Command: Lack of WHERE" );
        }
        moveOn();
        parseCondition();
        parseClosingBracket();
    }

    private void parseWildAttribList() throws ParseException {
        if (tokens[currentIdx].equals("*")) {
            moveOn();
            return;
        }
        parseAttributeList();
    }

    private void parseJoin() throws ParseException {
        moveOn();
        parseTableName(tokens[currentIdx]);
        moveOn();
        if (!DBKeyWords.isTargetType(DBKeyWords.AND, tokens[currentIdx])) {
            throw new ParseException("Invalid JOIN Command: Lack of AND between two table names" );
        }
        moveOn();
        parseTableName(tokens[currentIdx]);
        moveOn();
        if (!DBKeyWords.isTargetType(DBKeyWords.ON, tokens[currentIdx])) {
            throw new ParseException("Invalid JOIN Command: Lack of ON" );
        }
        moveOn();
        parseAttributeName();
        if (!DBKeyWords.isTargetType(DBKeyWords.AND, tokens[currentIdx])) {
            throw new ParseException("Invalid JOIN Command: Lack of AND between two column names" );
        }
        moveOn();
        parseAttributeName();
        parseClosingBracket();
    }

    private void parseDrop() throws ParseException {
        moveOn();
        if (DBKeyWords.isTargetType(DBKeyWords.DATABASE, tokens[currentIdx])) {
            moveOn();
            parseDataBaseName();
        } else if (DBKeyWords.isTargetType(DBKeyWords.TABLE, tokens[currentIdx])) {
            moveOn();
            parseTableName(tokens[currentIdx]);
            moveOn();
        } else {
            throw new ParseException("Invalid DROP Command: Lack of command TABLE or DATABASE");
        }
        parseClosingBracket();
    }

    private void parseDelete() throws ParseException {
        moveOn();
        if (!DBKeyWords.isTargetType(DBKeyWords.FROM, tokens[currentIdx])) {
            throw new ParseException("Invalid DELETE Command: Lack of command FROM" );
        }
        moveOn();
        parseTableName(tokens[currentIdx]);
        moveOn();
        if (!DBKeyWords.isTargetType(DBKeyWords.WHERE, tokens[currentIdx])) {
            throw new ParseException("Invalid DELETE Command: Lack of command WHERE" );
        }
        moveOn();
        parseCondition();
        parseClosingBracket();
    }
    private void parseCondition() throws ParseException {
        if (tokens[currentIdx].equals("(")) {
            moveOn();
            parseCondition();
            if (!tokens[currentIdx].equals(")")) {
                throw new ParseException("Invalid Condition Command: Lack of closing parenthesis" );
            }
        } else {
            parseAttributeName();
            if (!isComparator(tokens[currentIdx])) {
                throw new ParseException("Invalid Condition Command: Invalid Comparator" );
            }
            moveOn();
            if (!isValue(tokens[currentIdx])) {
                throw new ParseException("Invalid Condition Command: Invalid Value" );
            }
        }
        moveOn();
        if (isOutOfBound || !isBoolOperator(tokens[currentIdx])) { return; }
        moveOn();
        parseCondition();
    }

    private boolean isBoolOperator(String value) {
        boolean isAND = DBKeyWords.isTargetType(DBKeyWords.AND, value);
        boolean isOR = DBKeyWords.isTargetType(DBKeyWords.OR, value);
        return isAND || isOR;
    }

    private boolean isComparator(String value) {
        Pattern pattern = Pattern.compile("==|>|<|>=|<=|!=");
        Matcher matcher = pattern.matcher(value.toLowerCase());
        if (matcher.find()) { return true; }
        return DBKeyWords.isTargetType(DBKeyWords.LIKE, value);
    }

    private void parseAlter() throws ParseException {
        moveOn();
        if (!DBKeyWords.isTargetType(DBKeyWords.TABLE, tokens[currentIdx])) {
            throw new ParseException("Invalid ALTER Command: Lack of command TABLE" );
        }
        moveOn();
        parseTableName(tokens[currentIdx]);
        moveOn();
        parseAlterationType(tokens[currentIdx]);
        parseAttributeName();
        parseClosingBracket();
    }

    private void parseInsert() throws ParseException {
        moveOn();
        if (!DBKeyWords.isTargetType(DBKeyWords.INTO, tokens[currentIdx])) {
            throw new ParseException("Invalid INSERT Command: Lack of command INTO" );
        }
        moveOn();
        parseTableName(tokens[currentIdx]);
        moveOn();
        if (!DBKeyWords.isTargetType(DBKeyWords.VALUES, tokens[currentIdx])) {
            throw new ParseException("Invalid INSERT Command: Lack of command INTO" );
        }
        moveOn();
        if (!tokens[currentIdx].equals("(")) {
            throw new ParseException("Invalid INSERT Command: Lack of an opening parenthesis" );
        }
        moveOn();
        parseValueList();
        if (!tokens[currentIdx].equals(")")) {
            throw new ParseException("Invalid INSERT Command: Lack of an closing parenthesis" );
        }
        moveOn();
        parseClosingBracket();
    }

    private void parseValueList() throws ParseException {
        if (!isValue(tokens[currentIdx])) {
            throw new ParseException("Invalid INSERT Command: Invalid Value " + tokens[currentIdx] );
        }
        moveOn();
        if (!tokens[currentIdx].equals(",")) { return; }
        moveOn();
        parseValueList();
    }

    private boolean isValue(String value) {
        boolean isNullType = DBKeyWords.isTargetType(DBKeyWords.NULL, value);
        if (isNullType || isStringLiteral(value)) { return true; }
        return isIntegerLiteral(value) || isFloatLiteral(value) || isBooleanLiteral(value);
    }

    private boolean isStringLiteral(String value) {
        Pattern pattern = Pattern.compile("^'(.*)'$");
        Matcher matcher = pattern.matcher(value);
        if (!matcher.find()) { return false; }
        if (value.isEmpty()) { return true; }
        for (int i = 1; i < value.length() - 1; i++) {
            if (!isCharLiteral(value.substring(i, i + 1))) { return false; }
        }
        return true;
    }

    private boolean isCharLiteral(String value) {
        return value.equals(" ") || isLetter(value) || isSymbol(value) || isDigit(value);
    }
    private boolean isSymbol(String letter) {
        String patternString = "!|#|\\$|%|&|\\(|\\)|\\*|\\+|,|-|\\.|/|:|;|>|=|<|\\?|@|\\[|\\\\|]|\\^|_|`|\\{|}|~";
        return letter.matches(patternString);
    }

    private void moveOn() {
        currentIdx++;
        isOutOfBound = tokens.length <= currentIdx;
    }

    private boolean isBooleanLiteral(String value) {
        boolean isTrue = DBKeyWords.isTargetType(DBKeyWords.TRUE, value);
        boolean isFalse = DBKeyWords.isTargetType(DBKeyWords.FALSE, value);
        return isTrue || isFalse;
    }
    private boolean isFloatLiteral(String value) {
        String patternString = "\\.";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(value);
        if (!matcher.find()) { return false; }
        if (value.charAt(0) == '+' || value.charAt(0) == '-') {
            String[] digits = value.substring(1).split("\\.");
            return isDigitSequence(digits[0]) && isDigitSequence(digits[1]);
        }
        String[] digits = value.split("\\.");
        return isDigitSequence(digits[0]) && isDigitSequence(digits[1]);
    }

    private boolean isIntegerLiteral(String value) {
        if (value.charAt(0) == '+' || value.charAt(0) == '-') {
            return isDigitSequence(value.substring(1));
        }
        return isDigitSequence(value);
    }

    private boolean isDigitSequence(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (!isDigit(value.substring(i, i + 1))) {
                return false;
            }
        }
        return true;
    }

    private void parseAlterationType(String type) throws ParseException {
        boolean isADD = DBKeyWords.isTargetType(DBKeyWords.ADD, type);
        boolean isDROP = DBKeyWords.isTargetType(DBKeyWords.DROP, type);
        if (isADD || isDROP) {
            currentIdx++;
            return;
        }
        throw new ParseException("Invalid AlterationType: " +  type);
    }

    private void parseCreateTable() throws ParseException {
        moveOn();
        parseTableName(tokens[currentIdx]);
        moveOn();
        if (isOutOfBound || tokens[currentIdx].equals(";")) { return; }
        if (!tokens[currentIdx].equals("(")) {
            throw new ParseException("Invalid Create Table Command: Lack of command: (" );
        }
        moveOn();
        parseAttributeList();
        if (!tokens[currentIdx].equals(")")) {
            throw new ParseException("Invalid Create Table Command: Lack of command: )" );
        }
        moveOn();
    }

    private void parseAttributeList() throws ParseException {
        parseAttributeName();
        if (!tokens[currentIdx].equals(",")) { return; }
        moveOn();
        parseAttributeList();
    }

    private void parseAttributeName() throws ParseException {
        String patternString = "\\.";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(tokens[currentIdx]);
        if (matcher.find()) {
            String[] texts = tokens[currentIdx].split("\\.");
            parseTableName(texts[0]);
            parsePlainText(texts[1]);
        } else {
            parsePlainText(tokens[currentIdx]);
        }
        moveOn();
    }

    private void parseTableName(String name) throws ParseException {
        parsePlainText(name);
    }

    private void parseCreateDatabase() throws ParseException {
        moveOn();
        parseDataBaseName();
    }

    private void parseDataBaseName() throws ParseException {
        parsePlainText(tokens[currentIdx]);
        moveOn();
    }

    private void parsePlainText(String text) throws ParseException {
        for (int i = 0; i < text.length(); i++) {
            String subStr = text.substring(i, i + 1);
            if (!isLetter(subStr) && !isDigit(subStr)) {
                throw new ParseException("Invalid plain text: " + text);
            }
        }
    }

    private boolean isLetter(String letter) {
        return isUppercase(letter) || isLowercase(letter);
    }

    private boolean isUppercase(String letter) {
        String patternString = "^[A-Z]$";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(letter);
        return matcher.find();
    }

    private boolean isLowercase(String letter) {
        String patternString = "^[a-z]$";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(letter);
        return matcher.find();
    }

    private boolean isDigit(String letter) {
        String patternString = "^[0-9]$";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(letter);
        return matcher.find();
    }

    public static Condition parseCondition(DBServer server, int currentIdx) {
        String token = server.getTokens()[currentIdx];
        Condition condition;
        if (token.equals("(")) {
            int endingIndex = findEndingParenthesis(server, currentIdx);
            currentIdx++;
            condition = parseCondition(server, currentIdx);
            if (endingIndex + 1 == server.getTokens().length) { return condition; }
            currentIdx = endingIndex + 1;
        } else {
            condition = formSingleCondition(server, currentIdx);
            currentIdx+=3;
        }
        boolean isReachedParseEnd = server.getTokens().length <= currentIdx + 1;
        if (isReachedParseEnd) { return condition; }
        return formConditions(server, currentIdx, condition);
    }

    private static Condition formConditions(DBServer server, int currentIdx, Condition currentCondition) {
        String token = server.getTokens()[currentIdx];
        boolean isAndType = DBKeyWords.isTargetType(DBKeyWords.AND, token);
        boolean isOrType = DBKeyWords.isTargetType(DBKeyWords.OR, token);
        boolean isBooleanOperator = isAndType || isOrType;
        if (!isBooleanOperator) { return currentCondition; }
        currentIdx++;
        Condition nextCondition = parseCondition(server, currentIdx);
        return new Condition(currentCondition, token, nextCondition);
    }

    private static Condition formSingleCondition(DBServer server, int currentIdx) {
        String attributeName = server.getTokens()[currentIdx];
        currentIdx++;
        String comparator = server.getTokens()[currentIdx];
        currentIdx++;
        String value = server.getTokens()[currentIdx];
        return new Condition(attributeName, comparator, value);
    }

    private static int findEndingParenthesis(DBServer server, int currentIdx) {
        int openParenthesis = 0;
        while (currentIdx < server.getTokens().length) {
            if (server.getTokens()[currentIdx].equals("(")) {
                openParenthesis++;
            } else if (server.getTokens()[currentIdx].equals(")")) {
                openParenthesis--;
            }
            if (openParenthesis == 0) { return currentIdx; }
            currentIdx++;
        }
        return -1;
    }
}
