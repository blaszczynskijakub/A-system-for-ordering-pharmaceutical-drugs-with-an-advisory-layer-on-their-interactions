package org.example.src.Forms;

import org.example.src.Forms.Client.Medicine;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public interface DataHandler {
    default ArrayList<Medicine> populateComboBox(String dane, PreparedStatement preparedStatement, Connection connection, ResultSet resultSet, int client_id) {
        try {


//            String query = "SELECT drug_name FROM drugs WHERE first_name=? AND last_name=? ";
            String query;
            if(dane.split(" ").length>1 && preparedStatement!=null) {
                 query = "SELECT drug_name, producent_name, price, id FROM drugs WHERE drug_name like ? OR drug_name like ? OR producent_name like ? OR producent_name like ?";

                preparedStatement.setString(3, dane.split(" ")[1]+"%");
                preparedStatement.setString(4, dane.split(" ")[1]+"%");
            }
            else{
                query = "SELECT drug_name, producent_name, price, id FROM drugs WHERE drug_name like ?  OR producent_name like ?";

            }

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, dane.split(" ")[0]+"%");
            preparedStatement.setString(2, dane.split(" ")[0]+"%");

            resultSet = preparedStatement.executeQuery();

            // Populate the comboBox1 with the fetched data
            ArrayList<Medicine> items = new ArrayList<>();
            while (resultSet.next()) {
//                items.add("nazwa: " + resultSet.getString("drug_name") + ", producent: " + resultSet.getString("producent_name")+ " , cena: "+resultSet.getString("price")+" pln"+ " "+resultSet.getString("id"));
                items.add(new Medicine(resultSet.getInt("id"), resultSet.getString("drug_name"),resultSet.getString("producent_name"),resultSet.getInt("price"), Medicine.OrderStatus.inRealisation                ));
            }
            // Add items to comboBox1

            // Close the resources
            resultSet.close();
            preparedStatement.close();
            return items;



        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }

    }

    default ArrayList<String> populateComboBoxWithNames(String dane, PreparedStatement preparedStatement, Connection connection, ResultSet resultSet) {
        try {


//            String query = "SELECT drug_name FROM drugs WHERE first_name=? AND last_name=? ";
            String query;
                query = "SELECT * FROM clients WHERE first_name like ? or last_name like ? or first_name like ? or last_name like ? ";





            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, dane.split(" ")[0]+"%");
            preparedStatement.setString(2, dane.split(" ")[0]+"%");
            preparedStatement.setString(3, dane.split(" ")[1]+"%");
            preparedStatement.setString(4, dane.split(" ")[1]+"%");

            resultSet = preparedStatement.executeQuery();

            // Populate the comboBox1 with the fetched data
            ArrayList<String> items = new ArrayList<>();
            while (resultSet.next()) {
//                items.add("nazwa: " + resultSet.getString("drug_name") + ", producent: " + resultSet.getString("producent_name")+ " , cena: "+resultSet.getString("price")+" pln"+ " "+resultSet.getString("id"));
                items.add("imię i nazwisko: "+resultSet.getString("first_name")+" "+resultSet.getString("last_name")+" "+"adres: "+resultSet.getString("address")+", "+resultSet.getString("city")+" id: "+resultSet.getString("id"));
            }
            // Add items to comboBox1

            // Close the resources
            resultSet.close();
            preparedStatement.close();
            return items;



        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }

    }


    default public void deleteAcc(String dane, Connection connection) {
        try {
            if(dane!=null) {
                String queryFirst = "DELETE FROM transactions WHERE  account_id=?";
                PreparedStatement preparedStatementFirst = connection.prepareStatement(queryFirst);
                preparedStatementFirst.setString(1, ((String) dane.split(" ")[8]));

                preparedStatementFirst.executeUpdate();
                String query = "DELETE FROM drugs WHERE  drug_name=?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, ((String) dane.split(" ")[4]));

                preparedStatement.executeUpdate();


                preparedStatement.close();
                preparedStatementFirst.close();
            }
            else {
                JOptionPane.showMessageDialog(null, "Nie udało się usunąć, ponieważ nic nie wybrano!", "Error", JOptionPane.ERROR_MESSAGE);

            }


        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }


    }
}
