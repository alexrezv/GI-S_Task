package api;

import model.Account;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import service.DbService;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

public class AccountServiceTest {

    @Test
    public void shouldFindAccount() {
        ArgumentCaptor<Function<ResultSet, Object>> captor = ArgumentCaptor.forClass(Function.class);
        DbService dbServiceMock = Mockito.mock(DbService.class);
        AccountService accountService = new AccountService(dbServiceMock);

        doReturn(singletonList(new Account(0, "John", "Hopkins")))
                .when(dbServiceMock).select(any(), any(), any());
        assertEquals(new Account("John", "Hopkins"), accountService.find("John"));

        Mockito.verify(dbServiceMock).select(eq("WHERE firstName = ?"), captor.capture(), eq("John"));
        Function<ResultSet, Object> result = captor.getValue();

        try {
            ResultSet resultSetMock = Mockito.mock(ResultSet.class);
            Mockito.when(resultSetMock.getLong(1)).thenReturn(0L);
            Mockito.when(resultSetMock.getString(2)).thenReturn("John");
            Mockito.when(resultSetMock.getString(3)).thenReturn("Hopkins");
            Mockito.when(resultSetMock.next()).thenReturn(true).thenReturn(false);

            assertEquals(result.apply(resultSetMock), new Account(0L, "John", "Hopkins"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void shouldUpdateAccountByFirstName() {
        DbService dbServiceMock = Mockito.mock(DbService.class);
        doReturn(1L).when(dbServiceMock).update(singletonMap("lastName", "Petrov"),
                "WHERE firstName = ?", "Bart");

        AccountService accountService = new AccountService(dbServiceMock);
        assertTrue(accountService.update("Bart", "Petrov"));
    }

    @Test
    public void shouldUpdateAccountById() {
        DbService dbServiceMock = Mockito.mock(DbService.class);
        doReturn(1L).when(dbServiceMock).update(singletonMap("lastName", "Petrov"),
                "WHERE id = ?", 1L);

        AccountService accountService = new AccountService(dbServiceMock);
        assertTrue(accountService.update(1L, "Petrov"));
    }
}
