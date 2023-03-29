package edu.uob;

import java.util.Arrays;
import java.util.List;

public class Condition {
    protected String attributeName;
    protected String comparator;
    protected String value;
    protected String boolOperator;
    protected List<Condition> subConditions;

    public Condition(String attribute, String comparator, String value) {
        this.attributeName = attribute;
        this.comparator = comparator;
        this.value = value;
    }

    public Condition(Condition firstCondition, String operator, Condition secondCondition) {
        this.subConditions = Arrays.asList(firstCondition, secondCondition);
        this.boolOperator = operator;
    }
}
