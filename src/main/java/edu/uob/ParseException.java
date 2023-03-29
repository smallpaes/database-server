package edu.uob;

import java.io.Serial;

public class ParseException extends Exception {
    @Serial private static final long serialVersionUID = 1;

    public ParseException(String message) {
        super(message);
    }
}
