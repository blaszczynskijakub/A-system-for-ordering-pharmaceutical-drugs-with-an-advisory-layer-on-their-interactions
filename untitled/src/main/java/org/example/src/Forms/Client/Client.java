package org.example.src.Forms.Client;

import org.example.src.Forms.DataHandler;
import org.example.src.Forms.logging.LogInForm;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class Client extends JFrame implements ActionListener, DataHandler {
    private JButton manageCardsButton;
    private JButton changeAccountDetailsButton;
    private JButton transferMoneyButton;
    private JPanel jPanel;
    private JLabel mainLabel;
    private JLabel nameLabel;
    private JLabel accountNumberLabel;
    private JLabel nameFillLabel;
    private JLabel accountFillLabel;
    private JLabel titleLabel;
    private JList eatSchedule;
    private JButton logOutButton;

    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private final int clientId;
    private double balance;
    private ArrayList<Medicine> medicines;

    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;


    public Client(int clientId, String firstName, String lastName, String address, String city, Connection connection) {
        this.clientId = clientId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.city = city;
        this.connection=connection;
        this.medicines = new ArrayList<>();
        initUI();
        loadMedicinesData();
        startClock();
    }

    private void initUI() {
        setTitle("Aplikacja Klienta");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(jPanel);
        addActionListeners();
        nameFillLabel.setText(firstName + " " + lastName);
        setVisible(true);
        pack();
    }

    private void addActionListeners() {
        manageCardsButton.addActionListener(this);
        changeAccountDetailsButton.addActionListener(this);
        transferMoneyButton.addActionListener(this);
        logOutButton.addActionListener(this);
    }


    private void loadMedicinesData() {
        String query = "SELECT `drug_id`,`id`,`transaciont_id`, `drug_name`,`producent`,`price` , `status`, `drug_type`  FROM client_and_drug_all_info_fixed WHERE `id` = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, clientId);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Medicine medicine = new Medicine(
                        resultSet.getInt("drug_id"),
                        resultSet.getInt("id"),
                        resultSet.getInt("transaciont_id"),
                        resultSet.getString("drug_name"),
                        resultSet.getString("producent"),
                        resultSet.getInt("price"),
                        Medicine.OrderStatus.valueOf(resultSet.getString("status"))
                        , resultSet.getString("drug_type")

                );
                medicines.add(medicine);
            }
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    private void startClock() {
        Timer clockTimer = new Timer(1000, new ActionListener() {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

            @Override
            public void actionPerformed(ActionEvent e) {
                accountFillLabel.setText(timeFormat.format(new java.util.Date()));
            }
        });
        clockTimer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == changeAccountDetailsButton) {
            openChangeAccountDetails();
        } else if (e.getSource() == transferMoneyButton) {
            openTransferMoneyForm();
        } else if (e.getSource() == manageCardsButton) {
            openManageCardsForm();
        } else if (e.getSource() == logOutButton) {
            logout();
        }
    }

    private void openChangeAccountDetails() {
        setVisible(false);
        new ClientCredentialsForm(this);
    }

    private void openTransferMoneyForm() {
        setVisible(false);
        new OrderMeds(this);
    }

    private void openManageCardsForm() {
        setVisible(false);
        new CurrentMedicinesForm(this, connection);
    }

    private void logout() {
        setVisible(false);
        new LogInForm();
    }

    protected void updateCredentials(String firstName, String lastName, String address, String city) {
        String query = "UPDATE clients SET first_name = ?, last_name = ?, address = ?, city = ? WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, firstName);
            preparedStatement.setString(2, lastName);
            preparedStatement.setString(3, address);
            preparedStatement.setString(4, city);
            preparedStatement.setInt(5, this.clientId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            showError(e.getMessage());
        }
        updateClientInfo();
    }

    public void updateClientInfo() {
        String query = "SELECT `Imię`, `Nazwisko`, `Adres`, `Miasto` FROM clients_info_view WHERE ID = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, clientId);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                firstName = resultSet.getString(1);
                lastName = resultSet.getString(2);
                address = resultSet.getString(3);
                city = resultSet.getString(4);
            }
        } catch (SQLException e) {
            showError(e.getMessage());
        }
        nameFillLabel.setText(firstName + " " + lastName);
        repaint();
    }



    protected void deleteAccount() {
        executeUpdate("DELETE FROM drugs_and_clients WHERE client_id = ?", clientId);
        executeUpdate("DELETE FROM clients WHERE id = ?", clientId);
    }

    private void executeUpdate(String query, int parameter) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, parameter);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    public ArrayList<Medicine> getAvailableCards() throws SQLException {
        return Medicine.generateCards(connection, clientId);
    }

    public void addCardToAccount(Medicine card) {
        String query = "INSERT INTO drugs_and_clients(drug_id, client_id) VALUES (?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, card.getDrugId());
            preparedStatement.setInt(2, clientId);
            preparedStatement.executeUpdate();
            medicines.add(card);
            showMessage("Pomyślnie dodano lek.");
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    public void deleteCard(Medicine card) {
        if (showYesNoPopup("Czy jesteś pewien, że chcesz usunąć ten lek. Ta operacja jest nieodwracalna. Nie wpłynie to na realizację zamówienia.")) {
            String query = "DELETE FROM drugs_and_clients WHERE drug_id = ? AND client_id= ? AND id= ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, card.getDrugId());
                preparedStatement.setInt(2, card.getClientId());
                preparedStatement.setInt(3, card.getTransactionId());
                preparedStatement.executeUpdate();
                medicines.remove(card);
                showMessage("Pomyślnie usunięto lek.");
            } catch (SQLException e) {
                showError(e.getMessage());
            }
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Błąd", JOptionPane.ERROR_MESSAGE);
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    public static boolean showYesNoPopup(String message) {
        int option = JOptionPane.showConfirmDialog(null, message, "Potwierdzenie", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return option == JOptionPane.YES_OPTION;
    }

    public PreparedStatement getPreparedStatement() {
        return preparedStatement;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public JButton getManageCardsButton() {
        return manageCardsButton;
    }

    public JButton getChangeAccountDetailsButton() {
        return changeAccountDetailsButton;
    }

    public JButton getTransferMoneyButton() {
        return transferMoneyButton;
    }

    public JPanel getjPanel() {
        return jPanel;
    }

    public JLabel getMainLabel() {
        return mainLabel;
    }

    public JLabel getNameLabel() {
        return nameLabel;
    }

    public JLabel getAccountNumberLabel() {
        return accountNumberLabel;
    }

    public JLabel getNameFillLabel() {
        return nameFillLabel;
    }

    public JLabel getAccountFillLabel() {
        return accountFillLabel;
    }

    public JLabel getTitleLabel() {
        return titleLabel;
    }

    public JList getEatSchedule() {
        return eatSchedule;
    }

    public JButton getLogOutButton() {
        return logOutButton;
    }

    public Connection getConnection() {
        return connection;
    }

    public ResultSet getResultSet() {
        return resultSet;
    }

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public double getBalance() { return balance; }
    public ArrayList<Medicine> getCreditCards() { return medicines; }
    public int getClientId() { return clientId; }
    public ArrayList<Medicine> getMedicines() { return medicines; }
}
