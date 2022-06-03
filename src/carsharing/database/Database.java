package carsharing.database;

import carsharing.daointerface.DatabaseInterface;
import carsharing.models.Car;
import carsharing.models.Company;
import carsharing.models.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database implements DatabaseInterface {

    static final String JdbcDriver = "org.h2.Driver";
    private String databaseURL;

    Connection conn = null;
    Statement stmt = null;
    PreparedStatement pstmt = null;

    /*
        - konstruktor klase prima generisan URL
        - automatski će brisati već postojeće tabele:
     */
    public Database(String url) {
        this.databaseURL = url;

        try {
            Class.forName(JdbcDriver);
            conn = DriverManager.getConnection(databaseURL);
            // "autoCommit" mora da bude "true" radi testova:
            conn.setAutoCommit(true);

            stmt = conn.createStatement();


                deleteCustomerTable();
                deleteCarTable();
                deleteCompanyTable();

                //- pozivi ovih metoda su služili za testiranje


            final String CREATE_COMPANY_TABLE = "CREATE TABLE IF NOT EXISTS company (" +
                    "ID INT PRIMARY KEY AUTO_INCREMENT, " +
                    " NAME VARCHAR(30) NOT NULL UNIQUE)";
            // kreiranje "Company" tabele:
            stmt.execute(CREATE_COMPANY_TABLE);

            final String CREATE_CAR_TABLE = "CREATE TABLE IF NOT EXISTS car (" +
                    "ID INT PRIMARY KEY AUTO_INCREMENT," +
                    "NAME VARCHAR(30) NOT NULL UNIQUE," +
                    "COMPANY_ID INT NOT NULL," +
                    "CONSTRAINT fk_companyID FOREIGN KEY (COMPANY_ID)" +
                    "REFERENCES company(ID))";
            // kreiranje "Car" tabele:
            stmt.execute(CREATE_CAR_TABLE);

            final String CREATE_CUSTOMER_TABLE = "CREATE TABLE IF NOT EXISTS customer (" +
                    "ID INT PRIMARY KEY AUTO_INCREMENT," +
                    "NAME VARCHAR(30) NOT NULL UNIQUE," +
                    "RENTED_CAR_ID INT DEFAULT NULL," +
                    "CONSTRAINT fk_carRentedID FOREIGN KEY (RENTED_CAR_ID)" +
                    "REFERENCES car(ID))";
            // kreiranje "Customer" tabele:
            stmt.execute(CREATE_CUSTOMER_TABLE);


        } catch (Exception e) {
            System.out.println("Error while initiating database tables: ");
            e.printStackTrace();
        }
    }

    // metoda koja vraća sve slobodne automobile po zadatoj kompaniji:
    @Override
    public List<Car> getAllCompanyAvailableCars(int companyId) {
        List<Car> availableCars = new ArrayList<>();
        String sqlQuery = "SELECT CAR.ID, CAR.NAME, CAR.COMPANY_ID " +
                "FROM CAR LEFT JOIN CUSTOMER " +
                "ON CAR.ID = CUSTOMER.RENTED_CAR_ID " +
                "WHERE CUSTOMER.NAME IS NULL AND CAR.COMPANY_ID = ?";

        try {

            pstmt = conn.prepareStatement(sqlQuery);
            pstmt.setInt(1, companyId);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Car car = new Car(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("company_id")
                );

                availableCars.add(car);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return availableCars;
    }

    // metoda koja dodaje novu kompaniju u tabelu:
    @Override
    public void addCompany(String name) {
        final String ADD_NEW_COMPANY = "INSERT INTO company (name) VALUES (?)";

        try {

            pstmt = conn.prepareStatement(ADD_NEW_COMPANY);
            pstmt.setString(1, name);
            pstmt.executeUpdate();

        } catch (Exception e) {
            System.out.println("Error while adding a new company!");
            e.printStackTrace();
        }
    }

    // mušterija koja dodaje novi automobil u tabelu:
    @Override
    public void addCar(String name, int companyId) {
        final String ADD_NEW_CAR = "INSERT INTO car (name, company_id) VALUES (?, ?)";

        try {
            pstmt = conn.prepareStatement(ADD_NEW_CAR);

            pstmt.setString(1, name);
            pstmt.setInt(2, companyId);

            pstmt.executeUpdate();

        } catch (Exception e) {
            System.out.println("Error while adding a new car!");
            e.printStackTrace();
        }

    }

    // metoda koja dodaje novu mušteriju u tabelu:
    @Override
    public void addCustomer(String name) {
        final String ADD_CUSTOMER = "INSERT INTO customer (name) VALUES (?)";

        try {

            pstmt = conn.prepareStatement(ADD_CUSTOMER);
            pstmt.setString(1, name);
            pstmt.executeUpdate();

        } catch (Exception e) {
            System.out.println("Error while adding a new customer!");
            e.printStackTrace();
        }

    }

    // metoda koja vraća sve kompanije iz tabele:
    @Override
    public List<Company> getAllCompanies() {
        final String GET_ALL_COMPANIES = "SELECT * FROM company ORDER BY id";
        List<Company> results = new ArrayList<>();

        try {
            pstmt = conn.prepareStatement(GET_ALL_COMPANIES);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Integer id = rs.getInt("id");
                String name = rs.getString("name");

                results.add(new Company(id, name));
            }

        } catch (Exception e) {
            System.out.println("Error while getting all companies from the table!");
            e.printStackTrace();
        }
        return results;
    }

    // metoda koja vraća sve automobile prema određenoj kompaniji:
    @Override
    public List<Car> getAllCars(int companyID) {
        final String GET_CARS_BY_COMPANY = "SELECT * FROM car WHERE company_id = ? ORDER BY id";
        List<Car> results = new ArrayList<>();

        try {
            pstmt = conn.prepareStatement(GET_CARS_BY_COMPANY);
            pstmt.setInt(1, companyID);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Integer id = rs.getInt("id");
                String name = rs.getString("name");
                Integer company_id = rs.getInt("company_id");

                results.add(new Car(id, name, company_id));
            }
        } catch (Exception e) {
            System.out.println("Error while getting all cars by company from the table!");
            e.printStackTrace();
        }
        return results;
    }

    // metoda koja vraća sve mušterije iz tabele:
    @Override
    public List<Customer> getAllCustomers() {
        final String GET_ALL_CUSTOMERS = "SELECT * FROM customer ORDER BY ID";
        List<Customer> results = new ArrayList<>();

        try {
            pstmt = conn.prepareStatement(GET_ALL_CUSTOMERS);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Integer id = rs.getInt("id");
                String name = rs.getString("name");
                Integer rentedCarID = rs.getInt("rented_car_id");

                results.add(new Customer(id, name, rentedCarID));
            }
        } catch (Exception e) {
            System.out.println("Error while getting all customers from the table!");
            e.printStackTrace();
        }
        return results;
    }

    // metoda preko koje se rentira automobil:
    @Override
    public void rentCar(Customer customer, int companyId) {
        final String RENT_CAR_SQL = "UPDATE customer SET rented_car_id = ? WHERE id = ?";

        try {
            pstmt = conn.prepareStatement(RENT_CAR_SQL);

            pstmt.setInt(1, companyId);
            pstmt.setInt(2, customer.getId());

            pstmt.executeUpdate();

        } catch (Exception e) {
            System.out.println("Error while renting the car!");
            e.printStackTrace();
        }

    }

    // metoda preko koje se vraća automobil:
    @Override
    public void returnCar(Customer customer, int companyId) {
        final String RETURN_CAR_SQL = "UPDATE customer SET rented_car_id = null WHERE id = ?";

        try {

            pstmt = conn.prepareStatement(RETURN_CAR_SQL);
            pstmt.setInt(1, customer.getId());
            pstmt.executeUpdate();

        } catch (Exception e) {
            System.out.println("Error while returning the car!");
            e.printStackTrace();
        }
    }

    // metoda koja zatvara konekciju ka bazi:
    @Override
    public void closeAndExit() {
        try {
            stmt.close();
            conn.close();
        } catch (Exception e) {
            System.out.println("Error while closing the connection!");
            e.printStackTrace();
        }
        System.exit(0);
    }

    // vraćanje svih automobila iz "CAR" tabele:
    @Override
    public List<Car> getAllCars() {
        final String GET_ALL_CARS = "SELECT * FROM CAR;";
        List<Car> results = new ArrayList<>();

        try(ResultSet rs = conn.prepareStatement(GET_ALL_CARS).executeQuery()) {
            while (rs.next()) {
                results.add(
                        new Car(
                                rs.getInt("id"),
                                rs.getString("name"),
                                rs.getInt("company_id")
                        )
                );
            }
        } catch (SQLException e) {
            System.out.println("Error while getting all cars from the CAR table, check the error message" + e.getMessage());
        }
        return results;

    }


    // metoda koja briše već postojeću "CUSTOMER" tabelu:
    @Override
    public void deleteCustomerTable() {
        final String DELETE_CUSTOMER_TABLE = "DROP TABLE IF EXISTS CUSTOMER;";

        try(Statement statement = conn.createStatement()) {
            statement.execute(DELETE_CUSTOMER_TABLE);
        } catch (SQLException e) {
            System.out.println("Error while deleting the CUSTOMER table, check the error message:" + e.getMessage());
        }
    }

    // metoda koja briše već postojeću "CAR" tabelu:
    @Override
    public void deleteCarTable() {
        final String DELETE_CAR_TABLE = "DROP TABLE IF EXISTS CAR;";

        try {
            stmt = conn.createStatement();
            stmt.execute(DELETE_CAR_TABLE);
        } catch (SQLException e) {
            System.out.println("Error while deleting the CAR table, check the error message:" + e.getMessage());
        }
    }

    @Override
    // metoda koja briše već postojeću "COMPANY" tabelu:
    public void deleteCompanyTable() {
        final String DELETE_COMPANY_TABLE = "DROP TABLE IF EXISTS COMPANY;";

        try {
            stmt = conn.createStatement();
            stmt.execute(DELETE_COMPANY_TABLE);
        } catch (SQLException e) {
            System.out.println("Error while deleting the COMPANY table, check the error message:" + e.getMessage());
        }
    }
}
