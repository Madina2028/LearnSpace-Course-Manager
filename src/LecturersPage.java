import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import DatabaseManager.Db_connect;

public class LecturersPage extends JPanel {
    private JTable lecturerTable;
    private DefaultTableModel tableModel;
    private JButton insertButton, updateButton, deleteButton;

    public LecturersPage() {
        setLayout(new BorderLayout());

        // Top Panel with Title
        JPanel topPanel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel("Lecturers", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setBorder(new EmptyBorder(20, 0, 20, 0));
        topPanel.add(titleLabel, BorderLayout.NORTH);
        add(topPanel, BorderLayout.NORTH);

        // Table Model
        tableModel = new DefaultTableModel();
        lecturerTable = new JTable(tableModel);
        lecturerTable.setFont(new Font("Arial", Font.PLAIN, 14));
        lecturerTable.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(lecturerTable);
        add(scrollPane, BorderLayout.CENTER);


        // Load Lecturers
        loadLecturers();

        // Buttons
        JPanel buttonPanel = new JPanel();
        insertButton = new JButton("Add Lecturer");
        updateButton = new JButton("Edit Lecturer");
        deleteButton = new JButton("Remove Lecturer");

        buttonPanel.add(insertButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Button Actions
        insertButton.addActionListener(this::insertLecturer);
        updateButton.addActionListener(this::updateLecturer);
        deleteButton.addActionListener(this::deleteLecturer);
    }

    /**
     * Load only lecturers (users with the "Lecturer" role) from the database.
     */
    private void loadLecturers() {
        try (Connection connection = Db_connect.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT user_id, name, email, phone_no, gender FROM Users WHERE role='lecturer'")) {

            // Get column names
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            String[] columnNames = new String[columnCount];

            for (int i = 0; i < columnCount; i++) {
                columnNames[i] = metaData.getColumnName(i + 1);
            }

            tableModel.setColumnIdentifiers(columnNames);
            tableModel.setRowCount(0);

            // Load data into the table
            while (resultSet.next()) {
                Object[] rowData = new Object[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    rowData[i] = resultSet.getObject(i + 1);
                }
                tableModel.addRow(rowData);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading lecturer data.");
            e.printStackTrace();
        }
    }

    /**
     * Insert a new lecturer using a popup form.
     */
    private void insertLecturer(ActionEvent e) {
        JTextField idField = new JTextField();  // User enters ID
        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField genderField = new JTextField();

        Object[] message = {
                "ID:", idField,
                "Name:", nameField,
                "Email:", emailField,
                "Phone No:", phoneField,
                "gender: ", genderField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Enter Lecturer Details", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String idText = idField.getText().trim();
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phoneNo = phoneField.getText().trim();
            String gender = genderField.getText().trim();
            String password = "123456"; // Default password

            if (idText.isEmpty() || name.isEmpty() || email.isEmpty() || phoneNo.isEmpty() || gender.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields must be filled!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                int id = Integer.parseInt(idText);  // Convert ID to integer

                try (Connection connection = Db_connect.getConnection();
                     PreparedStatement statement = connection.prepareStatement(
                             "INSERT INTO Users (user_id, name, email, password_hash, phone_no, gender, role) VALUES (?, ?, ?, ?, ?, ?, 'lecturer')")) {

                    statement.setInt(1, id);
                    statement.setString(2, name);
                    statement.setString(3, email);
                    statement.setString(4, password);
                    statement.setString(5, phoneNo);
                    statement.setString(6, gender);

                    statement.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Lecturer added successfully.");
                    loadLecturers();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "ID must be a number!", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error inserting lecturer: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    /**
     * Update an existing lecturer's details.
     */
    private void updateLecturer(ActionEvent e) {
        int selectedRow = lecturerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a lecturer to update.");
            return;
        }

        int userId = (int) tableModel.getValueAt(selectedRow, 0);
        String currentName = (String) tableModel.getValueAt(selectedRow, 1);
        String currentEmail = (String) tableModel.getValueAt(selectedRow, 2);
        String currentPhone = (String) tableModel.getValueAt(selectedRow, 3);
        String currentGender = (String) tableModel.getValueAt(selectedRow, 4);

        JTextField nameField = new JTextField(currentName);
        JTextField emailField = new JTextField(currentEmail);
        JTextField phoneField = new JTextField(currentPhone);
        JTextField genderField = new JTextField(currentGender);

        Object[] message = {
                "Name:", nameField,
                "Email:", emailField,
                "Phone No:", phoneField,
                "Gender: ", genderField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Update Lecturer Details", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String newName = nameField.getText().trim();
            String newEmail = emailField.getText().trim();
            String newPhone = phoneField.getText().trim();
            String newGender = genderField.getText().trim();

            if (newName.isEmpty() || newEmail.isEmpty() || newPhone.isEmpty() || newGender.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields must be filled!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection connection = Db_connect.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "UPDATE Users SET name=?, email=?, phone_no=?, gender=? WHERE user_id=? AND role='lecturer'")) {

                statement.setString(1, newName);
                statement.setString(2, newEmail);
                statement.setString(3, newPhone);
                statement.setString(4,newGender);
                statement.setInt(5, userId);
                statement.executeUpdate();
                JOptionPane.showMessageDialog(this, "Lecturer updated successfully.");
                loadLecturers();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error updating lecturer.");
                ex.printStackTrace();
            }
        }
    }

    /**
     * Delete a lecturer from the system.
     */
    private void deleteLecturer(ActionEvent e) {
        int selectedRow = lecturerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a lecturer to delete.");
            return;
        }

        int userId = (int) tableModel.getValueAt(selectedRow, 0);
        String lecturerName = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to remove lecturer " + lecturerName + "?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection connection = Db_connect.getConnection();
                 PreparedStatement statement = connection.prepareStatement("DELETE FROM Users WHERE user_id=? AND role='lecturer'")) {

                statement.setInt(1, userId);
                statement.executeUpdate();
                JOptionPane.showMessageDialog(this, "Lecturer removed successfully.");
                loadLecturers();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting lecturer.");
                ex.printStackTrace();
            }
        }
    }
}