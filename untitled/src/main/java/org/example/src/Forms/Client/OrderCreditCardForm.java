package org.example.src.Forms.Client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;

public class OrderCreditCardForm extends JFrame implements ActionListener {
    private JPanel mainPanel;
    private JLabel titleLabel;
    private JLabel cardsLabel;

    public JComboBox<String> getCardsComboBox() {
        return cardsComboBox;
    }

    private JComboBox<String> cardsComboBox;
    private JButton performButton;
    private JButton quitButton;
    private final ArrayList<Medicine> availableCards;

    private final Client parent;
    private final CurrentMedicinesForm cardsForm;

    public OrderCreditCardForm(Client parent, CurrentMedicinesForm cardsForm) throws SQLException {
        this.parent = parent;
        this.cardsForm = cardsForm;

        performButton.addActionListener(this);
        quitButton.addActionListener(this);

        availableCards = parent.getAvailableCards();

        for(Medicine card : availableCards){
            cardsComboBox.addItem(card.toString());
        }


        setContentPane(mainPanel);
        setVisible(true);
        pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == performButton){
            parent.addCardToAccount(availableCards.get(cardsComboBox.getSelectedIndex()));

            ArrayList<String> dataList = new ArrayList<>();
            for(Medicine card : parent.getCreditCards()){
                dataList.add(card.toString());
            }
            String[] data = new String[dataList.size()];
            dataList.toArray(data);

            cardsForm.getList1().setListData(data);
            cardsForm.repaint();

            dispose();
        }
        if(e.getSource() == quitButton){
            dispose();
        }
    }
}
