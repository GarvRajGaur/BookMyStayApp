/**
 * BookingRecord
 *
 * Use Case 8: Booking History & Reporting
 *
 * Represents an immutable snapshot of a single confirmed booking.
 * Once created, a BookingRecord cannot be altered — it captures the
 * exact state of the reservation at the moment of confirmation.
 *
 * Why immutable?
 * Historical records must be tamper-proof. If a booking record could
 * be modified after the fact, the audit trail would be unreliable.
 * All fields are final, and there are no setter methods.
 *
 * Stored fields:
 *   - guestName    : who made the booking
 *   - roomType     : type of room assigned (Single / Double / Suite)
 *   - roomId       : unique room ID assigned during allocation (e.g., SINGLE-101)
 *   - bookingIndex : sequential position in history (1-based, for reporting)
 *
 * @author GARV RAJ
 * @version 8.0
 */
public class BookingRecord {

    private final String guestName;
    private final String roomType;
    private final String roomId;
    private final int    bookingIndex;

    /**
     * Constructs an immutable BookingRecord.
     *
     * @param bookingIndex sequential booking number (1, 2, 3 ...)
     * @param guestName    name of the confirmed guest
     * @param roomType     type of room assigned
     * @param roomId       unique room ID assigned during allocation
     */
    public BookingRecord(int bookingIndex, String guestName,
                         String roomType, String roomId) {
        this.bookingIndex = bookingIndex;
        this.guestName    = guestName;
        this.roomType     = roomType;
        this.roomId       = roomId;
    }

    /** @return sequential booking number */
    public int getBookingIndex() { return bookingIndex; }

    /** @return guest name */
    public String getGuestName() { return guestName; }

    /** @return room type string */
    public String getRoomType()  { return roomType; }

    /** @return unique room ID */
    public String getRoomId()    { return roomId; }

    /**
     * Returns a formatted, human-readable summary of this booking record.
     */
    @Override
    public String toString() {
        return String.format("  [#%02d] Guest: %-20s | Room Type: %-8s | Room ID: %s",
                bookingIndex, guestName, roomType, roomId);
    }
}