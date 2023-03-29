package edu.uob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import edu.uob.InterpretException.StringWithNoQuoteException;
import edu.uob.InterpretException.FailedCreatingFileException;

public class DeleteCMD extends DBCmd {
    public DeleteCMD() {
        super();
        tableNames = new ArrayList<>();
        currentIdx = 2;
    }

    @Override
    public String query(DBServer server) {
        try {
            queryTableName(server);
            Table table = server.getDB().getTableByName(tableNames.get(0));
            colNames = new ArrayList<>(Arrays.asList(table.getColumnNames()));
            currentIdx++;
            List<List<String>> updatedDataValues = queryCondition(server, currentIdx, table);
            table.updateDataValues(updatedDataValues);
            saveTable(server);
            return "[OK]";
        } catch (TableException | StringWithNoQuoteException | IOException |
                 FailedCreatingFileException e) {
            return "[ERROR]: Failed deleting table--" + e.getMessage();
        }  catch (Exception e) {
            return "[ERROR]: Failed deleting table";
        }
    }

    private List<List<String>> queryCondition(DBServer server, int index, Table table) throws StringWithNoQuoteException {
        String[] titles = table.getTitles();
        String[][] dataValues = table.getDataValues();
        Condition condition = Parser.parseCondition(server, index);
        List<List<String>> newDataValues = new ArrayList<>();
        for (String[] dataValue : dataValues) {
            if (isMatchConditions(condition, titles, dataValue)) { continue; }
            newDataValues.add(new ArrayList<>(Arrays.asList(dataValue)));
        }
        return newDataValues;
    }
}
