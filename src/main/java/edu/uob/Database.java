package edu.uob;

import java.util.ArrayList;
import java.util.List;
import edu.uob.TableException.NoTableFoundException;

public class Database {
    private final String name;
    private List<Table> tables;

    public Database(String dbName) {
        name = dbName;
        tables = new ArrayList<>();
    }

    public void addTable(Table table) {
        tables.add(table);
    }

    public String getDBName() {
        return name;
    }

    public Table getTableByName(String name) throws NoTableFoundException {
        for (Table table : tables) {
            if(table.getName().equalsIgnoreCase(name)) {
                return table;
            }
        }
        throw new NoTableFoundException(name);
    }

    public boolean isTableExists(String name) {
        for (Table table : tables) {
            if(table.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public void deleteTableByName(String name) throws NoTableFoundException {
        if (!isTableExists(name))  { throw new NoTableFoundException(name); }
        ArrayList<Table> newTableList = new ArrayList<>();
        for (Table table : tables) {
            if (!table.getName().equalsIgnoreCase(name)) {
                newTableList.add(table);
            }
        }
        tables = newTableList;
    }
}
