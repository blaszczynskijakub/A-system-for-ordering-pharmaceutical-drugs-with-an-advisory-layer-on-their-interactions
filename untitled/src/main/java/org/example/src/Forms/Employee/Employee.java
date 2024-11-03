package org.example.src.Forms.Employee;


import org.example.src.Forms.DataHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;

public class Employee extends JFrame implements ActionListener, DataHandler {

    public Connection getConnection() {
        return connection;
    }

    private ResultSet testResultSet;

    public void setTestResultSet(ResultSet testResultSet) {
        this.testResultSet = testResultSet;
    }

    private ResultSet resultSet;
    public PreparedStatement getPreparedStatement() {
        return preparedStatement;
    }

    public void setPreparedStatement(PreparedStatement preparedStatement) {
        this.preparedStatement = preparedStatement;
    }

    private PreparedStatement preparedStatement;
    private JButton closeAccButton;
    private JPanel jPanel;
    private JLabel mainLabel;
    private JLabel nameLabel;
    private JLabel balanceLabel;
    private JLabel accountNumberLabel;
    private JLabel nameFillLabel;
    private JLabel positionFillLabel;
    private JLabel branchNameFillLabel;
    //    private JLabel branchAdressFillLabel;
    private JLabel titleLabel;
    private JButton changeDataButton;
    private JLabel adresOddzialuLabel;
    private JLabel label1;
    private JButton controlOrdersButton;
    private JButton editDrugsButton;
    private JButton addAccButton;


    private String firstName;
    private String lastName;
    private String address;
    private String position;
    private final int employeeId;
    private double balance;

    private Connection connection = null;


    public Employee(int employeeId, String firstName, String lastName, String position, String branchName, String branchAdress) {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/database_good", "root", "ColGate1978");
        } catch (Exception e) {
            System.out.println(e);
        }




        this.firstName = firstName;
        this.lastName = lastName;
        this.address = branchAdress;
        this.position = position;
        this.employeeId = employeeId;
        setTitle("Aplikacja Pracownika");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setContentPane(jPanel);
        closeAccButton.addActionListener(this);
        changeDataButton.addActionListener(this);
        controlOrdersButton.addActionListener(this);
        editDrugsButton.addActionListener(this);
        addAccButton.addActionListener(this);

        nameFillLabel.setText(firstName + " " + lastName);
        positionFillLabel.setText(position);
        branchNameFillLabel.setText(branchName);
        label1.setText(branchAdress);

