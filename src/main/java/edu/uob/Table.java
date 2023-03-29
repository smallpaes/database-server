package edu.uob;

import java.util.ArrayList;
import java.util.List;

import edu.uob.TableException.InsertInsufficientValuesException;
import edu.uob.TableException.UsingReservedWordException;
import edu.uob.TableException.InsertTooManyValuesException;
import edu.uob.TableException.NoColumnFoundException;
import edu.uob.TableException.NoDataValueProvidedException;
import edu.uob.TableException.ColumnAlreadyExistException;
import edu.uob.TableException.IDColumnNotUpdatableException;



public class Table {
    private final String name;
    private final String primaryKey;
    private final List<String> columnNames;
    private List<List<String>> dataValues;
    private int lastPrimaryKey;

    public Table(String tableName, int lastPrimaryKey) {
        this.name = tableName.toLowerCase();
        this.primaryKey = "id";
        this.columnNames = new ArrayList<>();
        this.dataValues = new ArrayList<>();
        this.lastPrimaryKey = lastPrimaryKey;
    }

    public void addRowWithID(List<String> row) throws InsertInsufficientValuesException, InsertTooManyValuesException {
        if (row.size() > columnNames.size()) {
            throw new InsertTooManyValuesException(name);
        } else if (row.size() < columnNames.size()) {
            throw new InsertInsufficientValuesException(name);
        }
        dataValues.add(row);
    }

    public void setLastPrimaryKey(int number) {
        lastPrimaryKey = number;
    }

    public int getLastPrimaryKey() {
        return lastPrimaryKey;
    }

    public String getPk() {
        return primaryKey;
    }

    public void addRowWithoutID(List<String> row) throws InsertInsufficientValuesException, InsertTooManyValuesException {
        if (row.size() + 1 > columnNames.size()) {
            throw new InsertTooManyValuesException(name);
        } else if (row.size() + 1 < columnNames.size()) {
            throw new InsertInsufficientValuesException(name);
        }
        ArrayList<String> rowWithID = new ArrayList<>();
        rowWithID.add(Integer.toString(lastPrimaryKey + 1));
        rowWithID.addAll(row);
        lastPrimaryKey++;
        addRowWithID(rowWithID);
    }

    public String getName() {
        return name;
    }

    public String[] getTitles() {
        String[] titles = new String[columnNames.size()];
        for (int i = 0; i < columnNames.size(); i++) {
            titles[i] = columnNames.get(i);
        }
        return titles;
    }

    public int getTitleIndexByName(String columnName) {
        for (int i = 0; i < columnNames.size(); i++) {
            if (columnNames.get(i).equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }

    public void addColumn(String name) throws ColumnAlreadyExistException, UsingReservedWordException {
        if (isColumnExist(name)) {
            throw new ColumnAlreadyExistException(name);
        }
        if (DBKeyWords.isKeyword(name)) {
            throw new UsingReservedWordException(name);
        }
        columnNames.add(name);
        for (List<String> row : dataValues) {
            row.add("NULL");
        }
    }

    public void dropColumn(String name) throws NoColumnFoundException, IDColumnNotUpdatableException {
        if (name.equals(primaryKey)) {
            throw new IDColumnNotUpdatableException();
        }
        int columnIndex = getColumnIdxByName(name);
        if (columnIndex < 0) {
            throw new NoColumnFoundException(name);
        }
        columnNames.remove(name);
        for (List<String> row : dataValues) {
            row.remove(columnIndex);
        }
    }

    public int getColumnIdxByName(String columnName) {
        int columnIndex = -1;
        for (int i = 0; i < columnNames.size(); i++) {
            if (columnNames.get(i).equalsIgnoreCase(columnName)) {
                columnIndex = i;
                break;
            }
        }
        return columnIndex;
    }

    public boolean isColumnExist(String name) throws UsingReservedWordException {
        if (DBKeyWords.isKeyword(name)) {
            throw new UsingReservedWordException(name);
        }
        for (String column : columnNames) {
            if (column.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public String[][] getDataValues() {
        int rowAmount = dataValues.size();
        int columnAmount = columnNames.size();
        String[][] values = new String[rowAmount][columnAmount];
        for (int i = 0; i < rowAmount; i++) {
            for (int j = 0; j < columnAmount; j++) {
                values[i][j] = dataValues.get(i).get(j);
            }
        }

        return values;
    }

    public List<List<String>> getDataValuesByColumns(List<String> cols) throws NoColumnFoundException {
        int[] selectedIndexes = new int[cols.size()];
        for (int i = 0; i < cols.size(); i++) {
            int idx = getColumnIdxByName(cols.get(i));
            if (idx < 0) { throw new NoColumnFoundException(cols.get(i)); }
            selectedIndexes[i] = idx;
        }
        List<List<String>> newDataValues = new ArrayList<>();
        int rowAmount = dataValues.size();
        int columnAmount = selectedIndexes.length;
        for (int i = 0; i < rowAmount; i++) {
            newDataValues.add(new ArrayList<>());
            for (int j = 0; j < columnAmount; j++) {
                newDataValues.get(i).add(dataValues.get(i).get(selectedIndexes[j]));
            }
        }
        return newDataValues;
    }

    public void updateDataValues(List<List<String>> dataValues) throws NoDataValueProvidedException {
        if (dataValues == null) {
            throw new NoDataValueProvidedException();
        }
        this.dataValues = dataValues;
    }

    public String[] getColumnNames() {
        return columnNames.toArray(new String[0]);
    }

    public static String tableToString(List<String> cols, List<List<String>> dataValues) {
        StringBuilder table = new StringBuilder();
        for (String col : cols) {
            table.append(col);
            table.append('\t');
        }
        table.append('\n');
        for (List<String> row : dataValues) {
            for (String col : row) {
                table.append(col);
                table.append('\t');
            }
            table.append('\n');
        }
        return table.toString();
    }

    protected List<String> getRawTitlesByTitles(String[] names) throws NoColumnFoundException, UsingReservedWordException {
        ArrayList<String> titles = new ArrayList<>();
        for (String inputName : names) {
            boolean isFound = false;
            for (String savedName : columnNames) {
                if (!savedName.equalsIgnoreCase(inputName)) { continue; }
                if (DBKeyWords.isKeyword(savedName)) {
                    throw new UsingReservedWordException(name);
                }
                titles.add(savedName);
                isFound = true;
                break;
            }
            if (isFound) { continue; }
            throw new NoColumnFoundException(inputName);
        }
        return titles;
    }
}
