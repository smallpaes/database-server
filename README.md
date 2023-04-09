# Database Server

A database server that allows users to use a simplified version of the 
SQL database query language. 
The parser, interpreter, and aforementioned functionality 
are built from scratch using Java, Maven, and JUnit with proper testing, 
while the file system is being used to be able to persistently store data.

## Features

The query language we shall use for this purpose supports the following main types of query:

| Command | Functionality                                                                             |
|---------|-------------------------------------------------------------------------------------------|
| USE     | Changes the database against which the following queries will be run                      |
| CREATE  | Constructs a new database or table (depending on the provided parameters)                 |
| INSERT  | Adds a new record (row) to an existing table                                              |
| SELECT  | Searches for records that match the given condition                                       |
| UPDATE  | Changes the existing data contained within a table                                        |
| ALTER   | Changes the structure (columns) of an existing table: add or drop                         |
| DELETE  | Removes records that match the given condition from an existing table                     |
| DROP    | Removes a specified table from a database, or removes the entire database                 |
| JOIN    | Performs an inner join on two tables (returning all permutations of all matching records) |

> A grammar that fully defines the simplified query language is provided in <a href="https://github.com/drslock/JAVA2022/blob/main/Weekly%20Workbooks/07%20Briefing%20on%20DB%20assignment/resources/BNF.txt" target="_blank">this BNF document</a>.


### Prerequisite / Additional Information
* Primary key is always called `id` and is auto-generated by the server.
* Table and database names are case-sensitive for querying, but will be saved as lowercase.
* SQL keywords are reserved words and are case-insensitive.
* Column names are case-insensitive for querying, but the case given by the users will be preserved.
* Foreign keys can not be set using query language, but relies on the user to remember which attributes are keys to maintain table relationships.
* For comparison, no data will be returned if it is not valid.


## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.


### Prerequisites

- Java Development Kit (JDK) 19
- Apache Maven 3.9.0

### Installing

Follow these steps to get the development environment running:

1. Clone this repository using `git clone https://github.com/smallpaes/database-server.git`.
2. Navigate to the project directory using `cd database-server`.
3. Run `./mvnw clean install` to build the project and install the necessary dependencies.

### Using the database

```bash
# Run the server
$ ./mvnw exec:java@server

# Run the client to start querying
$ ./mvnw exec:java@client
```


### Compiling

Run `./mvnw compile` to compile the project.


### Running the tests

Run `./mvnw test` for testing.


## Acknowledgments

* This is an assignment given by the instructor at the University of Bristol: [Simon](https://github.com/drslock) from the course of Object-Oriented Programming with Java 2022.
* This assignment is built on top on the base <a href="https://github.com/drslock/JAVA2022/tree/main/Weekly%20Workbooks/07%20Briefing%20on%20DB%20assignment/resources/cw-db" target="_blank">Maven project</a>.
