package xyz.faizaan.sijpadonatebot;

public class Transaction {

    public String amount, date, type, category /* class */, subcategory /* sub class */;

    @Override
    public String toString() {
        return "Transaction{" +
                "amount='" + amount + '\'' +
                ", date='" + date + '\'' +
                ", type='" + type + '\'' +
                ", category='" + category + '\'' +
                ", subcategory='" + subcategory + '\'' +
                '}';
    }
}
