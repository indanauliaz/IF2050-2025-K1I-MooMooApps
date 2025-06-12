package moomoo.apps.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TransactionModel {
    private final IntegerProperty id;
    private final StringProperty transactionType; 
    private final StringProperty description;
    private final DoubleProperty amount;
    private final StringProperty category;
    private final ObjectProperty<LocalDate> date;
    private final StringProperty paymentMethod;
    private final StringProperty notes;
    private final IntegerProperty userId; 

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; 

    public TransactionModel(int id, String transactionType, String description, double amount, String category, 
                            LocalDate date, String paymentMethod, String notes, int userId) {
        this.id = new SimpleIntegerProperty(id);
        this.transactionType = new SimpleStringProperty(transactionType);
        this.description = new SimpleStringProperty(description);
        this.amount = new SimpleDoubleProperty(amount);
        this.category = new SimpleStringProperty(category);
        this.date = new SimpleObjectProperty<>(date);
        this.paymentMethod = new SimpleStringProperty(paymentMethod);
        this.notes = new SimpleStringProperty(notes);
        this.userId = new SimpleIntegerProperty(userId);
    }
    
    public TransactionModel(String transactionType, String description, double amount, String category, 
                            LocalDate date, String paymentMethod, String notes, int userId) {
        this.id = new SimpleIntegerProperty(0); 
        this.transactionType = new SimpleStringProperty(transactionType);
        this.description = new SimpleStringProperty(description);
        this.amount = new SimpleDoubleProperty(amount);
        this.category = new SimpleStringProperty(category);
        this.date = new SimpleObjectProperty<>(date);
        this.paymentMethod = new SimpleStringProperty(paymentMethod);
        this.notes = new SimpleStringProperty(notes);
        this.userId = new SimpleIntegerProperty(userId);
    }


    public IntegerProperty idProperty() { return id; }
    public StringProperty transactionTypeProperty() { return transactionType; }
    public StringProperty descriptionProperty() { return description; }
    public DoubleProperty amountProperty() { return amount; }
    public StringProperty categoryProperty() { return category; }
    public ObjectProperty<LocalDate> dateProperty() { return date; }
    public StringProperty paymentMethodProperty() { return paymentMethod; }
    public StringProperty notesProperty() { return notes; }
    public IntegerProperty userIdProperty() { return userId; }

    // Setter getter standard
    public int getId() { return id.get(); }
    public String getTransactionType() { return transactionType.get(); }
    public String getDescription() { return description.get(); }
    public double getAmount() { return amount.get(); }
    public String getCategory() { return category.get(); }
    public LocalDate getDate() { return date.get(); }
    public String getDateString() { return date.get() != null ? DATE_FORMATTER.format(date.get()) : ""; }
    public String getPaymentMethod() { return paymentMethod.get(); }
    public String getNotes() { return notes.get(); }
    public int getUserId() { return userId.get(); }

    public void setId(int id) { this.id.set(id); }
    public void setTransactionType(String type) { this.transactionType.set(type); }
    public void setDescription(String description) { this.description.set(description); }
    public void setAmount(double amount) { this.amount.set(amount); }
    public void setCategory(String category) { this.category.set(category); }
    public void setDate(LocalDate date) { this.date.set(date); }
    public void setPaymentMethod(String method) { this.paymentMethod.set(method); }
    public void setNotes(String notes) { this.notes.set(notes); }
    public void setUserId(int userId) { this.userId.set(userId); }
}