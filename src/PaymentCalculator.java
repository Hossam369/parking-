import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class PaymentCalculator {
    public static void calculatePayment(int ticketId, double pricePerHour) {
        try {
            File file = new File("tickets.txt");
            if (!file.exists()) {
                System.out.println("Ticket with ID " + ticketId + " not found.");
                return;
            }
            List<String> lines = new ArrayList<>();
            FileReader reader = new FileReader(file);
            Scanner scanner = new Scanner(reader);
            boolean found = false;
            String plateNumber = "";
            String customerName = "";
            LocalDateTime entryDateTime = null;
            long totalHours = 0; // Declare totalHours here with a default value
            double totalAmount = 0.0; // Declare totalAmount here with a default value

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length >= 7 && Integer.parseInt(parts[0]) == ticketId) {
                    found = true;
                    plateNumber = parts[1];
                    customerName = parts[5];
                    entryDateTime = LocalDateTime.parse(parts[2]);

                    LocalDateTime exitDateTime = LocalDateTime.now();
                    // LocalTime entryTime = entryDateTime.toLocalTime(); // Not used in this version of the fix
                    // LocalTime exitTime = exitDateTime.toLocalTime(); // Not used in this version of the fix

                    Duration duration = Duration.between(entryDateTime, exitDateTime);
                    long calculatedHours = duration.toHours();
                    long totalMinutes = duration.toMinutes();
                    if (totalMinutes > 0 && calculatedHours < 1) {
                        calculatedHours = 1;
                    }

                    double calculatedAmount = calculatedHours * pricePerHour;

                    totalHours = calculatedHours; // Assign to the higher-scope variable
                    totalAmount = calculatedAmount; // Assign to the higher-scope variable

                    lines.add(parts[0] + "," + parts[1] + "," + parts[2] + "," + exitDateTime + "," +
                            totalHours + "," + parts[5] + "," + parts[6] + "," + totalAmount);
                } else {
                    lines.add(line);
                }
            }
            scanner.close();
            if (!found) {
                System.out.println("Ticket with ID " + ticketId + " not found.");
                return;
            }
            FileWriter writer = new FileWriter(file);
            for (String line : lines) {
                writer.write(line + "\n");
            }
            writer.close();

            System.out.println("Ticket ID: " + ticketId);
            System.out.println("Plate Number: " + plateNumber);
            System.out.println("Customer Name: " + customerName);
            System.out.println("Entry Time: " + entryDateTime.toLocalTime());
            System.out.println("Exit Time: " + LocalDateTime.now().toLocalTime());
            System.out.println("Total Parking Duration: " + totalHours + " hours");
            System.out.println("Total Amount to Pay: " + totalAmount + " EGP");
        } catch (Exception e) {
            System.out.println("Error calculating payment: " + e.getMessage());
        }
    }
}
