package db.everything;

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

    public IFile(String name, String path, long size, String date) {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String[] getValuesArray() {
        return new String[]{name, path, size + "", date};
    }
}