package org.example.src.Forms.Client;

import org.example.src.Forms.DataHandler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


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


    private String firstName;
    private String lastName;
    private String address;
    private String city;
    public final int clientId;


    private double balance;
    public ArrayList<Medicine> medicines;

    Connection connection = null;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;


    public Client(int clientId, String firstName, String lastName, String address, String city ) {
        medicines = new ArrayList<>();
        try {
            connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/database_good", "root", "ColGate1978");
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT `drug_id`,`id`,`transaciont_id`, `drug_name`,`producent`,`price` , `status`  FROM client_and_drug_all_info_fixed WHERE `id` = ?");
            preparedStatement.setInt(1, clientId);
            ResultSet resultSet = preparedStatement.executeQuery();




            while (resultSet.next()) {
                medicines.add(new Medicine(Integer.parseInt(resultSet.getString(1)), Integer.parseInt(resultSet.getString(2)), Integer.parseInt(resultSet.getString(3)),resultSet.getString(4),resultSet.getString(5), resultSet.getInt(6), Medicine.OrderStatus.valueOf(resultSet.getString(7))));
            }


        } catch (Exception e) {
            System.out.println(e);
        }

        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.city = city;
        this.clientId = clientId;
        this.balance = balance;
        setTitle("Aplikacja Klienta");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setContentPane(jPanel);

        manageCardsButton.addActionListener(this);
        changeAccountDetailsButton.addActionListener(this);
        transferMoneyButton.addActionListener(this);

        nameFillLabel.setText(firstName + " " + lastName);


        Timer clockTimer = new Timer(1000, new ActionListener() {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

            @Override
            public void actionPerformed(ActionEvent e) {
                java.util.Date javaDate = new java.util.Date();
                accountFillLabel.setText(timeFormat.format( new Date(javaDate.getTime())));
            }
        });
        clockTimer.start();

        setVisible(true);
        pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == changeAccountDetailsButton) {
            setVisible(false);
            new ClientCredentialsForm(this);
        } else if (e.getSource() == transferMoneyButton) {
            setVisible(false);
            new OrderMeds(this);
        } else if (e.getSource() == manageCardsButton) {
            setVisible(false);
            new CurrentMedicinesForm(this, connection);

        }

    }

    protected void updateCredentials(String firstName, String lastName, String address, String city) {
        // TODO check if given strings are logical (e.g return if firstName contains a digit)

        PreparedStatement preparedStatement;
        try {
            preparedStatement = connection.prepareStatement("UPDATE clients SET first_name = ?, last_name = ?, address = ?, city = ? WHERE id = ?");

            preparedStatement.setString(1, firstName);
            preparedStatement.setString(2, lastName);
            preparedStatement.setString(3, address);
            preparedStatement.setString(4, city);
            preparedStatement.setInt(5, this.clientId);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e);
            return;
        }

        updateClientInfo();
    }

    protected void deleteAccount() {
        PreparedStatement deleteDrugs = null;
        PreparedStatement deleteClient = null;

        try {

            try {
                deleteDrugs = connection.prepareStatement("DELETE FROM drugs_and_clients WHERE client_id = ?");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            try {
                deleteClient = connection.prepareStatement("DELETE FROM clients WHERE id = ?");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            System.out.println(this.clientId);
            deleteDrugs.setInt(1, this.clientId);
            deleteClient.setInt(1, this.clientId);

            deleteDrugs.executeUpdate(); // First delete from drugs_and_clients
            deleteClient.executeUpdate(); // Then delete from clients


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean makeTransaction(boolean standard, double amount, String receiver, OrderMeds form) {
        PreparedStatement insertTransaction;
        PreparedStatement subtractExpressCost;
        java.util.Date javaDate = new java.util.Date();
        Date mySQLDate = new Date(javaDate.getTime());
        int transactionTypeId;
        try {
            insertTransaction = connection.prepareStatement("INSERT INTO transactions(amount, type_id, account_id, transaction_date) VALUES (?, ?, ?, ?)");
            insertTransaction.setDouble(1, amount);

            if (standard) {
                transactionTypeId = 2;
                insertTransaction.setInt(2, transactionTypeId);

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(mySQLDate);
                calendar.add(Calendar.DAY_OF_MONTH, 1);

                insertTransaction.setDate(4, new Date(calendar.getTimeInMillis()));
                subtractExpressCost = connection.prepareStatement("UPDATE medicines SET balance = (balance - ?) WHERE client_id = ?");
                subtractExpressCost.setDouble(1, amount);
                subtractExpressCost.setInt(2, clientId);
                subtractExpressCost.executeUpdate();

            } else {
                transactionTypeId = 8;
                insertTransaction.setInt(2, transactionTypeId);
                insertTransaction.setDate(4, mySQLDate);

                subtractExpressCost = connection.prepareStatement("UPDATE medicines SET balance = (balance - 5 - ?) WHERE client_id = ?");
                subtractExpressCost.setDouble(1, amount);
                subtractExpressCost.setInt(2, clientId);

                subtractExpressCost.executeUpdate();
            }

            insertTransaction.executeUpdate();
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }

        PreparedStatement getAccountID;
        PreparedStatement insertIncoming;
        try {
            getAccountID = connection.prepareStatement("SELECT clients_info_view.`ID konta` FROM clients_info_view WHERE `Numer konta` = ?");
            getAccountID.setString(1, receiver);
            ResultSet resultSet = getAccountID.executeQuery();
            resultSet.next();

            insertIncoming = connection.prepareStatement("INSERT INTO transactions (amount, type_id, account_id, transaction_date) VALUES (?, ?, ?, ?)");
            insertIncoming.setDouble(1, amount);
            insertIncoming.setInt(2, transactionTypeId - 1);
            insertIncoming.setInt(3, resultSet.getInt(1));
            insertIncoming.setDate(4, mySQLDate);
            insertIncoming.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(form, "Przelano do klienta innego banku");
        }

        updateClientInfo();
        return true;
    }

    public void updateClientInfo() {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT `Imię`, `Nazwisko`, `Adres`, `Miasto`, `Saldo` FROM clients_info_view WHERE ID = ?");
            preparedStatement.setInt(1, clientId);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();

            firstName = resultSet.getString(1);
            lastName = resultSet.getString(2);
            address = resultSet.getString(3);
            city = resultSet.getString(4);
            balance = resultSet.getDouble(5);
        } catch (SQLException e) {
            System.out.println(e);
            return;
        }

        nameFillLabel.setText(firstName + " " + lastName);
        repaint();
    }

    protected void applyForLoan(double amount) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM loan_info_view WHERE `ID klienta` = ? AND Zatwierdzono IS NULL");
            preparedStatement.setInt(1, clientId);
            ResultSet resultSet = preparedStatement.executeQuery();
            int rows = 0;

            while (resultSet.next())
                rows++;

            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Nie możesz mieć więcej niż jeden oczekujący wniosek o pożyczkę");
                return;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            return;
        }

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO loans(amount, client_id) VALUES(?, ?)");
            preparedStatement.setDouble(1, amount);
            preparedStatement.setInt(2, clientId);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            return;
        }

        JOptionPane.showMessageDialog(this, "Poprawnie złożono wniosek");
    }

    public ArrayList<Medicine> getAvailableCards() throws SQLException {

        return Medicine.generateCards(connection, clientId);
    }

    public void addCardToAccount(Medicine card) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO drugs_and_clients(drug_id, client_id) VALUES (?, ?)");
            preparedStatement.setInt(1, card.drug_id);
            preparedStatement.setInt(2, clientId);


            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            return;
        }
        medicines.add(card);
        JOptionPane.showMessageDialog(this, "Pommelling dodano kartę");
    }

    public void deleteCard(Medicine cardIndex) {
        if (showYesNoPopup("Are you sure you want to delete this medicine? This action cannot be undone.")) {
            Medicine card = cardIndex;
            try {
                PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM drugs_and_clients WHERE drug_id = ? AND client_id= ? AND id= ?");
                preparedStatement.setInt(1, card.drug_id);
                preparedStatement.setInt(2, card.client_id);
                preparedStatement.setInt(3, card.id);
                preparedStatement.executeUpdate();
                medicines.remove(cardIndex);
                JOptionPane.showMessageDialog(this, "Successfully deleted the card.");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, e.getMessage());
            }
        }
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public double getBalance() {
        return balance;
    }



    public ArrayList<Medicine> getCreditCards() {
        return medicines;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public PreparedStatement getPreparedStatement() {
        return preparedStatement;
    }

    public void setPreparedStatement(PreparedStatement preparedStatement) {
        this.preparedStatement = preparedStatement;
    }

    public Connection getConnection() {
        return connection;
    }

    public ResultSet getResultSet() {
        return resultSet;
    }




    public static boolean showYesNoPopup(String message) {
        // Show dialog with Yes/No options
        int option = JOptionPane.showConfirmDialog(
                null,
                message,
                "Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        // Return true for "Yes", false for "No"
        return option == JOptionPane.YES_OPTION;
    }







    // History of transactions
    public class transactionsFrame extends JFrame implements ActionListener {
        private final Client parent;
        private final JButton quitButton;

        private transactionsFrame(Client parent) {
            this.parent = parent;

            JPanel jPanel = new JPanel(new GridLayout(3, 1));
            jPanel.setBackground(new Color(24, 26, 48));

            Font font = new Font("Cooper Black", Font.BOLD | Font.ITALIC, 22);
            JLabel label = new JLabel();
            label.setFont(font);
            label.setForeground(new Color(255, 255, 255));
            label.setText("Bank Bilardzistów");
            label.setHorizontalAlignment(JLabel.CENTER);
            jPanel.add(label);

            setTitle("Historia transakcji");
            setContentPane(jPanel);

            quitButton = new JButton();
            quitButton.setText("Powrót");
            quitButton.addActionListener(this);

            ArrayList<Object[]> dataList = new ArrayList<>();

            try {
                PreparedStatement preparedStatement = Client.this.connection.prepareStatement("SELECT * FROM transactions_all_clients");
//                preparedStatement.setString(1, Employee.this.accountNumber);
                ResultSet set = preparedStatement.executeQuery();
                while (set.next()) {
                    Object[] row = new Object[]{set.getInt(1), set.getString(2), set.getString(3)};
                    dataList.add(row);
                }
                Object[][] data = new Object[dataList.size()][];
                dataList.toArray(data);
                String[] columns = {"Rodzaj transakcji", "Data", "Kwota"};
                DefaultTableModel tableModel = new DefaultTableModel(data, columns);

                JTable transactions = new JTable(tableModel);
                transactions.getColumnModel().getColumn(0).setMinWidth(220);
                JScrollPane scrollPane = new JScrollPane(transactions);
                jPanel.add(scrollPane);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Brak transakcji do pokazania");
            }

            jPanel.add(quitButton);

            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent windowEvent) {
                    parent.setVisible(true);
                }
            });

            setSize(400, 300);
            setVisible(true);

        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == quitButton) {
                parent.setVisible(true);
                dispose();
            }
        }
    }

}
