package edu.uob;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import edu.uob.InterpretException.FailedReadingFileException;

public class UseCMD extends DBCmd {

    public UseCMD() {
        super();
        currentIdx = 1;
    }

    @Override
    public String query(DBServer server) {
        dbName = server.getTokens()[currentIdx].toLowerCase();
        String path = server.getStorageFolderPath() + File.separator + dbName;
        try {
            readDbFromFile(path, server);
            return "[OK]";
        } catch (Exception e) {
            return "[ERROR]: Failed using database " + dbName;
        }
    }

    private void readDbFromFile(String filePath, DBServer server) throws FailedReadingFileException, FileNotFoundException {
        File directory = new File(filePath);
        if (!directory.exists() || !directory.isDirectory()) {
            throw new FileNotFoundException("File not found or is not a directory: " + filePath);
        }
        // init database
        server.setDB(new Database(directory.getName()));

        try {
            final FilenameFilter tableFilter = (file, name) -> !name.contains("_config");
            final FilenameFilter configFilter = (file, name) -> name.contains("_config");
            File[] tableFiles = directory.listFiles(tableFilter);
            File[] configFiles = directory.listFiles(configFilter);
            for (File file : tableFiles != null ? tableFiles : new File[0]) {
                readTableFile(file, configFiles, filePath, server);
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private void readTableFile(File file, File[] configFiles, String filePath, DBServer server) throws FailedReadingFileException {
        int lastPk = readLastPkFromFile(file.getName(), configFiles);
        Table table = new Table(file.getName().split(".tab")[0], lastPk);
        try (FileReader reader = new FileReader(file);
             BufferedReader bufferedReader = new BufferedReader(reader)
             ) {
            String lineString;
            boolean isHeader = true;
            int rowCount = 0;
            while ((lineString = bufferedReader.readLine()) != null) {
                String[] columns = lineString.split("\t");
                if (isHeader) {
                    for (String column : columns) {
                        table.addColumn(column);
                    }
                    isHeader = false;
                } else {
                    ArrayList<String> row = new ArrayList<>(Arrays.asList(columns));
                    table.addRowWithID(row);
                    rowCount++;
                }
            }
            if (lastPk == -1) {
                table.setLastPrimaryKey(rowCount);
                String tablePath = filePath + File.separator + file.getName().split(".tab")[0];
                saveConfig(tablePath, rowCount);
            }
            server.getDB().addTable(table);
        } catch (Exception e) {
            throw new FailedReadingFileException("Table: " + table.getName());
        }
    }

    private int readLastPkFromFile(String fileName, File[] files) throws FailedReadingFileException {
        File configFile = null;
        for (File file : files) {
            if (file.getName().equals(fileName.split(".tab")[0] + "_config.tab")) {
                configFile = file;
                break;
            }
        }
        if (configFile == null) { return -1; }
        try (FileReader reader = new FileReader(configFile);
             BufferedReader bufferedReader = new BufferedReader(reader)
             ) {
            String lineString = bufferedReader.readLine();
            return Integer.parseInt(lineString.split("=")[1]);
        } catch (Exception e) {
            throw new FailedReadingFileException("config file");
        }
    }
}
