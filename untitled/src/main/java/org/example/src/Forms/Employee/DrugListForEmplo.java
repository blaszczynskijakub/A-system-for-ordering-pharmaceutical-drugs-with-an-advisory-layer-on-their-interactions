package org.example.src.Forms.Employee;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DrugListForEmplo extends JDialog {
    private final JFrame parent;
    private final Connection connection;
    private JButton quitButton;
    private JButton editButton;
    private JButton addButton;
    private JButton deleteButton;
    private JList<String> transactionsList;
    private DefaultListModel<String> listModel;

    public DrugListForEmplo(JFrame parent, Connection connection) {
        this.parent = parent;
        this.connection = connection;
        initializeUI();
        loadTransactions();
        setVisible(true);
    }

    private void initializeUI() {
        setTitle("Historia transakcji");
        setContentPane(createMainPanel());
        setSize(600, 400);
        setLocationRelativeTo(parent);
        addWindowCloseListener();
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(24, 26, 48));

        JLabel titleLabel = createTitleLabel();
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JScrollPane transactionScrollPane = createTransactionScrollPane();
        mainPanel.add(transactionScrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    private JLabel createTitleLabel() {
        JLabel label = new JLabel("Pracownik", JLabel.CENTER);
        label.setFont(new Font("Cooper Black", Font.BOLD | Font.ITALIC, 22));
        label.setForeground(Color.WHITE);
        return label;
    }

    private JScrollPane createTransactionScrollPane() {
        listModel = new DefaultListModel<>();
        transactionsList = new JList<>(listModel);
        transactionsList.setFont(new Font("Arial", Font.PLAIN, 16));
        transactionsList.setBackground(new Color(240, 240, 240));
        return new JScrollPane(transactionsList);
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        quitButton = createButton("Powrót", e -> closeForm());
        editButton = createButton("Edytuj", e -> editRow());
        addButton = createButton("Dodaj", e -> addRow());
        deleteButton = createButton("Usuń", e -> deleteRow());

        buttonPanel.add(quitButton);
        buttonPanel.add(editButton);
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        return buttonPanel;
    }

    private JButton createButton(String text, ActionListener actionListener) {
        JButton button = new JButton(text);
        button.addActionListener(actionListener);
        return button;
    }

    private void addWindowCloseListener() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                parent.setVisible(true);
            }
        });
    }

    private void closeForm() {
        setVisible(false);
        parent.setVisible(true);
    }

    private void loadTransactions() {
        listModel.clear();
        String query = "SELECT id, drug_name, producent_name, drug_type, price FROM drugs";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String transaction = formatTransaction(resultSet);
                listModel.addElement(transaction);
            }
        } catch (SQLException e) {
            showError("Brak transakcji do pokazania");
        }
    }

    private String formatTransaction(ResultSet resultSet) throws SQLException {
        return String.format("Order ID: %d, Drug name: %s, Producent name: %s, Drug type: %s, Price: %d",
                resultSet.getInt("id"),
                resultSet.getString("drug_name"),
                resultSet.getString("producent_name"),
                resultSet.getString("drug_type"),
                resultSet.getInt("price"));
    }

    private void editRow() {
        String selectedTransaction = transactionsList.getSelectedValue();
        if (selectedTransaction == null) return;

        int orderId = extractOrderId(selectedTransaction);
        String newDrugName = promptForInput("Wprowadź nową nazwę leku:");
        String newProducentName = promptForInput("Wprowadź nową nazwę producenta:");
        String newDrugType = promptForInput("Wprowadź nowy typ leku:");
        String newPrice = promptForInput("Wprowadź nową cenę:");

        try {
            String query = "UPDATE drugs SET drug_name=?, producent_name=?, drug_type=?, price=? WHERE id=?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, newDrugName);
                preparedStatement.setString(2, newProducentName);
                preparedStatement.setString(3, newDrugType);
                preparedStatement.setInt(4, Integer.parseInt(newPrice));
                preparedStatement.setInt(5, orderId);
                preparedStatement.executeUpdate();
            }
            loadTransactions();
        } catch (SQLException e) {
            showError("Nie udało się edytować danych");
        }
    }

    private void addRow() {
        String drugName = promptForInput("Wprowadź nazwę leku:");
        String producentName = promptForInput("Wprowadź nazwę producenta:");
        String drugType = promptForInput("Wprowadź typ leku:");
        String price = promptForInput("Wprowadź cenę:");

        try {
            String query = "INSERT INTO drugs (drug_name, producent_name, drug_type, price) VALUES (?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, drugName);
                preparedStatement.setString(2, producentName);
                preparedStatement.setString(3, drugType);
                preparedStatement.setInt(4, Integer.parseInt(price));
                preparedStatement.executeUpdate();
            }
            loadTransactions();
        } catch (SQLException e) {
            showError("Nie udało się dodać leku");
        }
    }

    private void deleteRow() {
        String selectedTransaction = transactionsList.getSelectedValue();
        if (selectedTransaction == null) return;

        int orderId = extractOrderId(selectedTransaction);
        try {
            String query = "DELETE FROM drugs WHERE id=?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, orderId);
                preparedStatement.executeUpdate();
            }
            loadTransactions();
        } catch (SQLException e) {
            showError("Nie udało się usunąć leku, ponieważ są klienci, którzy go używają.");
        }
    }

    private int extractOrderId(String transaction) {
        int startIndex = transaction.indexOf("Order ID:") + 9;
        int endIndex = transaction.indexOf(",", startIndex);
        return Integer.parseInt(transaction.substring(startIndex, endIndex).trim());
    }

    private String promptForInput(String message) {
        return JOptionPane.showInputDialog(this, message);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
