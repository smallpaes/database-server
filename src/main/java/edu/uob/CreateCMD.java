package edu.uob;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import edu.uob.InterpretException.FailedCreatingFileException;
import edu.uob.TableException.ColumnAlreadyExistException;
import edu.uob.TableException.TableAlreadyExistException;
import edu.uob.TableException.UsingReservedWordException;

public class CreateCMD extends DBCmd {
    public CreateCMD() {
        super();
        tableNames = new ArrayList<>();
        colNames = new ArrayList<>();
        currentIdx = 1;
    }

    @Override
    public String query(DBServer server) {
        if (DBKeyWords.isTargetType(DBKeyWords.DATABASE, server.getTokens()[currentIdx])) {
            currentIdx++;
            return createDatabase(server);
        }
        try {
            currentIdx++;
            queryTableName(server);
            String tableName = tableNames.get(0);
            Table table = new Table(tableName, 0);
            if (server.getTokens().length > currentIdx && !server.getTokens()[currentIdx].equals("(")) {
                table.addColumn(table.getPk());
                createTable(server, new String[]{table.getPk()});
                server.getDB().addTable(table);
                return "[OK]";
            }
            currentIdx++;
            queryAttributeList(server);
            table.addColumn(table.getPk());
            for (String col : colNames) {
                table.addColumn(col);
            }
            createTable(server, table.getTitles());
            server.getDB().addTable(table);
        } catch (ColumnAlreadyExistException | IOException | TableAlreadyExistException
                 | FailedCreatingFileException e) {
            return "[ERROR]: Failed creating table--" + e.getMessage();
        } catch (UsingReservedWordException e) {
            return "[ERROR]: " + e.getMessage();
        } catch (Exception e) {
            return "[ERROR]: Failed creating table;";
        }
        return "[OK]";
    }

    private void createTable(DBServer server, String[] cols) throws IOException, TableAlreadyExistException, FailedCreatingFileException {
        String tableName = tableNames.get(0).toLowerCase();
        String dbName = server.getDB().getDBName();
        String tablePath = server.getStorageFolderPath() + File.separator + dbName + File.separator + tableName;
        File file = new File(tablePath + ".tab");

        boolean isCreated = file.createNewFile();
        if (!isCreated) {
            throw new TableAlreadyExistException(tableName);
        }
        if (cols == null) { return; }
        try (FileWriter writer = new FileWriter(file);
             BufferedWriter bufferWriter = new BufferedWriter(writer)) {
            bufferWriter.write(String.join("\t", cols));
            saveConfig(tablePath, 0);
        }
    }

    private String createDatabase(DBServer server) {
        String dbName = server.getTokens()[currentIdx].toLowerCase();
        if (DBKeyWords.isKeyword(dbName)) {
            return "[ERROR]: Cannot uee reserved word " + dbName;
        }
        String dbPath = server.getStorageFolderPath() + File.separator + dbName;
        File directory = new File(dbPath);
        if (directory.exists()) {
            return "[ERROR]: Database " + dbName + " already exist";
        }
        if (!directory.mkdir()) {
            return "[ERROR]: Failed while creating database" + dbName;
        }
        return "[OK]";
    }

    private void queryAttributeList(DBServer server) {
        queryAttributeName(server);
        if (!server.getTokens()[currentIdx].equals(",")) { return; }
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
}
