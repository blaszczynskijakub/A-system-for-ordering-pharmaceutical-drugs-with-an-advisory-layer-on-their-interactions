package org.example.src.Forms.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientCredentialsForm extends JFrame implements ActionListener {
    private JPanel mainPanel;
    private JLabel mainLabel;
    private JLabel firstNameLabel;
    private JLabel lastNameLabel;
    private JLabel addressLabel;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField addressField;
    private JButton acceptButton;
    private JLabel titleLabel;
    private JTextField cityTextField;
    private JButton quitButton;
    private JLabel cityLabel;
    private JButton deleteButton;


    private final Client parent;
    public ClientCredentialsForm(Client client) {
        parent = client;
        firstNameField.setText(client.getFirstName());
        lastNameField.setText(client.getLastName());
        addressField.setText(client.getAddress());
        cityTextField.setText(client.getCity());

        acceptButton.addActionListener(this);
        quitButton.addActionListener(this);
        deleteButton.addActionListener(this);

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
            parent.updateCredentials(firstNameField.getText(), lastNameField.getText(), addressField.getText(), cityTextField.getText());
            dispose();
            parent.setVisible(true);
        }
        if(e.getSource() == quitButton){
            dispose();
            parent.setVisible(true);
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

            yesButton.addActionListener(new YesButtonActionListener(frame));
            noButton.addActionListener(new NoButtonActionListener(frame));

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
    }

    private class YesButtonActionListener implements ActionListener {
        private final JFrame parent;

        private YesButtonActionListener(JFrame parent) {
            this.parent = parent;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ClientCredentialsForm.this.parent.deleteAccount();
            parent.dispose();
        }
    }

    private class NoButtonActionListener implements ActionListener {
        private final JFrame parent;

        private NoButtonActionListener(JFrame parent) {
            this.parent = parent;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            parent.dispose();
        }
    }

}
