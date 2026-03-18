import java.util.Map;

/**
 * BookingService
 *
 * Use Case 6 + 8: Reservation Confirmation, Room Allocation & History Recording
 *
 * Processes queued booking requests in FIFO order and performs
 * room allocation as a single atomic logical operation:
 *
 *   1. Dequeue the next Reservation from the request queue
 *   2. Check inventory for availability
 *   3. Generate a unique room ID
 *   4. Register the ID in AllocationTracker (Set prevents reuse)
 *   5. Decrement inventory immediately
 *   6. Record confirmed reservation in BookingHistory (UC8)
 *   7. Confirm the reservation
 *
 * @author GARV RAJ
 * @version 8.0
 */
public class BookingService {

    private BookingRequestQueue requestQueue;
    private RoomInventory       inventory;
    private AllocationTracker   tracker;
    private BookingHistory      bookingHistory; // UC8 — optional, null-safe

    private Map<String, Integer> roomIdCounters;

    /**
     * UC1-7 constructor — no history recording.
     */
    public BookingService(BookingRequestQueue requestQueue,
                          RoomInventory inventory,
                          AllocationTracker tracker) {
        this(requestQueue, inventory, tracker, null);
    }

    /**
     * UC8 constructor — with history recording enabled.
     *
     * @param requestQueue   the FIFO queue of pending booking requests
     * @param inventory      the centralized room inventory
     * @param tracker        the allocation tracker that enforces uniqueness
     * @param bookingHistory the history log to record confirmed reservations
     */
    public BookingService(BookingRequestQueue requestQueue,
                          RoomInventory inventory,
                          AllocationTracker tracker,
                          BookingHistory bookingHistory) {
        this.requestQueue   = requestQueue;
        this.inventory      = inventory;
        this.tracker        = tracker;
        this.bookingHistory = bookingHistory;

        roomIdCounters = new java.util.HashMap<>();
        roomIdCounters.put("Single", 101);
        roomIdCounters.put("Double", 201);
        roomIdCounters.put("Suite",  301);
    }

    /**
     * Processes all queued booking requests in FIFO order.
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
     * 1. Dequeue next Reservation (FIFO)
     * 2. Read availability (read-only)
     * 3. Decline if unavailable
     * 4. Generate unique room ID
     * 5. Register in AllocationTracker (Set blocks duplicates)
     * 6. Decrement inventory (atomic with step 5)
     * 7. Record in BookingHistory (UC8 — if history provided)
     * 8. Print confirmation
     */
    public void processSingleRequest() {
        // Step 1: Dequeue — FIFO order from UC5
        Reservation reservation = requestQueue.dequeueRequest();
        if (reservation == null) {
            System.out.println("[QUEUE EMPTY] No requests to process.");
            return;
        }

        String guestName = reservation.getGuestName();
        String roomType  = reservation.getRoomType();

        System.out.println("\nProcessing: " + reservation);

        // Step 2: Check availability — read-only
        Map<String, Integer> availability = inventory.getAvailabilityMap();
        Integer count = availability.get(roomType);

        // Step 3: Decline if unavailable
        if (count == null || count <= 0) {
            System.out.println("  [DECLINED]  No " + roomType
                    + " rooms available for guest: " + guestName);
            return;
        }

        // Step 4: Generate unique room ID
        String roomId = generateRoomId(roomType);

        // Step 5: Register in AllocationTracker — Set prevents reuse
        boolean registered = tracker.allocate(roomType, roomId);
        if (!registered) {
            System.out.println("  [ERROR]     Room ID " + roomId
                    + " already allocated. Skipping to prevent double-booking.");
            return;
        }

        // Step 6: Decrement inventory — atomic with registration
        inventory.updateAvailability(roomType, -1);

        // Step 7: Record in BookingHistory (UC8)
        // Null-safe — works for both UC6 (no history) and UC8 (with history)
        if (bookingHistory != null) {
            bookingHistory.addRecord(guestName, roomType, roomId);
        }

        // Step 8: Confirm
        System.out.println("  [CONFIRMED] Guest: " + guestName
                + " | Room Type: " + roomType
                + " | Assigned Room ID: " + roomId);
    }

    /**
     * Generates the next sequential room ID for the given room type.
     * Format: TYPE-NUMBER (e.g., SINGLE-101, DOUBLE-201, SUITE-301)
     */
    private String generateRoomId(String roomType) {
        int number = roomIdCounters.getOrDefault(roomType, 100);
        roomIdCounters.put(roomType, number + 1);
        return roomType.toUpperCase() + "-" + number;
    }
}