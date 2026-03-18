/**
 * MAIN CLASS UseCase10BookingCancellation
 *
 * Use Case 10: Booking Cancellation & Inventory Rollback
 *
 * Description:
 * This class demonstrates safe cancellation of confirmed bookings with
 * full inventory rollback. Each cancellation reverses all state changes
 * from the original allocation — in a strict, controlled order — using
 * a Stack<String> to track released room IDs in LIFO order.
 *
 * New classes introduced:
 *   - CancellationRecord:  immutable record of a single cancellation event
 *   - CancellationService: validates + performs controlled rollback using Stack
 *
 * Why Stack (LIFO) for rollback?
 * The most recently cancelled room is always the top of the stack —
 * naturally modeling an undo operation. If a room is re-allocated after
 * cancellation, the most recently freed room (top of stack) is the
 * natural first candidate.
 *
 * Scenarios Demonstrated:
 *   Scenario 1 — Cancel a confirmed booking          → CANCELLED + inventory restored
 *   Scenario 2 — Cancel a second confirmed booking   → CANCELLED + stack grows
 *   Scenario 3 — Cancel a non-existent room ID       → REJECTED  (no state change)
 *   Scenario 4 — Cancel already-cancelled booking    → REJECTED  (no state change)
 *   Scenario 5 — Cancel a third booking              → CANCELLED + LIFO order shown
 *   Verify     — New booking on restored inventory   → CONFIRMED (system stable)
 *
 * @author GARV RAJ
 * @version 10.0
 */
public class UseCase10BookingCancellation {

