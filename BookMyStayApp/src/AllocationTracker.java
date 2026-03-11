import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * AllocationTracker
 *
 * Use Case 6: Reservation Confirmation & Room Allocation
 *
 * Tracks all allocated room IDs to guarantee uniqueness across
 * every booking in the system. Uses a nested data structure:
 *
 *   HashMap<String, Set<String>>
 *   └── Key:   Room type  (e.g., "Single", "Double", "Suite")
 *   └── Value: Set of assigned room IDs for that type
 *              (e.g., { "SINGLE-101", "SINGLE-102" })
 *
 * Why HashMap<String, Set<String>>?
 * - HashMap provides O(1) lookup per room type
 * - Set enforces uniqueness — the same room ID cannot exist twice
 * - Grouping by type allows per-type reporting and validation
 * - Together they eliminate double-booking by design, not by runtime checks alone
 *
 * @author GARV RAJ
 * @version 6.0
 */
public class AllocationTracker {

    // Maps each room type to the set of room IDs already assigned
    private HashMap<String, Set<String>> allocatedRooms;

    /**
     * Initializes the tracker with empty sets for each known room type.
     */
    public AllocationTracker() {
        allocatedRooms = new HashMap<>();
        allocatedRooms.put("Single", new HashSet<>());
        allocatedRooms.put("Double", new HashSet<>());
        allocatedRooms.put("Suite",  new HashSet<>());
    }

    /**
     * Attempts to record a room ID assignment for a given room type.
     *
     * The Set's add() method returns false if the ID already exists,
     * making duplicate detection automatic — no manual loop or comparison needed.
     *
     * @param roomType the type of room being allocated
     * @param roomId   the unique room ID being assigned
     * @return true if the room ID was successfully recorded (unique),
     *         false if the ID was already allocated (duplicate detected)
     */
    public boolean allocate(String roomType, String roomId) {
        Set<String> assigned = allocatedRooms.get(roomType);
        if (assigned == null) {
            // Unknown room type — initialize a new set defensively
            assigned = new HashSet<>();
            allocatedRooms.put(roomType, assigned);
        }
        // Set.add() returns false if element already exists → duplicate caught
        return assigned.add(roomId);
    }

    /**
     * Checks whether a given room ID has already been allocated,
     * regardless of room type.
     *
     * @param roomId the room ID to check
     * @return true if already allocated, false if available
     */
    public boolean isAllocated(String roomId) {
        for (Set<String> ids : allocatedRooms.values()) {
            if (ids.contains(roomId)) return true;
        }
        return false;
    }

    /**
     * Returns the total number of rooms allocated across all types.
     *
     * @return total allocation count
     */
    public int getTotalAllocated() {
        int total = 0;
        for (Set<String> ids : allocatedRooms.values()) {
            total += ids.size();
        }
        return total;
    }

    /**
     * Displays a full report of all allocated room IDs grouped by room type.
     * Read-only — does not modify any allocation state.
     */
    public void displayAllocations() {
        System.out.println("Allocated Room IDs by Type:");
        for (Map.Entry<String, Set<String>> entry : allocatedRooms.entrySet()) {
            String type  = entry.getKey();
            Set<String> ids = entry.getValue();
            if (ids.isEmpty()) {
                System.out.println("  " + type + " Rooms : (none allocated)");
            } else {
                System.out.println("  " + type + " Rooms : " + ids);
            }
        }
    }
}