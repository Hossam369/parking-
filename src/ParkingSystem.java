import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ParkingSystem {

    private static Scanner scanner = new Scanner(System.in);
    private static double pricePerHour = 10.0;
    private static Operator currentOperator;
    private static final String USERS_FILE_PATH = "users.txt";
    private static final String TICKETS_FILE_PATH = "tickets.txt";
    private static final String CUSTOMERS_FILE_PATH = "customers.txt";
    private static final String PARKING_SPOTS_FILE_PATH = "parking_spots.txt";
    private static final String PARKED_CARS_FILE_PATH = "parked_cars.txt";

    public static double getPricePerHour() {
        return pricePerHour;
    }

    private static void initializeUsersFile() {
        File usersFile = new File(USERS_FILE_PATH);
        if (!usersFile.exists()) {
            try (FileWriter writer = new FileWriter(usersFile)) {
                writer.write("1,admin,admin123,Admin\n");
                writer.write("2,operator,op123,Operator\n");
                System.out.println(USERS_FILE_PATH + " not found. Created with default admin (admin/admin123) and operator (operator/op123).");
            } catch (IOException e) {
                System.out.println("Error creating " + USERS_FILE_PATH + ": " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        initializeUsersFile();
        Admin admin = new Admin(0, "", "");

        System.out.println("Welcome to the Parking Guidance System!");
        boolean running = true;
        while (running) {
            System.out.print("Are you an Admin, Operator, or Customer? (Enter '1' for Admin, '2' for Operator, '3' for Customer, '4' to Exit): ");
            String userType = scanner.nextLine().trim();

            switch (userType) {
                case "1":
                    handleAdminLogin(admin);
                    break;
                case "2":
                    Operator operatorInstance = new Operator(0, "", "");
                    handleOperatorLogin(operatorInstance);
                    break;
                case "3":
                    if (currentOperator == null || currentOperator.getCurrentShiftId() == -1) {
                        System.out.println("No active operator shift. A customer cannot proceed without an active operator.");
                        System.out.println("Please have an operator log in and start/resume a shift.");
                    } else {
                        handleCustomer(currentOperator);
                    }
                    break;
                case "4":
                    running = false;
                    System.out.println("Thank you for using the system. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid input. Please enter '1' for Admin, '2' for Operator, '3' for Customer, or '4' to Exit.");
                    break;
            }
        }
        scanner.close();
    }

    public static List<ParkingSpot> getParkingSpots() {
        List<ParkingSpot> spots = new ArrayList<>();
        try {
            File file = new File(PARKING_SPOTS_FILE_PATH);
            if (!file.exists()) return spots;
            try(Scanner fileScanner = new Scanner(file)){
                while (fileScanner.hasNextLine()) {
                    String line = fileScanner.nextLine();
                    String[] parts = line.split(",");
                    if(parts.length == 3){
                        String spotId = parts[0];
                        ParkingSpot spot = new ParkingSpot(spotId);
                        boolean isOccupied = Boolean.parseBoolean(parts[1]);
                        int ticketId = Integer.parseInt(parts[2]);
                        if (isOccupied && ticketId != -1) {
                            spot.occupySpot(ticketId);
                        }
                        spots.add(spot);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error retrieving parking spots: " + e.getMessage());
        }
        return spots;
    }

    private static void handleAdminLogin(Admin adminInstance) {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        if (adminInstance.login(username, password)) {
            System.out.println("Admin login successful!");
            boolean adminRunning = true;
            while (adminRunning) {
                System.out.println("\n--- Admin Menu ---");
                System.out.println("1. Add Parking Spot");
                System.out.println("2. View Total Spots");
                System.out.println("3. View Parked Cars Report");
                System.out.println("4. Remove Parking Spot");
                System.out.println("5. View Users");
                System.out.println("6. Add User");
                System.out.println("7. Update User");
                System.out.println("8. Delete User");
                System.out.println("9. View Shifts Report with Payments");
                System.out.println("10. Exit");
                System.out.print("Choose an option: ");
                String choiceStr = scanner.nextLine();
                int choice = -1;
                try {
                    choice = Integer.parseInt(choiceStr);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a number.");
                    continue;
                }

                switch (choice) {
                    case 1:
                        try {
                            File file = new File(PARKING_SPOTS_FILE_PATH);
                            int newSpotId = 1;
                            if (file.exists() && file.length() > 0) {
                                try(Scanner fileScanner = new Scanner(file)){
                                    while (fileScanner.hasNextLine()) {
                                        String line = fileScanner.nextLine();
                                        if (!line.trim().isEmpty()){
                                            String[] parts = line.split(",");
                                            if (parts.length > 0 && !parts[0].trim().isEmpty()){
                                                try {
                                                    newSpotId = Math.max(newSpotId, Integer.parseInt(parts[0].trim()) + 1);
                                                } catch (NumberFormatException ex){
                                                    // ignore lines that don't start with a number
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            try(FileWriter writer = new FileWriter(file, true)){
                                writer.write(newSpotId + ",false,-1\n");
                            }
                            System.out.println("Parking spot " + newSpotId + " added successfully!");
                        } catch (Exception e) {
                            System.out.println("Error adding parking spot: " + e.getMessage());
                        }
                        break;
                    case 2:
                        adminInstance.viewTotalSpots();
                        break;
                    case 3:
                        adminInstance.viewParkedCarsReport();
                        break;
                    case 4:
                        System.out.print("Enter spot ID to remove: ");
                        String spotIdToRemove = scanner.nextLine();
                        adminInstance.removeSpot(spotIdToRemove);
                        break;
                    case 5:
                        adminInstance.viewUsers();
                        break;
                    case 6:
                        adminInstance.AddUser();
                        break;
                    case 7:
                        adminInstance.UpdateUser();
                        break;
                    case 8:
                        adminInstance.DeleteUser();
                        break;
                    case 9:
                        adminInstance.viewShiftsReport();
                        break;
                    case 10:
                        adminRunning = false;
                        System.out.println("Logging out...");
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            }
        } else {
            System.out.println("Invalid Admin credentials.");
        }
    }

    private static void handleOperatorLogin(Operator operatorInstance) {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        if (operatorInstance.login(username, password)) {
            System.out.println("Operator login successful!");
            currentOperator = operatorInstance;
            boolean operatorRunning = true;
            while (operatorRunning) {
                System.out.println("\n--- Operator Menu ---");
                System.out.println("1. Suggest Parking Spot");
                System.out.println("2. End Shift");
                System.out.println("3. Calculate Payment at Exit Station");
                System.out.println("4. Exit (Logout)");
                System.out.print("Choose an option: ");
                String choiceStr = scanner.nextLine();
                int choice = -1;
                try {
                    choice = Integer.parseInt(choiceStr);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a number.");
                    continue;
                }

                switch (choice) {
                    case 1:
                        operatorInstance.SuggestParkingSpot();
                        break;
                    case 2:
                        operatorInstance.endShift();
                        if(operatorInstance.getCurrentShiftId() == -1) {
                            currentOperator = null;
                            operatorRunning = false;
                            System.out.println("Shift ended. Returning to main menu.");
                        }
                        break;
                    case 3:
                        operatorInstance.calculatePaymentAtExitStation(getPricePerHour());
                        break;
                    case 4:
                        operatorInstance.logoutWithoutEndingShift();
                        operatorRunning = false;
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            }
        } else {
            System.out.println("Invalid Operator credentials.");
        }
    }

    private static Ticket findActiveTicketForCustomer(String customerName) {
        File ticketsFile = new File(TICKETS_FILE_PATH);
        if (!ticketsFile.exists()) return null;
        Ticket latestActiveTicket = null;
        LocalDateTime latestEntryDateTime = null;
        try (Scanner fileScanner = new Scanner(ticketsFile)) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                String[] parts = line.split(",", -1);
                if (parts.length >= 6 && parts[5].trim().equalsIgnoreCase(customerName)) {
                    String exitDateTimeStr = (parts.length > 3) ? parts[3].trim() : "";
                    if (exitDateTimeStr.isEmpty()) {
                        try {
                            int ticketId = Integer.parseInt(parts[0].trim());
                            String plateNumber = parts[1].trim();
                            LocalDateTime entryDateTime = LocalDateTime.parse(parts[2].trim());
                            if (latestActiveTicket == null || entryDateTime.isAfter(latestEntryDateTime)) {
                                latestActiveTicket = new Ticket(ticketId, plateNumber);
                                latestActiveTicket.setCustomerName(customerName);
                                latestActiveTicket.setEntryTime(entryDateTime.toLocalTime());
                                if (parts.length > 6 && !parts[6].trim().isEmpty()) latestActiveTicket.setShiftId(Integer.parseInt(parts[6].trim()));
                                latestEntryDateTime = entryDateTime;
                            }
                        } catch (NumberFormatException | DateTimeParseException e) {
                            System.err.println("Skipping malformed active ticket line: " + line + " | Error: " + e.getMessage());
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading " + TICKETS_FILE_PATH + ": " + e.getMessage());
        }
        return latestActiveTicket;
    }

    private static Ticket getTicketDetailsById(int ticketIdToFind) {
        File ticketsFile = new File(TICKETS_FILE_PATH);
        if (!ticketsFile.exists()) return null;
        try (Scanner fileScanner = new Scanner(ticketsFile)) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                String[] parts = line.split(",", -1);
                if (parts.length > 0 && !parts[0].trim().isEmpty()) {
                    try {
                        int currentFileTicketId = Integer.parseInt(parts[0].trim());
                        if (currentFileTicketId == ticketIdToFind) {
                            Ticket ticket = new Ticket(currentFileTicketId, parts.length > 1 ? parts[1].trim() : "");
                            if (parts.length > 2 && !parts[2].trim().isEmpty()) ticket.setEntryTime(LocalDateTime.parse(parts[2].trim()).toLocalTime());
                            if (parts.length > 3 && !parts[3].trim().isEmpty()) ticket.setExitTime(LocalDateTime.parse(parts[3].trim()).toLocalTime());
                            if (parts.length > 4 && !parts[4].trim().isEmpty()) ticket.setTotalHours(Double.parseDouble(parts[4].trim()));
                            if (parts.length > 5 && !parts[5].trim().isEmpty()) ticket.setCustomerName(parts[5].trim());
                            if (parts.length > 6 && !parts[6].trim().isEmpty()) ticket.setShiftId(Integer.parseInt(parts[6].trim()));
                            if (parts.length > 7 && !parts[7].trim().isEmpty()) ticket.setTotalAmount(Double.parseDouble(parts[7].trim()));
                            return ticket;
                        }
                    } catch (NumberFormatException | DateTimeParseException e) {
                        System.err.println("Skipping malformed ticket line during ID search: " + line + " | Error: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading " + TICKETS_FILE_PATH + " for details: " + e.getMessage());
        }
        return null;
    }

    private static void handleCustomer(Operator activeOperator) {
        System.out.print("Enter your name: ");
        String name = scanner.nextLine();
        System.out.print("Enter your plate number: ");
        String plateNumber = scanner.nextLine();

        Customer customer = findCustomerByName(name);
        if (customer == null) {
            System.out.println("New customer detected. Registering...");
            customer = new Customer(name, plateNumber);
            try (FileWriter writer = new FileWriter(CUSTOMERS_FILE_PATH, true)) {
                writer.write(name + "," + plateNumber + "\n");
            } catch (Exception e) {
                System.out.println("Error saving customer: " + e.getMessage());
            }
        } else {
            System.out.println("Welcome back, " + name + "!");
        }

        boolean customerRunning = true;
        Ticket sessionActiveTicket = findActiveTicketForCustomer(customer.getName());

        while (customerRunning) {
            System.out.println("\n--- Customer Menu ---");
            System.out.println("1. Print Ticket (Enter Parking)");
            System.out.println("2. Pay and Exit");
            System.out.println("3. View Ticket Details by ID");
            System.out.println("4. Exit to Main Menu");
            System.out.print("Choose an option: ");
            String choiceStr = scanner.nextLine();
            int choice = -1;
            try {
                choice = Integer.parseInt(choiceStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            switch (choice) {
                case 1: // Print Ticket (Enter Parking)
                    if (sessionActiveTicket != null) {
                        System.out.println("You already have an active ticket (ID: " + sessionActiveTicket.getTicketId() + "). Please pay and exit first.");
                    } else {
                        if (activeOperator.getCurrentShiftId() == -1) {
                            System.out.println("Critical Error: No active operator shift.");
                            customerRunning = false; break;
                        }
                        boolean hasAvailableSpot = false;
                        for (ParkingSpot spot : getParkingSpots()) {
                            if (!spot.isOccupied()) {
                                hasAvailableSpot = true; break;
                            }
                        }
                        if (hasAvailableSpot) {
                            int nextTicketId = 1;
                            File ticketsFile = new File(TICKETS_FILE_PATH);
                            if (ticketsFile.exists()) {
                                try (Scanner ticketScanner = new Scanner(ticketsFile)){
                                    while(ticketScanner.hasNextLine()){
                                        String line = ticketScanner.nextLine();
                                        String[] parts = line.split(",");
                                        if(parts.length > 0 && !parts[0].isEmpty()){
                                            try {
                                                nextTicketId = Math.max(nextTicketId, Integer.parseInt(parts[0].trim()) + 1);
                                            } catch (NumberFormatException e) {/*ignore*/}
                                        }
                                    }
                                } catch (IOException e) { /* Error reading existing tickets, ID might not be unique */
                                    System.err.println("Error reading tickets.txt for ID generation: " + e.getMessage());
                                }
                            }

                            Ticket printedTicket = customer.printTicket(nextTicketId, activeOperator.getCurrentShiftId());
                            if (printedTicket != null) {
                                assignParkingSpot(printedTicket.getTicketId());
                                sessionActiveTicket = printedTicket;
                            } else {
                                System.out.println("Failed to print ticket. Please try again.");
                            }
                        } else {
                            System.out.println("Sorry, no available parking spots. Please try again later.");
                        }
                    }
                    break;
                case 2: // Pay and Exit
                    if (sessionActiveTicket != null) {
                        System.out.println("Processing payment for your active ticket ID: " + sessionActiveTicket.getTicketId());
                        customer.payParking(sessionActiveTicket, pricePerHour);
                        freeParkingSpot(sessionActiveTicket.getTicketId());
                        System.out.println("Payment successful for ticket " + sessionActiveTicket.getTicketId() + ". Thank you!");
                        sessionActiveTicket = null;
                    } else {
                        System.out.println("You don't have an active ticket associated with this session.");
                        System.out.print("Enter Ticket ID to pay (if you know it, otherwise leave blank to cancel): ");
                        String ticketIdToPayStr = scanner.nextLine().trim();
                        if (!ticketIdToPayStr.isEmpty()) {
                            try {
                                int ticketIdToPay = Integer.parseInt(ticketIdToPayStr);
                                Ticket ticketDetails = getTicketDetailsById(ticketIdToPay);

                                if (ticketDetails != null) {
                                    if (ticketDetails.getExitTime() == null) { // Active and unpaid
                                        System.out.println("Processing payment for ticket ID: " + ticketIdToPay);
                                        // We need a customer object to call payParking.
                                        // If the ticketDetails has customerName, we could try to find/create that customer.
                                        // For simplicity, we'll use the current customer object, assuming they are paying for their own ticket.
                                        // If ticketDetails.getCustomerName() is different, this might be logically inconsistent for record keeping in Customer class.
                                        // However, PaymentCalculator only needs ticketId.
                                        customer.payParking(ticketDetails, pricePerHour); // Pass the fetched ticket object
                                        freeParkingSpot(ticketIdToPay);
                                        System.out.println("Payment successful for ticket " + ticketIdToPay + ". Thank you!");
                                    } else { // Ticket has an exit time, so it's considered processed/paid.
                                        System.out.println("Ticket ID " + ticketIdToPay + " has already been processed and closed.");
                                        System.out.println("  Customer Name: " + (ticketDetails.getCustomerName() != null ? ticketDetails.getCustomerName() : "N/A"));
                                        System.out.println("  Plate Number: " + (ticketDetails.getPlateNumber() != null ? ticketDetails.getPlateNumber() : "N/A"));
                                        System.out.println("  Entry Time: " + (ticketDetails.getEntryTime() != null ? ticketDetails.getEntryTime().toString() : "N/A"));
                                        System.out.println("  Exit Time: " + ticketDetails.getExitTime()); // Known to be non-null here
                                        System.out.println("  Total Hours: " + ticketDetails.getTotalHours());
                                        System.out.println("  Total Amount: " + ticketDetails.getTotalAmount() + " EGP");
                                    }
                                } else {
                                    System.out.println("Ticket ID " + ticketIdToPay + " not found.");
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("Invalid Ticket ID format.");
                            }
                        }
                    }
                    break;
                case 3: // View Ticket Details by ID
                    System.out.print("Enter ticket ID to view details: ");
                    String ticketIdStrView = scanner.nextLine();
                    try {
                        int ticketIdToView = Integer.parseInt(ticketIdStrView);
                        viewTicketDetails(ticketIdToView);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid Ticket ID format.");
                    }
                    break;
                case 4:
                    customerRunning = false;
                    System.out.println("Exiting customer menu...");
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static Customer findCustomerByName(String name) {
        try (Scanner fileScanner = new Scanner(new File(CUSTOMERS_FILE_PATH))) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length == 2 && parts[0].equalsIgnoreCase(name)) {
                    return new Customer(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            // If file doesn't exist, it's fine, new customer will be created.
        }
        return null;
    }

    private static void assignParkingSpot(int ticketId) {
        List<ParkingSpot> parkingSpots = getParkingSpots();
        ParkingSpot assignedSpot = null;
        for (ParkingSpot spot : parkingSpots) {
            if (!spot.isOccupied()) {
                spot.occupySpot(ticketId);
                assignedSpot = spot;
                System.out.println("Please park at spot: " + spot.getSpotId());
                break;
            }
        }

        if (assignedSpot != null) {
            try {
                String entryDate = "";
                String customerName = "";
                String plateNumber = "";
                try(Scanner ticketsScanner = new Scanner(new File(TICKETS_FILE_PATH))){
                    while (ticketsScanner.hasNextLine()) {
                        String line = ticketsScanner.nextLine();
                        String[] parts = line.split(",");
                        // Ensure parts has enough elements before accessing them
                        if (parts.length >= 6 && !parts[0].trim().isEmpty() && Integer.parseInt(parts[0].trim()) == ticketId) {
                            entryDate = parts[2]; // Assuming entry date is always present for a printed ticket
                            customerName = parts[5];
                            plateNumber = parts[1];
                            break;
                        }
                    }
                }
                try(FileWriter parkedCarsWriter = new FileWriter(PARKED_CARS_FILE_PATH, true)){
                    parkedCarsWriter.write(ticketId + "," + assignedSpot.getSpotId() + "," +
                            entryDate + "," + customerName + "," + plateNumber + "\n");
                }

                List<String> spotLines = new ArrayList<>();
                try(Scanner spotsScanner = new Scanner(new File(PARKING_SPOTS_FILE_PATH))){
                    while (spotsScanner.hasNextLine()) {
                        String line = spotsScanner.nextLine();
                        String[] parts = line.split(",");
                        if (parts.length > 0 && parts[0].equals(assignedSpot.getSpotId())) {
                            spotLines.add(parts[0] + ",true," + ticketId);
                        } else {
                            spotLines.add(line);
                        }
                    }
                }
                try(FileWriter spotsWriter = new FileWriter(PARKING_SPOTS_FILE_PATH, false)){
                    for (String line : spotLines) {
                        spotsWriter.write(line + "\n");
                    }
                }
            } catch (Exception e) {
                System.out.println("Error updating "+PARKED_CARS_FILE_PATH +" or "+PARKING_SPOTS_FILE_PATH+": " + e.getMessage());
            }
        } else {
            System.out.println("Critical Error: No available spot found to assign, though check passed earlier.");
        }
    }

    private static void freeParkingSpot(int ticketId) {
        String spotIdToFree = null;
        try {
            File parkedCarsFile = new File(PARKED_CARS_FILE_PATH);
            if (parkedCarsFile.exists()) {
                List<String> parkedCarLines = new ArrayList<>();
                boolean entryFound = false;
                try(Scanner carScanner = new Scanner(parkedCarsFile)){
                    while (carScanner.hasNextLine()) {
                        String line = carScanner.nextLine();
                        String[] parts = line.split(",");
                        if (parts.length > 1 && !parts[0].trim().isEmpty() && Integer.parseInt(parts[0].trim()) == ticketId) {
                            spotIdToFree = parts[1];
                            entryFound = true;
                        } else {
                            parkedCarLines.add(line);
                        }
                    }
                }
                if(entryFound){
                    try(FileWriter writer = new FileWriter(parkedCarsFile, false)){
                        for (String line : parkedCarLines) {
                            writer.write(line + "\n");
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error updating "+PARKED_CARS_FILE_PATH+": " + e.getMessage());
        }

        if (spotIdToFree != null) {
            try {
                List<String> spotLines = new ArrayList<>();
                File spotsFile = new File(PARKING_SPOTS_FILE_PATH);
                boolean spotUpdated = false;
                if (spotsFile.exists()) {
                    try(Scanner spotsScanner = new Scanner(spotsFile)){
                        while (spotsScanner.hasNextLine()) {
                            String line = spotsScanner.nextLine();
                            String[] parts = line.split(",");
                            if (parts.length > 0 && parts[0].equals(spotIdToFree)) {
                                spotLines.add(parts[0] + ",false,-1");
                                spotUpdated = true;
                            } else {
                                spotLines.add(line);
                            }
                        }
                    }
                    if(spotUpdated){
                        try(FileWriter spotsWriter = new FileWriter(spotsFile, false)){
                            for (String line : spotLines) {
                                spotsWriter.write(line + "\n");
                            }
                        }
                        System.out.println("Spot " + spotIdToFree + " freed.");
                    } else {
                        System.out.println("Spot " + spotIdToFree + " (from ticket "+ticketId+") not found in "+PARKING_SPOTS_FILE_PATH+" to update.");
                    }
                }
            } catch (IOException e) {
                System.out.println("Error updating "+PARKING_SPOTS_FILE_PATH+": " + e.getMessage());
            }
        } else {
            // This case can happen if parked_cars.txt was cleared or ticketId never parked.
            // System.out.println("Ticket ID " + ticketId + " not found in "+PARKED_CARS_FILE_PATH+", cannot determine spot to free.");
        }
    }

    private static void viewTicketDetails(int ticketId) {
        Ticket ticket = getTicketDetailsById(ticketId);
        if (ticket != null) {
            System.out.println("--- Ticket Details ---");
            System.out.println("Ticket ID: " + ticket.getTicketId());
            System.out.println("Plate Number: " + (ticket.getPlateNumber() != null ? ticket.getPlateNumber() : "N/A"));
            System.out.println("Customer Name: " + (ticket.getCustomerName() != null ? ticket.getCustomerName() : "N/A"));
            if (ticket.getEntryTime() != null) System.out.println("Entry Time: " + ticket.getEntryTime());

            if (ticket.getExitTime() != null) {
                System.out.println("Exit Time: " + ticket.getExitTime());
                System.out.println("Total Hours: " + ticket.getTotalHours());
                System.out.println("Total Amount: " + ticket.getTotalAmount() + " EGP");
            } else {
                System.out.println("Status: Car is currently parked (ticket is active/unpaid).");
            }
            System.out.println("Shift ID: " + ticket.getShiftId());
            System.out.println("----------------------");
        } else {
            System.out.println("Ticket with ID " + ticketId + " not found.");
        }
    }
}