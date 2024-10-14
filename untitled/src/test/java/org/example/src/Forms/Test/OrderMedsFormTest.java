package org.example.src.Forms.Test;

import org.example.src.Forms.Client.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class OrderMedsFormTest {

    @Mock
        Client clientMock;


    private OrderMedsForm orderMedsForm;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        orderMedsForm = new OrderMedsForm(clientMock);


    }


    @Test
    void testOutputEnoughMoney(){
        when(clientMock.getBalance()).thenReturn(901.0);
        when(clientMock.getAccountNumber()).thenReturn("123456789"); // Stubbing getAccountNumber
        when(clientMock.makeTransaction(any(Boolean.class),any(Double.class),any(String.class),any(OrderMedsForm.class))).thenReturn(true);
        orderMedsForm.setTransactionTypeBox(0);
        orderMedsForm.setAmountTextField("100");
        assert(orderMedsForm.performButtonAction());

    }
    @Test
    void testOutputEnoughMoneyExpress(){
        when(clientMock.getBalance()).thenReturn(2221.0);
        when(clientMock.getAccountNumber()).thenReturn("123456789"); // Stubbing getAccountNumber
        when(clientMock.makeTransaction(any(Boolean.class),any(Double.class),any(String.class),any(OrderMedsForm.class))).thenReturn(true);

        orderMedsForm.setTransactionTypeBox(1);
        orderMedsForm.setAmountTextField("2215");
        assert(orderMedsForm.performButtonAction());

    }
    @Test
    void testOutputNotEnoughMoneyExpress(){
        when(clientMock.getBalance()).thenReturn(4.0);
        when(clientMock.getAccountNumber()).thenReturn("123456789"); // Stubbing getAccountNumber
        when(clientMock.makeTransaction(any(Boolean.class),any(Double.class),any(String.class),any(OrderMedsForm.class))).thenReturn(true);

        orderMedsForm.setTransactionTypeBox(1);
        orderMedsForm.setAmountTextField("1");
        assert(!orderMedsForm.performButtonAction());

    }

    @Test
    void testOutputNotEnoughMoney(){
        when(clientMock.getBalance()).thenReturn(4.0);
        when(clientMock.getAccountNumber()).thenReturn("123456789"); // Stubbing getAccountNumber
        when(clientMock.makeTransaction(any(Boolean.class),any(Double.class),any(String.class),any(OrderMedsForm.class))).thenReturn(true);

        orderMedsForm.setTransactionTypeBox(0);
        orderMedsForm.setAmountTextField("5");
        assert(!orderMedsForm.performButtonAction());

    }

}