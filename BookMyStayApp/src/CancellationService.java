import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * CancellationService
 *
 * Use Case 10: Booking Cancellation & Inventory Rollback
 *
 * Handles safe cancellation of confirmed bookings by reversing all
 * state changes made during allocation — in a strict, controlled order.
 *
 * Core Data Structure — Stack<String>:
 * A Stack<String> tracks recently released room IDs in LIFO order.
 * When a room is cancelled, its ID is pushed onto the rollback stack.
 * This means the MOST RECENTLY cancelled room is always at the top —
 * naturally modelling undo/rollback behavior.
 *
 * Why Stack over List or Queue?
 * - Queue (FIFO): would process the OLDEST cancellation first — wrong for undo
 * - List:         no inherent ordering discipline — order is arbitrary
 * - Stack (LIFO): the last cancelled room is the first candidate for re-allocation,
 *                 which mirrors real-world "last room freed = first room re-offered"
 *
 * Cancellation Steps (controlled mutation — strict order):
 *   1. Validate that the room ID exists in confirmed bookings
 *   2. Validate it has not already been cancelled
 *   3. Retrieve the BookingRecord for that room ID
 *   4. Remove from confirmed bookings map
 *   5. Push released room ID onto rollback Stack
 *   6. Restore inventory count (+1 for the room type)
 *   7. Record cancellation in CancellationRecord list
 *   8. Confirm rollback complete
 *
 * @author GARV RAJ
 * @version 10.0
 */
public class CancellationService {

    // Stack tracks released room IDs in LIFO order — last cancelled = top of stack
    private Stack<String> rollbackStack;

    // Maps room ID → BookingRecord for O(1) lookup during cancellation
    private Map<String, BookingRecord> confirmedBookings;

    // Tracks which room IDs have already been cancelled — prevents double-cancel
    private Map<String, Boolean> cancelledRooms;

    // Ordered list of all cancellation events — audit trail
    private List<CancellationRecord> cancellationLog;

    // Shared references to core system state
    private RoomInventory  inventory;
    private BookingHistory bookingHistory;

    // Auto-incrementing counter for cancellation sequence numbers
    private int cancellationCounter;

    /**
     * Constructs the CancellationService with references to shared system state.
     *
     * @param inventory      the centralized room inventory (for rollback restoration)
     * @param bookingHistory the booking history (to mark cancelled bookings)
     */
    public CancellationService(RoomInventory inventory, BookingHistory bookingHistory) {
        this.inventory          = inventory;
        this.bookingHistory     = bookingHistory;
        this.rollbackStack      = new Stack<>();
        this.confirmedBookings  = new HashMap<>();
        this.cancelledRooms     = new HashMap<>();
        this.cancellationLog    = new ArrayList<>();
        this.cancellationCounter = 0;

        // Index all confirmed bookings from history into the lookup map
        // This allows O(1) retrieval by room ID during cancellation
        for (BookingRecord record : bookingHistory.getHistory()) {
            confirmedBookings.put(record.getRoomId(), record);
        }
    }

