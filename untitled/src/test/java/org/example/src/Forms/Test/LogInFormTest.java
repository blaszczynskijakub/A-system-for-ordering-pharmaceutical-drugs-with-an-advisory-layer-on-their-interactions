package org.example.src.Forms.Test;

import org.example.src.Forms.logging.LogInForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

public class LogInFormTest {

    @Mock
    private Connection mockConnection;
    @Mock
    private Statement mockStatement;
    @Mock
    private ResultSet mockResultSet;


    private LogInForm logInForm;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        logInForm = new LogInForm();
        logInForm.setConnection(mockConnection);
    }

    @Test
    void testEmployeeLoginSuccess() throws Exception {
        // Configure mock to meet the login condition
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false); // First call true, subsequent false
        when(mockResultSet.getString(2)).thenReturn("Pietruszka");
        when(mockResultSet.getString(3)).thenReturn("Kumpel");

        // Simulate entering credentials
        logInForm.getLoginField().setText("Pietruszka Kumpel");
        logInForm.getPasswordField().setText("PRACOWNIK");
        logInForm.handleLogin();

        assertEquals("EMPLOYEE", logInForm.getUserType());
    }

    @Test
    void testClientLoginSuccess() throws Exception {
        // Configure mock to meet the login condition
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString(2)).thenReturn("Ola");
        when(mockResultSet.getString(3)).thenReturn("Makota");

        // Simulate entering credentials
        logInForm.getLoginField().setText("Ola Makota");
        logInForm.getPasswordField().setText("KLIENT");
        logInForm.handleLogin();

        assertEquals("CLIENT", logInForm.getUserType());
    }

    @Test
    void testLoginFailure() throws Exception {
        // Configure mock to simulate failed login
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Simulate entering invalid credentials
        logInForm.getLoginField().setText("Invalid User");
        logInForm.getPasswordField().setText("InvalidPass");
        logInForm.handleLogin();


        assertEquals("", logInForm.getUserType());
    }
}
