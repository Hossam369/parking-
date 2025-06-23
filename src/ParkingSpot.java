public class ParkingSpot {
    private String spotId;
    private boolean isOccupied = false;
    private int ticketId = -1;

    public ParkingSpot(String spotId) {
        this.spotId = spotId;
    }

    public String getSpotId() {
        return spotId;
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public int getTicketId() {
        return ticketId;
    }

    public void occupySpot(int ticketId) {
        this.isOccupied = true;
        this.ticketId = ticketId;
    }

    public void freeSpot() {
        this.isOccupied = false;
        this.ticketId = -1;
    }
}
