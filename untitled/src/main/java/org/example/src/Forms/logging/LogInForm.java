package org.example.src.Forms.logging;

import org.example.src.Forms.Client.Client;
import org.example.src.Forms.Employee.Employee;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Arrays;

public class LogInForm extends JFrame implements ActionListener {
    private JPanel mainPanel;
    private JLabel titleLabel;
    private JLabel loginLabel;
    private JTextField loginField;
    private JPasswordField passwordField;
    private JLabel passwordLabel;
    private JButton loginButton;

    private String userType;
    private Connection connection = null;

    public LogInForm() {
        userType = "";
        initializeDatabaseConnection();
        setupUI();
    }


    private void setupUI() {
        setTitle("Logowanie");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginButton.addActionListener(this);
        setContentPane(mainPanel);
        setVisible(true);
        pack();
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
            if (validateEmployeeLogin(enteredUsername, enteredPassword)) {
                userType = "EMPLOYEE";
            } else if (validateClientLogin(enteredUsername, Arrays.toString(enteredPassword))) {
                userType = "CLIENT";
            } else {
                JOptionPane.showMessageDialog(this, "Niepoprawne dane logowania");
            }
        } finally {
            java.util.Arrays.fill(enteredPassword, ' ');
        }

        if (!userType.isEmpty()) {
            setVisible(false);
        }
    }


    private boolean validateEmployeeLogin(String username, char[] password) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id, first_name, last_name, department_id, position, password_hash FROM employees WHERE CONCAT(first_name, ' ', last_name) = ?")) {
            statement.setString(1, username);
            ResultSet set = statement.executeQuery();

            if (set.next()) {
                String storedHash = set.getString("password_hash");

                boolean passwordMatch = PasswordUtils.checkPassword(new String(password), storedHash);
                java.util.Arrays.fill(password, ' ');

                if (passwordMatch) {
                    new Employee(set.getInt("id"), set.getString("first_name"), set.getString("last_name"),
                            set.getString("position"), "pass", "pass", connection);
                    return true;
                } else {
                    JOptionPane.showMessageDialog(this, "Nieprawidłowe dane logowania");
                }
            }
        } catch (SQLException exception) {
            System.out.println("Błąd bazy danych: " + exception);
        }
        return false;
    }


    private boolean validateClientLogin(String username, String password) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT password_hash, id, first_name, last_name, address, city FROM clients WHERE CONCAT(first_name, ' ', last_name) = ?")) {
            statement.setString(1, username);
            ResultSet set = statement.executeQuery();

            if (set.next()) {
                String storedHash = set.getString("password_hash");

                if (PasswordUtils.checkPassword(password, storedHash)) {
                    new Client(set.getInt("id"), set.getString("first_name"), set.getString("last_name"), set.getString("address"), set.getString("city"),connection);
                    return true;
                } else {
                    JOptionPane.showMessageDialog(this, "Nieprawidłowe dane logowania");
                }
            }
        } catch (SQLException exception) {
            System.out.println("Błąd bazy danych: " + exception);
        }
        return false;
    }

    private void initializeDatabaseConnection() {
        try {
            String dbUrl = System.getenv("DB_URL");
            String dbUser = System.getenv("DB_USER");
            String dbPassword = System.getenv("DB_PASSWORD");
            connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        } catch (Exception e) {
            System.out.println("Błąd przy nawiązywaniu połączenia: " + e);
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