        setVisible(true);
        pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == changeDataButton) {
            setVisible(false);
            new ManageAcc(this);
        } else if (e.getSource() == closeAccButton) {
            setVisible(false);
            new DeleteUser(this);
        }
        else if (e.getSource() == controlOrdersButton) {
            setVisible(false);
            System.out.println(123);
            new TransactionsFrame(this, connection);
        }
        else if(e.getSource() == editDrugsButton){
            setVisible(false);
            new DrugListForEmplo(this,connection);
        }
        else if(e.getSource() == addAccButton){
            setVisible(false);
                String drugName = JOptionPane.showInputDialog(this, "Wprowadź imię:");
                String producentName = JOptionPane.showInputDialog(this, "Wprowadź nazwisko:");
                String drugType = JOptionPane.showInputDialog(this, "Wprowadź miasto:");
                String price = JOptionPane.showInputDialog(this, "Wprowadź adres:");

                try {
                    String query = "INSERT INTO clients (first_name, last_name, address, city) VALUES (?, ?, ?, ?)";
                    PreparedStatement preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setString(1, drugName);
                    preparedStatement.setString(2, producentName);
                    preparedStatement.setString(4, drugType);
                    preparedStatement.setString(3, price);
                    preparedStatement.executeUpdate();
                } catch (SQLException d) {
                    JOptionPane.showMessageDialog(this, "Nie udało się dodać leku");
                }
            }


    }

    public void updateCredentials(String firstName, String lastName, String address, String city, String id) {
        // TODO check if given strings are logical (e.g return if firstName contains a digit)

        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement("UPDATE clients SET first_name = ?, last_name = ?, address = ?, city = ? WHERE id = ?");

            preparedStatement.setString(1, firstName);
            preparedStatement.setString(2, lastName);
            preparedStatement.setString(3, address);
            preparedStatement.setString(4, city);
            preparedStatement.setInt(5, Integer.parseInt(id));

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e);
            return;
        }

        updateClientInfo();
    }

    public void deleteAccount(int id) {
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
            System.out.println(id);
            deleteDrugs.setInt(1, id);
            deleteClient.setInt(1, id);

            deleteDrugs.executeUpdate(); // First delete from drugs_and_clients
            deleteClient.executeUpdate(); // Then delete from clients


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    protected boolean makeTransaction(boolean standard, double amount, String receiver, DeleteUser form) {
        PreparedStatement insertTransaction = null;
        PreparedStatement subtractExpressCost = null;
        java.util.Date javaDate = new java.util.Date();
        java.sql.Date mySQLDate = new java.sql.Date(javaDate.getTime());
        int transactionTypeId;
        try {
            insertTransaction = connection.prepareStatement("INSERT INTO transactions(amount, type_id, account_id, transaction_date) VALUES (?, ?, ?, ?)");
            insertTransaction.setDouble(1, amount);
//                    insertTransaction.setInt(3, departmentId);

            if (standard) {
                transactionTypeId = 2;
                insertTransaction.setInt(2, transactionTypeId);

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(mySQLDate);
                calendar.add(Calendar.DAY_OF_MONTH, 1);

                insertTransaction.setDate(4, new java.sql.Date(calendar.getTimeInMillis()));
            } else {
                transactionTypeId = 8;
                insertTransaction.setInt(2, transactionTypeId);
                insertTransaction.setDate(4, mySQLDate);

                subtractExpressCost = connection.prepareStatement("UPDATE medicines SET balance = balance - 5 WHERE client_id = ?");
                subtractExpressCost.setInt(1, employeeId);

                subtractExpressCost.executeUpdate();
            }

            insertTransaction.executeUpdate();
        } catch (Exception e) {
            System.out.println(e + " TTT");
            return false;
        }

        PreparedStatement transferMoneyToReceiver = null;
        PreparedStatement getAccountID = null;
        PreparedStatement insertIncoming = null;
        try {
//            getAccountID = connection.prepareStatement("SELECT clients_info_view.`ID konta` FROM clients_info_view WHERE `Numer konta` = ?");
            getAccountID.setString(1, receiver);
             resultSet = getAccountID.executeQuery();
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


    private void updateClientInfo() {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT `Imię`, `Nazwisko`, `Adres`, `Miasto`, `Saldo` FROM clients_info_view WHERE ID = ?");
            preparedStatement.setInt(1, employeeId);
             resultSet = preparedStatement.executeQuery();
            resultSet.next();

            firstName = resultSet.getString(1);
            lastName = resultSet.getString(2);
            address = resultSet.getString(3);
            position = resultSet.getString(4);
            balance = resultSet.getDouble(5);

            repaint();

        } catch (SQLException e) {
            System.out.println(e);
            return;
        }

    }

    protected String[] getLoans(){
        String[] loans = null;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT ID, `Imie i nazwisko` FROM loan_info_view WHERE Zatwierdzono IS NULL");

             resultSet = preparedStatement.executeQuery();

            ArrayList<String> loansList = new ArrayList<>();

            while (resultSet.next()){
                loansList.add(resultSet.getInt(1) + ", " + resultSet.getString(2));
            }

            loans = loansList.toArray(new String[0]);

        }catch (SQLException e){
            JOptionPane.showMessageDialog(this, e.getMessage());
        }

        return loans;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    protected String[] getLoanInfo(int id){
        String[] loans = null;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT Kwota, `Imie i nazwisko`, `Adres zamieszkania`, `Saldo klienta`, `Obrót na koncie` FROM loan_info_view WHERE ID = ? AND Zatwierdzono IS NULL");
            preparedStatement.setInt(1, id);

             resultSet = preparedStatement.executeQuery();
            resultSet.next();

            ArrayList<String> loansList = new ArrayList<>();

            loansList.add(String.valueOf(resultSet.getInt(1)));
            loansList.add(resultSet.getString(2));
            loansList.add(resultSet.getString(3));
            loansList.add(String.valueOf(resultSet.getDouble(4)));
            loansList.add(String.valueOf(resultSet.getDouble(5)));

            loans = loansList.toArray(new String[0]);

        }catch (SQLException e){
            JOptionPane.showMessageDialog(this, e.getMessage());
        }

        return loans;
    }

    protected void reviewLoan(boolean approved, int id){
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE loans SET approved = ? WHERE id = ?");
            preparedStatement.setBoolean(1, approved);
            preparedStatement.setInt(2, id);

            preparedStatement.executeUpdate();
        }catch (SQLException e){
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }


    public ResultSet getResultSet() {
        return resultSet;
    }

//    public ArrayList<String> populateComboBox(String dane) {
//        try {
//
//
//            String query = "SELECT id, acc_nr, acc_id FROM close_acc_info_view WHERE first_name=? AND last_name=? ";
//
//            preparedStatement = connection.prepareStatement(query);
//            preparedStatement.setString(1, dane.split(" ")[0]);
//            preparedStatement.setString(2, dane.split(" ")[1]);
//             resultSet = preparedStatement.executeQuery();
//
//            // Populate the comboBox1 with the fetched data
//            ArrayList<String> items = new ArrayList<>();
//            while (resultSet.next()) {
//                items.add("id: " + resultSet.getString("id") + ", nr konta: " + resultSet.getString("acc_nr")+ " , id konta: "+resultSet.getString("acc_id"));
//            }
//            // Add items to comboBox1
//
//            // Close the resources
//            resultSet.close();
//            preparedStatement.close();
//            return items;
//
//
//
//        } catch (SQLException ex) {
//            throw new RuntimeException(ex);
//        }
//
//
//    }

    public void deleteAcc(String dane, Connection connection) {
        try {
            String queryFirst = "DELETE FROM transactions WHERE  account_id=?";
            PreparedStatement preparedStatementFirst = this.connection.prepareStatement(queryFirst);
            preparedStatementFirst.setString(1, ((String) dane.split(" ")[8]));

            preparedStatementFirst.executeUpdate();
            String query = "DELETE FROM medicines WHERE  account_number=?";
            PreparedStatement preparedStatement = this.connection.prepareStatement(query);
            preparedStatement.setString(1, ((String) dane.split(" ")[4]));

            preparedStatement.executeUpdate();


            preparedStatement.close();
            preparedStatementFirst.close();


        } catch (SQLException ex) {
            throw new RuntimeException(ex);
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

    public String getPosition() {
        return position;
    }

    public double getBalance() {
        return balance;
    }


    // History of transactions
    public class TransactionsFrame extends JDialog {
        private JFrame parent;
        private Connection connection;
        private JButton quitButton;
        private JButton changeStatus;
        private JList<String> transactionsList; // Make transactionsList an instance variable
        private DefaultListModel<String> listModel;

        public TransactionsFrame(JFrame parent, Connection connection) {
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

            // Add change status button with functionality
            changeStatus = new JButton("Zmień status na 'w dostawie'");
            changeStatus.addActionListener(e -> {


                int startIndex = transactionsList.getSelectedValue().indexOf("Order ID:") + 10; // Start after "Order ID:"
                int endIndex = transactionsList.getSelectedValue().indexOf("Date:");

                String orderIdString = transactionsList.getSelectedValue().substring(startIndex, endIndex).trim(); // Extract the order ID as a string
                int orderId = Integer.parseInt(orderIdString.replace(",","")); // Parse to integer

                String queryFirst = "UPDATE  drugs_and_clients SET status='taken' WHERE  id=?";

                PreparedStatement preparedStatementFirst = null;
                try {
                    preparedStatementFirst = this.connection.prepareStatement(queryFirst);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
                try {
                    preparedStatementFirst.setInt(1, orderId);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }


                try {
                    preparedStatementFirst.executeUpdate();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
                //mam tu id klienta i id transakcji konkretnej
                //ale jednak trzeba w sql tez ten status wsadzić :/

            });
            listModel = new DefaultListModel<>();
            transactionsList = new JList<>(listModel); // Initialize transactionsList

            // Load transactions into the list
            loadTransactions();

            transactionsList.setFont(new Font("Arial", Font.PLAIN, 16));
            transactionsList.setBackground(new Color(240, 240, 240));
            JScrollPane scrollPane = new JScrollPane(transactionsList);
            scrollPane.setPreferredSize(new Dimension(380, 200));

            jPanel.add(scrollPane, BorderLayout.CENTER);
            jPanel.add(quitButton, BorderLayout.SOUTH);
            jPanel.add(changeStatus, BorderLayout.EAST);

            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent windowEvent) {
                    parent.setVisible(true);
                }
            });

            setSize(600, 400); // Increased size for better readability
            setLocationRelativeTo(parent);
            setVisible(true);
        }
        private void loadTransactions() {
            listModel.clear(); // Clear the list before loading transactions
            try {
                PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM transactions_all_clients WHERE  status=?");
                preparedStatement.setString(1,"inRealisation");



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

    }

    }



