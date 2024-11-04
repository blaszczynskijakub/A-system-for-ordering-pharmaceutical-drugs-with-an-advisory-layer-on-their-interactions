package org.example.src.Forms;

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

    private void initializeDatabaseConnection() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/database_good", "root", "#hom^ik34");
        } catch (Exception e) {
            System.out.println(e);
        }
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
        String enteredPassword = String.valueOf(passwordField.getPassword());

        if (validateEmployeeLogin(enteredUsername, enteredPassword)) {
            userType = "EMPLOYEE";
            // Employee logic here...
        } else if (validateClientLogin(enteredUsername, enteredPassword)) {
            userType = "CLIENT";
            // Client logic here...
        } else {
            JOptionPane.showMessageDialog(this, "Niepoprawne dane logowania");
        }

        if (!userType.isEmpty()) {
            setVisible(false);
        }
    }

    private boolean validateEmployeeLogin(String username, String password) {
        if (password.equalsIgnoreCase("PRACOWNIK")) {
            try (Statement statement = connection.createStatement()) {
                ResultSet set = statement.executeQuery("SELECT * FROM employees_info_view");
                while (set.next()) {
                    if ((set.getString(2) + " " + set.getString(3)).equalsIgnoreCase(username)) {
                        new Employee(set.getInt(1), set.getString(2), set.getString(3),
                                set.getString(4), set.getString(5), set.getString(6));
                        return true;
                    }
                }
            } catch (SQLException exception) {
                System.out.println(exception);
            }
        }
        return false;
    }

    private boolean validateClientLogin(String username, String password) {
        if (password.equalsIgnoreCase("KLIENT")) {
            try (Statement statement = connection.createStatement()) {
                ResultSet set = statement.executeQuery("SELECT * FROM clients_info_view");
                while (set.next()) {
                    if ((set.getString(2) + " " + set.getString(3)).equalsIgnoreCase(username)) {
                        new Client(set.getInt(1), set.getString(2), set.getString(3),
                                set.getString(4), set.getString(5));
                        return true;
                    }
                }
            } catch (SQLException exception) {
                System.out.println(123);
                System.out.println(exception);
            }
        }
        return false;
    }

    // Getter and Setter for testing purposes
    public String getUserType() {
        return userType;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    // Getters for UI elements for testing
    public JTextField getLoginField() {
        return loginField;
    }

    public JPasswordField getPasswordField() {
        return passwordField;
    }

    public JButton getLoginButton() {
        return loginButton;
    }
}
