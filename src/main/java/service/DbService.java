package service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class DbService implements AutoCloseable {
    private Connection connection;
    private String tableName;

    public DbService(Connection connection, String tableName) {
        this.connection = connection;
        this.tableName = tableName;
    }

    /**
     * @param fieldValueMapping map of fields and their new values
     * @param criteria          String like "WHERE name = ?"
     * @param criteriaValues    array of values for '?' in criteria
     * @return
     */
    public long update(Map<String, Object> fieldValueMapping, String criteria, Object... criteriaValues) {
        //generate what to set
        StringBuilder whatToSet = new StringBuilder();
        for (String field : fieldValueMapping.keySet()) {
            whatToSet.append(field).append(" = ?, ");
        }
        whatToSet.reverse().delete(0, 2).reverse();

        String query = "UPDATE " + tableName +
                " SET " + whatToSet.toString() +
                " " + criteria;

        int parameterIdx = 1;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (Object value : fieldValueMapping.values()) {
                setParameter(statement, value, parameterIdx++);
            }
            for (Object value : criteriaValues) {
                setParameter(statement, value, parameterIdx++);
            }
            statement.execute();
            return statement.getUpdateCount();
        } catch (SQLException e) {
            return 0;
        }
    }

    private void setParameter(PreparedStatement statement, Object value, int position) throws SQLException {

        if (value instanceof String) {
            statement.setString(position, (String) value);
        } else if (value instanceof Integer) {
            statement.setInt(position, (Integer) value);
        } else if (value instanceof Long) {
            statement.setLong(position, (Long) value);
        } else if (value == null) {
            statement.setNull(position, java.sql.Types.NULL);
        } else {
            throw new RuntimeException("Can't use type " + value.getClass() + "in SQL query");
        }
    }

    /**
     * @param criteria       String like "WHERE name = ? and color = ?"
     * @param mapper         Function to build an entity from ResultSet
     * @param criteriaValues array of values for '?' in criteria
     * @param <T>            type of entity we are collecting
     * @return
     */
    public <T> List<T> select(String criteria, Function<ResultSet, T> mapper, Object... criteriaValues) {

        ResultSet rs;

        String query = "SELECT * FROM " + tableName + " " + criteria;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            int parameterIdx = 1;
            for (Object value : criteriaValues) {
                setParameter(statement, value, parameterIdx++);
            }
            rs = statement.executeQuery();
        } catch (SQLException e) {
            return Collections.emptyList();
        }

        List<T> results = new ArrayList<>();
        if (rs != null) {
            try {
                while (rs.next()) {
                    results.add(mapper.apply(rs));
                }
            } catch (SQLException e) {
                return Collections.emptyList();
            }
        }
        return results;
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }
}
