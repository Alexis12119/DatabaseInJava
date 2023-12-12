import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.*;
import java.sql.*;

public class EmployeeLoanAppGUI {
    private static final String JDBC_URL = "jdbc:mariadb://localhost:3306/Activity";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "alexis";

    private JFrame frame;
    private JTable employeeTable;
    private DefaultTableModel tableModel;

    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField middleInitialField;
    private JComboBox<String> positionComboBox;
    private JComboBox<String> ageComboBox;
    private JComboBox<String> salaryRangeComboBox;
    private JComboBox<String> addressComboBox;
    private JComboBox<String> deptCodeComboBox;
    private JTextField searchField;
    private JButton searchButton;

    public EmployeeLoanAppGUI() {
        frame = new JFrame("Employee Loan Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 600);

        // Create the table model with column names
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };
        tableModel.addColumn("Employee ID");
        tableModel.addColumn("Name");
        tableModel.addColumn("Position");
        tableModel.addColumn("Salary");
        tableModel.addColumn("Age");
        tableModel.addColumn("Address");
        tableModel.addColumn("DeptCode");

        // Create the JTable
        employeeTable = new JTable(tableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };
        employeeTable.setPreferredScrollableViewportSize(new Dimension(800, 800));
        JScrollPane scrollPane = new JScrollPane(employeeTable);

        // Set font size for table header
        JTableHeader header = employeeTable.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 14));

        // Set font size for table cells
        employeeTable.setFont(new Font("SansSerif", Font.PLAIN, 12));

        frame.add(scrollPane, BorderLayout.CENTER);
        addMenuButtons(frame.getContentPane());

        // Load initial data into the table
        loadEmployeeData();

        // Center the window
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void showLoanAmountForSelectedEmployee() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Please select an employee to show the loan amount.");
            return;
        }

        int eid = (int) tableModel.getValueAt(selectedRow, 0);

        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "SELECT SUM(LoanAmount) AS TotalLoanAmount FROM Loan WHERE Eid = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, eid);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        int totalLoanAmount = resultSet.getInt("TotalLoanAmount");
                        JOptionPane.showMessageDialog(null, "Total Loan Amount: " + totalLoanAmount);
                    } else {
                        JOptionPane.showMessageDialog(null, "This employee does not have any loans.");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception appropriately
        }
    }

    private void loadEmployeeData() {
        // Retrieve data from the database and populate the table
        tableModel.setRowCount(0); // Clear existing rows
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "SELECT * FROM EmployeeInfo";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        int eid = resultSet.getInt("Eid");
                        Object[] rowData = {
                                eid,
                                resultSet.getString("Name"),
                                resultSet.getString("Position"),
                                resultSet.getString("Salary"),
                                resultSet.getInt("Age"),
                                resultSet.getString("Address"),
                                resultSet.getString("DeptCode"),
                        };
                        tableModel.addRow(rowData);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception appropriately
        }
    }

    private void addMenuButtons(Container container) {
        // Create a panel with FlowLayout for the buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 40));
        JButton addEmployeeBtn = new JButton("Add New Employee");
        JButton updateEmployeeBtn = new JButton("Update Employee");
        JButton deleteEmployeeBtn = new JButton("Delete Employee");
        JButton showLoanButton = new JButton("Show Loan Amount");
        JButton applyNewLoanButton = new JButton("Apply New Loan");
        JButton removeLoanButton = new JButton("Remove Loan");
        JButton getDeptInfoButton = new JButton("Get Department Info"); // Add the new button

        addEmployeeBtn.addActionListener(e -> showAddEmployeeDialog());
        updateEmployeeBtn.addActionListener(e -> showUpdateEmployeeDialog());
        deleteEmployeeBtn.addActionListener(e -> deleteEmployee());
        showLoanButton.addActionListener(e -> showLoanAmountForSelectedEmployee());
        applyNewLoanButton.addActionListener(e -> applyNewLoanForSelectedEmployee());
        removeLoanButton.addActionListener(e -> removeLoanForSelectedEmployee());
        getDeptInfoButton.addActionListener(e -> getDeptInfoForSelectedEmployee()); // Add action listener

        buttonPanel.setLayout(new GridLayout(2, 3)); // Adjust the number of rows and columns as needed

        buttonPanel.add(addEmployeeBtn);
        buttonPanel.add(updateEmployeeBtn);
        buttonPanel.add(deleteEmployeeBtn);
        buttonPanel.add(showLoanButton);
        buttonPanel.add(applyNewLoanButton);
        buttonPanel.add(removeLoanButton);
        buttonPanel.add(getDeptInfoButton);

        // Create a new panel for search at the top
        JPanel searchPanel = new JPanel();
        searchField = new JTextField(20);
        searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchEmployee());

        // Add key listener to the search field
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    searchEmployee();
                }
            }
        });

        searchPanel.add(new JLabel("Search Name:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // Add the search panel at the top
        container.add(searchPanel, BorderLayout.NORTH);

        // Add the button panel at the bottom
        container.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void removeLoanForSelectedEmployee() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Please select an employee to remove the loan.");
            return;
        }

        int eid = (int) tableModel.getValueAt(selectedRow, 0);

        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "DELETE FROM Loan WHERE Eid = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, eid);

                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(null, "Loan removed successfully.");
                    // Refresh the table after removing the loan
                    loadEmployeeData();
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to remove the loan.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception appropriately
        }
    }

    private void getDeptInfoForSelectedEmployee() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Please select an employee to retrieve department info.");
            return;
        }

        String deptCode = (String) tableModel.getValueAt(selectedRow, 6);

        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "SELECT DeptDescription FROM DepartmentInfo WHERE DeptCode = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, deptCode);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        String deptDescription = resultSet.getString("DeptDescription");
                        JOptionPane.showMessageDialog(null, "Department Info: " + deptDescription);
                    } else {
                        JOptionPane.showMessageDialog(null, "Department info not found for selected employee.");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception appropriately
        }
    }

    private void applyNewLoanForSelectedEmployee() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Please select an employee to apply a new loan.");
            return;
        }

        int eid = (int) tableModel.getValueAt(selectedRow, 0);

        int newLoanAmount = Integer.parseInt(JOptionPane.showInputDialog("Enter new loan amount:"));

        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "INSERT INTO Loan (Eid, LoanAmount, Date) VALUES (?, ?, CURRENT_DATE)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, eid);
                preparedStatement.setInt(2, newLoanAmount);

                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(null, "New loan applied successfully.");
                    // Refresh the table after applying a new loan
                    loadEmployeeData();
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to apply a new loan.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception appropriately
        }
    }

    // Add this method to search for employees by name
    private void searchEmployee() {
        String searchTerm = searchField.getText();

        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "SELECT * FROM EmployeeInfo WHERE Name LIKE ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, "%" + searchTerm + "%");

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    // Clear existing rows
                    tableModel.setRowCount(0);

                    while (resultSet.next()) {
                        Object[] rowData = {
                                resultSet.getInt("Eid"),
                                resultSet.getString("Name"),
                                resultSet.getString("Position"),
                                resultSet.getString("Salary"),
                                resultSet.getInt("Age"),
                                resultSet.getString("Address"),
                                resultSet.getString("DeptCode")
                        };
                        tableModel.addRow(rowData);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception appropriately
        }
    }

    private void showUpdateEmployeeDialog() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Please select an employee to update.");
            return;
        }

        Object[] rowData = new Object[tableModel.getColumnCount()];
        for (int i = 0; i < rowData.length; i++) {
            rowData[i] = tableModel.getValueAt(selectedRow, i);
        }

        // Explicitly initialize fields
        firstNameField = new JTextField();
        firstNameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isLetter(c)) {
                    e.consume(); // Ignore non-alphabetic characters
                }
            }
        });
        lastNameField = new JTextField();
        lastNameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isLetter(c)) {
                    e.consume(); // Ignore non-alphabetic characters
                }
            }
        });
        middleInitialField = new JTextField();
        middleInitialField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                // Allow only a single character
                if (middleInitialField.getText().length() >= 1 || !Character.isLetter(c)) {
                    e.consume(); // Ignore additional characters
                }
            }
        });

        positionComboBox = new JComboBox<>(new String[] { "Sales", "Secretary", "Manager", "Developer", "Analyst" });
        ageComboBox = new JComboBox<>(generateAgeRangeArray());
        salaryRangeComboBox = new JComboBox<>(new String[] { "Below 10000", "10000-50000", "Above 50000" });
        addressComboBox = new JComboBox<>(
                new String[] { "Brgy. San Ignacio, San Pablo, Laguna 4000", "Brgy. Dayap, Calauan, Laguna 4012",
                        "Brgy. Muzon,Tay Tay, Rizal 1920" });
        deptCodeComboBox = new JComboBox<>(new String[] { "CRD", "SD", "BPD", "PD", "SCD" });

        String fullName = rowData[1].toString();
        String[] nameParts = fullName.split(" ");

        firstNameField.setText(nameParts.length > 0 ? nameParts[0] : "");
        lastNameField.setText(nameParts.length > 1 ? nameParts[1] : "");
        middleInitialField.setText(nameParts.length > 2 ? nameParts[2] : "");

        positionComboBox.setSelectedItem(rowData[2]);
        ageComboBox.setSelectedItem(rowData[4].toString());
        salaryRangeComboBox.setSelectedItem(rowData[3].toString());
        addressComboBox.setSelectedItem(rowData[5]);
        deptCodeComboBox.setSelectedItem(rowData[6]);

        JPanel panel = new JPanel(new GridLayout(9, 2));
        panel.add(new JLabel("First Name:"));
        panel.add(firstNameField);
        panel.add(new JLabel("Last Name:"));
        panel.add(lastNameField);
        panel.add(new JLabel("Middle Initial:"));
        panel.add(middleInitialField);
        panel.add(new JLabel("Position:"));
        panel.add(positionComboBox);
        panel.add(new JLabel("Age:"));
        panel.add(ageComboBox);
        panel.add(new JLabel("Salary Range:"));
        panel.add(salaryRangeComboBox);
        panel.add(new JLabel("Address:"));
        panel.add(addressComboBox);
        panel.add(new JLabel("DeptCode:"));
        panel.add(deptCodeComboBox);

        int result = JOptionPane.showConfirmDialog(null, panel, "Update Employee",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            updateEmployee(rowData[0].toString()); // Pass the Eid to updateEmployee
        }
    }

    private void updateEmployee(String eid) {
        String fullName = firstNameField.getText() + " " + middleInitialField.getText() +
                ". " + lastNameField.getText();
        String position = (String) positionComboBox.getSelectedItem();
        String age = (String) ageComboBox.getSelectedItem();
        String salaryRange = (String) salaryRangeComboBox.getSelectedItem();
        String address = (String) addressComboBox.getSelectedItem();
        String deptCode = (String) deptCodeComboBox.getSelectedItem();

        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            // Update EmployeeInfo table
            String updateSql = "UPDATE EmployeeInfo SET Name=?, Position=?, Salary=?, Age=?, Address=?, DeptCode=? WHERE Eid=?";
            try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
                updateStatement.setString(1, fullName);
                updateStatement.setString(2, position);
                updateStatement.setString(3, salaryRange);
                updateStatement.setInt(4, Integer.parseInt(age));
                updateStatement.setString(5, address);
                updateStatement.setString(6, deptCode);
                updateStatement.setString(7, eid); // Use the provided Eid

                int rowsAffected = updateStatement.executeUpdate();
                if (rowsAffected > 0) {
                    // Refresh the table after update
                    loadEmployeeData();
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to update the employee.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error occurred: " + e.getMessage());
        }
    }

    private void deleteEmployee() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Please select an employee to delete.");
            return;
        }

        String eid = tableModel.getValueAt(selectedRow, 0).toString();

        int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this employee?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
                // Delete from EmployeeInfo table
                String deleteSql = "DELETE FROM EmployeeInfo WHERE Eid=?";
                try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSql)) {
                    deleteStatement.setString(1, eid);

                    int rowsAffected = deleteStatement.executeUpdate();
                    if (rowsAffected > 0) {
                        // Refresh the table after deletion
                        loadEmployeeData();
                    } else {
                        JOptionPane.showMessageDialog(null, "Failed to delete the employee.");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error occurred: " + e.getMessage());
            }
        }
    }

    private void showAddEmployeeDialog() {
        firstNameField = new JTextField();
        firstNameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isLetter(c)) {
                    e.consume(); // Ignore non-alphabetic characters
                }
            }
        });
        lastNameField = new JTextField();
        lastNameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isLetter(c)) {
                    e.consume(); // Ignore non-alphabetic characters
                }
            }
        });

        middleInitialField = new JTextField();
        middleInitialField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                // Allow only a single character
                if (middleInitialField.getText().length() >= 1 || !Character.isLetter(c)) {
                    e.consume(); // Ignore additional characters
                }
            }
        });

        positionComboBox = new JComboBox<>(new String[] { "Sales", "Secretary", "Manager", "Developer", "Analyst" });
        ageComboBox = new JComboBox<>(generateAgeRangeArray());
        salaryRangeComboBox = new JComboBox<>(new String[] { "Below 10000", "10000-50000", "Above 50000" });
        addressComboBox = new JComboBox<>(
                new String[] { "Brgy. San Ignacio, San Pablo, Laguna 4000", "Brgy. Dayap, Calauan, Laguna 4012",
                        "Brgy. Muzon,Tay Tay, Rizal 1920" });
        deptCodeComboBox = new JComboBox<>(new String[] { "CRD", "SD", "BPD", "PD", "SCD" });

        JPanel panel = new JPanel(new GridLayout(9, 2));
        panel.add(new JLabel("First Name:"));
        panel.add(firstNameField);
        panel.add(new JLabel("Last Name:"));
        panel.add(lastNameField);
        panel.add(new JLabel("Middle Initial:"));
        panel.add(middleInitialField);
        panel.add(new JLabel("Position:"));
        panel.add(positionComboBox);
        panel.add(new JLabel("Age:"));
        panel.add(ageComboBox);
        panel.add(new JLabel("Salary Range:"));
        panel.add(salaryRangeComboBox);
        panel.add(new JLabel("Address:"));
        panel.add(addressComboBox);
        panel.add(new JLabel("DeptCode:"));
        panel.add(deptCodeComboBox);

        int result = JOptionPane.showConfirmDialog(null, panel, "Add New Employee",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            insertEmployee();
        }
    }

    private String[] generateAgeRangeArray() {
        String[] ageRangeArray = new String[43]; // 18 to 60 inclusive
        for (int i = 0; i < 43; i++) {
            ageRangeArray[i] = String.valueOf(18 + i);
        }
        return ageRangeArray;
    }

    private void insertEmployee() {
        String fullName = firstNameField.getText() + " " + middleInitialField.getText() +
                ". " + lastNameField.getText();
        String position = (String) positionComboBox.getSelectedItem();
        String age = (String) ageComboBox.getSelectedItem();
        String salaryRange = (String) salaryRangeComboBox.getSelectedItem();
        String address = (String) addressComboBox.getSelectedItem();
        String deptCode = (String) deptCodeComboBox.getSelectedItem();

        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            // Insert into EmployeeInfo table with auto-incrementing Eid
            String employeeInfoSql = "INSERT INTO EmployeeInfo (Name, Position, Salary, Age, Address, DeptCode) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement employeeInfoStatement = connection
                    .prepareStatement(employeeInfoSql, Statement.RETURN_GENERATED_KEYS)) {
                employeeInfoStatement.setString(1, fullName);
                employeeInfoStatement.setString(2, position);
                employeeInfoStatement.setString(3, salaryRange);
                employeeInfoStatement.setInt(4, Integer.parseInt(age));
                employeeInfoStatement.setString(5, address);
                employeeInfoStatement.setString(6, deptCode);

                int rowsAffected = employeeInfoStatement.executeUpdate();
                if (rowsAffected > 0) {
                    // Refresh the table after insertion
                    loadEmployeeData();
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to add the new employee.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error occurred: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(EmployeeLoanAppGUI::new);
    }
}
