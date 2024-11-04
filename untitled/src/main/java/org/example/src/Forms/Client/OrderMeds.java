package org.example.src.Forms.Client;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class OrderMeds extends JFrame implements ActionListener {

    private JPanel mainPanel;
    private JLabel mainLabel;
    private JLabel firstNameLabel;
    private JLabel lastNameLabel;
    private JLabel addressLabel;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField addressField;
    private JButton acceptButton;
    private JLabel cardsLabel;
    private JList cardList;
    private JLabel titleLabel;
    private JTextField cityTextField;
    private JButton quitButton;
    private JLabel cityLabel;
    private JButton deleteButton;
    private JComboBox comboBox1;
    private JButton searchButton;
    private JButton orderMedButton;

    public JComboBox getComboBox1() {
        return comboBox1;
    }

    private final Client parent;

    public void searchButton()
    {
        String name = firstNameField.getText();
        //name.split(" ").length!=2 && name.split(" ").length!=1
        if(name.matches(".*[^a-zA-Z0-9\\s].*") || name.isEmpty())
        {
            JFrame frame = new JFrame();
            JPanel jPanel = new JPanel();
            jPanel.setBackground(new Color(24, 26, 48));
            JLabel label = new JLabel("Proszę uzupełnić pole bez znaków specjalnych");
            label.setForeground(new Color(255, 255, 255));
            JButton okButton = new JButton("Ok");

            okButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    frame.dispose(); // Close the current JFrame
                }
            });
            jPanel.add(okButton);
            jPanel.add(label);



            addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                    parent.setVisible(true);
                }
            });

            frame.setContentPane(jPanel);

            frame.pack();
            frame.setVisible(true);
        }
        else {
            ArrayList<Medicine> items = parent.populateComboBox(name, parent.getPreparedStatement(), parent.getConnection(), parent.getResultSet(), parent.clientId);
            comboBox1.setModel(new DefaultComboBoxModel<>(items.toArray(new Medicine[0])));
//            System.out.println((((String) comboBox1.getSelectedItem()).split(" "))[(((String) comboBox1.getSelectedItem()).split(" ")).length-1]);

        }
    }
    public OrderMeds(Client employee) {
        parent = employee;

        quitButton.addActionListener(this);
        searchButton.addActionListener(this);
        orderMedButton.addActionListener(this);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                parent.setVisible(true);
            }
        });

        setContentPane(mainPanel);
        setVisible(true);
        pack();
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == acceptButton){
//            parent.updateCredentials(firstNameField.getText(), lastNameField.getText(), addressField.getText(), cityTextField.getText());
            dispose();
            parent.setVisible(true);

        }
        if(e.getSource() == quitButton){
            dispose();
            parent.setVisible(true);
        }
        if (e.getSource() == searchButton) {

            searchButton();

        }
        if(e.getSource() == orderMedButton){
             final ArrayList<Medicine> availableCards;

            try {
                availableCards = parent.getAvailableCards();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
//
//            System.out.println(comboBox1.getSelectedItem());

            //nowa array dla lekow w realizacji itd.
            if(availableCards.size()<30)
            parent.addCardToAccount((Medicine) comboBox1.getSelectedItem());
            else{
                JOptionPane.showMessageDialog(this, "Za dużo zamówień, do konta może być przypisanych łączenie maksymalnie 29 leków, usuń jakieś produkty");

            }
//





//            ArrayList<String> dataList = new ArrayList<>();
//            for(Medicine card : parent.getCreditCards()){
//                dataList.add(card.toString());
//            }
//            String[] data = new String[dataList.size()];
//            dataList.toArray(data);
            try {
                parent.connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/database_good", "root", "#hom^ik34");
                PreparedStatement preparedStatement = parent.connection.prepareStatement("SELECT `drug_id`,`id`,`transaciont_id`, `drug_name`,`producent`,`price` ,`status` FROM client_and_drug_all_info_fixed WHERE `id` = ?");
                preparedStatement.setInt(1, parent.clientId);
                ResultSet resultSet = preparedStatement.executeQuery();
                int i=0;
                while (parent.medicines.size()>0) {
                    parent.medicines.remove(0);
//                medicines.add(new Medicine(resultSet.getString(1), resultSet.getDate(2), resultSet.getString(3)));
                }
                while (resultSet.next()) {
                    parent.medicines.add(new Medicine(Integer.parseInt(resultSet.getString(1)), Integer.parseInt(resultSet.getString(2)), Integer.parseInt(resultSet.getString(3)),resultSet.getString(4),resultSet.getString(5), resultSet.getInt(6),Medicine.OrderStatus.valueOf(resultSet.getString(7))));
//                medicines.add(new Medicine(resultSet.getString(1), resultSet.getDate(2), resultSet.getString(3)));
                }
            }catch (Exception s){
                System.out.println(s);
            }
//
//
//            dispose();


        }


    }

    private class YesButtonActionListener implements ActionListener {
        private final JFrame parent;

        private YesButtonActionListener(JFrame parent) {
            this.parent = parent;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OrderMeds.this.parent.deleteAccount();
            parent.dispose();
        }
    }



}

