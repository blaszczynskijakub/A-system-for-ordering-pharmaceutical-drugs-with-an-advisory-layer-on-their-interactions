package org.example.src.Forms.Employee;

import org.example.src.Forms.DataHandler;
import org.example.src.Forms.logging.LogInForm;
import org.example.src.Forms.logging.PasswordUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class Employee extends JFrame implements ActionListener, DataHandler {

    private ResultSet resultSet;
    private PreparedStatement preparedStatement;
    private Connection connection;

    private JButton closeAccButton;
    private JPanel jPanel;
    private JLabel mainLabel;
    private JLabel nameLabel;
    private JLabel balanceLabel;
    private JLabel nameFillLabel;
    private JLabel positionFillLabel;
    private JLabel titleLabel;
    private JButton changeDataButton;
    private JLabel label1;
    private JButton controlOrdersButton;
    private JButton editDrugsButton;
    private JButton addAccButton;
    private JButton logOutButton;

    private String firstName;
    private String lastName;
    private String address;
    private String position;
    private final int employeeId;
    private double balance;

    public Employee(int employeeId, String firstName, String lastName, String position, String branchName, String branchAddress, Connection connection) {
        this.employeeId = employeeId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.position = position;
        this.address = branchAddress;
        initializeUI(branchName, branchAddress);
        this.connection=connection;
    }

    private void initializeUI(String branchName, String branchAddress) {
        setSize(500, 400);

        setLocationRelativeTo(null);
        this.setResizable(false);

        setResizable(false);
        setTitle("Aplikacja Pracownika");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(jPanel);

        setUpLabels(branchName, branchAddress);
        setUpButtons();

        setVisible(true);
    }

    private void setUpLabels(String branchName, String branchAddress) {
        nameFillLabel.setText(firstName + " " + lastName);
        positionFillLabel.setText(position);
    }

    private void setUpButtons() {
        closeAccButton.addActionListener(this);
        changeDataButton.addActionListener(this);
        controlOrdersButton.addActionListener(this);
        editDrugsButton.addActionListener(this);
        addAccButton.addActionListener(this);
        logOutButton.addActionListener(this);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == changeDataButton) {
            openManageAccountForm();
        } else if (e.getSource() == closeAccButton) {
            openDeleteUserForm();
        } else if (e.getSource() == controlOrdersButton) {
            openTransactionHistory();
        } else if (e.getSource() == editDrugsButton) {
            openEditDrugsForm();
        } else if (e.getSource() == logOutButton) {
            logout();
        } else if (e.getSource() == addAccButton) {
            addNewAccount();
        }
    }

    public void openManageAccountForm() {
        setVisible(false);
        new ManageAcc(this);
    }

    private void openDeleteUserForm() {
        setVisible(false);
        new DeleteUser(this);
    }

    public void openTransactionHistory() {
        setVisible(false);
        new TransactionsFrame(this, connection);
    }

    private void openEditDrugsForm() {
        setVisible(false);
        new DrugListForEmplo(this, connection);
    }

    public void logout() {
        setVisible(false);
        new LogInForm();
    }

    public void addNewAccount() {
        UIManager.put("OptionPane.inputDialogTitle", "");
        UIManager.put("OptionPane.cancelButtonText", "Anuluj");
        UIManager.put("OptionPane.cancelButtonText", "Anuluj");
        UIManager.put("OptionPane.messageDialogTitle", "Informacja");



        String firstName = JOptionPane.showInputDialog(this, "Wprowadź imię:");
        if (firstName == null) {
            showError("Nie udało się dodać klienta");

            return;
        }
        String lastName = JOptionPane.showInputDialog(this, "Wprowadź nazwisko:");
        if (lastName == null) {
            showError("Nie udało się dodać klienta");

            return;
        }
        String city = JOptionPane.showInputDialog(this, "Wprowadź miasto:");
        if (city == null) {
            showError("Nie udało się dodać klienta");

            return;
        }
        String address = JOptionPane.showInputDialog(this, "Wprowadź adres:");
        if (address == null) {
            showError("Nie udało się dodać klienta");

            return;
        }
        try {
            char[] plainPassword = JOptionPane.showInputDialog(this, "Wprowadź hasło:").toCharArray();
            insertNewClient(firstName, lastName, city, address, plainPassword);
            java.util.Arrays.fill(plainPassword, '\0');
            JOptionPane.showMessageDialog(this, "Pomyślnie dodano klienta");




        } catch (Exception e) {
            showError("Nie udało się dodać klienta");

            return;
        }
    }

    private void insertNewClient(String firstName, String lastName, String address, String city, char[] password) {
        String query = "INSERT INTO clients (first_name, last_name, address, city, password_hash) VALUES (?, ?, ?, ?, ?)";

        String passwordStr = new String(password);
        String hashedPassword = PasswordUtils.hashPassword(passwordStr);

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, firstName);
            preparedStatement.setString(2, lastName);
            preparedStatement.setString(3, address);
            preparedStatement.setString(4, city);
            preparedStatement.setString(5, hashedPassword);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            showError("Nie udało się dodać klienta: " + e.getMessage());
        } finally {
            passwordStr = null;
        }
    }



    public void updateCredentials(String firstName, String lastName, String address, String city, String id) {
        String query = "UPDATE clients SET first_name = ?, last_name = ?, address = ?, city = ? WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, firstName);
            preparedStatement.setString(2, lastName);
            preparedStatement.setString(3, address);
            preparedStatement.setString(4, city);
            preparedStatement.setInt(5, Integer.parseInt(id));
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            showError("Nie udało się zmienić danych");

        }
        updateClientInfo();
    }

    public void deleteAccount(int id) {
        executeDelete("DELETE FROM drugs_and_clients WHERE client_id = ?", id);
        executeDelete("DELETE FROM clients WHERE id = ?", id);
    }

    private void executeDelete(String query, int id) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    private void updateClientInfo() {
        String query = "SELECT `Imię`, `Nazwisko`, `Adres`, `Miasto` FROM clients_info_view WHERE ID = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, employeeId);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                firstName = resultSet.getString(1);
                lastName = resultSet.getString(2);
                address = resultSet.getString(3);
                position = resultSet.getString(4);

            }
            repaint();
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Błąd", JOptionPane.ERROR_MESSAGE);
    }

    public Connection getConnection() {
        return connection;
    }

    public PreparedStatement getPreparedStatement() {
        return preparedStatement;
    }

    public ResultSet getResultSet() {
        return resultSet;
    }

    public class TransactionsFrame extends JDialog {
        private JFrame parent;
        private Connection connection;
        private JButton quitButton;
        private JButton changeStatus;
        private JList<String> transactionsList;
        private DefaultListModel<String> listModel;

        public TransactionsFrame(JFrame parent, Connection connection) {
            this.parent = parent;
            this.connection = connection;
            initializeUI();
        }

        private void initializeUI() {
            this.setLocationRelativeTo(null);
            this.setResizable(false);
            JPanel jPanel = new JPanel(new BorderLayout());
            setUpHeader(jPanel);
            setUpButtons(jPanel);
            setUpTransactionsList(jPanel);
            this.setResizable(false);


            setTitle("Zamówienia klientów");
            setContentPane(jPanel);
            setSize(1500, 650);
            setLocationRelativeTo(parent);
            setVisible(true);
        }

        private void setUpHeader(JPanel jPanel) {
            jPanel.setBackground(new Color(255,123,51));
            JLabel label = new JLabel("Zamówienia", JLabel.CENTER);
            label.setFont(new Font("Cooper Black", Font.BOLD | Font.ITALIC, 22));
            label.setForeground(Color.WHITE);
            jPanel.add(label, BorderLayout.NORTH);
        }

        private void setUpButtons(JPanel jPanel) {
            quitButton = new JButton("Powrót");
            quitButton.addActionListener(e -> closeDialog());
            changeStatus = new JButton("Zmień status na 'w dostawie'");
            changeStatus.addActionListener(e -> updateTransactionStatus());
            jPanel.add(quitButton, BorderLayout.SOUTH);
            jPanel.add(changeStatus, BorderLayout.EAST);
        }

        private void setUpTransactionsList(JPanel jPanel) {
            listModel = new DefaultListModel<>();
            transactionsList = new JList<>(listModel);
            loadTransactions();
            transactionsList.setFont(new Font("Arial", Font.PLAIN, 16));
            transactionsList.setBackground(new Color(240, 240, 240));
            JScrollPane scrollPane = new JScrollPane(transactionsList);
            scrollPane.setPreferredSize(new Dimension(380, 200));
            jPanel.add(scrollPane, BorderLayout.CENTER);
        }

        private void closeDialog() {
            setVisible(false);
            parent.setVisible(true);
        }

        private void loadTransactions() {
            listModel.clear();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM transactions_all_clients WHERE  status=?");
                preparedStatement.setString(1, "inRealisation");


                ResultSet set = preparedStatement.executeQuery();
                while (set.next()) {
                    String transaction = String.format("Client ID: %d, Name: %s %s, Drug: %s, Price: %d, Order ID: %d, Date: %s",
                            set.getInt(1),
                            set.getString(2),
                            set.getString(3),
                            set.getString(4),
                            set.getInt(7),
                            set.getInt(8),
                            set.getTimestamp(9)
                    );
                    listModel.addElement(transaction);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Brak transakcji do pokazania");
            }
        }

        private void updateTransactionStatus() {
            String selectedValue = transactionsList.getSelectedValue();
            if (selectedValue == null) return;

            int orderId = extractOrderId(selectedValue);
            updateOrderStatusInDatabase(orderId);
            this.initializeUI();
        }

        private int extractOrderId(String transactionDetails) {
            int startIndex = transactionDetails.indexOf("Order ID:") + 10;
            int endIndex = transactionDetails.indexOf("Date:");
            return Integer.parseInt(transactionDetails.substring(startIndex, endIndex).trim().replace(",", ""));
        }

        private void updateOrderStatusInDatabase(int orderId) {
            String query = "UPDATE drugs_and_clients SET status='taken' WHERE id=?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, orderId);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                showError(e.getMessage());
            }
        }
    }
}
