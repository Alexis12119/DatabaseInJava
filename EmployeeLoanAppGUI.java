import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EmployeeLoanAppGUI {

    private static final String JDBC_URL = "jdbc:mariadb://localhost:3306/Activity";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "alexis";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Employee Loan Application");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 400);

            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(6, 1));
            addMenuButtons(panel);

            frame.getContentPane().add(panel);
            // Center the window
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private static void addMenuButtons(JPanel panel) {
        JButton retrieveEmployeeBtn = new JButton("Retrieve Employee Information");
        JButton calculateLoanBtn = new JButton("Calculate Total Loan Amount");
        JButton retrieveDeptBtn = new JButton("Retrieve Department Information");
        JButton applyLoanBtn = new JButton("Apply New Loan");
        JButton addEmployeeBtn = new JButton("Add New Employee");
        JButton deleteEmployeeBtn = new JButton("Delete Employee");
        JButton updateLoanBtn = new JButton("Update Loan");
        JButton deleteLoanBtn = new JButton("Delete Loan");
        JButton exitBtn = new JButton("Exit");

        retrieveEmployeeBtn.addActionListener(e -> {
            try {
                retrieveEmployeeInformation();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
        calculateLoanBtn.addActionListener(e -> {
            try {
                calculateTotalLoanAmount();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
        retrieveDeptBtn.addActionListener(e -> {
            try {
                retrieveDepartmentInformation();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
        applyLoanBtn.addActionListener(e -> {
            applyNewLoan();
        });
        addEmployeeBtn.addActionListener(e -> {
            try {
                addNewEmployee();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
        updateLoanBtn.addActionListener(e -> {
            try {
                updateLoan();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
        deleteEmployeeBtn.addActionListener(e -> {
            try {
                deleteEmployee();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
        deleteLoanBtn.addActionListener(e -> {
            try {
                deleteLoan();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
        exitBtn.addActionListener(e -> System.exit(0));

        panel.add(retrieveEmployeeBtn);
        panel.add(calculateLoanBtn);
        panel.add(retrieveDeptBtn);
        panel.add(applyLoanBtn);
        panel.add(addEmployeeBtn);
        panel.add(deleteEmployeeBtn);
        panel.add(updateLoanBtn);
        panel.add(deleteLoanBtn);
        panel.add(exitBtn);
    }

    private static void deleteEmployee() throws SQLException {
        String employeeId = JOptionPane.showInputDialog("Enter Employee ID:");

        if (employeeId != null && !employeeId.isEmpty()) {
            try {
                // Check if the eid exists
                if (isEidExists(Integer.parseInt(employeeId))) {
                    try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
                        // Retrieve employee information for display
                        String retrieveEmployeeSql = "SELECT * FROM EmployeeInfo WHERE Eid = ?";
                        try (PreparedStatement retrieveEmployeeStatement = connection
                                .prepareStatement(retrieveEmployeeSql)) {
                            retrieveEmployeeStatement.setInt(1, Integer.parseInt(employeeId));
                            try (ResultSet resultSet = retrieveEmployeeStatement.executeQuery()) {
                                if (resultSet.next()) {
                                    String employeeInfo = String.format(
                                            "Employee ID: %s\nName: %s\nPosition: %s\nSalary: %.2f\nAge: %d\nAddress: %s\nDeptCode: %s",
                                            resultSet.getString("Eid"),
                                            resultSet.getString("Name"),
                                            resultSet.getString("Position"),
                                            resultSet.getDouble("Salary"),
                                            resultSet.getInt("Age"),
                                            resultSet.getString("Address"),
                                            resultSet.getString("DeptCode"));

                                    int confirmation = JOptionPane.showConfirmDialog(
                                            null,
                                            "Are you sure you want to delete this employee?\n\n" + employeeInfo,
                                            "Confirmation",
                                            JOptionPane.YES_NO_OPTION);

                                    if (confirmation == JOptionPane.YES_OPTION) {
                                        // Delete from EmployeeInfo table
                                        String deleteEmployeeSql = "DELETE FROM EmployeeInfo WHERE Eid = ?";
                                        try (PreparedStatement deleteEmployeeStatement = connection
                                                .prepareStatement(deleteEmployeeSql)) {
                                            deleteEmployeeStatement.setInt(1, Integer.parseInt(employeeId));
                                            int rowsAffected = deleteEmployeeStatement.executeUpdate();
                                            if (rowsAffected > 0) {
                                                JOptionPane.showMessageDialog(null, "Employee deleted successfully.");
                                            } else {
                                                JOptionPane.showMessageDialog(null, "Failed to delete the employee.");
                                            }
                                        }
                                    }
                                } else {
                                    JOptionPane.showMessageDialog(null, "Employee not found.");
                                }
                            }
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Employee ID does not exist. Please try again with a valid ID.");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Invalid input format. Please enter a valid numeric value for ID.");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error occurred: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static void updateLoan() throws SQLException {
        String employeeId = JOptionPane.showInputDialog("Enter Employee ID:");
        String newLoanAmount = JOptionPane.showInputDialog("Enter New Loan Amount:");

        if (employeeId != null && !employeeId.isEmpty() &&
                newLoanAmount != null && !newLoanAmount.isEmpty()) {
            try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
                String sql = "UPDATE Loan SET LoanAmount = ? WHERE Eid = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setDouble(1, Double.parseDouble(newLoanAmount));
                    preparedStatement.setInt(2, Integer.parseInt(employeeId));

                    int rowsAffected = preparedStatement.executeUpdate();
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(null, "Loan updated successfully.");
                    } else {
                        JOptionPane.showMessageDialog(null, "Failed to update the loan.");
                    }
                }
            }
        }
    }

    private static void deleteLoan() throws SQLException {
        String employeeId = JOptionPane.showInputDialog("Enter Employee ID:");

        if (employeeId != null && !employeeId.isEmpty()) {
            try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
                String sql = "DELETE FROM Loan WHERE Eid = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setInt(1, Integer.parseInt(employeeId));

                    int rowsAffected = preparedStatement.executeUpdate();
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(null, "Loan deleted successfully.");
                    } else {
                        JOptionPane.showMessageDialog(null, "Failed to delete the loan.");
                    }
                }
            }
        }
    }

    private static void retrieveEmployeeInformation() throws SQLException {
        String employeeId = JOptionPane.showInputDialog("Enter Employee ID:");
        if (employeeId != null && !employeeId.isEmpty()) {
            try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
                String sql = "SELECT * FROM EmployeeInfo WHERE Eid = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setInt(1, Integer.parseInt(employeeId));
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        if (resultSet.next()) {
                            String employeeInfo = String.format(
                                    "<html><table border='1' cellpadding='5'>" +
                                            "<tr><th>Name</th><th>Position</th><th>DeptCode</th></tr>" +
                                            "<tr><td>%s</td><td>%s</td><td>%s</td></tr>" +
                                            "</table></html>",
                                    resultSet.getString("Name"),
                                    resultSet.getString("Position"),
                                    resultSet.getString("DeptCode"));
                            JOptionPane.showMessageDialog(null, employeeInfo);
                        } else {
                            JOptionPane.showMessageDialog(null, "Employee not found.");
                        }
                    }
                }
            }
        }
    }

    private static void calculateTotalLoanAmount() throws SQLException {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "SELECT Eid, SUM(LoanAmount) AS TotalLoanAmount FROM Loan GROUP BY Eid";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    StringBuilder loanInfo = new StringBuilder(
                            "<html><table border='1' cellpadding='5'>" +
                                    "<tr><th>Employee ID</th><th>Total Loan Amount</th></tr>");
                    while (resultSet.next()) {
                        loanInfo.append(String.format("<tr><td>%d</td><td>%.2f</td></tr>",
                                resultSet.getInt("Eid"),
                                resultSet.getDouble("TotalLoanAmount")));
                    }
                    loanInfo.append("</table></html>");
                    JOptionPane.showMessageDialog(null, loanInfo.toString());
                }
            }
        }
    }

    private static void retrieveDepartmentInformation() throws SQLException {
        String employeeId = JOptionPane.showInputDialog("Enter Employee ID:");
        if (employeeId != null && !employeeId.isEmpty()) {
            try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
                String sql = "SELECT DI.DeptDescription FROM DepartmentInfo DI " +
                        "JOIN EmployeeInfo EI ON DI.DeptCode = EI.DeptCode " +
                        "WHERE EI.Eid = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setInt(1, Integer.parseInt(employeeId));
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        StringBuilder departmentInfo = new StringBuilder(
                                "<html><table border='1' cellpadding='5'>" +
                                        "<tr><th>Department Information</th></tr>");
                        if (resultSet.next()) {
                            departmentInfo.append(String.format("<tr><td>%s</td></tr>",
                                    resultSet.getString("DeptDescription")));
                        } else {
                            departmentInfo
                                    .append("<tr><td>No department information available for the employee</td></tr>");
                        }
                        departmentInfo.append("</table></html>");
                        JOptionPane.showMessageDialog(null, departmentInfo.toString());
                    }
                }
            }
        }
    }

    private static void applyNewLoan() {
        String employeeId = JOptionPane.showInputDialog("Enter Employee ID:");

        if (employeeId != null && !employeeId.isEmpty()) {
            try {
                // Check if the eid already exists
                if (isEidExists(Integer.parseInt(employeeId))) {
                    String loanAmount = JOptionPane.showInputDialog("Enter Loan Amount:");

                    if (loanAmount != null && !loanAmount.isEmpty()) {
                        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                            String formattedDate = dateFormat.format(new Date());

                            // Insert into Loan table
                            String loanSql = "INSERT INTO Loan (Eid, LoanAmount, Date) VALUES (?, ?, ?)";
                            try (PreparedStatement loanStatement = connection.prepareStatement(loanSql)) {
                                loanStatement.setInt(1, Integer.parseInt(employeeId));
                                loanStatement.setDouble(2, Double.parseDouble(loanAmount));
                                loanStatement.setString(3, formattedDate);
                                int rowsAffected = loanStatement.executeUpdate();
                                if (rowsAffected > 0) {
                                    JOptionPane.showMessageDialog(null,
                                            "Loan applied successfully.\nDetails:\nEmployee ID: " + employeeId
                                                    + "\nLoan Amount: " + loanAmount);
                                } else {
                                    JOptionPane.showMessageDialog(null, "Failed to apply the loan.");
                                }
                            }
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Please enter a valid Loan Amount.");
                    }
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Employee ID does not exist. Please try again with a valid ID.");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null,
                        "Invalid input format. Please enter valid numeric values for ID and Loan Amount.");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error occurred: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static void addNewEmployee() throws SQLException {
        String employeeId = JOptionPane.showInputDialog("Enter Employee ID:");

        if (employeeId != null && !employeeId.isEmpty()) {
            try {
                // Check if the eid already exists
                if (isEidExists(Integer.parseInt(employeeId))) {
                    JOptionPane.showMessageDialog(null,
                            "Employee ID already exists. Please try again with a different ID.");
                } else {
                    // Collect information about the new employee
                    String name = getInput("Enter Employee Name:");
                    while (!isValidInput(name)) {
                        JOptionPane.showMessageDialog(null, "Please enter a valid Name.");
                        name = getInput("Enter Employee Name:");
                    }

                    String position = getInput("Enter Employee Position:");
                    while (!isValidInput(position)) {
                        JOptionPane.showMessageDialog(null, "Please enter a valid Position.");
                        position = getInput("Enter Employee Position:");
                    }

                    String salary = getInput("Enter Employee Salary:");
                    while (!isValidDouble(salary)) {
                        JOptionPane.showMessageDialog(null, "Please enter a valid Salary.");
                        salary = getInput("Enter Employee Salary:");
                    }

                    String age = getInput("Enter Employee Age:");
                    while (!isValidInteger(age)) {
                        JOptionPane.showMessageDialog(null, "Please enter a valid Age.");
                        age = getInput("Enter Employee Age:");
                    }

                    String address = getInput("Enter Employee Address:");
                    while (!isValidInput(address)) {
                        JOptionPane.showMessageDialog(null, "Please enter a valid Address.");
                        address = getInput("Enter Employee Address:");
                    }

                    String deptCode = getInput("Enter Department Code:");
                    while (!isValidInput(deptCode)) {
                        JOptionPane.showMessageDialog(null, "Please enter a valid Department Code.");
                        deptCode = getInput("Enter Department Code:");
                    }

                    // All inputs are valid, proceed to insertion
                    try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
                        // Insert into EmployeeInfo table
                        String employeeInfoSql = "INSERT INTO EmployeeInfo (Eid, Name, Position, Salary, Age, Address, DeptCode) VALUES (?, ?, ?, ?, ?, ?, ?)";
                        try (PreparedStatement employeeInfoStatement = connection
                                .prepareStatement(employeeInfoSql)) {
                            employeeInfoStatement.setInt(1, Integer.parseInt(employeeId));
                            employeeInfoStatement.setString(2, name);
                            employeeInfoStatement.setString(3, position);
                            employeeInfoStatement.setDouble(4, Double.parseDouble(salary));
                            employeeInfoStatement.setInt(5, Integer.parseInt(age));
                            employeeInfoStatement.setString(6, address);
                            employeeInfoStatement.setString(7, deptCode);
                            employeeInfoStatement.executeUpdate();

                            JOptionPane.showMessageDialog(null, "Employee added successfully.");
                        }
                    }
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null,
                        "Invalid input format. Please enter valid numeric values for ID, Salary, and Age.");
            }
        }
    }

    private static String getInput(String message) {
        return JOptionPane.showInputDialog(message);
    }

    private static boolean isValidDouble(String input) {
        try {
            Double.parseDouble(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isValidInteger(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isValidInput(String input) {
        return input != null && !input.isEmpty();
    }

    // Helper method to check if eid already exists
    private static boolean isEidExists(int eid) throws SQLException {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "SELECT COUNT(*) FROM EmployeeInfo WHERE Eid = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, eid);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        int count = resultSet.getInt(1);
                        return count > 0;
                    }
                }
            }
        }
        return false;
    }

}
