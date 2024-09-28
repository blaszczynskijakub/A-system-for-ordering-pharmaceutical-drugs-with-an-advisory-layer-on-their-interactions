package org.example.src.Forms.Client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;

public class Client extends JFrame implements ActionListener {
    private JButton manageCardsButton;
    private JButton changeAccountDetailsButton;
    private JButton transferMoneyButton;
    private JButton loanButton;
    private JPanel jPanel;
    private JLabel mainLabel;
    private JLabel nameLabel;
    private JLabel balanceLabel;
    private JLabel accountNumberLabel;
    private JLabel nameFillLabel;
    private JLabel balanceFillLabel;
    private JLabel accountFillLabel;
    private JLabel titleLabel;
    private JButton transactionsHistoryButton;


    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private final int clientId;
    private final int accountId;
    private final String accountNumber;



    private double balance;
    private ArrayList<CreditCard> creditCards;

    private Connection connection = null;
    public void setBalance(double balance) {
        this.balance = balance;
    }
    public Client(int clientId, String firstName, String lastName, String address, String city, Double balance, String accountNumber, int accountId) {
        creditCards = new ArrayList<>();
        try {
            connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/database_good", "root", "ColGate1978");
            PreparedStatement preparedStatement = Client.this.connection.prepareStatement("SELECT `Numer karty`, `Data ważności`, Producent FROM cards_view WHERE `Id klienta` = ?");
            preparedStatement.setInt(1, clientId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                creditCards.add(new CreditCard(resultSet.getString(1), resultSet.getDate(2), resultSet.getString(3)));
            }
        }catch (Exception e){
            System.out.println(e);
        }

        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.city = city;
        this.clientId = clientId;
        this.balance = balance;
        this.accountNumber = accountNumber;
        this.accountId = accountId;
        setTitle("Aplikacja Klienta");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setContentPane(jPanel);

        manageCardsButton.addActionListener(this);
        changeAccountDetailsButton.addActionListener(this);
        transferMoneyButton.addActionListener(this);
        loanButton.addActionListener(this);
        transactionsHistoryButton.addActionListener(this);

        nameFillLabel.setText(firstName + " " + lastName);
        balanceFillLabel.setText(String.valueOf(balance));
        accountFillLabel.setText(accountNumber);

        setVisible(true);
        pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == changeAccountDetailsButton){
            setVisible(false);
            new ClientCredentialsForm(this);
        }
        else if(e.getSource() == transferMoneyButton){
            setVisible(false);
            new TransferForm(this);
        }
        else if(e.getSource() == loanButton){
            setVisible(false);
            new LoanForm(this);
        }
        else if(e.getSource() == manageCardsButton){
            setVisible(false);
            new CreditCardsForm(this);
        }
        else if(e.getSource() == transactionsHistoryButton){
            setVisible(false);
            new transactionsFrame(this);
        }

    }

    protected void updateCredentials(String firstName, String lastName, String address, String city){
        // TODO check if given strings are logical (e.g return if firstName contains a digit)

        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement("UPDATE clients SET first_name = ?, last_name = ?, address = ?, city = ? WHERE id = ?");

            preparedStatement.setString(1, firstName);
            preparedStatement.setString(2, lastName);
            preparedStatement.setString(3, address);
            preparedStatement.setString(4, city);
            preparedStatement.setInt(5, this.clientId);

            preparedStatement.executeUpdate();
        }catch (SQLException e){
            System.out.println(e);
            return;
        }

        updateClientInfo();
    }

    protected void deleteAccount(){
        PreparedStatement deleteCards = null;
        PreparedStatement deleteAccount = null;
        PreparedStatement deleteClient = null;
        try {
            deleteCards = connection.prepareStatement("DELETE FROM credit_card WHERE client_id = ?");
            deleteAccount = connection.prepareStatement("DELETE FROM accounts WHERE client_id = ?");
            deleteClient = connection.prepareStatement("DELETE FROM clients WHERE id = ?");

            deleteCards.setInt(1, this.clientId);
            deleteAccount.setInt(1, this.clientId);
            deleteClient.setInt(1, this.clientId);

            deleteCards.executeUpdate();
            deleteAccount.executeUpdate();
            deleteClient.executeUpdate();

        }catch (SQLException e){
            System.out.println(e);
            return;
        }

        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    public boolean makeTransaction(boolean standard, double amount, String receiver, TransferForm form){
        PreparedStatement insertTransaction = null;
        PreparedStatement subtractExpressCost = null;
        java.util.Date javaDate = new java.util.Date();
        java.sql.Date mySQLDate = new java.sql.Date(javaDate.getTime());
        int transactionTypeId;
        try{
            insertTransaction = connection.prepareStatement("INSERT INTO transactions(amount, type_id, account_id, transaction_date) VALUES (?, ?, ?, ?)");
            insertTransaction.setDouble(1, amount);
            insertTransaction.setInt(3, accountId);

            if(standard){
                transactionTypeId = 2;
                insertTransaction.setInt(2, transactionTypeId);

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(mySQLDate);
                calendar.add(Calendar.DAY_OF_MONTH, 1);

                insertTransaction.setDate(4, new java.sql.Date(calendar.getTimeInMillis()));
            }
            else{
                transactionTypeId = 8;
                insertTransaction.setInt(2, transactionTypeId);
                insertTransaction.setDate(4, mySQLDate);

                subtractExpressCost = connection.prepareStatement("UPDATE accounts SET balance = balance - 5 WHERE client_id = ?");
                subtractExpressCost.setInt(1, clientId);

                subtractExpressCost.executeUpdate();
            }

            insertTransaction.executeUpdate();
        }catch (Exception e){
            System.out.println(e);
            return false;
        }

        PreparedStatement getAccountID = null;
        PreparedStatement insertIncoming = null;
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
        }catch (SQLException e){
            JOptionPane.showMessageDialog(form, "Przelano do klienta innego banku");
        }

        updateClientInfo();
        return true;
    }

    public void updateClientInfo(){
        try{
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT `Imię`, `Nazwisko`, `Adres`, `Miasto`, `Saldo` FROM clients_info_view WHERE ID = ?");
            preparedStatement.setInt(1, clientId);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();

            firstName = resultSet.getString(1);
            lastName = resultSet.getString(2);
            address = resultSet.getString(3);
            city = resultSet.getString(4);
            balance = resultSet.getDouble(5);
        }catch (SQLException e){
            System.out.println(e);
            return;
        }

        nameFillLabel.setText(firstName + " " + lastName);
        balanceFillLabel.setText(String.valueOf(balance));
        repaint();
    }

    protected void applyForLoan(double amount){
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM loan_info_view WHERE `ID klienta` = ? AND Zatwierdzono IS NULL");
            preparedStatement.setInt(1, clientId);
            ResultSet resultSet = preparedStatement.executeQuery();
            int rows = 0;

            while(resultSet.next())
                rows++;

            if(rows > 0){
                JOptionPane.showMessageDialog(this, "Nie możesz mieć więcej niż jeden oczekujący wniosek o pożyczkę");
                return;
            }
        }catch (SQLException e){
            JOptionPane.showMessageDialog(this, e.getMessage());
            return;
        }

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO loans(amount, client_id) VALUES(?, ?)");
            preparedStatement.setDouble(1, amount);
            preparedStatement.setInt(2, clientId);

            preparedStatement.executeUpdate();
        }catch (SQLException e){
            JOptionPane.showMessageDialog(this, e.getMessage());
            return;
        }

        JOptionPane.showMessageDialog(this,"Poprawnie złożono wniosek");
    }

    public ArrayList<CreditCard> getAvailableCards(){
        return CreditCard.generateCards(connection);
    }

    public void addCardToAccount(CreditCard card){
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO credit_card(card_number, expiry_date, client_id, producent_name) VALUES (?, ?, ?, ?)");
            preparedStatement.setString(1, card.cardNumber());
            preparedStatement.setDate(2, card.expiryDate());
            preparedStatement.setInt(3, this.clientId);
            preparedStatement.setString(4, card.producerName());

            preparedStatement.executeUpdate();
        }catch (SQLException e){
            JOptionPane.showMessageDialog(this, e.getMessage());
            return;
        }
        creditCards.add(card);
        JOptionPane.showMessageDialog(this, "Pomyślnie dodano kartę");
    }

    public void deleteCard(int cardIndex){
        CreditCard card = creditCards.get(cardIndex);
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM credit_card WHERE card_number = ?");
            preparedStatement.setString(1, card.cardNumber());

            preparedStatement.executeUpdate();
        }catch (SQLException e){
            JOptionPane.showMessageDialog(this, e.getMessage());
            return;
        }
        creditCards.remove(cardIndex);
        JOptionPane.showMessageDialog(this, "Pomyślnie usunięto kartę");
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

    public String getAccountNumber() {
        return accountNumber;
    }

    public ArrayList<CreditCard> getCreditCards() {
        return creditCards;
    }

    public void setConnection(Connection connection){
        this.connection = connection;
    }

    // History of transactions
    private class transactionsFrame extends JFrame implements ActionListener{
        private final Client parent;
        private final JButton quitButton;
        private transactionsFrame(Client parent){
            this.parent = parent;

            JPanel jPanel = new JPanel(new GridLayout(3,1));
            jPanel.setBackground(new Color(24, 26, 48));

            Font font = new Font("Cooper Black", Font.BOLD | Font.ITALIC, 22);
            JLabel label = new JLabel();
            label.setFont(font);
            label.setForeground(new Color(255,255,255));
            label.setText("Bank Bilardzistów");
            label.setHorizontalAlignment(JLabel.CENTER);
            jPanel.add(label);

            setTitle("Historia transakcji");
            setContentPane(jPanel);

            quitButton = new JButton();
            quitButton.setText("Powrót");
            quitButton.addActionListener(this);

            ArrayList<Object[]> dataList = new ArrayList<>();

            try{
                PreparedStatement preparedStatement = Client.this.connection.prepareStatement("SELECT `Rodzaj transakcji`, Data, Kwota FROM transactions_view WHERE `Numer konta` = ?");
                preparedStatement.setString(1, Client.this.accountNumber);
                ResultSet set = preparedStatement.executeQuery();
                while(set.next()){
                    Object[] row = new Object[]{set.getString(1), set.getDate(2),set.getDouble(3)};
                    dataList.add(row);
                }
                Object[][] data = new Object[dataList.size()][];
                dataList.toArray(data);
                String[] columns = {"Rodzaj transakcji", "Data","Kwota"};
                DefaultTableModel tableModel = new DefaultTableModel(data, columns);

                JTable transactions = new JTable(tableModel);
                transactions.getColumnModel().getColumn(0).setMinWidth(220);
                JScrollPane scrollPane = new JScrollPane(transactions);
                jPanel.add(scrollPane);
            }catch (SQLException e){
                JOptionPane.showMessageDialog(this, "Brak transakcji do pokazania");
            }

            jPanel.add(quitButton);

            addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                    parent.setVisible(true);
                }
            });

            setSize(400, 300);
            setVisible(true);

        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == quitButton){
                parent.setVisible(true);
                dispose();
            }
        }
    }

}
