package org.example.src.Forms.Client;

import org.example.src.Forms.logging.LogInForm;

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
        initializeUI();
        initializeFieldsWithData();
        addWindowCloseListener();
        setVisible(true);
    }

    private void initializeUI() {
        setTitle("Dane klienta");
        setContentPane(mainPanel);
        setSize(500, 350);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
        setUpButtonListeners();
        setVisible(true);
    }

    private void initializeFieldsWithData() {
        firstNameField.setText(parent.getFirstName());
        lastNameField.setText(parent.getLastName());
        addressField.setText(parent.getAddress());
        cityTextField.setText(parent.getCity());
    }

    private void setUpButtonListeners() {
        acceptButton.addActionListener(this);
        quitButton.addActionListener(this);
        deleteButton.addActionListener(this);
    }

    private void addWindowCloseListener() {
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
            updateClientDataAndClose();
        } else if (e.getSource() == quitButton) {
            closeForm();
        } else if (e.getSource() == deleteButton) {
            showDeleteConfirmationDialog();
        }
    }

    private void updateClientDataAndClose() {
        parent.updateCredentials(
                firstNameField.getText(),
                lastNameField.getText(),
                addressField.getText(),
                cityTextField.getText()
        );
        closeForm();
    }

    private void closeForm() {
        dispose();
        parent.setVisible(true);
    }

    private void showDeleteConfirmationDialog() {
        JDialog confirmationDialog = createConfirmationDialog();
        confirmationDialog.setVisible(true);
    }

    private JDialog createConfirmationDialog() {
        JDialog dialog = new JDialog(this, "Potwierdź usunięcie", true);
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));
        panel.setBackground(new Color(24, 26, 48));

        JLabel label = new JLabel("Czy na pewno chcesz usunąć konto?", JLabel.CENTER);
        label.setForeground(Color.WHITE);
        panel.add(label);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(24, 26, 48));
        JButton yesButton = createConfirmationButton("Tak", new ConfirmDeletionActionListener(dialog));
        JButton noButton = createConfirmationButton("Nie", new CancelDeletionActionListener(dialog));
        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);
        panel.add(buttonPanel);

        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        return dialog;
    }

    private JButton createConfirmationButton(String text, ActionListener actionListener) {
        JButton button = new JButton(text);
        button.addActionListener(actionListener);
        return button;
    }

    private class ConfirmDeletionActionListener implements ActionListener {
        private final JDialog confirmationDialog;

        private ConfirmDeletionActionListener(JDialog confirmationDialog) {
            this.confirmationDialog = confirmationDialog;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            deleteAccountAndRedirect();
            confirmationDialog.dispose();
        }
    }

    private class CancelDeletionActionListener implements ActionListener {
        private final JDialog confirmationDialog;

        private CancelDeletionActionListener(JDialog confirmationDialog) {
            this.confirmationDialog = confirmationDialog;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            confirmationDialog.dispose();
        }
    }

    private void deleteAccountAndRedirect() {
        parent.deleteAccount();
        dispose();
        new LogInForm();
    }
}
