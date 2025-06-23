import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;



public class Admin extends User // 1.1 this is inhertance 
{

    // private : Restricts access to the scanner object within the Admin class.
    //Static : ensures there is only one instance of scanner shared across all instances of the Admin class.
    private static Scanner scanner = new Scanner(System.in);
   
   
    // constructor
    public Admin(int userId, String username, String password) {
        super(userId, username, password, "Admin");  //Uses the superclass User constructor to initialize the admin object.
                                                         //Sets the role as "Admin".
    }

    //
    public boolean login(String username, String password) {
        try {
            File file = new File("users.txt");
            if (!file.exists()) return false; // if the file exists. If not, it immediately returns false, indicating login failure.
             
            /*
                FileReader can read characters or an array of characters, but it doesn't handle entire lines efficiently.
                Scannr can interpret the character stream from FileReader and read lines directly.
                Wrapping FileReader in Scanner allows you to easily process the file line by line.   
            */ 
            FileReader reader = new FileReader(file); 
            Scanner fileScanner = new Scanner(reader);


           /*
                -Uses a while loop to read each line of the file.
                -Splits the line using comma (,) as the delimiter.
                -The result is stored in an array called parts. 

                -The method hasNextLine() in the Scanner class  :
                is used to check whether there is another line of input available to read from the source.
                return :
                true if there is another line of input.
                false if the end of the input has been reached.
            */
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                String[] parts = line.split(",");
                /*
                  parts[0]:
                  Represents the user ID from a line in the file. 
                  */
                if (parts.length == 4 && parts[1].equals(username) && parts[2].equals(password) && parts[3].equals("Admin")) {
                    fileScanner.close();
                    return true;
                }
            }

            fileScanner.close();

            /*
                Why catch Exception?
                To handle unexpected issues that may occur during file reading, like:
                File not found
                IO errors
                Parsing errors
                Printing the error message helps to identify the problem when debugging.
            */
        } catch (Exception e) {
            System.out.println("Error during login: " + e.getMessage());
        }
        
