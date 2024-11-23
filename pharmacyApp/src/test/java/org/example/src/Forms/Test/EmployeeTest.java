package org.example.src.Forms.Test;

import org.example.src.Forms.Employee.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

public class EmployeeTest {

    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private ResultSet mockResultSet;

    private Employee employee;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        employee = new Employee(1, "John", "Doe", "Manager", "TestBranch", "123 Branch St", mockConnection);
    }



    @Test
    void testAddNewAccount_SQLException() throws SQLException {
        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Database error"));

        employee.addNewAccount();

        verify(mockPreparedStatement, times(1)).executeUpdate();
    }


    @Test
    void testUpdateCredentials() throws SQLException {
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        employee.updateCredentials("Jane", "Smith", "456 Elm St", "NewCity", "1");

        verify(mockPreparedStatement, times(1)).executeUpdate();

        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);

        verify(mockPreparedStatement, atLeastOnce()).setString(anyInt(), valueCaptor.capture());

        assertEquals("Jane", valueCaptor.getAllValues().get(0));
        assertEquals("Smith", valueCaptor.getAllValues().get(1));
        assertEquals("456 Elm St", valueCaptor.getAllValues().get(2));
        assertEquals("NewCity", valueCaptor.getAllValues().get(3));

        assertEquals(4, valueCaptor.getAllValues().size());
    }



    @Test
    void testUpdateCredentials_SQLException() throws SQLException {
        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Database error"));

        employee.updateCredentials("Jane", "Smith", "456 Elm St", "NewCity", "1");

        verify(mockPreparedStatement, times(1)).executeUpdate();
    }

    @Test
    void testDeleteAccount() throws SQLException {
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        employee.deleteAccount(1);

        verify(mockPreparedStatement, times(2)).executeUpdate();
    }



    @Test
    void testOpenManageAccountForm() {
        employee.openManageAccountForm();

        assertFalse(employee.isVisible());
    }

    @Test
    void testOpenTransactionHistory() {
        employee.openTransactionHistory();

        assertFalse(employee.isVisible());
    }

    @Test
    void testLogout() {
        employee.logout();

        assertFalse(employee.isVisible());
    }

    @Test
    void testLogout_ConnectionCloseFailure() throws SQLException {
        doThrow(new SQLException("Connection close error")).when(mockConnection).close();

        employee.logout();

        assertFalse(employee.isVisible());
    }
}
