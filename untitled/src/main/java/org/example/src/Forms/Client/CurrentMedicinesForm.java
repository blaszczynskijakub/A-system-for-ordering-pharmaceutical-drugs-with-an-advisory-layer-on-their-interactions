package org.example.src.Forms.Client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class CurrentMedicinesForm extends JFrame implements ActionListener {
    private JPanel mainPanel;
    private JLabel titleLabel;
    private JLabel cardsLabel;
    private JButton orderCardButton;
    private JButton quitButton;
    private JList<Medicine> list1;
    private JButton deleteButton;
    private JList<Medicine> list2;
    private JList<Medicine> list3;
    private JButton confReceiveButton;

    private final Client parent;
    private Connection connection;

    public CurrentMedicinesForm(Client parent, Connection connection) {
        this.parent = parent;
        this.connection = connection;

        // Load medicines and populate lists
        loadMedicinesIntoLists();

        // Set up listeners for exclusive selection
        list1.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !list1.isSelectionEmpty()) {
                list2.clearSelection();
                list3.clearSelection();
            }
        });

        list2.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !list2.isSelectionEmpty()) {
                list1.clearSelection();
                list3.clearSelection();
            }
        });

        list3.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !list3.isSelectionEmpty()) {
                list1.clearSelection();
                list2.clearSelection();
            }
        });

        // Set up action listeners for buttons
        quitButton.addActionListener(this);
        deleteButton.addActionListener(this);
        confReceiveButton.addActionListener(this);

        // Window close operation
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                parent.setVisible(true);
            }
        });

        // Frame setup
        setContentPane(mainPanel);
        setVisible(true);
        pack();
    }

    private void loadMedicinesIntoLists() {
        ArrayList<Medicine> dataListInRealisation = new ArrayList<>();
        ArrayList<Medicine> dataListInReceived = new ArrayList<>();
        ArrayList<Medicine> dataListToTake = new ArrayList<>();

        try {
            // Load medicines from database
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT `drug_id`, `id`, `transaciont_id`, `drug_name`, `producent`, `price`, `status` " +
                            "FROM client_and_drug_all_info_fixed WHERE `id` = ?"
            );
            preparedStatement.setInt(1, parent.clientId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Medicine med = new Medicine(
                        resultSet.getInt("drug_id"),
                        resultSet.getInt("id"),
                        resultSet.getInt("transaciont_id"),
                        resultSet.getString("drug_name"),
                        resultSet.getString("producent"),
                        resultSet.getInt("price"),
                        Medicine.OrderStatus.valueOf(resultSet.getString("status"))
                );

                switch (med.getOrderStatus()) {
                    case inRealisation -> dataListInRealisation.add(med);
                    case taken -> dataListInReceived.add(med);
                    case readyToTake -> dataListToTake.add(med);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading medicines: " + ex.getMessage());
        }

        // Set lists' data
        list1.setListData(dataListToTake.toArray(new Medicine[0]));
        list2.setListData(dataListInReceived.toArray(new Medicine[0]));
        list3.setListData(dataListInRealisation.toArray(new Medicine[0]));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == quitButton) {
            dispose();
            parent.setVisible(true);
        }
        else if (e.getSource() == deleteButton) {
            // Handle deletion based on selection in any of the lists
            if (!list1.isSelectionEmpty()) {
                parent.deleteCard(list1.getSelectedValue());
            } else if (!list2.isSelectionEmpty()) {
                parent.deleteCard(list2.getSelectedValue());
            } else if (!list3.isSelectionEmpty()) {
                parent.deleteCard(list3.getSelectedValue());
            }

            // Reload the form to update lists
            loadMedicinesIntoLists();
        }
        else if (e.getSource() == confReceiveButton) {
            // Confirmation logic for list2
            if (!list2.isSelectionEmpty()) {
                Medicine medOrdered = list2.getSelectedValue();
                int idOfOrder = medOrdered.getId();

                String query = "UPDATE drugs_and_clients SET status='readyToTake' WHERE id=?";
                try (PreparedStatement preparedStatement = this.connection.prepareStatement(query)) {
                    preparedStatement.setInt(1, idOfOrder);
                    preparedStatement.executeUpdate();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error updating status: " + ex.getMessage());
                }

                // Reload medicines list after update
                loadMedicinesIntoLists();
            }
        }
    }

    // Getters for the lists if needed outside this class
    public JList<Medicine> getList1() {
        return list1;
    }

    public JList<Medicine> getList2() {
        return list2;
    }

    public JList<Medicine> getList3() {
        return list3;
    }
}
