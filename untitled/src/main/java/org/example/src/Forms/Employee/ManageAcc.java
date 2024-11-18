package org.example.src.Forms.Employee;

import javax.swing.*;
import java.awt.*;
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

        // Center the frame on the screen and set it to a larger size
        setSize(800, 600);  // You can adjust this size if needed
        setLocationRelativeTo(null);

        // Set up the main frame settings
        setTitle("Zarządzaj kontami");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setContentPane(mainPanel);
        setVisible(true);
        pack();
        this.setResizable(false);
        // Add a window listener to handle the parent visibility on close
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

    private void showDeleteConfirmationDialog() {
        // Create and set up the delete confirmation dialog
        JFrame frame = new JFrame();
        JPanel jPanel = new JPanel();
        jPanel.setBackground(new Color(24, 26, 48));

        JLabel label = new JLabel("Czy na pewno chcesz usunąć konto?");
        label.setForeground(Color.WHITE);
        JButton yesButton = new JButton("Tak");
        JButton noButton = new JButton("Nie");

        yesButton.addActionListener(e -> {
            parent.deleteAccount(Integer.parseInt(textField1.getText())); // Adjust as necessary
            frame.dispose();
            dispose();
            parent.setVisible(true);
        });

        noButton.addActionListener(e -> frame.dispose());

        // Add components to the dialog panel
        jPanel.add(label);
        jPanel.add(yesButton);
        jPanel.add(noButton);

        // Set up the dialog frame settings
        frame.setContentPane(jPanel);
        frame.setSize(400, 150);  // Adjust size as needed
        frame.setLocationRelativeTo(this);  // Center it relative to the main frame
        frame.setResizable(false);
        frame.setVisible(true);
    }
}
