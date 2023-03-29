package edu.uob;

import java.io.Serial;

public class TableException extends Exception {
    @Serial private static final long serialVersionUID = 1;

    public TableException(String message) {
        super(message);
    }

    public static class NoColumnFoundException extends TableException {
        @Serial private static final long serialVersionUID = 1;
        public NoColumnFoundException(String columnName) {
            super("Column " + columnName + " does not exist");
        }
    }

    public static class ColumnAlreadyExistException extends TableException {
        @Serial private static final long serialVersionUID = 1;
        public ColumnAlreadyExistException(String columnName) {
            super("Column " + columnName + " already exist");
        }
    }

    public static class IDColumnNotUpdatableException extends TableException {
        @Serial private static final long serialVersionUID = 1;
        public IDColumnNotUpdatableException() {
            super("ID column is not updatable");
        }
    }

    public static class TableAlreadyExistException extends TableException {
        @Serial private static final long serialVersionUID = 1;
        public TableAlreadyExistException(String tableName) {
            super("Table " + tableName + " already exist");
        }
    }

    public static class NoTableFoundException extends TableException {
        @Serial private static final long serialVersionUID = 1;
        public NoTableFoundException(String tableName) {
            super("Table " + tableName + " does not exist");
        }
    }

    public static class InsertInsufficientValuesException extends TableException {
        @Serial private static final long serialVersionUID = 1;
        public InsertInsufficientValuesException(String tableName) {
            super("Insufficient values provided while inserting data to table " + tableName);
        }
    }

    public static class InsertTooManyValuesException extends TableException {
        @Serial private static final long serialVersionUID = 1;
        public InsertTooManyValuesException(String tableName) {
            super("Too many values provided while inserting data to table " + tableName);
        }
    }

    public static class NoDataValueProvidedException extends TableException {
        @Serial private static final long serialVersionUID = 1;
        public NoDataValueProvidedException() {
            super("No data value is provided");
        }
    }

    public static class UsingReservedWordException extends TableException {
        @Serial private static final long serialVersionUID = 1;
        public UsingReservedWordException(String keyword) {
            super("Cannot use reserved words " + keyword + "as database, table, or attribute names");
        }
    }

    public static class FailedDeletingTableException extends TableException {
        @Serial private static final long serialVersionUID = 1;
        public FailedDeletingTableException(String tableName) {
            super("Failed deleting table " + tableName);
        }
    }
}
