package org.example.src.Forms.Client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    private final ArrayList<CreditCard> availableCards;

    private final Client parent;
    private final CreditCardsForm cardsForm;

    public OrderCreditCardForm(Client parent, CreditCardsForm cardsForm){
        this.parent = parent;
        this.cardsForm = cardsForm;

        performButton.addActionListener(this);
        quitButton.addActionListener(this);

        availableCards = parent.getAvailableCards();

        for(CreditCard card : availableCards){
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
            for(CreditCard card : parent.getCreditCards()){
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
