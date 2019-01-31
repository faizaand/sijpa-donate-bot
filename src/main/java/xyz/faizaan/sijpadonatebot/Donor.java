package xyz.faizaan.sijpadonatebot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Donor {

    public String firstName, lastName, address, city, state, zipCode;
    public List<Transaction> transactions = new ArrayList<>();

    @Override
    public String toString() {

        return "Donor{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", address='" + address + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", zipCode='" + zipCode + '\'' +
                ", transactions (" + transactions.size() + ")=" + transactions.toString() +
                '}';
    }
}
