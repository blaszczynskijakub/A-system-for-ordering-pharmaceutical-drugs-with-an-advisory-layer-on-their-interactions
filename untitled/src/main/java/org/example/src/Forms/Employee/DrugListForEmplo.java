package org.example.src.Forms.Employee;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DrugListForEmplo extends JDialog {
    private JFrame parent;
    private Connection connection;
    private JButton quitButton;
    private JButton editButton;
    private JButton addButton;
    private JButton deleteButton;
    private JList<String> transactionsList;
    private DefaultListModel<String> listModel;

    public DrugListForEmplo(JFrame parent, Connection connection) {
        this.parent = parent;
        this.connection = connection;

        JPanel jPanel = new JPanel(new BorderLayout());
        jPanel.setBackground(new Color(24, 26, 48));

        Font font = new Font("Cooper Black", Font.BOLD | Font.ITALIC, 22);
        JLabel label = new JLabel("Bank Bilardzistów", JLabel.CENTER);
        label.setFont(font);
        label.setForeground(Color.WHITE);
        jPanel.add(label, BorderLayout.NORTH);

        setTitle("Historia transakcji");
        setContentPane(jPanel);

        quitButton = new JButton("Powrót");
        quitButton.addActionListener(e -> {
            setVisible(false);
            parent.setVisible(true);
        });

        // Button to change the status of the selected drug


        // Button to edit data of the selected drug
        editButton = new JButton("Edytuj");
        editButton.addActionListener(e -> editRow());

        // Button to add a new drug row
        addButton = new JButton("Dodaj");
        addButton.addActionListener(e -> addRow());

        // Button to delete the selected drug
        deleteButton = new JButton("Usuń");
        deleteButton.addActionListener(e -> deleteRow());

        listModel = new DefaultListModel<>();
        transactionsList = new JList<>(listModel);

        // Load transactions into the list
        loadTransactions();

        transactionsList.setFont(new Font("Arial", Font.PLAIN, 16));
        transactionsList.setBackground(new Color(240, 240, 240));
        JScrollPane scrollPane = new JScrollPane(transactionsList);
        scrollPane.setPreferredSize(new Dimension(380, 200));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(quitButton);
        buttonPanel.add(editButton);
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);

        jPanel.add(scrollPane, BorderLayout.CENTER);
        jPanel.add(buttonPanel, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                parent.setVisible(true);
            }
        });

        setSize(600, 400);
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private void loadTransactions() {
        listModel.clear();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT id, drug_name, producent_name, drug_type, price FROM drugs");

            ResultSet set = preparedStatement.executeQuery();
            while (set.next()) {
                String transaction = String.format("Order ID: %d, Drug name: %s, Producent name: %s, Drug type: %s, Price: %d",
                        set.getInt(1),
                        set.getString(2),
                        set.getString(3),
                        set.getString(4),
                        set.getInt(5)
                );
                listModel.addElement(transaction);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Brak transakcji do pokazania");
        }
    }



    private void editRow() {
        if (transactionsList.getSelectedValue() != null) {
            int orderId = extractOrderId(transactionsList.getSelectedValue());
            String newDrugName = JOptionPane.showInputDialog(this, "Wprowadź nową nazwę leku:");
            String newProducentName = JOptionPane.showInputDialog(this, "Wprowadź nową nazwę producenta:");
            String newDrugType = JOptionPane.showInputDialog(this, "Wprowadź nowy typ leku:");
            String newPrice = JOptionPane.showInputDialog(this, "Wprowadź nową cenę:");

            try {
                String query = "UPDATE drugs SET drug_name=?, producent_name=?, drug_type=?, price=? WHERE id=?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, newDrugName);
                preparedStatement.setString(2, newProducentName);
                preparedStatement.setString(3, newDrugType);
                preparedStatement.setInt(4, Integer.parseInt(newPrice));
                preparedStatement.setInt(5, orderId);
                preparedStatement.executeUpdate();
                loadTransactions();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Nie udało się edytować danych");
            }
        }
    }

    private void addRow() {
        String drugName = JOptionPane.showInputDialog(this, "Wprowadź nazwę leku:");
        String producentName = JOptionPane.showInputDialog(this, "Wprowadź nazwę producenta:");
        String drugType = JOptionPane.showInputDialog(this, "Wprowadź typ leku:");
        String price = JOptionPane.showInputDialog(this, "Wprowadź cenę:");

        try {
            String query = "INSERT INTO drugs (drug_name, producent_name, drug_type, price) VALUES (?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, drugName);
            preparedStatement.setString(2, producentName);
            preparedStatement.setString(3, drugType);
            preparedStatement.setInt(4, Integer.parseInt(price));
            preparedStatement.executeUpdate();
            loadTransactions();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Nie udało się dodać leku");
        }
    }

    private void deleteRow() {
        if (transactionsList.getSelectedValue() != null) {
            int orderId = extractOrderId(transactionsList.getSelectedValue());

            try {
                String query = "DELETE FROM drugs WHERE id=?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, orderId);
                preparedStatement.executeUpdate();
                loadTransactions();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Nie udało się usunąć leku, ponieważ są klienci, którzy go używają.");
            }
        }
    }

    private int extractOrderId(String transaction) {
        int startIndex = transaction.indexOf("Order ID:") + 9;
        int endIndex = transaction.indexOf(",", startIndex);
        return Integer.parseInt(transaction.substring(startIndex, endIndex).trim());
    }
}