    /**
     * Cancels a confirmed booking by room ID.
     *
     * Performs full state rollback in strict order:
     * validate → remove from confirmed map → push to rollback stack
     * → restore inventory → log cancellation record → confirm.
     *
     * If validation fails at any point, NO state change occurs.
     * The system remains fully consistent on every failure path.
     *
     * @param roomId the unique room ID of the booking to cancel
     */
    public void cancelBooking(String roomId) {
        System.out.println("  Processing cancellation for Room ID: " + roomId);

        // ---------------------------------------------------------------
        // Step 1: Validate — room ID must exist in confirmed bookings
        // ---------------------------------------------------------------
        if (!confirmedBookings.containsKey(roomId)) {
            System.out.println("  [REJECTED]  Room ID \"" + roomId
                    + "\" not found in confirmed bookings. Nothing to cancel.");
            return;
        }

        // ---------------------------------------------------------------
        // Step 2: Validate — must not already be cancelled (no double-cancel)
        // ---------------------------------------------------------------
        if (cancelledRooms.getOrDefault(roomId, false)) {
            System.out.println("  [REJECTED]  Room ID \"" + roomId
                    + "\" has already been cancelled. Duplicate cancellation prevented.");
            return;
        }

        // ---------------------------------------------------------------
        // Step 3: Retrieve the confirmed BookingRecord
        // ---------------------------------------------------------------
        BookingRecord record   = confirmedBookings.get(roomId);
        String        guestName = record.getGuestName();
        String        roomType  = record.getRoomType();

        // ---------------------------------------------------------------
        // Step 4: Remove from confirmed bookings map — booking no longer active
        // ---------------------------------------------------------------
        confirmedBookings.remove(roomId);

        // ---------------------------------------------------------------
        // Step 5: Push released room ID onto LIFO rollback Stack
        // Most recently cancelled room sits at top — ready for re-allocation
        // ---------------------------------------------------------------
        rollbackStack.push(roomId);
        System.out.println("  [ROLLBACK]  Room ID \"" + roomId
                + "\" pushed onto rollback stack. Stack size: " + rollbackStack.size());

        // ---------------------------------------------------------------
        // Step 6: Restore inventory — increment count for the room type
        // Inventory update is immediate — availability reflects reality
        // ---------------------------------------------------------------
        inventory.updateAvailability(roomType, +1);

        // ---------------------------------------------------------------
        // Step 7: Mark as cancelled and log the cancellation event
        // ---------------------------------------------------------------
        cancelledRooms.put(roomId, true);
        cancellationCounter++;
        CancellationRecord cancellation =
                new CancellationRecord(cancellationCounter, guestName, roomType, roomId);
        cancellationLog.add(cancellation);

        // ---------------------------------------------------------------
        // Step 8: Confirm successful rollback
        // ---------------------------------------------------------------
        System.out.println("  [CANCELLED] Guest: " + guestName
                + " | Room Type: " + roomType
                + " | Room ID: " + roomId + " released back to inventory.");
    }

    /**
     * Displays the current state of the rollback Stack.
     *
     * Shows all released room IDs in LIFO order (top = most recently cancelled).
     * Read-only — stack is not modified during display.
     */
    public void displayRollbackStack() {
        System.out.println("  Rollback Stack (top = most recently cancelled):");
        if (rollbackStack.isEmpty()) {
            System.out.println("    (empty — no cancellations performed)");
            return;
        }

        // Iterate from top to bottom for LIFO display
        Stack<String> temp = new Stack<>();
        temp.addAll(rollbackStack);
        int position = 1;
        while (!temp.isEmpty()) {
            System.out.println("    [" + position + "] " + temp.pop()
                    + (position == 1 ? "  ← TOP (most recent)" : ""));
            position++;
        }
    }

    /**
     * Displays the full cancellation log in chronological order.
     * Read-only — log is not modified during display.
     */
    public void displayCancellationLog() {
        System.out.println("  Cancellation Log:");
        if (cancellationLog.isEmpty()) {
            System.out.println("    (no cancellations recorded)");
            return;
        }
        System.out.println("  " + "-".repeat(65));
        for (CancellationRecord c : cancellationLog) {
            System.out.println(c);
        }
    }

    /**
     * Returns the total number of successful cancellations performed.
     *
     * @return cancellation count
     */
    public int getTotalCancellations() {
        return cancellationCounter;
    }

    /**
     * Returns the number of room IDs currently held in the rollback stack.
     *
     * @return rollback stack size
     */
    public int getRollbackStackSize() {
        return rollbackStack.size();
    }

    /**
     * Peeks at the top of the rollback stack without removing it.
     * Returns the most recently released room ID.
     *
     * @return room ID at the top of the stack, or null if empty
     */
    public String peekRollbackStack() {
        return rollbackStack.isEmpty() ? null : rollbackStack.peek();
    }
}