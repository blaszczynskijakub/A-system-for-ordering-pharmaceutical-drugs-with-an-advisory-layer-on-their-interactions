package org.example.src.Forms.Client;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Medicine {

    public enum OrderStatus {
        inRealisation,
        readyToTake,
        taken
    }

    private OrderStatus orderStatus = OrderStatus.inRealisation;

    private final int drugId;
    private final int clientId;
    private int transactionId;
    private final String drugName;
    private final String producent;
    private  String drugType;
    private final int price;

    // Constructor with all fields
    public Medicine(int drugId, int clientId, int transactionId, String drugName, String producent, int price, OrderStatus status, String drugType) {
        this.drugName = drugName;
        this.producent = producent;
        this.price = price;
        this.drugId = drugId;
        this.clientId = clientId;
        this.transactionId = transactionId;
        this.drugType=drugType;

        this.orderStatus = status;
    }

    // Constructor with only drug_id for combobox filling(other fields default to 0 or -1)
    public Medicine(int drugId, String drugName, String producent, int price, OrderStatus status, String drugType) {
        this.drugName = drugName;
        this.producent = producent;
        this.price = price;
        this.drugId = drugId;
        this.clientId = -1;
        this.transactionId = -1;
        this.drugType=drugType;
    }

    public String getDrugName() {
        return drugName;
    }

    public String getProducent() {
        return producent;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public int getDrugId() {
        return drugId;
    }

    public int getClientId() {
        return clientId;
    }

    public String getDrugType() {
        return drugType;
    }

    @Override
    public String toString() {
        return drugName + ", " + producent +", "+ drugType +  ", " + price + " PLN";
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    // Static method to generate list of medicines
    static ArrayList<Medicine> generateCards(Connection connection, int client_id) throws SQLException {
        ArrayList<Medicine> availableCards = new ArrayList<Medicine>();
        ResultSet resultSet = null;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT `drug_id`,`id`,`transaciont_id`, `drug_name`,`producent`,`price`,`status`,`drug_type`  FROM client_and_drug_all_info_fixed WHERE `id` = ?"
            );
            preparedStatement.setInt(1, client_id);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                availableCards.add(new Medicine(Integer.parseInt(resultSet.getString(1)), Integer.parseInt(resultSet.getString(2)), Integer.parseInt(resultSet.getString(3)), resultSet.getString(4), resultSet.getString(5), resultSet.getInt(6), OrderStatus.valueOf(resultSet.getString(7)),resultSet.getString("drug_type")));
            }
            //avoiding to be null
            availableCards.add(new Medicine(150, 1, -1, "d", "d", -1, OrderStatus.inRealisation, "pass"));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return availableCards;
    }
}
