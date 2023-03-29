package edu.uob;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import edu.uob.InterpretException.StringWithNoQuoteException;

public class ExampleDBTests {

    private DBServer server;

    // Create a new server _before_ every @Test
    @BeforeEach
    public void setup() {
        server = new DBServer();
    }

    // Random name generator - useful for testing "bare earth" queries (i.e. where tables don't previously exist)
    private String generateRandomName()
    {
        String randomName = "";
        for(int i=0; i<10 ;i++) randomName += (char)( 97 + (Math.random() * 25.0));
        return randomName;
    }

    private String sendCommandToServer(String command) {
        // Try to send a command to the server - this call will timeout if it takes too long (in case the server enters an infinite loop)
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> { return server.handleCommand(command);},
                "Server took too long to respond (probably stuck in an infinite loop)");
    }

    // A basic test that creates a database, creates a table, inserts some test data, then queries it.
    // It then checks the response to see that a couple of the entries in the table are returned as expected
    @Test
    public void testBasicCreateAndQuery() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("id"), "Id column should be given automatically after table creation, but is was not returned by SELECT *");
        assertTrue(response.contains("Steve"), "An attempt was made to add Steve to the table, but they were not returned by SELECT *");
        assertTrue(response.contains("Clive"), "An attempt was made to add Clive to the table, but they were not returned by SELECT *");
    }

    @Test
    public void testCreateDuplicateDatabase() {
        String randomName = generateRandomName();
        String response = sendCommandToServer("CREATE DATABASE " + randomName + ";");
        assertTrue(response.contains("[OK]"), "A valid create query was made, however an [OK] tag was not returned");
        response = sendCommandToServer("CREATE DATABASE " + randomName + ";");
        assertTrue(response.contains("[ERROR]"), "Trying to create a duplicate database, however an [ERROR] tag was not returned");
    }

    @Test
    public void testCreateDatabaseUsingReservedWords() {
        String response = sendCommandToServer("CREATE DATABASE like;");
        assertTrue(response.contains("[ERROR]"), "An invalid database name `like` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("CREATE DATABASE false;");
        assertTrue(response.contains("[ERROR]"), "An invalid database name `false` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("CREATE DATABASE true;");
        assertTrue(response.contains("[ERROR]"), "An invalid database name `true` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("CREATE DATABASE or;");
        assertTrue(response.contains("[ERROR]"), "An invalid database name `or` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("CREATE DATABASE and;");
        assertTrue(response.contains("[ERROR]"), "An invalid database name`and` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("CREATE DATABASE drop;");
        assertTrue(response.contains("[ERROR]"), "An invalid database name `drop` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("CREATE DATABASE database;");
        assertTrue(response.contains("[ERROR]"), "An invalid database name `database` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("CREATE DATABASE table;");
        assertTrue(response.contains("[ERROR]"), "An invalid database name `table` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("CREATE DATABASE from;");
        assertTrue(response.contains("[ERROR]"), "An invalid database name `from` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("CREATE DATABASE set;");
        assertTrue(response.contains("[ERROR]"), "An invalid database name `set` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("CREATE DATABASE values;");
        assertTrue(response.contains("[ERROR]"), "An invalid database name `values` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("CREATE DATABASE null;");
        assertTrue(response.contains("[ERROR]"), "An invalid database name `null` was used, however an [ERROR] tag was not returned");
    }

    @Test
    public void testCreateTableUsingReservedWords() {
        String response = sendCommandToServer("CREATE Table like;");
        assertTrue(response.contains("[ERROR]"), "An invalid table name `like` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("CREATE Table false;");
        assertTrue(response.contains("[ERROR]"), "An invalid table name `false` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("CREATE Table true;");
        assertTrue(response.contains("[ERROR]"), "An invalid table name `true` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("CREATE Table or;");
        assertTrue(response.contains("[ERROR]"), "An invalid table name `or` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("CREATE Table and;");
        assertTrue(response.contains("[ERROR]"), "An invalid table name`and` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("CREATE Table drop;");
        assertTrue(response.contains("[ERROR]"), "An invalid table name `drop` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("CREATE Table database;");
        assertTrue(response.contains("[ERROR]"), "An invalid table name `database` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("CREATE Table table;");
        assertTrue(response.contains("[ERROR]"), "An invalid table name `table` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("CREATE Table from;");
        assertTrue(response.contains("[ERROR]"), "An invalid table name `from` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("CREATE Table set;");
        assertTrue(response.contains("[ERROR]"), "An invalid table name `set` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("CREATE Table values;");
        assertTrue(response.contains("[ERROR]"), "An invalid table name `values` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("CREATE Table null;");
        assertTrue(response.contains("[ERROR]"), "An invalid table name `null` was used, however an [ERROR] tag was not returned");
    }

    @Test
    public void testCreateTableAndAttributesUsingReservedWords() {
        String response = sendCommandToServer("CREATE TABLE marks (like, mark, pass);");
        assertTrue(response.contains("[ERROR]"), "An invalid table name `like` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("CREATE TABLE marks (true, mark, pass);");
        assertTrue(response.contains("[ERROR]"), "An invalid table name `true` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("CREATE TABLE marks (false, mark, pass);");
        assertTrue(response.contains("[ERROR]"), "An invalid table name `false` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("CREATE TABLE marks (and, mark, pass);");
        assertTrue(response.contains("[ERROR]"), "An invalid table name `and` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("CREATE TABLE marks (or, mark, pass);");
        assertTrue(response.contains("[ERROR]"), "An invalid table name `or` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("CREATE TABLE marks (drop, mark, pass);");
        assertTrue(response.contains("[ERROR]"), "An invalid table name `drop` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("CREATE TABLE marks (database, mark, pass);");
        assertTrue(response.contains("[ERROR]"), "An invalid table name `database` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("CREATE TABLE marks (table, mark, pass);");
        assertTrue(response.contains("[ERROR]"), "An invalid table name `table` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("CREATE TABLE marks (from, mark, pass);");
        assertTrue(response.contains("[ERROR]"), "An invalid table name `from` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("CREATE TABLE marks (set, mark, pass);");
        assertTrue(response.contains("[ERROR]"), "An invalid table name `set` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("CREATE TABLE marks (values, mark, pass);");
        assertTrue(response.contains("[ERROR]"), "An invalid table name `values` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("CREATE TABLE marks (null, mark, pass);");
        assertTrue(response.contains("[ERROR]"), "An invalid table name `null` was used, however an [ERROR] tag was not returned");
    }

    @Test
    public void testCreateAndQueryWithExtraSpace() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE  " + randomName + ";");
        String  response = sendCommandToServer("USE " + randomName + ";");
        assertTrue(response.contains("[OK]"), "A valid use query was made, however an [OK] tag was not returned");
        response = sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        assertTrue(response.contains("[OK]"), "A valid create table query was made, however an [OK] tag was not returned");
        response = sendCommandToServer("INSERT INTO  marks VALUES('Steve',  65, TRUE);");
        assertTrue(response.contains("[OK]"), "A valid insert query was made, however an [OK] tag was not returned");
        response = sendCommandToServer("INSERT INTO   marks VALUES ('Dave'  , 55,  TRUE);");
        assertTrue(response.contains("[OK]"), "A valid insert query was made, however an [OK] tag was not returned");
        response = sendCommandToServer("INSERT INTO    marks VALUES('Bob',35,FALSE);");
        assertTrue(response.contains("[OK]"), "A valid insert query was made, however an [OK] tag was not returned");
        response = sendCommandToServer("INSERT INTO     marks VALUES ( 'Clive' , 20, FALSE);");
        assertTrue(response.contains("[OK]"), "A valid insert query was made, however an [OK] tag was not returned");
        response = sendCommandToServer("SELECT   * FROM   marks;");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("Steve"), "An attempt was made to add Steve to the table, but they were not returned by SELECT *");
        assertTrue(response.contains("Clive"), "An attempt was made to add Clive to the table, but they were not returned by SELECT *");
        assertTrue(response.contains("Dave"), "An attempt was made to add Dave to the table, but they were not returned by SELECT *");
        assertTrue(response.contains("Bob"), "An attempt was made to add Bob to the table, but they were not returned by SELECT *");
    }

    @Test
    public void testBasicCreateTableNoAttribute() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("CREATE TABLE marks;");
        assertTrue(response.contains("[OK]"), "A valid create table query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid create table query was made, however an [ERROR] tag was returned");
        response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("id"), "An attempt was made to query newly created table without given attributes, but id was not returned by SELECT *");
    }

    @Test
    public void testCreateTableWithTableNameAttributeAndQuery() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("CREATE TABLE marks (marks.name, marks.mark, marks.pass);");
        assertTrue(response.contains("[OK]"), "A valid create table query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid create table query was made, however an [ERROR] tag was returned");
        response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("name"), "A valid select query after creating tables with attribute was made, however the attribute `name` was not returned");
        assertTrue(response.contains("mark"), "A valid select query after creating tables with attribute was made, however the attribute `mark` was not returned");
        assertTrue(response.contains("pass"), "A valid select query after creating tables with attribute was made, however the attribute `pass` was not returned");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");
        response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("Steve"), "An attempt was made to add Steve to the table, but they were not returned by SELECT *");
        assertTrue(response.contains("Clive"), "An attempt was made to add Clive to the table, but they were not returned by SELECT *");
    }

    @Test
    public void testCreateTableWithDuplicateAttributeAndQuery() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("CREATE TABLE marks (marks.name, marks.mark, marks.pass, name);");
        assertTrue(response.contains("[ERROR]"), "Creating a table with duplicate attributes, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "Creating a table with duplicate attributes, however an [OK] tag was returned");
    }

    @Test
    public void testCreateTableWithAttributeMissingClosingBracket() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("CREATE TABLE marks (marks.name, marks.mark, marks.pass;");
        assertTrue(response.contains("[ERROR]"), "Creating table with attribute but lack of closing parenthesis, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "ACreating table with attribute but lack of closing parenthesis, however an [OK] tag was returned");
    }

    @Test
    public void testCreateExistingDatabase() {
        String randomName = generateRandomName();
        String response = sendCommandToServer("CREATE DATABASE " + randomName + ";");
        assertTrue(response.contains("[OK]"), "A valid create database query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid create database query was made, however an [ERROR] tag was returned");
        sendCommandToServer("USE " + randomName + ";");
        response = sendCommandToServer("CREATE DATABASE " + randomName + ";");
        assertTrue(response.contains("[ERROR]"), "Creating an existing database, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "Creating an existing database, however an [OK] tag was returned");
    }

    @Test
    public void testCreateExistingTable() {
        String randomName = generateRandomName();
        String response = sendCommandToServer("CREATE DATABASE " + randomName + ";");
        assertTrue(response.contains("[OK]"), "A valid create database query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid create database query was made, however an [ERROR] tag was returned");
        sendCommandToServer("USE " + randomName + ";");
        response = sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        assertTrue(response.contains("[OK]"), "A valid create table query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid create table query was made, however an [ERROR] tag was returned");
        response = sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        assertTrue(response.contains("[ERROR]"), "Creating an existing table, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "Creating an existing table, however an [OK] tag was returned");
    }

    @Test
    public void testCreateWithInvalidToken() {
        String randomName = generateRandomName();
        String response = sendCommandToServer("CREATE DROP " + randomName + ";");
        assertTrue(response.contains("[ERROR]"), "An invalid create database query was made with DROP, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An invalid create database query was made with DROP, however an [OK] tag was returned");
    }

    @Test
    public void testCreateAndQueryWithCaseInsensitiveKeyword() {
        String randomName = generateRandomName();
        String response = sendCommandToServer("CrEAte daTaBaSe " + randomName + ";");
        assertTrue(response.contains("[OK]"), "A valid create database query was made, however an [OK] tag was not returned");
        response = sendCommandToServer("use " + randomName + ";");
        assertTrue(response.contains("[OK]"), "A valid use database query was made, however an [OK] tag was not returned");
        response = sendCommandToServer("CreatE Table marks (name, mark, pass);");
        assertTrue(response.contains("[OK]"), "A valid create table query was made, however an [OK] tag was not returned");
        response = sendCommandToServer("inserT Into marks values ('Steve', 65, TRUE);");
        assertTrue(response.contains("[OK]"), "A valid insert query was made, however an [OK] tag was not returned");
        response = sendCommandToServer("insert INTO marks VALUES ('Dave', 55, TRUE);");
        assertTrue(response.contains("[OK]"), "A valid insert query was made, however an [OK] tag was not returned");
        response = sendCommandToServer("inserT INTO marks ValuES ('Bob', 35, FALSE);");
        assertTrue(response.contains("[OK]"), "A valid insert query was made, however an [OK] tag was not returned");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");
        response = sendCommandToServer("SeLeCt * From marks;");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("id"), "Id column should be given automatically after table creation, but is was not returned by SELECT *");
        assertTrue(response.contains("Steve"), "An attempt was made to add Steve to the table, but they were not returned by SELECT *");
        assertTrue(response.contains("Clive"), "An attempt was made to add Clive to the table, but they were not returned by SELECT *");
    }

    // USE
    @Test
    public void testUseNonExistingDatabase() {
        String randomName = generateRandomName();
        String response = sendCommandToServer("USE " + randomName + ";");
        assertTrue(response.contains("[ERROR]"), "Using a non-existing database, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "Using a non-existing database, however an [OK] tag was returned");
    }

    @Test
    public void testUseDatabaseWithTableHasNoConfig() {
        String randomName = generateRandomName();
        String response = sendCommandToServer("USE " + randomName + ";");
        assertTrue(response.contains("[ERROR]"), "Using a non-existing database, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "Using a non-existing database, however an [OK] tag was returned");
    }

    // SELECT
    @Test
    public void testSelectWithMultipleAttributes() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        String response = sendCommandToServer("SELECT name, pass FROM marks;");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("name"), "An attempt was made to get data from the table with defined columns, but they were not returned by SELECT name, pass FROM marks;");
        assertTrue(response.contains("pass"), "An attempt was made to get data from the table with defined columns, but they were not returned by SELECT name, pass FROM marks;");
        assertTrue(!response.contains("mark"), "An attempt was made to get data from the table with defined columns, but returned columns not needed by SELECT name, pass FROM marks;");
    }

    @Test
    public void testSelectWithReservedWordsAttributeName() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        String response = sendCommandToServer("SELECT table, pass FROM marks;");
        assertTrue(response.contains("[ERROR]"), "An invalid query was made using reserved word `table` as column name, however an [ERROR] tag was not returned");
        response = sendCommandToServer("SELECT name, FALSE FROM marks;");
        assertTrue(response.contains("[ERROR]"), "An invalid query was made using reserved word `FALSE` as column name, however an [ERROR] tag was not returned");
    }

    @Test
    public void testSelectFromTableWithReservedWords() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        String response = sendCommandToServer("SELECT mark, pass FROM FALSE;");
        assertTrue(response.contains("[ERROR]"), "An invalid query was made use reserved word `table` as table name, however an [ERROR] tag was not returned");
    }

    @Test
    public void testSelectWithMultipleAttributesWithSpace() {
        String randomName = generateRandomName();
        String response = sendCommandToServer("CREATE DATABASE " + randomName + ";");
        assertTrue(response.contains("[OK]"), "A valid create query was made, however an [OK] tag was not returned");
        response = sendCommandToServer("USE " + randomName + ";");
        assertTrue(response.contains("[OK]"), "A valid use query was made, however an [OK] tag was not returned");
        response = sendCommandToServer("CREATE TABLE  marks  ( name,  mark,  pass  );");
        assertTrue(response.contains("[OK]"), "A valid create table query was made, however an [OK] tag was not returned");
        response = sendCommandToServer("INSERT INTO marks VALUES ('Steve' , 65,  TRUE);");
        assertTrue(response.contains("[OK]"), "A valid insert query was made, however an [OK] tag was not returned");
        response = sendCommandToServer("SELECT name  ,   pass  FROM       marks;");
        assertTrue(response.contains("[OK]"), "A valid select query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("name"), "An attempt was made to get data from the table with defined columns, but they were not returned by SELECT name, pass FROM marks;");
        assertTrue(response.contains("pass"), "An attempt was made to get data from the table with defined columns, but they were not returned by SELECT name, pass FROM marks;");
        assertTrue(response.contains("Steve"), "An attempt was made to get data from the table with defined columns, but they were not returned by SELECT name, pass FROM marks;");
        assertTrue(response.contains("TRUE"), "An attempt was made to get data from the table with defined columns, but they were not returned by SELECT name, pass FROM marks;");
        assertTrue(!response.contains("mark"), "An attempt was made to get data from the table with defined columns, but returned columns not needed by SELECT name, pass FROM marks;");
    }

    @Test
    public void testSelectWithTableColumnName() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        String response = sendCommandToServer("SELECT marks.name, marks.pass FROM marks;");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("name"), "An attempt was made to get data from the table with defined columns, but they were not returned by SELECT name, pass FROM marks;");
        assertTrue(response.contains("pass"), "An attempt was made to get data from the table with defined columns, but they were not returned by SELECT name, pass FROM marks;");
        assertTrue(!response.contains("mark"), "An attempt was made to get data from the table with defined columns, but returned columns not needed by SELECT name, pass FROM marks;");
    }

    @Test
    public void testSelectWithInvalidToken() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        String response = sendCommandToServer("SELECT name, pass ON marks;");

        assertTrue(response.contains("[ERROR]"), "An invalid token `ON`, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An invalid token `ON`, however an [OK] tag was returned");
    }

    @Test
    public void testSelectWithConditions() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass, class);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE, 'B');");
        sendCommandToServer("INSERT INTO marks VALUES ('Mike', 32, TRUE, 'A');");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE, 'B');");
        sendCommandToServer("INSERT INTO marks VALUES ('Tim', 35, TRUE, 'A');");
        sendCommandToServer("INSERT INTO marks VALUES ('John', 45, FALSE, 'C');");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE, 'A');");
        sendCommandToServer("INSERT INTO marks VALUES ('Jane', 55.4, FALSE, 'C');");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE, NULL);");
        sendCommandToServer("INSERT INTO marks VALUES ('Henry', 65, FALSE, 'C');");

        String  response = sendCommandToServer("SELECT * FROM marks WHERE (mark <= 20);");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("Clive"), "An attempt was made to get data, but they were not returned by SELECT * FROM marks WHERE (mark <= 20);");
        assertFalse(response.contains("Mike"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (mark <= 20);");

        response = sendCommandToServer("SELECT * FROM marks WHERE (mark < 21);");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("Clive"), "An attempt was made to get data, but they were not returned by SELECT * FROM marks WHERE (mark <= 20);");
        assertFalse(response.contains("Mike"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (mark <= 20);");

        response = sendCommandToServer("SELECT * FROM marks WHERE (mark >= 35 AND class == 'C' AND (name != 'Henry'));");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("John"), "An attempt was made to get data, but they were not returned by SELECT * FROM marks WHERE (mark >= 35 AND class == 'C' AND (name != 'Henry'));");
        assertFalse(response.contains("Henry"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (mark >= 35 AND class == 'C' AND (name != 'Henry'));");

        response = sendCommandToServer("SELECT * FROM marks WHERE (mark == 65 AND class == 'C');");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("Henry"), "An attempt was made to get data, but they were not returned by SELECT * FROM marks WHERE (mark == 65 AND class == 'C')");
        assertFalse(response.contains("Steve"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (mark == 65 AND class == 'C')");

        response = sendCommandToServer("SELECT * FROM marks WHERE ((mark > 34 OR pass == FALSE) AND (class == 'B'));");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("Bob"), "An attempt was made to get data, but they were not returned by SELECT * FROM marks WHERE ((mark > 34 OR pass == FALSE) AND class == 'B');");
        assertTrue(response.contains("Clive"), "An attempt was made to get data, but they were not returned by SELECT * FROM marks WHERE ((mark > 34 OR pass == FALSE) AND class == 'B');");
        assertFalse(response.contains("Henry"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE ((mark > 34 OR pass == FALSE) AND class == 'B');");
        assertFalse(response.contains("Steve"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE ((mark > 34 OR pass == FALSE) AND class == 'B');");
        assertFalse(response.contains("Tim"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE ((mark > 34 OR pass == FALSE) AND class == 'B');");
        assertFalse(response.contains("Dave"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE ((mark > 34 OR pass == FALSE) AND class == 'B');");
        assertFalse(response.contains("Mike"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE ((mark > 34 OR pass == FALSE) AND class == 'B');");
        assertFalse(response.contains("John"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE ((mark > 34 OR pass == FALSE) AND class == 'B');");

        response = sendCommandToServer("SELECT * FROM marks WHERE (mark > 34 AND (class == 'B' OR pass == FALSE));");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("Bob"), "An attempt was made to get data, but they were not returned by SELECT * FROM marks WHERE (mark > 34 AND (class == 'B' OR pass == FALSE));");
        assertTrue(response.contains("John"), "An attempt was made to get data, but they were not returned by SELECT * FROM marks WHERE (mark > 34 AND (class == 'B' OR pass == FALSE));");
        assertTrue(response.contains("Henry"), "An attempt was made to get data, but they were not returned by SELECT * FROM marks WHERE (mark > 34 AND (class == 'B' OR pass == FALSE));");
        assertFalse(response.contains("Steve"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (mark > 34 AND (class == 'B' OR pass == FALSE));");
        assertFalse(response.contains("Tim"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (mark > 34 AND (class == 'B' OR pass == FALSE));");
        assertFalse(response.contains("Dave"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (mark > 34 AND (class == 'B' OR pass == FALSE));");
        assertFalse(response.contains("Mike"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (mark > 34 AND (class == 'B' OR pass == FALSE));");
        assertFalse(response.contains("Clive"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (mark > 34 AND (class == 'B' OR pass == FALSE));");

        response = sendCommandToServer("SELECT * FROM marks WHERE (mark > 34 AND (class == 'B' OR class == NULL));");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("Bob"), "An attempt was made to get data, but they were not returned by SELECT * FROM marks WHERE (mark > 34 AND (class == 'B' OR class == NULL));");
        assertTrue(response.contains("Steve"), "An attempt was made to get data, but they were not returned by SELECT * FROM marks WHERE (mark > 34 AND (class == 'B' OR class == NULL));");
        assertFalse(response.contains("Tim"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (mark > 34 AND (class == 'B' OR class == NULL));");
        assertFalse(response.contains("John"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (mark > 34 AND (class == 'B' OR class == NULL));");
        assertFalse(response.contains("Dave"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (mark > 34 AND (class == 'B' OR class == NULL));");
        assertFalse(response.contains("Henry"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (mark > 34 AND (class == 'B' OR class == NULL));");

        response = sendCommandToServer("SELECT * FROM marks WHERE ((mark > 31.8 AND mark <= 55.4) AND (class == 'B' OR pass == FALSE));");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("Bob"), "An attempt was made to get data, but they were not returned by SELECT * FROM marks WHERE (mark > 34 AND (class == 'B' OR class == NULL));");
        assertTrue(response.contains("John"), "An attempt was made to get data, but they were not returned by SELECT * FROM marks WHERE (mark > 34 AND (class == 'B' OR class == NULL));");
        assertTrue(response.contains("Jane"), "An attempt was made to get data, but they were not returned by SELECT * FROM marks WHERE (mark > 34 AND (class == 'B' OR class == NULL));");
        assertFalse(response.contains("Tim"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (mark > 34 AND (class == 'B' OR class == NULL));");
        assertFalse(response.contains("Dave"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (mark > 34 AND (class == 'B' OR class == NULL));");
        assertFalse(response.contains("Henry"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (mark > 34 AND (class == 'B' OR class == NULL));");

        response = sendCommandToServer("SELECT * FROM marks WHERE ((mark > +31.8 AND mark <= +55.4) AND (class == 'B' OR pass == falSe));");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("Bob"), "An attempt was made to get data, but they were not returned by SELECT * FROM marks WHERE (mark > 34 AND (class == 'B' OR class == NULL));");
        assertTrue(response.contains("John"), "An attempt was made to get data, but they were not returned by SELECT * FROM marks WHERE (mark > 34 AND (class == 'B' OR class == NULL));");
        assertTrue(response.contains("Jane"), "An attempt was made to get data, but they were not returned by SELECT * FROM marks WHERE (mark > 34 AND (class == 'B' OR class == NULL));");
        assertFalse(response.contains("Tim"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (mark > 34 AND (class == 'B' OR class == NULL));");
        assertFalse(response.contains("Dave"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (mark > 34 AND (class == 'B' OR class == NULL));");
        assertFalse(response.contains("Henry"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (mark > 34 AND (class == 'B' OR class == NULL));");
    }

    @Test
    public void testSelectWithConditionsNoReturnData() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass, class);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE, 'B');");
        sendCommandToServer("INSERT INTO marks VALUES ('Mike', 32, TRUE, 'A');");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE, 'B');");
        sendCommandToServer("INSERT INTO marks VALUES ('Tim', 35, TRUE, 'A');");
        sendCommandToServer("INSERT INTO marks VALUES ('John', 45, FALSE, 'C');");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE, 'A');");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE, NULL);");
        sendCommandToServer("INSERT INTO marks VALUES ('Henry', 65, FALSE, 'C');");

        String response = sendCommandToServer("SELECT * FROM marks WHERE (pass > TRUE);");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertFalse(response.contains("Clive"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (pass > TRUE);");
        assertFalse(response.contains("Mike"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (pass > TRUE);");
        assertFalse(response.contains("Bob"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (pass > TRUE);");
        assertFalse(response.contains("Tim"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (pass > TRUE);");
        assertFalse(response.contains("John"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (pass > TRUE);");
        assertFalse(response.contains("Dave"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (pass > TRUE);");

        response = sendCommandToServer("SELECT * FROM marks WHERE (mark <= 'Mike');");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertFalse(response.contains("Clive"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (pass > TRUE);");
        assertFalse(response.contains("Mike"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (pass > TRUE);");
        assertFalse(response.contains("Bob"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (pass > TRUE);");
        assertFalse(response.contains("Tim"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (pass > TRUE);");
        assertFalse(response.contains("John"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (pass > TRUE);");
        assertFalse(response.contains("Dave"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (pass > TRUE);");

        response = sendCommandToServer("SELECT * FROM marks WHERE (name <= 2);");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertFalse(response.contains("Clive"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (pass > TRUE);");
        assertFalse(response.contains("Mike"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (pass > TRUE);");
        assertFalse(response.contains("Bob"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (pass > TRUE);");
        assertFalse(response.contains("Tim"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (pass > TRUE);");
        assertFalse(response.contains("John"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (pass > TRUE);");
        assertFalse(response.contains("Dave"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (pass > TRUE);");

        response = sendCommandToServer("SELECT * FROM marks WHERE (name > NULL);");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertFalse(response.contains("Clive"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (pass > TRUE);");
        assertFalse(response.contains("Mike"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (pass > TRUE);");
        assertFalse(response.contains("Bob"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (pass > TRUE);");
        assertFalse(response.contains("Tim"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (pass > TRUE);");
        assertFalse(response.contains("John"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (pass > TRUE);");
        assertFalse(response.contains("Dave"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (pass > TRUE);");

        response = sendCommandToServer("SELECT * FROM marks WHERE (mark LIKE 2);");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertFalse(response.contains("Clive"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (pass > TRUE);");
        assertFalse(response.contains("Mike"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (pass > TRUE);");
        assertFalse(response.contains("Bob"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (pass > TRUE);");
        assertFalse(response.contains("Tim"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (pass > TRUE);");
        assertFalse(response.contains("John"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (pass > TRUE);");
        assertFalse(response.contains("Dave"), "An attempt was made to get data, but got unmatched data by SELECT * FROM marks WHERE (pass > TRUE);");
    }

    // ALTER TABLE: ADD
    @Test
    public void testBasicAlterAddColumn() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertTrue(response.contains("Bob"), "An attempt was made to add Bob to the table, but they were not returned by SELECT * FROM marks;");
        response = sendCommandToServer("ALTER TABLE marks ADD isAbsent;");
        assertTrue(response.contains("[OK]"), "A valid alter query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid alter query was made, however an [ERROR] tag was returned");
        response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("NULL"), "An attempt was made to get data, but NULL was not returned by SELECT * FROM marks;");
        response = sendCommandToServer("SELECT * FROM marks WHERE isAbsent == null;");
        assertTrue(response.contains("Bob"), "An attempt was made to get data, but Bob was not returned by SELECT * FROM marks;");
        response = sendCommandToServer("SELECT * FROM marks WHERE isAbsent == NULL;");
        assertTrue(response.contains("Bob"), "An attempt was made to get data, but Bob was not returned by SELECT * FROM marks;");
        response = sendCommandToServer("INSERT INTO marks VALUES ('John', 35, TRUE);");
        assertTrue(response.contains("[ERROR]"), "Expected an [ERROR] tag to be returned while inserting a new row without newly added column");
        assertFalse(response.contains("[OK]"), "Inserted too less values, however an [OK] tag was returned");
        response = sendCommandToServer("INSERT INTO marks VALUES ('John', 35, TRUE, FALSE);");
        assertTrue(response.contains("[OK]"), "Expect an [OK] tag to be returned while inserting a new row after a new column is added");
        response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("John"), "An attempt was made to add John to the table, but they were not returned by SELECT * FROM marks;");
    }

    @Test
    public void testAlterAddColumnUsingReservedWords() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        String response = sendCommandToServer("ALTER TABLE marks ADD like;");
        assertTrue(response.contains("[ERROR]"), "An invalid column name `like` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("ALTER TABLE marks ADD true;");
        assertTrue(response.contains("[ERROR]"), "An invalid column name `true` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("ALTER TABLE marks ADD false;");
        assertTrue(response.contains("[ERROR]"), "An invalid column name `false` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("ALTER TABLE marks ADD and;");
        assertTrue(response.contains("[ERROR]"), "An invalid column name `and` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("ALTER TABLE marks ADD or;");
        assertTrue(response.contains("[ERROR]"), "An invalid column name `or` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("ALTER TABLE marks ADD drop;");
        assertTrue(response.contains("[ERROR]"), "An invalid column name `drop` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("ALTER TABLE marks ADD database;");
        assertTrue(response.contains("[ERROR]"), "An invalid column name `database` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("ALTER TABLE marks ADD table;");
        assertTrue(response.contains("[ERROR]"), "An invalid column name `table` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("ALTER TABLE marks ADD from;");
        assertTrue(response.contains("[ERROR]"), "An invalid column name `from` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("ALTER TABLE marks ADD set;");
        assertTrue(response.contains("[ERROR]"), "An invalid column name `set` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("ALTER TABLE marks ADD values;");
        assertTrue(response.contains("[ERROR]"), "An invalid column name `values` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("ALTER TABLE marks ADD null;");
        assertTrue(response.contains("[ERROR]"), "An invalid column name `null` was used, however an [ERROR] tag was not returned");
        response = sendCommandToServer("ALTER TABLE null ADD isAbsent;");
        assertTrue(response.contains("[ERROR]"), "An invalid table name `null` was used, however an [ERROR] tag was not returned");
    }

    @Test
    public void testAlterAddColumnWithCaseInsensitiveKeywords() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertTrue(response.contains("Bob"), "An attempt was made to add Bob to the table, but they were not returned by SELECT *");
        response = sendCommandToServer("ALTER TABLE marks ADD isAbsent;");
        assertTrue(response.contains("[OK]"), "A valid alter query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid alter query was made, however an [ERROR] tag was returned");
        response = sendCommandToServer("inserT IntO marks values ('John', 35, TRUE);");
        assertTrue(response.contains("[ERROR]"), "Expected an [ERROR] tag to be returned while inserting a new row without newly added column");
        assertFalse(response.contains("[OK]"), "Inserted too less values, however an [OK] tag was returned");
        response = sendCommandToServer("insert into marks VaLUEs ('John', 35, TRUE, FALSE);");
        assertTrue(response.contains("[OK]"), "Expect an [OK] tag to be returned while inserting a new row after a new column is added");
        response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("John"), "An attempt was made to add John to the table, but they were not returned by SELECT *");
    }

    @Test
    public void testAlterAddTableColumn() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertTrue(response.contains("Bob"), "An attempt was made to add Bob to the table, but they were not returned by SELECT *");
        response = sendCommandToServer("ALTER TABLE marks ADD marks.isAbsent;");
        assertTrue(response.contains("[OK]"), "A valid alter query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid alter query was made, however an [ERROR] tag was returned");
        response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("isAbsent"), "A valid select query after creating tables with attribute was made, however the attribute `isAbsent` was not returned");
        response = sendCommandToServer("INSERT INTO marks VALUES ('John', 35, TRUE);");
        assertTrue(response.contains("[ERROR]"), "Expected an [ERROR] tag to be returned while inserting a new row without newly added column");
        assertFalse(response.contains("[OK]"), "Inserted too less values, however an [OK] tag was returned");
        response = sendCommandToServer("INSERT INTO marks VALUES ('John', 35, TRUE, FALSE);");
        assertTrue(response.contains("[OK]"), "Expect an [OK] tag to be returned while inserting a new row after a new column is added");
        response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("John"), "An attempt was made to add John to the table, but they were not returned by SELECT *");
    }

    @Test
    public void testAlterAddTableColumnWithSpace() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertTrue(response.contains("Bob"), "An attempt was made to add Bob to the table, but they were not returned by SELECT *");
        response = sendCommandToServer("ALTER    TABLE      marks     ADD         marks.isAbsent;");
        assertTrue(response.contains("[OK]"), "A valid alter query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid alter query was made, however an [ERROR] tag was returned");
        response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("isAbsent"), "A valid select query after creating tables with attribute was made, however the attribute `isAbsent` was not returned");
        response = sendCommandToServer("INSERT INTO marks VALUES ('John', 35, TRUE);");
        assertTrue(response.contains("[ERROR]"), "Expected an [ERROR] tag to be returned while inserting a new row without newly added column");
        assertFalse(response.contains("[OK]"), "Inserted too less values, however an [OK] tag was returned");
        response = sendCommandToServer("INSERT    INTO    marks     VALUES     (  'John'  ,   35, TRUE,    FALSE  );");
        assertTrue(response.contains("[OK]"), "Expect an [OK] tag to be returned while inserting a new row after a new column is added");
        response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("John"), "An attempt was made to add John to the table, but they were not returned by SELECT *");
    }

    @Test
    public void testAlterLowercaseAddColumn() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertTrue(response.contains("Bob"), "An attempt was made to add Bob to the table, but they were not returned by SELECT *");
        response = sendCommandToServer("alter table marks add isAbsent;");
        assertTrue(response.contains("[OK]"), "A valid alter query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid alter query was made, however an [ERROR] tag was returned");
    }

    @Test
    public void testAlterAddExistingColumn() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[OK]"), "A valid select query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid select query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("Bob"), "An attempt was made to add Bob to the table, but they were not returned by SELECT *");
        response = sendCommandToServer("ALTER TABLE marks ADD mark;");
        assertTrue(response.contains("[ERROR]"), "Expected an [ERROR] tag to be returned while inserting an existing column");
        assertFalse(response.contains("[OK]"), "Inserted an existing column, however an [OK] tag was returned");
    }

    @Test
    public void testAlterUsingInvalidTokens() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        String response = sendCommandToServer("ALTER ON marks ADD mark;");
        assertTrue(response.contains("[ERROR]"), "Expected an [ERROR] tag to be returned while using an invalid token `ON`");
        assertFalse(response.contains("[OK]"), "Using an invalid token `ON`, however an [OK] tag was returned");
    }

    // A test to make sure that databases can be reopened after server restart
    @Test
    public void testAlterAddCamelCaseColumnSavedCorrectly() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertTrue(response.contains("Bob"), "An attempt was made to add Bob to the table, but they were not returned by SELECT *");
        response = sendCommandToServer("ALTER TABLE marks ADD isAbsent;");
        assertTrue(response.contains("[OK]"), "A valid alter query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid alter query was made, however an [ERROR] tag was returned");

        // Create a new server object
        server = new DBServer();
        sendCommandToServer("USE " + randomName + ";");
        response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("isAbsent"), "Column `isAbsent` was added to a table and the server restarted - but incorrect column name was returned by SELECT *");
    }

    @Test
    public void testAlterAddColumnToNonExistingTable() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[OK]"), "A valid select query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid select query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("Bob"), "An attempt was made to add Bob to the table, but they were not returned by SELECT *");
        response = sendCommandToServer("ALTER TABLE test ADD mark;");
        assertTrue(response.contains("[ERROR]"), "Expected an [ERROR] tag to be returned while adding a column to a non-existing table");
        assertFalse(response.contains("[OK]"), "Inserted a column to a non-existing table, however an [OK] tag was returned");
    }

    // ALTER TABLE: DROP
    @Test
    public void testBasicAlterDropColumn() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertTrue(response.contains("Bob"), "An attempt was made to add Bob to the table, but they were not returned by SELECT *");
        response = sendCommandToServer("ALTER TABLE marks DROP mark;");
        assertTrue(response.contains("[OK]"), "A valid alter query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid alter query was made, however an [ERROR] tag was returned");
        response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertTrue(response.contains("Bob"), "An attempt was made to drop mark from the table, but name value were not returned by SELECT *");
        assertTrue(!response.contains("35"), "An attempt was made to drop mark from the table, but mark value were returned by SELECT *");
    }

    @Test
    public void testAlterDropColumnWithCaseInsensitiveKeywords() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        String response = sendCommandToServer("Select * from marks;");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertTrue(response.contains("Bob"), "An attempt was made to add Bob to the table, but they were not returned by SELECT *");
        response = sendCommandToServer("Alter Table marks drop mark;");
        assertTrue(response.contains("[OK]"), "A valid alter query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid alter query was made, however an [ERROR] tag was returned");
        response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertTrue(response.contains("Bob"), "An attempt was made to drop mark from the table, but name value were not returned by SELECT *");
        assertTrue(!response.contains("35"), "An attempt was made to drop mark from the table, but mark value were returned by SELECT *");
    }

    @Test
    public void testAlterDropNonExistingColumn() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[OK]"), "A valid select query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid select query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("Bob"), "An attempt was made to add Bob to the table, but they were not returned by SELECT *");
        response = sendCommandToServer("ALTER TABLE marks DROP isAbsent;");
        assertTrue(response.contains("[ERROR]"), "Expected an [ERROR] tag to be returned while dropping a non-existing column");
        assertFalse(response.contains("[OK]"), "Dropping a non-existing column, however an [OK] tag was returned");
    }

    @Test
    public void testAlterDropIdColumn() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[OK]"), "A valid select query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid select query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("Bob"), "An attempt was made to add Bob to the table, but they were not returned by SELECT *");
        response = sendCommandToServer("ALTER TABLE marks DROP id;");
        assertTrue(response.contains("[ERROR]"), "Expected an [ERROR] tag to be returned while dropping id column");
        assertFalse(response.contains("[OK]"), "Dropping id column, however an [OK] tag was returned");
    }

    @Test
    public void testAlterDropColumnToNonExistingTable() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[OK]"), "A valid select query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid select query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("Bob"), "An attempt was made to add Bob to the table, but they were not returned by SELECT *");
        response = sendCommandToServer("ALTER TABLE test DROP mark;");
        assertTrue(response.contains("[ERROR]"), "Expected an [ERROR] tag to be returned while dropping a column to a non-existing table");
        assertFalse(response.contains("[OK]"), "Dropped a column to a non-existing table, however an [OK] tag was returned");
    }

    @Test
    public void testAlterUsingInvalidType() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[OK]"), "A valid select query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid select query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("Bob"), "An attempt was made to add Bob to the table, but they were not returned by SELECT *");
        response = sendCommandToServer("ALTER TABLE marks DELETE mark;");
        assertTrue(response.contains("[ERROR]"), "Expected an [ERROR] tag to be returned while using wrong alteration type");
    }

    // DELETE
    @Test
    public void testBasicDelete() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");

        String response = sendCommandToServer("DELETE FROM marks WHERE name == 'Dave';");
        assertTrue(response.contains("[OK]"), "A valid delete query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid delete query was made, however an [ERROR] tag was returned");
        response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("Steve"), "An attempt was made to delete Dave to the table, but Steve was not returned by SELECT *");
        assertTrue(response.contains("Clive"), "An attempt was made to delete Dave to the table, but Clive was not returned by SELECT *");
        assertTrue(response.contains("Bob"), "An attempt was made to delete Dave to the table, but Bob was not returned by SELECT *");
        assertTrue(!response.contains("Dave"), "An attempt was made to delete Dave to the table, but Dave was returned by SELECT *");
    }

    @Test
    public void testDeleteWithCaseInsensitiveKeyword() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");

        String response = sendCommandToServer("deletE from marks WherE name == 'Dave';");
        assertTrue(response.contains("[OK]"), "A valid delete query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid delete query was made, however an [ERROR] tag was returned");
        response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("Steve"), "An attempt was made to delete Dave to the table, but Steve was not returned by SELECT *");
        assertTrue(response.contains("Clive"), "An attempt was made to delete Dave to the table, but Clive was not returned by SELECT *");
        assertTrue(response.contains("Bob"), "An attempt was made to delete Dave to the table, but Bob was not returned by SELECT *");
        assertTrue(!response.contains("Dave"), "An attempt was made to delete Dave to the table, but Dave was returned by SELECT *");
    }

    @Test
    public void testDeleteWithMultipleMatches() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");

        String response = sendCommandToServer("DELETE FROM marks WHERE mark >= 35;");
        assertTrue(response.contains("[OK]"), "A valid delete query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid delete query was made, however an [ERROR] tag was returned");

        response = sendCommandToServer("DELETE FROM marks WHERE (mark >= 35);");
        assertTrue(response.contains("[OK]"), "A valid delete query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid delete query was made, however an [ERROR] tag was returned");

        response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(!response.contains("Steve"), "An attempt was made to delete data with mark >= 35 to the table, but Steve was returned by SELECT *");
        assertTrue(response.contains("Clive"), "An attempt was made to delete data with mark >= 35 to the table, but Clive was not returned by SELECT *");
        assertTrue(!response.contains("Bob"), "An attempt was made to delete data with mark >= 35 to the table, but Bob was returned by SELECT *");
        assertTrue(!response.contains("Dave"), "An attempt was made to delete data with mark >= 35 to the table, but Dave was returned by SELECT *");
    }

    @Test
    public void testDeleteWithMultipleMatchesWithSpace() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");

        String response = sendCommandToServer("DELETE FROM marks WHERE mark >= 35;");
        assertTrue(response.contains("[OK]"), "A valid delete query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid delete query was made, however an [ERROR] tag was returned");

        response = sendCommandToServer("DELETE   FROM     marks      WHERE    (    mark    >=     35   )   ;");
        assertTrue(response.contains("[OK]"), "A valid delete query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid delete query was made, however an [ERROR] tag was returned");

        response = sendCommandToServer("DELETE FROM marks WHERE mark>=35;");
        assertTrue(response.contains("[OK]"), "A valid delete query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid delete query was made, however an [ERROR] tag was returned");

        response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(!response.contains("Steve"), "An attempt was made to delete data with mark >= 35 to the table, but Steve was returned by SELECT *");
        assertTrue(response.contains("Clive"), "An attempt was made to delete data with mark >= 35 to the table, but Clive was not returned by SELECT *");
        assertTrue(!response.contains("Bob"), "An attempt was made to delete data with mark >= 35 to the table, but Bob was returned by SELECT *");
        assertTrue(!response.contains("Dave"), "An attempt was made to delete data with mark >= 35 to the table, but Dave was returned by SELECT *");
    }

    @Test
    public void testDeleteBeingSaveSuccessfully() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");

        String response = sendCommandToServer("DELETE FROM marks WHERE name == 'Dave';");
        assertTrue(response.contains("[OK]"), "A valid delete query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid delete query was made, however an [ERROR] tag was returned");
        response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("Steve"), "An attempt was made to delete Dave to the table, but Steve was not returned by SELECT *");
        assertTrue(response.contains("Clive"), "An attempt was made to delete Dave to the table, but Clive was not returned by SELECT *");
        assertTrue(response.contains("Bob"), "An attempt was made to delete Dave to the table, but Bob was not returned by SELECT *");
        assertTrue(!response.contains("Dave"), "An attempt was made to delete Dave to the table, but Dave was returned by SELECT *");

        // Create a new server object
        server = new DBServer();
        sendCommandToServer("USE " + randomName + ";");
        response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("Steve"), "An attempt was made to delete Dave to the table, but Steve was not returned by SELECT *");
        assertTrue(response.contains("Clive"), "An attempt was made to delete Dave to the table, but Clive was not returned by SELECT *");
        assertTrue(response.contains("Bob"), "An attempt was made to delete Dave to the table, but Bob was not returned by SELECT *");
        assertTrue(!response.contains("Dave"), "An attempt was made to delete Dave to the table, but Dave was returned by SELECT *");
    }

    @Test
    public void testDeleteFromNonExistingTable() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");
        String response = sendCommandToServer("DELETE FROM test WHERE name == 'Dave';");
        assertTrue(response.contains("[ERROR]"), "Deleting data from a non-existing table, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "Deleting data from a non-existing table, however an [OK] tag was returned");
    }

    @Test
    public void testDeleteWithInvalidTokens() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");
        String response = sendCommandToServer("DELETE VALUES test WHERE name == 'Dave';");
        assertTrue(response.contains("[ERROR]"), "Invalid token `VALUES` while deleting, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "Invalid token `VALUES` while deleting, however an [OK] tag was returned");

        response = sendCommandToServer("DELETE FROM test VALUES name == 'Dave';");
        assertTrue(response.contains("[ERROR]"), "Invalid token `VALUES` while deleting, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "Invalid token `VALUES` while deleting, however an [OK] tag was returned");
    }

    // JOIN
    @Test
    public void testBasicJoin() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");

        sendCommandToServer("CREATE TABLE coursework (task, submission);");
        sendCommandToServer("INSERT INTO coursework VALUES ('OXO', 3);");
        sendCommandToServer("INSERT INTO coursework VALUES ('DB', 1);");
        sendCommandToServer("INSERT INTO coursework VALUES ('OXO', 4);");
        sendCommandToServer("INSERT INTO coursework VALUES ('STAG', 2);");

        String response = sendCommandToServer("JOIN coursework AND marks ON submission AND id;");
        assertTrue(response.contains("[OK]"), "A valid join query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid join query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("coursework.task"), "An attempt was made to join coursework and marks tables, but joined column name `coursework.task` was not returned");
        assertTrue(response.contains("marks.name"), "An attempt was made to join coursework and marks tables, but joined column name `marks.name` was not returned");
        assertTrue(response.contains("marks.mark"), "An attempt was made to join coursework and marks tables, but joined column name `marks.mark` was not returned");
        assertTrue(response.contains("marks.pass"), "An attempt was made to join coursework and marks tables, but joined column name `marks.pass` was not returned");
        assertFalse(response.contains("marks.id"), "An attempt was made to join coursework and marks tables, and joined column name `marks.id` should not be return");
        assertFalse(response.contains("coursework.id"), "An attempt was made to join coursework and marks tables, and joined column name `marks.id` should not be return");
        assertFalse(response.contains("coursework.submission"), "An attempt was made to join coursework and marks tables, and joined column name `marks.submission` should not be return");
        assertTrue(response.contains("id"), "An attempt was made to join coursework and marks tables, but id was not returned");
        assertFalse(!response.contains("id"), "New id column was not returned after joining two tables");
        assertTrue(response.contains("1\tOXO\tBob\t35\tFALSE"), "An attempt was made to join coursework and marks tables, but row with id 1 was not returned");
        assertTrue(response.contains("3\tOXO\tClive\t20\tFALSE"), "An attempt was made to join coursework and marks tables, but row with id 1 was not returned");
        assertTrue(response.contains("O"), "An attempt was made to join coursework and marks tables, but id 1 was not returned");

        response = sendCommandToServer("JOIN coursework AND marks ON id AND id;");
        assertTrue(response.contains("[OK]"), "A valid join query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid join query was made, however an [ERROR] tag was returned");

        assertTrue(response.contains("coursework.task"), "An attempt was made to join coursework and marks tables, but joined column name `coursework.task` was not returned");
        assertTrue(response.contains("coursework.submission"), "An attempt was made to join coursework and marks tables, but joined column name `coursework.submission` was not returned");
        assertTrue(response.contains("marks.name"), "An attempt was made to join coursework and marks tables, but joined column name `marks.name` was not returned");
        assertTrue(response.contains("marks.mark"), "An attempt was made to join coursework and marks tables, but joined column name `marks.mark` was not returned");
        assertTrue(response.contains("marks.pass"), "An attempt was made to join coursework and marks tables, but joined column name `marks.pass` was not returned");
        assertTrue(!response.contains("marks.id"), "An attempt was made to join coursework and marks tables, but joined column name `marks.id` was returned");
        assertTrue(!response.contains("coursework.id"), "An attempt was made to join coursework and marks tables, but joined column name `marks.id` was returned");
        assertTrue(response.contains("id"), "An attempt was made to join coursework and marks tables, but id was not returned");
        assertFalse(!response.contains("id"), "New id column was not returned after joining two tables");

        response = sendCommandToServer("JOIN marks AND coursework ON id AND submission;");
        assertTrue(response.contains("[OK]"), "A valid join query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid join query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("coursework.task"), "An attempt was made to join coursework and marks tables, but joined column name `coursework.task` was not returned");
        assertTrue(response.contains("marks.name"), "An attempt was made to join coursework and marks tables, but joined column name `marks.name` was not returned");
        assertTrue(response.contains("marks.mark"), "An attempt was made to join coursework and marks tables, but joined column name `marks.mark` was not returned");
        assertTrue(response.contains("marks.pass"), "An attempt was made to join coursework and marks tables, but joined column name `marks.pass` was not returned");
        assertTrue(!response.contains("marks.id"), "An attempt was made to join coursework and marks tables, but joined column name `marks.id` was returned");
        assertTrue(!response.contains("coursework.id"), "An attempt was made to join coursework and marks tables, but joined column name `marks.id` was returned");
        assertTrue(!response.contains("coursework.submission"), "An attempt was made to join coursework and marks tables, but joined column name `marks.submission` was returned");
        assertTrue(response.contains("id"), "An attempt was made to join coursework and marks tables, but id was not returned");
        assertFalse(!response.contains("id"), "New id column was not returned after joining two tables");
    }

    @Test
    public void testJoinWithCaseInsensitiveKeyword() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");

        sendCommandToServer("CREATE TABLE coursework (task, submission);");
        sendCommandToServer("INSERT INTO coursework VALUES ('OXO', 3);");
        sendCommandToServer("INSERT INTO coursework VALUES ('DB', 1);");
        sendCommandToServer("INSERT INTO coursework VALUES ('OXO', 4);");
        sendCommandToServer("INSERT INTO coursework VALUES ('STAG', 2);");

        String response = sendCommandToServer("join coursework anD marks oN submission and id;");
        assertTrue(response.contains("[OK]"), "A valid join query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid join query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("coursework.task"), "An attempt was made to join coursework and marks tables, but joined column name `coursework.task` was not returned");
        assertTrue(response.contains("marks.name"), "An attempt was made to join coursework and marks tables, but joined column name `marks.name` was not returned");
        assertTrue(response.contains("marks.mark"), "An attempt was made to join coursework and marks tables, but joined column name `marks.mark` was not returned");
        assertTrue(response.contains("marks.pass"), "An attempt was made to join coursework and marks tables, but joined column name `marks.pass` was not returned");
        assertFalse(response.contains("marks.id"), "An attempt was made to join coursework and marks tables, and joined column name `marks.id` should not be return");
        assertFalse(response.contains("coursework.id"), "An attempt was made to join coursework and marks tables, and joined column name `marks.id` should not be return");
        assertFalse(response.contains("coursework.submission"), "An attempt was made to join coursework and marks tables, and joined column name `marks.submission` should not be return");
        assertTrue(response.contains("id"), "An attempt was made to join coursework and marks tables, but id was not returned");
        assertFalse(!response.contains("id"), "New id column was not returned after joining two tables");
        assertTrue(response.contains("1\tOXO\tBob\t35\tFALSE"), "An attempt was made to join coursework and marks tables, but row with id 1 was not returned");
        assertTrue(response.contains("3\tOXO\tClive\t20\tFALSE"), "An attempt was made to join coursework and marks tables, but row with id 1 was not returned");
        assertTrue(response.contains("O"), "An attempt was made to join coursework and marks tables, but id 1 was not returned");
    }

    @Test
    public void testJoinWithReservedWordAttributeName() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");

        sendCommandToServer("CREATE TABLE coursework (task, submission);");
        sendCommandToServer("INSERT INTO coursework VALUES ('OXO', 3);");
        sendCommandToServer("INSERT INTO coursework VALUES ('DB', 1);");
        sendCommandToServer("INSERT INTO coursework VALUES ('OXO', 4);");
        sendCommandToServer("INSERT INTO coursework VALUES ('STAG', 2);");

        String response = sendCommandToServer("JOIN coursework AND marks ON true AND id;");
        assertTrue(response.contains("[ERROR]"), "An invalid join query using reserved keyword `AND` as column name, however an [ERROR] tag was not returned");

        response = sendCommandToServer("JOIN coursework AND marks ON submission AND OR;");
        assertTrue(response.contains("[ERROR]"), "An invalid join query using reserved keyword `OR` as column name, however an [ERROR] tag was not returned");
    }

    @Test
    public void testJoinWithReservedWordTableName() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");

        sendCommandToServer("CREATE TABLE coursework (task, submission);");
        sendCommandToServer("INSERT INTO coursework VALUES ('OXO', 3);");
        sendCommandToServer("INSERT INTO coursework VALUES ('DB', 1);");
        sendCommandToServer("INSERT INTO coursework VALUES ('OXO', 4);");
        sendCommandToServer("INSERT INTO coursework VALUES ('STAG', 2);");

        String response = sendCommandToServer("JOIN false AND marks ON submission AND id;");
        assertTrue(response.contains("[ERROR]"), "An invalid join query using reserved keyword `AND` as column name, however an [ERROR] tag was not returned");
    }

    @Test
    public void testJoinWithNoResult() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");

        sendCommandToServer("CREATE TABLE coursework (task, submission);");
        sendCommandToServer("INSERT INTO coursework VALUES ('OXO', 3);");
        sendCommandToServer("INSERT INTO coursework VALUES ('DB', 1);");
        sendCommandToServer("INSERT INTO coursework VALUES ('OXO', 4);");
        sendCommandToServer("INSERT INTO coursework VALUES ('STAG', 2);");

        String response = sendCommandToServer("JOIN coursework AND marks ON task AND name;");
        assertTrue(response.contains("[OK]"), "A valid join query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid join query was made, however an [ERROR] tag was returned");

        assertTrue(response.contains("coursework.submission"), "An attempt was made to join coursework and marks tables, but joined column name `coursework.task` was not returned");
        assertTrue(response.contains("marks.mark"), "An attempt was made to join coursework and marks tables, but joined column name `marks.mark` was not returned");
        assertTrue(response.contains("marks.pass"), "An attempt was made to join coursework and marks tables, but joined column name `marks.pass` was not returned");
        assertTrue(!response.contains("marks.id"), "An attempt was made to join coursework and marks tables, but joined column name `marks.id` was returned");
        assertTrue(!response.contains("coursework.id"), "An attempt was made to join coursework and marks tables, but joined column name `marks.id` was returned");
        assertTrue(!response.contains("coursework.task"), "An attempt was made to join coursework and marks tables, but joined column name `marks.submission` was returned");
        assertTrue(response.contains("id"), "An attempt was made to join coursework and marks tables, but id was not returned");
        assertFalse(!response.contains("id"), "New id column was not returned after joining two tables");
        assertTrue(!response.contains("65"), "No value should be returned from mark columns, but got return value");
        assertTrue(!response.contains("TRUE"), "No value should be returned from pass columns, but got return value");
    }

    @Test
    public void testBasicJoinWithSpace() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");

        sendCommandToServer("CREATE TABLE coursework (task, submission);");
        sendCommandToServer("INSERT INTO coursework VALUES ('OXO', 3);");
        sendCommandToServer("INSERT INTO coursework VALUES ('DB', 1);");
        sendCommandToServer("INSERT INTO coursework VALUES ('OXO', 4);");
        sendCommandToServer("INSERT INTO coursework VALUES ('STAG', 2);");

        String response = sendCommandToServer("JOIN   coursework   AND      marks   ON     submission    AND     id   ;");
        assertTrue(response.contains("[OK]"), "A valid join query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid join query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("coursework.task"), "An attempt was made to join coursework and marks tables, but joined column name `coursework.task` was not returned");
        assertTrue(response.contains("marks.name"), "An attempt was made to join coursework and marks tables, but joined column name `marks.name` was not returned");
        assertTrue(response.contains("marks.mark"), "An attempt was made to join coursework and marks tables, but joined column name `marks.mark` was not returned");
        assertTrue(response.contains("marks.pass"), "An attempt was made to join coursework and marks tables, but joined column name `marks.pass` was not returned");
        assertTrue(!response.contains("marks.id"), "An attempt was made to join coursework and marks tables, but joined column name `marks.id` was returned");
        assertTrue(!response.contains("coursework.id"), "An attempt was made to join coursework and marks tables, but joined column name `marks.id` was returned");
        assertTrue(!response.contains("coursework.submission"), "An attempt was made to join coursework and marks tables, but joined column name `marks.submission` was returned");
        assertTrue(response.contains("id"), "An attempt was made to join coursework and marks tables, but id was not returned");
        assertFalse(!response.contains("id"), "New id column was not returned after joining two tables");

        response = sendCommandToServer("JOIN   coursework   AND     marks    ON    id   AND   id;");
        assertTrue(response.contains("[OK]"), "A valid join query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid join query was made, however an [ERROR] tag was returned");

        assertTrue(response.contains("coursework.task"), "An attempt was made to join coursework and marks tables, but joined column name `coursework.task` was not returned");
        assertTrue(response.contains("coursework.submission"), "An attempt was made to join coursework and marks tables, but joined column name `coursework.submission` was not returned");
        assertTrue(response.contains("marks.name"), "An attempt was made to join coursework and marks tables, but joined column name `marks.name` was not returned");
        assertTrue(response.contains("marks.mark"), "An attempt was made to join coursework and marks tables, but joined column name `marks.mark` was not returned");
        assertTrue(response.contains("marks.pass"), "An attempt was made to join coursework and marks tables, but joined column name `marks.pass` was not returned");
        assertTrue(!response.contains("marks.id"), "An attempt was made to join coursework and marks tables, but joined column name `marks.id` was returned");
        assertTrue(!response.contains("coursework.id"), "An attempt was made to join coursework and marks tables, but joined column name `marks.id` was returned");
        assertTrue(response.contains("id"), "An attempt was made to join coursework and marks tables, but id was not returned");
        assertFalse(!response.contains("id"), "New id column was not returned after joining two tables");

        response = sendCommandToServer("JOIN marks AND coursework ON id AND submission;");
        assertTrue(response.contains("[OK]"), "A valid join query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid join query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("coursework.task"), "An attempt was made to join coursework and marks tables, but joined column name `coursework.task` was not returned");
        assertTrue(response.contains("marks.name"), "An attempt was made to join coursework and marks tables, but joined column name `marks.name` was not returned");
        assertTrue(response.contains("marks.mark"), "An attempt was made to join coursework and marks tables, but joined column name `marks.mark` was not returned");
        assertTrue(response.contains("marks.pass"), "An attempt was made to join coursework and marks tables, but joined column name `marks.pass` was not returned");
        assertTrue(!response.contains("marks.id"), "An attempt was made to join coursework and marks tables, but joined column name `marks.id` was returned");
        assertTrue(!response.contains("coursework.id"), "An attempt was made to join coursework and marks tables, but joined column name `marks.id` was returned");
        assertTrue(!response.contains("coursework.submission"), "An attempt was made to join coursework and marks tables, but joined column name `marks.submission` was returned");
        assertTrue(response.contains("id"), "An attempt was made to join coursework and marks tables, but id was not returned");
        assertFalse(!response.contains("id"), "New id column was not returned after joining two tables");
    }

    @Test
    public void testJoinOnNonExistingColumn() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");

        sendCommandToServer("CREATE TABLE coursework (task, submission);");
        sendCommandToServer("INSERT INTO coursework VALUES ('OXO', 3);");
        sendCommandToServer("INSERT INTO coursework VALUES ('DB', 1);");
        sendCommandToServer("INSERT INTO coursework VALUES ('OXO', 4);");
        sendCommandToServer("INSERT INTO coursework VALUES ('STAG', 2);");

        String response = sendCommandToServer("JOIN coursework AND marks ON submission AND isAbsent;");
        assertTrue(response.contains("[ERROR]"), "A invalid join on a non-existing column query was made, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "A valid join on a non-existing column query was made, however an [OK] tag was returned");

        response = sendCommandToServer("JOIN coursework AND marks ON isAbsent AND mark;");
        assertTrue(response.contains("[ERROR]"), "A invalid join on a non-existing column query was made, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "A valid join on a non-existing column query was made, however an [ERROR] tag was returned");
    }

    @Test
    public void testJoinOnTableNameColumns() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");

        sendCommandToServer("CREATE TABLE coursework (task, submission);");
        sendCommandToServer("INSERT INTO coursework VALUES ('OXO', 3);");
        sendCommandToServer("INSERT INTO coursework VALUES ('DB', 1);");
        sendCommandToServer("INSERT INTO coursework VALUES ('OXO', 4);");
        sendCommandToServer("INSERT INTO coursework VALUES ('STAG', 2);");

        String response = sendCommandToServer("JOIN coursework AND marks ON coursework.submission AND marks.id;");
        assertTrue(response.contains("[OK]"), "A valid join was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A invalid join to a non-existing table query was made, however an [ERROR] tag was returned");

        response = sendCommandToServer("JOIN coursework AND marks ON submission AND marks.id;");
        assertTrue(response.contains("[OK]"), "A valid join was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A invalid join to a non-existing table query was made, however an [ERROR] tag was returned");

        response = sendCommandToServer("JOIN coursework AND marks ON marks.id AND coursework.submission;");
        assertTrue(response.contains("[OK]"), "A valid join was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A invalid join to a non-existing table query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("coursework.task"), "An attempt was made to join coursework and marks tables, but joined column name `coursework.task` was not returned");
        assertTrue(response.contains("marks.name"), "An attempt was made to join coursework and marks tables, but joined column name `marks.name` was not returned");
        assertTrue(response.contains("marks.mark"), "An attempt was made to join coursework and marks tables, but joined column name `marks.mark` was not returned");
        assertTrue(response.contains("marks.pass"), "An attempt was made to join coursework and marks tables, but joined column name `marks.pass` was not returned");
        assertTrue(!response.contains("marks.id"), "An attempt was made to join coursework and marks tables, but joined column name `marks.id` was returned");
        assertTrue(!response.contains("coursework.id"), "An attempt was made to join coursework and marks tables, but joined column name `marks.id` was returned");
        assertTrue(!response.contains("coursework.submission"), "An attempt was made to join coursework and marks tables, but joined column name `marks.submission` was returned");
        assertTrue(response.contains("id"), "An attempt was made to join coursework and marks tables, but id was not returned");
        assertFalse(!response.contains("id"), "New id column was not returned after joining two tables");
    }

    @Test
    public void testJoinToNonExistingTable() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");

        sendCommandToServer("CREATE TABLE coursework (task, submission);");
        sendCommandToServer("INSERT INTO coursework VALUES ('OXO', 3);");
        sendCommandToServer("INSERT INTO coursework VALUES ('DB', 1);");
        sendCommandToServer("INSERT INTO coursework VALUES ('OXO', 4);");
        sendCommandToServer("INSERT INTO coursework VALUES ('STAG', 2);");

        String response = sendCommandToServer("JOIN noExist AND marks ON submission AND mark;");
        assertTrue(response.contains("[ERROR]"), "A invalid join to a non-existing table query was made, however an [OK] tag was returned");
        assertFalse(response.contains("[OK]"), "A invalid join to a non-existing table query was made, however an [ERROR] tag was not returned");

        response = sendCommandToServer("JOIN coursework AND noExist ON submission AND mark;");
        assertTrue(response.contains("[ERROR]"), "A invalid join to a non-existing table query was made, however an [OK] tag was returned");
        assertFalse(response.contains("[OK]"), "A invalid join to a non-existing table query was made, however an [ERROR] tag was not returned");
    }

    @Test
    public void testJointWithInvalidToken() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");

        sendCommandToServer("CREATE TABLE coursework (task, submission);");
        sendCommandToServer("INSERT INTO coursework VALUES ('OXO', 3);");
        sendCommandToServer("INSERT INTO coursework VALUES ('DB', 1);");
        sendCommandToServer("INSERT INTO coursework VALUES ('OXO', 4);");
        sendCommandToServer("INSERT INTO coursework VALUES ('STAG', 2);");

        String response = sendCommandToServer("JOIN coursework VALUES marks ON submission AND mark;");
        assertTrue(response.contains("[ERROR]"), "An invalid token `VALUES`, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An invalid token `VALUES`, however an [OK] tag was returned");

        response = sendCommandToServer("JOIN coursework AND marks VALUES submission AND mark;");
        assertTrue(response.contains("[ERROR]"), "An invalid token `VALUES`, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An invalid token `VALUES`, however an [OK] tag was returned");

        response = sendCommandToServer("JOIN coursework AND marks ON submission VALUES mark;");
        assertTrue(response.contains("[ERROR]"), "An invalid token `VALUES`, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An invalid token `VALUES`, however an [OK] tag was returned");
    }

    // UPDATE
    @Test
    public void testBasicUpdate() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");

        String response = sendCommandToServer("UPDATE marks SET mark = 38 WHERE name == 'Clive';");
        assertTrue(response.contains("[OK]"), "A valid update query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid update query was made, however an [ERROR] tag was returned");

        response = sendCommandToServer("SELECT mark FROM marks WHERE name == 'Clive';");
        assertTrue(response.contains("38"), "An attempt was made to update Clive's mark, but the return value was wrong by SELECT mark FROM marks WHERE name == 'Clive';");
        assertFalse(response.contains("20"), "An attempt was made to update Clive's mark, but the return the original value by SELECT mark FROM marks WHERE name == 'Clive';");
    }

    @Test
    public void testUpdateWithCaseInsensitiveKeywords() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");

        String response = sendCommandToServer("upDate marks SeT mark = 38 WherE name == 'Clive';");
        assertTrue(response.contains("[OK]"), "A valid update query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid update query was made, however an [ERROR] tag was returned");

        response = sendCommandToServer("SELECT mark FROM marks WHERE name == 'Clive';");
        assertTrue(response.contains("38"), "An attempt was made to update Clive's mark, but the return value was wrong by SELECT mark FROM marks WHERE name == 'Clive';");
        assertFalse(response.contains("20"), "An attempt was made to update Clive's mark, but the return the original value by SELECT mark FROM marks WHERE name == 'Clive';");
    }

    @Test
    public void testUpdateReservedWordAttribute() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");
        String response = sendCommandToServer("UPDATE marks SET mark = 38 WHERE true == 'Clive';");
        assertTrue(response.contains("[ERROR]"), "An valid update using reserved word `true` as column name, however an [ERROR] tag was not returned");
        response = sendCommandToServer("UPDATE marks SET set = 38 WHERE name == 'Clive';");
        assertTrue(response.contains("[ERROR]"), "An valid update using reserved word `set` as column name, however an [ERROR] tag was not returned");
    }

    @Test
    public void testUpdateReservedWordTableName() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");
        String response = sendCommandToServer("UPDATE values SET mark = 38 WHERE name == 'Clive';");
        assertTrue(response.contains("[ERROR]"), "An valid update using reserved word `values` as table name, however an [ERROR] tag was not returned");
    }

    @Test
    public void testUpdateAfterAlterAddColumn() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertTrue(response.contains("Bob"), "An attempt was made to add Bob to the table, but they were not returned by SELECT * FROM marks;");
        response = sendCommandToServer("ALTER TABLE marks ADD isAbsent;");
        assertTrue(response.contains("[OK]"), "A valid alter query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid alter query was made, however an [ERROR] tag was returned");
        response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("NULL"), "An attempt was made to get data, but NULL was not returned by SELECT * FROM marks;");

        response = sendCommandToServer("UPDATE marks SET isAbsent = TRUE WHERE name == 'Bob';");
        assertTrue(response.contains("[OK]"), "A valid update query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid update query was made, however an [ERROR] tag was returned");

        response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("TRUE"), "An attempt was made to update Bob's isAbsent, but the return value was wrong by SELECT * FROM marks;");
        assertFalse(response.contains("NULL"), "An attempt was made to update Bob's mark, but the return the original value by SELECT * FROM marks;");
    }

    @Test
    public void testBasicUpdateWithSpace() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");

        String response = sendCommandToServer("UPDATE   marks     SET    mark    =     38     WHERE      name     ==  'Clive'  ;");
        assertTrue(response.contains("[OK]"), "A valid update query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid update query was made, however an [ERROR] tag was returned");
        response = sendCommandToServer("SELECT mark FROM marks WHERE name == 'Clive';");
        assertTrue(response.contains("38"), "An attempt was made to update Clive's mark, but the return value was wrong by SELECT mark FROM marks WHERE name == 'Clive';");
        assertFalse(response.contains("20"), "An attempt was made to update Clive's mark, but the return the original value by SELECT mark FROM marks WHERE name == 'Clive';");

        response = sendCommandToServer("UPDATE marks SET mark=40 WHERE mark>60 OR pass==true; ");
        assertTrue(response.contains("[OK]"), "A valid update query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid update query was made, however an [ERROR] tag was returned");
        response = sendCommandToServer("SELECT mark FROM marks WHERE name == 'Steve';");
        assertTrue(response.contains("40"), "An attempt was made to update Steve's mark, but the return value was wrong by SELECT mark FROM marks WHERE name == 'Steve';");
        assertFalse(response.contains("65"), "An attempt was made to update Steve's mark, but the return the original value by SELECT mark FROM marks WHERE name == 'Steve';");
        response = sendCommandToServer("SELECT mark FROM marks WHERE name == 'Dave';");
        assertTrue(response.contains("40"), "An attempt was made to update Dave's mark, but the return value was wrong by SELECT mark FROM marks WHERE name == 'Dave';");
        assertFalse(response.contains("55"), "An attempt was made to update Dave's mark, but the return the original value by SELECT mark FROM marks WHERE name == 'Dave';");
    }

    @Test
    public void testUpdateNonExistingAttribute() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");


        String response = sendCommandToServer("UPDATE marks SET isAbsent = TRUE WHERE pass == TRUE;");
        assertTrue(response.contains("[ERROR]"), "An invalid update query was made to update a non-existing column, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An invalid update query was made to update a non-existing column, however an [OK] tag was returned");
    }

    @Test
    public void testUpdateSignedNumber() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");

        String response = sendCommandToServer("UPDATE marks SET mark = +38 WHERE name == 'Clive';");
        assertTrue(response.contains("[OK]"), "A valid update query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid update query was made, however an [ERROR] tag was returned");

        response = sendCommandToServer("SELECT mark FROM marks WHERE name == 'Clive';");
        assertTrue(response.contains("+38"), "An attempt was made to update Clive's mark, but the return value was wrong by SELECT mark FROM marks WHERE name == 'Clive';");
        assertFalse(response.contains("20"), "An attempt was made to update Clive's mark, but the return the original value by SELECT mark FROM marks WHERE name == 'Clive';");

        response = sendCommandToServer("UPDATE marks SET mark = -33.8 WHERE name == 'Clive';");
        assertTrue(response.contains("[OK]"), "A valid update query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid update query was made, however an [ERROR] tag was returned");

        response = sendCommandToServer("SELECT mark FROM marks WHERE name == 'Clive';");
        assertTrue(response.contains("-33.8"), "An attempt was made to update Clive's mark, but the return value was wrong by SELECT mark FROM marks WHERE name == 'Clive';");
        assertFalse(response.contains("+38"), "An attempt was made to update Clive's mark, but the return the original value by SELECT mark FROM marks WHERE name == 'Clive';");
    }

    @Test
    public void testUpdateFloatingNumber() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");

        String response = sendCommandToServer("UPDATE marks SET mark = 38.3 WHERE name == 'Clive';");
        assertTrue(response.contains("[OK]"), "A valid update query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid update query was made, however an [ERROR] tag was returned");

        response = sendCommandToServer("SELECT mark FROM marks WHERE name == 'Clive';");
        assertTrue(response.contains("38.3"), "An attempt was made to update Clive's mark, but the return value was wrong by SELECT mark FROM marks WHERE name == 'Clive';");
        assertFalse(response.contains("20"), "An attempt was made to update Clive's mark, but the return the original value by SELECT mark FROM marks WHERE name == 'Clive';");
    }

    @Test
    public void testUpdateStringLiteralWithSymbol() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");

        String response = sendCommandToServer("UPDATE marks SET name = 'Clive*' WHERE name == 'Clive';");
        assertTrue(response.contains("[OK]"), "A valid update query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid update query was made, however an [ERROR] tag was returned");

        response = sendCommandToServer("SELECT name FROM marks WHERE mark == 20;");
        assertTrue(response.contains("Clive*"), "An attempt was made to update Clive's mark, but the return value was wrong by SELECT name FROM marks WHERE mark == 20;");

        response = sendCommandToServer("SELECT mark FROM marks WHERE name == 'Clive';");
        assertTrue(!response.contains("20"), "An attempt was made to update Clive's mark, but the return value was wrong by SELECT mark FROM marks WHERE name == 'Clive';");
    }

    @Test
    public void testUpdatePrimaryKey() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");


        String response = sendCommandToServer("UPDATE marks SET id = 1111 WHERE pass == TRUE;");
        assertTrue(response.contains("[ERROR]"), "An invalid update query was made to update primary key, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An invalid update query was made to update primary key, however an [OK] tag was returned");
    }

    @Test
    public void testUpdateNonExistingTable() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");

        String response = sendCommandToServer("UPDATE tests SET mark = 38 WHERE isAbsent == TRUE;");
        assertTrue(response.contains("[ERROR]"), "An invalid update query was made to update a non-existing table, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An invalid update query was made to update a non-existing table, however an [OK] tag was returned");
    }

    @Test
    public void testUpdateWithMultipleMatches() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");

        String response = sendCommandToServer("UPDATE marks SET pass = FALSE WHERE name LIKE 've';");
        assertTrue(response.contains("[OK]"), "A valid update query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid update query was made, however an [ERROR] tag was returned");

        server = new DBServer();
        sendCommandToServer("USE " + randomName + ";");
        response = sendCommandToServer("SELECT pass FROM marks WHERE name == 'Steve';");
        assertTrue(response.contains("FALSE"), "An attempt was made to update Steve's pass status, but the return value was wrong by SELECT pass FROM marks WHERE name == 'Steve';");
        assertFalse(response.contains("TRUE"), "An attempt was made to update Steve's pass status, but the return the original value by SELECT pass FROM marks WHERE name == 'Steve';");

        response = sendCommandToServer("SELECT pass FROM marks WHERE name == 'Dave';");
        assertTrue(response.contains("FALSE"), "An attempt was made to update Dave's pass status, but the return value was wrong by SELECT pass FROM marks WHERE name == 'Dave';");
        assertFalse(response.contains("TRUE"), "An attempt was made to update Dave's pass status, but the return the original value by SELECT pass FROM marks WHERE name == 'Dave';");



    }

    @Test
    public void testUpdateWithMultipleAttributes() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");

        String response = sendCommandToServer("UPDATE marks SET pass = FALSE, mark = 40 WHERE name LIKE 've';");
        assertTrue(response.contains("[OK]"), "A valid update query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid update query was made, however an [ERROR] tag was returned");

        response = sendCommandToServer("SELECT mark, pass FROM marks WHERE name == 'Steve';");
        assertTrue(response.contains("FALSE"), "An attempt was made to update Steve's pass status, but the return value was wrong by SELECT mark, pass FROM marks WHERE name == 'Steve';");
        assertFalse(response.contains("TRUE"), "An attempt was made to update Steve's pass status, but the return the original value by SELECT mark, pass FROM marks WHERE name == 'Steve';");
        assertTrue(response.contains("40"), "An attempt was made to update Steve's mark status, but the return value was wrong by SELECT mark, pass FROM marks WHERE name == 'Steve';");
        assertFalse(response.contains("65"), "An attempt was made to update Steve's mark status, but the return the original value by SELECT mark, pass FROM marks WHERE name == 'Steve';");

        response = sendCommandToServer("SELECT mark, pass FROM marks WHERE name == 'Dave';");
        assertTrue(response.contains("FALSE"), "An attempt was made to update Dave's pass status, but the return value was wrong by SELECT mark, pass FROM marks WHERE name == 'Dave';");
        assertFalse(response.contains("TRUE"), "An attempt was made to update Dave's pass status, but the return the original value by SELECT mark, pass FROM marks WHERE name == 'Dave';");
        assertTrue(response.contains("40"), "An attempt was made to update Steve's mark status, but the return value was wrong by SELECT mark, pass FROM marks WHERE name == 'Steve';");
        assertFalse(response.contains("55"), "An attempt was made to update Steve's mark status, but the return the original value by SELECT mark, pass FROM marks WHERE name == 'Steve';");
    }

    @Test
    public void testUpdateWithInvalidNameValuePair() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");

        String response = sendCommandToServer("UPDATE marks SET mark ! 1111 WHERE pass == TRUE;");
        assertTrue(response.contains("[ERROR]"), "An invalid name value pair for update, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An invalid name value pair for update, however an [OK] tag was returned");

        response = sendCommandToServer("UPDATE marks SET mark = nul WHERE pass == TRUE;");
        assertTrue(response.contains("[ERROR]"), "An invalid name value pair for update, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An invalid name value pair for update, however an [OK] tag was returned");
    }

    @Test
    public void testUpdateWithInvalidToken() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");

        String response = sendCommandToServer("UPDATE marks ON mark = 1111 WHERE pass == TRUE;");
        assertTrue(response.contains("[ERROR]"), "An invalid token `ON`, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An invalid token `ON`, however an [OK] tag was returned");

        response = sendCommandToServer("UPDATE marks SET mark = 1111 ON pass == TRUE;");
        assertTrue(response.contains("[ERROR]"), "An invalid token `ON`, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An invalid token `ON`, however an [OK] tag was returned");
    }


    // A test to make sure that querying returns a valid ID (this test also implicitly checks the "==" condition)
    // (these IDs are used to create relations between tables, so it is essential that they work !)
    @Test
    public void testQueryID() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        String response = sendCommandToServer("SELECT id FROM marks WHERE name == 'Steve';");
        // Convert multi-lined responses into just a single line
        String singleLine = response.replace("\n"," ").trim();
        // Split the line on the space character
        String[] tokens = singleLine.split(" ");
        // Check that the very last token is a number (which should be the ID of the entry)
        String lastToken = tokens[tokens.length-1];
        try {
            Integer.parseInt(lastToken);
        } catch (NumberFormatException nfe) {
            fail("The last token returned by `SELECT id FROM marks WHERE name == 'Steve';` should have been an integer ID, but was " + lastToken);
        }
    }

    @Test
    public void testUpdateIsSavedSuccessfully() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");

        String response = sendCommandToServer("UPDATE marks SET mark = 38 WHERE name == 'Clive';");
        assertTrue(response.contains("[OK]"), "A valid update query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid update query was made, however an [ERROR] tag was returned");

        response = sendCommandToServer("SELECT mark FROM marks WHERE name == 'Clive';");
        assertTrue(response.contains("38"), "An attempt was made to update Clive's mark, but the return value was wrong by SELECT mark FROM marks WHERE name == 'Clive';");
        assertFalse(response.contains("20"), "An attempt was made to update Clive's mark, but the return the original value by SELECT mark FROM marks WHERE name == 'Clive';");

        // Create a new server object
        server = new DBServer();
        sendCommandToServer("USE " + randomName + ";");
        response = sendCommandToServer("SELECT mark FROM marks WHERE name == 'Clive';");
        assertTrue(response.contains("38"), "An attempt was made to update Clive's mark, but the return value was wrong by SELECT mark FROM marks WHERE name == 'Clive';");
        assertFalse(response.contains("20"), "An attempt was made to update Clive's mark, but the return the original value by SELECT mark FROM marks WHERE name == 'Clive';");
    }

    // DROP
    @Test
    public void testBasicDropTable() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE students (name, gender, age);");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");

        String response = sendCommandToServer("DROP TABLE marks;");
        assertTrue(response.contains("[OK]"), "A valid drop query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid drop query was made, however an [ERROR] tag was returned");

        response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[ERROR]"), "Selecting a non-existing table, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "Selecting a non-existing table, however an [OK] tag was returned");
    }

    @Test
    public void testDropTableUsingReservedWords() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE students (name, gender, age);");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");

        String response = sendCommandToServer("DROP TABLE table;");
        assertTrue(response.contains("[ERROR]"), "Dropping a table using a name `table` that is a reserved word, however an [ERROR] tag was not returned");
        response = sendCommandToServer("DROP TABLE null;");
        assertTrue(response.contains("[ERROR]"), "Dropping a table using a name `null` that is a reserved word, however an [ERROR] tag was not returned");
        response = sendCommandToServer("DROP TABLE true;");
        assertTrue(response.contains("[ERROR]"), "Dropping a table using a name `true` that is a reserved word, however an [ERROR] tag was not returned");
        response = sendCommandToServer("DROP TABLE values;");
        assertTrue(response.contains("[ERROR]"), "Dropping a table using a name `values` that is a reserved word, however an [ERROR] tag was not returned");
    }

    @Test
    public void testDropWithCaseInsensitiveKeywords() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE students (name, gender, age);");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");

        String response = sendCommandToServer("droP TablE marks;");
        assertTrue(response.contains("[OK]"), "A valid drop query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid drop query was made, however an [ERROR] tag was returned");

        response = sendCommandToServer("selecT * from marks;");
        assertTrue(response.contains("[ERROR]"), "Selecting a non-existing table, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "Selecting a non-existing table, however an [OK] tag was returned");
    }
    @Test
    public void testDropTableWithSpace() {
        String randomName = generateRandomName();
        String response = sendCommandToServer("CREATE  DATABASE " + randomName + " ;");
        assertTrue(response.contains("[OK]"), "A valid creating table query was made, however an [OK] tag was not returned");
        response = sendCommandToServer("USE    " + randomName + ";  ");
        assertTrue(response.contains("[OK]"), "A valid use table query was made, however an [OK] tag was not returned");
        response = sendCommandToServer("CREATE   TABLE students (name, gender, age);");
        assertTrue(response.contains("[OK]"), "A valid create table query was made, however an [OK] tag was not returned");
        response = sendCommandToServer("CREATE TABLE   marks (name, mark, pass);");
        assertTrue(response.contains("[OK]"), "A valid create table query was made, however an [OK] tag was not returned");
        sendCommandToServer("INSERT INTO marks   VALUES (     'Steve',   65       , TRUE     );");
        response = sendCommandToServer("DROP    TABLE        marks;");
        assertTrue(response.contains("[OK]"), "A valid drop query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid drop query was made, however an [ERROR] tag was returned");
        response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[ERROR]"), "Selecting a non-existing table, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "Selecting a non-existing table, however an [OK] tag was returned");
    }


    @Test
    public void testDropNonExistingTable() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");

        String response = sendCommandToServer("DROP TABLE marks;");
        assertTrue(response.contains("[ERROR]"), "Dropping an non-existing table, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "Dropping an non-existing table, however an [OK] tag was returned");
    }

    @Test
    public void testBasicDropDatabase() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");


        String response = sendCommandToServer("DROP DATABASE " + randomName + ";");
        assertTrue(response.contains("[OK]"), "A valid drop query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid drop query was made, however an [ERROR] tag was returned");

        response = sendCommandToServer("USE " + randomName + ";");
        assertTrue(response.contains("[ERROR]"), "Using a non-existing database, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "Using a non-existing database, however an [OK] tag was returned");

        response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[ERROR]"), "Selecting a non-existing table, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "Selecting a non-existing table, however an [OK] tag was returned");
    }

    @Test
    public void testDropDatabaseNameUsingReservedWords() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");

        String response = sendCommandToServer("DROP DATABASE database;");
        assertTrue(response.contains("[ERROR]"), "Dropping a database using a name `database` that is a reserved keyword, however an [ERROR] tag was not returned");
        response = sendCommandToServer("DROP DATABASE TABLE;");
        assertTrue(response.contains("[ERROR]"), "Dropping a database using a name `TABLE` that is a reserved keyword, however an [ERROR] tag was not returned");
        response = sendCommandToServer("DROP DATABASE values;");
        assertTrue(response.contains("[ERROR]"), "Dropping a database using a name `values` that is a reserved keyword, however an [ERROR] tag was not returned");
    }

    @Test
    public void testDropNonExistingDatabase() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");

        String response = sendCommandToServer("DROP DATABASE notExistDatabase;");
        assertTrue(response.contains("[ERROR]"), "Dropping a non-existing database, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "Dropping a non-existing database, however an [OK] tag was returned");
    }

    @Test
    public void testDropInvalidToken() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");

        String response = sendCommandToServer("DROP VALUES " + randomName + ";");
        assertTrue(response.contains("[ERROR]"), "Invalid token `VALUES` for dropping, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "Invalid token `VALUES` for dropping, however an [OK] tag was returned");
    }

    // INSERT
    @Test
    public void testInsertWithMissingToken() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");

        String response = sendCommandToServer("INSERT marks VALUES ('Steve', 65, TRUE);");
        assertTrue(response.contains("[ERROR]"), "An invalid query missing INTO was made, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An invalid query missing INTO was made, however an [OK] tag was returned");

        sendCommandToServer("INSERT INTO marks ('Dave', 55, TRUE);");
        assertTrue(response.contains("[ERROR]"), "An invalid query missing VALUES was made, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An invalid query missing VALUES was made, however an [OK] tag was returned");

        sendCommandToServer("INSERT INTO marks VALUES 'Bob', 35, FALSE);");
        assertTrue(response.contains("[ERROR]"), "An invalid query missing `(` was made, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An invalid query missing `(` was made, however an [OK] tag was returned");

        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE;");
        assertTrue(response.contains("[ERROR]"), "An invalid query missing `)` was made, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An invalid query missing `)` was made, however an [OK] tag was returned");
    }

    @Test
    public void testInsertWithInvalidToken() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");

        String response = sendCommandToServer("INSERT SELECT marks VALUES ('Steve', 65, TRUE);");
        assertTrue(response.contains("[ERROR]"), "An invalid query using SELECT was made, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An invalid query using SELECT was made, however an [OK] tag was returned");

        sendCommandToServer("INSERT INTO marks SELECT ('Dave', 55, TRUE);");
        assertTrue(response.contains("[ERROR]"), "An invalid query using SELECT was made, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An invalid query using SELECT was made, however an [OK] tag was returned");

        sendCommandToServer("INSERT INTO marks VALUES ) 'Bob', 35, FALSE);");
        assertTrue(response.contains("[ERROR]"), "An invalid query using `)` was made, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An invalid query using `)` was made, however an [OK] tag was returned");

        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE(;");
        assertTrue(response.contains("[ERROR]"), "An invalid query using `(` was made, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An invalid query using `(` was made, however an [OK] tag was returned");
    }

    @Test
    public void testInsertWithInvalidValueList() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");

        String response = sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, nul);");
        assertTrue(response.contains("[ERROR]"), "An invalid value `nul`, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An invalid value `nul`, however an [OK] tag was returned");
    }

    @Test
    public void testInsertWithTooManyValues() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        String response = sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE, NULL);");

        assertTrue(response.contains("[ERROR]"), "Inserting too many values, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "Inserting too many values, however an [OK] tag was returned");
    }

    @Test
    public void testInsertIntoNonExistingTable() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        String response = sendCommandToServer("INSERT INTO students VALUES ('Steve', 65, TRUE, NULL);");
        assertTrue(response.contains("[ERROR]"), "Inserting into non-existing table, however an [ERROR] tag was not returned");
    }

    @Test
    public void testInsertIntoTableNameWithReservedWords() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        String response = sendCommandToServer("INSERT INTO table VALUES ('Steve', 65, TRUE, NULL);");
        assertTrue(response.contains("[ERROR]"), "Inserting into a table with a name `marks` that is a reserved word, however an [ERROR] tag was not returned");
    }

    @Test
    public void testInsertWithInsufficientValues() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        String response = sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65);");

        assertTrue(response.contains("[ERROR]"), "Inserting insufficient values, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "Inserting insufficient values, however an [OK] tag was returned");
    }

    @Test
    public void testConditionWithInvalidTokens() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass, class);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE, 'B');");
        sendCommandToServer("INSERT INTO marks VALUES ('Mike', 32, TRUE, 'A');");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE, 'B');");
        sendCommandToServer("INSERT INTO marks VALUES ('Tim', 35, TRUE, 'A');");
        sendCommandToServer("INSERT INTO marks VALUES ('John', 45, FALSE, 'C');");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE, 'A');");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE, NULL);");
        sendCommandToServer("INSERT INTO marks VALUES ('Henry', 65, FALSE, 'C');");

        String response = sendCommandToServer("SELECT * FROM marks WHERE (mark == 65 ON class == 'C');");
        assertTrue(response.contains("[ERROR]"), "An invalid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[OK]"), "A valid query was made, however an [ERROR] tag was returned");

        response = sendCommandToServer("SELECT * FROM marks WHERE (mark == 65 AND class == 'C';");
        assertTrue(response.contains("[ERROR]"), "An invalid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[OK]"), "A valid query was made, however an [ERROR] tag was returned");

        response = sendCommandToServer("SELECT * FROM marks WHERE (mark == 65 AND class == nul);");
        assertTrue(response.contains("[ERROR]"), "An invalid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[OK]"), "A valid query was made, however an [ERROR] tag was returned");

        response = sendCommandToServer("SELECT * FROM marks WHERE (mark == 65 AND class bigger 'C');");
        assertTrue(response.contains("[ERROR]"), "An invalid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[OK]"), "A valid query was made, however an [ERROR] tag was returned");
    }


    // A test to make sure that databases can be reopened after server restart
    @Test
    public void testTablePersistsAfterRestart() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        // Create a new server object
        server = new DBServer();
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("Steve"), "Steve was added to a table and the server restarted - but Steve was not returned by SELECT *");
    }

    @Test
    public void testTableConfigPersistsAfterRestart() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        // Create a new server object
        server = new DBServer();
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("INSERT INTO marks VALUES ('Mike', 24, TRUE);");
        String response = sendCommandToServer("SELECT id FROM marks WHERE name LIKE 'ike';");
        assertTrue(response.contains("2"), "New data with using updated pk was inserted, but wrong id was returned by SELECT id FROM marks WHERE name LIKE 'ike';");
    }

    // Test to make sure that the [ERROR] tag is returned in the case of an error (and NOT the [OK] tag)
    @Test
    public void testForErrorTag() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        String response = sendCommandToServer("SELECT * FROM libraryfines;");
        assertTrue(response.contains("[ERROR]"), "An attempt was made to access a non-existent table, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An attempt was made to access a non-existent table, however an [OK] tag was returned");
    }

    @Test
    public void testInvalidCmd() {
        String randomName = generateRandomName();
        String response = sendCommandToServer("KILL DATABASE " + randomName + ";");
        assertTrue(response.contains("[ERROR]"), "Using an invalid command, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "Using an invalid command, however an [OK] tag was returned");
    }

    @Test
    public void testInvalidPlainText() {
        String response = sendCommandToServer("CREATE DATABASE test^t;");
        assertTrue(response.contains("[ERROR]"), "Using invalid plain text as database name, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "Using invalid plain text as database name, however an [OK] tag was returned");
    }

    @Test
    public void testMissingEndingSemiColon() {
        String randomName = generateRandomName();
        String response = sendCommandToServer("CREATE DATABASE " + randomName);
        assertTrue(response.contains("[ERROR]"), "Missing closing semi-colon, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "Missing closing semi-colon, however an [OK] tag was returned");
    }

    @Test
    public void testTableNameCaseInsensitive() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("CREATE TABLE MARkS (name, mark, pass);");
        assertTrue(response.contains("[OK]"), "A valid create query was made, however an [OK] tag was not returned");
        response = sendCommandToServer("INSERT INTO maRks VALUES ('Steve', 65, TRUE);");
        assertTrue(response.contains("[OK]"), "A valid insert query was made, however an [OK] tag was not returned");
        response = sendCommandToServer("INSERT INTO mArks VALUES ('Dave', 55, TRUE);");
        assertTrue(response.contains("[OK]"), "A valid insert query was made, however an [OK] tag was not returned");
        response = sendCommandToServer("INSERT INTO markS VALUES ('Bob', 35, FALSE);");
        assertTrue(response.contains("[OK]"), "A valid insert query was made, however an [OK] tag was not returned");
        response = sendCommandToServer("INSERT INTO Marks VALUES ('Clive', 20, FALSE);");
        assertTrue(response.contains("[OK]"), "A valid insert query was made, however an [OK] tag was not returned");
        response = sendCommandToServer("SELECT * FROM mARkS;");
        assertTrue(response.contains("[OK]"), "A valid select query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("Steve"), "A valid select query was made, but Steve was not returned by SELECT * FROM mARkS;");
        assertTrue(response.contains("Clive"), "A valid select query was made, but Clive was not returned by SELECT * FROM mARkS;");

        // Create a new server object
        server = new DBServer();
        sendCommandToServer("USE " + randomName + ";");
        response = sendCommandToServer("SELECT * FROM MARKs;");
        assertTrue(response.contains("[OK]"), "A valid select query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("Steve"), "A valid select query was made after restart, but Steve was not returned by SELECT * FROM mARkS;");
        assertTrue(response.contains("Clive"), "A valid select query was made after restart, but Clive was not returned by SELECT * FROM mARkS;");
    }

    @Test
    public void testColumnNameCaseInsensitive() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("CREATE TABLE marks (NaMe, MarK, PASS);");
        assertTrue(response.contains("[OK]"), "A valid create query was made, however an [OK] tag was not returned");
        response = sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        assertTrue(response.contains("[OK]"), "A valid insert query was made, however an [OK] tag was not returned");
        response = sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        assertTrue(response.contains("[OK]"), "A valid insert query was made, however an [OK] tag was not returned");
        response = sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        assertTrue(response.contains("[OK]"), "A valid insert query was made, however an [OK] tag was not returned");
        response = sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");
        assertTrue(response.contains("[OK]"), "A valid insert query was made, however an [OK] tag was not returned");
        response = sendCommandToServer("SELECT name, mark, pass FROM marks;");
        assertTrue(response.contains("[OK]"), "A valid select query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("NaMe"), "A valid select query was made, but NaMe column was not returned by SELECT name, mark, pass FROM marks;");
        assertTrue(response.contains("MarK"), "A valid select query was made, but MarK column was not returned by SELECT name, mark, pass FROM marks;");
        assertTrue(response.contains("PASS"), "A valid select query was made, but PASS column was not returned by SELECT name, mark, pass FROM marks;");
        assertFalse(response.contains("name"), "A valid select query was made, but wrong column name `name` was returned by SELECT name, mark, pass FROM marks");
        assertFalse(response.contains("mark"), "A valid select query was made, but wrong column name `mark` was returned by SELECT name, mark, pass FROM marks");
        assertFalse(response.contains("pass"), "A valid select query was made, but wrong column name `pass` was returned by SELECT name, mark, pass FROM marks");


        // Create a new server object
        server = new DBServer();
        sendCommandToServer("USE " + randomName + ";");
        response = sendCommandToServer("SELECT name, mark, pass FROM marks WHERE namE Like 've' AND pasS == TRUE AND maRK > 55;");
        assertTrue(response.contains("[OK]"), "A valid select query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("Steve"), "An attempt was made to get Steve, but it wes not returned by SELECT name, mark, pass FROM marks WHERE namE Like 've' AND pasS == TRUE AND maRK > 55;");
        assertFalse(response.contains("Dave"), "An attempt was made to get Steve, but Dave wes returned by SELECT name, mark, pass FROM marks WHERE namE Like 've' AND pasS == TRUE AND maRK > 55;");
        assertTrue(response.contains("NaMe"), "A valid select query was made, but NaMe column was not returned by SELECT name, mark, pass FROM marks WHERE namE Like 've' AND pasS == TRUE AND maRK > 55;");
        assertTrue(response.contains("MarK"), "A valid select query was made, but MarK column was not returned by SELECT name, mark, pass FROM marks WHERE namE Like 've' AND pasS == TRUE AND maRK > 55;");
        assertTrue(response.contains("PASS"), "A valid select query was made, but PASS column was not returned by SELECT name, mark, pass FROM marks WHERE namE Like 've' AND pasS == TRUE AND maRK > 55;");
    }

    @Test
    public void testStringLiteral() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        assertTrue(response.contains("[OK]"), "A valid create query was made, however an [OK] tag was not returned");
        response = sendCommandToServer("INSERT INTO marks VALUES ('Stev^_{}~e', 65, TRUE);");
        assertTrue(response.contains("[OK]"), "A valid insert query was made, however an [OK] tag was not returned");
        response = sendCommandToServer("INSERT INTO marks VALUES ('!Da$%&ve', 55, TRUE);");
        assertTrue(response.contains("[OK]"), "A valid insert query was made, however an [OK] tag was not returned");
        response = sendCommandToServer("INSERT INTO marks VALUES ('!p?as()*+-s', 35, FALSE);");
        assertTrue(response.contains("[OK]"), "A valid insert query was made, however an [OK] tag was not returned");
        response = sendCommandToServer("INSERT INTO marks VALUES ('./Cli:;ve>', 20, FALSE);");
        assertTrue(response.contains("[OK]"), "A valid insert query was made, however an [OK] tag was not returned");
        response = sendCommandToServer("INSERT INTO marks VALUES ('John  @#$#!>', 80, FALSE);");
        assertTrue(response.contains("[OK]"), "A valid insert query was made, however an [OK] tag was not returned");
        response = sendCommandToServer("SELECT name, mark, pass FROM marks WHERE name Like '!';");
        assertTrue(response.contains("[OK]"), "A valid select query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("35"), "A valid select query was made, but `35` was not returned by SELECT name, mark, pass FROM marks WHERE name Like '!';");
        assertTrue(response.contains("!Da$%&ve"), "A valid select query was made, but `!Da$%&ve` was not returned by SELECT name, mark, pass FROM marks WHERE name Like '!';");
        assertTrue(response.contains("!p?as()*+-s"), "A valid select query was made, but `!p?as()*+-s` was not returned by SELECT name, mark, pass FROM marks WHERE name Like '!';");
        assertTrue(response.contains("John  @#$#!>"), "A valid select query was made, but `John  @#$#!>` was not returned by SELECT name, mark, pass FROM marks WHERE name Like '!';");
        assertTrue(response.contains("80"), "A valid select query was made, but `80` was not returned by SELECT name, mark, pass FROM marks WHERE name Like '!';");
        assertFalse(response.contains("./Cli:;ve>"), "A valid select query was made, but `./Cli:;ve>` was returned by SELECT name, mark, pass FROM marks WHERE name Like '!';");
        assertFalse(response.contains("Stev^_{}~e"), "A valid select query was made, but `Stev^_{}~e` was returned by SELECT name, mark, pass FROM marks WHERE name Like '!';");

        response = sendCommandToServer("SELECT name, mark, pass FROM marks WHERE name == 'John  @#$#!>';");
        assertTrue(response.contains("[OK]"), "A valid select query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("80"), "A valid select query was made, but `35` was not returned by SELECT name, mark, pass FROM marks WHERE name Like '!';");
        assertTrue(response.contains("John  @#$#!>"), "A valid select query was made, but `!Da$%&ve` was not returned by SELECT name, mark, pass FROM marks WHERE name Like '!';");
        assertFalse(response.contains("./Cli:;ve>"), "A valid select query was made, but `./Cli:;ve>` was returned by SELECT name, mark, pass FROM marks WHERE name Like '!';");
        assertFalse(response.contains("Stev^_{}~e"), "A valid select query was made, but `Stev^_{}~e` was returned by SELECT name, mark, pass FROM marks WHERE name Like '!';");

        response = sendCommandToServer("SELECT name, mark, pass FROM marks WHERE (name == 'John  @#$#!>' OR name LIKE '()');");
        assertTrue(response.contains("[OK]"), "A valid select query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("80"), "A valid select query was made, but `35` was not returned by SELECT name, mark, pass FROM marks WHERE (name == 'John  @#$#!>' OR name LIKE '()');");
        assertTrue(response.contains("John  @#$#!>"), "A valid select query was made, but `!Da$%&ve` was not returned by SELECT name, mark, pass FROM marks WHERE (name == 'John  @#$#!>' OR name LIKE '()');");
        assertTrue(response.contains("35"), "A valid select query was made, but `35` was not returned by SELECT name, mark, pass FROM marks WHERE (name == 'John  @#$#!>' OR name LIKE '()');");
        assertTrue(response.contains("!p?as()*+-s"), "A valid select query was made, but `!Da$%&ve` was not returned by SELECT name, mark, pass FROM marks WHERE (name == 'John  @#$#!>' OR name LIKE '()');");
        assertFalse(response.contains("./Cli:;ve>"), "A valid select query was made, but `./Cli:;ve>` was returned by SELECT name, mark, pass FROM marks WHERE (name == 'John  @#$#!>' OR name LIKE '()');");
        assertFalse(response.contains("Stev^_{}~e"), "A valid select query was made, but `Stev^_{}~e` was returned by SELECT name, mark, pass FROM marks WHERE (name == 'John  @#$#!>' OR name LIKE '()');");
    }

    @Test
    public void testInvalidStringLiteral() {
        assertThrows(StringWithNoQuoteException.class, () -> ValueType.retrieveStringFromQuote("123"));
        assertThrows(StringWithNoQuoteException.class, () -> ValueType.retrieveStringFromQuote("TRUE"));
        assertThrows(StringWithNoQuoteException.class, () -> ValueType.retrieveStringFromQuote("TEXT"));
        assertThrows(StringWithNoQuoteException.class, () -> ValueType.retrieveStringFromQuote("'TEXT"));
        assertThrows(StringWithNoQuoteException.class, () -> ValueType.retrieveStringFromQuote("''TEXT"));
        assertDoesNotThrow(() -> ValueType.retrieveStringFromQuote("'TEXT'"));
    }

    @Test
    public void testGetTitleIndexByName() {
        Table table = new Table("testTable", 0);
        try {
            table.addColumn("first");
            table.addColumn("second");
            assertEquals(0, table.getTitleIndexByName("first"));
            assertEquals(-1, table.getTitleIndexByName("third"));
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }

    }

    @Test
    public void testUpdateDataValues() {
        Table table = new Table("testTable", 0);
        List<List<String>> dataValues = new ArrayList<>();
        ArrayList<String> row = new ArrayList<>();
        try {
            table.addColumn("order");
            row.add("first");
            dataValues.add(row);
            assertThrows(TableException.class, () -> table.updateDataValues(null));
            assertDoesNotThrow(() -> table.updateDataValues(dataValues));
            assertEquals("first", table.getDataValues()[0][0]);
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }
}