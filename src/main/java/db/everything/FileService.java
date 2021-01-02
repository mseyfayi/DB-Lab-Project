package db.everything;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class FileService implements Serializable {
    private static FileService instance;
    private Database database;
    private String tableName = "file";

    static FileService getInstance() {
        if (instance == null) {
            instance = new FileService();
        }
        return instance;
    }

    private FileService() {
        database = Database.getInstance();
    }

    private void createTable() {
        database.createTable(
                tableName,
                "Name VARCHAR(255)",
                "Path VARCHAR(255)",
                "Size INT(6) UNSIGNED",
                "Date TIMESTAMP",
                " PRIMARY KEY (Path, Name)"
        );
    }

    private void dropTable() {
        database.dropTable("file");
    }

    void reindexTable() {
        dropTable();
        createTable();
        List<File> allFiles = getFiles();
        insertAll(allFiles);
    }

    void insertAll(List<File> files) {
        for (File file : files) {
            insert(file);
        }
    }

    void insert(File file) {
        database.insert(tableName, new String[]{"Name", "Path", "Size", "Date"}, file.getValuesArray());
    }

    private @Nullable
    String createWhereClaus(@Nullable String text) {
        return text == null || text.length() == 0 ? null : "Name regexp '.*" + text + ".*'";
    }

    List<File> search(@Nullable String text) {
        List<File> list = new ArrayList<>();

        ResultSet rs = database.search(tableName, createWhereClaus(text));
        try {
            while (rs != null && rs.next()) {
                String name = rs.getString("Name");
                String path = rs.getString("Path");
                long size = rs.getLong("Size");
                String date = rs.getString("Date");

                File file = new File(name, path, size, date);
                list.add(file);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    private List<File> getFiles() {
        String myDocuments = System.getProperty("user.home");

        String dateFormat = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        List<File> results = new ArrayList<>();
        try (Stream<Path> files = Files.list(Paths.get(myDocuments))) {
            results = files
                    .map(f -> {
                        try {
                            BasicFileAttributes attribs = Files.readAttributes(f, BasicFileAttributes.class);
                            return new File(
                                    f.toFile().getName(),
                                    f.getParent().toFile().getAbsolutePath(),
                                    attribs.size(),
                                    sdf.format(attribs.creationTime().toMillis())
                            );
                        } catch (IOException ignored) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return results;
    }
}