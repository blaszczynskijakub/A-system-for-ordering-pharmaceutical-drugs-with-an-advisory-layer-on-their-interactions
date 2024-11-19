package org.example.src.Forms.Test;

import org.example.src.Forms.Client.AdvisoryLayer;
import org.example.src.Forms.Client.Medicine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AdvisoryLayerTest {

    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockStatement;
    @Mock
    private ResultSet mockResultSet;

    private AdvisoryLayer advisoryLayer;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        advisoryLayer = new AdvisoryLayer(mockConnection);
    }

    @Test
    void testCheckForInteractions_NoAlcoholWarning_NoInteractions() throws SQLException {
        Medicine testMedicine = new Medicine(1, "TestDrug", "TestProducer", 100, Medicine.OrderStatus.inRealisation, "painkiller");

        when(mockResultSet.next()).thenReturn(false);

        boolean result = advisoryLayer.checkForInteractions(1, testMedicine);

        assertTrue(result, "The method should return true when no interactions or warnings are found.");
        verify(mockConnection, atLeastOnce()).prepareStatement(anyString());
    }

    @Test
    void testFindAlternativeMedicine_AlternativeExistsWithMockedInteraction() throws SQLException {
        Medicine testMedicine = new Medicine(1, "TestDrug", "TestProducer", 100, Medicine.OrderStatus.inRealisation, "painkiller");

        when(mockResultSet.next())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);

        // first is the drug we want to add and the second is alternative we found and want to add
        when(mockResultSet.getInt("id")).thenReturn(2, 3);
        when(mockResultSet.getString("drug_name")).thenReturn("ConflictingDrug", "AlternativeDrug");
        when(mockResultSet.getString("producent_name")).thenReturn("ConflictingProducer", "AltProducer");
        when(mockResultSet.getInt("price")).thenReturn(120, 150);
        when(mockResultSet.getString("drug_type")).thenReturn("painkiller", "painkiller");
        when(mockResultSet.getBoolean("kolestypol")).thenReturn(true, false);
        when(mockResultSet.getBoolean("acidity")).thenReturn(false, false);

        ResultSet mockProblemDrug = mock(ResultSet.class);

        // attributes of the drug we already have related to account
        when(mockProblemDrug.getBoolean("kolestypol")).thenReturn(false);
        when(mockProblemDrug.getBoolean("acidity")).thenReturn(true);

        AdvisoryLayer spyAdvisoryLayer = spy(advisoryLayer);

        doAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                ResultSet resultSet = invocation.getArgument(0);
                ResultSet problemDrug = invocation.getArgument(1);
                return resultSet.getBoolean("kolestypol") && problemDrug.getBoolean("acidity");
            }
        }).when(spyAdvisoryLayer).hasInteraction(any(ResultSet.class), any(ResultSet.class));

        Medicine alternative = spyAdvisoryLayer.findAlternativeMedicine(testMedicine, mockProblemDrug);

        assertNotNull(alternative, "An alternative medicine should be found after resolving conflicts.");
        assertEquals("AlternativeDrug", alternative.getDrugName());
        assertEquals("AltProducer", alternative.getProducent());
        verify(mockStatement, atLeastOnce()).executeQuery();

    }



    @Test
    void testFindAlternativeMedicine_NoAlternativeExists() throws SQLException {
        Medicine testMedicine = new Medicine(1, "TestDrug", "TestProducer", 100, Medicine.OrderStatus.inRealisation, "painkiller");

        when(mockResultSet.next()).thenReturn(false);

        Medicine alternative = advisoryLayer.findAlternativeMedicine(testMedicine, mockResultSet);

        assertNull(alternative, "No alternative should be found if none exist.");
        verify(mockStatement, atLeastOnce()).executeQuery();
    }

    @Test
    void testFindAlternativeMedicine_MultipleAlternatives() throws SQLException {
        Medicine testMedicine = new Medicine(1, "TestDrug", "TestProducer", 100, Medicine.OrderStatus.inRealisation, "painkiller");

        when(mockResultSet.next())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);

        when(mockResultSet.getInt("id")).thenReturn(2, 3);
        when(mockResultSet.getString("drug_name")).thenReturn("AlternativeDrug1", "AlternativeDrug2");
        when(mockResultSet.getString("producent_name")).thenReturn("AltProducer1", "AltProducer2");
        when(mockResultSet.getInt("price")).thenReturn(150, 120);
        when(mockResultSet.getString("drug_type")).thenReturn("painkiller", "painkiller");

        AdvisoryLayer spyAdvisoryLayer = spy(advisoryLayer);
        doReturn(false).when(spyAdvisoryLayer).hasInteraction(any(ResultSet.class), any(ResultSet.class));

        Medicine alternative = spyAdvisoryLayer.findAlternativeMedicine(testMedicine, mockResultSet);

        assertNotNull(alternative, "At least one alternative medicine should be found.");
        assertEquals("AlternativeDrug1", alternative.getDrugName());
        assertEquals("AltProducer1", alternative.getProducent());
        verify(mockStatement, atLeastOnce()).executeQuery();
    }
}
