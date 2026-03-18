import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * SynchronizedInventory
 *
 * Use Case 11: Concurrent Booking Simulation (Thread Safety)
 *
 * A thread-safe room inventory that wraps availability data in
 * synchronized methods, guaranteeing consistent reads and writes
 * under concurrent access from multiple booking threads.
 *
 * The Critical Race Condition it prevents:
 *
 *   WITHOUT synchronization (unsynchronized inventory):
 *   ┌─────────────────────────────────────────────────────────┐
 *   │ Thread A: reads  availability["Single"] = 1  (count > 0)│
 *   │ Thread B: reads  availability["Single"] = 1  (count > 0)│
 *   │ Thread A: writes availability["Single"] = 0  (decrements)│
 *   │ Thread B: writes availability["Single"] = 0  (decrements)│
 *   │ RESULT: Two rooms allocated, inventory now shows -1 !   │
 *   └─────────────────────────────────────────────────────────┘
 *
 *   WITH synchronization (this class):
 *   ┌─────────────────────────────────────────────────────────┐
 *   │ Thread A: enters synchronized block, locks the object   │
 *   │ Thread A: reads availability["Single"] = 1 → allocates  │
 *   │ Thread A: writes availability["Single"] = 0 → exits     │
 *   │ Thread B: acquires lock, reads availability["Single"] = 0│
 *   │ Thread B: count = 0 → booking DECLINED safely           │
 *   │ RESULT: Only one room allocated. Inventory stays at 0.  │
 *   └─────────────────────────────────────────────────────────┘
 *
 * Key design:
 * checkAndAllocate() combines the READ (check availability) and
 * WRITE (decrement) as ONE atomic synchronized operation.
 * Splitting these into two separate synchronized methods would
 * still create a TOCTOU race between the two calls.
 *
 * @author GARV RAJ
 * @version 11.0
 */
public class SynchronizedInventory {

    // Shared mutable state — accessed by all booking threads
    private final Map<String, Integer> availabilityMap;

    /**
     * Initializes inventory with default room counts.
     *
     * @param singleCount number of Single rooms available
     * @param doubleCount number of Double rooms available
     * @param suiteCount  number of Suite rooms available
     */
    public SynchronizedInventory(int singleCount, int doubleCount, int suiteCount) {
        availabilityMap = new HashMap<>();
        availabilityMap.put("Single", singleCount);
        availabilityMap.put("Double", doubleCount);
        availabilityMap.put("Suite",  suiteCount);
    }

    /**
     * Atomically checks availability and decrements inventory in one
     * synchronized operation — the CRITICAL SECTION of the booking system.
     *
     * This method is the core protection against double-booking under
     * concurrency. Because both the check AND the decrement happen inside
     * a single synchronized block, no other thread can interleave between
     * reading the count and writing the update.
     *
     * @param roomType the type of room to allocate
     * @return true if allocation succeeded (room was available),
     *         false if no rooms of this type are available
     */
    public synchronized boolean checkAndAllocate(String roomType) {
        Integer count = availabilityMap.get(roomType);
        if (count == null || count <= 0) {
            return false; // No rooms available — decline without state change
        }
        availabilityMap.put(roomType, count - 1); // Atomic decrement
        return true;
    }

    /**
     * Atomically restores inventory for a room type (used by cancellation).
     * Synchronized to prevent concurrent cancellations corrupting the count.
     *
     * @param roomType the type of room being released
     */
    public synchronized void restoreAvailability(String roomType) {
        availabilityMap.merge(roomType, 1, Integer::sum);
    }

    /**
     * Returns a thread-safe snapshot of current availability.
     * Returns an unmodifiable copy so callers cannot mutate the map.
     *
     * @return unmodifiable snapshot of availability counts
     */
    public synchronized Map<String, Integer> getSnapshot() {
        return Collections.unmodifiableMap(new HashMap<>(availabilityMap));
    }

    /**
     * Displays the current inventory state.
     * Synchronized so the display is consistent (not mid-update).
     */
    public synchronized void displayInventory() {
        System.out.println("  Current Room Availability:");
        for (Map.Entry<String, Integer> entry : availabilityMap.entrySet()) {
            System.out.println("    " + entry.getKey()
                    + " Rooms : " + entry.getValue() + " available");
        }
    }
}