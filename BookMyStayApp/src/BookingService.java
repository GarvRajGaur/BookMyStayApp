import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * BookingService
 *
 * Use Case 6: Reservation Confirmation & Room Allocation
 *
 * Processes queued booking requests in FIFO order and performs
 * room allocation as a single atomic logical operation:
 *
 *   1. Dequeue the next Reservation from the request queue
 *   2. Check inventory for availability
 *   3. Generate a unique room ID
 *   4. Register the ID in AllocationTracker (Set prevents reuse)
 *   5. Decrement inventory immediately
 *   6. Confirm the reservation
 *
 * Why atomic operations matter:
 * If step 5 (inventory decrement) were separated from step 4 (ID recording),
 * a second request could read the same availability count before the first
 * request finished — resulting in two guests assigned to the same room.
 * Performing both steps together eliminates this window of inconsistency.
 *
 * @author GARV RAJ
 * @version 6.0
 */
public class BookingService {

    private BookingRequestQueue requestQueue;
    private RoomInventory       inventory;
    private AllocationTracker   tracker;

    // Counter used to generate sequential room IDs per type
    private Map<String, Integer> roomIdCounters;

    /**
     * Constructs the BookingService with shared references to the
     * request queue, inventory, and allocation tracker.
     *
     * @param requestQueue the FIFO queue of pending booking requests
     * @param inventory    the centralized room inventory
     * @param tracker      the allocation tracker that enforces uniqueness
     */
    public BookingService(BookingRequestQueue requestQueue,
                          RoomInventory inventory,
                          AllocationTracker tracker) {
        this.requestQueue = requestQueue;
        this.inventory    = inventory;
        this.tracker      = tracker;

        // Initialize per-type room number counters
        roomIdCounters = new java.util.HashMap<>();
        roomIdCounters.put("Single", 101);
        roomIdCounters.put("Double", 201);
        roomIdCounters.put("Suite",  301);
    }

    /**
     * Processes all queued booking requests in FIFO order.
     *
     * Each request is handled as one atomic logical unit:
     * availability check → ID generation → ID registration → inventory update → confirmation.
     * If any step fails, the reservation is declined and the queue moves on.
     */
    public void processAllRequests() {
        System.out.println("Processing all queued booking requests (FIFO order)...");
        System.out.println("==================================================");

        while (!requestQueue.isEmpty()) {
            processSingleRequest();
        }

        System.out.println("==================================================");
        System.out.println("All requests have been processed.");
    }

    /**
     * Dequeues and processes exactly one booking request.
     *
     * Steps performed atomically:
     * 1. Dequeue next Reservation (FIFO — oldest request first)
     * 2. Read current availability from inventory (read-only check)
     * 3. If unavailable → decline immediately, no state change
     * 4. If available  → generate unique room ID
     * 5. Register ID in AllocationTracker (Set blocks any duplicate)
     * 6. Decrement inventory count (synchronized with allocation)
     * 7. Print confirmation with assigned room ID
     */
    public void processSingleRequest() {
        // Step 1: Dequeue — respects FIFO from UC5
        Reservation reservation = requestQueue.dequeueRequest();
        if (reservation == null) {
            System.out.println("[QUEUE EMPTY] No requests to process.");
            return;
        }

        String guestName = reservation.getGuestName();
        String roomType  = reservation.getRoomType();

        System.out.println("\nProcessing: " + reservation);

        // Step 2: Check availability — read-only access to inventory
        Map<String, Integer> availability = inventory.getAvailabilityMap();
        Integer count = availability.get(roomType);

        // Step 3: Decline if unavailable
        if (count == null || count <= 0) {
            System.out.println("  [DECLINED]  No " + roomType
                    + " rooms available for guest: " + guestName);
            return;
        }

        // Step 4: Generate a unique room ID using per-type counter
        String roomId = generateRoomId(roomType);

        // Step 5: Register in AllocationTracker — Set prevents reuse
        boolean registered = tracker.allocate(roomType, roomId);
        if (!registered) {
            // This should never happen with sequential ID generation,
            // but defensive programming catches any edge case
            System.out.println("  [ERROR]     Room ID " + roomId
                    + " already allocated. Skipping to prevent double-booking.");
            return;
        }

        // Step 6: Decrement inventory immediately — atomic with registration
        inventory.updateAvailability(roomType, -1);

        // Step 7: Confirm the reservation
        System.out.println("  [CONFIRMED] Guest: " + guestName
                + " | Room Type: " + roomType
                + " | Assigned Room ID: " + roomId);
    }

    /**
     * Generates the next sequential room ID for a given room type.
     * Format: "TYPE-NUMBER" (e.g., "SINGLE-101", "DOUBLE-201", "SUITE-301")
     *
     * The counter increments after each allocation, ensuring no two
     * rooms of the same type ever share an ID within this session.
     *
     * @param roomType the type of room being allocated
     * @return a new unique room ID string
     */
    private String generateRoomId(String roomType) {
        int number = roomIdCounters.getOrDefault(roomType, 100);
        roomIdCounters.put(roomType, number + 1);
        return roomType.toUpperCase() + "-" + number;
    }
}