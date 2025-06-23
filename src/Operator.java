import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Operator extends User {
    private static Scanner consoleScanner = new Scanner(System.in); // Renamed to avoid conflict
    private int currentShiftId;
    private static final String SHIFTS_FILE_PATH = "shifts.txt";
    private static final String USERS_FILE_PATH = "users.txt";

    public Operator(int userId, String username, String password) {
        super(userId, username, password, "Operator");
        this.currentShiftId = -1; // No active shift initially
    }

    private boolean findAndSetLatestActiveShift(int userId) {
        File shiftsFile = new File(SHIFTS_FILE_PATH);
        if (!shiftsFile.exists()) {
            return false;
        }
        int activeShiftToSetId = -1;
        LocalDateTime latestShiftStartTime = null;
        try (Scanner fileScanner = new Scanner(shiftsFile)) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                String[] parts = line.split(",", -1);
                if (parts.length >= 3) {
                    try {
                        int lineShiftId = Integer.parseInt(parts[0].trim());
                        int lineUserId = Integer.parseInt(parts[1].trim());
                        if (lineUserId == userId) {
                            boolean isActiveShiftLine = (parts.length == 3) ||
                                    (parts.length == 4 && parts[3].trim().isEmpty());
                            if (isActiveShiftLine) {
                                LocalDateTime startTime = LocalDateTime.parse(parts[2].trim());
                                if (activeShiftToSetId == -1 || startTime.isAfter(latestShiftStartTime)) {
                                    activeShiftToSetId = lineShiftId;
                                    latestShiftStartTime = startTime;
                                }
                            }
                        }
                    } catch (NumberFormatException | DateTimeParseException e) {
                        System.out.println("Skipping malformed line in " + SHIFTS_FILE_PATH + ": '" + line + "'. Error: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading " + SHIFTS_FILE_PATH + " to find active shift: " + e.getMessage());
            return false;
        }
        if (activeShiftToSetId != -1) {
            this.currentShiftId = activeShiftToSetId;
            return true;
        }
        return false;
    }

    public boolean login(String username, String password) {
        try {
            File usersFile = new File(USERS_FILE_PATH);
            if (!usersFile.exists()) {
                System.out.println(USERS_FILE_PATH + " not found.");
                return false;
            }
            try (Scanner fileScanner = new Scanner(usersFile)) {
                while (fileScanner.hasNextLine()) {
                    String line = fileScanner.nextLine().trim();
                    String[] parts = line.split(",");
                    if (parts.length == 4 && parts[1].trim().equals(username) &&
                            parts[2].trim().equals(password) && parts[3].trim().equals("Operator")) {
                        int operatorUserId = Integer.parseInt(parts[0].trim());
                        if (findAndSetLatestActiveShift(operatorUserId)) {
                            System.out.println("Resumed active shift ID: " + this.currentShiftId + " for operator ID: " + operatorUserId);
                        } else {
                            startShift(operatorUserId);
                        }
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error during operator login: " + e.getMessage());
        }
        return false;
    }

    private void startShift(int userId) {
        try {
            File shiftsFile = new File(SHIFTS_FILE_PATH);
            int newShiftId = 1;
            if (shiftsFile.exists()) {
                try (Scanner fileScanner = new Scanner(shiftsFile)) {
                    while (fileScanner.hasNextLine()) {
                        String line = fileScanner.nextLine().trim();
                        String[] parts = line.split(",");
                        if (parts.length > 0 && !parts[0].isEmpty()) {
                            try {
                                newShiftId = Math.max(newShiftId, Integer.parseInt(parts[0].trim()) + 1);
                            } catch (NumberFormatException e) {
                                // Ignore
                            }
                        }
                    }
                }
            }
            try (FileWriter writer = new FileWriter(shiftsFile, true)) {
                String startTime = LocalDateTime.now().toString();
                writer.write(newShiftId + "," + userId + "," + startTime + "\n");
            }
            this.currentShiftId = newShiftId;
            System.out.println("Shift started with ID: " + this.currentShiftId + " for operator ID: " + userId);
        } catch (Exception e) {
            System.out.println("Error starting shift: " + e.getMessage());
        }
    }

    public void endShift() {
        if (this.currentShiftId == -1) {
            System.out.println("No active shift to end.");
            return;
        }
        File shiftsFile = new File(SHIFTS_FILE_PATH);
        if (!shiftsFile.exists()) {
            System.out.println("No shifts file found to end shift (".concat(SHIFTS_FILE_PATH).concat(")."));
            return;
        }
        List<String> lines = new ArrayList<>();
        boolean shiftFoundAndUpdated = false;
        try (Scanner fileScanner = new Scanner(shiftsFile)) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                String[] parts = line.split(",", -1);
                if (parts.length >= 1 && !parts[0].trim().isEmpty()) {
                    try {
                        int lineShiftId = Integer.parseInt(parts[0].trim());
                        if (lineShiftId == this.currentShiftId) {
                            boolean isActiveShiftLine = (parts.length == 3) ||
                                    (parts.length == 4 && parts[3].trim().isEmpty());
                            if (isActiveShiftLine && parts.length >=3) {
                                String endTime = LocalDateTime.now().toString();
                                lines.add(parts[0].trim() + "," + parts[1].trim() + "," + parts[2].trim() + "," + endTime);
                                shiftFoundAndUpdated = true;
                            } else {
                                lines.add(line);
                            }
                        } else {
                            lines.add(line);
                        }
                    } catch (NumberFormatException e) {
                        lines.add(line);
                    }
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading shifts file to end shift: " + e.getMessage());
            return;
        }

        if (shiftFoundAndUpdated) {
            // Enclose FileWriter operations in a try-catch block for IOException
            try (FileWriter writer = new FileWriter(shiftsFile, false)) {
                for (String updatedLine : lines) {
                    writer.write(updatedLine + "\n");
                }
                System.out.println("Shift " + this.currentShiftId + " ended.");
                this.currentShiftId = -1;
            } catch (IOException e) {
                System.out.println("Error writing updated shifts file: " + e.getMessage());
                // Optionally, consider how to handle this error, e.g., restore original lines or log critical error
            }
        } else {
            System.out.println("Could not find active shift " + this.currentShiftId + " in the expected format to end. It might have been ended already or an error occurred.");
        }
    }

    public void logoutWithoutEndingShift() {
        if (this.currentShiftId != -1) {
            System.out.println("Logging out without ending shift. Shift " + this.currentShiftId + " is still active.");
        } else {
            System.out.println("Logging out. No active shift was associated with this session.");
        }
    }

    public int getCurrentShiftId() {
        return this.currentShiftId;
    }

    public void SuggestParkingSpot() {
        try {
            File spotsFile = new File("parking_spots.txt");
            if (!spotsFile.exists()) {
                System.out.println("No parking spots available (parking_spots.txt not found).");
                return;
            }
            System.out.println("\n--- Parking Spots Status ---");
            boolean hasAvailableSpot = false;
            String suggestedSpotInfo = "No available spots.";
            try (Scanner spotsScanner = new Scanner(spotsFile)) {
                while (spotsScanner.hasNextLine()) {
                    String spotLine = spotsScanner.nextLine().trim();
                    String[] spotParts = spotLine.split(",");
                    if (spotParts.length == 3) {
                        String spotIdStr = spotParts[0].trim();
                        boolean isOccupied = Boolean.parseBoolean(spotParts[1].trim());
                        System.out.println("Spot ID: " + spotIdStr + " - " + (isOccupied ? "Occupied" : "Available"));
                        if (isOccupied) {
                            // Could add logic to show customer details if needed
                        } else {
                            if (!hasAvailableSpot) {
                                hasAvailableSpot = true;
                                suggestedSpotInfo = "Suggested Parking Spot for Customer: Spot ID " + spotIdStr;
                            }
                        }
                        System.out.println("--------------------");
                    }
                }
            }
            System.out.println(suggestedSpotInfo);
        } catch (Exception e) {
            System.out.println("Error retrieving parking spots: " + e.getMessage());
        }
    }

    public void calculatePaymentAtExitStation(double pricePerHour) {
        System.out.print("Enter ticket ID to calculate payment: ");
        int ticketId = -1;
        String ticketIdStr = consoleScanner.nextLine();
        try {
            ticketId = Integer.parseInt(ticketIdStr);
            PaymentCalculator.calculatePayment(ticketId, pricePerHour);
        } catch (NumberFormatException e){
            System.out.println("Invalid ticket ID format.");
        }
    }
}