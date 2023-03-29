package edu.uob;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import edu.uob.TableException.NoTableFoundException;
import edu.uob.TableException.UsingReservedWordException;
import edu.uob.TableException.NoColumnFoundException;

public class JoinCMD extends DBCmd {
    public JoinCMD() {
        super();
        tableNames = new ArrayList<>();
        colNames = new ArrayList<>();
        currentIdx = 1;
    }

    @Override
    public String query(DBServer server) {
        try {
            queryTableName(server);
            currentIdx++;
            queryTableName(server);
            currentIdx++;
            queryAttributeName(server);
            currentIdx++;
            queryAttributeName(server);
            orderColNames();
            return "[OK] \n" + createJoinTable(server);
        } catch (NoColumnFoundException | NoTableFoundException | UsingReservedWordException e) {
            return "[ERROR]: Failed joining tables--" + e.getMessage();
        } catch (Exception e) {
            return "[ERROR]: Failed joining tables";
        }
    }

    private void orderColNames() {
        String patternString = "\\.";
        Pattern pattern = Pattern.compile(patternString);
        HashMap<String, String> tablesColMap = new HashMap<>();
        for (String name : tableNames) {
            tablesColMap.put(name, null);
        }
        ArrayList<String> colsLeft = new ArrayList<>();
        for (String name : colNames) {
            Matcher matcher = pattern.matcher(name);
            if (!matcher.find()) {
                colsLeft.add(name);
                continue;
            }
            String[] texts = name.split("\\.");
            String tableName = texts[0];
            tablesColMap.put(tableName, texts[1]);
        }
        int iteration = 0;
        for (String name : tableNames) {
            if (tablesColMap.get(name) != null) {
                colNames.set(iteration, tablesColMap.get(name));
                iteration++;
                continue;
            }
            colNames.set(iteration, colsLeft.get(0));
            colsLeft.remove(0);
            iteration++;
        }
    }

    private String createJoinTable(DBServer server) throws NoColumnFoundException, NoTableFoundException, UsingReservedWordException {
        Table firstTable = server.getDB().getTableByName(tableNames.get(0));
        Table secondTable = server.getDB().getTableByName(tableNames.get(1));
        boolean isFirstColExist = firstTable.isColumnExist(colNames.get(0));
        boolean isSecondColExist = secondTable.isColumnExist(colNames.get(1));
        if (!isFirstColExist) {
            throw new NoColumnFoundException(colNames.get(0));
        }
        if (!isSecondColExist) {
            throw new NoColumnFoundException(colNames.get(1));
        }
        List<String> joinedTitles = joinTitles(firstTable, secondTable);
        List<List<String>> joinedDataValues = joinDataValues(firstTable, secondTable);
        return Table.tableToString(joinedTitles, joinedDataValues);
    }

    private List<String> joinTitles(Table firstTable, Table secondTable) {
        ArrayList<String> titles = new ArrayList<>();
        titles.add(firstTable.getPk());
        titles.addAll(getJoinedTitles(firstTable, colNames.get(0)));
        titles.addAll(getJoinedTitles(secondTable, colNames.get(1)));
        return titles;
    }

    private List<String> getJoinedTitles(Table table, String column) {
        ArrayList<String> titles = new ArrayList<>();
        for (String title : table.getTitles()) {
            if (!title.equalsIgnoreCase(column) && !title.equalsIgnoreCase(table.getPk())) {
                titles.add(table.getName() + "." + title);
            }
        }
        return titles;
    }


    private List<List<String>> joinDataValues(Table firstTable, Table secondTable) {
        List<List<String>> dataValues = new ArrayList<>();
        String firstTitle = colNames.get(0);
        int firstTitleIndex = firstTable.getTitleIndexByName(firstTitle);
        String[][] firstTableData = firstTable.getDataValues();
        String secondTitle = colNames.get(1);
        int secondTitleIndex = secondTable.getTitleIndexByName(secondTitle);
        String[][] secondTableData = secondTable.getDataValues();

        int newId = 1;
        for (String[] firstTableRow : firstTableData) {
            for (String[] secondTableRow : secondTableData) {
                List<String> joinedRow = joinRow(firstTableRow, firstTitleIndex, secondTableRow, secondTitleIndex, newId);
                if (joinedRow.isEmpty()) { continue; }
                newId++;
                dataValues.add(joinedRow);
            }
        }
        return dataValues;
    }

    private List<String> joinRow(String[] tableOneRow, int firstIdx, String[] tableTwoRow, int secondIdx, int newId) {
        String firstTableValue = tableOneRow[firstIdx];
        String secondTableValue = tableTwoRow[secondIdx];
        ArrayList<String> row = new ArrayList<>();
        if (!firstTableValue.equals(secondTableValue)) { return row; }
        row.add(Integer.toString(newId));
        row.addAll(getValuesFromTable(tableOneRow, firstIdx));
        row.addAll(getValuesFromTable(tableTwoRow, secondIdx));
        return row;
    }

    private ArrayList<String> getValuesFromTable(String[] tableRow, int joinedIdx) {
        ArrayList<String> row = new ArrayList<>();
        if (joinedIdx > 0) {
            row.addAll(Arrays.asList(Arrays.copyOfRange(tableRow, 1, joinedIdx)));
        }
        if (joinedIdx + 1 < tableRow.length) {
            row.addAll(Arrays.asList(Arrays.copyOfRange(tableRow, joinedIdx + 1, tableRow.length)));
        }
        return row;
    }

    private void queryAttributeName(DBServer server) {
        String token = server.getTokens()[currentIdx];
        colNames.add(token);
        currentIdx++;
    }
}
