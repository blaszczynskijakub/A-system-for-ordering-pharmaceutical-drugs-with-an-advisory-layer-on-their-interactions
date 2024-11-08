package org.example.src.Forms;

import org.example.src.Forms.Client.Medicine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public interface DataHandler {
    default ArrayList<Medicine> populateComboBox(String dane, PreparedStatement preparedStatement, Connection connection, ResultSet resultSet, int client_id) {
        try {
            String query;
            if (dane.split(" ").length > 1 && preparedStatement != null) {
                query = "SELECT drug_name, producent_name, price, id FROM drugs WHERE drug_name like ? OR drug_name like ? OR producent_name like ? OR producent_name like ?";
                preparedStatement.setString(3, dane.split(" ")[1] + "%");
                preparedStatement.setString(4, dane.split(" ")[1] + "%");
            } else {
                query = "SELECT drug_name, producent_name, price, id, drug_type FROM drugs WHERE drug_name like ? OR producent_name like ?";
            }

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, dane.split(" ")[0] + "%");
            preparedStatement.setString(2, dane.split(" ")[0] + "%");

            resultSet = preparedStatement.executeQuery();

            ArrayList<Medicine> items = new ArrayList<>();
            while (resultSet.next()) {
                items.add(new Medicine(
                        resultSet.getInt("id"),
                        resultSet.getString("drug_name"),
                        resultSet.getString("producent_name"),
                        resultSet.getInt("price"),
                        Medicine.OrderStatus.inRealisation
                        , resultSet.getString("drug_type")
                ));
            }

            resultSet.close();
            preparedStatement.close();
            return items;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    default ArrayList<String> populateComboBoxWithNames(String dane, PreparedStatement preparedStatement, Connection connection, ResultSet resultSet) {
        try {
            String query = "SELECT * FROM clients WHERE first_name like ? or last_name like ? or first_name like ? or last_name like ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, dane.split(" ")[0] + "%");
            preparedStatement.setString(2, dane.split(" ")[0] + "%");
            preparedStatement.setString(3, dane.split(" ")[1] + "%");
            preparedStatement.setString(4, dane.split(" ")[1] + "%");

            resultSet = preparedStatement.executeQuery();

            ArrayList<String> items = new ArrayList<>();
            while (resultSet.next()) {
                items.add("imię i nazwisko: " + resultSet.getString("first_name") + " " + resultSet.getString("last_name") + " adres: " + resultSet.getString("address") + ", " + resultSet.getString("city") + " id: " + resultSet.getString("id"));
            }

            resultSet.close();
            preparedStatement.close();
            return items;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }


}
