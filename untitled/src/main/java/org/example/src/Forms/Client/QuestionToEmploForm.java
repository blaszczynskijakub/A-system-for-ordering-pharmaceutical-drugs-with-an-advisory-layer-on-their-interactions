package org.example.src.Forms.Client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class QuestionToEmploForm extends JFrame implements ActionListener {
    private JPanel mainPanel;
    private JLabel amountLabel;
    private JTextField amountTextField;
    private JButton performButton;
    private JButton quitButton;
    private JLabel titleLabel;

    private final Client parent;

    public QuestionToEmploForm(Client client) {
        parent = client;

        performButton.addActionListener(this);
        quitButton.addActionListener(this);

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

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == quitButton){
            dispose();
            parent.setVisible(true);
        }
        if(e.getSource() == performButton){
            double amount;
            try {
                amount = Double.parseDouble(amountTextField.getText());
            }catch (Exception exception){
                JOptionPane.showMessageDialog(this, "Niepoprawna kwota");
                return;
            }
            parent.applyForLoan(amount);
            dispose();
            parent.setVisible(true);
        }
    }
}
