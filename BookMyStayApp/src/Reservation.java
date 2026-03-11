/**
 * Reservation
 *
 * Represents a guest's intent to book a specific room type.
 * This is a lightweight data-holding object (value object) that
 * captures the guest name and requested room type at the time
 * of submission.
 *
 * No booking confirmation or inventory interaction occurs here.
 * A Reservation is simply a request — it becomes a confirmed
 * booking only after allocation (Use Case 6+).
 *
 * @author GARV RAJ
 * @version 5.0
 */
public class Reservation {

    private String guestName;
    private String roomType;

    /**
     * Constructs a Reservation with the guest's name and desired room type.
     *
     * @param guestName the name of the guest making the request
     * @param roomType  the type of room requested (e.g., "Single", "Double", "Suite")
     */
    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }

    /**
     * Returns the name of the guest who submitted this request.
     *
     * @return guest name
     */
    public String getGuestName() {
        return guestName;
    }

    /**
     * Returns the room type requested by the guest.
     *
     * @return room type string
     */
    public String getRoomType() {
        return roomType;
    }

    /**
     * Returns a human-readable summary of this reservation request.
     *
     * @return formatted reservation string
     */
    @Override
    public String toString() {
        return "Reservation [Guest: " + guestName + ", Room Type: " + roomType + "]";
    }
}