package carsharing;

import carsharing.app.CarSharingApp;
import carsharing.daointerface.DatabaseInterface;
import carsharing.database.Database;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        String databaseURL = getDatabaseURL(getDatabaseName(args));
        DatabaseInterface database = new Database(databaseURL);

        CarSharingApp app = new CarSharingApp(database);
        app.start();
    }

    private static String getDatabaseURL(String name) {
        return String.format("jdbc:h2:./src/carsharing/db/%s", name);
    }

    private static String getDatabaseName(String[] args) {
        List<String> arguments = List.of(args);

        return (arguments.size() == 2 && arguments.indexOf("-databaseFileName") == 0) ?
                arguments.get(1) : "carsharing";
    }
}