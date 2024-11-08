package org.example.src.Forms.Client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
    private final Connection connection;

    public CurrentMedicinesForm(Client parent, Connection connection) {
        this.parent = parent;
        this.connection = connection;
        initializeUI();
        loadMedicinesIntoLists();
        setUpListSelectionListeners();
    }

    private void initializeUI() {
        setTitle("Aktualne leki");
        setContentPane(mainPanel);
        setSize(1300, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowCloseListener();
        setUpButtonListeners();
        setVisible(true);
    }

    private void addWindowCloseListener() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                parent.setVisible(true);
            }
        });
    }

    private void setUpButtonListeners() {
        quitButton.addActionListener(this);
        deleteButton.addActionListener(this);
        confReceiveButton.addActionListener(this);
    }

    private void setUpListSelectionListeners() {
        setExclusiveSelection(list1, list2, list3);
        setExclusiveSelection(list2, list1, list3);
        setExclusiveSelection(list3, list1, list2);
    }

    private void setExclusiveSelection(JList<Medicine> targetList, JList<Medicine> otherList1, JList<Medicine> otherList2) {
        targetList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !targetList.isSelectionEmpty()) {
                otherList1.clearSelection();
                otherList2.clearSelection();
            }
        });
    }

    private void loadMedicinesIntoLists() {
        ArrayList<Medicine> toTakeList = new ArrayList<>();
        ArrayList<Medicine> receivedList = new ArrayList<>();
        ArrayList<Medicine> inRealisationList = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT `drug_id`, `id`, `transaciont_id`, `drug_name`, `producent`, `price`, `status`, `drug_type` " +
                        "FROM client_and_drug_all_info_fixed WHERE `id` = ?")) {
            preparedStatement.setInt(1, parent.getClientId());
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Medicine medicine = extractMedicine(resultSet);
                categorizeMedicine(medicine, toTakeList, receivedList, inRealisationList);
            }
        } catch (SQLException ex) {
            showError("Błąd przy wczytywaniu leków: " + ex.getMessage());
        }

        populateLists(toTakeList, receivedList, inRealisationList);
    }

    private Medicine extractMedicine(ResultSet resultSet) throws SQLException {
        return new Medicine(
                resultSet.getInt("drug_id"),
                resultSet.getInt("id"),
                resultSet.getInt("transaciont_id"),
                resultSet.getString("drug_name"),
                resultSet.getString("producent"),
                resultSet.getInt("price"),
                Medicine.OrderStatus.valueOf(resultSet.getString("status"))
                , resultSet.getString("drug_type")
        );
    }

    private void categorizeMedicine(Medicine medicine, ArrayList<Medicine> toTakeList, ArrayList<Medicine> receivedList, ArrayList<Medicine> inRealisationList) {
        switch (medicine.getOrderStatus()) {
            case inRealisation -> inRealisationList.add(medicine);
            case taken -> receivedList.add(medicine);
            case readyToTake -> toTakeList.add(medicine);
        }
    }

    private void populateLists(ArrayList<Medicine> toTakeList, ArrayList<Medicine> receivedList, ArrayList<Medicine> inRealisationList) {
        list1.setListData(toTakeList.toArray(new Medicine[0]));
        list2.setListData(receivedList.toArray(new Medicine[0]));
        list3.setListData(inRealisationList.toArray(new Medicine[0]));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == quitButton) {
            closeForm();
        } else if (e.getSource() == deleteButton) {
            deleteSelectedMedicine();
        } else if (e.getSource() == confReceiveButton) {
            confirmMedicineReceived();
        }
    }

    private void closeForm() {
        dispose();
        parent.setVisible(true);
    }

    private void deleteSelectedMedicine() {
        Medicine selectedMedicine = getSelectedMedicine();
        if (selectedMedicine != null) {
            parent.deleteCard(selectedMedicine);
            loadMedicinesIntoLists();
        }
    }

    private void confirmMedicineReceived() {
        Medicine selectedMedicine = list2.getSelectedValue();
        if (selectedMedicine != null) {
            updateMedicineStatus(selectedMedicine.getTransactionId(), "readyToTake");
            loadMedicinesIntoLists();
        }
    }

    private Medicine getSelectedMedicine() {
        if (!list1.isSelectionEmpty()) {
            return list1.getSelectedValue();
        } else if (!list2.isSelectionEmpty()) {
            return list2.getSelectedValue();
        } else if (!list3.isSelectionEmpty()) {
            return list3.getSelectedValue();
        }
        return null;
    }

    private void updateMedicineStatus(int transactionId, String status) {
        String query = "UPDATE drugs_and_clients SET status=? WHERE id=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, status);
            preparedStatement.setInt(2, transactionId);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            showError("Błąd przy aktualizacji: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
