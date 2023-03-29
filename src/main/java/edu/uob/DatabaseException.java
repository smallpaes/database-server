package edu.uob;

import java.io.Serial;

public class DatabaseException extends Exception {
    @Serial
    private static final long serialVersionUID = 1;

    public DatabaseException(String message) {
        super(message);
    }

    public static class FailedDeletingDatabaseException extends DatabaseException {
        @Serial private static final long serialVersionUID = 1;
        public FailedDeletingDatabaseException(String databaseName) {
            super("Failed deleting database " + databaseName);
        }
    }

    public static class DatabaseNotFoundException extends DatabaseException {
        @Serial private static final long serialVersionUID = 1;
        public DatabaseNotFoundException(String databaseName) {
            super("Cannot find database: " + databaseName);
        }
    }
}
