package edu.uob;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokeniser {
    public Tokeniser() {}

    private String[] tokenise(String input)
    {
        String[] specialCharacters = {"(",")",",",";"};
        // Add in some extra padding spaces around the "special characters"
        // so we can be sure that they are separated by AT LEAST one space (possibly more)
        for (String specialCharacter : specialCharacters) {
            input = input.replace(specialCharacter, " " + specialCharacter + " ");
        }
        // Add in some extra padding spaces around the "special Comparator"
        Pattern pattern = Pattern.compile("==|>=|<=|>|<|!=|=");
        Matcher matcher = pattern.matcher(input);
        input = matcher.replaceAll(" $0 ");
        // Remove all double spaces (the previous replacements may have added some)
        // This is "blind" replacement - replacing if they exist, doing nothing if they don't
        while (input.contains("  ")) {
            input = input.replaceAll("  ", " ");
        }
        // Again, remove any whitespace from the beginning and end that might have been introduced
        input = input.trim();
        // Finally split on the space char (since there will now ALWAYS be a space between tokens)
        return input.split(" ");
    }

    public String[] getTokens(String query) {
        query = query.trim();
        ArrayList<String> tokens = new ArrayList<>();
        // Split the query on single quotes (to separate out query characters from string literals)
        String[] fragments = query.split("'");
        for (int i=0; i<fragments.length; i++) {
            // Every odd fragment is a string literal, so just append it without any alterations
            if (i % 2 != 0) {
                tokens.add("'" + fragments[i] + "'");
            }
            else {
                // If it's not a string literal, it must be query characters (which need further processing)
                // Tokenise the fragments into an array of strings
                String[] nextBatchOfTokens = tokenise(fragments[i]);
                // Then add these to the "result" array list (needs a bit of conversion)
                tokens.addAll(Arrays.asList(nextBatchOfTokens));
            }
        }
        return tokens.toArray(new String[0]);
    }
}
