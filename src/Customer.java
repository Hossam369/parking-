import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Scanner;

public class Customer {
    private String name;
    private String plateNumber;
    private Ticket currentTicket;

    public Customer(String name, String plateNumber) {
        this.name = name;
        this.plateNumber = plateNumber;
    }

    public String getName() {
        return name;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public Ticket printTicket(int ticketId, int shiftId) {
        currentTicket = new Ticket(ticketId, this.plateNumber);
        currentTicket.setEntryTime(LocalTime.now());
        currentTicket.setCustomerName(this.name);
        currentTicket.setShiftId(shiftId);

        try {
            File file = new File("tickets.txt");
            FileWriter writer = new FileWriter(file, true);
            String entryDate = LocalDateTime.of(LocalDate.now(), currentTicket.getEntryTime()).toString();
            writer.write(ticketId + "," + this.plateNumber + "," + entryDate + ",,0.0," + this.name + "," + shiftId + ",0.0\n");
            writer.close();
            System.out.println("Ticket printed for customer " + name + " with ID: " + ticketId);
        } catch (Exception e) {
            System.out.println("Error saving ticket: " + e.getMessage());
            return null;
        }
        return currentTicket;
    }

    public void payParking(Ticket ticket, double pricePerHour) {
        PaymentCalculator.calculatePayment(ticket.getTicketId(), pricePerHour);
    }
}
