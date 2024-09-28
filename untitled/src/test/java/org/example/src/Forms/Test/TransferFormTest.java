package org.example.src.Forms.Test;

import org.example.src.Forms.Client.Client;
import org.example.src.Forms.Client.TransferForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class TransferFormTest {

    @Mock
        Client clientMock;


    private TransferForm transferForm;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        transferForm = new TransferForm(clientMock);


    }


    @Test
    void testOutputEnoughMoney(){
        when(clientMock.getBalance()).thenReturn(901.0);
        when(clientMock.getAccountNumber()).thenReturn("123456789"); // Stubbing getAccountNumber
        when(clientMock.makeTransaction(any(Boolean.class),any(Double.class),any(String.class),any(TransferForm.class))).thenReturn(true);
        transferForm.setTransactionTypeBox(0);
        transferForm.setAmountTextField("100");
        assert(transferForm.performButtonAction());

    }
    @Test
    void testOutputEnoughMoneyExpress(){
        when(clientMock.getBalance()).thenReturn(2221.0);
        when(clientMock.getAccountNumber()).thenReturn("123456789"); // Stubbing getAccountNumber
        when(clientMock.makeTransaction(any(Boolean.class),any(Double.class),any(String.class),any(TransferForm.class))).thenReturn(true);

        transferForm.setTransactionTypeBox(1);
        transferForm.setAmountTextField("2215");
        assert(transferForm.performButtonAction());

    }
    @Test
    void testOutputNotEnoughMoneyExpress(){
        when(clientMock.getBalance()).thenReturn(4.0);
        when(clientMock.getAccountNumber()).thenReturn("123456789"); // Stubbing getAccountNumber
        when(clientMock.makeTransaction(any(Boolean.class),any(Double.class),any(String.class),any(TransferForm.class))).thenReturn(true);

        transferForm.setTransactionTypeBox(1);
        transferForm.setAmountTextField("1");
        assert(!transferForm.performButtonAction());

    }

    @Test
    void testOutputNotEnoughMoney(){
        when(clientMock.getBalance()).thenReturn(4.0);
        when(clientMock.getAccountNumber()).thenReturn("123456789"); // Stubbing getAccountNumber
        when(clientMock.makeTransaction(any(Boolean.class),any(Double.class),any(String.class),any(TransferForm.class))).thenReturn(true);

        transferForm.setTransactionTypeBox(0);
        transferForm.setAmountTextField("5");
        assert(!transferForm.performButtonAction());

    }

}