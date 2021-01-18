package xyz.faizaan.sijpadonatebot;

public class Transaction {

    public String amount, date, type, checkNum, category /* class */, subcategory /* sub class */;

    @Override
    public String toString() {
        return "Transaction{" +
                "amount='" + amount + '\'' +
                ", date='" + date + '\'' +
                ", type='" + type + '\'' +
                ", checkNum='" + checkNum + '\'' +
                ", category='" + category + '\'' +
                ", subcategory='" + subcategory + '\'' +
                '}';
    }
}
