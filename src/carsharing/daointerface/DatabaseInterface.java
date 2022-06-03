package carsharing.daointerface;

import carsharing.models.Car;
import carsharing.models.Company;
import carsharing.models.Customer;

import java.util.List;

public interface DatabaseInterface {
    public void addCompany(String name);
    public void addCar(String name, int companyId);
    public void addCustomer(String name);

    public List<Company> getAllCompanies();
    public List<Car> getAllCars();
    public List<Customer> getAllCustomers();
    public List<Car> getAllCars(int companyID);
    public List<Car> getAllCompanyAvailableCars(int companyId);

    public void rentCar(Customer customer, int companyId);
    public void returnCar(Customer customer, int companyId);

    public void deleteCompanyTable();
    public void deleteCarTable();
    public void deleteCustomerTable();

    public void closeAndExit();


}
