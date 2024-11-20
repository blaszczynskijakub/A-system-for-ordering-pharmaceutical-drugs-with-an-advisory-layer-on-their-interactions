
package org.example.src.Forms.Test;

import org.example.src.Forms.Employee.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
    void testAddNewAccount_Success() throws SQLException {
        String passwordHash = "hashedPassword";
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        doNothing().when(mockPreparedStatement).setString(anyInt(), anyString());
        doNothing().when(mockPreparedStatement).setInt(anyInt(), anyInt());

        employee.addNewAccount();

        verify(mockPreparedStatement, atLeastOnce()).executeUpdate();
    }

    @Test
    void testUpdateCredentials_Success() throws SQLException {
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        employee.updateCredentials("Jane", "Smith", "456 Elm St", "NewCity", "1");

        verify(mockPreparedStatement, atLeastOnce()).executeUpdate();
    }

    @Test
    void testDeleteAccount_Success() throws SQLException {
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

}
