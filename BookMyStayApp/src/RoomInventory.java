import java.util.HashMap;
import java.util.Map;

/**
 * RoomInventory
 *
 * Centralized inventory management class that holds availability counts
 * for each room type using a HashMap.
 *
 * Introduced in Use Case 3 and extended in Use Case 4 to support
 * read-only access patterns via the SearchService.
 *
 * @author GARV RAJ
 * @version 3.0
 */
public class RoomInventory {

    // Maps room type name to available count
    private HashMap<String, Integer> availabilityMap;

    public RoomInventory() {
        availabilityMap = new HashMap<>();
        // Default initial stock
        availabilityMap.put("Single", 5);
        availabilityMap.put("Double", 3);
        availabilityMap.put("Suite", 2);
    }

    /**
     * Returns a read-only view of the availability map.
     * Used by SearchService to safely access inventory data
     * without risk of modification.
     *
     * @return unmodifiable map of room type to availability count
     */
    public Map<String, Integer> getAvailabilityMap() {
        return java.util.Collections.unmodifiableMap(availabilityMap);
    }

    /**
     * Updates availability for a given room type.
     * Negative delta represents a booking; positive represents a release/cancellation.
     *
     * @param roomType the type of room (e.g., "Single", "Double", "Suite")
     * @param delta    the change in availability count
     */
    public void updateAvailability(String roomType, int delta) {
        if (availabilityMap.containsKey(roomType)) {
            int current = availabilityMap.get(roomType);
            int updated = current + delta;
            if (updated < 0) {
                System.out.println("Error: Not enough availability for " + roomType + " rooms.");
            } else {
                availabilityMap.put(roomType, updated);
                System.out.println("Updated " + roomType + " availability to: " + updated);
            }
        } else {
            System.out.println("Error: Room type '" + roomType + "' not found in inventory.");
        }
    }

    /**
     * Displays the full inventory state to the console.
     */
    public void displayInventory() {
        System.out.println("Current Room Availability:");
        for (Map.Entry<String, Integer> entry : availabilityMap.entrySet()) {
            System.out.println("  " + entry.getKey() + " Rooms: " + entry.getValue() + " available");
        }
    }
}