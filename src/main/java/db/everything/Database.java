package db.everything;

import com.sun.istack.internal.Nullable;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

public class Database {
    private static Database instance;
    private final String CONNECTION_STRING = "jdbc:h2:~/everythingdb;user=sa";
    private Statement statement = DriverManager.getConnection(CONNECTION_STRING).createStatement();

    private Database() throws SQLException {
    }

    static Database getInstance() {
        if (instance == null) {
            try {
                instance = new Database();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    void createTable(String name, String... columns) {
        String query = buildCreateQuery(name, columns);
        execute(query);
    }

    void dropTable(String name) {
        String query = "DROP TABLE " + name;
        execute(query);

    }

    private String buildCreateQuery(String name, String[] columns) {
        StringBuilder queryBuilder = new StringBuilder("CREATE TABLE " + name + " (");
        buildComaSeparatedString(columns, queryBuilder);
        queryBuilder.append(")");

        return queryBuilder.toString();
    }

    void insert(String tableName, String[] columns, String[] values) {
        String[] newValues = Arrays.stream(values).map(v -> '\'' + v + '\'').toArray(String[]::new);
        String query = buildInsertQuery(tableName, columns, newValues);
        execute(query);
    }

    private void execute(String query) {
        System.out.println(query);
        try {
            statement.execute(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private ResultSet executeQuery(String query) {
        System.out.println(query);
        try {
            return statement.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String buildInsertQuery(String tableName, String[] columns, String[] values) {
        StringBuilder queryBuilder = new StringBuilder("INSERT INTO " + tableName + " (");
        buildComaSeparatedString(columns, queryBuilder);
        queryBuilder.append(") " + "VALUES (");
        buildComaSeparatedString(values, queryBuilder);
        queryBuilder.append(")");

        return queryBuilder.toString();
    }

    private void buildComaSeparatedString(String[] columns, StringBuilder queryBuilder) {
        for (int i = 0; i < columns.length; i++) {
            queryBuilder.append(columns[i]);
            if (i < columns.length - 1)
                queryBuilder.append(", ");
        }
    }

    private String buildSelectQuery(String tableName, @Nullable String whereClaus, @Nullable String sort) {
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM " + tableName);

        if (whereClaus != null && whereClaus.length() > 0) {
            queryBuilder.append(" WHERE ");
            queryBuilder.append(whereClaus);
        }

        if (sort != null && sort.length() > 0) {
            queryBuilder.append(" ORDER BY ");
            queryBuilder.append(sort);
        }

        return queryBuilder.toString();
    }

    ResultSet search(String tableName, @Nullable String whereClaus, @Nullable String sort) {
        String query = buildSelectQuery(tableName, whereClaus, sort);
        return executeQuery(query);
    }
}