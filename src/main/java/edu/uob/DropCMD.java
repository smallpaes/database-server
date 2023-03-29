package edu.uob;

import java.io.File;
import java.util.ArrayList;
import edu.uob.TableException.NoTableFoundException;
import edu.uob.TableException.UsingReservedWordException;
import edu.uob.DatabaseException.DatabaseNotFoundException;
import edu.uob.DatabaseException.FailedDeletingDatabaseException;
import edu.uob.TableException.FailedDeletingTableException;


public class DropCMD extends DBCmd {
    public DropCMD() {
        super();
        tableNames = new ArrayList<>();
        currentIdx = 1;
    }

    @Override
    public String query(DBServer server) {
        try {
            if (DBKeyWords.isTargetType(DBKeyWords.TABLE, server.getTokens()[currentIdx])) {
                currentIdx++;
                queryTableName(server);
                return dropTable(server);
            }
            currentIdx++;
            dbName = server.getTokens()[currentIdx];
            if (DBKeyWords.isKeyword(dbName)) {
                throw new UsingReservedWordException(dbName);
            }
            return dropDatabase(server);
        } catch (UsingReservedWordException | FailedDeletingTableException | NoTableFoundException |
                 DatabaseNotFoundException | FailedDeletingDatabaseException e) {
            return "[ERROR]: " + e.getMessage();
        }
    }

    private String dropTable(DBServer server) throws FailedDeletingTableException, NoTableFoundException {
        try {
            dbName = server.getDB().getDBName();
            server.getDB().deleteTableByName(tableNames.get(0));
            String tablePath = server.getStorageFolderPath() + File.separator + dbName + File.separator + tableNames.get(0) + ".tab";
            File file = new File(tablePath);
            if (!file.exists()) {
                throw new NoTableFoundException(tableNames.get(0));
            }
            if (!file.delete()) {
                throw new FailedDeletingTableException(tableNames.get(0));
            }
            deleteConfigFile(server, tableNames.get(0));
        } catch (Exception e) {
            throw new FailedDeletingTableException(tableNames.get(0));
        }
        return "[OK]";
    }

    private void deleteConfigFile(DBServer server, String tableName) throws FailedDeletingTableException {
        try {
            dbName = server.getDB().getDBName();
            String path = server.getStorageFolderPath() + File.separator + dbName + File.separator + tableName + "_config.tab";
            File configFile = new File(path);
            if (!configFile.delete()) {
                throw new FailedDeletingTableException(tableNames.get(0));
            }
        } catch (Exception e) {
            throw new FailedDeletingTableException(tableName);
        }
    }
    private String dropDatabase(DBServer server) throws FailedDeletingDatabaseException, DatabaseNotFoundException, FailedDeletingTableException, UsingReservedWordException {
        String tablePath = server.getStorageFolderPath() + File.separator + dbName;
        File directory = new File(tablePath);
        if (!directory.exists() || !directory.isDirectory()) {
            throw new DatabaseNotFoundException(dbName);
        }
        try {
            deleteFiles(directory.listFiles());
            if (!directory.delete()) {
                throw new FailedDeletingDatabaseException(dbName);
            }
        } catch (Exception e) {
            throw new FailedDeletingDatabaseException(dbName);
        }
        resetCurrentDatabase(server, dbName);
        return "[OK]";
    }

    private void deleteFiles(File[] files) throws FailedDeletingTableException {
        if (files == null || files.length == 0) { return; }
        for (File file : files) {
            try {
                if (!file.delete()) {
                    throw new FailedDeletingTableException(tableNames.get(0));
                }
            } catch (Exception e) {
                throw new FailedDeletingTableException(tableNames.get(0));
            }
        }
    }

    private void resetCurrentDatabase(DBServer server, String dbName) {
        if (dbName.equalsIgnoreCase(server.getDB().getDBName())) {
            server.resetDB();
        }
    }
}
