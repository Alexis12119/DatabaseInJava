import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class EmployeeLoanApp {

    private static final String JDBC_URL = "jdbc:mariadb://localhost:3306/Activity";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "alexis";

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("1. Retrieve Employee Information");
                System.out.println("2. Calculate Total Loan Amount for Each Employee");
                System.out.println("3. Retrieve Department Information for a Given Employee");
                System.out.println("4. Apply New Loan");
                System.out.println("5. Exit");
                System.out.print("Enter your choice: ");

                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume the newline character

                switch (choice) {
                    case 1:
                        retrieveEmployeeInformation(scanner);
                        break;
                    case 2:
                        calculateTotalLoanAmount(scanner);
                        break;
                    case 3:
                        retrieveDepartmentInformation(scanner);
                        break;
                    case 4:
                        applyNewLoan(scanner);
                        break;
                    case 5:
                        System.out.println("Exiting the application.");
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void retrieveEmployeeInformation(Scanner scanner) throws SQLException {
        System.out.print("Enter Employee ID: ");
        int employeeId = scanner.nextInt();
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "SELECT * FROM EmployeeInfo WHERE Eid = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, employeeId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        System.out.println("Employee Information:");
                        System.out.println("Name: " + resultSet.getString("Name"));
                        System.out.println("Position: " + resultSet.getString("Position"));
                        System.out.println("DeptCode: " + resultSet.getString("DeptCode"));
                        // Add more fields as needed
                    } else {
                        System.out.println("Employee not found.");
                    }
                }
            }
        }
    }

    private static void calculateTotalLoanAmount(Scanner scanner) throws SQLException {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "SELECT Eid, SUM(LoanAmount) AS TotalLoanAmount FROM Loan GROUP BY Eid";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        System.out.println("Employee ID: " + resultSet.getInt("Eid"));
                        System.out.println("Total Loan Amount: " + resultSet.getDouble("TotalLoanAmount"));
                        System.out.println();
                    }
                }
            }
        }
    }

    private static void retrieveDepartmentInformation(Scanner scanner) throws SQLException {
        System.out.print("Enter Employee ID: ");
        int employeeId = scanner.nextInt();
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "SELECT DI.DeptDescription FROM DepartmentInfo DI " +
                    "JOIN EmployeeInfo EI ON DI.DeptCode = EI.DeptCode " +
                    "WHERE EI.Eid = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, employeeId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        System.out.println("Department Information:");
                        System.out.println("DeptDescription: " + resultSet.getString("DeptDescription"));
                    } else {
                        System.out.println("Employee not found or no department information available.");
                    }
                }
            }
        }
    }

    private static void applyNewLoan(Scanner scanner) throws SQLException {
        System.out.print("Enter Employee ID: ");
        int employeeId = scanner.nextInt();
        System.out.print("Enter Loan Amount: ");
        double loanAmount = scanner.nextDouble();
        scanner.nextLine(); // Consume the newline character
        System.out.print("Enter Loan Description: ");
        String loanDescription = scanner.nextLine();

        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            // Assuming a Date field for loan date
            String sql = "INSERT INTO Loan (Eid, LoanAmount, Description, Date) VALUES (?, ?, ?, NOW())";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, employeeId);
                preparedStatement.setDouble(2, loanAmount);
                preparedStatement.setString(3, loanDescription);

                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Loan applied successfully.");
                } else {
                    System.out.println("Failed to apply the loan.");
                }
            }
        }
    }
}
