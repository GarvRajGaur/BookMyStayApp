import java.util.LinkedList;
import java.util.Queue;

/**
 * SharedBookingQueue
 *
 * Use Case 11: Concurrent Booking Simulation (Thread Safety)
 *
 * A thread-safe wrapper around a Queue<Reservation> that allows
 * multiple threads to safely enqueue and dequeue booking requests
 * without race conditions.
 *
 * Why is synchronization needed here?
 * Without synchronization, two threads could both call poll() at the
 * same moment and receive the SAME Reservation — processing it twice
 * and potentially allocating the same room to two different guests.
 *
 * How it works:
 * The 'synchronized' keyword on each method ensures that only ONE
 * thread at a time can execute that method on this object. Every
 * other thread attempting to enter any synchronized method is blocked
 * (put in the object's WAIT state) until the current thread exits.
 *
 * This object acts as the CRITICAL SECTION GUARD for the shared queue.
 *
 * Design note — why not use ConcurrentLinkedQueue directly?
 * This class makes the synchronization intent explicit and educational.
 * It also allows bundling isEmpty() + poll() as an atomic peek-and-take
 * operation (safeDequeue), which a bare ConcurrentLinkedQueue does not.
 *
 * @author GARV RAJ
 * @version 11.0
 */
public class SharedBookingQueue {

    // The underlying queue — shared across all threads
    private final Queue<Reservation> queue;

    /**
     * Initializes an empty shared booking queue.
     */
    public SharedBookingQueue() {
        queue = new LinkedList<>();
    }

    /**
     * Thread-safely adds a reservation to the end of the queue.
     *
     * 'synchronized' guarantees only one thread enqueues at a time.
     * Without this, two threads adding simultaneously could corrupt
     * the internal LinkedList node pointers.
     *
     * @param reservation the booking request to enqueue
     */
    public synchronized void enqueue(Reservation reservation) {
        queue.offer(reservation);
    }

    /**
     * Thread-safely removes and returns the next reservation (FIFO).
     *
     * This is an atomic check-and-take:
     * Both isEmpty() and poll() happen inside one synchronized block,
     * preventing a "time-of-check to time-of-use" (TOCTOU) race where:
     *   Thread A checks isEmpty() → false
     *   Thread B checks isEmpty() → false  (same item!)
     *   Thread A calls poll()     → gets the item
     *   Thread B calls poll()     → gets null (item already taken)
     *
     * With synchronization, only ONE thread can enter this method
     * at a time, eliminating the TOCTOU window entirely.
     *
     * @return next Reservation, or null if the queue is empty
     */
    public synchronized Reservation safeDequeue() {
        if (queue.isEmpty()) return null;
        return queue.poll();
    }

    /**
     * Thread-safely checks whether the queue is empty.
     *
     * @return true if no requests are pending
     */
    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * Thread-safely returns the current number of pending requests.
     *
     * @return queue size
     */
    public synchronized int size() {
        return queue.size();
    }
}