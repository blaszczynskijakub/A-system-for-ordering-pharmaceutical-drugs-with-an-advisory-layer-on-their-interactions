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
        setTitle("Zarządanie lekami");
        setSize(1500, 650);
        setLocationRelativeTo(parent);
        setResizable(false);
        setContentPane(createMainPanel());
        addWindowCloseListener();
        this.setResizable(false);

    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(255, 123, 51));

        JLabel titleLabel = createTitleLabel();
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JScrollPane transactionScrollPane = createTransactionScrollPane();
        mainPanel.add(transactionScrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    private JLabel createTitleLabel() {
        JLabel label = new JLabel("Lista leków", JLabel.CENTER);
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
        quitButton = createButton("Wróć", e -> closeForm());
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
        String query = "SELECT id, drug_name, producent_name, drug_type, price, acidity, kolestypol, digestion, high_affinity, opiodis, carbon, alcohol, need_cover FROM drugs";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String transaction = formatTransaction(resultSet);
                listModel.addElement(transaction);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Brak zamówień do wyświetlenia.");
        }
    }

    private String formatTransaction(ResultSet resultSet) throws SQLException {
        return String.format("Order ID: %d, Drug Name: %s, Manufacturer: %s, Type: %s, Price: %d, Acidity: %s, Kolestypol: %s, Digestion: %s, High Affinity: %s, Opiodis: %s, Carbon: %s, Alcohol: %s, Cover Needed: %s",
                resultSet.getInt("id"),
                resultSet.getString("drug_name"),
                resultSet.getString("producent_name"),
                resultSet.getString("drug_type"),
                resultSet.getInt("price"),
                booleanToText(resultSet.getObject("acidity")),
                booleanToText(resultSet.getObject("kolestypol")),
                booleanToText(resultSet.getObject("digestion")),
                booleanToText(resultSet.getObject("high_affinity")),
                booleanToText(resultSet.getObject("opiodis")),
                booleanToText(resultSet.getObject("carbon")),
                booleanToText(resultSet.getObject("alcohol")),
                booleanToText(resultSet.getObject("need_cover")));
    }

    private String booleanToText(Object value) {
        return (value == null) ? "NULL" : (Boolean.TRUE.equals(value) ? "1" : "0");
    }

    private void editRow() {
        String selectedTransaction = transactionsList.getSelectedValue();
        if (selectedTransaction == null) return;

        int orderId = extractOrderId(selectedTransaction);

        String[] fields = {"Drug Name", "Manufacturer", "Type", "Price", "Acidity", "Kolestypol", "Digestion", "High Affinity", "Opiodis", "Carbon", "Alcohol", "Cover Needed"};
        String[] newValues = new String[fields.length];

        for (int i = 0; i < fields.length; i++) {
            newValues[i] = promptForInput("Wprowadź nowy " + fields[i] + ":");
            if (newValues[i] == null) return;
        }
        try {
            String query = "UPDATE drugs SET drug_name=?, producent_name=?, drug_type=?, price=?, acidity=?, kolestypol=?, digestion=?, high_affinity=?, opiodis=?, carbon=?, alcohol=?, need_cover=? WHERE id=?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                for (int i = 0; i < newValues.length ; i++) {
                    preparedStatement.setObject(i + 1, parseInput(newValues[i]));
                }
                preparedStatement.setInt(13, orderId);
                preparedStatement.executeUpdate();
            }
            loadTransactions();
        } catch (SQLException e) {
            showError("Nie udało się zmienić."+e);
        }
    }

    private Object parseInput(String input) {
        if ("NULL".equalsIgnoreCase(input)) return null;
        return "1".equals(input) || "0".equals(input) ? Integer.parseInt(input) : input;
    }

    private void addRow() {
        String[] prompts = {"Drug Name", "Manufacturer", "Type", "Price", "Acidity", "Kolestypol", "Digestion", "High Affinity", "Opiodis", "Carbon", "Alcohol", "Cover Needed"};
        String[] values = new String[prompts.length];

        for (int i = 0; i < prompts.length; i++) {
            values[i] = promptForInput("Wprowadź " + prompts[i] + ":");
            if (values[i] == null) return; // cancelled input
        }

        try {
            String query = "INSERT INTO drugs (drug_name, producent_name, drug_type, price, acidity, kolestypol, digestion, high_affinity, opiodis, carbon, alcohol, need_cover) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                for (int i = 0; i < values.length; i++) {
                    preparedStatement.setObject(i + 1, parseInput(values[i]));
                }
                preparedStatement.executeUpdate();
            }
            loadTransactions();
        } catch (SQLException e) {
            showError("Nie udało się dodac leku");
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
            showError("Nie udało się usunać leku, ponieważ niektórzy klienci z niego korzystają.");
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
        JOptionPane.showMessageDialog(this, message, "Błąd", JOptionPane.ERROR_MESSAGE);
    }
}
