package org.example.src.Forms.Client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;

public class CurrentMedicinesForm extends JFrame implements ActionListener {
    private JPanel mainPanel;
    private JLabel titleLabel;
    private JLabel cardsLabel;
    private JButton orderCardButton;
    private JButton quitButton;
    private JList<String> list1;
    private JButton deleteButton;
    private JList list2;
    private JList list3;
    private JButton confReceiveButton;

    private final Client parent;

    public CurrentMedicinesForm(Client parent){
        this.parent = parent;

        //TODO low priority : change list to JTable
        ArrayList<String> dataListInRealisation = new ArrayList<>();
        ArrayList<String> dataListInReceived = new ArrayList<>();
        ArrayList<String> dataListToTake = new ArrayList<>();


        for(Medicine card : parent.getCreditCards()){
            if(card.getOrderStatus()== Medicine.OrderStatus.inRealisation)
                dataListInRealisation.add(card.toString());

            else if(card.getOrderStatus()== Medicine.OrderStatus.taken)
                dataListInReceived.add(card.toString());
            else if (card.getOrderStatus()== Medicine.OrderStatus.readyToTake) {
                dataListToTake.add(card.toString());
            }


        }
        String[] dataInRealisation = new String[dataListInRealisation.size()];
        dataListInRealisation.toArray(dataInRealisation);
        list3.setListData(dataInRealisation);

        String[] dataReceived = new String[dataListInReceived.size()];
        dataListInReceived.toArray(dataReceived);
        list2.setListData(dataReceived);

        String[] dataReadyToTake = new String[dataListToTake.size()];
        dataListToTake.toArray(dataReadyToTake);
        list1.setListData(dataReadyToTake);


        quitButton.addActionListener(this);
        deleteButton.addActionListener(this);
        confReceiveButton.addActionListener(this);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                parent.setVisible(true);
            }
        });

        setContentPane(mainPanel);
        setVisible(true);
        pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == quitButton){
            dispose();
            parent.setVisible(true);
        }
        else if(e.getSource() == orderCardButton){
            if(parent.getCreditCards().size() >= 3){
                JOptionPane.showMessageDialog(this, "Nie można posiadać więcej niż 3 karty kredytowe");
                return;
            }
            try {
                new OrderCreditCardForm(parent, this);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
        else if(e.getSource() == deleteButton){
            if(getList1().isSelectionEmpty() && getList2().isSelectionEmpty()) {
                parent.deleteCard(list3.getSelectedIndex());

                ArrayList<String> dataList = new ArrayList<>();
                for (Medicine card : parent.getCreditCards()) {
                    dataList.add(card.toString());
                }
                String[] data = new String[dataList.size()];
                dataList.toArray(data);

//                list3.setListData(data);
            }
            else if (getList2().isSelectionEmpty() && getList3().isSelectionEmpty() ) {

                parent.deleteCard(list1.getSelectedIndex());
                ArrayList<String> dataList = new ArrayList<>();
                for (Medicine card : parent.getCreditCards()) {
                    dataList.add(card.toString());
                }
                String[] data = new String[dataList.size()];
                dataList.toArray(data);

//                list1.setListData(data);
            }
            else if (getList1().isSelectionEmpty() && getList3().isSelectionEmpty()){
                parent.deleteCard(list2.getSelectedIndex());
                ArrayList<String> dataList = new ArrayList<>();
                for (Medicine card : parent.getCreditCards()) {
                    dataList.add(card.toString());
                }
                String[] data = new String[dataList.size()];
                dataList.toArray(data);

//                list2.setListData(data);
            }




            repaint();
        }
//        else if(e.getSource() == confReceiveButton){
//
//            list2.getSelect();
//            ArrayList<String> dataList = new ArrayList<>();
//            for(Medicine card : parent.getCreditCards()){
//                dataList.add(card.toString());
//            }
//            String[] data = new String[dataList.size()];
//            dataList.toArray(data);
//
//            list1.setListData(data);
//            repaint();
//        }
    }

    public JList<String> getList1() {
        return list1;
    }

    public JList getList3() {
        return list3;
    }

    public JList getList2() {
        return list2;
    }
}
