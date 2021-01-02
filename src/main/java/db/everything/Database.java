package db.everything;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

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
        try {
            statement.execute(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String buildCreateQuery(String name, String[] columns) {
        StringBuilder queryBuilder = new StringBuilder("CREATE TABLE " + name + " (\n");
        for (int i = 0; i < columns.length; i++) {
            queryBuilder.append(columns[i]);
            if (i < columns.length - 1)
                queryBuilder.append(",");
        }
        queryBuilder.append(")");

        return queryBuilder.toString();
    }
}