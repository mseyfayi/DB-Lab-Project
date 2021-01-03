package db.everything;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;

class FileService {
    private static FileService instance;
    private final Path path = Paths.get(System.getProperty("user.home"));
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
        List<IFile> allFiles = getFiles();
        insertAll(allFiles);
    }

    private void insertAll(List<IFile> files) {
        for (IFile file : files) {
            insert(file);
        }
    }

    private void insert(IFile file) {
        database.insert(tableName, new String[]{"Name", "Path", "Size", "Date"}, file.getValuesArray());
    }

    private @Nullable
    String createSearchWhereClaus(@Nullable String text, boolean isMatchCase) {
        if (text == null || text.length() == 0)
            return null;
        else if (!isMatchCase)
            return "lower(Name) regexp '.*" + text.toLowerCase() + ".*'";
        else
            return "Name regexp '.*" + text + ".*'";
    }

    List<IFile> search(@Nullable String text, @Nullable String sort, boolean isMatchCase) {
        List<IFile> list = new ArrayList<>();

        ResultSet rs = database.search(tableName, createSearchWhereClaus(text, isMatchCase), sort);
        try {
            while (rs != null && rs.next()) {
                String name = rs.getString("Name");
                String path1 = rs.getString("Path");
                long size = rs.getLong("Size");
                String date = rs.getString("Date");

                IFile file = new IFile(name, path1, size, date);
                list.add(file);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    private List<IFile> getFiles() {
        List<IFile> results = new ArrayList<>();
        try (Stream<Path> files = Files.list(path)) {
            results = files
                    .map(IFile::getIFileFromPath)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return results;
    }


    private String createDeleteWhereClaus(String name) {
        return String.format("Path = '%s' AND Name = '%s'", path.toString(), name);
    }

    private void entryDelete(String name) {
        database.delete(tableName, createDeleteWhereClaus(name));
    }

    private void entryCreate(String name) {
        IFile newFile = IFile.getIFileFromPath(path.resolve(name));
        assert newFile != null;
        insert(newFile);
    }

    void fileWatch(Runnable callback) {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            path.register(watchService, ENTRY_CREATE, ENTRY_DELETE);
            WatchKey watchKey;
            while ((watchKey = watchService.take()) != null) {
                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    String fileName = event.context().toString();
                    WatchEvent.Kind<?> kind = event.kind();

                    if (fileName.equals("everythingdb.mv.db")) {
                        continue;
                    }

                    if (kind == ENTRY_DELETE) {
                        entryDelete(fileName);
                    } else {
                        entryCreate(fileName);
                    }

                    callback.run();
                }
                watchKey.reset();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}