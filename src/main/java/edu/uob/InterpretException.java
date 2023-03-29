package edu.uob;

import java.io.Serial;

public class InterpretException extends Exception {
    @Serial private static final long serialVersionUID = 1;

    public InterpretException(String message) {
        super(message);
    }

    public static class StringWithNoQuoteException extends InterpretException {
        @Serial private static final long serialVersionUID = 1;
        public StringWithNoQuoteException(String value) {
            super("String " + value + " is not wrapped by single quotations");
        }
    }

    public static class FailedCreatingFileException extends InterpretException {
        @Serial private static final long serialVersionUID = 1;
        public FailedCreatingFileException(String fileInfo) {
            super("Failed creating file: " + fileInfo);
        }
    }

    public static class FailedReadingFileException extends InterpretException {
        @Serial private static final long serialVersionUID = 1;
        public FailedReadingFileException(String fileInfo) {
            super("Failed reading file: " + fileInfo);
        }
    }
}
