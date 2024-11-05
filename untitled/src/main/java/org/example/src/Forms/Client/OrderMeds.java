package org.example.src.Forms.Client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;

public class OrderMeds extends JFrame implements ActionListener {

    private JPanel mainPanel;
    private JLabel mainLabel;
    private JLabel firstNameLabel;
    private JTextField firstNameField;
    private JButton acceptButton;
    private JLabel titleLabel;
    private JButton quitButton;
    private JComboBox<Medicine> comboBox1;
    private JButton searchButton;
    private JButton orderMedButton;

    private final Client parent;

    public OrderMeds(Client parent) {
        this.parent = parent;
        initializeUI();
        setUpButtonListeners();
        setVisible(true);
    }

    private void initializeUI() {
        setTitle("Zamów leki");
        setContentPane(mainPanel);
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowCloseListener();
        pack();
    }

    private void setUpButtonListeners() {
        quitButton.addActionListener(this);
        searchButton.addActionListener(this);
        orderMedButton.addActionListener(this);
    }

    private void addWindowCloseListener() {
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                parent.setVisible(true);
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == acceptButton) {
            closeForm();
        } else if (e.getSource() == quitButton) {
            closeForm();
        } else if (e.getSource() == searchButton) {
            handleSearchAction();
        } else if (e.getSource() == orderMedButton) {
            handleOrderMedAction();
        }
    }

    private void closeForm() {
        dispose();
        parent.setVisible(true);
    }

    private void handleSearchAction() {
        String name = firstNameField.getText().trim();
        if (isValidName(name)) {
            loadMedicinesIntoComboBox(name);
        } else {
            showInvalidInputDialog();
        }
    }

    private boolean isValidName(String name) {
        return !name.isEmpty() && name.matches("[a-zA-Z0-9\\s]+");
    }

    private void loadMedicinesIntoComboBox(String name) {
        ArrayList<Medicine> items = parent.populateComboBox(name, parent.getPreparedStatement(), parent.getConnection(), parent.getResultSet(), parent.getClientId());
        comboBox1.setModel(new DefaultComboBoxModel<>(items.toArray(new Medicine[0])));
    }

    private void showInvalidInputDialog() {
        JOptionPane.showMessageDialog(this, "Proszę uzupełnić pole bez znaków specjalnych", "Invalid Input", JOptionPane.ERROR_MESSAGE);
    }

    private void handleOrderMedAction() {
        try {
            ArrayList<Medicine> availableCards = parent.getAvailableCards();
            //max cards to have
            if (availableCards.size() < 30) {
                Medicine selectedMedicine = (Medicine) comboBox1.getSelectedItem();
                if (selectedMedicine != null) {
                    parent.addCardToAccount(selectedMedicine);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Za dużo zamówień, do konta może być przypisanych łączenie maksymalnie 29 leków, usuń jakieś produkty", "Order Limit Exceeded", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Błąd przy wczytywaniu aktualnych leków: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }


}
