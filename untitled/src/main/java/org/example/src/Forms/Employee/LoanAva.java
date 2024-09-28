package org.example.src.Forms.Employee;


import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoanAva extends JFrame implements ActionListener {
    private JLabel mainLabel;
    private JPanel mainPanel;
    private JLabel amountLabel;
    private JButton performButton;
    private JButton quitButton;
    private JLabel titleLabel;
    private JLabel balanceLabel;
    private JLabel trafficLabel;
    private JLabel nameLabel;
    private JLabel addressLabel;
    private JLabel amountFillLabel;
    private JLabel nameFillLabel;
    private JLabel addressFillLabel;
    private JLabel balanceFillLabel;
    private JLabel trafficFillLabel;
    private JComboBox<String> comboBox1;
    private JLabel clientLabel;
    private JButton declineButton;

    private final Employee parent;

    public LoanAva(Employee employee) {
        parent = employee;

        performButton.addActionListener(this);
        declineButton.addActionListener(this);
        quitButton.addActionListener(this);
        comboBox1.addActionListener(this);

        String[] items = parent.getLoans();

        for(String item : items){
            comboBox1.addItem(item);
        }

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
        if(e.getSource() == performButton && comboBox1.getItemCount()!=0){
            String[] parts = comboBox1.getItemAt(comboBox1.getSelectedIndex()).split(",");
            parent.reviewLoan(true, Integer.parseInt(parts[0]));

            comboBox1.removeAllItems();
            String[] items = parent.getLoans();

            for(String item : items){
                comboBox1.addItem(item);
            }

            repaint();
        }
        if(e.getSource() == declineButton && comboBox1.getItemCount()!=0 ){
            String[] parts = comboBox1.getItemAt(comboBox1.getSelectedIndex()).split(",");
            parent.reviewLoan(false, Integer.parseInt(parts[0]));

            comboBox1.removeAllItems();
            String[] items = parent.getLoans();

            for(String item : items){
                comboBox1.addItem(item);
            }

            repaint();
        }
        if(e.getSource() == comboBox1) {
            try {
                String[] parts = comboBox1.getItemAt(comboBox1.getSelectedIndex()).split(",");

                String[] info = parent.getLoanInfo(Integer.parseInt(parts[0]));

                amountFillLabel.setText(info[0]);
                nameFillLabel.setText(info[1]);
                addressFillLabel.setText(info[2]);
                balanceFillLabel.setText(info[3]);
                trafficFillLabel.setText(info[4]);

                repaint();
            }
            catch (Exception s) {
            }
        }
    }
}
