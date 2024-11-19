package org.example.src.Forms.Client;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdvisoryLayer {

    private final Connection connection;

    public AdvisoryLayer(Connection connection) {
        this.connection = connection;
    }


    public boolean checkForInteractions(int clientId, Medicine selectedMedicine) {
        try {
            // Warning 1: alco
            Boolean containsAlcohol = getAttributeForMedicine(selectedMedicine, "alcohol");
            if (containsAlcohol != null && containsAlcohol) {
                int option = JOptionPane.showOptionDialog(
                        null,
                        "Wybrany lek zawiera alkohol, unikaj obsługi  maszyn/pojazdów, czy na pewno chcesz kontynuować?",
                        "Ostrzeżenie o zawartości alkoholu",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null,
                        new Object[]{"Tak", "Nie"},
                        "Tak"
                );
                if (option == JOptionPane.NO_OPTION) {
                    return false;
                }
            }

            Boolean needsCover = getAttributeForMedicine(selectedMedicine, "need_cover");

            PreparedStatement preparedStatementCover = connection.prepareStatement(
                    "SELECT * FROM drugs WHERE id IN (SELECT drug_id FROM drugs_and_clients WHERE client_id = ?) AND drug_type='cover'"


            );
            preparedStatementCover.setInt(1, clientId);
            ResultSet resultSetCover = preparedStatementCover.executeQuery();
            if (needsCover != null && needsCover && !resultSetCover.isBeforeFirst()) {
                int option = JOptionPane.showOptionDialog(
                        null,
                        "Wybrany lek wymaga osłony, możesz zamówić ją zamówić wyszukując kateogrii 'osłona', czy na pewno chcesz kontynuować?",
                        "Ostrzeżenie o konieczności zażycia osłony",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null,
                        new Object[]{"Tak", "Nie"},
                        "Tak"
                );
                if (option == JOptionPane.NO_OPTION) {
                    return false;
                }
            }




            // get all drugs the client already ordered
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT * FROM drugs WHERE id IN (SELECT drug_id FROM drugs_and_clients WHERE client_id = ?)"
            );
            preparedStatement.setInt(1, clientId);
            ResultSet resultSet = preparedStatement.executeQuery();

            Boolean[] selectedAttributes = getAttributesForMedicine(selectedMedicine);

            while (resultSet.next()) {
                int existingDrugId = resultSet.getInt("id");

                if (existingDrugId == selectedMedicine.getDrugId()) {
                    continue;
                }

                Boolean[] existingAttributes = new Boolean[]{
                        getNullableBoolean(resultSet, "acidity"),
                        getNullableBoolean(resultSet, "kolestypol"),
                        getNullableBoolean(resultSet, "digestion"),
                        getNullableBoolean(resultSet, "high_affinity"),
                        getNullableBoolean(resultSet, "opiodis"),
                        getNullableBoolean(resultSet, "carbon"),
                        getNullableBoolean(resultSet, "alcohol")
                };

                // check if all attributes are the same
                boolean attributesMatch = true;
                for (int i = 0; i < selectedAttributes.length; i++) {
                    if (selectedAttributes[i] != null && !selectedAttributes[i].equals(existingAttributes[i]) ||
                            selectedAttributes[i] == null && existingAttributes[i] != null) {
                        attributesMatch = false;
                        break;
                    }
                }

                if (attributesMatch) {
                    continue;
                }

                if (hasInteraction(resultSet, selectedMedicine)) {
                    int option = JOptionPane.showOptionDialog(
                            null,
                            "Wybrany lek może wchodzić w interakcję z lekiem: " +
                                    resultSet.getString("drug_name") + ", " +
                                    resultSet.getString("producent_name") +
                                    ". Czy chcesz kontynuować?",
                            "Wykryto potencjalną interakcję",
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.WARNING_MESSAGE,
                            null,
                            new Object[]{"Tak", "Nie", "Znajdź zamiennik"},
                            "Tak"
                    );

                    if (option == JOptionPane.YES_OPTION) {
                        return true;
                    } else if (option == JOptionPane.CANCEL_OPTION) {
                        Medicine alternative = findAlternativeMedicine(selectedMedicine, resultSet);
                        if (alternative != null) {
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Proponowany zamiennik: " + alternative.getDrugName() + ", " +
                                            alternative.getProducent(),
                                    "Znaleziono zamiennik",
                                    JOptionPane.INFORMATION_MESSAGE
                            );
                            return false;
                        } else {
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Brak dostępnego zamiennika bez interakcji.",
                                    "Brak zamiennika",
                                    JOptionPane.INFORMATION_MESSAGE
                            );
                        }
                    }
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }


    public Medicine findAlternativeMedicine(Medicine selectedMedicine, ResultSet resultSetProblem) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(
                "SELECT * FROM drugs WHERE drug_type = ? AND id != ?"
        );
        stmt.setString(1, selectedMedicine.getDrugType());
        stmt.setInt(2, selectedMedicine.getDrugId());
        ResultSet resultSet = stmt.executeQuery();
        while (resultSet.next()) {
            Medicine alternative = new Medicine(
                    resultSet.getInt("id"),
                    resultSet.getString("drug_name"),
                    resultSet.getString("producent_name"),
                    resultSet.getInt("price"),
                    Medicine.OrderStatus.inRealisation,
                    resultSet.getString("drug_type")
            );

            if (!hasInteraction(resultSet, resultSetProblem)) {
                return alternative;
            }
        }
        return null;
    }


    private boolean hasInteraction(ResultSet resultSet, Medicine selectedMedicine) throws SQLException {
        Boolean acidity = getNullableBoolean(resultSet, "acidity");
        Boolean kolestypol = getNullableBoolean(resultSet, "kolestypol");
        Boolean digestion = getNullableBoolean(resultSet, "digestion");
        Boolean highAffinity = getNullableBoolean(resultSet, "high_affinity");
        Boolean opiodis = getNullableBoolean(resultSet, "opiodis");
        Boolean carbon = getNullableBoolean(resultSet, "carbon");
        Boolean alcohol = getNullableBoolean(resultSet, "alcohol");

        Boolean[] selectedAttributes = getAttributesForMedicine(selectedMedicine);


        // 1. colestipol=1 with acidity=1
        if (kolestypol != null && selectedAttributes[1] != null &&
                acidity != null && selectedAttributes[0] != null) {
            if ((kolestypol && selectedAttributes[0]) || (selectedAttributes[1] && acidity)) {
                return true;
            }
        }

        // 2. digestion overdoze
        if (digestion != null && selectedAttributes[2] != null) {
            if ((digestion && selectedAttributes[2]) || (!digestion && !selectedAttributes[2])) {
                return true;
            }
        }



        // 3. high affinity with low aff
        if (highAffinity != null && selectedAttributes[3] != null) {
            if ((highAffinity && !selectedAttributes[3]) || (selectedAttributes[3] && !highAffinity)) {
                return true;
            }
        }

        // 4. opioids overdoze
        if (opiodis != null && selectedAttributes[4] != null) {
            if (opiodis && selectedAttributes[4]) {
                return true;
            }
        }

        // 5.alcohol: driving and opioids
        if (alcohol != null && selectedAttributes[6] != null && selectedAttributes[4] != null && opiodis!=null ) {
            if ((alcohol && selectedAttributes[4]) || (selectedAttributes[6] && opiodis)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasInteraction(ResultSet resultSet, ResultSet problemDrug) throws SQLException {
        Boolean acidity = getNullableBoolean(resultSet, "acidity");
        Boolean kolestypol = getNullableBoolean(resultSet, "kolestypol");
        Boolean digestion = getNullableBoolean(resultSet, "digestion");
        Boolean highAffinity = getNullableBoolean(resultSet, "high_affinity");
        Boolean opiodis = getNullableBoolean(resultSet, "opiodis");
        Boolean carbon = getNullableBoolean(resultSet, "carbon");
        Boolean alcohol = getNullableBoolean(resultSet, "alcohol");

        Boolean acidityProblem = getNullableBoolean(problemDrug, "acidity");
        Boolean kolestypolProblem = getNullableBoolean(problemDrug, "kolestypol");
        Boolean digestionProblem = getNullableBoolean(problemDrug, "digestion");
        Boolean highAffinityProblem = getNullableBoolean(problemDrug, "high_affinity");
        Boolean opiodisProblem = getNullableBoolean(problemDrug, "opiodis");
        Boolean carbonProblem = getNullableBoolean(problemDrug, "carbon");
        Boolean alcoholProblem = getNullableBoolean(problemDrug, "alcohol");


        if (kolestypol != null && kolestypolProblem != null &&
                acidity != null && acidityProblem != null) {
            if ((kolestypol && acidityProblem) || (kolestypolProblem && acidity)) {
                return true;
            }
        }

        if (digestion != null && digestionProblem != null) {
            if ((digestion && digestionProblem ) || (!digestion && !digestionProblem )) {
                return true;
            }
        }

        if (highAffinity != null && highAffinityProblem != null) {
            if ((highAffinity && !highAffinityProblem) || (highAffinityProblem && !highAffinity)) {
                return true;
            }
        }

        if (opiodis != null && opiodisProblem != null) {
            if (opiodis && opiodisProblem) {
                return true;
            }
        }

        if (alcohol != null && alcoholProblem!= null && opiodisProblem != null && opiodis!=null) {
            if ((alcohol && opiodisProblem) || (alcoholProblem && opiodis)) {
                return true;
            }
        }

        return false;
    }


    public Boolean[] getAttributesForMedicine(Medicine medicine) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT acidity, kolestypol, digestion, high_affinity, opiodis, carbon, alcohol FROM drugs WHERE id = ?"
        );
        preparedStatement.setInt(1, medicine.getDrugId());
        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            return new Boolean[]{
                    getNullableBoolean(resultSet, "acidity"),
                    getNullableBoolean(resultSet, "kolestypol"),
                    getNullableBoolean(resultSet, "digestion"),
                    getNullableBoolean(resultSet, "high_affinity"),
                    getNullableBoolean(resultSet, "opiodis"),
                    getNullableBoolean(resultSet, "carbon"),
                    getNullableBoolean(resultSet, "alcohol")
            };
        }
        return new Boolean[7];
    }


    private Boolean getNullableBoolean(ResultSet resultSet, String columnName) throws SQLException {
        boolean value = resultSet.getBoolean(columnName);
        return resultSet.wasNull() ? null : value;
    }


    private Boolean getAttributeForMedicine(Medicine medicine, String columnName) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT " + columnName + " FROM drugs WHERE id = ?"
        );
        preparedStatement.setInt(1, medicine.getDrugId());
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            return getNullableBoolean(resultSet, columnName);
        }
        return null;
    }
}
