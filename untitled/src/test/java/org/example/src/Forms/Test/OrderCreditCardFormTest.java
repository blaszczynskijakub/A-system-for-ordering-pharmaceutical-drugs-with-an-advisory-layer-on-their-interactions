package org.example.src.Forms.Test;

import org.example.src.Forms.Client.Client;
import org.example.src.Forms.Client.CreditCard;
import org.example.src.Forms.Client.CreditCardsForm;
import org.example.src.Forms.Client.OrderCreditCardForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.swing.*;
import java.sql.Date;
import java.util.ArrayList;

import static org.mockito.Mockito.when;

class OrderCreditCardFormTest {

    @Mock
    ArrayList<CreditCard> availableCardsMock;  @Mock
    Client clientMock;  @Mock
    CreditCardsForm creditCardsFormMock;

    private JComboBox<String> cardsCombo;
    private OrderCreditCardForm orderCreditCardForm;
    CreditCard creditCard = new CreditCard("1234466890423456", new Date(2030, 12, 22),"Visa");
    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(clientMock.getAvailableCards()).thenReturn(   new ArrayList<CreditCard>() {
            {
                add(creditCard);

            }
        });
        orderCreditCardForm = new OrderCreditCardForm(clientMock, creditCardsFormMock);
    }


    @Test
    void checkCardsTest()
    {
        for (int i = 0; i < orderCreditCardForm.getCardsComboBox().getItemCount(); i++) {
            String item = orderCreditCardForm.getCardsComboBox().getItemAt(i).toString();


            assert("1234466890423456".equals(item.split(" ")[0]));


        }
    }
}