package edu.uob;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import edu.uob.InterpretException.FailedCreatingFileException;
import edu.uob.InterpretException.StringWithNoQuoteException;
import edu.uob.TableException.NoTableFoundException;
import edu.uob.TableException.UsingReservedWordException;

public abstract class DBCmd {
    protected List<String> tableNames;
    protected List<String> colNames;
    protected String dbName;
    protected int currentIdx;

    protected abstract String query(DBServer server);

    protected boolean isMatchConditions(Condition condition, String[] titles, String[] row) throws StringWithNoQuoteException {
        boolean hasSubConditions = condition.subConditions != null && condition.subConditions.size() > 0;
        return hasSubConditions ? isMatchSubConditions(condition, titles, row) : isMatchOneCondition(condition, titles, row);
    }

    protected boolean isMatchSubConditions(Condition condition, String[] titles, String[] row) throws StringWithNoQuoteException {
        boolean isMatchFirstConds = isMatchConditions(condition.subConditions.get(0), titles, row);
        boolean isMatchSecondConds = isMatchConditions(condition.subConditions.get(1), titles, row);
        boolean isBooleanOperator = DBKeyWords.isTargetType(DBKeyWords.AND, condition.boolOperator);
        return isBooleanOperator ? isMatchFirstConds && isMatchSecondConds : isMatchFirstConds || isMatchSecondConds;
    }

    protected boolean isMatchOneCondition(Condition condition, String[] titles, String[] row) throws StringWithNoQuoteException {
        int columnIndex = getIndexInTitles(titles, condition.attributeName);
        String conditionValue = condition.value;
        boolean isInvalidCompare = ValueType.isComparableTypes(row[columnIndex], conditionValue);
        if (!isInvalidCompare) { return false; }
        boolean isString = ValueType.parseType(conditionValue).equals(ValueType.STRING);
        if (isString) {
            conditionValue = ValueType.retrieveStringFromQuote(conditionValue);
        }
        return ValueType.compareRawToTargetValue(condition.comparator, row[columnIndex], conditionValue);
    }

    private int getIndexInTitles(String[] titles, String title) {
        for (int i = 0; i < titles.length; i++) {
            if (titles[i].equalsIgnoreCase(title)) {
                return i;
            }
        }
        return -1;
    }

    protected void saveTable(DBServer server) throws IOException, NoTableFoundException, FailedCreatingFileException {
        String tableName = tableNames.get(0);
        String dbName = server.getDB().getDBName();
        String tablePath = server.getStorageFolderPath() + File.separator + dbName + File.separator + tableName;

        File file = new File(tablePath + ".tab");
        try (FileWriter writer = new FileWriter(file);
             BufferedWriter bufferWriter = new BufferedWriter(writer)) {
            Table table = server.getDB().getTableByName(tableName);
            String[] titles = table.getTitles();
            bufferWriter.write(String.join("\t", titles));
            String[][] values = table.getDataValues();
            for (String[] rowValues : values) {
                bufferWriter.newLine();
                bufferWriter.write(String.join("\t", rowValues));
            }
            saveConfig(tablePath, table.getLastPrimaryKey());
        } catch (Exception e) {
            throw new FailedCreatingFileException("table: " + tableName);
        }
    }

    protected void queryTableName(DBServer server) throws UsingReservedWordException {
        String tableName = server.getTokens()[currentIdx].toLowerCase();
        if (DBKeyWords.isKeyword(tableName)) {
            throw new UsingReservedWordException(tableName);
        }
        tableNames.add(tableName);
        currentIdx++;
    }

    protected void saveConfig(String tablePath, int lastPk) throws FailedCreatingFileException, IOException {
        String filePath = tablePath + "_config.tab";
        File configFile = new File(filePath);
        if (!configFile.exists() && !configFile.createNewFile()) {
            throw new FailedCreatingFileException("config file");
        }
        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write("pk=" + lastPk);
        } catch (Exception e) {
            throw new FailedCreatingFileException("config file");
        }
    }
}
