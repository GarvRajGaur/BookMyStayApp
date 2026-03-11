import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SearchService
 *
 * Use Case 4: Room Search & Availability Check
 *
 * Responsibilities:
 * - Retrieves current availability from RoomInventory (read-only)
 * - Matches available room types to their Room domain objects
 * - Filters out room types with zero availability
 * - Displays room details and pricing for available options
 *
 * This class DOES NOT modify inventory or trigger any booking logic.
 * It enforces a strict separation between read access and write operations.
 *
 * Key Concepts Applied:
 * - Read-Only Access:   Uses getAvailabilityMap() which returns an unmodifiable map
 * - Defensive Programming: Validates availability > 0 before displaying
 * - Separation of Concerns: No booking or inventory mutation logic here
 * - Domain Model Usage: Delegates room detail display to Room objects
 *
 * @author GARV RAJ
 * @version 4.0
 */
public class RoomSearchService {

    private RoomInventory inventory;
    private List<Room> roomCatalog;

    /**
     * Constructs a SearchService with a reference to the shared inventory
     * and the hotel's room catalog.
     *
     * @param inventory   the centralized room inventory (read access only)
     * @param roomCatalog list of Room objects representing all room types
     */
    public SearchService(RoomInventory inventory, List<Room> roomCatalog) {
        this.inventory = inventory;
        this.roomCatalog = roomCatalog;
    }

    /**
     * Performs a read-only search of available rooms.
     *
     * Steps:
     * 1. Retrieve availability data from inventory (unmodifiable)
     * 2. Iterate over room catalog
     * 3. Check each room type's availability count
     * 4. Skip rooms with zero or missing availability
     * 5. Display details for all available rooms
     *
     * System state is guaranteed to remain unchanged after this operation.
     */
    public void searchAvailableRooms() {
        System.out.println("Searching for available rooms...");
        System.out.println("--------------------------------------------------");

        // Step 1: Read-only access to inventory — no modification possible
        Map<String, Integer> availabilitySnapshot = inventory.getAvailabilityMap();

        boolean anyAvailable = false;

        // Step 2-5: Match rooms to availability, filter, and display
        for (Room room : roomCatalog) {
            String type = room.getRoomType();
            Integer count = availabilitySnapshot.get(type);

            // Defensive check: skip if type not in inventory or count is zero
            if (count == null || count <= 0) {
                System.out.println("[Unavailable] Room Type: " + type + " — No rooms currently available.");
                continue;
            }

            // Display available room info
            System.out.print("[Available]   ");
            room.displayDetails();
            System.out.println("              Rooms Left: " + count);
            anyAvailable = true;
        }

        System.out.println("--------------------------------------------------");

        if (!anyAvailable) {
            System.out.println("No rooms are currently available.");
        } else {
            System.out.println("Search complete. Inventory was not modified.");
        }
    }
}