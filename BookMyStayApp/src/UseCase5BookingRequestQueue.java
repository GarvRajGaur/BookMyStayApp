
/**
 * MAIN CLASS UseCase5BookingRequestQueue
 *
 * Use Case 5: Booking Request (First-Come-First-Served)
 *
 * Description:
 * This class demonstrates fair intake of multiple booking requests
 * using a Queue data structure. Requests arrive from different guests
 * and are stored in strict arrival order (FIFO).
 *
 * No room allocation or inventory update occurs in this use case.
 * The focus is entirely on orderly request intake — preparing the
 * system for the allocation phase introduced in Use Case 6.
 *
 * Flow Demonstrated:
 * 1. Multiple guests submit booking requests simultaneously
 * 2. Each request is enqueued in arrival order
 * 3. Queue contents are displayed to confirm FIFO ordering
 * 4. Next-to-be-processed request is previewed via peek
 * 5. System state (inventory) remains completely unchanged
 *
 * Why this matters:
 * Without a queue, simultaneous requests during peak demand could be
 * handled in an arbitrary or unfair order. The Queue guarantees that
 * the earliest request is always processed first.
 *
 * @author GARV RAJ
 * @version 5.0
 */
public class UseCase5BookingRequestQueue {

    public static void main(String[] args) {

        System.out.println("=================================================");
        System.out.println("   BookMyStay Hotel Booking System               ");
        System.out.println("   Use Case 5: Booking Request (FIFO Queue)      ");
        System.out.println("=================================================");
        System.out.println();

        // ---------------------------------------------------------------
        // Step 1: Initialize the booking request queue
        // ---------------------------------------------------------------
        BookingRequestQueue bookingQueue = new BookingRequestQueue();

        System.out.println("Booking queue initialized. Awaiting guest requests...");
        System.out.println("--------------------------------------------------");
        System.out.println();

        // ---------------------------------------------------------------
        // Step 2: Guests submit booking requests (simulating peak demand)
        // Each request is timestamped by its position in the queue.
        // The Queue preserves this arrival order automatically — no
        // sorting or timestamp comparison needed.
        // ---------------------------------------------------------------
        System.out.println("Incoming Booking Requests:");
        System.out.println("--------------------------------------------------");

        bookingQueue.addRequest(new Reservation("Alice Johnson", "Single"));
        bookingQueue.addRequest(new Reservation("Bob Smith",    "Double"));
        bookingQueue.addRequest(new Reservation("Carol White",  "Suite"));
        bookingQueue.addRequest(new Reservation("David Brown",  "Single"));
        bookingQueue.addRequest(new Reservation("Eva Green",    "Double"));

        System.out.println("--------------------------------------------------");
        System.out.println("Total requests received: " + bookingQueue.getQueueSize());
        System.out.println();

        // ---------------------------------------------------------------
        // Step 3: Display all queued requests in arrival order
        // This confirms FIFO — Alice's request remains at position [1]
        // ---------------------------------------------------------------
        System.out.println("--------------------------------------------------");
        bookingQueue.displayQueue();
        System.out.println("--------------------------------------------------");
        System.out.println();

        // ---------------------------------------------------------------
        // Step 4: Peek at the next request to be processed
        // peek() reads the head without removing it — queue unchanged
        // ---------------------------------------------------------------
        Reservation nextUp = bookingQueue.peekNextRequest();
        if (nextUp != null) {
            System.out.println("Next request to be processed (peek): " + nextUp);
            System.out.println("Queue size after peek: " + bookingQueue.getQueueSize()
                    + " (unchanged — peek does not remove)");
        }
        System.out.println();

        // ---------------------------------------------------------------
        // Step 5: Confirm no inventory was touched
        // ---------------------------------------------------------------
        System.out.println("--------------------------------------------------");
        System.out.println("Inventory Status: NOT MODIFIED");
        System.out.println("All " + bookingQueue.getQueueSize()
                + " requests are queued and awaiting allocation (Use Case 6).");
        System.out.println("--------------------------------------------------");
        System.out.println("Use Case 5 Complete.");
        System.out.println("=================================================");
    }
}