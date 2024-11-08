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

    /**
     * Checks for potential interactions between the selected medicine and any medicines the client has already ordered.
     * If an interaction is detected, provides options to continue, cancel, or find an alternative medicine.
     *
     * @param clientId         The ID of the client.
     * @param selectedMedicine The medicine that is being checked.
     * @return True if no interaction or if the user chooses to proceed despite the interaction, otherwise false.
     */
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
                        new Object[]{"Yes", "No"},
                        "Yes"
                );
                if (option == JOptionPane.NO_OPTION) {
                    return false; // Stop if the user chooses not to proceed
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
                        new Object[]{"Yes", "No"},
                        "Yes"
                );
                if (option == JOptionPane.NO_OPTION) {
                    return false; // Stop if the user chooses not to proceed
                }
            }




            // Retrieve all drugs the client has already ordered
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT * FROM drugs WHERE id IN (SELECT drug_id FROM drugs_and_clients WHERE client_id = ?)"
            );
            preparedStatement.setInt(1, clientId);
            ResultSet resultSet = preparedStatement.executeQuery();

            // Retrieve attributes of the selected medicine
            Boolean[] selectedAttributes = getAttributesForMedicine(selectedMedicine);

            // Compare selected medicine with all previously ordered drugs
            while (resultSet.next()) {
                int existingDrugId = resultSet.getInt("id");

                // Skip the interaction check if the selected medicine is the same as the existing drug
                if (existingDrugId == selectedMedicine.getDrugId()) {
                    continue; // Skip further checks if the same medication
                }

                // Get attributes of the existing drug
                Boolean[] existingAttributes = new Boolean[]{
                        getNullableBoolean(resultSet, "acidity"),
                        getNullableBoolean(resultSet, "kolestypol"),
                        getNullableBoolean(resultSet, "digestion"),
                        getNullableBoolean(resultSet, "high_affinity"),
                        getNullableBoolean(resultSet, "opiodis"),
                        getNullableBoolean(resultSet, "carbon"),
                        getNullableBoolean(resultSet, "alcohol")
                };

                // Check if all attributes are the same
                boolean attributesMatch = true;
                for (int i = 0; i < selectedAttributes.length; i++) {
                    if (selectedAttributes[i] != null && !selectedAttributes[i].equals(existingAttributes[i]) ||
                            selectedAttributes[i] == null && existingAttributes[i] != null) {
                        attributesMatch = false;
                        break;
                    }
                }

                // If attributes match, skip the interaction check
                if (attributesMatch) {
                    continue;
                }

                // Proceed with interaction check only if attributes differ
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
                            new Object[]{"Yes", "No", "Find Something Else"},
                            "Yes"
                    );

                    if (option == JOptionPane.YES_OPTION) {
                        return true; // Proceed despite the interaction
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
                            return false; // Return false to indicate alternative was found and interaction avoided
                        } else {
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Brak dostępnego zamiennika bez interakcji.",
                                    "Brak zamiennika",
                                    JOptionPane.INFORMATION_MESSAGE
                            );
                        }
                    }
                    return false; // Cancel if user chose "No" or if alternative was found
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // No interaction or interaction accepted
        return true;
    }

    /**
     * Finds an alternative medicine with the same drug type that does not cause interaction with the selected medicine.
     *
     * @param selectedMedicine The medicine to find alternatives for.
     * @return An alternative medicine if found, otherwise null.
     * @throws SQLException if a database access error occurs.
     */
    private Medicine findAlternativeMedicine(Medicine selectedMedicine, ResultSet resultSetProblem) throws SQLException {
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

            // Check if this alternative has no interaction with the selected medicine
            if (!hasInteraction(resultSet, resultSetProblem)) {
                return alternative;
            }
        }
        return null; // No alternative found
    }

    /**
     * Determines if there is an interaction between an existing drug in the database and the selected medicine.
     *
     * @param resultSet       The ResultSet containing the existing drug's data.
     * @param selectedMedicine The medicine being checked for interaction.
     * @return True if an interaction exists, otherwise false.
     * @throws SQLException if a database access error occurs.
     */
    private boolean hasInteraction(ResultSet resultSet, Medicine selectedMedicine) throws SQLException {
        Boolean acidity = getNullableBoolean(resultSet, "acidity");
        Boolean kolestypol = getNullableBoolean(resultSet, "kolestypol");
        Boolean digestion = getNullableBoolean(resultSet, "digestion");
        Boolean highAffinity = getNullableBoolean(resultSet, "high_affinity");
        Boolean opiodis = getNullableBoolean(resultSet, "opiodis");
        Boolean carbon = getNullableBoolean(resultSet, "carbon");
        Boolean alcohol = getNullableBoolean(resultSet, "alcohol");

        Boolean[] selectedAttributes = getAttributesForMedicine(selectedMedicine);

        // Check for each specific interaction rule, ensuring both values are non-null

        // 1. Colestipol with acidity=1
        if (kolestypol != null && selectedAttributes[1] != null &&
                acidity != null && selectedAttributes[0] != null) {
            if (kolestypol && selectedAttributes[0]) {
                return true; // Interaction detected due to colestipol binding to acidic drugs
            }
        }

        // 2. Digestion impact
        if (digestion != null && selectedAttributes[2] != null) {
            if ((digestion && !selectedAttributes[2]) || (!digestion && selectedAttributes[2])) {
                return true; // Conflict in digestion effects
            }
        }

        // 3. High affinity interaction (high-affinity drugs can dominate low-affinity drugs)
        if (highAffinity != null && selectedAttributes[3] != null) {
            if ((highAffinity && !selectedAttributes[3]) || (selectedAttributes[3] && !highAffinity)) {
                return true; // Strong medication dominating weak medication
            }
        }

        // 4. Opioid interaction (avoid two opioids)
        if (opiodis != null && selectedAttributes[4] != null) {
            if (opiodis && selectedAttributes[4]) {
                return true; // Interaction between two opioids
            }
        }

        // 6. Alcohol interaction (affects driving and interacts with opioids, antihistamines)
        if (alcohol != null && selectedAttributes[6] != null && alcohol) {
            if ((selectedAttributes[4] != null && selectedAttributes[4]) || (selectedAttributes[6] != null && selectedAttributes[6])) {
                return true; // Alcohol interaction detected
            }
        }

        return false; // No interaction detected
    }

    private boolean hasInteraction(ResultSet resultSet, ResultSet problemDrug) throws SQLException {
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

        // Check for each specific interaction rule, ensuring both values are non-null

        // 1. Colestipol with acidity=1
        if (kolestypol != null && kolestypolProblem != null &&
                acidity != null && acidityProblem != null) {
            if ((kolestypol && acidityProblem) || (kolestypolProblem && acidity)) {
                return true; // Interaction detected due to colestipol binding to acidic drugs
            }
        }

        // 2. Digestion impact
        if (digestion != null && digestionProblem != null) {
            if ((digestion && !digestionProblem || (!digestion && digestionProblem))) {
                return true; // Conflict in digestion effects
            }
        }

        // 3. High affinity interaction (high-affinity drugs can dominate low-affinity drugs)
        if (highAffinity != null && highAffinityProblem != null) {
            if ((highAffinity && !highAffinityProblem) || (highAffinityProblem && !highAffinity)) {
                return true; // Strong medication dominating weak medication
            }
        }

        // 4. Opioid interaction (avoid two opioids)
        if (opiodis != null && opiodisProblem != null) {
            if (opiodis && opiodisProblem) {
                return true; // Interaction between two opioids
            }
        }

        // 6. Alcohol interaction (affects driving and interacts with opioids, antihistamines)
        if (alcohol != null && alcoholProblem != null && alcohol) {
            if ((opiodisProblem != null && opiodisProblem) || (alcoholProblem != null && alcoholProblem)) {
                return true; // Alcohol interaction detected
            }
        }

        return false; // No interaction detected
    }

    /**
     * Retrieves the attributes for a given medicine from the database.
     *
     * @param medicine The medicine to retrieve attributes for.
     * @return An array of Boolean values representing the medicine's attributes.
     * @throws SQLException if a database access error occurs.
     */
    private Boolean[] getAttributesForMedicine(Medicine medicine) throws SQLException {
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
        return new Boolean[7]; // Default values if not found
    }

    /**
     * Helper method to retrieve nullable Boolean values from a ResultSet.
     *
     * @param resultSet   The ResultSet containing the data.
     * @param columnName  The column name of the boolean attribute.
     * @return A Boolean value, or null if the value was SQL NULL.
     * @throws SQLException if a database access error occurs.
     */
    private Boolean getNullableBoolean(ResultSet resultSet, String columnName) throws SQLException {
        boolean value = resultSet.getBoolean(columnName);
        return resultSet.wasNull() ? null : value;
    }

    /**
     * Retrieves a single attribute for a given medicine from the database.
     *
     * @param medicine    The medicine to retrieve attributes for.
     * @param columnName  The attribute column name to retrieve.
     * @return The Boolean value for the attribute, or null if not set.
     * @throws SQLException if a database access error occurs.
     */
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
