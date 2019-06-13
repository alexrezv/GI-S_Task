package service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;

public class DbServiceTest {

    private static Connection connection;
    private DbService dbService;

    private Connection getNewConnection() throws SQLException {
        String url = "jdbc:h2:mem:test";
        String user = "sa";
        String passwd = "sa";
        return DriverManager.getConnection(url, user, passwd);
    }

    private int executeUpdate(String query) throws SQLException {
        return connection.createStatement().executeUpdate(query);
    }

    private class Capital {
        long id;
        String name;
        String country;
        int population;

        Capital(String name, String country, int population) {
            this.name = name;
            this.country = country;
            this.population = population;
        }

        public boolean equals(Object obj) {
            return this.name.equals(((Capital) obj).name) &&
                    this.country.equals(((Capital) obj).country) &&
                    this.population == ((Capital) obj).population;
        }
    }

    private void createCapitalsTable() throws SQLException {
        String capitalsTableQuery = "CREATE TABLE capitals (" +
                "id LONG PRIMARY KEY," +
                "name VARCHAR(30)," +
                "country VARCHAR(30)," +
                "population INTEGER," +
                "UNIQUE(country)" +
                ")";
        executeUpdate(capitalsTableQuery);

        String[] capitals = {"Moscow", "Washington DC", "London", "Monaco"};
        String[] countries = {"Russia", "USA", "Great Britain", "Principality of Monaco"};
        int[] population = {11_920_000, 633_427, 8_136_000, 38_695};

        for (int i = 0; i < countries.length; i++) {
            executeUpdate("INSERT INTO capitals " +
                    "VALUES (" + i + ", '" + capitals[i] + "', '" +
                    countries[i] + "', '" + population[i] + "')");
        }
    }

    @Before
    public void init() throws SQLException {
        connection = getNewConnection();
        dbService = new DbService(connection, "capitals");
        createCapitalsTable();
    }

    @After
    public void close() throws SQLException {
        connection.close();
    }

    @Test
    public void shouldSelectAllFromTable() {
        List<Capital> expected = new ArrayList<>();
        expected.add(new Capital("Moscow", "Russia", 11_920_000));
        expected.add(new Capital("Washington DC", "USA", 633_427));
        expected.add(new Capital("London", "Great Britain", 8_136_000));
        expected.add(new Capital("Monaco", "Principality of Monaco", 38_695));

        Function<ResultSet, Capital> mapper = resultSet -> {
            try {
                return new Capital(resultSet.getString(2),
                        resultSet.getString(3),
                        resultSet.getInt(4));
            } catch (SQLException e) {
                return null;
            }
        };

        List<Capital> actual = dbService.select("", mapper);
        for (int i = 0; i < actual.size(); i++) {
            assertEquals(actual.get(i), expected.get(i));
        }
    }

    @Test
    public void shouldUpdateCapital() {
        assertEquals(1, dbService.update(singletonMap("population", 123465),
                "WHERE name = ?", "London"));
    }

}
