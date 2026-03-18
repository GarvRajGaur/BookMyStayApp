/**
 * MAIN CLASS UseCase12DataPersistenceRecovery
 *
 * Use Case 12: Data Persistence & System Recovery
 *
 * Description:
 * This class simulates the full lifecycle of a stateful application —
 * from initial operation through graceful shutdown (save) to system
 * restart with full state recovery (restore).
 *
 * New classes introduced:
 *   - PersistenceConstants:              file paths and format constants
 *   - InventoryPersistenceService:       save/load inventory → inventory_state.txt
 *   - BookingHistoryPersistenceService:  save/load history   → booking_history.txt
 *   - SystemStateManager:               orchestrates full save + restore cycle
 *   - SystemStateManager.RestoredState: bundles recovered inventory + history
 *
 * Persistence Format (plain text, pipe-delimited):
 *   inventory_state.txt  → "RoomType|Count"   per line
 *   booking_history.txt  → "Index|Guest|Type|RoomId" per line
 *
 * Lifecycle Demonstrated:
 *   Phase 1 — INITIAL RUN
 *     - Initialize inventory and process bookings (UC6 flow)
 *     - System is running with confirmed state in memory
 *
 *   Phase 2 — SHUTDOWN (SAVE)
 *     - SystemStateManager.saveAll() serializes inventory + history to files
 *     - Files are written to disk — state is now durable
 *
 *   Phase 3 — RESTART (RESTORE)
 *     - All in-memory objects are discarded (simulating JVM restart)
 *     - SystemStateManager.restoreAll() reads files back into new objects
 *     - Restored state is verified against the pre-shutdown state
 *
 *   Phase 4 — POST-RECOVERY OPERATION
 *     - System continues accepting new bookings on restored state
 *     - New booking is confirmed using the recovered inventory
 *
 *   Phase 5 — FAILURE TOLERANCE TEST
 *     - Restoring from non-existent files is attempted
 *     - System initializes with safe defaults — no crash
 *
 * @author GARV RAJ
 * @version 12.0
 */
public class UseCase12DataPersistenceRecovery {

