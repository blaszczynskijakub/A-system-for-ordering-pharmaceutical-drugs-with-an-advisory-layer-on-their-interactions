package org.example.src.Forms.Test;


import org.example.src.Forms.Client.Client;
import org.example.src.Forms.Client.Medicine;
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

public class ClientTest {

    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private ResultSet mockResultSet;

    private Client client;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        client = new Client(1, "John", "Doe", "123 Main St", "TestCity", mockConnection);
    }

    @Test
    void testLoadMedicinesData_Success() throws SQLException {
        when(mockResultSet.next())
                .thenReturn(true)
                .thenReturn(false);
        when(mockResultSet.getInt("drug_id")).thenReturn(1);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getInt("transaciont_id")).thenReturn(100);
        when(mockResultSet.getString("drug_name")).thenReturn("TestMedicine");
        when(mockResultSet.getString("producent")).thenReturn("TestProducer");
        when(mockResultSet.getInt("price")).thenReturn(200);
        when(mockResultSet.getString("status")).thenReturn("inRealisation");
        when(mockResultSet.getString("drug_type")).thenReturn("painkiller");

        client.loadMedicinesData();
        ArrayList<Medicine> medicines = client.getMedicines();

        assertEquals(1, medicines.size());
        assertEquals("TestMedicine", medicines.get(0).getDrugName());
        verify(mockPreparedStatement, atLeastOnce()).executeQuery();
    }

    @Test
    void testAddCardToAccount_Success() throws SQLException {
        Medicine testMedicine = new Medicine(1, "TestDrug", "TestProducer", 100, Medicine.OrderStatus.inRealisation, "painkiller");
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        client.addCardToAccount(testMedicine);

        assertEquals(1, client.getMedicines().size());
        assertEquals("TestDrug", client.getMedicines().get(0).getDrugName());
        verify(mockPreparedStatement, atLeastOnce()).executeUpdate();
    }

    @Test
    void testDeleteCard_Success() throws SQLException {
        Medicine testMedicine = new Medicine(1, "TestDrug", "TestProducer", 100, Medicine.OrderStatus.inRealisation, "painkiller");
        client.getMedicines().add(testMedicine);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        client.deleteCard(testMedicine);

        assertTrue(client.getMedicines().isEmpty());
        verify(mockPreparedStatement, atLeastOnce()).executeUpdate();
    }

    @Test
    void testDeleteAccount_Success() throws SQLException {
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        client.deleteAccount();

        verify(mockPreparedStatement, times(2)).executeUpdate();
    }
}
