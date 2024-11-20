
package org.example.src.Forms.Test;

import org.example.src.Forms.Client.Medicine;
import org.example.src.Forms.DataHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.Mockito.*;

public class DataHandlerTest {

    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private ResultSet mockResultSet;

    private DataHandler dataHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dataHandler = new DataHandler() {};
    }

    @Test
    void testPopulateComboBox_Success() throws SQLException {
        String queryData = "TestDrug";
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("drug_name")).thenReturn("TestDrug");
        when(mockResultSet.getString("producent_name")).thenReturn("TestProducer");
        when(mockResultSet.getInt("price")).thenReturn(100);
        when(mockResultSet.getString("drug_type")).thenReturn("painkiller");

        ArrayList<Medicine> medicines = dataHandler.populateComboBox(queryData, mockPreparedStatement, mockConnection, mockResultSet, 1);

        assertEquals(1, medicines.size());
        assertEquals("TestDrug", medicines.get(0).getDrugName());
        verify(mockPreparedStatement, atLeastOnce()).executeQuery();
    }

    @Test
    void testPopulateComboBoxWithClients_Success() throws SQLException {
        String queryData = "John Doe";
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString("first_name")).thenReturn("John");
        when(mockResultSet.getString("last_name")).thenReturn("Doe");
        when(mockResultSet.getString("address")).thenReturn("123 Main St");
        when(mockResultSet.getString("city")).thenReturn("TestCity");
        when(mockResultSet.getString("id")).thenReturn("1");

        ArrayList<String> clients = dataHandler.populateComboBoxWithClients(queryData, mockPreparedStatement, mockConnection, mockResultSet);

        assertEquals(1, clients.size());
        assertTrue(clients.get(0).contains("John Doe"));
        verify(mockPreparedStatement, atLeastOnce()).executeQuery();
    }

    @Test
    void testPopulateComboBox_NoResults() throws SQLException {
        String queryData = "NoMatch";
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        ArrayList<Medicine> medicines = dataHandler.populateComboBox(queryData, mockPreparedStatement, mockConnection, mockResultSet, 1);

        assertTrue(medicines.isEmpty());
        verify(mockPreparedStatement, atLeastOnce()).executeQuery();
    }

}
