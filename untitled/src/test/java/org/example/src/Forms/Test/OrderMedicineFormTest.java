package org.example.src.Forms.Test;

import org.example.src.Forms.Client.Client;
import org.example.src.Forms.Client.Medicine;
import org.example.src.Forms.Client.CurrentMedicinesForm;
import org.example.src.Forms.Client.OrderCreditCardForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.swing.*;
import java.sql.Date;
import java.util.ArrayList;

import static org.mockito.Mockito.when;

class OrderMedicineFormTest {

    @Mock
    ArrayList<Medicine> availableCardsMock;  @Mock
    Client clientMock;  @Mock
    CurrentMedicinesForm currentMedicinesFormMock;

    private JComboBox<String> cardsCombo;
    private OrderCreditCardForm orderCreditCardForm;
    Medicine medicine = new Medicine("1234466890423456", new Date(2030, 12, 22),"Visa");
    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(clientMock.getAvailableCards()).thenReturn(   new ArrayList<Medicine>() {
            {
                add(medicine);

            }
        });
        orderCreditCardForm = new OrderCreditCardForm(clientMock, currentMedicinesFormMock);
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