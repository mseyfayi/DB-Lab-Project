package db.everything;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class FileService {
    private static FileService instance;
    private Database database;
    private String tableName;

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
        tableName = "file";
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