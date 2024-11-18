package org.example.src.Forms.Employee;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ManageAcc extends JFrame implements ActionListener {
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
    private JLabel nameLabel;
    private JTextField textField1;

    private final Employee parent;

    public ManageAcc(Employee employee) {
        parent = employee;

        acceptButton.addActionListener(this);
        quitButton.addActionListener(this);

        setSize(800, 600);
        setLocationRelativeTo(null);

        setTitle("Zarządzaj kontami");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setContentPane(mainPanel);
        setVisible(true);
        pack();
        this.setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                parent.setVisible(true);
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == acceptButton) {
            parent.updateCredentials(
                    textField1.getText(),
                    lastNameField.getText(),
                    addressField.getText(),
                    cityTextField.getText(),
                    firstNameField.getText()
            );
            dispose();
            parent.setVisible(true);
        } else if (e.getSource() == quitButton) {
            dispose();
            parent.setVisible(true);
        }
    }


}
