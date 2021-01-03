package db.everything;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Objects;

public class IFile {
    private String name;
    private String path;
    private long size;
    private String date;

    @Override
    public String toString() {
        return path + " " + name;
    }

    IFile(String name, String path, long size, String date) {
        this.name = name;
        this.path = path;
        this.size = size;
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IFile file = (IFile) o;
        return name.equals(file.name) &&
                path.equals(file.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, path);
    }

    String[] getValuesArray() {
        return new String[]{name, path, size + "", date};
    }

    static IFile getIFileFromPath(Path path) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            BasicFileAttributes attribs = Files.readAttributes(path, BasicFileAttributes.class);
            return new IFile(
                    path.toFile().getName(),
                    path.getParent().toFile().getAbsolutePath(),
                    attribs.size(),
                    sdf.format(attribs.creationTime().toMillis())
            );
        } catch (IOException ignored) {
            return null;
        }
    }
}