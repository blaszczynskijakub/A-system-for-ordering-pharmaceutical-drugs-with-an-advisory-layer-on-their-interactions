package org.example.src.Forms.Client;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

public record CreditCard(String cardNumber, java.sql.Date expiryDate, String producerName) {
    private static final String[] producers = {"Visa", "MasterCard", "Discover", "American Express"};

    @Override
    public String toString() {
        return cardNumber + " " + expiryDate.toString() + " " + producerName;
    }

    public static ArrayList<CreditCard> generateCards(Connection connection){
        ArrayList<CreditCard> availableCards = new ArrayList<CreditCard>();
        ResultSet resultSet = null;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT `Numer karty` FROM cards_view");
            resultSet = preparedStatement.executeQuery();
        } catch (SQLException e) {
            System.out.println(e);
        }

        for(int i =0; i < producers.length; i++) {
            boolean duplicate = false;
            int leftLimit = 48;
            int rightLimit = 57;
            int targetStringLength = 16;
            Random random = new Random();

            String generatedString = random.ints(leftLimit, rightLimit + 1)
                    .limit(targetStringLength)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();
            try {
                while (resultSet.next()) {
                    if (resultSet.getString(1).equals(generatedString)){
                        i--;
                        duplicate = true;
                        break;
                    }
                }
            }catch (SQLException e){
                System.out.println(e);
            }

            if(!duplicate){
                java.util.Date date = new java.util.Date();
                Calendar c = Calendar.getInstance();
                c.setTime(date);
                c.add(Calendar.YEAR, 5);
                date = c.getTime();

                java.sql.Date sqlDate = new java.sql.Date(date.getTime());

                availableCards.add(new CreditCard(generatedString, sqlDate, producers[i]));
            }
        }

        return availableCards;
    }
}
