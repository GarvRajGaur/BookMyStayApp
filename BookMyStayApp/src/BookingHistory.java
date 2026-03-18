import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * BookingHistory
 *
 * Use Case 8: Booking History & Reporting
 *
 * Maintains a chronological, ordered record of all confirmed bookings
 * using a List<BookingRecord>.
 *
 * Why List over Set or Map?
 * - List preserves insertion order — bookings appear in confirmation sequence
 * - List allows sequential access for reporting (first booking → last booking)
 * - Unlike Set, List allows retrieval by position (get index #1, #2, etc.)
 * - Unlike Map, no key is needed — position IS the implicit ordering
 *
 * Persistence Mindset:
 * Although stored in memory, this List is treated as long-lived data.
 * Records are never removed or modified once added — only appended.
 * This mirrors how a real database append-only log would behave,
 * conceptually preparing learners for file/DB persistence later.
 *
 * @author GARV RAJ
 * @version 8.0
 */
public class BookingHistory {

    // Ordered, append-only list of confirmed booking records
    private List<BookingRecord> history;

    // Auto-incrementing counter for booking sequence numbers
    private int bookingCounter;

    /**
     * Initializes an empty booking history.
     */
    public BookingHistory() {
        history        = new ArrayList<>();
        bookingCounter = 0;
    }

    /**
     * Appends a new confirmed booking to the history.
     *
     * Called by BookingService immediately after a reservation is confirmed.
     * The booking counter increments with each record, giving every booking
     * a unique, sequential position in the audit trail.
     *
     * This method is the ONLY way to add records — no bulk insert, no
     * modification of existing records.
     *
     * @param guestName name of the confirmed guest
     * @param roomType  type of room assigned
     * @param roomId    unique room ID assigned during allocation
     */
    public void addRecord(String guestName, String roomType, String roomId) {
        bookingCounter++;
        BookingRecord record = new BookingRecord(bookingCounter, guestName, roomType, roomId);
        history.add(record);
    }

    /**
     * Returns an unmodifiable view of the full booking history.
     *
     * Callers can iterate and read records but cannot add, remove,
     * or reorder them — protecting the integrity of the audit trail.
     *
     * @return unmodifiable list of all BookingRecord entries
     */
    public List<BookingRecord> getHistory() {
        return Collections.unmodifiableList(history);
    }

    /**
     * Returns the total number of confirmed bookings recorded.
     *
     * @return size of the history list
     */
    public int getTotalBookings() {
        return history.size();
    }

    /**
     * Checks whether any bookings have been recorded yet.
     *
     * @return true if history is empty
     */
    public boolean isEmpty() {
        return history.isEmpty();
    }

    /**
     * Displays the full booking history in chronological order.
     * Read-only — does not modify the history list.
     */
    public void displayHistory() {
        if (isEmpty()) {
            System.out.println("  No bookings recorded in history.");
            return;
        }
        System.out.println("  Booking No. | Guest Name           | Room Type | Room ID");
        System.out.println("  " + "-".repeat(65));
        for (BookingRecord record : history) {
            System.out.println(record);
        }
    }
}