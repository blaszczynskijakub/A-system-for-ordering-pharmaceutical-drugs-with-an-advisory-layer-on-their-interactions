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

    public int drug_id;
    public int client_id;
    public int id;
    public String drug_name, producent;
    public int price;

    // Constructor with all fields
    public Medicine(int drug_id, int client_id, int id, String drug_name, String producent, int price, OrderStatus status) {
        this.drug_name = drug_name;
        this.producent = producent;
        this.price = price;
        this.drug_id = drug_id;
        this.client_id = client_id;
        this.id = id;

        this.orderStatus = status;
    }

    // Constructor with only drug_id (other fields default to 0 or -1)
    public Medicine(int drug_id, String drug_name, String producent, int price, OrderStatus status) {
        this.drug_name = drug_name;
        this.producent = producent;
        this.price = price;
        this.drug_id = drug_id;
        this.client_id = -1;  // Default value, can be changed as needed
        this.id = -1;         // Default value, can be changed as needed
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return drug_name + ", " + producent + ", " + price + " PLN";
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    // Static method to generate list of medicines
    public static ArrayList<Medicine> generateCards(Connection connection, int client_id) throws SQLException {
        ArrayList<Medicine> availableCards = new ArrayList<Medicine>();
        ResultSet resultSet = null;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT `drug_id`,`id`,`transaciont_id`, `drug_name`,`producent`,`price`,`status`  FROM client_and_drug_all_info_fixed WHERE `id` = ?"
            );
            preparedStatement.setInt(1, client_id);

            resultSet = preparedStatement.executeQuery();

            // Iterate through the result set and add data to availableCards
            while (resultSet.next()) {
                availableCards.add(new Medicine(Integer.parseInt(resultSet.getString(1)), Integer.parseInt(resultSet.getString(2)), Integer.parseInt(resultSet.getString(3)), resultSet.getString(4), resultSet.getString(5), resultSet.getInt(6), OrderStatus.valueOf(resultSet.getString(7))));
            }

            // This adds a default card if the result set is empty
            availableCards.add(new Medicine(150, 1, -1, "d", "d", -1, OrderStatus.inRealisation));
        } catch (SQLException e) {
            e.printStackTrace(); // Better error logging
        }

        return availableCards;
    }
}
