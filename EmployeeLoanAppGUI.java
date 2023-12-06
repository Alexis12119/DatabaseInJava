import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class EmployeeLoanAppGUI {

    private static final String JDBC_URL = "jdbc:mariadb://localhost:3306/Activity";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "alexis";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Employee Loan Application");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 300);

            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(5, 1));
            addMenuButtons(panel);

            frame.getContentPane().add(panel);
            frame.setVisible(true);
        });
    }

    private static void addMenuButtons(JPanel panel) {
        JButton retrieveEmployeeBtn = new JButton("Retrieve Employee Information");
        JButton calculateLoanBtn = new JButton("Calculate Total Loan Amount");
        JButton retrieveDeptBtn = new JButton("Retrieve Department Information");
        JButton applyLoanBtn = new JButton("Apply New Loan");
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
            try {
                applyNewLoan();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
        exitBtn.addActionListener(e -> System.exit(0));

        panel.add(retrieveEmployeeBtn);
        panel.add(calculateLoanBtn);
        panel.add(retrieveDeptBtn);
        panel.add(applyLoanBtn);
        panel.add(exitBtn);
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
                                    resultSet.getString("DeptCode")
                            );
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
                            departmentInfo.append("<tr><td>No department information available for the employee</td></tr>");
                        }
                        departmentInfo.append("</table></html>");
                        JOptionPane.showMessageDialog(null, departmentInfo.toString());
                    }
                }
            }
        }
    }

    private static void applyNewLoan() throws SQLException {
        String employeeId = JOptionPane.showInputDialog("Enter Employee ID:");
        String loanAmount = JOptionPane.showInputDialog("Enter Loan Amount:");
        String loanDescription = JOptionPane.showInputDialog("Enter Loan Description:");

        if (employeeId != null && !employeeId.isEmpty() &&
                loanAmount != null && !loanAmount.isEmpty() &&
                loanDescription != null && !loanDescription.isEmpty()) {
            try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
                String sql = "INSERT INTO Loan (Eid, LoanAmount, Description, Date) VALUES (?, ?, ?, NOW())";
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setInt(1, Integer.parseInt(employeeId));
                    preparedStatement.setDouble(2, Double.parseDouble(loanAmount));
                    preparedStatement.setString(3, loanDescription);

                    int rowsAffected = preparedStatement.executeUpdate();
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(null, "Loan applied successfully.");
                    } else {
                        JOptionPane.showMessageDialog(null, "Failed to apply the loan.");
                    }
                }
            }
        }
    }
}
