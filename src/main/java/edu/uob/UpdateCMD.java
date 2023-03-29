package edu.uob;

import java.io.IOException;
import java.util.*;

import edu.uob.TableException.NoTableFoundException;
import edu.uob.TableException.UsingReservedWordException;
import edu.uob.TableException.NoColumnFoundException;
import edu.uob.TableException.IDColumnNotUpdatableException;
import edu.uob.InterpretException.StringWithNoQuoteException;
import edu.uob.InterpretException.FailedCreatingFileException;

public class UpdateCMD extends DBCmd {
    private final Map<String, String> nameValueMap = new HashMap<>();
    public UpdateCMD() {
        super();
        tableNames = new ArrayList<>();
        colNames = new ArrayList<>();
        currentIdx = 1;
    }

    @Override
    public String query(DBServer server) {
        try {
            queryTableName(server);
            Table table = server.getDB().getTableByName(tableNames.get(0));
            currentIdx++;
            queryNameValueList(server);
            currentIdx++;
            List<List<String>> newDataValues = queryCondition(server, currentIdx, table);
            table.updateDataValues(newDataValues);
            saveTable(server);
            return "[OK]";
        } catch (NoTableFoundException | StringWithNoQuoteException | FailedCreatingFileException |
                 IOException | IDColumnNotUpdatableException |
                 NoColumnFoundException | UsingReservedWordException e) {
            return "[ERROR]: Failed updating data to table--" + e.getMessage();
        } catch (Exception e) {
            return "[ERROR]: Failed updating data to table";
        }
    }

    private List<List<String>> queryCondition(DBServer server, int index, Table table) throws StringWithNoQuoteException, IDColumnNotUpdatableException, NoColumnFoundException {
        String[] colNamesArray = table.getTitles();
        String[][] dataValues = table.getDataValues();
        Condition condition = Parser.parseCondition(server, index);
        List<List<String>> newDataValues = new ArrayList<>();
        for (String[] dataValue : dataValues) {
            if (!isMatchConditions(condition, colNamesArray, dataValue)) {
                newDataValues.add(new ArrayList<>(Arrays.asList(dataValue)));
                continue;
            }
            String[] updatedRow = updateRowValues(dataValue, table);
            newDataValues.add(new ArrayList<>(Arrays.asList(updatedRow)));
        }
        return newDataValues;
    }

    private String[] updateRowValues(String[] row, Table table) throws IDColumnNotUpdatableException, NoColumnFoundException {
        for (Map.Entry<String, String> entry : nameValueMap.entrySet()) {
            String columnName  = entry.getKey();
            String value = entry.getValue();
            if (columnName.equalsIgnoreCase(table.getPk())) {
                throw new IDColumnNotUpdatableException();
            }
            int columnIndex = table.getColumnIdxByName(columnName);
            if (columnIndex < 0) {
                throw new NoColumnFoundException(columnName);
            }
            row[columnIndex] = value;
        }
        return row;
    }

    private void queryNameValueList(DBServer server) throws UsingReservedWordException {
        queryNameValuePair(server);
        if (!server.getTokens()[currentIdx].equals(",")) { return; }
        currentIdx++;
        queryNameValueList(server);
    }

    private void queryNameValuePair(DBServer server) throws UsingReservedWordException {
        String attributeName = server.getTokens()[currentIdx];
        if (DBKeyWords.isKeyword(attributeName)) {
            throw new UsingReservedWordException(attributeName);
        }
        currentIdx+=2;
        String value = server.getTokens()[currentIdx];
        nameValueMap.put(attributeName, value);
        currentIdx++;
    }
}
