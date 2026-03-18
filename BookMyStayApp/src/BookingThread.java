import java.util.List;

/**
 * BookingThread
 *
 * Use Case 11: Concurrent Booking Simulation (Thread Safety)
 *
 * Implements Runnable to represent a single guest's booking attempt
 * running on its own thread. Multiple BookingThread instances run
 * concurrently, all competing to read from the same SharedBookingQueue
 * and write to the same SynchronizedInventory.
 *
 * Thread Lifecycle:
 *   1. Thread starts → run() is called by the JVM
 *   2. Thread calls sharedQueue.safeDequeue() — synchronized, FIFO
 *   3. Thread calls inventory.checkAndAllocate() — synchronized, atomic
 *   4. If allocated → generates room ID, logs result to shared list
 *   5. If declined  → logs decline, no state change
 *   6. Thread finishes → run() returns, thread terminates
 *
 * Why Runnable over extending Thread?
 * Implementing Runnable separates the task (booking logic) from the
 * execution mechanism (Thread). This follows the principle of
 * composition over inheritance and allows the same Runnable to be
 * submitted to an ExecutorService in future use cases.
 *
 * Shared result logging:
 * Results are added to a synchronized List passed in from the main class.
 * This avoids interleaved console output from competing threads making
 * the output unreadable.
 *
 * @author GARV RAJ
 * @version 11.0
 */
public class BookingThread implements Runnable {

    private final String                 threadName;
    private final SharedBookingQueue     sharedQueue;
    private final SynchronizedInventory  inventory;
    private final List<String>           resultLog;

    // Per-type room number counters — shared across all threads via synchronized block
    private static final java.util.Map<String, Integer> roomCounters
            = new java.util.HashMap<>();

    static {
        roomCounters.put("Single", 101);
        roomCounters.put("Double", 201);
        roomCounters.put("Suite",  301);
    }

    /**
     * Constructs a BookingThread with references to all shared resources.
     *
     * @param threadName  identifier for this thread (e.g., "Thread-1")
     * @param sharedQueue the shared booking request queue
     * @param inventory   the synchronized room inventory
     * @param resultLog   shared list to collect results for ordered display
     */
    public BookingThread(String threadName,
                         SharedBookingQueue sharedQueue,
                         SynchronizedInventory inventory,
                         List<String> resultLog) {
        this.threadName  = threadName;
        this.sharedQueue = sharedQueue;
        this.inventory   = inventory;
        this.resultLog   = resultLog;
    }

    /**
     * Main thread execution logic.
     *
     * Each thread attempts to dequeue one booking request and process it.
     * All shared-resource access is protected by synchronization in
     * SharedBookingQueue and SynchronizedInventory.
     *
     * A small sleep is introduced between dequeue and allocation to
     * amplify the window where a race condition WOULD occur without
     * synchronization — proving the synchronized approach prevents it.
     */
    @Override
    public void run() {
        // Step 1: Thread-safely dequeue the next reservation (FIFO)
        Reservation reservation = sharedQueue.safeDequeue();

        if (reservation == null) {
            synchronized (resultLog) {
                resultLog.add("[" + threadName + "] No reservation available in queue.");
            }
            return;
        }

        String guestName = reservation.getGuestName();
        String roomType  = reservation.getRoomType();

        synchronized (resultLog) {
            resultLog.add("[" + threadName + "] Dequeued request → Guest: "
                    + guestName + " | Room Type: " + roomType);
        }

        // Small sleep to amplify the concurrency window
        // Without synchronized inventory, a race condition is highly likely here
        try { Thread.sleep(10); } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Step 2: Atomically check availability AND decrement (critical section)
        boolean allocated = inventory.checkAndAllocate(roomType);

        if (allocated) {
            // Step 3: Generate a unique room ID — synchronized to prevent ID collisions
            String roomId = generateRoomId(roomType);
            synchronized (resultLog) {
                resultLog.add("[" + threadName + "] [CONFIRMED] Guest: "
                        + guestName + " | Room: " + roomId);
            }
        } else {
            synchronized (resultLog) {
                resultLog.add("[" + threadName + "] [DECLINED]  Guest: "
                        + guestName + " | No " + roomType + " rooms available.");
            }
        }
    }

    /**
     * Generates the next sequential room ID for the given room type.
     * Synchronized on roomCounters to prevent two threads generating the same ID.
     *
     * @param roomType the room type being allocated
     * @return unique room ID string
     */
    private static synchronized String generateRoomId(String roomType) {
        int number = roomCounters.getOrDefault(roomType, 100);
        roomCounters.put(roomType, number + 1);
        return roomType.toUpperCase() + "-" + number;
    }
}