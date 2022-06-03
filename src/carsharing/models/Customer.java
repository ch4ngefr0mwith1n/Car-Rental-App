package carsharing.models;

public class Customer {

    private Integer id;
    private String name;
    private Integer rentedCarID;

    public Customer(Integer id, String name, Integer rentedCarID) {
        this.id = id;
        this.name = name;
        this.rentedCarID = null;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getRentedCarID() {
        return rentedCarID;
    }

    public void setRentedCarID(Integer rentedCarID) {
        this.rentedCarID = rentedCarID;
    }

    // metoda "toString" će da vraća ime mušterije:
    @Override
    public String toString() {
        return name;
    }
}
