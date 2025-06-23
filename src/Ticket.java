import java.time.LocalTime;

public class Ticket {
    private int ticketId;
    private String plateNumber;
    private LocalTime entryTime;
    private LocalTime exitTime;
    private String customerName;
    private int shiftId;
    private double totalHours;
    private double totalAmount;

    public Ticket(int ticketId, String plateNumber) {
        this.ticketId = ticketId;
        this.plateNumber = plateNumber;
    }

    public int getTicketId() {
        return ticketId;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public LocalTime getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(LocalTime entryTime) {
        this.entryTime = entryTime;
    }

    public LocalTime getExitTime() {
        return exitTime;
    }

    public void setExitTime(LocalTime exitTime) {
        this.exitTime = exitTime;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public int getShiftId() {
        return shiftId;
    }

    public void setShiftId(int shiftId) {
        this.shiftId = shiftId;
    }

    public double getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(double totalHours) {
        this.totalHours = totalHours;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
}
