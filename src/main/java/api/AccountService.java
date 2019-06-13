package api;

import model.Account;
import service.DbService;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;

import static java.util.Collections.singletonMap;

public class AccountService {
    private final DbService dbService;

    public AccountService(DbService dbService) {
        this.dbService = dbService;
    }

    public boolean update(long accountId, String lastName) {
        return 1 == dbService.update(singletonMap("lastName", lastName), "WHERE id = ?", accountId);
    }

    public boolean update(String firstName, String lastName) {
        return 1 == dbService.update(singletonMap("lastName", lastName), "WHERE firstName = ?", firstName);
    }

    public boolean update(Account account, String lastName) {
        return update(account.getId(), lastName);
    }

    public Account find(String firstName) {

        Function<ResultSet, Account> mapper = resultSet -> {
            try {
                return new Account(resultSet.getLong(1),
                        resultSet.getString(2),
                        resultSet.getString(3));
            } catch (SQLException e) {
                return null;
            }
        };

        List<Account> result = dbService.select("WHERE firstName = ?", mapper, firstName);

        if (result.isEmpty()) {
            return null;
        } else {
            return result.get(0);
        }
    }
}