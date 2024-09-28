package org.example.src.Forms.Test;

import org.example.src.Forms.Client.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;

public class ClientTest {

    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockStatement;
    @Mock
    private ResultSet mockResultSet;

    private Client client;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        //when(mockConnection.createStatement()).thenReturn(mockStatement);
        client = new Client(1,"Jacek", "Placek", "Plackowa", "Wroclaw", 42.42, "PL12345678954313", 1);
        client.setConnection(mockConnection);
    }

    @Test
    void testUpdateClientInfo() throws SQLException{
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true); // Simulate that there is a row in the ResultSet
        when(mockResultSet.getString(1)).thenReturn("John"); // Set expected values for your test
        when(mockResultSet.getString(2)).thenReturn("Doe");
        when(mockResultSet.getString(3)).thenReturn("123 Main St");
        when(mockResultSet.getString(4)).thenReturn("City");
        when(mockResultSet.getDouble(5)).thenReturn(1000.0);

        client.updateClientInfo();

        assertEquals("John", client.getFirstName());
        assertEquals("Doe", client.getLastName());
        assertEquals("123 Main St", client.getFirstName());
        assertEquals("City", client.getLastName());
        assertEquals(1000.0, client.getBalance());
    }
}
