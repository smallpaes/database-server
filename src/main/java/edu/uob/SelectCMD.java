package edu.uob;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import edu.uob.InterpretException.StringWithNoQuoteException;
import edu.uob.TableException.NoTableFoundException;
import edu.uob.TableException.UsingReservedWordException;
import edu.uob.TableException.NoColumnFoundException;

public class SelectCMD extends DBCmd {
    public SelectCMD() {
        super();
        tableNames = new ArrayList<>();
        colNames = new ArrayList<>();
        currentIdx = 1;
    }

    @Override
    public String query(DBServer server) {
        try {
            queryWildAttribList(server);
            queryTableName(server);
            Table table = server.getDB().getTableByName(tableNames.get(0));
            if (colNames.get(0).equals("*")) {
                colNames = new ArrayList<>(Arrays.asList(table.getColumnNames()));
            } else {
                colNames = table.getRawTitlesByTitles(colNames.toArray(new String[0]));
            }
            String result;
            if (!DBKeyWords.isTargetType(DBKeyWords.WHERE, server.getTokens()[currentIdx])) {
                result = getDataValue(table);
                return "[OK]: \n" + result;
            }
            currentIdx++;
            List<List<String>> dataValues = queryCondition(server, currentIdx, table);
            result = Table.tableToString(colNames, dataValues);
            return "[OK]: \n" + result;
        } catch (NoTableFoundException | NoColumnFoundException | StringWithNoQuoteException | UsingReservedWordException e) {
            return "[ERROR]: Failed getting data from table--" + e.getMessage();
        } catch (Exception e) {
            return "[ERROR]: Failed getting data";
        }
    }

    private List<List<String>> queryCondition(DBServer server, int index, Table table) throws NoColumnFoundException, StringWithNoQuoteException {
        String[] rawTitles = table.getTitles();
        List<List<String>> dataValues = table.getDataValuesByColumns(colNames);
        String[][] rawDataValues = table.getDataValues();
        Condition condition = Parser.parseCondition(server, index);
        List<List<String>> newDataValues = new ArrayList<>();
        for (int i = 0; i < dataValues.size(); i++) {
            if (!isMatchConditions(condition, rawTitles, rawDataValues[i])) {
                continue;
            }
            newDataValues.add(dataValues.get(i));
        }
        return newDataValues;
    }

    private void queryWildAttribList(DBServer server) {
        String token = server.getTokens()[currentIdx];
        if (token.equals("*")) {
            colNames.add("*");
            currentIdx += 2;
            return;
        }
        queryAttributeList(server);
    }

    private void queryAttributeList(DBServer server) {
        queryAttributeName(server);
        if (!server.getTokens()[currentIdx].equals(",")) {
            currentIdx++;
            return;
        }
        currentIdx++;
        queryAttributeList(server);
    }

    private void queryAttributeName(DBServer server) {
        String token = server.getTokens()[currentIdx];
        String patternString = "\\.";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(token);
        if (matcher.find()) {
            String[] texts = token.split("\\.");
            colNames.add(texts[1]);
        } else {
            colNames.add(token);
        }
        currentIdx++;
    }

    private String getDataValue(Table table) throws NoColumnFoundException {
        return Table.tableToString(colNames, table.getDataValuesByColumns(colNames));
    }
}
