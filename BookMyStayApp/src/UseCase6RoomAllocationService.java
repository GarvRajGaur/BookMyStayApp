/**
 * MAIN CLASS UseCase6RoomAllocationService
 *
 * Use Case 6: Reservation Confirmation & Room Allocation
 *
 * Description:
 * This class demonstrates the full booking lifecycle — from queued request
 * to confirmed reservation with a uniquely assigned room ID.
 *
 * It builds directly on Use Case 5 (FIFO queue) and Use Case 3 (inventory),
 * now adding:
 *   - AllocationTracker: HashMap<String, Set<String>> to prevent double-booking
 *   - BookingService:    atomic allocation — ID registration + inventory update
 *                        happen together as one logical unit
 *
 * Scenario Demonstrated:
 * - 5 guests submit requests (2 Single, 2 Double, 1 Suite)
 * - Inventory has limited stock (2 Single, 1 Double, 1 Suite)
 * - Requests are processed in strict FIFO order
 * - One Double request is declined (inventory exhausted mid-queue)
 * - Allocated room IDs are unique — no duplicates possible
 * - Final state of inventory and allocations is reported
 *
 * @author GARV RAJ
 * @version 6.0
 */
public class UseCase6RoomAllocationService {

    public static void main(String[] args) {

        System.out.println("=================================================");
        System.out.println("   BookMyStay Hotel Booking System               ");
        System.out.println("   Use Case 6: Reservation Confirmation &        ");
        System.out.println("              Room Allocation                    ");
        System.out.println("=================================================");
        System.out.println();

        // ---------------------------------------------------------------
        // Step 1: Initialize inventory with controlled stock levels
        // Intentionally limited to demonstrate decline scenario
        // ---------------------------------------------------------------
        RoomInventory inventory = new RoomInventory();

        // Override defaults to create a realistic scarcity scenario:
        // Start from a fresh state — book down to controlled counts
        inventory.updateAvailability("Single", -3); // 5 → 2
        inventory.updateAvailability("Double", -2); // 3 → 1
        inventory.updateAvailability("Suite",  -1); // 2 → 1

        System.out.println();
        System.out.println("Inventory at start of allocation:");
        inventory.displayInventory();
        System.out.println();

        // ---------------------------------------------------------------
        // Step 2: Build the FIFO booking request queue (from Use Case 5)
        // 5 requests — the 2nd Double request will be declined (only 1 left)
        // ---------------------------------------------------------------
        BookingRequestQueue requestQueue = new BookingRequestQueue();

        System.out.println("--------------------------------------------------");
        System.out.println("Incoming Booking Requests:");
        System.out.println("--------------------------------------------------");
        requestQueue.addRequest(new Reservation("Alice Johnson", "Single"));
        requestQueue.addRequest(new Reservation("Bob Smith",    "Double"));
        requestQueue.addRequest(new Reservation("Carol White",  "Suite"));
        requestQueue.addRequest(new Reservation("David Brown",  "Single"));
        requestQueue.addRequest(new Reservation("Eva Green",    "Double")); // will be declined

        System.out.println();
        System.out.println("Queue snapshot before processing:");
        requestQueue.displayQueue();
        System.out.println();

        // ---------------------------------------------------------------
        // Step 3: Initialize AllocationTracker
        // HashMap<String, Set<String>> — groups room IDs by type,
        // Set enforces uniqueness — no room ID can appear twice
        // ---------------------------------------------------------------
        AllocationTracker tracker = new AllocationTracker();

        // ---------------------------------------------------------------
        // Step 4: BookingService processes all requests
        // Each request: dequeue → availability check → ID generation
        //               → Set registration → inventory decrement → confirm
        // ---------------------------------------------------------------
        BookingService bookingService = new BookingService(requestQueue, inventory, tracker);
        bookingService.processAllRequests();

        // ---------------------------------------------------------------
        // Step 5: Post-processing reports
        // ---------------------------------------------------------------
        System.out.println();
        System.out.println("--------------------------------------------------");
        System.out.println("Final Inventory State (after all allocations):");
        inventory.displayInventory();

        System.out.println();
        System.out.println("--------------------------------------------------");
        System.out.println("Allocation Report:");
        tracker.displayAllocations();
        System.out.println("Total rooms allocated: " + tracker.getTotalAllocated());

        System.out.println("--------------------------------------------------");
        System.out.println("Use Case 6 Complete.");
        System.out.println("Double-booking prevented. Inventory synchronized.");
        System.out.println("=================================================");
    }
}