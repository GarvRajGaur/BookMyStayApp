import java.util.LinkedList;
import java.util.Queue;

/**
 * BookingRequestQueue
 *
 * Use Case 5: Booking Request (First-Come-First-Served)
 *
 * Manages incoming booking requests using a Queue data structure.
 * Requests are stored in the exact order they are received, enforcing
 * the FIFO (First-Come-First-Served) principle.
 *
 * Responsibilities:
 * - Accept and enqueue incoming Reservation requests
 * - Preserve arrival order automatically via Queue semantics
 * - Provide queue inspection (size, peek, display) without processing
 * - Ensure NO inventory mutation occurs at this stage
 *
 * Why Queue over List or Array?
 * A Queue models a real-world waiting line. Unlike a List, it enforces
 * access discipline — elements are added at the tail and read from the
 * head. This prevents any request from being skipped or reordered,
 * which is critical for fairness during peak booking demand.
 *
 * @author GARV RAJ
 * @version 5.0
 */
public class BookingRequestQueue {

    // LinkedList implements Queue — preserves insertion order (FIFO)
    private Queue<Reservation> requestQueue;

    /**
     * Initializes an empty booking request queue.
     */
    public BookingRequestQueue() {
        requestQueue = new LinkedList<>();
    }

    /**
     * Accepts a guest's booking request and adds it to the end of the queue.
     * This simulates a guest submitting a request at a specific moment in time.
     * The queue automatically preserves arrival order — no manual sorting needed.
     *
     * @param reservation the guest's booking request to enqueue
     */
    public void addRequest(Reservation reservation) {
        requestQueue.offer(reservation);
        System.out.println("Request received: " + reservation);
    }

    /**
     * Displays all pending requests in the queue in arrival order (FIFO).
     * This is a read-only inspection — the queue is not modified.
     *
     * Demonstrates: Request Ordering — the queue preserves insertion order,
     * so the first request added always appears first.
     */
    public void displayQueue() {
        if (requestQueue.isEmpty()) {
            System.out.println("Booking queue is empty. No pending requests.");
            return;
        }

        System.out.println("Pending Booking Requests (in arrival order):");
        int position = 1;
        for (Reservation r : requestQueue) {
            System.out.println("  [" + position + "] " + r);
            position++;
        }
    }

    /**
     * Returns the number of requests currently waiting in the queue.
     *
     * @return total pending request count
     */
    public int getQueueSize() {
        return requestQueue.size();
    }

    /**
     * Returns (but does NOT remove) the next request to be processed.
     * Useful for inspection before allocation begins.
     *
     * @return the head Reservation, or null if queue is empty
     */
    public Reservation peekNextRequest() {
        return requestQueue.peek();
    }

    /**
     * Indicates whether the queue has any pending requests.
     *
     * @return true if queue is empty, false otherwise
     */
    public boolean isEmpty() {
        return requestQueue.isEmpty();
    }
}