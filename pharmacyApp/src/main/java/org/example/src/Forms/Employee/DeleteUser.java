package org.example.src.Forms.Employee;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class DeleteUser extends JFrame implements ActionListener {
    private JPanel mainPanel;
    private JLabel firstNameLabel;
    private JTextField firstNameField;
    private JButton acceptButton;
    private JLabel titleLabel;
    private JButton quitButton;
    private JButton deleteButton;
    private JComboBox<String> comboBox1;
    private JButton searchButton;

    private final Employee parent;

    public DeleteUser(Employee employee) {
        UIManager.put("OptionPane.messageDialogTitle", "Informacja");
        this.parent = employee;
        initializeUI();
        setUpButtonListeners();
        setVisible(true);
    }

    private void initializeUI() {
        setSize(900, 500);
        setLocationRelativeTo(null);

        setResizable(false);
        setTitle("Znajdź klienta");
        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowCloseListener();
        this.setResizable(false);
    }

    private void setUpButtonListeners() {
        quitButton.addActionListener(this);
        deleteButton.addActionListener(this);
        searchButton.addActionListener(this);
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
        if (e.getSource() == quitButton) {
            closeForm();
        } else if (e.getSource() == searchButton) {
            handleSearchAction();
        } else if (e.getSource() == deleteButton) {
            showDeleteConfirmationDialog();
        }
    }

    private void closeForm() {
        dispose();
        parent.setVisible(true);
    }

    private void handleSearchAction() {
        String name = firstNameField.getText().trim();
        if (isValidName(name)) {
            loadUserNamesIntoComboBox(name);
        } else {
            showInvalidInputDialog();
        }
    }

    private boolean isValidName(String name) {
        return name.split(" ").length == 2;
    }

    private void loadUserNamesIntoComboBox(String name) {
        ArrayList<String> items = parent.populateComboBoxWithClients(name, parent.getPreparedStatement(), parent.getConnection(), parent.getResultSet());
        comboBox1.setModel(new DefaultComboBoxModel<>(items.toArray(new String[0])));
    }

    private void showInvalidInputDialog() {
        JDialog dialog = new JDialog(this, "Złe dane wejściowe", true);
        JPanel panel = createDialogPanel("Proszę użyj formatu 'Imię Nazwisko'.");
        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JPanel createDialogPanel(String message) {
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));
        panel.setBackground(Color.WHITE);

        JLabel label = new JLabel(message, JLabel.CENTER);
        label.setForeground(new Color(255, 123, 51));
        panel.add(label);

        JButton okButton = new JButton("Ok");
        okButton.addActionListener(e -> ((JDialog) SwingUtilities.getWindowAncestor(okButton)).dispose());
        panel.add(okButton);

        return panel;
    }

    private void showDeleteConfirmationDialog() {
        JDialog dialog = new JDialog(this, "Potwierdź usunięcie", true);
        JPanel panel = createConfirmationPanel(dialog);
        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JPanel createConfirmationPanel(JDialog dialog) {
        JPanel panel = new JPanel(new GridLayout(2, 2, 20, 10));
        panel.setBackground(new Color(255, 255, 255));

        JLabel label = new JLabel("Czy na pewno chcesz usunąć tego użytkownika?", JLabel.CENTER);
        label.setForeground(new Color(255, 123, 51));
        panel.add(label);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);

        JButton yesButton = createConfirmationButton("Tak", e -> handleUserDeletion(dialog));
        JButton noButton = createConfirmationButton("Nie", e -> dialog.dispose());

        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);
        panel.add(buttonPanel);

        return panel;
    }

    private JButton createConfirmationButton(String text, ActionListener actionListener) {
        JButton button = new JButton(text);
        button.addActionListener(actionListener);
        return button;
    }

    private void handleUserDeletion(JDialog confirmationDialog) {
        String selectedUser = (String) comboBox1.getSelectedItem();
        if (selectedUser != null) {
            int userId = extractUserId(selectedUser);
            parent.deleteAccount(userId);
            confirmationDialog.dispose();
            JOptionPane.showMessageDialog(this, "Pomyślnie usunięto klienta");
            dispose();
            parent.setVisible(true);
        }
        else{
            JOptionPane.showMessageDialog(this, "Nie udało się usunąć klienta");

        }
    }

    private int extractUserId(String selectedUser) {
        String[] parts = selectedUser.split(" ");
        return Integer.parseInt(parts[parts.length - 1]);
    }
}
