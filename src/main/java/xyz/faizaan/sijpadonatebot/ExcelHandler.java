package xyz.faizaan.sijpadonatebot;

import com.codoid.products.exception.FilloException;
import com.codoid.products.fillo.Connection;
import com.codoid.products.fillo.Fillo;
import com.codoid.products.fillo.Recordset;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ExcelHandler {

    private Connection spreadsheet;

    public ExcelHandler(File spreadsheetFile) throws Exception {
        this.spreadsheet = new Fillo().getConnection(spreadsheetFile.getAbsolutePath());
    }

    public List<String> getAllNames() {
        String query = "Select * from Sheet1";
        try {
            Recordset set = spreadsheet.executeQuery(query);

            List<String> names = new ArrayList<>();

            while (set.next()) {
                String name = set.getField("First Name") + " " + set.getField("Last Name");
                names.add(name);
            }

            return names;
        } catch (FilloException e) {
            return null;
        }
    }

    public Donor getDonorInfo(String fullName) {
        String[] nameParts = fullName.split(" ");
        Donor donor = new Donor();
        donor.firstName = nameParts[0];
        donor.lastName = nameParts[1];

        String query = "Select * from Sheet1";
        try {
            Recordset set = spreadsheet.executeQuery(query).where("\"First Name\"='" + donor.firstName + "'").where("\"Last Name\"='" + donor.lastName + "'");

            if (set.next()) {
                donor.address = set.getField("Address");
                donor.city = set.getField("City");
                donor.state = set.getField("State");
                donor.zipCode = set.getField("Zip Code");

                makeTransaction(donor, set);

                // this should return all the transactions
                while (set.next()) {
                    makeTransaction(donor, set);
                }
            }
        } catch (FilloException e) {
            e.printStackTrace();
            return null;
        }

        return donor;
    }

    private void makeTransaction(Donor donor, Recordset set) throws FilloException {
        Transaction transaction = new Transaction();
        transaction.amount = set.getField("Amount");
        transaction.type = set.getField("Type");
        transaction.date = set.getField("Date");
        transaction.category = set.getField("Class");
        transaction.subcategory = set.getField("Sub Class");
        donor.transactions.add(transaction);
    }

}
