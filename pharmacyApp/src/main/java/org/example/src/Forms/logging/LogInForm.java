package org.example.src.Forms.logging;

import org.example.src.Forms.Client.Client;
import org.example.src.Forms.Employee.Employee;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class LogInForm extends JFrame implements ActionListener {
    private JPanel mainPanel;
    private JLabel titleLabel;
    private JLabel loginLabel;
    public JTextField loginField;
    public JPasswordField passwordField;
    private JLabel passwordLabel;
    private JButton loginButton;

    private String userType;
    private Connection connection;

    public LogInForm() {
        userType = "";
        initializeDatabaseConnection();
        setupUI();
    }

    private void setupUI() {
        setTitle("Logowanie");
        setSize(1000, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginButton.addActionListener(this);
        setContentPane(mainPanel);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) {
            handleLogin();
        }
    }

    public void handleLogin() {
        String enteredUsername = loginField.getText();
        char[] enteredPassword = passwordField.getPassword();

        try {
            String passwordString = new String(enteredPassword);

            if (validateEmployeeLogin(enteredUsername, enteredPassword)) {
                userType = "EMPLOYEE";
            } else if (validateClientLogin(enteredUsername, passwordString)) {
                userType = "CLIENT";
            } else {
                UIManager.put("OptionPane.messageDialogTitle", "Informacja");
                JOptionPane.showMessageDialog(this, "Niepoprawne dane logowania");
            }
        } finally {
            // clear entered password
            java.util.Arrays.fill(enteredPassword, ' ');
        }

        if (!userType.isEmpty()) {
            setVisible(false);
        }
    }

    public boolean validateEmployeeLogin(String username, char[] password) {
        String query = "SELECT id, first_name, last_name,  position, password_hash FROM employees WHERE CONCAT(first_name, ' ', last_name) = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String storedHash = resultSet.getString("password_hash");

                boolean passwordMatch = PasswordUtils.checkPassword(new String(password), storedHash);
                java.util.Arrays.fill(password, ' ');

                if (passwordMatch) {
                    new Employee(resultSet.getInt("id"), resultSet.getString("first_name"), resultSet.getString("last_name"),
                            resultSet.getString("position"), "Branch Name", "Branch Address", connection);
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error during employee login: " + e);
        }
        return false;
    }

    public boolean validateClientLogin(String username, String password) {
        String query = "SELECT password_hash, id, first_name, last_name, address, city FROM clients WHERE CONCAT(first_name, ' ', last_name) = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String storedHash = resultSet.getString("password_hash");

                if (storedHash == null) {
                    System.err.println("Upewnij się ze klient ma hasło w bazie.");
                    return false;
                }

                boolean passwordMatch = PasswordUtils.checkPassword(password, storedHash);

                if (passwordMatch) {
                    new Client(resultSet.getInt("id"), resultSet.getString("first_name"), resultSet.getString("last_name"),
                            resultSet.getString("address"), resultSet.getString("city"), connection);
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Błąd w bazie w czasie logowania: " + e);
        }
        return false;
    }

    private void initializeDatabaseConnection() {
        try {
            String dbUrl = System.getenv("DB_URL");
            String dbUser = System.getenv("DB_USER");
            String dbPassword = System.getenv("DB_PASSWORD");
            connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        } catch (SQLException e) {
            System.err.println("Błąd połączenia z baząr: " + e);
            JOptionPane.showMessageDialog(this, "Błąd przy nawiązywaniu połączenia z bazą danych.", "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public String getUserType() {
        return userType;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public JTextField getLoginField() {
        return loginField;
    }

    public JPasswordField getPasswordField() {
        return passwordField;
    }
}