    public static void main(String[] args) {

        System.out.println("=================================================");
        System.out.println("   BookMyStay Hotel Booking System               ");
        System.out.println("   Use Case 10: Booking Cancellation &           ");
        System.out.println("               Inventory Rollback                ");
        System.out.println("=================================================");
        System.out.println();

        // ---------------------------------------------------------------
        // Step 1: Initialize inventory and run full allocation (UC6)
        // ---------------------------------------------------------------
        RoomInventory inventory = new RoomInventory();
        inventory.updateAvailability("Single", -3); // 5 → 2
        inventory.updateAvailability("Double", -2); // 3 → 1
        inventory.updateAvailability("Suite",  -1); // 2 → 1

        BookingRequestQueue requestQueue = new BookingRequestQueue();
        requestQueue.addRequest(new Reservation("Alice Johnson", "Single"));
        requestQueue.addRequest(new Reservation("Bob Smith",    "Double"));
        requestQueue.addRequest(new Reservation("Carol White",  "Suite"));
        requestQueue.addRequest(new Reservation("David Brown",  "Single"));
        requestQueue.addRequest(new Reservation("Eva Green",    "Double")); // declined

        AllocationTracker tracker      = new AllocationTracker();
        BookingHistory    history      = new BookingHistory();
        BookingService    bookingService = new BookingService(
                requestQueue, inventory, tracker, history);

        System.out.println("--------------------------------------------------");
        System.out.println("Running Allocation (UC6)...");
        System.out.println("--------------------------------------------------");
        bookingService.processAllRequests();
        System.out.println();

        // Confirmed after allocation:
        //   Alice Johnson  → SINGLE-101
        //   Bob Smith      → DOUBLE-201
        //   Carol White    → SUITE-301
        //   David Brown    → SINGLE-102
        //   Eva Green      → DECLINED

        System.out.println("Inventory After Allocation:");
        inventory.displayInventory();
        System.out.println();
        System.out.println("Booking History After Allocation:");
        history.displayHistory();
        System.out.println();

        // ---------------------------------------------------------------
        // Step 2: Initialize CancellationService
        // Indexes all confirmed bookings from history into lookup map
        // ---------------------------------------------------------------
        CancellationService cancellationService =
                new CancellationService(inventory, history);

        System.out.println("=================================================");
        System.out.println("Running Cancellation Scenarios...");
        System.out.println("=================================================");

        // ---------------------------------------------------------------
        // Scenario 1: Cancel Alice's confirmed booking (SINGLE-101)
        // ---------------------------------------------------------------
        System.out.println();
        System.out.println("Scenario 1: Cancel confirmed booking SINGLE-101 (Alice Johnson)");
        System.out.println("--------------------------------------------------");
        cancellationService.cancelBooking("SINGLE-101");

        System.out.println();
        System.out.println("  Inventory after Scenario 1:");
        inventory.displayInventory();
        System.out.println();
        System.out.println("  Rollback Stack after Scenario 1:");
        cancellationService.displayRollbackStack();

        // ---------------------------------------------------------------
        // Scenario 2: Cancel Bob's confirmed booking (DOUBLE-201)
        // ---------------------------------------------------------------
        System.out.println();
        System.out.println("Scenario 2: Cancel confirmed booking DOUBLE-201 (Bob Smith)");
        System.out.println("--------------------------------------------------");
        cancellationService.cancelBooking("DOUBLE-201");

        System.out.println();
        System.out.println("  Inventory after Scenario 2:");
        inventory.displayInventory();
        System.out.println();
        System.out.println("  Rollback Stack after Scenario 2 (LIFO — DOUBLE-201 on top):");
        cancellationService.displayRollbackStack();

        // ---------------------------------------------------------------
        // Scenario 3: Attempt to cancel a non-existent room ID
        // ---------------------------------------------------------------
        System.out.println();
        System.out.println("Scenario 3: Cancel non-existent room ID \"SINGLE-999\"");
        System.out.println("--------------------------------------------------");
        cancellationService.cancelBooking("SINGLE-999");

        System.out.println();
        System.out.println("  Inventory after Scenario 3 (must be unchanged):");
        inventory.displayInventory();

        // ---------------------------------------------------------------
        // Scenario 4: Attempt to cancel already-cancelled SINGLE-101
        // ---------------------------------------------------------------
        System.out.println();
        System.out.println("Scenario 4: Attempt to cancel already-cancelled \"SINGLE-101\"");
        System.out.println("--------------------------------------------------");
        cancellationService.cancelBooking("SINGLE-101");

        System.out.println();
        System.out.println("  Inventory after Scenario 4 (must be unchanged):");
        inventory.displayInventory();

        // ---------------------------------------------------------------
        // Scenario 5: Cancel Carol's Suite (SUITE-301)
        // ---------------------------------------------------------------
        System.out.println();
        System.out.println("Scenario 5: Cancel confirmed booking SUITE-301 (Carol White)");
        System.out.println("--------------------------------------------------");
        cancellationService.cancelBooking("SUITE-301");

        System.out.println();
        System.out.println("  Inventory after Scenario 5:");
        inventory.displayInventory();
        System.out.println();
        System.out.println("  Rollback Stack after Scenario 5 (LIFO order):");
        cancellationService.displayRollbackStack();

        // ---------------------------------------------------------------
        // Step 3: Full cancellation log and stack summary
        // ---------------------------------------------------------------
        System.out.println();
        System.out.println("=================================================");
        System.out.println("Cancellation Log (all confirmed cancellations):");
        System.out.println("=================================================");
        cancellationService.displayCancellationLog();
        System.out.println("  Total cancellations: "
                + cancellationService.getTotalCancellations());
        System.out.println("  Rollback stack size: "
                + cancellationService.getRollbackStackSize());
        System.out.println("  Top of stack (most recently cancelled): "
                + cancellationService.peekRollbackStack());

        // ---------------------------------------------------------------
        // Step 4: Verify — new booking succeeds on restored inventory
        // Proves the system is fully stable after cancellations
        // ---------------------------------------------------------------
        System.out.println();
        System.out.println("=================================================");
        System.out.println("System Stability Check:");
        System.out.println("New booking on restored inventory...");
        System.out.println("=================================================");

        BookingValidator validator = new BookingValidator();
        try {
            validator.validateBookingRequest("Frank Lee", "Double", inventory);
            inventory.updateAvailability("Double", -1);
            history.addRecord("Frank Lee", "Double", "DOUBLE-202");
            System.out.println("  [CONFIRMED] Guest: Frank Lee"
                    + " | Room Type: Double | Assigned Room ID: DOUBLE-202");
        } catch (InvalidGuestNameException | InvalidRoomTypeException
                 | RoomNotAvailableException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }

        // ---------------------------------------------------------------
        // Step 5: Final state summary
        // ---------------------------------------------------------------
        System.out.println();
        System.out.println("=================================================");
        System.out.println("Final System State:");
        System.out.println("=================================================");
        System.out.println();
        System.out.println("Final Inventory:");
        inventory.displayInventory();
        System.out.println();
        System.out.println("Final Booking History:");
        history.displayHistory();
        System.out.println();
        System.out.println("--------------------------------------------------");
        System.out.println("Summary:");
        System.out.println("  Bookings confirmed : " + history.getTotalBookings());
        System.out.println("  Bookings cancelled : "
                + cancellationService.getTotalCancellations());
        System.out.println("  Rollback stack size: "
                + cancellationService.getRollbackStackSize());
        System.out.println("  System remained stable across all scenarios.");
        System.out.println("--------------------------------------------------");
        System.out.println("Use Case 10 Complete.");
        System.out.println("=================================================");
    }
}