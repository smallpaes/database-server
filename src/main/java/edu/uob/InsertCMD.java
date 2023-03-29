package edu.uob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import edu.uob.InterpretException.StringWithNoQuoteException;
import edu.uob.InterpretException.FailedCreatingFileException;
import edu.uob.TableException.NoTableFoundException;
import edu.uob.TableException.InsertInsufficientValuesException;
import edu.uob.TableException.UsingReservedWordException;
import edu.uob.TableException.InsertTooManyValuesException;

public class InsertCMD extends DBCmd {
    private final List<String> values = new ArrayList<>();
    public InsertCMD() {
        super();
        tableNames = new ArrayList<>();
        colNames = new ArrayList<>();
        currentIdx = 2;
    }

    @Override
    public String query(DBServer server) {
        try {
            queryTableName(server);
            currentIdx += 2;
            queryValueList(server);
            Table table = server.getDB().getTableByName(tableNames.get(0));
            table.addRowWithoutID(values);
            saveTable(server);
            return "[OK]";
        } catch (StringWithNoQuoteException | NoTableFoundException | InsertInsufficientValuesException |
                 InsertTooManyValuesException | FailedCreatingFileException | IOException | UsingReservedWordException e) {
            return "[ERROR]: Failed inserting to table--" + e.getMessage();
        } catch (Exception e) {
            return "[ERROR]: Failed inserting to table";
        }
    }

    private void queryValueList(DBServer server) throws InterpretException.StringWithNoQuoteException {
        String value = server.getTokens()[currentIdx];
        ValueType valueType = ValueType.parseType(value);
        if (valueType == ValueType.STRING) {
            values.add(ValueType.retrieveStringFromQuote(value));
        } else {
            values.add(value);
        }
        currentIdx++;
        if (!server.getTokens()[currentIdx].equals(",")) { return; }
        currentIdx++;
        queryValueList(server);
    }
}
