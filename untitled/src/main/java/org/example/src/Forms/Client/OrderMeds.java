package org.example.src.Forms.Client;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
            ArrayList<String> items = parent.populateComboBox(name, parent.getPreparedStatement(), parent.getConnection(), parent.getResultSet());
            comboBox1.setModel(new DefaultComboBoxModel<>(items.toArray(new String[0])));
        }
    }
    public OrderMeds(Client employee) {
        parent = employee;

        quitButton.addActionListener(this);
        deleteButton.addActionListener(this);
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

            availableCards = parent.getAvailableCards();


            parent.addCardToAccount(availableCards.get(comboBox1.getSelectedIndex()));

            ArrayList<String> dataList = new ArrayList<>();
            for(Medicine card : parent.getCreditCards()){
                dataList.add(card.toString());
            }
            String[] data = new String[dataList.size()];
            dataList.toArray(data);



            dispose();


        }

            if(e.getSource() == deleteButton){
            JFrame frame = new JFrame();
            JPanel jPanel = new JPanel();
            jPanel.setBackground(new Color(24, 26, 48));
            JLabel label = new JLabel("Czy na pewno chcesz usunąć konto?");
            label.setForeground(new Color(255, 255, 255));
            JButton yesButton = new JButton("Tak");
            JButton noButton = new JButton("Nie");

            jPanel.add(label);
            jPanel.add(yesButton);
            jPanel.add(noButton);



            addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                    parent.setVisible(true);
                }
            });

            noButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {frame.dispose(); // Close the current JFrame
                }
            });
            yesButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    parent.deleteAcc(((String) comboBox1.getSelectedItem()), parent.getConnection());

                    ;



                    frame.dispose(); // Close the current JFrame
                }
            });



            frame.setContentPane(jPanel);

            frame.pack();
            frame.setVisible(true);
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

