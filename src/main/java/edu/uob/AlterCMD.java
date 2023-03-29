package edu.uob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import edu.uob.TableException.ColumnAlreadyExistException;
import edu.uob.TableException.IDColumnNotUpdatableException;
import edu.uob.TableException.NoTableFoundException;
import edu.uob.TableException.NoColumnFoundException;
import edu.uob.TableException.UsingReservedWordException;
import edu.uob.InterpretException.FailedCreatingFileException;
public class AlterCMD extends DBCmd {
    public AlterCMD() {
        super();
        tableNames = new ArrayList<>();
        colNames = new ArrayList<>();
        currentIdx = 2;
    }

    @Override
    public String query(final DBServer server) {
        try {
            queryTableName(server);
            String alterationType = server.getTokens()[currentIdx];
            currentIdx++;
            queryAttributeName(server);
            if (DBKeyWords.isTargetType(DBKeyWords.ADD, alterationType)) {
                addColumn(server);
            } else {
                dropColumn(server);
            }
            saveTable(server);
            return "[OK]";
        } catch (ColumnAlreadyExistException | IDColumnNotUpdatableException | NoTableFoundException | NoColumnFoundException |
                 IOException | FailedCreatingFileException | UsingReservedWordException e) {
            return "[ERROR]: Failed altering table--" + e.getMessage();
        } catch (Exception e) {
            return "[ERROR]: Failed altering table;";
        }
    }

    private void addColumn(final DBServer server) throws ColumnAlreadyExistException, NoTableFoundException, UsingReservedWordException {
        Table table = server.getDB().getTableByName(tableNames.get(0));
        table.addColumn(colNames.get(0));
    }

    private void dropColumn(final DBServer server) throws NoColumnFoundException, NoTableFoundException, IDColumnNotUpdatableException {
        Table table = server.getDB().getTableByName(tableNames.get(0));
        table.dropColumn(colNames.get(0));
    }

    private void queryAttributeName(final DBServer server) {
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
}
