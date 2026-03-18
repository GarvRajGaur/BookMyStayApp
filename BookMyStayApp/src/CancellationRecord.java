/**
 * CancellationRecord
 *
 * Use Case 10: Booking Cancellation & Inventory Rollback
 *
 * Represents an immutable record of a single cancellation event.
 * Stored alongside BookingHistory so that cancellations are fully
 * traceable — every reversal has a permanent audit entry.
 *
 * Like BookingRecord, all fields are final — cancellation records
 * cannot be altered after creation. This preserves the integrity
 * of the audit trail.
 *
 * @author GARV RAJ
 * @version 10.0
 */
public class CancellationRecord {

    private final String guestName;
    private final String roomType;
    private final String roomId;
    private final int    cancellationIndex;

    /**
     * Constructs an immutable CancellationRecord.
     *
     * @param cancellationIndex sequential cancellation number (1, 2, 3 ...)
     * @param guestName         name of the guest who cancelled
     * @param roomType          type of room that was released
     * @param roomId            unique room ID that was released back to inventory
     */
    public CancellationRecord(int cancellationIndex, String guestName,
                              String roomType, String roomId) {
        this.cancellationIndex = cancellationIndex;
        this.guestName         = guestName;
        this.roomType          = roomType;
        this.roomId            = roomId;
    }

    /** @return sequential cancellation number */
    public int getCancellationIndex() { return cancellationIndex; }

    /** @return name of the guest who cancelled */
    public String getGuestName()      { return guestName; }

    /** @return room type that was released */
    public String getRoomType()       { return roomType; }

    /** @return room ID that was released back to inventory */
    public String getRoomId()         { return roomId; }

    /**
     * Returns a formatted, human-readable summary of this cancellation record.
     */
    @Override
    public String toString() {
        return String.format(
                "  [CANCEL #%02d] Guest: %-20s | Room Type: %-8s | Room ID: %s",
                cancellationIndex, guestName, roomType, roomId);
    }
}