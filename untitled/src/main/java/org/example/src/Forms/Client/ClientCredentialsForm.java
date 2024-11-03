package org.example.src.Forms.Client;

import org.example.src.Forms.LogInForm;

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
        this.parent = client;

        // Initialize fields with client's data
        firstNameField.setText(client.getFirstName());
        lastNameField.setText(client.getLastName());
        addressField.setText(client.getAddress());
        cityTextField.setText(client.getCity());

        // Set up action listeners for buttons
        acceptButton.addActionListener(this);
        quitButton.addActionListener(this);
        deleteButton.addActionListener(this);

        // Handle window closing
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
        if (e.getSource() == acceptButton) {
            // Update client credentials and close form
            parent.updateCredentials(
                    firstNameField.getText(),
                    lastNameField.getText(),
                    addressField.getText(),
                    cityTextField.getText()
            );
            dispose();
            parent.setVisible(true);
        } else if (e.getSource() == quitButton) {
            // Close form and return to parent
            dispose();
            parent.setVisible(true);
        } else if (e.getSource() == deleteButton) {
            // Show confirmation dialog for account deletion
            showDeleteConfirmationDialog();
        }
    }

    private void showDeleteConfirmationDialog() {
        // Create a modal JDialog for confirmation
        JDialog dialog = new JDialog(this, "Confirm Deletion", true);
        JPanel panel = new JPanel();
        panel.setBackground(new Color(24, 26, 48));

        JLabel label = new JLabel("Czy na pewno chcesz usunąć konto?");
        label.setForeground(Color.WHITE);
        JButton yesButton = new JButton("Tak");
        JButton noButton = new JButton("Nie");

        panel.add(label);
        panel.add(yesButton);
        panel.add(noButton);

        // Add action listeners to the buttons
        yesButton.addActionListener(new YesButtonActionListener(dialog));
        noButton.addActionListener(new NoButtonActionListener(dialog));

        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this); // Center on the parent frame
        dialog.setVisible(true);
    }

    private class YesButtonActionListener implements ActionListener {
        private final JDialog confirmationDialog;

        private YesButtonActionListener(JDialog confirmationDialog) {
            this.confirmationDialog = confirmationDialog;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // Delete account and close dialogs
            ClientCredentialsForm.this.parent.deleteAccount();
            confirmationDialog.dispose(); // Close the confirmation dialog
            ClientCredentialsForm.this.dispose(); // Close the main form
            new LogInForm();
        }
    }

    private class NoButtonActionListener implements ActionListener {
        private final JDialog confirmationDialog;

        private NoButtonActionListener(JDialog confirmationDialog) {
            this.confirmationDialog = confirmationDialog;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // Close the confirmation dialog without deleting the account
            confirmationDialog.dispose();
        }
    }
}