    public static void main(String[] args) throws InterruptedException {

        System.out.println("=================================================");
        System.out.println("   BookMyStay Hotel Booking System               ");
        System.out.println("   Use Case 12: Data Persistence & Recovery      ");
        System.out.println("=================================================");
        System.out.println();

        // ═══════════════════════════════════════════════════════════════
        // PHASE 1: INITIAL RUN — build state through normal booking flow
        // ═══════════════════════════════════════════════════════════════
        System.out.println("╔═════════════════════════════════════════════╗");
        System.out.println("║  PHASE 1: Initial System Run                ║");
        System.out.println("╚═════════════════════════════════════════════╝");
        System.out.println();

        // Initialize inventory with controlled stock
        RoomInventory inventory = new RoomInventory();
        inventory.updateAvailability("Single", -3); // 5 → 2
        inventory.updateAvailability("Double", -1); // 3 → 2
        inventory.updateAvailability("Suite",  -1); // 2 → 1

        // Queue and process booking requests
        BookingRequestQueue requestQueue = new BookingRequestQueue();
        requestQueue.addRequest(new Reservation("Alice Johnson", "Single"));
        requestQueue.addRequest(new Reservation("Bob Smith",    "Double"));
        requestQueue.addRequest(new Reservation("Carol White",  "Suite"));
        requestQueue.addRequest(new Reservation("David Brown",  "Single"));

        AllocationTracker tracker  = new AllocationTracker();
        BookingHistory    history  = new BookingHistory();
        BookingService    bookSvc  = new BookingService(
                requestQueue, inventory, tracker, history);

        System.out.println("--------------------------------------------------");
        System.out.println("Processing bookings...");
        System.out.println("--------------------------------------------------");
        bookSvc.processAllRequests();
        System.out.println();

        System.out.println("Pre-Shutdown System State:");
        System.out.println("  Inventory:");
        inventory.displayInventory();
        System.out.println("  Booking History:");
        history.displayHistory();
        System.out.println();

        // ═══════════════════════════════════════════════════════════════
        // PHASE 2: SHUTDOWN — serialize and save state to files
        // ═══════════════════════════════════════════════════════════════
        System.out.println("╔═════════════════════════════════════════════╗");
        System.out.println("║  PHASE 2: System Shutdown — Saving State    ║");
        System.out.println("╚═════════════════════════════════════════════╝");
        System.out.println();

        SystemStateManager stateManager = new SystemStateManager();
        stateManager.saveAll(inventory, history);
        System.out.println();

        // Show what was written to each file
        System.out.println("Contents of " + PersistenceConstants.INVENTORY_FILE + ":");
        printFile(PersistenceConstants.INVENTORY_FILE);
        System.out.println();
        System.out.println("Contents of " + PersistenceConstants.BOOKING_HISTORY_FILE + ":");
        printFile(PersistenceConstants.BOOKING_HISTORY_FILE);
        System.out.println();

        // Simulate JVM shutdown — discard all in-memory objects
        System.out.println("  [SHUTDOWN] All in-memory objects discarded.");
        System.out.println("  [SHUTDOWN] JVM terminated (simulated).");
        inventory = null;
        history   = null;
        tracker   = null;
        System.out.println();

        // ═══════════════════════════════════════════════════════════════
        // PHASE 3: RESTART — restore state from files
        // ═══════════════════════════════════════════════════════════════
        System.out.println("╔═════════════════════════════════════════════╗");
        System.out.println("║  PHASE 3: System Restart — Restoring State  ║");
        System.out.println("╚═════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("  [STARTUP]  JVM started (simulated).");
        System.out.println("  [STARTUP]  Loading persisted state from disk...");
        System.out.println();

        SystemStateManager restoreManager = new SystemStateManager();
        SystemStateManager.RestoredState restored = restoreManager.restoreAll();

        // Assign recovered state to working variables
        RoomInventory recoveredInventory = restored.inventory;
        BookingHistory recoveredHistory  = restored.history;
        System.out.println();

        // ═══════════════════════════════════════════════════════════════
        // PHASE 3b: VERIFY — confirm restored state matches pre-shutdown
        // ═══════════════════════════════════════════════════════════════
        System.out.println("--------------------------------------------------");
        System.out.println("Verification — Restored State vs Pre-Shutdown:");
        System.out.println("--------------------------------------------------");
        System.out.println("  Restored Inventory:");
        recoveredInventory.displayInventory();
        System.out.println();
        System.out.println("  Restored Booking History:");
        recoveredHistory.displayHistory();
        System.out.println();
        System.out.println("  Verification Result:");
        System.out.println("    Booking records recovered : "
                + recoveredHistory.getTotalBookings() + " (expected: 4)  "
                + (recoveredHistory.getTotalBookings() == 4 ? "✓ MATCH" : "✗ MISMATCH"));

        // ═══════════════════════════════════════════════════════════════
        // PHASE 4: POST-RECOVERY — continue operating on restored state
        // ═══════════════════════════════════════════════════════════════
        System.out.println();
        System.out.println("╔═════════════════════════════════════════════╗");
        System.out.println("║  PHASE 4: Post-Recovery Operation           ║");
        System.out.println("╚═════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("  New booking request on recovered inventory...");
        System.out.println("--------------------------------------------------");

        BookingValidator validator = new BookingValidator();
        try {
            validator.validateBookingRequest("Eva Green", "Double", recoveredInventory);
            recoveredInventory.updateAvailability("Double", -1);
            recoveredHistory.addRecord("Eva Green", "Double", "DOUBLE-202");
            System.out.println("  [CONFIRMED] Guest: Eva Green | Room: DOUBLE-202");
        } catch (InvalidGuestNameException | InvalidRoomTypeException
                 | RoomNotAvailableException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }

        System.out.println();
        System.out.println("  Final Inventory After Post-Recovery Booking:");
        recoveredInventory.displayInventory();
        System.out.println();
        System.out.println("  Final Booking History (including new booking):");
        recoveredHistory.displayHistory();

        // ═══════════════════════════════════════════════════════════════
        // PHASE 5: FAILURE TOLERANCE — restore from non-existent files
        // ═══════════════════════════════════════════════════════════════
        System.out.println();
        System.out.println("╔═════════════════════════════════════════════╗");
        System.out.println("║  PHASE 5: Failure Tolerance Test            ║");
        System.out.println("╚═════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("  Attempting restore from non-existent files...");
        System.out.println("--------------------------------------------------");

        // Delete the persistence files to simulate missing data
        new java.io.File(PersistenceConstants.INVENTORY_FILE).delete();
        new java.io.File(PersistenceConstants.BOOKING_HISTORY_FILE).delete();

        SystemStateManager failSafeManager = new SystemStateManager();
        SystemStateManager.RestoredState defaultState = failSafeManager.restoreAll();

        System.out.println();
        System.out.println("  System state after recovery attempt:");
        System.out.println("  Inventory (defaults):");
        defaultState.inventory.displayInventory();
        System.out.println("  Booking history records: "
                + defaultState.history.getTotalBookings()
                + " (empty — correct for missing file)");
        System.out.println("  System is operational despite missing files ✓");

        // ═══════════════════════════════════════════════════════════════
        // SUMMARY
        // ═══════════════════════════════════════════════════════════════
        System.out.println();
        System.out.println("=================================================");
        System.out.println("Use Case 12 Complete.");
        System.out.println("  Phase 1 — Initial run:    4 bookings confirmed");
        System.out.println("  Phase 2 — Shutdown:       state serialized to 2 files");
        System.out.println("  Phase 3 — Restart:        state fully recovered");
        System.out.println("  Phase 4 — Post-recovery:  new booking on restored state");
        System.out.println("  Phase 5 — Fault tolerance: missing files handled safely");
        System.out.println("=================================================");
    }

    /**
     * Reads and prints the content of a file to console.
     * Used to show exactly what was serialized to disk.
     */
    private static void printFile(String filename) {
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("    " + line);
            }
        } catch (java.io.IOException e) {
            System.out.println("    [Could not read file: " + e.getMessage() + "]");
        }
    }
}