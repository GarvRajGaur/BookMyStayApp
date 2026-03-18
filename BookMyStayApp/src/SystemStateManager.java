import java.util.List;
import java.util.Map;

/**
 * SystemStateManager
 *
 * Use Case 12: Data Persistence & System Recovery
 *
 * Orchestrates the complete save and restore cycle for all critical
 * system state components. Acts as the single entry point for both
 * system shutdown (save) and system startup (restore).
 *
 * Components persisted:
 *   1. Room inventory availability map  → inventory_state.txt
 *   2. Booking history records          → booking_history.txt
 *
 * Why a dedicated orchestrator class?
 * Individual persistence services only know about their own file.
 * SystemStateManager coordinates them in the correct order and ensures
 * that a partial save or restore failure in one component does not
 * silently corrupt the other. It also provides a clean API:
 *   - systemStateManager.saveAll(...)   → shutdown path
 *   - systemStateManager.restoreAll()  → startup path
 *
 * This class mirrors the role of a SessionManager or StateController
 * in production systems that coordinate multiple data sources.
 *
 * @author GARV RAJ
 * @version 12.0
 */
public class SystemStateManager {

    private final InventoryPersistenceService     inventoryService;
    private final BookingHistoryPersistenceService historyService;

    /**
     * Constructs the SystemStateManager wiring both persistence services.
     */
    public SystemStateManager() {
        this.inventoryService = new InventoryPersistenceService();
        this.historyService   = new BookingHistoryPersistenceService();
    }

    /**
     * SHUTDOWN PATH — Saves all critical system state to disk.
     *
     * Saves in order:
     *   1. Inventory snapshot   (availability counts per room type)
     *   2. Booking history      (all confirmed BookingRecord entries)
     *
     * If either save fails, the error is logged but the other save
     * still proceeds — partial persistence is better than none.
     *
     * @param inventory the current RoomInventory to persist
     * @param history   the current BookingHistory to persist
     */
    public void saveAll(RoomInventory inventory, BookingHistory history) {
        System.out.println("  Initiating system state save (shutdown)...");
        System.out.println("  ------------------------------------------");

        // Step 1: Save inventory snapshot
        Map<String, Integer> snapshot = inventory.getAvailabilityMap();
        inventoryService.saveInventory(snapshot);

        // Step 2: Save booking history
        historyService.saveBookingHistory(history);

        System.out.println("  ------------------------------------------");
        System.out.println("  System state saved successfully.");
    }

    /**
     * STARTUP PATH — Restores all system state from disk.
     *
     * Restore order:
     *   1. Inventory — loaded and applied to a fresh RoomInventory
     *   2. Booking history — loaded into a new BookingHistory object
     *
     * Failure Tolerance:
     * If inventory file is missing → RoomInventory starts with defaults.
     * If history file is missing   → BookingHistory starts empty.
     * In both cases the system remains operational — no crash.
     *
     * @return RestoredState containing recovered inventory and history
     */
    public RestoredState restoreAll() {
        System.out.println("  Initiating system state restore (startup)...");
        System.out.println("  ------------------------------------------");

        // Step 1: Restore inventory
        RoomInventory restoredInventory = new RoomInventory();
        Map<String, Integer> savedMap   = inventoryService.loadInventory();

        if (savedMap != null && !savedMap.isEmpty()) {
            // Apply saved counts by calculating the delta from the defaults
            // Default RoomInventory starts with Single=5, Double=3, Suite=2
            // We need to set it to exactly the saved values
            restoredInventory = buildInventoryFromSnapshot(savedMap);
            System.out.println("  [RESTORED] Inventory loaded from file.");
        } else {
            System.out.println("  [DEFAULT]  Inventory initialized with defaults.");
        }

        // Step 2: Restore booking history
        BookingHistory restoredHistory = new BookingHistory();
        List<BookingRecord> savedRecords = historyService.loadBookingHistory();

        if (!savedRecords.isEmpty()) {
            for (BookingRecord record : savedRecords) {
                restoredHistory.addRecord(
                        record.getGuestName(),
                        record.getRoomType(),
                        record.getRoomId()
                );
            }
            System.out.println("  [RESTORED] Booking history loaded from file.");
        } else {
            System.out.println("  [DEFAULT]  Booking history starts empty.");
        }

        System.out.println("  ------------------------------------------");
        System.out.println("  System state restored successfully.");

        return new RestoredState(restoredInventory, restoredHistory);
    }

    /**
     * Constructs a RoomInventory with exactly the saved availability counts.
     *
     * RoomInventory defaults to Single=5, Double=3, Suite=2.
     * We compute the delta needed to reach each saved count and apply it.
     *
     * @param snapshot the saved availability map
     * @return RoomInventory with counts matching the snapshot exactly
     */
    private RoomInventory buildInventoryFromSnapshot(Map<String, Integer> snapshot) {
        // Default starting counts — must match RoomInventory constructor
        java.util.Map<String, Integer> defaults = new java.util.HashMap<>();
        defaults.put("Single", 5);
        defaults.put("Double", 3);
        defaults.put("Suite",  2);

        RoomInventory inv = new RoomInventory();
        for (Map.Entry<String, Integer> entry : snapshot.entrySet()) {
            String roomType   = entry.getKey();
            int    savedCount = entry.getValue();
            int    defaultVal = defaults.getOrDefault(roomType, 0);
            int    delta      = savedCount - defaultVal;
            if (delta != 0) {
                inv.updateAvailability(roomType, delta);
            }
        }
        return inv;
    }

    // -------------------------------------------------------------------
    // Inner class: RestoredState
    // Bundles the two restored components as a single return value
    // -------------------------------------------------------------------

    /**
     * RestoredState
     *
     * Simple value holder returned by restoreAll().
     * Bundles the recovered inventory and history so the caller
     * receives both in a single, clearly typed return value.
     */
    public static class RestoredState {
        public final RoomInventory  inventory;
        public final BookingHistory history;

        public RestoredState(RoomInventory inventory, BookingHistory history) {
            this.inventory = inventory;
            this.history   = history;
        }
    }
}