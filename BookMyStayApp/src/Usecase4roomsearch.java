import java.util.ArrayList;
import java.util.List;

/**
 * MAIN CLASS UseCase4RoomSearch
 *
 * Use Case 4: Room Search & Availability Check
 *
 * Description:
 * This class demonstrates safe, read-only access to the room inventory.
 * A guest initiates a room search. The SearchService reads availability
 * data and room details without modifying any system state.
 *
 * This use case builds on Use Case 3's centralized inventory by introducing
 * a strict separation between read operations (search) and write operations
 * (booking/inventory updates).
 *
 * Flow Demonstrated:
 * 1. Initialize room inventory with availability counts
 * 2. Build room catalog (domain objects with pricing and amenities)
 * 3. Simulate a booking to make one room type unavailable
 * 4. Guest triggers a room search via SearchService
 * 5. SearchService reads and filters inventory — state unchanged
 *
 * @author GARV RAJ
 * @version 4.0
 */
public class Usecase4roomsearch {

    public static void main(String[] args) {

        System.out.println("=== BookMyStay Hotel Booking System ===");
        System.out.println("Use Case 4: Room Search & Availability Check");
        System.out.println("--------------------------------------------------");

        // ---------------------------------------------------------------
        // Step 1: Initialize centralized inventory (from Use Case 3)
        // ---------------------------------------------------------------
        RoomInventory inventory = new RoomInventory();

        System.out.println("Initial Inventory State:");
        inventory.displayInventory();
        System.out.println();

        // ---------------------------------------------------------------
        // Step 2: Build room catalog — one representative Room per type
        // These domain objects carry pricing and amenity information
        // ---------------------------------------------------------------
        List<Room> roomCatalog = new ArrayList<>();
        roomCatalog.add(new SingleRoom(101, 100.0, true));
        roomCatalog.add(new DoubleRoom(102, 150.0, true));
        roomCatalog.add(new SuiteRoom(103, 300.0, true));

        // ---------------------------------------------------------------
        // Step 3: Simulate a prior booking — all Double rooms fully booked
        // This ensures UC4 demonstrates filtering of unavailable types
        // ---------------------------------------------------------------
        System.out.println("Simulating bookings (all Double rooms booked)...");
        inventory.updateAvailability("Double", -3); // removes all 3 Double rooms
        System.out.println();

        // ---------------------------------------------------------------
        // Step 4: Guest initiates room search via SearchService
        // SearchService uses read-only access — inventory is NOT modified
        // ---------------------------------------------------------------
        System.out.println("Guest Action: Search for available rooms");
        System.out.println();

        SearchService searchService = new SearchService(inventory, roomCatalog);
        searchService.searchAvailableRooms();

        // ---------------------------------------------------------------
        // Step 5: Verify inventory is unchanged after search
        // ---------------------------------------------------------------
        System.out.println();
        System.out.println("Inventory State After Search (must be unchanged):");
        inventory.displayInventory();
        System.out.println("--------------------------------------------------");
        System.out.println("Use Case 4 Complete. System state was preserved.");
    }
}