        return false;
    }

    
    public void AddUser() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        System.out.print("Enter role (Admin/Operator): ");
        String role = scanner.nextLine();

        try 
        {

            File file = new File("users.txt");
            int newId = 1;

            if (file.exists()) 
            {
                FileReader reader = new FileReader(file);
                Scanner fileScanner = new Scanner(reader);

                while (fileScanner.hasNextLine()) 
                {
                    
                    String line = fileScanner.nextLine(); //Reads the next line from the file as a String.

                    /*
                      So "3,JohnDoe,pass123,Admin" becomes:
                            parts[0] = "3"
                            parts[1] = "JohnDoe"
                            parts[2] = "pass123"
                            parts[3] = "Admin"
                    */
                    String[] parts = line.split(",");// Splits the line by the comma delimiter , into an array of strings.

                    /*
                    Determines the next available ID for the new user by finding the maximum existing ID in the file and adding 1 to it.
                    Ensures that no two users have the same ID.
                    */
                    newId = Math.max(newId, Integer.parseInt(parts[0]) + 1);
                    /*
                        parts[0] contains the user ID as a string, e.g., "3".
                        Integer.parseInt(parts[0]) converts the string "3" into the integer 3.
                        Integer.parseInt(parts[0]) + 1 adds 1 to get 4. This represents the next possible new ID after the current one.
                        Math.max(newId, ...) compares the current newId (initially 1) with this new candidate ID and sets newId to the larger of the two.
                        This means newId will always store the highest user ID + 1 found so far.
                    
                     */

                }
                fileScanner.close();
            }

            /*

             Creates a FileWriter object to write to the users.txt file.
             The second parameter true means append mode — 
             it will add new data to the end of the file without overwriting the existing content.

             */
            FileWriter writer = new FileWriter(file, true);
            writer.write(newId + "," + username + "," + password + "," + role + "\n");
            writer.close();
            System.out.println("User added successfully!");

        } catch (Exception e) {
            System.out.println("Error adding user: " + e.getMessage());
        }
    }


    public void UpdateUser() {
        System.out.print("Enter user ID to update: ");
        int userId = scanner.nextInt();
        scanner.nextLine();

        try {
            File file = new File("users.txt");
            if (!file.exists()) {
                System.out.println("No users found.");
                return;
            }
            List<String> lines = new ArrayList<>(); // create l liste to store lines from file 
            FileReader reader = new FileReader(file);
            Scanner fileScanner = new Scanner(reader);
            boolean found = false;
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length == 4 && Integer.parseInt(parts[0]) == userId) {
                    found = true;
                    System.out.print("Enter new username: ");
                    String username = scanner.nextLine();
                    System.out.print("Enter new password: ");
                    String password = scanner.nextLine();
                    System.out.print("Enter new role (Admin/Operator): ");
                    String role = scanner.nextLine();
                    lines.add(userId + "," + username + "," + password + "," + role);//adds exactly one string (the updated user data line) to the lines list at one position.
                } 
                else 
                {
                    lines.add(line);
                }
            }
            fileScanner.close();
            if (!found) {
                System.out.println("User with ID " + userId + " not found.");
                return;
            }
            FileWriter writer = new FileWriter(file);
            for (String line : lines)  // will iterate over each line in the list
            {
                writer.write(line + "\n"); //Opens the file for writing. This overwrites the whole file (doesn't append).
            }
            writer.close();
            System.out.println("User updated successfully!");
        } catch (Exception e) {
            System.out.println("Error updating user: " + e.getMessage());
        }
    }

    public void DeleteUser() 
    {
        System.out.print("Enter user ID to delete: ");
        int userId = scanner.nextInt();
        scanner.nextLine();

        try 
        {
            File file = new File("users.txt");
            if (!file.exists())  // will update condetion 
            {
                System.out.println("No users found.");
                return;
            }

            List<String> lines = new ArrayList<>();
            FileReader reader = new FileReader(file);
            Scanner fileScanner = new Scanner(reader);
            boolean found = false;
            while (fileScanner.hasNextLine()) 
            {
                String line = fileScanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length == 4 && Integer.parseInt(parts[0]) == userId) 
                {
                    found = true;
                } 
                else
                {
                    lines.add(line); // if line doesnot match ,add it to list 
                }

            }

            fileScanner.close();
            if (!found) 
            {
                System.out.println("User with ID " + userId + " not found.");
                return;
            }

            FileWriter writer = new FileWriter(file);
            for (String line : lines) 
            {
                writer.write(line + "\n");
            }
            
            writer.close();
            System.out.println("User deleted successfully!");
        } catch (Exception e) {
            System.out.println("Error deleting user: " + e.getMessage());
        }
    }

    public void viewTotalSpots() 
    {
        try 
        {
            File file = new File("parking_spots.txt");
            if (!file.exists()) 
            {
                System.out.println("Total parking spots: 0");
                return;
            }

            FileReader reader = new FileReader(file);
            Scanner fileScanner = new Scanner(reader);
            int count = 0;
            while (fileScanner.hasNextLine()) 
            {
                fileScanner.nextLine();
                count++;
            } 
            fileScanner.close();
            System.out.println("Total parking spots: " + count);
        } catch (Exception e) {
            System.out.println("Error retrieving total spots: " + e.getMessage());
        }
    }

    public void viewParkedCarsReport() 
    {
        try 
        {
            File file = new File("parked_cars.txt");
            if (!file.exists()) 
            {
                System.out.println("No cars are currently parked.");
                return;
            }
        
            FileReader reader = new FileReader(file);
            Scanner fileScanner = new Scanner(reader);
            System.out.println("Parked Cars Report:");
            boolean hasCars = false;
            while (fileScanner.hasNextLine()) 
            {
                hasCars = true;
                String line = fileScanner.nextLine();
                String[] parts = line.split(",");
                System.out.println("Ticket ID: " + parts[0]);
                System.out.println("Spot ID: " + parts[1]);
                System.out.println("Customer Name: " + parts[3]);
                System.out.println("Plate Number: " + parts[4]);
                System.out.println("Entry Date: " + parts[2]);
                System.out.println("--------------------");
            }
            fileScanner.close();

            if (!hasCars) 
            {
                System.out.println("No cars are currently parked.");
            }
            
        } 
        catch (Exception e)
        {
            System.out.println("Error retrieving parked cars report: " + e.getMessage());
        }
    }

    public void viewShiftsReport() 
    {
        System.out.println("\n--- Shifts Report ---");
        File shiftsFile = new File("shifts.txt");
        File ticketsFile = new File("tickets.txt");
        File usersFile = new File("users.txt");

        if (!shiftsFile.exists()) 
        {
            System.out.println("No shifts found.");
            return;
        }

        boolean hasShifts = false;
        /*
            Uses a try-with-resources block to open shifts.txt for reading safely.
            Scanner reads the file line by line. 
         */
        try (FileReader shiftsReader = new FileReader(shiftsFile); Scanner shiftsScanner = new Scanner(shiftsReader)) 
        {

            while (shiftsScanner.hasNextLine()) 
            {
                hasShifts = true;
                String shiftLine = shiftsScanner.nextLine();
                String[] shiftParts = shiftLine.split(",");
                if (shiftParts.length < 3) 
                { 
                    /*
                        Validates that the line has at least 3 parts (shiftId, operatorId, startTime).
                        If the line is malformed, prints a warning and skips to the next line. 
                    */
                    System.out.println("Warning: Skipping malformed shift line: " + shiftLine);
                    continue;
                }
                /*
                    trim() :
                        It removes spaces, tabs, newlines, or any other whitespace characters from the start and end of the string.
                        it does not remove spaces or characters inside the string, only at the edges.
                 */
                int shiftId = Integer.parseInt(shiftParts[0].trim());  
                String operatorId = shiftParts[1].trim();
                String startTime = shiftParts[2].trim();
                String endTime = shiftParts.length > 3 && shiftParts[3] != null && !shiftParts[3].trim().isEmpty() ? shiftParts[3].trim() : "Still ongoing";

                String operatorName = "Unknown";
                if (usersFile.exists()) 
                {
                    try (FileReader usersReader = new FileReader(usersFile); Scanner usersScanner = new Scanner(usersReader)) 
                    {
                        while (usersScanner.hasNextLine()) {
                            String userLine = usersScanner.nextLine();
                            String[] userParts = userLine.split(",");
                            if (userParts.length > 1 && userParts[0].trim().equals(operatorId))
                            {
                                operatorName = userParts[1].trim();
                                break;
                            }
                        }
                    } 
                    catch (java.io.IOException e_users) 
                    { // Catch specific to users file reading
                        System.out.println("Warning: Error reading users file: " + e_users.getMessage());
                    }
                }

                System.out.println("Shift ID: " + shiftId);
                System.out.println("Operator: " + operatorName + " (ID: " + operatorId + ")");
                System.out.println("Start Time: " + startTime);
                System.out.println("End Time: " + endTime);

                // calculate totalPayments
                double totalPayments = 0.0;
                if (ticketsFile.exists()) 
                {
                    try (FileReader ticketsReader = new FileReader(ticketsFile); Scanner ticketsScanner = new Scanner(ticketsReader)) 
                    {
                        while (ticketsScanner.hasNextLine()) 
                        {
                            String ticketLine = ticketsScanner.nextLine();
                            String[] ticketParts = ticketLine.split(",");
                            if (ticketParts.length > 6 && ticketParts[6] != null && !ticketParts[6].trim().isEmpty() && ticketParts[6].trim().equals(String.valueOf(shiftId))) 
                            {
                                if (ticketParts.length > 7 && ticketParts[7] != null && !ticketParts[7].trim().isEmpty())
                                {
                                    try 
                                    {
                                        totalPayments += Double.parseDouble(ticketParts[7].trim());
                                    } 
                                    catch (NumberFormatException e_payment) 
                                    {
                                        System.out.println("Warning: Could not parse payment amount for ticket. Line: \"" + ticketLine + "\". Amount string: \"" + ticketParts[7] + "\". Error: " + e_payment.getMessage());
                                    }
                                }
                            }
                        }
                    }
                    catch (java.io.IOException e_tickets) 
                    { // Catch specific to tickets file reading
                        System.out.println("Warning: Error reading tickets file: " + e_tickets.getMessage());
                    }
                }
                System.out.println("Total Payments: " + totalPayments + " EGP");
                System.out.println("--------------------");
            }
        } 
        catch (java.io.IOException | NumberFormatException e_shifts) 
        { // Catch for shifts file reading and main processing
            System.out.println("Error retrieving shifts report: " + e_shifts.getMessage());
        }

        if (!hasShifts && shiftsFile.exists()) 
        {
            System.out.println("No shifts data found in the file.");
        }
    }
   

    public void removeSpot(String spotId) 
    {
        try 
        {
            File file = new File("parking_spots.txt");
            if (!file.exists()) 
            {
                System.out.println("No parking spots found.");
                return;
            }
            List<String> lines = new ArrayList<>();
            FileReader reader = new FileReader(file);
            Scanner fileScanner = new Scanner(reader);
            boolean found = false;
            while (fileScanner.hasNextLine()) 
            {
                String line = fileScanner.nextLine();
                String[] parts = line.split(",");
                if (parts[0].equals(spotId)) 
                {
                    found = true;
                } 
                else 
                {
                    lines.add(line);
                }
            }
            fileScanner.close();

            if (!found) 
            {
                System.out.println("Parking spot with ID " + spotId + " not found.");
                return;
            }

            FileWriter writer = new FileWriter(file);
            for (String line : lines) 
            {
                writer.write(line + "\n");
            }
            writer.close();
            System.out.println("Parking spot " + spotId + " removed.");
        } 
        catch (Exception e) 
        {
            System.out.println("Error removing parking spot: " + e.getMessage());
        }
    }

    public void viewUsers() 
    {
        try 
        {
            File file = new File("users.txt");
            if (!file.exists()) 
            {
                System.out.println("No users found.");
                return;
            }
            FileReader reader = new FileReader(file);
            Scanner fileScanner = new Scanner(reader);
            System.out.println("List of users:");
            while (fileScanner.hasNextLine()) 
            {
                String line = fileScanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    System.out.println("ID: " + parts[0] + ", Username: " + parts[1] + ", Role: " + parts[3]);
                }
            }
            fileScanner.close();
        } 
        catch (Exception e) 
        {
            System.out.println("Error retrieving users: " + e.getMessage());
        }
    }
}

