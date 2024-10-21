package org.example.src.Forms.Client;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Medicine {

    public int drug_id;
    public int client_id;
    public int id;

    // Constructor with all fields
    public Medicine(int drug_id, int client_id, int id) {
        this.drug_id = drug_id;
        this.client_id = client_id;
        this.id = id;
    }

    // Constructor with only drug_id (other fields default to 0 or -1)
    public Medicine(int drug_id) {
        this.drug_id = drug_id;
        this.client_id = -1;  // Default value, can be changed as needed
        this.id = -1;         // Default value, can be changed as needed
    }

    // Getters and Setters
    public int getDrugId() {
        return drug_id;
    }

    public void setDrugId(int drug_id) {
        this.drug_id = drug_id;
    }

    public int getClientId() {
        return client_id;
    }

    public void setClientId(int client_id) {
        this.client_id = client_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return drug_id + " " + "yeah";
    }

    // Static method to generate list of medicines
    public static ArrayList<Medicine> generateCards(Connection connection, int client_id) throws SQLException {
        ArrayList<Medicine> availableCards = new ArrayList<Medicine>();
        ResultSet resultSet = null;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT `drug_id`, `client_id`, `id` FROM drugs_and_clients WHERE `client_id`=?"
            );
            preparedStatement.setInt(1, client_id);

            resultSet = preparedStatement.executeQuery();

            // Iterate through the result set and add data to availableCards
            while (resultSet.next()) {
                availableCards.add(new Medicine(resultSet.getInt(1), resultSet.getInt(2), resultSet.getInt(3)));
            }

            // This adds a default card if the result set is empty
            availableCards.add(new Medicine(150, 1, -1));
        } catch (SQLException e) {
            e.printStackTrace(); // Better error logging
        }

        return availableCards;
    }
}
