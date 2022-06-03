package carsharing.app;

import carsharing.daointerface.DatabaseInterface;
import carsharing.models.Car;
import carsharing.models.Company;
import carsharing.models.Customer;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CarSharingApp {

    private static Scanner scanner = new Scanner(System.in);
    private DatabaseInterface database;
    private static Map<CarSharingMenu, String> menuMap;

    public CarSharingApp(DatabaseInterface database) {
        this.database = database;
    }

    static {
        menuMap = new HashMap<>();

        String mainMenu = "1. Log in as a manager\n" +
                "2. Log in as a customer\n" +
                "3. Create a customer\n" +
                "0. Exit";

        String managerMenu = "1. Company list\n" +
                "2. Create a company\n" +
                "0. Back";

        String carMenu = "\n1. Car list\n" +
                "2. Create a car\n" +
                "0. Back";

        String rentingMenu = "1. Rent a car\n" +
                "2. Return a rented car\n" +
                "3. My rented car\n" +
                "0. Back";

        menuMap.put(CarSharingMenu.MAIN_MENU, mainMenu);
        menuMap.put(CarSharingMenu.MANAGER_MENU, managerMenu);
        menuMap.put(CarSharingMenu.CAR_MENU, carMenu);
        menuMap.put(CarSharingMenu.RENTING_MENU, rentingMenu);
    }


    private void showRentedCar(Customer customer) {
        Integer rentedCarID = customer.getRentedCarID();

        if (rentedCarID == null) {
            System.out.println("You didn't rent a car!\n");
            return;
        }

        // pronalaženje rentiranog automobila za datu mušteriju:
        Car rentedCar = database.getAllCars().stream()
                .filter(x -> x.getId().equals(rentedCarID))
                .findAny()
                .orElse(null);

        // pronalaženje kompanije čije je vlasništvo dati automobil:
        Company companyUsed = database.getAllCompanies().stream()
                .filter(x -> x.getId().equals(rentedCar.getCompanyID()))
                .findAny()
                .orElse(null);

        System.out.println("Your rented car:");
        System.out.println(rentedCar.getName());
        System.out.println("Company:");
        System.out.println(companyUsed.getName());
        System.out.println();
    }


    private void returnCar(Customer customer) {
        Integer rentedCarID = customer.getRentedCarID();

        if (rentedCarID == null) {
            System.out.println("You didn't rent a car!\n");
            return;
        }

        // automobil je sada slobodan:
        database.returnCar(customer, customer.getRentedCarID());
        customer.setRentedCarID(null);
        System.out.println("You've returned a rented car!\n");

    }

    private void rentCar(Customer customer) {
        if (customer.getRentedCarID() != null) {
            System.out.println("You've already rented a car!");
            return;
        }

        List<Company> companyList = database.getAllCompanies();
        if (companyList.isEmpty()) {
            System.out.println("The company list is empty!\n");
            return;
        }

        for (;;) {
            System.out.println("Choose a company:");
            printList(companyList);
            System.out.println("0. Back\n");

            String input = scanner.nextLine().trim();
            int selectedOption = 0;

            try {
                selectedOption = Integer.parseInt(input);

                if (selectedOption < 0 || selectedOption > companyList.size()) {
                    System.out.println("Invalid option!\n");
                    return;
                }

                if (selectedOption == 0) {
                    return;
                }

                // izbor kompanije iz menija:
                Company selectedCompany = companyList.get(selectedOption - 1);
                List<Car> avaliableCars = database.getAllCompanyAvailableCars(selectedCompany.getId());

                if (avaliableCars.isEmpty()) {
                    System.out.printf("No available cars in the %s company\n", selectedCompany.getName());
                    return;
                }

                System.out.print("Choose a car:\n");
                printList(avaliableCars);
                System.out.print("0. Exit\n");

                // proces rentiranja:
                int selection = Integer.parseInt(scanner.nextLine());
                if (selection >= 1 && selection <= avaliableCars.size()) {

                    Car rentedCar = avaliableCars.get(selection - 1);
                    database.rentCar(customer, rentedCar.getId());
                    customer.setRentedCarID(rentedCar.getId());
                    System.out.printf("You rented '%s'\n", rentedCar.getName());

                }

                return;

            } catch (NumberFormatException ex) {
                System.err.println("Invalid option!\n");
            }
        }

    }

    // metoda koja izlistava automobile za zadatu kompaniju:
    private void showCars(List<Car> companyCars) {
        if (companyCars.isEmpty()) {
            System.out.println("The car list is empty!");
            return;
        }

        System.out.println("Car list:");
        printList(companyCars);

    }

    private void addNewCustomer() {
        System.out.println("Enter the customer name:");
        database.addCustomer(scanner.nextLine().trim());
        System.out.println("The customer was added!\n");
    }

    private void addNewCar(Company company) {
        System.out.println("Enter the car name:");

        database.addCar(scanner.nextLine().trim(), company.getId());

        System.out.println("The car was added!");
    }

    private void addNewCompany() {
        System.out.println("Enter the company name:");
        database.addCompany(scanner.nextLine().trim());
        System.out.println("The company was created!\n");
    }


    // metoda koja prikazuje elemente liste u formatu potrebnom za aplikaciju:
    private <T> void printList (List<T> list) {
        // "default" vrijednost je "0":
        AtomicInteger counter = new AtomicInteger();
        list.forEach(obj -> {
            counter.getAndIncrement();
            System.out.printf("%s. %s \n", counter, obj.toString());
        });
    }

    // ----------------------------------------> METODE ZA MENI <----------------------------------------
    private void startRentingMenu(Customer customer) {
        for (;;) {
            System.out.println(menuMap.get(CarSharingMenu.RENTING_MENU));

            String input = scanner.nextLine().trim();
            int selectedOption = 0;
            try {
                selectedOption = Integer.parseInt(input);

                switch(selectedOption) {
                    case 0:
                        return;
                    case 1:
                        rentCar(customer);
                        break;
                    case 2:
                        returnCar(customer);
                        break;
                    case 3:
                        showRentedCar(customer);
                        break;
                    default:
                        System.out.println("Invalid option!\n");
                }
            } catch (NumberFormatException ex) {
                System.out.println("Invalid option!\n");
            }
        }
    }

    private void startCustomerMenu() {
        List<Customer> customersList = new ArrayList<>(database.getAllCustomers());

        if (customersList.isEmpty()) {
            System.out.println("The customer list is empty!\n");
            return;
        }

        System.out.println("Customer list:");
        printList(customersList);
        System.out.println("0. Back\n");

        for (;;) {
            String input = scanner.nextLine().trim();
            int selectedOption = 0;
            try {
                selectedOption = Integer.parseInt(input);

                if (selectedOption < 0 || selectedOption > customersList.size()) {
                    System.out.println("Invalid option!\n");
                    return;
                }

                if (selectedOption == 0) {
                    return;
                }

                startRentingMenu(customersList.get(selectedOption - 1));
                return;

            } catch (NumberFormatException ex) {
                System.out.println("Invalid option!\n");
            }
        }
    }

    private void startCarMenu(Company company) {
        System.out.printf("'%s' company", company.getName());

        for (;;) {
            System.out.println(menuMap.get(CarSharingMenu.CAR_MENU));
            List<Car> companyCars = database.getAllCars(company.getId());

            String input = scanner.nextLine().trim();
            int selectedOption = 0;

            try {
                selectedOption = Integer.parseInt(input);
                switch (selectedOption) {
                    case 1:
                        showCars(companyCars);
                        break;
                    case 2:
                        addNewCar(company);
                        break;
                    case 0:
                        startManagerMenu();
                        return;
                    default:
                        System.out.println("Invalid option!\n");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid option!\n");
            }
        }
    }

    private void startCompanyMenu() {
        List<Company> allCompanies = new ArrayList<>(database.getAllCompanies());

        if (allCompanies.isEmpty()) {
            System.out.println("The company list is empty!\n");
            return;
        }

        for (;;) {
            System.out.println("Choose the company:");
            printList(allCompanies);
            System.out.println("0. Back");

            String input = scanner.nextLine().trim();
            int selectedOption = 0;
            try {
                selectedOption = Integer.parseInt(input);
                if (selectedOption < 0 || selectedOption > allCompanies.size()) {
                    System.out.println("Invalid option!\n");
                    return;
                }

                if (selectedOption == 0) {
                    return;
                }

                Company foundCompany = allCompanies.get(selectedOption - 1);
                startCarMenu(foundCompany);

            } catch (NumberFormatException e) {
                System.out.println("Invalid option!\n");
            }
        }
    }

    private void startManagerMenu() {
        for (;;) {
            System.out.println(menuMap.get(CarSharingMenu.MANAGER_MENU));

            String input = scanner.nextLine().trim();
            int selectedOption = 0;
            try {
                selectedOption = Integer.parseInt(input);

                switch (selectedOption) {
                    case 1:
                        startCompanyMenu();
                        break;
                    case 2:
                        addNewCompany();
                        break;
                    case 0:
                        start();
                        break;
                    default:
                        System.out.println("Invalid option!\n");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid option!\n");
            }
        }
    }

    public void start() {
        for (;;) {
            System.out.println(menuMap.get(CarSharingMenu.MAIN_MENU));

            String input = scanner.nextLine().trim();
            int selectedOption = 0;
            try {
                selectedOption = Integer.parseInt(input);

                switch (selectedOption) {
                    case 1:
                        startManagerMenu();
                        break;
                    case 2:
                        startCustomerMenu();
                        break;
                    case 3:
                        addNewCustomer();
                        break;
                    case 0:
                        database.closeAndExit();
                        break;
                    default:
                        System.out.println("Invalid option!\n");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid option!\n");
            }
        }
    }

}
